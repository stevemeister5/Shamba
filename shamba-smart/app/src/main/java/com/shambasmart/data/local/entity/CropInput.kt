package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "crop_inputs",
    foreignKeys = [
        ForeignKey(
            entity = CropPlanting::class,
            parentColumns = ["id"],
            childColumns = ["plantingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantingId")]
)
data class CropInput(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantingId: Long,
    val inputType: String,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val cost: Double? = null,
    val date: LocalDate,
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