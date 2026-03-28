package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.data.local.entity.MilkCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface CheeseDao {
    // Cheese Batch queries
    @Query("SELECT * FROM cheese_batches ORDER BY productionDate DESC")
    fun getAllCheeseBatches(): Flow<List<CheeseBatch>>

    @Query("SELECT * FROM cheese_batches WHERE status = :status")
    fun getBatchesByStatus(status: String): Flow<List<CheeseBatch>>

    @Query("SELECT * FROM cheese_batches WHERE id = :id")
    suspend fun getBatchById(id: Long): CheeseBatch?

    @Query("SELECT SUM(yieldKg) FROM cheese_batches WHERE status = 'available'")
    suspend fun getTotalAvailableCheese(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: CheeseBatch): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<CheeseBatch>)

    @Update
    suspend fun updateBatch(batch: CheeseBatch)

    @Delete
    suspend fun deleteBatch(batch: CheeseBatch)

    @Query("UPDATE cheese_batches SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBatchStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    // Milk Collection queries
    @Query("SELECT * FROM milk_collections ORDER BY date DESC")
    fun getAllMilkCollections(): Flow<List<MilkCollection>>

    @Query("SELECT * FROM milk_collections WHERE date = :date")
    suspend fun getCollectionsByDate(date: String): List<MilkCollection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: MilkCollection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<MilkCollection>)

    @Update
    suspend fun updateCollection(collection: MilkCollection)

    @Delete
    suspend fun deleteCollection(collection: MilkCollection)

    // Sync queries
    @Query("UPDATE cheese_batches SET isSynced = :synced WHERE id = :id")
    suspend fun updateBatchSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE milk_collections SET isSynced = :synced WHERE id = :id")
    suspend fun updateCollectionSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM cheese_batches WHERE isSynced = 0")
    suspend fun getUnsyncedBatches(): List<CheeseBatch>

    @Query("SELECT * FROM milk_collections WHERE isSynced = 0")
    suspend fun getUnsyncedCollections(): List<MilkCollection>

    // SyncManager support
    @Query("SELECT * FROM cheese_batches WHERE updatedAt > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<CheeseBatch>
}
