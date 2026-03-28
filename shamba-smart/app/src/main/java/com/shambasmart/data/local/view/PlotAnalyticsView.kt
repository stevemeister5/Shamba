package com.shambasmart.data.local.view

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Plot analytics data entity for storing pre-computed plot data.
 * Updated periodically by background workers.
 */
@Entity(tableName = "plot_analytics")
data class PlotAnalyticsView(
    @PrimaryKey
    val plotId: Long = 0,
    val plotName: String = "",
    val sizeAcres: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val healthScore: Double? = null,
    val performanceIndex: Double? = null,
    val cropType: String? = null,
    val variety: String? = null,
    val plantingDate: String? = null,
    val expectedHarvestDate: String? = null,
    val plantingStatus: String? = null,
    val annualYieldKg: Double = 0.0,
    val activePestReports: Int = 0,
    val maxPestSeverity: Int? = null,
    val harvestsThisYear: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)