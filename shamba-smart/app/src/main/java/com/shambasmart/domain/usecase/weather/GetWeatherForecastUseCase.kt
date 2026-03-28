package com.shambasmart.domain.usecase.weather

import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeatherForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(): Flow<List<WeatherLog>> {
        return weatherRepository.getAllWeatherLogs()
    }

    suspend fun getById(id: Long): Result<WeatherLog?> {
        return try {
            val weatherLog = weatherRepository.getWeatherLogById(id)
            Result.success(weatherLog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRecent(limit: Int): Flow<List<WeatherLog>> {
        return weatherRepository.getRecentWeatherLogs(limit)
    }
}
