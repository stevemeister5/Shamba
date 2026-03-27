package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.StoreItem
import kotlinx.coroutines.flow.Flow

interface InfrastructureRepository {
    fun getAllStoreItems(): Flow<List<StoreItem>>
    fun getItemsByCategory(category: String): Flow<List<StoreItem>>
    suspend fun insertStoreItem(item: StoreItem): Long
    suspend fun updateStoreItem(item: StoreItem)
    suspend fun deleteStoreItem(item: StoreItem)
    suspend fun updateStoreItemQuantity(id: Long, quantity: Double)
    suspend fun getLowStockItems(): List<StoreItem>
    suspend fun getTotalStoreValue(): Double?
}
