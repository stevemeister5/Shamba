package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.HealthRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HealthRecordRepository {
    fun getRecordsByAnimalId(animalId: Long): Flow<List<HealthRecord>>
    fun getRecordsByType(type: String): Flow<List<HealthRecord>>
    suspend fun getUpcomingDueRecords(date: LocalDate): List<HealthRecord>
    suspend fun insertHealthRecord(record: HealthRecord): Long
    suspend fun updateHealthRecord(record: HealthRecord)
    suspend fun deleteHealthRecord(record: HealthRecord)
    suspend fun getAllRecords(): List<HealthRecord>
}