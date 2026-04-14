package com.shambasmart.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shambasmart.data.local.dao.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.coroutines.flow.first

@HiltWorker
class DashboardRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val animalDao: AnimalDao,
    private val milkDao: MilkProductionDao,
    private val cheeseDao: CheeseDao,
    private val taskDao: TaskDao,
    private val feedDao: FeedDao,
    private val scoutingDao: ScoutingReportDao,
    private val dashboardDao: DashboardViewDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            
            // Collect current counts/sums
            val herdSize = animalDao.getActiveAnimalCount().first()
            val goats = animalDao.getCountBySpecies("Goat").first()
            val sheep = animalDao.getCountBySpecies("Sheep").first()
            val milkToday = milkDao.getTotalYieldByDate(today) ?: 0.0
            val cheeseStock = cheeseDao.getTotalAvailableCheese()?.toInt() ?: 0
            val pendingTasks = taskDao.getPendingTaskCount(today)
            val overdue = taskDao.getOverdueTasks(today).size
            val lowFeed = feedDao.getLowStockCountSync()
            val pests = scoutingDao.getCriticalPestCountSync()

            // Update the KPI view
            dashboardDao.refreshDashboardKpis(
                herdSize = herdSize,
                goatCount = goats,
                sheepCount = sheep,
                todayMilk = milkToday,
                cheeseStock = cheeseStock,
                pendingTasks = pendingTasks,
                overdueTasks = overdue,
                lowFeed = lowFeed,
                pests = pests
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
