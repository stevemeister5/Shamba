package com.shambasmart.data.local.entity.maarifa

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Maarifa Operational Rule — computable rules for the farm management system.
 * 
 * This is the second layer of Maarifa's three-layer architecture.
 * Rules are small, explicit, and additive. New rule types can be added
 * without changing existing rules or schemas.
 * 
 * Rule types:
 * - withdrawal_period: drug milk/meat withdrawal calculations
 * - dose_calculation: weight-based drug dosing
 * - planting_window: crop planting date windows by season
 * - growth_stage: growth stage day counts from planting
 * - gestation: pregnancy length, kidding/lambing date projections
 * - vaccination_interval: vaccination schedule calculations
 * - oestrus_cycle: heat detection and mating timing
 * - feed_requirement: feed stock depletion calculations
 * - notifiable_disease: diseases requiring TVLA notification
 * - diagnostic_rule: symptom → differential → treatment chains
 */
@Entity(tableName = "maarifa_rules")
data class OperationalRule(
    @PrimaryKey
    val ruleId: String,                           // unique, e.g. "withdrawal_oxytet_la_goat"
    
    val ruleType: String,                         // see rule types above
    
    val species: String? = null,                  // comma-separated: "goat,sheep,pig"
    
    val crop: String? = null,                     // "maize" — optional
    
    val location: String? = null,                 // "korogwe" — optional
    
    val parametersJson: String,                   // JSON string of all rule-specific values
                                                  // e.g. {"milk_withdrawal_days":7,"meat_withdrawal_days":28}
    
    val contentReferences: String? = null,        // comma-separated chunk_ids that define
                                                  // this rule — for conflict detection
    
    val source: String,                           // e.g. "Norbrook product data sheet 2023"
    
    val lastVerified: String,                     // ISO date
    
    val supersedes: String? = null,               // rule_id of older rule this replaces
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    /** Parse species into a list */
    fun getSpeciesList(): List<String> =
        species?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    
    /** Parse content references into a list */
    fun getContentReferencesList(): List<String> =
        contentReferences?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    
    /** Check if this rule applies to a given species */
    fun appliesToSpecies(targetSpecies: String): Boolean {
        val speciesList = getSpeciesList()
        return speciesList.isEmpty() || speciesList.any { 
            it.equals(targetSpecies, ignoreCase = true) 
        }
    }
    
    /** Check if this rule applies to a given crop */
    fun appliesToCrop(targetCrop: String): Boolean {
        return crop == null || crop.equals(targetCrop, ignoreCase = true)
    }
    
    companion object {
        val RULE_TYPES = listOf(
            "withdrawal_period",
            "dose_calculation",
            "planting_window",
            "growth_stage",
            "gestation",
            "vaccination_interval",
            "oestrus_cycle",
            "feed_requirement",
            "notifiable_disease",
            "diagnostic_rule"
        )
    }
}