package com.shambasmart.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.CalendarDao
import com.shambasmart.data.local.dao.TaskDao
import com.shambasmart.data.local.entity.CalendarEvent
import com.shambasmart.data.local.entity.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarDao: CalendarDao,
    private val taskDao: TaskDao
) : ViewModel() {

    val allEvents: StateFlow<List<CalendarEvent>> = calendarDao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            calendarDao.insert(event)
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            calendarDao.delete(event)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            taskDao.updateStatus(task.id, "completed", today)
        }
    }
}