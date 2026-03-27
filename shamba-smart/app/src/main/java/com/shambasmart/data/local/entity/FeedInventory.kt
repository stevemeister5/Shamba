package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(tableName = "feed_inventory")
data class FeedInventory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feedType: String,
    val stockLevel: Double,
    val unit: String,
    val reorderThreshold: Double? = null,
    val lastRestockDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val costPerUnit: Double? = null,
    val notes: String? = null,
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
