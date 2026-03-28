package com.shambasmart.domain.usecase.cheese

import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.domain.repository.CheeseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCheeseInventoryUseCase @Inject constructor(
    private val cheeseRepository: CheeseRepository
) {
    operator fun invoke(): Flow<List<CheeseBatch>> {
        return cheeseRepository.getAllCheeseBatches()
    }

    suspend fun getById(id: Long): Result<CheeseBatch?> {
        return try {
            val batch = cheeseRepository.getCheeseBatchById(id)
            Result.success(batch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getByStatus(status: String): Flow<List<CheeseBatch>> {
        return cheeseRepository.getCheeseBatchesByStatus(status)
    }

    fun getTotalInventoryByCheeseType(): Flow<Map<String, Double>> {
        return cheeseRepository.getTotalInventoryByCheeseType()
    }
}