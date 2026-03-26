package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey
    val id: Int = 1,
    val lastSyncTimestamp: Long = 0,
    val syncInProgress: Boolean = false,
    val lastSyncError: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)