package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.FeedDao
import com.shambasmart.data.local.entity.FeedInventory
import com.shambasmart.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val feedDao: FeedDao
) : FeedRepository {

    override fun getAllFeedInventory(): Flow<List<FeedInventory>> = feedDao.getAllFeedInventory()

    override suspend fun getFeedInventoryById(id: Long): FeedInventory? {
        val allFeeds = feedDao.getAllFeedInventory().firstOrNull() ?: emptyList()
        return allFeeds.find { it.id == id }
    }

    override fun getFeedInventoryByType(feedType: String): Flow<List<FeedInventory>> =
        feedDao.getAllFeedInventory().map { list ->
            list.filter { it.feedType == feedType }
        }

    override fun getLowStockFeed(): Flow<List<FeedInventory>> =
        feedDao.getAllFeedInventory().map { list ->
            list.filter { it.stockLevel <= (it.reorderThreshold ?: 0.0) }
        }

    override fun getTotalStockByType(): Flow<Map<String, Double>> =
        feedDao.getAllFeedInventory().map { list ->
            list.groupBy { it.feedType }
                .mapValues { (_, items) -> items.sumOf { it.stockLevel } }
        }

    override suspend fun insertFeedInventory(feedInventory: FeedInventory): Long {
        val now = System.currentTimeMillis()
        return feedDao.insert(feedInventory.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateFeedInventory(feedInventory: FeedInventory) {
        feedDao.update(feedInventory.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteFeedInventory(feedInventory: FeedInventory) {
        feedDao.delete(feedInventory)
    }
}