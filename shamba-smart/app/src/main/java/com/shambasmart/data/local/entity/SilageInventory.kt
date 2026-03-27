package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

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
    val isSynced: Boolean = false,
    // Revision-based delta sync fields
    @ColumnInfo(name = "revision_id")
    val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
