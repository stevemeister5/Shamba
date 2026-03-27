package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Pest scouting report generated from camera detection.
 * 
 * Auto-populated when a pest is detected via the ONNX vision pipeline.
 * Links to a Plot for GPS-based location tracking.
 */
@Entity(
    tableName = "scouting_reports",
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
data class ScoutingReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plotId: Long, // FK to Plot
    val pestType: String, // e.g., "fall_armyworm", "stalk_borer", "aphids"
    val severityScore: Int, // 1-5 scale (1=Low, 2=Minor, 3=Moderate, 4=Severe, 5=Critical)
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    val imageUri: String? = null, // Encrypted local path to compressed image
    val detectedAt: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val managementApplied: String? = null, // Protocol applied (from Maarifa)
    val isResolved: Boolean = false,
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
 * Severity level enum for pest detection.
 */
enum class SeverityLevel(val score: Int, val label: String) {
    LOW(1, "Low"),
    MINOR(2, "Minor"),
    MODERATE(3, "Moderate"),
    SEVERE(4, "Severe"),
    CRITICAL(5, "Critical");

    companion object {
        fun fromScore(score: Int): SeverityLevel = values().firstOrNull { it.score == score } ?: LOW
    }
}

/**
 * Result of pest detection inference.
 */
data class PestDetection(
    val pestClass: String, // e.g., "fall_armyworm"
    val confidence: Float,
    val boundingBox: BoundingBox,
    val severityLevel: SeverityLevel
)

/**
 * Bounding box for detected pest.
 */
data class BoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

/**
 * Complete inference result from vision pipeline.
 */
data class InferenceResult(
    val detections: List<PestDetection>,
    val processingTimeMs: Long,
    val modelVersion: String,
    val imageSize: Pair<Int, Int>
)