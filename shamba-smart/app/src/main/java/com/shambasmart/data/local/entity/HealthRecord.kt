package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("animalId")]
)
data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val animalId: Long,
    val type: String,
    val description: String? = null,
    val vaccineName: String? = null,
    val batchNumber: String? = null,
    val date: LocalDate,
    val nextDueDate: LocalDate? = null,
    val veterinarian: String? = null,
    val cost: Double? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)