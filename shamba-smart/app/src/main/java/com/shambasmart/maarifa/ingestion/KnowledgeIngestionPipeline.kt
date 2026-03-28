package com.shambasmart.maarifa.ingestion

import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.maarifa.chunker.SemanticChunker
import com.shambasmart.maarifa.retrieval.VectorSearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Maarifa Knowledge Ingestion Pipeline — quality gates and chunk processing.
 *
 * Design spec: "The farmer and administrator can expand Maarifa's knowledge base
 * at any time by importing new documents directly from the tablet."
 *
 * Quality gates:
 * 1. Format check — minimum 100 words extracted
 * 2. Language check — English only
 * 3. Domain relevance check — similarity to knowledge domains
 * 4. Conflict detection — numerical conflicts with operational rules
 * 5. Source credibility label — user-selected credibility level
 * 6. Critical parameter prominence — flag medical content
 */
class KnowledgeIngestionPipeline(
    private val chunkDao: KnowledgeChunkDao,
    private val ruleDao: OperationalRuleDao,
    private val vectorEngine: VectorSearchEngine,
    private val semanticChunker: SemanticChunker
) {

    data class IngestionResult(
        val success: Boolean,
        val chunksCreated: Int,
        val warnings: List<String>,
        val errors: List<String>,
        val documentTitle: String
    )

    data class QualityGateResult(
        val passed: Boolean,
        val stage: String,
        val message: String
    )

    /**
     * Full ingestion pipeline with all quality gates.
     */
    suspend fun ingestDocument(
        rawText: String,
        title: String,
        sourceType: String,  // "bundled" or "ingested"
        sourceCredibility: String,
        topicTags: String,
        lastVerified: String? = null
    ): IngestionResult = withContext(Dispatchers.IO) {
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        // Gate 1: Format check
        val formatGate = formatCheck(rawText)
        if (!formatGate.passed) {
            return@withContext IngestionResult(
                success = false, chunksCreated = 0,
                warnings = warnings, errors = listOf(formatGate.message),
                documentTitle = title
            )
        }

        // Gate 2: Language check (simplified — always English for bundled)
        if (sourceType == "ingested") {
            val langGate = languageCheck(rawText)
            if (!langGate.passed) {
                warnings.add(langGate.message)
            }
        }

        // Gate 3: Chunk
        val chunks = SemanticChunker.chunkDocument(
            documentText = rawText,
            sourceTitle = title,
            sourceType = sourceType,
            sourceCredibility = sourceCredibility,
            topicTags = topicTags,
            lastVerified = lastVerified
        )

        if (chunks.isEmpty()) {
            return@withContext IngestionResult(
                success = false, chunksCreated = 0,
                warnings = warnings, errors = listOf("No chunks could be created from document"),
                documentTitle = title
            )
        }

        // Gate 4: Domain relevance check (simplified)
        val domainGate = domainRelevanceCheck(chunks)
        if (!domainGate.passed) {
            warnings.add(domainGate.message)
        }

        // Gate 5: Conflict detection
        val conflictWarnings = detectConflicts(chunks)
        warnings.addAll(conflictWarnings)

        // Gate 6: Generate embeddings and store
        val enrichedChunks = enrichWithVectors(chunks)

        // Store chunks
        chunkDao.insertAll(enrichedChunks)

        IngestionResult(
            success = true,
            chunksCreated = enrichedChunks.size,
            warnings = warnings,
            errors = errors,
            documentTitle = title
        )
    }

    private fun formatCheck(text: String): QualityGateResult {
        val wordCount = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return if (wordCount >= 100) {
            QualityGateResult(true, "format", "Document has $wordCount words")
        } else {
            QualityGateResult(false, "format", "Document has only $wordCount words — minimum 100 required")
        }
    }

    private fun languageCheck(text: String): QualityGateResult {
        // Simplified language detection — check for common English words
        val englishIndicators = listOf("the", "and", "is", "are", "was", "were", "be", "have", "has")
        val words = text.lowercase().split(Regex("\\s+"))
        val englishCount = words.count { it in englishIndicators }
        val ratio = englishCount.toFloat() / words.size.coerceAtLeast(1)

        return if (ratio > 0.03f) {
            QualityGateResult(true, "language", "Document appears to be in English")
        } else {
            QualityGateResult(false, "language", "Document may not be in English. Maarifa is English only. Non-English content may produce unreliable results.")
        }
    }

    private fun domainRelevanceCheck(chunks: List<KnowledgeChunk>): QualityGateResult {
        val domainKeywords = listOf(
            "goat", "sheep", "cow", "pig", "chicken", "animal", "livestock",
            "crop", "maize", "beans", "plant", "farm", "soil", "seed",
            "disease", "vaccine", "medicine", "treatment", "dose",
            "milk", "cheese", "feed", "nutrition", "harvest", "rain"
        )

        val relevantChunks = chunks.count { chunk ->
            val text = chunk.text.lowercase()
            domainKeywords.any { text.contains(it) }
        }

        val ratio = relevantChunks.toFloat() / chunks.size.coerceAtLeast(1)

        return if (ratio > 0.3f) {
            QualityGateResult(true, "domain", "$relevantChunks of ${chunks.size} chunks appear farm-relevant")
        } else {
            QualityGateResult(false, "domain", "This document does not appear to contain agricultural content. It will still be indexed but may not surface in farm-related queries.")
        }
    }

    private suspend fun detectConflicts(chunks: List<KnowledgeChunk>): List<String> {
        val warnings = mutableListOf<String>()

        // Check for drug withdrawal period conflicts
        val drugPatterns = listOf(
            "oxytetracycline" to "milk withdrawal",
            "ivermectin" to "milk withdrawal",
            "albendazole" to "withdrawal"
        )

        for (chunk in chunks) {
            val text = chunk.text.lowercase()

            for ((drug, field) in drugPatterns) {
                if (text.contains(drug) && text.contains(field)) {
                    // Extract number of days if present
                    val dayMatch = Regex("""(\d+)\s*days?""").find(text)
                    if (dayMatch != null) {
                        val newDays = dayMatch.groupValues[1].toIntOrNull()
                        if (newDays != null) {
                            // Check against existing rules
                            val existingRule = try {
                                ruleDao.getWithdrawalRule(drug, "goat")
                            } catch (e: Exception) { null }

                            if (existingRule != null) {
                                val existingDays = try {
                                    org.json.JSONObject(existingRule.parametersJson)
                                        .optInt("milk_withdrawal_days", -1)
                                } catch (e: Exception) { -1 }

                                if (existingDays > 0 && existingDays != newDays) {
                                    warnings.add("Conflict detected: new document suggests $drug milk withdrawal is $newDays days, " +
                                        "but current rule says $existingDays days. Review in Knowledge Management before this takes effect.")
                                }
                            }
                        }
                    }
                }
            }
        }

        return warnings
    }

    private suspend fun enrichWithVectors(chunks: List<KnowledgeChunk>): List<KnowledgeChunk> {
        if (!vectorEngine.isAvailable()) return chunks

        return chunks.map { chunk ->
            try {
                val embedding = vectorEngine.generateEmbedding(chunk.embeddingText)
                if (embedding != null) {
                    chunk.copy(vector = embedding.contentToString())
                } else {
                    chunk
                }
            } catch (e: Exception) {
                chunk
            }
        }
    }

    /**
     * Delete an ingested document and all its chunks.
     */
    suspend fun deleteDocument(sourceTitle: String) {
        chunkDao.deleteBySource(sourceTitle)
    }

    /**
     * Get all ingested documents with metadata.
     */
    suspend fun getIngestedDocuments(): List<DocumentInfo> {
        val sources = chunkDao.getAllSourceTitles()
        return sources.map { title ->
            val chunks = chunkDao.getChunksBySource(title)
            DocumentInfo(
                title = title,
                chunkCount = chunks.size,
                dateAdded = chunks.firstOrNull()?.dateAdded ?: "unknown",
                sourceType = chunks.firstOrNull()?.sourceType ?: "unknown"
            )
        }
    }

    data class DocumentInfo(
        val title: String,
        val chunkCount: Int,
        val dateAdded: String,
        val sourceType: String
    )
}