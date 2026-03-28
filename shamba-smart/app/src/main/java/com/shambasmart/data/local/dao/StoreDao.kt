package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.StoreItem
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM store_items ORDER BY name ASC")
    fun getAllStoreItems(): Flow<List<StoreItem>>

    @Query("SELECT * FROM store_items WHERE category = :category")
    fun getItemsByCategory(category: String): Flow<List<StoreItem>>

    @Query("SELECT * FROM store_items WHERE quantity <= reorderLevel")
    suspend fun getLowStockItems(): List<StoreItem>

    @Query("SELECT * FROM store_items WHERE expiryDate <= :date")
    suspend fun getExpiringItems(date: String): List<StoreItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StoreItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StoreItem>)

    @Update
    suspend fun update(item: StoreItem)

    @Delete
    suspend fun delete(item: StoreItem)

    @Query("UPDATE store_items SET quantity = :quantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE store_items SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM store_items WHERE isSynced = 0")
    suspend fun getUnsyncedItems(): List<StoreItem>

    @Query("SELECT SUM(quantity * costPerUnit) FROM store_items WHERE costPerUnit IS NOT NULL")
    suspend fun getTotalStoreValue(): Double?

    // SyncManager support
    @Query("SELECT * FROM store_items WHERE updatedAt > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<StoreItem>
}
