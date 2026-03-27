package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.SyncStatus

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_status WHERE id = 1")
    suspend fun getSyncStatus(): SyncStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncStatus(syncStatus: SyncStatus)

    @Update
    suspend fun updateSyncStatus(syncStatus: SyncStatus)

    @Query("UPDATE sync_status SET lastSyncTimestamp = :timestamp WHERE id = 1")
    suspend fun updateLastSyncTimestamp(timestamp: Long)

    @Query("UPDATE sync_status SET syncInProgress = :inProgress WHERE id = 1")
    suspend fun updateSyncInProgress(inProgress: Boolean)

    @Query("UPDATE sync_status SET lastSyncError = :error, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSyncError(error: String?, updatedAt: Long = System.currentTimeMillis())

    /**
     * Updates the watermark timestamp for a specific entity type.
     * Used by watermark-based delta sync.
     */
    @Query("UPDATE sync_status SET lastAnimalSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateAnimalWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastHealthRecordSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateHealthRecordWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastReproductionSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateReproductionWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastMilkProductionSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateMilkProductionWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastPlotSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updatePlotWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastCropSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateCropWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastHarvestSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateHarvestWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastSilageSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSilageWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastWeatherSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateWeatherWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastCheeseSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateCheeseWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastFeedSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateFeedWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastStoreSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateStoreWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastFinancialSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateFinancialWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastWorkerSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateWorkerWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastTaskSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateTaskWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastCalendarSync = :timestamp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateCalendarWatermark(timestamp: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * Generic method to update watermark by entity type.
     */
    suspend fun updateEntityWatermark(entityType: String, timestamp: Long) {
        when (entityType) {
            "animals" -> updateAnimalWatermark(timestamp)
            "health_records" -> updateHealthRecordWatermark(timestamp)
            "reproduction" -> updateReproductionWatermark(timestamp)
            "milk_production" -> updateMilkProductionWatermark(timestamp)
            "plots" -> updatePlotWatermark(timestamp)
            "crops" -> updateCropWatermark(timestamp)
            "harvests" -> updateHarvestWatermark(timestamp)
            "silage" -> updateSilageWatermark(timestamp)
            "weather" -> updateWeatherWatermark(timestamp)
            "cheese" -> updateCheeseWatermark(timestamp)
            "feed" -> updateFeedWatermark(timestamp)
            "store" -> updateStoreWatermark(timestamp)
            "financial" -> updateFinancialWatermark(timestamp)
            "workers" -> updateWorkerWatermark(timestamp)
            "tasks" -> updateTaskWatermark(timestamp)
            "calendar" -> updateCalendarWatermark(timestamp)
        }
    }
}
