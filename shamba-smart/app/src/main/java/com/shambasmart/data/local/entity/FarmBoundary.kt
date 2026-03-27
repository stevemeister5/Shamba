package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Farm boundary polygon stored as GPS coordinates
 * 
 * Each plot can have one boundary polygon defined by
 * a series of GPS points. The boundary is used for:
 * - Area calculation
 * - Visual display on maps
 * - GPS-based location verification
 */
@Entity(
    tableName = "farm_boundaries",
    foreignKeys = [
        ForeignKey(
            entity = Plot::class,
            parentColumns = ["id"],
            childColumns = ["plotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plotId")]
)
data class FarmBoundary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plotId: Long,
    val name: String,
    val areaAcres: Double,
    val areaSquareMeters: Double,
    val perimeterMeters: Double,
    val pointCount: Int,
    val centroidLatitude: Double,
    val centroidLongitude: Double,
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    // Revision-based delta sync fields
    @ColumnInfo(name = "revision_id")
    val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Individual boundary point with GPS coordinates
 * 
 * Each point represents a corner or significant location
 * on the farm boundary polygon.
 */
@Entity(
    tableName = "boundary_points",
    foreignKeys = [
        ForeignKey(
            entity = FarmBoundary::class,
            parentColumns = ["id"],
            childColumns = ["boundaryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("boundaryId")]
)
data class BoundaryPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val boundaryId: Long,
    val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Double,
    val timestamp: Long,
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