package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.HealthRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE animalId = :animalId ORDER BY date DESC")
    fun getRecordsByAnimalId(animalId: Long): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE type = :type ORDER BY date DESC")
    fun getRecordsByType(type: String): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE nextDueDate <= :date AND nextDueDate IS NOT NULL")
    suspend fun getUpcomingDueRecords(date: LocalDate): List<HealthRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HealthRecord>)

    @Update
    suspend fun update(record: HealthRecord)

    @Delete
    suspend fun delete(record: HealthRecord)

    @Query("UPDATE health_records SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM health_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<HealthRecord>

    // SyncManager support
    @Query("SELECT * FROM health_records WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<HealthRecord>

    // ContextBridge support
    @Query("SELECT * FROM health_records WHERE animalId = :animalId ORDER BY date DESC")
    suspend fun getRecordsByAnimal(animalId: Long): List<HealthRecord>

    @Query("SELECT * FROM health_records ORDER BY date DESC")
    suspend fun getAllRecords(): List<HealthRecord>
}
