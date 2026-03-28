package com.shambasmart.domain.usecase.weather

import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetWeatherEventHistoryUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(): Flow<List<WeatherLog>> {
        return weatherRepository.getAllWeatherLogs()
    }

    fun getEventHistory(): Flow<List<WeatherLog>> {
        return weatherRepository.getAllWeatherLogs().map { logs ->
            logs.filter { !it.unusualEvents.isNullOrBlank() }
        }
    }

    suspend fun getById(id: Long): Result<WeatherLog?> {
        return try {
            val weatherLog = weatherRepository.getWeatherLogById(id)
            Result.success(weatherLog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
