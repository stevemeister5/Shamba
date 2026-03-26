package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "plots")
data class Plot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sizeAcres: Double,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val soilType: String? = null,
    val currentUse: String? = null,
    val currentCropId: Long? = null,
    val notes: String? = null,
    // Digital Twin Benchmarking Fields
    val soilMoistureSensorId: String? = null,
    val baselineCropsPerM2: Double? = null,
    val targetYieldKg: Double? = null,
    // Performance Tracking
    val lastYieldKg: Double? = null,
    val performanceIndex: Double? = null,
    val healthScore: Double? = null,
    // GPS Boundary
    val boundaryPoints: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
