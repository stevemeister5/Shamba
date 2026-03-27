package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.CalendarDao
import com.shambasmart.data.local.dao.TaskDao
import com.shambasmart.data.local.entity.CalendarEvent
import com.shambasmart.data.local.entity.Task
import com.shambasmart.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val calendarDao: CalendarDao,
    private val taskDao: TaskDao
) : CalendarRepository {

    // Calendar Events

    override fun getAllEvents(): Flow<List<CalendarEvent>> = calendarDao.getAllEvents()

    override suspend fun getEventById(id: Long): CalendarEvent? {
        val allEvents = calendarDao.getAllEvents().firstOrNull() ?: emptyList()
        return allEvents.find { it.id == id }
    }

    override fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> =
        calendarDao.getAllEvents().map { list ->
            list.filter { it.date == date }
        }

    override fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>> =
        calendarDao.getEventsBetweenDates(startDate, endDate)

    override fun getEventsByType(type: String): Flow<List<CalendarEvent>> =
        calendarDao.getEventsByType(type)

    override suspend fun insertEvent(event: CalendarEvent): Long {
        val now = System.currentTimeMillis()
        return calendarDao.insert(event.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateEvent(event: CalendarEvent) {
        calendarDao.update(event.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteEvent(event: CalendarEvent) {
        calendarDao.delete(event)
    }

    // Tasks

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTasksByDate(date: LocalDate): Flow<List<Task>> =
        taskDao.getAllTasks().map { list ->
            list.filter { it.dueDate == date }
        }

    override fun getTasksByStatus(status: String): Flow<List<Task>> =
        taskDao.getTasksByStatus(status)

    override fun getTasksByWorker(workerId: Long): Flow<List<Task>> =
        taskDao.getTasksByWorker(workerId)

    override suspend fun getOverdueTasks(date: LocalDate): List<Task> =
        taskDao.getOverdueTasks(date)

    override suspend fun insertTask(task: Task): Long {
        val now = System.currentTimeMillis()
        return taskDao.insert(task.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    override suspend fun updateTaskStatus(id: Long, status: String) {
        taskDao.updateStatus(id, status)
    }
}