package com.shambasmart.domain.usecase.cheese

import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.domain.repository.CheeseRepository
import javax.inject.Inject

class AddCheeseBatchUseCase @Inject constructor(
    private val cheeseRepository: CheeseRepository
) {
    suspend operator fun invoke(batch: CheeseBatch): Result<Long> {
        return try {
            val id = cheeseRepository.insertCheeseBatch(batch)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(batch: CheeseBatch): Result<Unit> {
        return try {
            cheeseRepository.updateCheeseBatch(batch)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(batch: CheeseBatch): Result<Unit> {
        return try {
            cheeseRepository.deleteCheeseBatch(batch)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
