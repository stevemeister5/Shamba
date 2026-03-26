package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.MaintenanceTask
import com.shambasmart.data.local.entity.MaintenanceType
import com.shambasmart.data.local.entity.MaintenanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceTaskDao {
    @Query("SELECT * FROM maintenance_tasks ORDER BY scheduledDate ASC")
    fun getAllMaintenanceTasks(): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE status = :status ORDER BY scheduledDate ASC")
    fun getTasksByStatus(status: MaintenanceStatus): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE type = :type ORDER BY scheduledDate ASC")
    fun getTasksByType(type: MaintenanceType): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE scheduledDate <= :date AND status = 'SCHEDULED' ORDER BY scheduledDate ASC")
    fun getUpcomingTasks(date: Long): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE scheduledDate < :date AND status = 'SCHEDULED' ORDER BY scheduledDate ASC")
    fun getOverdueTasks(date: Long): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE infrastructureId = :infrastructureId ORDER BY scheduledDate DESC")
    fun getTasksByInfrastructure(infrastructureId: String): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE type = 'DIPPING_TANK_CLEANING' AND status = 'SCHEDULED' ORDER BY scheduledDate ASC")
    fun getDippingTankCleaningSchedule(): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE type = 'EQUIPMENT_SERVICING' AND status = 'SCHEDULED' ORDER BY scheduledDate ASC")
    fun getEquipmentServicingSchedule(): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE id = :id")
    suspend fun getMaintenanceTaskById(id: Long): MaintenanceTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maintenanceTask: MaintenanceTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(maintenanceTasks: List<MaintenanceTask>)

    @Update
    suspend fun update(maintenanceTask: MaintenanceTask)

    @Delete
    suspend fun delete(maintenanceTask: MaintenanceTask)

    @Query("UPDATE maintenance_tasks SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: MaintenanceStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE maintenance_tasks SET completedDate = :completedDate, actualDurationHours = :actualDuration, status = 'COMPLETED', updatedAt = :updatedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, completedDate: Long, actualDuration: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE maintenance_tasks SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM maintenance_tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<MaintenanceTask>

    @Query("SELECT COUNT(*) FROM maintenance_tasks WHERE status = 'SCHEDULED' AND scheduledDate <= :date")
    suspend fun getUpcomingTaskCount(date: Long): Int

    @Query("SELECT COUNT(*) FROM maintenance_tasks WHERE status = 'SCHEDULED' AND scheduledDate < :date")
    suspend fun getOverdueTaskCount(date: Long): Int

    @Query("SELECT SUM(cost) FROM maintenance_tasks WHERE status = 'COMPLETED' AND completedDate BETWEEN :startDate AND :endDate")
    suspend fun getTotalMaintenanceCost(startDate: Long, endDate: Long): Double?
}