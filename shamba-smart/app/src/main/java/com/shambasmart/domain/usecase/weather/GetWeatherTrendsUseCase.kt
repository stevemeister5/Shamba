package com.shambasmart.domain.usecase.weather

import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetWeatherTrendsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<WeatherLog>> {
        return weatherRepository.getWeatherLogsByDateRange(startDate, endDate)
    }

    fun getAll(): Flow<List<WeatherLog>> {
        return weatherRepository.getAllWeatherLogs()
    }
}
