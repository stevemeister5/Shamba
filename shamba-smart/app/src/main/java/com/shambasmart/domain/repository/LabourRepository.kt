package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.AttendanceRecord
import com.shambasmart.data.local.entity.Task
import com.shambasmart.data.local.entity.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface LabourRepository {
    // Worker operations
    fun getAllActiveWorkers(): Flow<List<Worker>>
    suspend fun getWorkerById(id: Long): Worker?
    suspend fun insertWorker(worker: Worker): Long
    suspend fun updateWorker(worker: Worker)
    suspend fun deleteWorker(worker: Worker)
    suspend fun updateWorkerStatus(id: Long, status: String)

    // Attendance operations
    fun getAttendanceByWorkerId(workerId: Long): Flow<List<AttendanceRecord>>
    suspend fun getDaysWorked(workerId: Long, startDate: LocalDate, endDate: LocalDate): Int
    suspend fun insertAttendance(attendance: AttendanceRecord): Long
    suspend fun updateAttendance(attendance: AttendanceRecord)
    suspend fun deleteAttendance(attendance: AttendanceRecord)

    // Task operations
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByStatus(status: String): Flow<List<Task>>
    fun getTasksByWorker(workerId: Long): Flow<List<Task>>
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun updateTaskStatus(id: Long, status: String, completedDate: LocalDate? = null)
}
