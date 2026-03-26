package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "milk_production",
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
data class MilkProduction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val animalId: Long,
    val date: LocalDate,
    val morningYield: Double? = null,
    val eveningYield: Double? = null,
    val totalYield: Double,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)