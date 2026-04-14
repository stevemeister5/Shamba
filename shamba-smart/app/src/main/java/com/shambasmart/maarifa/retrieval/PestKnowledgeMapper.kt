package com.shambasmart.maarifa.retrieval

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shambasmart.maarifa.MaarifaDomains
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps pest detections to Tanzania Ministry of Agriculture approved management protocols.
 * Loads pest knowledge from assets/knowledge_base/pest_knowledge.json.
 */
@Singleton
class PestKnowledgeMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var pestProtocols: Map<String, PestProtocol>? = null

    /**
     * Gets the management protocol for a detected pest.
     * @param pestClass The detected pest class (e.g., "fall_armyworm")
     * @return PestProtocol with management instructions
     */
    fun getProtocol(pestClass: String): PestProtocol {
        if (pestProtocols == null) {
            loadProtocols()
        }
        return pestProtocols?.get(pestClass) ?: getDefaultProtocol(pestClass)
    }

    /**
     * Gets protocol by severity level.
     * @param pestClass The detected pest class
     * @param severityScore 1-5 severity scale
     * @return Management instructions appropriate for the severity
     */
    fun getProtocolBySeverity(pestClass: String, severityScore: Int): String {
        val protocol = getProtocol(pestClass)
        return when {
            severityScore >= 4 -> protocol.criticalManagement
            severityScore >= 3 -> protocol.moderateManagement
            else -> protocol.lowManagement
        }
    }

    /**
     * Gets pesticide recommendations with dosing and PHI.
     * @param pestClass The detected pest class
     * @return List of recommended pesticides
     */
    fun getPesticideRecommendations(pestClass: String): List<PesticideRecommendation> {
        val protocol = getProtocol(pestClass)
        return protocol.pesticides
    }

    /**
     * Gets biological control alternatives.
     * @param pestClass The detected pest class
     * @return List of biological control methods
     */
    fun getBiologicalControls(pestClass: String): List<String> {
        val protocol = getProtocol(pestClass)
        return protocol.biologicalControls
    }

    private fun loadProtocols() {
        try {
            val json = context.assets.open("knowledge_base/pest_knowledge.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<String, PestProtocol>>() {}.type
            pestProtocols = Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            pestProtocols = emptyMap()
        }
    }

    private fun getDefaultProtocol(pestClass: String): PestProtocol {
        return PestProtocol(
            pestClass = pestClass,
            displayName = pestClass.replace("_", " ").replaceFirstChar { it.uppercase() },
            domainTag = MaarifaDomains.PESTS,
            description = "Pest detected in field. Consult local extension officer for specific management protocols.",
            lowManagement = "Monitor closely. Document occurrence and location. Continue regular scouting.",
            moderateManagement = "Increase scouting frequency. Consider spot treatment. Consult Maize Research Institute recommendations.",
            criticalManagement = "Immediate action required. Apply approved treatment. Notify extension officer. Consider quarantine measures.",
            pesticides = emptyList(),
            biologicalControls = listOf(
                "Encourage natural predators",
                "Use pheromone traps for monitoring",
                "Maintain field hygiene"
            )
        )
    }
}

/**
 * Pest management protocol from Tanzania Ministry of Agriculture.
 */
data class PestProtocol(
    val pestClass: String,
    val displayName: String,
    val domainTag: String = MaarifaDomains.PESTS,
    val description: String,
    val lowManagement: String,
    val moderateManagement: String,
    val criticalManagement: String,
    val pesticides: List<PesticideRecommendation>,
    val biologicalControls: List<String>
)

/**
 * Pesticide recommendation with dosing and PHI.
 */
data class PesticideRecommendation(
    val name: String,
    val activeIngredient: String,
    val doseRate: String, // e.g., "50ml/20L water"
    val phi: Int, // Pre-Harvest Interval in days
    val maxApplications: Int,
    val notes: String
)