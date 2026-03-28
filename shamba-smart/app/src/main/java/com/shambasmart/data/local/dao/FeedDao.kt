package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.FeedInventory
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed_inventory ORDER BY feedType ASC")
    fun getAllFeedInventory(): Flow<List<FeedInventory>>

    @Query("SELECT * FROM feed_inventory WHERE feedType = :feedType")
    suspend fun getFeedByType(feedType: String): FeedInventory?

    @Query("SELECT * FROM feed_inventory WHERE stockLevel <= reorderThreshold")
    suspend fun getLowStockFeed(): List<FeedInventory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: FeedInventory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(feeds: List<FeedInventory>)

    @Update
    suspend fun update(feed: FeedInventory)

    @Delete
    suspend fun delete(feed: FeedInventory)

    @Query("UPDATE feed_inventory SET stockLevel = :stockLevel, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStockLevel(id: Long, stockLevel: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE feed_inventory SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM feed_inventory WHERE isSynced = 0")
    suspend fun getUnsyncedFeed(): List<FeedInventory>

    @Query("SELECT SUM(stockLevel * costPerUnit) FROM feed_inventory WHERE costPerUnit IS NOT NULL")
    suspend fun getTotalFeedValue(): Double?

    // SyncManager support
    @Query("SELECT * FROM feed_inventory WHERE updatedAt > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<FeedInventory>

    // ContextBridge support
    @Query("SELECT * FROM feed_inventory ORDER BY feedType ASC")
    suspend fun getAllFeeds(): List<FeedInventory>
}
