package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.HealthRecordDao
import com.shambasmart.data.local.entity.HealthRecord
import com.shambasmart.domain.repository.HealthRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRecordRepositoryImpl @Inject constructor(
    private val healthRecordDao: HealthRecordDao
) : HealthRecordRepository {

    override fun getRecordsByAnimalId(animalId: Long): Flow<List<HealthRecord>> = 
        healthRecordDao.getRecordsByAnimalId(animalId)

    override fun getRecordsByType(type: String): Flow<List<HealthRecord>> = 
        healthRecordDao.getRecordsByType(type)

    override suspend fun getUpcomingDueRecords(date: LocalDate): List<HealthRecord> = 
        healthRecordDao.getUpcomingDueRecords(date)

    override suspend fun insertHealthRecord(record: HealthRecord): Long {
        return healthRecordDao.insert(record.copy(isSynced = false))
    }

    override suspend fun updateHealthRecord(record: HealthRecord) {
        healthRecordDao.update(record.copy(isSynced = false))
    }

    override suspend fun deleteHealthRecord(record: HealthRecord) {
        healthRecordDao.delete(record)
    }

    override suspend fun getAllRecords(): List<HealthRecord> = 
        healthRecordDao.getAllRecords()
}