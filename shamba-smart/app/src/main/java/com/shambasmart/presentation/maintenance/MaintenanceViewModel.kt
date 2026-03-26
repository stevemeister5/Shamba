package com.shambasmart.presentation.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.MaintenanceTaskDao
import com.shambasmart.data.local.entity.MaintenanceTask
import com.shambasmart.data.local.entity.MaintenanceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val maintenanceTaskDao: MaintenanceTaskDao
) : ViewModel() {

    val tasks: StateFlow<List<MaintenanceTask>> = maintenanceTaskDao.getAllMaintenanceTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueCount: StateFlow<Int> = flow {
        val today = System.currentTimeMillis()
        emit(maintenanceTaskDao.getOverdueTaskCount(today))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val upcomingCount: StateFlow<Int> = flow {
        val today = System.currentTimeMillis()
        emit(maintenanceTaskDao.getUpcomingTaskCount(today))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addTask(task: MaintenanceTask) {
        viewModelScope.launch {
            maintenanceTaskDao.insert(task)
        }
    }

    fun updateTaskStatus(taskId: Long, status: MaintenanceStatus) {
        viewModelScope.launch {
            if (status == MaintenanceStatus.COMPLETED) {
                maintenanceTaskDao.markCompleted(
                    id = taskId,
                    completedDate = System.currentTimeMillis(),
                    actualDuration = 0.0 // Would be calculated from start time
                )
            } else {
                maintenanceTaskDao.updateStatus(taskId, status)
            }
        }
    }

    fun deleteTask(task: MaintenanceTask) {
        viewModelScope.launch {
            maintenanceTaskDao.delete(task)
        }
    }
}