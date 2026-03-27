package com.shambasmart.data.repository

import com.shambasmart.BuildConfig
import com.shambasmart.data.local.dao.WeatherCache
import com.shambasmart.data.local.dao.WeatherCacheDao
import com.shambasmart.data.remote.WeatherApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherCacheRepository @Inject constructor(
    private val weatherCacheDao: WeatherCacheDao,
    private val weatherApiClient: WeatherApiClient
) {
    companion object {
        private const val CACHE_EXPIRY_HOURS = 6L
        private const val CACHE_EXPIRY_MS = CACHE_EXPIRY_HOURS * 60 * 60 * 1000
    }

    fun getWeatherForecast(days: Int = 30): Flow<List<WeatherCache>> {
        val currentDate = java.time.LocalDate.now().toKotlinLocalDate()
        return weatherCacheDao.getNextDaysForecast(currentDate, days)
    }

    fun getWeatherForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WeatherCache>> {
        return weatherCacheDao.getWeatherForecast(startDate, endDate)
    }

    suspend fun getWeatherForDate(date: LocalDate): WeatherCache? {
        val cached = weatherCacheDao.getWeatherForDate(date)
        return if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            cached
        } else {
            null
        }
    }

    suspend fun refreshWeatherCache(latitude: Double = BuildConfig.FARM_LATITUDE, longitude: Double = BuildConfig.FARM_LONGITUDE): Result<Int> {
        return try {
            val response = weatherApiClient.weatherApiService.getForecast(
                latitude = latitude,
                longitude = longitude,
                apiKey = BuildConfig.WEATHER_API_KEY,
                count = 40
            )

            if (response.isSuccessful) {
                val forecastResponse = response.body()
                if (forecastResponse != null) {
                    val weatherCaches = mutableListOf<WeatherCache>()
                    val now = System.currentTimeMillis()
                    val expiresAt = now + CACHE_EXPIRY_MS

                    // Group forecasts by date and calculate daily highs/lows
                    val dailyForecasts = forecastResponse.list.groupBy { item ->
                        java.time.Instant.ofEpochSecond(item.dt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .toKotlinLocalDate()
                    }

                    dailyForecasts.forEach { (date, forecasts) ->
                        val tempHigh = forecasts.maxOf { it.main.temp_max }
                        val tempLow = forecasts.minOf { it.main.temp_min }
                        val avgHumidity = forecasts.map { it.main.humidity }.average()
                        val totalPrecipitation = forecasts.sumOf { it.rain?.`3h` ?: 0.0 }
                        val avgWindSpeed = forecasts.map { it.wind.speed }.average()
                        val mainCondition = forecasts.first().weather.firstOrNull()

                        weatherCaches.add(
                            WeatherCache(
                                date = date,
                                temperatureHigh = tempHigh,
                                temperatureLow = tempLow,
                                humidity = avgHumidity,
                                precipitation = totalPrecipitation,
                                windSpeed = avgWindSpeed,
                                weatherCondition = mainCondition?.main ?: "Unknown",
                                weatherIcon = mainCondition?.icon ?: "01d",
                                fetchedAt = now,
                                expiresAt = expiresAt
                            )
                        )
                    }

                    // Delete expired cache
                    weatherCacheDao.deleteExpired(now)

                    // Insert new cache
                    weatherCacheDao.insertAll(weatherCaches)

                    Result.success(weatherCaches.size)
                } else {
                    Result.failure(Exception("Empty response from weather API"))
                }
            } else {
                Result.failure(Exception("Weather API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearExpiredCache() {
        weatherCacheDao.deleteExpired()
    }

    suspend fun getValidCacheCount(): Int {
        return weatherCacheDao.getValidCacheCount()
    }
}