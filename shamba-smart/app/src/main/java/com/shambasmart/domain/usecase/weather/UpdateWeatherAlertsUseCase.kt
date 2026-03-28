package com.shambasmart.domain.usecase.weather

import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.domain.repository.WeatherRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class UpdateWeatherAlertsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(weatherLog: WeatherLog): Result<Unit> {
        return try {
            weatherRepository.updateWeatherLog(weatherLog)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAlertsByDateRange(startDate: LocalDate, endDate: LocalDate) = 
        weatherRepository.getWeatherLogsByDateRange(startDate, endDate)
}
