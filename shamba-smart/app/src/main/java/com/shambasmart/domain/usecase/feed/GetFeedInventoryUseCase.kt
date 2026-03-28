package com.shambasmart.domain.usecase.feed

import com.shambasmart.data.local.entity.FeedInventory
import com.shambasmart.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedInventoryUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(): Flow<List<FeedInventory>> {
        return feedRepository.getAllFeedInventory()
    }

    suspend fun getById(id: Long): Result<FeedInventory?> {
        return try {
            val feedInventory = feedRepository.getFeedInventoryById(id)
            Result.success(feedInventory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getByType(feedType: String): Flow<List<FeedInventory>> {
        return feedRepository.getFeedInventoryByType(feedType)
    }

    fun getLowStock(): Flow<List<FeedInventory>> {
        return feedRepository.getLowStockFeed()
    }

    fun getTotalStockByType(): Flow<Map<String, Double>> {
        return feedRepository.getTotalStockByType()
    }
}