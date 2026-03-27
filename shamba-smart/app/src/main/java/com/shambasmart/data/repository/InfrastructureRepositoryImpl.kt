package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.StoreDao
import com.shambasmart.data.local.entity.StoreItem
import com.shambasmart.domain.repository.InfrastructureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InfrastructureRepositoryImpl @Inject constructor(
    private val storeDao: StoreDao
) : InfrastructureRepository {

    override fun getAllStoreItems(): Flow<List<StoreItem>> = storeDao.getAllStoreItems()

    override fun getItemsByCategory(category: String): Flow<List<StoreItem>> =
        storeDao.getItemsByCategory(category)

    override suspend fun insertStoreItem(item: StoreItem): Long {
        val now = System.currentTimeMillis()
        return storeDao.insert(item.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateStoreItem(item: StoreItem) {
        storeDao.update(item.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteStoreItem(item: StoreItem) = storeDao.delete(item)

    override suspend fun updateStoreItemQuantity(id: Long, quantity: Double) {
        storeDao.updateQuantity(id, quantity)
    }

    override suspend fun getLowStockItems(): List<StoreItem> = storeDao.getLowStockItems()

    override suspend fun getTotalStoreValue(): Double? = storeDao.getTotalStoreValue()
}
