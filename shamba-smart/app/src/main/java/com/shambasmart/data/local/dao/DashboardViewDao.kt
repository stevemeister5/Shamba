package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.view.DashboardView
import com.shambasmart.data.local.view.PlotAnalyticsView
import com.shambasmart.data.local.view.LivestockDashboardView
import kotlinx.coroutines.flow.Flow

/**
 * DAO for database views - provides optimized read access to pre-joined data.
 */
@Dao
interface DashboardViewDao {

    @Query("SELECT * FROM DashboardView WHERE id = 1")
    fun getDashboardData(): Flow<DashboardView?>

    @Query("SELECT * FROM DashboardView WHERE id = 1")
    suspend fun getDashboardDataSync(): DashboardView?

    @Query("SELECT * FROM PlotAnalyticsView ORDER BY plot_name ASC")
    fun getAllPlotAnalytics(): Flow<List<PlotAnalyticsView>>

    @Query("SELECT * FROM PlotAnalyticsView WHERE plot_id = :plotId")
    suspend fun getPlotAnalyticsById(plotId: Long): PlotAnalyticsView?

    @Query("SELECT * FROM PlotAnalyticsView WHERE active_pest_reports > 0 ORDER BY max_pest_severity DESC")
    fun getPlotsWithActivePests(): Flow<List<PlotAnalyticsView>>

    @Query("SELECT * FROM LivestockDashboardView WHERE id = 1")
    fun getLivestockDashboardData(): Flow<LivestockDashboardView?>

    @Query("SELECT * FROM LivestockDashboardView WHERE id = 1")
    suspend fun getLivestockDashboardDataSync(): LivestockDashboardView?
}