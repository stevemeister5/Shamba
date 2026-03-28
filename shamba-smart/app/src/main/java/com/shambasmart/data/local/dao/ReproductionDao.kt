package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.ReproductionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ReproductionDao {
    @Query("SELECT * FROM reproduction_records WHERE damId = :damId ORDER BY matingDate DESC")
    fun getRecordsByDamId(damId: Long): Flow<List<ReproductionRecord>>

    @Query("SELECT * FROM reproduction_records WHERE sireId = :sireId")
    fun getRecordsBySireId(sireId: Long): Flow<List<ReproductionRecord>>

    @Query("SELECT * FROM reproduction_records WHERE pregnancyConfirmed = 1 AND actualBirthDate IS NULL")
    suspend fun getActivePregnancies(): List<ReproductionRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ReproductionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ReproductionRecord>)

    @Update
    suspend fun update(record: ReproductionRecord)

    @Delete
    suspend fun delete(record: ReproductionRecord)

    @Query("UPDATE reproduction_records SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM reproduction_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<ReproductionRecord>

    // SyncManager support
    @Query("SELECT * FROM reproduction_records WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<ReproductionRecord>

    // ContextBridge support
    @Query("SELECT * FROM reproduction_records WHERE damId = :animalId ORDER BY matingDate DESC")
    suspend fun getRecordsByAnimal(animalId: Long): List<ReproductionRecord>
}
