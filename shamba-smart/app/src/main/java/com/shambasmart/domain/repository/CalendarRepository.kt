package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.CalendarEvent
import com.shambasmart.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CalendarRepository {
    fun getAllEvents(): Flow<List<CalendarEvent>>
    suspend fun getEventById(id: Long): CalendarEvent?
    fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>>
    fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>>
    fun getEventsByType(type: String): Flow<List<CalendarEvent>>
    suspend fun insertEvent(event: CalendarEvent): Long
    suspend fun updateEvent(event: CalendarEvent)
    suspend fun deleteEvent(event: CalendarEvent)

    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByDate(date: LocalDate): Flow<List<Task>>
    fun getTasksByStatus(status: String): Flow<List<Task>>
    fun getTasksByWorker(workerId: Long): Flow<List<Task>>
    suspend fun getOverdueTasks(date: LocalDate): List<Task>
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun updateTaskStatus(id: Long, status: String)
}