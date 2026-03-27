package com.shambasmart.domain.usecase.labour

import com.shambasmart.data.local.dao.WorkerDao
import com.shambasmart.data.local.entity.Worker
import javax.inject.Inject

class AddWorkerUseCase @Inject constructor(
    private val workerDao: WorkerDao
) {
    suspend operator fun invoke(worker: Worker): Result<Long> {
        return try {
            val now = System.currentTimeMillis()
            val id = workerDao.insertWorker(worker.copy(createdAt = now, updatedAt = now, isSynced = false))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(worker: Worker): Result<Unit> {
        return try {
            workerDao.updateWorker(worker.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(worker: Worker): Result<Unit> {
        return try {
            workerDao.deleteWorker(worker)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}