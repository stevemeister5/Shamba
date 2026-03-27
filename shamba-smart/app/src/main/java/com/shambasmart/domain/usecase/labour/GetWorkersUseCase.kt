package com.shambasmart.domain.usecase.labour

import com.shambasmart.data.local.dao.WorkerDao
import com.shambasmart.data.local.entity.Worker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkersUseCase @Inject constructor(
    private val workerDao: WorkerDao
) {
    operator fun invoke(): Flow<List<Worker>> = workerDao.getAllActiveWorkers()

    suspend fun getById(id: Long): Worker? = workerDao.getWorkerById(id)
}