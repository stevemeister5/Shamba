package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "harvest_records",
    foreignKeys = [
        ForeignKey(
            entity = CropPlanting::class,
            parentColumns = ["id"],
            childColumns = ["cropPlantingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cropPlantingId")]
)
data class HarvestRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropPlantingId: Long,
    val harvestDate: LocalDate,
    val quantityKg: Double,
    val qualityGrade: String? = null,
    val destination: String? = null,
    val pricePerKg: Double? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)