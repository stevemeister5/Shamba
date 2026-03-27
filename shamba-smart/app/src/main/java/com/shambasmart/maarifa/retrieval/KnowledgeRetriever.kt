package com.shambasmart.maarifa.retrieval

import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.data.local.entity.maarifa.OperationalRule

class KnowledgeRetriever(
    private val chunkDao: KnowledgeChunkDao,
    private val ruleDao: OperationalRuleDao
) {
    companion object {
        private const val MAX_RESULTS = 8
    }

    suspend fun retrieve(
        query: String, species: String? = null, crop: String? = null,
        intent: String? = null, keywords: List<String> = emptyList()
    ): List<ScoredChunk> {
        val filtered = preFilter(species, crop, intent)
        if (filtered.isEmpty()) return emptyList()
        val terms = if (keywords.isNotEmpty()) keywords.map { it.lowercase() }
            else query.lowercase().split(Regex("\\s+")).filter { it.length > 2 }
        val scored = filtered.map { chunk ->
            val bm25 = computeBm25(chunk, terms)
            val meta = computeMeta(chunk, species, crop, intent)
            val final = (0.4f * bm25) + (0.2f * meta)
            ScoredChunk(chunk, final, bm25, 0f, meta)
        }
        return scored.filter { it.finalScore > 0.1f }
            .sortedByDescending { it.finalScore }.take(MAX_RESULTS)
    }

    private suspend fun preFilter(species: String?, crop: String?, intent: String?): List<KnowledgeChunk> {
        val tags = mutableListOf<String>()
        species?.let { tags.add(it.lowercase()) }
        crop?.let { tags.add(it.lowercase()) }
        when (intent) {
            "dosage_lookup","withdrawal_lookup" -> tags.add("medicines")
            "planting_advice","input_advice" -> tags.add("crops")
            "cheese_process" -> tags.add("cheese")
            "weather_risk" -> tags.add("weather")
        }
        if (tags.isEmpty()) return emptyList()
        return chunkDao.getChunksByTag(tags.first())
    }

    private fun computeBm25(chunk: KnowledgeChunk, terms: List<String>): Float {
        if (terms.isEmpty()) return 0f
        val text = chunk.text.lowercase()
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
                tags.any { it in listOf("disease","medicines","health","drug") }
            "planting_advice","input_advice","harvest_guidance" ->
                tags.any { it in listOf("crops","agronomy","planting","harvest") }
            "breeding_query" -> tags.any { it in listOf("reproduction","breeding") }
            "nutrition_query" -> tags.any { it in listOf("feed","nutrition") }
            "cheese_process" -> tags.any { it in listOf("cheese","dairy") }
            "weather_risk" -> tags.any { it in listOf("weather","climate") }
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