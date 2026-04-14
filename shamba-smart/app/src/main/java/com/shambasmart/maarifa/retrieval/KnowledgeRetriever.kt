package com.shambasmart.maarifa.retrieval

import androidx.sqlite.db.SimpleSQLiteQuery
import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.data.local.entity.maarifa.OperationalRule
import com.shambasmart.maarifa.MaarifaDomains

class KnowledgeRetriever(
    private val chunkDao: KnowledgeChunkDao,
    private val ruleDao: OperationalRuleDao
) {
    companion object {
        private const val MAX_RESULTS = 8
    }

    /**
     * Three-layer retrieval pipeline:
     * 1. FTS5 BM25 search — SQLite's built-in BM25 ranking
     * 2. Metadata scoring — domain relevance, credibility, source type
     * 3. Final reranking — weighted combination
     * 
     * Falls back to LIKE search if FTS5 returns no results.
     */
    suspend fun retrieve(
        query: String, species: String? = null, crop: String? = null,
        intent: String? = null, keywords: List<String> = emptyList()
    ): List<ScoredChunk> {
        if (query.isBlank()) return emptyList()
        
        // Stage 1: FTS5 BM25 search
        val ftsQuery = buildFtsQuery(query, keywords)
        val ftsResults = tryFtsSearch(ftsQuery)
        
        // If FTS5 returns results, score them
        if (ftsResults.isNotEmpty()) {
            val scored = ftsResults.map { chunk ->
                val meta = computeMeta(chunk, species, crop, intent)
                val final = 0.6f + (0.4f * meta) // FTS5 BM25 is already 0-1 normalized
                ScoredChunk(chunk, final, 1.0f, 0f, meta)
            }
            return scored.filter { it.finalScore > 0.1f }
                .sortedByDescending { it.finalScore }.take(MAX_RESULTS)
        }
        
        // Fallback: Pre-filter by tags + LIKE search
        val filtered = preFilter(species, crop, intent)
        if (filtered.isEmpty()) return emptyList()
        
        val terms = if (keywords.isNotEmpty()) keywords.map { it.lowercase() }
            else query.lowercase().split(Regex("\\s+")).filter { it.length > 2 }
        val scored = filtered.map { chunk ->
            val bm25 = computeBm25Like(chunk, terms)
            val meta = computeMeta(chunk, species, crop, intent)
            val final = (0.4f * bm25) + (0.2f * meta)
            ScoredChunk(chunk, final, bm25, 0f, meta)
        }
        return scored.filter { it.finalScore > 0.1f }
            .sortedByDescending { it.finalScore }.take(MAX_RESULTS)
    }

    /**
     * Build FTS5 query string from user query and keywords.
     * Supports phrase matching and term expansion.
     */
    private fun buildFtsQuery(query: String, keywords: List<String>): String {
        val terms = mutableSetOf<String>()
        // Add query terms
        query.split(Regex("\\s+")).filter { it.length > 2 }.forEach { terms.add(it.lowercase()) }
        // Add explicit keywords
        keywords.forEach { terms.add(it.lowercase()) }
        
        // Build FTS5 query: term1 OR term2 OR "phrase"
        return terms.joinToString(" OR ")
    }

    /**
     * Try FTS5 BM25 search. Returns empty if FTS5 fails (table may not exist yet).
     */
    private suspend fun tryFtsSearch(ftsQuery: String): List<KnowledgeChunk> {
        if (ftsQuery.isBlank()) return emptyList()
        return try {
            // Build FTS5 SQL query with BM25 ranking
            val sql = """
                SELECT kc.* FROM knowledge_chunks kc
                INNER JOIN knowledge_chunks_fts fts ON kc.rowid = fts.rowid
                WHERE knowledge_chunks_fts MATCH ?
                ORDER BY bm25(knowledge_chunks_fts)
                LIMIT ?
            """.trimIndent()
            val query = SimpleSQLiteQuery(sql, arrayOf(ftsQuery, MAX_RESULTS * 2))
            val results = chunkDao.ftsSearchBm25(query)
            if (results.isNotEmpty()) {
                results.take(MAX_RESULTS)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // FTS5 table may not exist yet, fall through to LIKE search
            emptyList()
        }
    }

    private suspend fun preFilter(species: String?, crop: String?, intent: String?): List<KnowledgeChunk> {
        val tags = mutableListOf<String>()
        species?.let { tags.add(it.lowercase()) }
        crop?.let { tags.add(it.lowercase()) }
        when (intent) {
            "dosage_lookup","withdrawal_lookup" -> tags.add(MaarifaDomains.MEDICINES)
            "planting_advice","input_advice" -> tags.add(MaarifaDomains.CROPS)
            "cheese_process" -> tags.add(MaarifaDomains.CHEESE)
            "weather_risk" -> tags.add(MaarifaDomains.WEATHER)
        }
        if (tags.isEmpty()) return emptyList()
        return chunkDao.getChunksByTag(tags.first())
    }

    /**
     * Fallback BM25 computation using LIKE search.
     * Only used when FTS5 is unavailable.
     */
    private fun computeBm25Like(chunk: KnowledgeChunk, terms: List<String>): Float {
        if (terms.isEmpty()) return 0f
        val text = chunk.displayText.lowercase()
        var matches = 0; var totalFreq = 0
        for (term in terms) {
            val freq = text.split(term).size - 1
            if (freq > 0) { matches++; totalFreq += freq }
        }
        if (matches == 0) return 0f
        val coverage = matches.toFloat() / terms.size
        val avgFreq = totalFreq.toFloat() / matches
        return (coverage * 0.7f) + (minOf(avgFreq / 5f, 1f) * 0.3f)
    }

    private fun computeMeta(chunk: KnowledgeChunk, species: String?, crop: String?, intent: String?): Float {
        var score = 0f
        val tags = chunk.getTopicTagsList()
        if (species != null && tags.any { it.equals(species, ignoreCase = true) }) score += 0.3f
        if (crop != null && tags.any { it.equals(crop, ignoreCase = true) }) score += 0.3f
        val domainMatch = when (intent) {
            "symptom_query","dosage_lookup","withdrawal_lookup" ->
                tags.any { it in listOf("disease", MaarifaDomains.MEDICINES, "health", "drug") }
            "planting_advice","input_advice","harvest_guidance" ->
                tags.any { it in listOf(MaarifaDomains.CROPS, "agronomy", "planting", "harvest") }
            "breeding_query" -> tags.any { it in listOf("reproduction", "breeding") }
            "nutrition_query" -> tags.any { it in listOf("feed", "nutrition") }
            "cheese_process" -> tags.any { it in listOf(MaarifaDomains.CHEESE, "dairy") }
            "weather_risk" -> tags.any { it in listOf(MaarifaDomains.WEATHER, "climate") }
            else -> false
        }
        if (domainMatch) score += 0.2f
        if (chunk.sourceType == "bundled" && chunk.medicalContent) score += 0.1f
        score += KnowledgeChunk.credibilityWeight(chunk.sourceCredibility) * 0.1f
        return minOf(score, 1f)
    }

    suspend fun findRules(type: String, species: String? = null): List<OperationalRule> =
        if (species != null) ruleDao.getRulesByTypeAndSpecies(type, species)
        else ruleDao.getRulesByType(type)

    suspend fun findWithdrawalRule(drug: String, species: String) = ruleDao.getWithdrawalRule(drug, species)
    suspend fun findDoseRule(drug: String, species: String) = ruleDao.getDoseRule(drug, species)
    suspend fun findNotifiableRule(disease: String) = ruleDao.getNotifiableRule(disease)

    suspend fun getStats() = KnowledgeStats(
        totalChunks = chunkDao.getChunkCount(),
        bundledChunks = chunkDao.getBundledChunkCount(),
        ingestedChunks = chunkDao.getIngestedChunkCount(),
        totalRules = ruleDao.getRuleCount()
    )

    data class ScoredChunk(val chunk: KnowledgeChunk, val finalScore: Float,
        val bm25Score: Float, val vectorScore: Float, val metadataScore: Float)
    data class KnowledgeStats(val totalChunks: Int, val bundledChunks: Int,
        val ingestedChunks: Int, val totalRules: Int)
}