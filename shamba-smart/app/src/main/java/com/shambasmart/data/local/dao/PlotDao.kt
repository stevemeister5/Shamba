package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.Plot
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots ORDER BY name ASC")
    fun getAllPlots(): Flow<List<Plot>>

    @Query("SELECT * FROM plots WHERE id = :id")
    suspend fun getPlotById(id: Long): Plot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plot: Plot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plots: List<Plot>)

    @Update
    suspend fun update(plot: Plot)

    @Delete
    suspend fun delete(plot: Plot)

    @Query("UPDATE plots SET currentCropId = :cropId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCurrentCrop(id: Long, cropId: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE plots SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM plots WHERE isSynced = 0")
    suspend fun getUnsyncedPlots(): List<Plot>

    @Query("SELECT SUM(sizeAcres) FROM plots")
    suspend fun getTotalAcres(): Double?

    // Digital Twin Benchmarking queries
    @Query("UPDATE plots SET performanceIndex = :performanceIndex, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePerformanceIndex(id: Long, performanceIndex: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE plots SET healthScore = :healthScore, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateHealthScore(id: Long, healthScore: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE plots SET lastYieldKg = :yieldKg, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLastYield(id: Long, yieldKg: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE plots SET baselineCropsPerM2 = :baseline, targetYieldKg = :target WHERE id = :id")
    suspend fun setBenchmarks(id: Long, baseline: Double, target: Double)

    @Query("UPDATE plots SET boundaryPoints = :points, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBoundaryPoints(id: Long, points: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE plots SET soilMoistureSensorId = :sensorId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setSensorId(id: Long, sensorId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM plots WHERE performanceIndex < :threshold ORDER BY performanceIndex ASC")
    fun getUnderperformingPlots(threshold: Double = 50.0): Flow<List<Plot>>

    // SyncManager support
    @Query("SELECT * FROM plots WHERE updatedAt > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<Plot>

    // ContextBridge support
    @Query("SELECT * FROM plots ORDER BY name ASC")
    suspend fun getAllPlotsList(): List<Plot>
}
