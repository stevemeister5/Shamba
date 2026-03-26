package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "cheese_batches")
data class CheeseBatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: String,
    val productionDate: LocalDate,
    val milkVolumeUsed: Double,
    val cheeseType: String,
    val starterCulture: String? = null,
    val rennetUsed: String? = null,
    val yieldKg: Double,
    val agingStartDate: LocalDate? = null,
    val agingLocation: String? = null,
    val packagingDate: LocalDate? = null,
    val unitsPacked: Int? = null,
    val weightPerUnit: Double? = null,
    val status: String = "aging",
    val qualityNotes: String? = null,
    val outcomeRating: Int? = null,
    val costMilk: Double? = null,
    val costInputs: Double? = null,
    val costLabour: Double? = null,
    val costPackaging: Double? = null,
    val totalCost: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)