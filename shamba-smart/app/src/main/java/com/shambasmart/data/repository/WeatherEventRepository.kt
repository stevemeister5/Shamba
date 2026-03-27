package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.WeatherEventDao
import com.shambasmart.data.local.dao.WeatherEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val rainfall: Double? = null,
    val windSpeed: Double? = null,
    val notes: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Singleton
class WeatherEventRepository @Inject constructor(
    private val weatherEventDao: WeatherEventDao
) {
    fun getAllEvents(): Flow<List<WeatherEventEntity>> = weatherEventDao.getAllEvents()

    fun getEventsByType(eventType: String): Flow<List<WeatherEventEntity>> = 
        weatherEventDao.getEventsByType(eventType)

    fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<WeatherEventEntity>> {
        val startMillis = startDate.toJavaLocalDate().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.toJavaLocalDate().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return weatherEventDao.getEventsBetweenDates(startMillis, endMillis)
    }

    fun getRecentEvents(limit: Int = 50): Flow<List<WeatherEventEntity>> = 
        weatherEventDao.getRecentEvents(limit)

    suspend fun logWeatherEvent(event: WeatherEvent): Result<Long> {
        return try {
            val entity = WeatherEventEntity(
                eventId = event.id,
                timestamp = event.timestamp,
                eventType = event.eventType,
                temperature = event.temperature,
                humidity = event.humidity,
                rainfall = event.rainfall,
                windSpeed = event.windSpeed,
                notes = event.notes,
                latitude = event.latitude,
                longitude = event.longitude,
                isSynced = false
            )
            val id = weatherEventDao.insert(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: WeatherEventEntity): Result<Unit> {
        return try {
            weatherEventDao.update(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(event: WeatherEventEntity): Result<Unit> {
        return try {
            weatherEventDao.delete(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventCountSince(eventType: String, startDate: LocalDate): Int {
        val startMillis = startDate.toJavaLocalDate().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return weatherEventDao.getEventCountSince(eventType, startMillis)
    }

    suspend fun getUnsyncedEvents(): List<WeatherEventEntity> = weatherEventDao.getUnsyncedEvents()

    suspend fun updateSyncStatus(id: Long, synced: Boolean) {
        weatherEventDao.updateSyncStatus(id, synced)
    }

    suspend fun getEventById(id: Long): WeatherEventEntity? = weatherEventDao.getEventById(id)
}