package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.WeatherDao
import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherDao: WeatherDao
) : WeatherRepository {

    override fun getAllWeatherLogs(): Flow<List<WeatherLog>> = weatherDao.getAllWeatherLogs()

    override suspend fun getWeatherLogById(id: Long): WeatherLog? {
        val allLogs = weatherDao.getAllWeatherLogs().firstOrNull() ?: emptyList()
        return allLogs.find { it.id == id }
    }

    override fun getWeatherLogsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WeatherLog>> =
        weatherDao.getAllWeatherLogs().map { list ->
            list.filter { it.date >= startDate && it.date <= endDate }
        }

    override fun getRecentWeatherLogs(limit: Int): Flow<List<WeatherLog>> =
        weatherDao.getAllWeatherLogs().map { list ->
            list.sortedByDescending { it.date }.take(limit)
        }

    override suspend fun insertWeatherLog(weatherLog: WeatherLog): Long {
        return weatherDao.insert(weatherLog.copy(createdAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun updateWeatherLog(weatherLog: WeatherLog) {
        weatherDao.update(weatherLog.copy(isSynced = false))
    }

    override suspend fun deleteWeatherLog(weatherLog: WeatherLog) {
        weatherDao.delete(weatherLog)
    }
}