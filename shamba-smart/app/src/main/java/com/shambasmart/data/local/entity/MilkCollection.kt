package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "milk_collections",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["source_animal_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("source_animal_id")]
)
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
    @ColumnInfo(name = "source_animal_id")
    val sourceAnimalId: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    // Revision-based delta sync fields
    @ColumnInfo(name = "revision_id")
    val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
