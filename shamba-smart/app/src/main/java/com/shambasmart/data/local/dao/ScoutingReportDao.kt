package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.ScoutingReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoutingReportDao {

    @Query("SELECT * FROM scouting_reports ORDER BY detectedAt DESC")
    fun getAllReports(): Flow<List<ScoutingReport>>

    @Query("SELECT * FROM scouting_reports WHERE id = :id")
    suspend fun getReportById(id: Long): ScoutingReport?

    @Query("SELECT * FROM scouting_reports WHERE plotId = :plotId ORDER BY detectedAt DESC")
    fun getReportsByPlot(plotId: Long): Flow<List<ScoutingReport>>

    @Query("SELECT * FROM scouting_reports WHERE pestType = :pestType ORDER BY detectedAt DESC")
    fun getReportsByPestType(pestType: String): Flow<List<ScoutingReport>>

    @Query("SELECT * FROM scouting_reports WHERE severityScore >= 4 ORDER BY detectedAt DESC")
    fun getCriticalReports(): Flow<List<ScoutingReport>>

    @Query("SELECT * FROM scouting_reports WHERE detectedAt BETWEEN :startDate AND :endDate ORDER BY detectedAt DESC")
    fun getReportsInDateRange(startDate: Long, endDate: Long): Flow<List<ScoutingReport>>

    @Query("SELECT * FROM scouting_reports WHERE isResolved = 0 ORDER BY detectedAt DESC")
    fun getUnresolvedReports(): Flow<List<ScoutingReport>>

    @Query("SELECT * FROM scouting_reports WHERE isSynced = 0")
    suspend fun getUnsyncedReports(): List<ScoutingReport>

    /**
     * Watermark-based delta sync: get rows modified after a given timestamp.
     */
    @Query("SELECT * FROM scouting_reports WHERE last_updated > :timestamp")
    suspend fun getRowsModifiedAfter(timestamp: Long): List<ScoutingReport>

    /**
     * Get heatmap data: aggregate reports by pest type and severity.
     */
    @Query("SELECT pestType, severityScore, gpsLatitude, gpsLongitude FROM scouting_reports WHERE isResolved = 0")
    suspend fun getHeatmapData(): List<ScoutingHeatmapPoint>

    /**
     * Count reports by severity for dashboard.
     */
    @Query("SELECT COUNT(*) FROM scouting_reports WHERE severityScore = :score")
    fun getCountBySeverity(score: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM scouting_reports WHERE isResolved = 0")
    fun getUnresolvedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScoutingReport): Long

    @Update
    suspend fun updateReport(report: ScoutingReport)

    @Delete
    suspend fun deleteReport(report: ScoutingReport)

    @Query("UPDATE scouting_reports SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE scouting_reports SET isResolved = :resolved, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markResolved(id: Long, resolved: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE scouting_reports SET managementApplied = :protocol, updatedAt = :updatedAt WHERE id = :id")
    suspend fun applyManagement(id: Long, protocol: String, updatedAt: Long = System.currentTimeMillis())
}

/**
 * Data class for heatmap overlay points.
 */
data class ScoutingHeatmapPoint(
    val pestType: String,
    val severityScore: Int,
    val gpsLatitude: Double,
    val gpsLongitude: Double
)