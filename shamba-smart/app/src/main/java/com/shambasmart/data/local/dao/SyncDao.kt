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
}