package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "milk_collections")
data class MilkCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val quantityLitres: Double,
    val qualityCheck: String? = null,
    val smellOk: Boolean? = null,
    val colorOk: Boolean? = null,
    val phLevel: Double? = null,
    val accepted: Boolean = true,
    val rejectionReason: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)