package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["assignedTo"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("assignedTo")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val assignedTo: Long? = null,
    val plotId: Long? = null,
    val animalGroupId: String? = null,
    val dueDate: LocalDate,
    val status: String = "pending",
    val completedDate: LocalDate? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)