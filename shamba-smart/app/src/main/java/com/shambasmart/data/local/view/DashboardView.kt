package com.shambasmart.data.local.view

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Dashboard KPI data entity.
 * Stores aggregated dashboard data for quick access.
 * Updated periodically by background workers.
 */
@Entity(tableName = "dashboard_kpi")
data class DashboardView(
    @PrimaryKey
    val id: Int = 1,
    val herdSize: Int = 0,
    val goatCount: Int = 0,
    val sheepCount: Int = 0,
    val todayMilkYield: Double = 0.0,
    val cheeseBatchesInAging: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val lowFeedAlerts: Int = 0,
    val criticalPestAlerts: Int = 0,
    val todayEvents: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)