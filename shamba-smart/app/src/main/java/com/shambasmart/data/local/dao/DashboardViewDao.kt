package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.view.DashboardView
import com.shambasmart.data.local.view.PlotAnalyticsView
import com.shambasmart.data.local.view.LivestockDashboardView
import kotlinx.coroutines.flow.Flow

/**
 * DAO for dashboard KPI data - provides access to pre-computed dashboard metrics.
 */
@Dao
interface DashboardViewDao {

    @Query("SELECT * FROM dashboard_kpi WHERE id = 1")
    fun getDashboardData(): Flow<DashboardView?>

    @Query("SELECT * FROM dashboard_kpi WHERE id = 1")
    suspend fun getDashboardDataSync(): DashboardView?

    @Query("SELECT * FROM plot_analytics ORDER BY plotName ASC")
    fun getAllPlotAnalytics(): Flow<List<PlotAnalyticsView>>

    @Query("SELECT * FROM plot_analytics WHERE plotId = :plotId")
    suspend fun getPlotAnalyticsById(plotId: Long): PlotAnalyticsView?

    @Query("SELECT * FROM plot_analytics WHERE activePestReports > 0 ORDER BY maxPestSeverity DESC")
    fun getPlotsWithActivePests(): Flow<List<PlotAnalyticsView>>

    @Query("SELECT * FROM livestock_dashboard WHERE id = 1")
    fun getLivestockDashboardData(): Flow<LivestockDashboardView?>

    @Query("SELECT * FROM livestock_dashboard WHERE id = 1")
    suspend fun getLivestockDashboardDataSync(): LivestockDashboardView?
    
    // Insert/Update operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDashboard(data: DashboardView)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlotAnalytics(data: PlotAnalyticsView)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLivestockDashboard(data: LivestockDashboardView)

    // Manual Refresh Logic (can be called from WorkManager or ViewModels)
    @Transaction
    suspend fun refreshDashboardKpis(
        herdSize: Int,
        goatCount: Int,
        sheepCount: Int,
        todayMilk: Double,
        cheeseStock: Int,
        pendingTasks: Int,
        overdueTasks: Int,
        lowFeed: Int,
        pests: Int
    ) {
        val current = getDashboardDataSync() ?: DashboardView(id = 1)
        insertOrUpdateDashboard(current.copy(
            herdSize = herdSize,
            goatCount = goatCount,
            sheepCount = sheepCount,
            todayMilkYield = todayMilk,
            cheeseBatchesInAging = cheeseStock,
            pendingTasks = pendingTasks,
            overdueTasks = overdueTasks,
            lowFeedAlerts = lowFeed,
            criticalPestAlerts = pests,
            lastUpdated = System.currentTimeMillis()
        ))
    }
}