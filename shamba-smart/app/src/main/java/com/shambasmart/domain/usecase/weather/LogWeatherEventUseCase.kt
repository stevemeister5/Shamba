package com.shambasmart.domain.usecase.weather

import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.domain.repository.WeatherRepository
import javax.inject.Inject

class LogWeatherEventUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(weatherLog: WeatherLog): Result<Long> {
        return try {
            val id = weatherRepository.insertWeatherLog(weatherLog)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(weatherLog: WeatherLog): Result<Unit> {
        return try {
            weatherRepository.deleteWeatherLog(weatherLog)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
