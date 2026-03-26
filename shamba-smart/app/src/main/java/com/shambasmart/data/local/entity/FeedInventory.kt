package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

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
    val isSynced: Boolean = false
)