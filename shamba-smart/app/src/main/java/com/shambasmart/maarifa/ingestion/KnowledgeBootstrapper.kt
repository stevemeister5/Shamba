package com.shambasmart.maarifa.ingestion

import android.content.Context
import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.data.local.entity.maarifa.OperationalRule
import com.shambasmart.maarifa.chunker.SemanticChunker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Maarifa Knowledge Bootstrapper — loads bundled knowledge base on first launch.
 *
 * Reads JSON files from assets/knowledge_base/ and populates Room database.
 * Only runs once on first launch (checks if bundled chunks already exist).
 *
 * Bundled knowledge sources:
 * - crops_maize.json
 * - livestock_goats.json
 * - medicines.json
 * - cheese.json
 * - weather_korogwe.json
 * - operational_rules.json
 */
class KnowledgeBootstrapper(
    private val context: Context,
    private val chunkDao: KnowledgeChunkDao,
    private val ruleDao: OperationalRuleDao,
    private val semanticChunker: SemanticChunker
) {

    private val knowledgeFiles = listOf(
        "knowledge_base/crops_maize.json",
        "knowledge_base/livestock_goats.json",
        "knowledge_base/medicines.json",
        "knowledge_base/cheese.json",
        "knowledge_base/weather_korogwe.json"
    )

    private val rulesFile = "knowledge_base/operational_rules.json"

    /**
     * Bootstrap all bundled knowledge. Call on first app launch.
     */
    suspend fun bootstrap() = withContext(Dispatchers.IO) {
        // Check if already bootstrapped
        val existingCount = chunkDao.getBundledChunkCount()
        if (existingCount > 0) {
            android.util.Log.d("KnowledgeBootstrapper", "Already bootstrapped with $existingCount bundled chunks")
            return@withContext
        }

        android.util.Log.d("KnowledgeBootstrapper", "Starting knowledge bootstrap...")

        // Load knowledge chunks
        var totalChunks = 0
        for (filePath in knowledgeFiles) {
            try {
                val chunks = loadChunksFromFile(filePath)
                chunkDao.insertAll(chunks)
                totalChunks += chunks.size
                android.util.Log.d("KnowledgeBootstrapper", "Loaded ${chunks.size} chunks from $filePath")
            } catch (e: Exception) {
                android.util.Log.e("KnowledgeBootstrapper", "Failed to load $filePath: ${e.message}")
            }
        }

        // Load operational rules
        try {
            val rules = loadRulesFromFile(rulesFile)
            ruleDao.insertAll(rules)
            android.util.Log.d("KnowledgeBootstrapper", "Loaded ${rules.size} operational rules")
        } catch (e: Exception) {
            android.util.Log.e("KnowledgeBootstrapper", "Failed to load rules: ${e.message}")
        }

        android.util.Log.d("KnowledgeBootstrapper", "Bootstrap complete: $totalChunks chunks loaded")
    }

    private fun loadChunksFromFile(filePath: String): List<KnowledgeChunk> {
        val json = context.assets.open(filePath).bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        val sourceTitle = root.getString("source_title")
        val sourceType = root.getString("source_type")
        val sourceCredibility = root.optString("source_credibility", "government_research")
        val topicTags = root.getString("topic_tags")
        val lastVerified = root.optString("last_verified", null)

        val chunksArray = root.getJSONArray("chunks")
        val chunks = mutableListOf<KnowledgeChunk>()

        for (i in 0 until chunksArray.length()) {
            val chunkObj = chunksArray.getJSONObject(i)
            val text = chunkObj.getString("text")
            val sectionHeader = chunkObj.optString("section_header", null)

            // Chunk using semantic chunker
            val createdChunks = semanticChunker.chunkDocument(
                documentText = text,
                sourceTitle = sourceTitle,
                sourceType = sourceType,
                sourceCredibility = sourceCredibility,
                topicTags = topicTags,
                lastVerified = lastVerified
            )

            // Set section header on each chunk
            for (chunk in createdChunks) {
                chunks.add(chunk.copy(sectionHeader = sectionHeader))
            }
        }

        return chunks
    }

    private fun loadRulesFromFile(filePath: String): List<OperationalRule> {
        val json = context.assets.open(filePath).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val rulesArray = root.getJSONArray("rules")
        val rules = mutableListOf<OperationalRule>()

        for (i in 0 until rulesArray.length()) {
            val ruleObj = rulesArray.getJSONObject(i)

            val ruleId = ruleObj.getString("rule_id")
            val ruleType = ruleObj.getString("rule_type")
            val species = ruleObj.optString("species", null)
            val crop = ruleObj.optString("crop", null)
            val location = ruleObj.optString("location", null)
            val parametersJson = ruleObj.getJSONObject("parameters").toString()
            val source = ruleObj.getString("source")
            val lastVerified = ruleObj.getString("last_verified")

            rules.add(OperationalRule(
                ruleId = ruleId,
                ruleType = ruleType,
                species = species,
                crop = crop,
                location = location,
                parametersJson = parametersJson,
                source = source,
                lastVerified = lastVerified
            ))
        }

        return rules
    }

    /**
     * Check if knowledge base is bootstrapped.
     */
    suspend fun isBootstrapped(): Boolean {
        return chunkDao.getBundledChunkCount() > 0
    }
}