package com.shambasmart.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

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
    suspend fun performSync() = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext
        }

        // Update sync status
        syncDao.updateSyncInProgress(true)

        try {
            // Get last sync timestamp
            val syncStatus = syncDao.getSyncStatus()
            val lastSync = syncStatus?.lastSyncTimestamp ?: 0L

            // Sync all unsynced data
            syncUnsyncedData()

            // Update last sync timestamp
            syncDao.updateLastSyncTimestamp(System.currentTimeMillis())
            syncDao.updateSyncInProgress(false)
            syncDao.updateSyncError(null)
        } catch (e: Exception) {
            syncDao.updateSyncInProgress(false)
            syncDao.updateSyncError(e.message)
            throw e
        }
    }

    private suspend fun syncUnsyncedData() {
        // Get all unsynced data
        val unsyncedAnimals = animalDao.getUnsyncedAnimals()
        val unsyncedHealthRecords = healthRecordDao.getUnsyncedRecords()
        val unsyncedReproduction = reproductionDao.getUnsyncedRecords()
        val unsyncedMilkProduction = milkProductionDao.getUnsyncedRecords()
        val unsyncedPlots = plotDao.getUnsyncedPlots()
        val unsyncedCrops = cropDao.getUnsyncedCrops()
        val unsyncedHarvests = harvestDao.getUnsyncedHarvests()
        val unsyncedSilage = silageDao.getUnsyncedSilage()
        val unsyncedWeather = weatherDao.getUnsyncedWeatherLogs()
        val unsyncedBatches = cheeseDao.getUnsyncedBatches()
        val unsyncedCollections = cheeseDao.getUnsyncedCollections()
        val unsyncedFeed = feedDao.getUnsyncedFeed()
        val unsyncedStoreItems = storeDao.getUnsyncedItems()
        val unsyncedIncome = financialDao.getUnsyncedIncome()
        val unsyncedExpenses = financialDao.getUnsyncedExpenses()
        val unsyncedLoans = financialDao.getUnsyncedLoans()
        val unsyncedWorkers = workerDao.getUnsyncedWorkers()
        val unsyncedAttendance = workerDao.getUnsyncedAttendance()
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        val unsyncedEvents = calendarDao.getUnsyncedEvents()

        // TODO: Send to remote server and mark as synced
        // For now, just mark all as synced locally
        unsyncedAnimals.forEach { animalDao.updateSyncStatus(it.id, true) }
        unsyncedHealthRecords.forEach { healthRecordDao.updateSyncStatus(it.id, true) }
        unsyncedReproduction.forEach { reproductionDao.updateSyncStatus(it.id, true) }
        unsyncedMilkProduction.forEach { milkProductionDao.updateSyncStatus(it.id, true) }
        unsyncedPlots.forEach { plotDao.updateSyncStatus(it.id, true) }
        unsyncedCrops.forEach { cropDao.updateSyncStatus(it.id, true) }
        unsyncedHarvests.forEach { harvestDao.updateSyncStatus(it.id, true) }
        unsyncedSilage.forEach { silageDao.updateSyncStatus(it.id, true) }
        unsyncedWeather.forEach { weatherDao.updateSyncStatus(it.id, true) }
        unsyncedBatches.forEach { cheeseDao.updateBatchSyncStatus(it.id, true) }
        unsyncedCollections.forEach { cheeseDao.updateCollectionSyncStatus(it.id, true) }
        unsyncedFeed.forEach { feedDao.updateSyncStatus(it.id, true) }
        unsyncedStoreItems.forEach { storeDao.updateSyncStatus(it.id, true) }
        unsyncedIncome.forEach { financialDao.updateIncomeSyncStatus(it.id, true) }
        unsyncedExpenses.forEach { financialDao.updateExpenseSyncStatus(it.id, true) }
        unsyncedLoans.forEach { financialDao.updateLoanSyncStatus(it.id, true) }
        unsyncedWorkers.forEach { workerDao.updateWorkerSyncStatus(it.id, true) }
        unsyncedAttendance.forEach { workerDao.updateAttendanceSyncStatus(it.id, true) }
        unsyncedTasks.forEach { taskDao.updateSyncStatus(it.id, true) }
        unsyncedEvents.forEach { calendarDao.updateSyncStatus(it.id, true) }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        const val SYNC_WORK_NAME = "shamba_smart_sync"
        val SYNC_INTERVAL = TimeUnit.MINUTES.toMinutes(15)
    }
}