package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "store_items")
data class StoreItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val expiryDate: LocalDate? = null,
    val reorderLevel: Double? = null,
    val costPerUnit: Double? = null,
    val supplier: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)