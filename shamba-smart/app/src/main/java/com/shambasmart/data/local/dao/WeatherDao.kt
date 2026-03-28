package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.WeatherLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_logs ORDER BY date DESC")
    fun getAllWeatherLogs(): Flow<List<WeatherLog>>

    @Query("SELECT * FROM weather_logs WHERE date = :date")
    suspend fun getWeatherByDate(date: LocalDate): WeatherLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: WeatherLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weathers: List<WeatherLog>)

    @Update
    suspend fun update(weather: WeatherLog)

    @Delete
    suspend fun delete(weather: WeatherLog)

    @Query("UPDATE weather_logs SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM weather_logs WHERE isSynced = 0")
    suspend fun getUnsyncedWeatherLogs(): List<WeatherLog>

    @Query("SELECT SUM(rainfallMm) FROM weather_logs WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalRainfall(startDate: LocalDate, endDate: LocalDate): Double?

    // SyncManager support
    @Query("SELECT * FROM weather_logs WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<WeatherLog>

    // ContextBridge support
    @Query("SELECT * FROM weather_logs ORDER BY date DESC")
    suspend fun getAllLogs(): List<WeatherLog>
}
