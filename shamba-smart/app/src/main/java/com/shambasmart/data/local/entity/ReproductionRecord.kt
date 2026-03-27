package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "reproduction_records",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["damId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["sireId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("damId"), Index("sireId")]
)
data class ReproductionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val damId: Long,
    val sireId: Long? = null,
    val type: String,
    val matingDate: LocalDate? = null,
    val pregnancyConfirmed: Boolean? = null,
    val expectedDueDate: LocalDate? = null,
    val actualBirthDate: LocalDate? = null,
    val numberOfKids: Int? = null,
    val numberOfAlive: Int? = null,
    val numberOfStillborn: Int? = null,
    val birthWeight: Double? = null,
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
