package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.WeatherLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface WeatherRepository {
    fun getAllWeatherLogs(): Flow<List<WeatherLog>>
    suspend fun getWeatherLogById(id: Long): WeatherLog?
    fun getWeatherLogsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WeatherLog>>
    fun getRecentWeatherLogs(limit: Int): Flow<List<WeatherLog>>
    suspend fun insertWeatherLog(weatherLog: WeatherLog): Long
    suspend fun updateWeatherLog(weatherLog: WeatherLog)
    suspend fun deleteWeatherLog(weatherLog: WeatherLog)
}