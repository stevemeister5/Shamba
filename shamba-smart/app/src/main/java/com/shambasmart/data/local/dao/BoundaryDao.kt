package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.BoundaryPointEntity
import com.shambasmart.data.local.entity.FarmBoundary
import kotlinx.coroutines.flow.Flow

@Dao
interface BoundaryDao {
    // FarmBoundary queries
    @Query("SELECT * FROM farm_boundaries WHERE plotId = :plotId LIMIT 1")
    suspend fun getBoundaryByPlotId(plotId: Long): FarmBoundary?

    @Query("SELECT * FROM farm_boundaries WHERE plotId = :plotId")
    fun getBoundaryByPlotIdFlow(plotId: Long): Flow<FarmBoundary?>

    @Query("SELECT * FROM farm_boundaries ORDER BY updatedAt DESC")
    fun getAllBoundaries(): Flow<List<FarmBoundary>>

    @Query("SELECT * FROM farm_boundaries WHERE id = :id")
    suspend fun getBoundaryById(id: Long): FarmBoundary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoundary(boundary: FarmBoundary): Long

    @Update
    suspend fun updateBoundary(boundary: FarmBoundary)

    @Delete
    suspend fun deleteBoundary(boundary: FarmBoundary)

    @Query("DELETE FROM farm_boundaries WHERE plotId = :plotId")
    suspend fun deleteBoundaryByPlotId(plotId: Long)

    // BoundaryPoint queries
    @Query("SELECT * FROM boundary_points WHERE boundaryId = :boundaryId ORDER BY pointIndex ASC")
    suspend fun getPointsByBoundaryId(boundaryId: Long): List<BoundaryPointEntity>

    @Query("SELECT * FROM boundary_points WHERE boundaryId = :boundaryId ORDER BY pointIndex ASC")
    fun getPointsByBoundaryIdFlow(boundaryId: Long): Flow<List<BoundaryPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: BoundaryPointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPoints(points: List<BoundaryPointEntity>)

    @Update
    suspend fun updatePoint(point: BoundaryPointEntity)

    @Delete
    suspend fun deletePoint(point: BoundaryPointEntity)

    @Query("DELETE FROM boundary_points WHERE boundaryId = :boundaryId")
    suspend fun deletePointsByBoundaryId(boundaryId: Long)

    // Sync queries
    @Query("UPDATE farm_boundaries SET isSynced = :synced WHERE id = :id")
    suspend fun updateBoundarySyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE boundary_points SET isSynced = :synced WHERE id = :id")
    suspend fun updatePointSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM farm_boundaries WHERE isSynced = 0")
    suspend fun getUnsyncedBoundaries(): List<FarmBoundary>

    @Query("SELECT * FROM boundary_points WHERE isSynced = 0")
    suspend fun getUnsyncedPoints(): List<BoundaryPointEntity>

    // Aggregate queries
    @Query("SELECT COUNT(*) FROM farm_boundaries")
    suspend fun getBoundaryCount(): Int

    @Query("SELECT SUM(areaAcres) FROM farm_boundaries")
    suspend fun getTotalAreaAcres(): Double?

    @Query("SELECT * FROM farm_boundaries WHERE plotId IN (SELECT id FROM plots WHERE currentUse = :cropType)")
    fun getBoundariesByCropType(cropType: String): Flow<List<FarmBoundary>>
}