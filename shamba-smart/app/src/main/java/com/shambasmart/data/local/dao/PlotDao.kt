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
}