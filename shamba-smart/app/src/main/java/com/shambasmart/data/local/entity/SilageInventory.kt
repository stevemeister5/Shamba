package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "silage_inventory")
data class SilageInventory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pitId: String,
    val fillDate: LocalDate,
    val cropType: String,
    val estimatedTonnage: Double,
    val feedOutStartDate: LocalDate? = null,
    val currentTonnage: Double? = null,
    val qualityNotes: String? = null,
    val fermentationComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)