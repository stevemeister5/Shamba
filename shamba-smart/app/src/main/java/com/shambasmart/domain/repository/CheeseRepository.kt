package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.CheeseBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CheeseRepository {
    fun getAllCheeseBatches(): Flow<List<CheeseBatch>>
    suspend fun getCheeseBatchById(id: Long): CheeseBatch?
    fun getCheeseBatchesByStatus(status: String): Flow<List<CheeseBatch>>
    fun getCheeseBatchesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CheeseBatch>>
    fun getTotalInventoryByCheeseType(): Flow<Map<String, Double>>
    suspend fun insertCheeseBatch(batch: CheeseBatch): Long
    suspend fun updateCheeseBatch(batch: CheeseBatch)
    suspend fun deleteCheeseBatch(batch: CheeseBatch)
}