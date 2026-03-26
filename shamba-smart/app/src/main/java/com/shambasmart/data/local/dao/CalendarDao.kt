package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date = :date")
    suspend fun getEventsByDate(date: LocalDate): List<CalendarEvent>

    @Query("SELECT * FROM calendar_events WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE type = :type")
    fun getEventsByType(type: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEvent>)

    @Update
    suspend fun update(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)

    @Query("UPDATE calendar_events SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM calendar_events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<CalendarEvent>

    @Query("DELETE FROM calendar_events WHERE date < :date")
    suspend fun deleteOldEvents(date: LocalDate)
}