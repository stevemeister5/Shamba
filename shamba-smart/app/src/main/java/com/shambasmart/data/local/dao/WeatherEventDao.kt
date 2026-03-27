package com.shambasmart.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Entity(tableName = "weather_events")
data class WeatherEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val timestamp: Long,
    val eventType: String,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val rainfall: Double? = null,
    val windSpeed: Double? = null,
    val notes: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Dao
interface WeatherEventDao {
    @Query("SELECT * FROM weather_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<WeatherEventEntity>>

    @Query("SELECT * FROM weather_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByType(eventType: String): Flow<List<WeatherEventEntity>>

    @Query("SELECT * FROM weather_events WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getEventsBetweenDates(startDate: Long, endDate: Long): Flow<List<WeatherEventEntity>>

    @Query("SELECT * FROM weather_events WHERE id = :id")
    suspend fun getEventById(id: Long): WeatherEventEntity?

    @Query("SELECT * FROM weather_events WHERE eventId = :eventId")
    suspend fun getEventByEventId(eventId: String): WeatherEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: WeatherEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<WeatherEventEntity>)

    @Update
    suspend fun update(event: WeatherEventEntity)

    @Delete
    suspend fun delete(event: WeatherEventEntity)

    @Query("UPDATE weather_events SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM weather_events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<WeatherEventEntity>

    @Query("SELECT COUNT(*) FROM weather_events WHERE eventType = :eventType AND timestamp >= :startDate")
    suspend fun getEventCountSince(eventType: String, startDate: Long): Int

    @Query("SELECT * FROM weather_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 50): Flow<List<WeatherEventEntity>>
}