package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.CropPlanting
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crop_plantings WHERE plotId = :plotId ORDER BY plantingDate DESC")
    fun getCropsByPlotId(plotId: Long): Flow<List<CropPlanting>>

    @Query("SELECT * FROM crop_plantings WHERE status = :status")
    fun getCropsByStatus(status: String): Flow<List<CropPlanting>>

    @Query("SELECT * FROM crop_plantings WHERE id = :id")
    suspend fun getCropById(id: Long): CropPlanting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crop: CropPlanting): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crops: List<CropPlanting>)

    @Update
    suspend fun update(crop: CropPlanting)

    @Delete
    suspend fun delete(crop: CropPlanting)

    @Query("UPDATE crop_plantings SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE crop_plantings SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM crop_plantings WHERE isSynced = 0")
    suspend fun getUnsyncedCrops(): List<CropPlanting>
}