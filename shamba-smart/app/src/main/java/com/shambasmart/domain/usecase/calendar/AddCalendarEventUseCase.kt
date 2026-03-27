package com.shambasmart.domain.usecase.calendar

import com.shambasmart.data.local.dao.CalendarDao
import com.shambasmart.data.local.entity.CalendarEvent
import javax.inject.Inject

class AddCalendarEventUseCase @Inject constructor(
    private val calendarDao: CalendarDao
) {
    suspend operator fun invoke(event: CalendarEvent): Result<Long> {
        return try {
            val now = System.currentTimeMillis()
            val id = calendarDao.insert(event.copy(createdAt = now, updatedAt = now, isSynced = false))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(event: CalendarEvent): Result<Unit> {
        return try {
            calendarDao.update(event.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(event: CalendarEvent): Result<Unit> {
        return try {
            calendarDao.delete(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}