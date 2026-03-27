package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

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
    val isSynced: Boolean = false,
    // Revision-based delta sync fields
    @ColumnInfo(name = "revision_id")
    val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
