package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries WHERE animalId = :animalId ORDER BY date DESC")
    fun getWeightEntriesByAnimalId(animalId: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE animalId = :animalId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeightEntry(animalId: Long): WeightEntry?

    @Query("SELECT * FROM weight_entries WHERE date = :date")
    suspend fun getWeightEntriesByDate(date: LocalDate): List<WeightEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weightEntry: WeightEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weightEntries: List<WeightEntry>)

    @Update
    suspend fun update(weightEntry: WeightEntry)

    @Delete
    suspend fun delete(weightEntry: WeightEntry)

    @Query("UPDATE weight_entries SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM weight_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<WeightEntry>

    // SyncManager support
    @Query("SELECT * FROM weight_entries WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<WeightEntry>

    // ContextBridge support
    @Query("SELECT * FROM weight_entries WHERE animalId = :animalId ORDER BY date DESC")
    suspend fun getWeightEntriesByAnimal(animalId: Long): List<WeightEntry>
}