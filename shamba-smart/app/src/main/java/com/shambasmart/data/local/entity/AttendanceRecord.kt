package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workerId")]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: Long,
    val date: LocalDate,
    val status: String,
    val reason: String? = null,
    @ColumnInfo(name = "daily_rate_snapshot")
    val dailyRateSnapshot: Long = 0L,  // TZS rate at time of work
    @ColumnInfo(name = "overtime_rate_snapshot")
    val overtimeRateSnapshot: Long = 0L,  // overtime rate at time of work
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
