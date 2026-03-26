package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE assignedTo = :workerId")
    fun getTasksByWorker(workerId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate <= :date AND status != 'completed'")
    suspend fun getOverdueTasks(date: LocalDate): List<Task>

    @Query("SELECT * FROM tasks WHERE dueDate = :date")
    suspend fun getTasksByDate(date: LocalDate): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET status = :status, completedDate = :completedDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedDate: LocalDate? = null, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'pending' AND dueDate <= :date")
    suspend fun getPendingTaskCount(date: LocalDate): Int
}