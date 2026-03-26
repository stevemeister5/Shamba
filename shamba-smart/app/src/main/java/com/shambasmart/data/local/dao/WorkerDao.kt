package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.AttendanceRecord
import com.shambasmart.data.local.entity.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface WorkerDao {
    // Worker queries
    @Query("SELECT * FROM workers WHERE status = 'active' ORDER BY name ASC")
    fun getAllActiveWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: Long): Worker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkers(workers: List<Worker>)

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("UPDATE workers SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateWorkerStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    // Attendance queries
    @Query("SELECT * FROM attendance_records WHERE workerId = :workerId ORDER BY date DESC")
    fun getAttendanceByWorkerId(workerId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getAttendanceByDate(date: LocalDate): List<AttendanceRecord>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE workerId = :workerId AND status = 'present' AND date >= :startDate AND date <= :endDate")
    suspend fun getDaysWorked(workerId: Long, startDate: LocalDate, endDate: LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceRecord>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceRecord)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceRecord)

    // Sync queries
    @Query("UPDATE workers SET isSynced = :synced WHERE id = :id")
    suspend fun updateWorkerSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE attendance_records SET isSynced = :synced WHERE id = :id")
    suspend fun updateAttendanceSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM workers WHERE isSynced = 0")
    suspend fun getUnsyncedWorkers(): List<Worker>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0")
    suspend fun getUnsyncedAttendance(): List<AttendanceRecord>
}