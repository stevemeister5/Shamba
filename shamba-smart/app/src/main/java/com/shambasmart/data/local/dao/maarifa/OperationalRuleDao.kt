package com.shambasmart.data.local.dao.maarifa

import androidx.room.*
import com.shambasmart.data.local.entity.maarifa.OperationalRule
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Maarifa Operational Rules.
 * 
 * Rules are the second layer — small, computable parameters
 * for drug dosages, withdrawal periods, planting windows,
 * gestation lengths, vaccination intervals, etc.
 */
@Dao
interface OperationalRuleDao {
    
    // === INSERT ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: OperationalRule)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<OperationalRule>)
    
    // === QUERY ===
    @Query("SELECT * FROM maarifa_rules WHERE ruleId = :ruleId")
    suspend fun getRuleById(ruleId: String): OperationalRule?
    
    @Query("SELECT * FROM maarifa_rules ORDER BY ruleType, lastVerified DESC")
    fun observeAllRules(): Flow<List<OperationalRule>>
    
    @Query("SELECT COUNT(*) FROM maarifa_rules")
    suspend fun getRuleCount(): Int
    
    // === BY TYPE ===
    @Query("SELECT * FROM maarifa_rules WHERE ruleType = :ruleType")
    suspend fun getRulesByType(ruleType: String): List<OperationalRule>
    
    @Query("SELECT * FROM maarifa_rules WHERE ruleType = :ruleType")
    fun observeRulesByType(ruleType: String): Flow<List<OperationalRule>>
    
    // === BY SPECIES ===
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE species LIKE '%' || :species || '%'
        ORDER BY ruleType
    """)
    suspend fun getRulesBySpecies(species: String): List<OperationalRule>
    
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = :ruleType 
          AND (species LIKE '%' || :species || '%' OR species IS NULL)
    """)
    suspend fun getRulesByTypeAndSpecies(ruleType: String, species: String): List<OperationalRule>
    
    // === BY CROP ===
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = :ruleType 
          AND (crop = :crop OR crop IS NULL)
          AND (location = :location OR location IS NULL)
    """)
    suspend fun getPlantingRules(crop: String, location: String): List<OperationalRule>
    
    // === SPECIFIC RULE LOOKUPS ===
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = 'withdrawal_period'
          AND parametersJson LIKE '%' || :drugGeneric || '%'
          AND (species LIKE '%' || :species || '%' OR species IS NULL)
        LIMIT 1
    """)
    suspend fun getWithdrawalRule(drugGeneric: String, species: String): OperationalRule?
    
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = 'dose_calculation'
          AND parametersJson LIKE '%' || :drugGeneric || '%'
          AND (species LIKE '%' || :species || '%' OR species IS NULL)
        LIMIT 1
    """)
    suspend fun getDoseRule(drugGeneric: String, species: String): OperationalRule?
    
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = 'gestation'
          AND (species LIKE '%' || :species || '%' OR species IS NULL)
        LIMIT 1
    """)
    suspend fun getGestationRule(species: String): OperationalRule?
    
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = 'vaccination_interval'
          AND parametersJson LIKE '%' || :diseaseName || '%'
          AND (species LIKE '%' || :species || '%' OR species IS NULL)
        LIMIT 1
    """)
    suspend fun getVaccinationRule(diseaseName: String, species: String): OperationalRule?
    
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = 'notifiable_disease'
          AND parametersJson LIKE '%' || :diseaseName || '%'
        LIMIT 1
    """)
    suspend fun getNotifiableRule(diseaseName: String): OperationalRule?
    
    // === CONFLICT DETECTION ===
    @Query("""
        SELECT * FROM maarifa_rules 
        WHERE ruleType = :ruleType
          AND parametersJson LIKE '%' || :parameterKey || '%'
    """)
    suspend fun findRulesWithParameter(ruleType: String, parameterKey: String): List<OperationalRule>
    
    // === DELETE ===
    @Query("DELETE FROM maarifa_rules WHERE ruleId = :ruleId")
    suspend fun deleteById(ruleId: String)
    
    @Query("DELETE FROM maarifa_rules WHERE ruleType = :ruleType")
    suspend fun deleteByType(ruleType: String)
    
    // === SYNC SUPPORT ===
    @Query("SELECT * FROM maarifa_rules WHERE isSynced = 0")
    suspend fun getUnsyncedRules(): List<OperationalRule>
    
    @Query("UPDATE maarifa_rules SET isSynced = 1 WHERE ruleId = :ruleId")
    suspend fun markSynced(ruleId: String)
}