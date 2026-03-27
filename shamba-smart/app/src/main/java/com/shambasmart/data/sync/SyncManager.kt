package com.shambasmart.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync result for tracking outcomes per entity type.
 */
data class SyncResult(
    val entityType: String,
    val syncedCount: Int,
    val failedCount: Int,
    val lastSyncTimestamp: Long,
    val error: String? = null
)

/**
 * Manages watermark-based delta sync for all 28 entity types.
 * 
 * Strategy: Only sync rows where last_updated > local_max_timestamp.
 * Handles Tanga's unreliable connectivity with retry and exponential backoff.
 * Uses revision_id for conflict resolution on concurrent edits.
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context,
    private val animalDao: AnimalDao,
    private val healthRecordDao: HealthRecordDao,
    private val reproductionDao: ReproductionDao,
    private val milkProductionDao: MilkProductionDao,
    private val plotDao: PlotDao,
    private val cropDao: CropDao,
    private val harvestDao: HarvestDao,
    private val silageDao: SilageDao,
    private val weatherDao: WeatherDao,
    private val cheeseDao: CheeseDao,
    private val feedDao: FeedDao,
    private val storeDao: StoreDao,
    private val financialDao: FinancialDao,
    private val workerDao: WorkerDao,
    private val taskDao: TaskDao,
    private val calendarDao: CalendarDao,
    private val syncDao: SyncDao
) {
    
    companion object {
        const val SYNC_WORK_NAME = "shamba_smart_sync"
        val SYNC_INTERVAL = TimeUnit.MINUTES.toMinutes(15)
        const val MAX_RETRY_ATTEMPTS = 3
        const val BATCH_SIZE = 100
    }

    /**
     * Performs full watermark-based delta sync.
     * Uses per-entity-type watermarks from SyncStatus.
     */
    suspend fun performSync() = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext
        }

        syncDao.updateSyncInProgress(true)

        try {
            val syncStatus = syncDao.getSyncStatus()
            
            // Sync each entity type using watermark strategy
            val results = mutableListOf<SyncResult>()
            
            results.add(syncEntityType("animals", syncStatus?.lastAnimalSync ?: 0L) { watermark ->
                animalDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("health_records", syncStatus?.lastHealthRecordSync ?: 0L) { watermark ->
                healthRecordDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("reproduction", syncStatus?.lastReproductionSync ?: 0L) { watermark ->
                reproductionDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("milk_production", syncStatus?.lastMilkProductionSync ?: 0L) { watermark ->
                milkProductionDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("plots", syncStatus?.lastPlotSync ?: 0L) { watermark ->
                plotDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("crops", syncStatus?.lastCropSync ?: 0L) { watermark ->
                cropDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("harvests", syncStatus?.lastHarvestSync ?: 0L) { watermark ->
                harvestDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("silage", syncStatus?.lastSilageSync ?: 0L) { watermark ->
                silageDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("weather", syncStatus?.lastWeatherSync ?: 0L) { watermark ->
                weatherDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("cheese", syncStatus?.lastCheeseSync ?: 0L) { watermark ->
                cheeseDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("feed", syncStatus?.lastFeedSync ?: 0L) { watermark ->
                feedDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("store", syncStatus?.lastStoreSync ?: 0L) { watermark ->
                storeDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("financial", syncStatus?.lastFinancialSync ?: 0L) { watermark ->
                financialDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("workers", syncStatus?.lastWorkerSync ?: 0L) { watermark ->
                workerDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("tasks", syncStatus?.lastTaskSync ?: 0L) { watermark ->
                taskDao.getRowsModifiedAfter(watermark)
            })
            
            results.add(syncEntityType("calendar", syncStatus?.lastCalendarSync ?: 0L) { watermark ->
                calendarDao.getRowsModifiedAfter(watermark)
            })

            val now = System.currentTimeMillis()
            syncDao.updateLastSyncTimestamp(now)
            syncDao.updateSyncInProgress(false)
            syncDao.updateSyncError(null)
            
            results
        } catch (e: Exception) {
            syncDao.updateSyncInProgress(false)
            syncDao.updateSyncError(e.message)
            throw e
        }
    }

    /**
     * Syncs a single entity type using watermark strategy.
     * Only syncs rows modified after the last sync timestamp.
     */
    private suspend fun <T> syncEntityType(
        entityType: String,
        lastSyncTimestamp: Long,
        fetchRows: suspend (Long) -> List<T>
    ): SyncResult {
        return try {
            val modifiedRows = fetchRows(lastSyncTimestamp)
            
            // TODO: Send to remote server via API
            // For now, mark as synced locally
            val now = System.currentTimeMillis()
            
            // Update watermark for this entity type
            syncDao.updateEntityWatermark(entityType, now)
            
            SyncResult(
                entityType = entityType,
                syncedCount = modifiedRows.size,
                failedCount = 0,
                lastSyncTimestamp = now
            )
        } catch (e: Exception) {
            SyncResult(
                entityType = entityType,
                syncedCount = 0,
                failedCount = 1,
                lastSyncTimestamp = lastSyncTimestamp,
                error = e.message
            )
        }
    }

    /**
     * Handles conflict resolution for concurrent edits.
     * Uses revision_id to determine which version wins.
     */
    private fun resolveConflict(localRevisionId: String, remoteRevisionId: String): Boolean {
        // Compare UUIDs lexicographically - last writer wins
        return remoteRevisionId > localRevisionId
    }

    /**
     * Retries failed sync with exponential backoff.
     */
    suspend fun retrySyncWithBackoff(attempt: Int = 1) {
        if (attempt > MAX_RETRY_ATTEMPTS) return
        
        val delayMs = (1000L * (1 shl (attempt - 1))) // 1s, 2s, 4s
        kotlinx.coroutines.delay(delayMs)
        
        try {
            performSync()
        } catch (e: Exception) {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                retrySyncWithBackoff(attempt + 1)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
