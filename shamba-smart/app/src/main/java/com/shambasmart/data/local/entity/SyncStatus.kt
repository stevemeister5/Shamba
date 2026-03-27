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
    val updatedAt: Long = System.currentTimeMillis(),
    // Watermark timestamps for delta sync per entity type
    val lastAnimalSync: Long = 0,
    val lastHealthRecordSync: Long = 0,
    val lastReproductionSync: Long = 0,
    val lastMilkProductionSync: Long = 0,
    val lastPlotSync: Long = 0,
    val lastCropSync: Long = 0,
    val lastHarvestSync: Long = 0,
    val lastSilageSync: Long = 0,
    val lastWeatherSync: Long = 0,
    val lastCheeseSync: Long = 0,
    val lastFeedSync: Long = 0,
    val lastStoreSync: Long = 0,
    val lastFinancialSync: Long = 0,
    val lastWorkerSync: Long = 0,
    val lastTaskSync: Long = 0,
    val lastCalendarSync: Long = 0,
    val lastAudioEventSync: Long = 0,
    val lastMaintenanceTaskSync: Long = 0,
    val lastKnowledgeChunkSync: Long = 0,
    val lastOperationalRuleSync: Long = 0,
    val lastFarmBoundarySync: Long = 0,
    val lastBoundaryPointSync: Long = 0,
    val lastMapMarkerSync: Long = 0,
    val lastMapLayerSync: Long = 0,
    val lastMapTileCacheSync: Long = 0,
    val lastScoutingReportSync: Long = 0
)
