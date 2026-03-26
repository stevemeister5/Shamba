package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagId: String? = null,
    val species: String,
    val breed: String? = null,
    val sex: String,
    val dateOfBirth: LocalDate? = null,
    val weight: Double? = null,
    val photoPath: String? = null,
    val status: String = "active",
    val parentId: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)