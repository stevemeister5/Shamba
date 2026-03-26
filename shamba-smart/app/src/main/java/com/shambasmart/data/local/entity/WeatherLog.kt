package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "weather_logs")
data class WeatherLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val rainfallMm: Double? = null,
    val maxTemp: Double? = null,
    val minTemp: Double? = null,
    val windLevel: String? = null,
    val unusualEvents: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)