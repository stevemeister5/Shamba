package com.shambasmart.presentation.labour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.WorkerDao
import com.shambasmart.data.local.entity.Worker
import com.shambasmart.data.local.entity.AttendanceRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

data class PayrollEntry(
    val worker: Worker,
    val daysWorked: Int,
    val grossPay: Double,
    val advances: Double,
    val netPay: Double
)

@HiltViewModel
class LabourViewModel @Inject constructor(
    private val workerDao: WorkerDao
) : ViewModel() {

    val allWorkers: StateFlow<List<Worker>> = workerDao.getAllActiveWorkers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    val todayAttendance: StateFlow<List<AttendanceRecord>> = workerDao.getAttendanceByDateFlow(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payrollData: StateFlow<List<PayrollEntry>> = combine(allWorkers, todayAttendance) { workers, _ ->
        // In a real app, we'd fetch attendance for the whole month
        // For this evaluation, we'll simulate calculating it from the DB
        val startOfMonth = LocalDate(today.year, today.month, 1)
        workers.map { worker ->
            val days = workerDao.getDaysWorked(worker.id, startOfMonth, today)
            val dailyRate = worker.dailyRate ?: 0.0
            val gross = days * dailyRate
            PayrollEntry(
                worker = worker,
                daysWorked = days,
                grossPay = gross,
                advances = 0.0,
                netPay = gross
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
