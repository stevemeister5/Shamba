package com.shambasmart.presentation.labour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.WorkerDao
import com.shambasmart.data.local.entity.Worker
import com.shambasmart.data.local.entity.AttendanceRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class LabourViewModel @Inject constructor(
    private val workerDao: WorkerDao
) : ViewModel() {

    val allWorkers: StateFlow<List<Worker>> = workerDao.getAllActiveWorkers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWorker(worker: Worker) {
        viewModelScope.launch {
            workerDao.insertWorker(worker)
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            workerDao.updateWorker(worker.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            workerDao.updateWorkerStatus(worker.id, "inactive")
        }
    }

    fun addAttendance(record: AttendanceRecord) {
        viewModelScope.launch {
            workerDao.insertAttendance(record)
        }
    }
}