package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "crop_plantings",
    foreignKeys = [
        ForeignKey(
            entity = Plot::class,
            parentColumns = ["id"],
            childColumns = ["plotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plotId")]
)
data class CropPlanting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plotId: Long,
    val cropType: String,
    val variety: String? = null,
    val plantingDate: LocalDate,
    val seedSource: String? = null,
    val seedQuantity: Double? = null,
    val expectedHarvestDate: LocalDate? = null,
    val actualHarvestDate: LocalDate? = null,
    val status: String = "growing",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)