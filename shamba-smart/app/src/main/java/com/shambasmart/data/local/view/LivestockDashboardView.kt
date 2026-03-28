package com.shambasmart.data.local.view

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Livestock dashboard data entity for storing pre-computed livestock data.
 * Updated periodically by background workers.
 */
@Entity(tableName = "livestock_dashboard")
data class LivestockDashboardView(
    @PrimaryKey
    val id: Int = 1,
    val goatCount: Int = 0,
    val sheepCount: Int = 0,
    val cattleCount: Int = 0,
    val chickenCount: Int = 0,
    val totalActive: Int = 0,
    val sickCount: Int = 0,
    val quarantinedCount: Int = 0,
    val avgWeightKg: Double? = null,
    val weeklyMilkYield: Double = 0.0,
    val upcomingBirths: Int = 0,
    val activeTreatments: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)