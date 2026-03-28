package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.SilageInventory
import kotlinx.coroutines.flow.Flow

@Dao
interface SilageDao {
    @Query("SELECT * FROM silage_inventory ORDER BY fillDate DESC")
    fun getAllSilage(): Flow<List<SilageInventory>>

    @Query("SELECT * FROM silage_inventory WHERE id = :id")
    suspend fun getSilageById(id: Long): SilageInventory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(silage: SilageInventory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(silages: List<SilageInventory>)

    @Update
    suspend fun update(silage: SilageInventory)

    @Delete
    suspend fun delete(silage: SilageInventory)

    @Query("UPDATE silage_inventory SET currentTonnage = :tonnage, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTonnage(id: Long, tonnage: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE silage_inventory SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM silage_inventory WHERE isSynced = 0")
    suspend fun getUnsyncedSilage(): List<SilageInventory>

    @Query("SELECT SUM(currentTonnage) FROM silage_inventory WHERE fermentationComplete = 1")
    suspend fun getTotalAvailableSilage(): Double?

    // SyncManager support
    @Query("SELECT * FROM silage_inventory WHERE updatedAt > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<SilageInventory>
}
