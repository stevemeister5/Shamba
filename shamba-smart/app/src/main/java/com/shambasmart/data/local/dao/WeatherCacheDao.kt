package com.shambasmart.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Entity(tableName = "weather_cache")
data class WeatherCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val temperatureHigh: Double,
    val temperatureLow: Double,
    val humidity: Double,
    val precipitation: Double,
    val windSpeed: Double,
    val weatherCondition: String,
    val weatherIcon: String,
    val fetchedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long
)

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE date = :date LIMIT 1")
    suspend fun getWeatherForDate(date: LocalDate): WeatherCache?

    @Query("SELECT * FROM weather_cache WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getWeatherForecast(startDate: LocalDate, endDate: LocalDate): Flow<List<WeatherCache>>

    @Query("SELECT * FROM weather_cache WHERE date >= :currentDate ORDER BY date ASC LIMIT :days")
    fun getNextDaysForecast(currentDate: LocalDate, days: Int = 30): Flow<List<WeatherCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weatherCache: WeatherCache): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weatherCaches: List<WeatherCache>)

    @Update
    suspend fun update(weatherCache: WeatherCache)

    @Delete
    suspend fun delete(weatherCache: WeatherCache)

    @Query("DELETE FROM weather_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpired(currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM weather_cache WHERE date < :date")
    suspend fun deleteOlderThan(date: LocalDate)

    @Query("SELECT COUNT(*) FROM weather_cache WHERE expiresAt > :currentTime")
    suspend fun getValidCacheCount(currentTime: Long = System.currentTimeMillis()): Int
}