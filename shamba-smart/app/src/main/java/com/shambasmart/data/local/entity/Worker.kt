package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String,
    val contact: String? = null,
    val hireDate: LocalDate,
    val dailyRate: Double? = null,
    val monthlyRate: Double? = null,
    val isSeasonal: Boolean = false,
    val endDate: LocalDate? = null,
    val status: String = "active",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)