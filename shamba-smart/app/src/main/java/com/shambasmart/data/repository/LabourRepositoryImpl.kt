package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.TaskDao
import com.shambasmart.data.local.dao.WorkerDao
import com.shambasmart.data.local.entity.AttendanceRecord
import com.shambasmart.data.local.entity.Task
import com.shambasmart.data.local.entity.Worker
import com.shambasmart.domain.repository.LabourRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabourRepositoryImpl @Inject constructor(
    private val workerDao: WorkerDao,
    private val taskDao: TaskDao
) : LabourRepository {

    // Worker operations
    override fun getAllActiveWorkers(): Flow<List<Worker>> = workerDao.getAllActiveWorkers()

    override suspend fun getWorkerById(id: Long): Worker? = workerDao.getWorkerById(id)

    override suspend fun insertWorker(worker: Worker): Long {
        val now = System.currentTimeMillis()
        return workerDao.insertWorker(worker.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateWorker(worker: Worker) {
        workerDao.updateWorker(worker.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteWorker(worker: Worker) = workerDao.deleteWorker(worker)

    override suspend fun updateWorkerStatus(id: Long, status: String) {
        workerDao.updateWorkerStatus(id, status)
    }

    // Attendance operations
    override fun getAttendanceByWorkerId(workerId: Long): Flow<List<AttendanceRecord>> =
        workerDao.getAttendanceByWorkerId(workerId)

    override suspend fun getDaysWorked(workerId: Long, startDate: LocalDate, endDate: LocalDate): Int =
        workerDao.getDaysWorked(workerId, startDate, endDate)

    override suspend fun insertAttendance(attendance: AttendanceRecord): Long {
        return workerDao.insertAttendance(attendance.copy(isSynced = false))
    }

    override suspend fun updateAttendance(attendance: AttendanceRecord) {
        workerDao.updateAttendance(attendance.copy(isSynced = false))
    }

    override suspend fun deleteAttendance(attendance: AttendanceRecord) =
        workerDao.deleteAttendance(attendance)

    // Task operations
    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTasksByStatus(status: String): Flow<List<Task>> = taskDao.getTasksByStatus(status)

    override fun getTasksByWorker(workerId: Long): Flow<List<Task>> = taskDao.getTasksByWorker(workerId)

    override suspend fun insertTask(task: Task): Long {
        val now = System.currentTimeMillis()
        return taskDao.insert(task.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)

    override suspend fun updateTaskStatus(id: Long, status: String, completedDate: LocalDate?) {
        taskDao.updateStatus(id, status, completedDate)
    }
}
