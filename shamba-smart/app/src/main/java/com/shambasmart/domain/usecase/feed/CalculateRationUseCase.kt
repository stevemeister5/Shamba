package com.shambasmart.domain.usecase.feed 
 
import com.shambasmart.data.local.entity.FeedInventory 
import com.shambasmart.domain.repository.FeedRepository 
import javax.inject.Inject 
 
class CalculateRationUseCase @Inject constructor( 
    private val feedRepository: FeedRepository 
) { 
    suspend fun updateFeedStock(feedInventory: FeedInventory): Result<Unit> { 
        return try { 
            feedRepository.updateFeedInventory(feedInventory) 
            Result.success(Unit) 
        } catch (e: Exception) { 
            Result.failure(e) 
        } 
    } 
 
    suspend fun addFeedStock(feedInventory: FeedInventory): Result<Long> { 
        return try { 
            val id = feedRepository.insertFeedInventory(feedInventory) 
            Result.success(id) 
        } catch (e: Exception) { 
            Result.failure(e) 
        } 
    } 
 
    suspend fun deleteFeedStock(feedInventory: FeedInventory): Result<Unit> { 
        return try { 
            feedRepository.deleteFeedInventory(feedInventory) 
            Result.success(Unit) 
        } catch (e: Exception) { 
            Result.failure(e) 
        } 
    } 
}
