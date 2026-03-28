package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.HarvestRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvest_records WHERE cropPlantingId = :cropPlantingId ORDER BY harvestDate DESC")
    fun getHarvestsByCropId(cropPlantingId: Long): Flow<List<HarvestRecord>>

    @Query("SELECT * FROM harvest_records ORDER BY harvestDate DESC")
    fun getAllHarvests(): Flow<List<HarvestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(harvest: HarvestRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(harvests: List<HarvestRecord>)

    @Update
    suspend fun update(harvest: HarvestRecord)

    @Delete
    suspend fun delete(harvest: HarvestRecord)

    @Query("UPDATE harvest_records SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM harvest_records WHERE isSynced = 0")
    suspend fun getUnsyncedHarvests(): List<HarvestRecord>

    // SyncManager support
    @Query("SELECT * FROM harvest_records WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<HarvestRecord>
}
