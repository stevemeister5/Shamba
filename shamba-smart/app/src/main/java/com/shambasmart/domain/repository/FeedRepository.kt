package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.FeedInventory
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface FeedRepository {
    fun getAllFeedInventory(): Flow<List<FeedInventory>>
    suspend fun getFeedInventoryById(id: Long): FeedInventory?
    fun getFeedInventoryByType(feedType: String): Flow<List<FeedInventory>>
    fun getLowStockFeed(): Flow<List<FeedInventory>>
    fun getTotalStockByType(): Flow<Map<String, Double>>
    suspend fun insertFeedInventory(feedInventory: FeedInventory): Long
    suspend fun updateFeedInventory(feedInventory: FeedInventory)
    suspend fun deleteFeedInventory(feedInventory: FeedInventory)
}