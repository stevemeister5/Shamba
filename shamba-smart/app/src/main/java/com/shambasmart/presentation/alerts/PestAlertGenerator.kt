package com.shambasmart.presentation.alerts

import com.shambasmart.data.local.dao.ScoutingReportDao
import com.shambasmart.data.local.entity.ScoutingReport
import com.shambasmart.maarifa.retrieval.PestKnowledgeMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates actionable alerts for critical pest detections.
 * Monitors ScoutingReports and generates dashboard notifications
 * when "Critical" severity is logged.
 */
@Singleton
class PestAlertGenerator @Inject constructor(
    private val scoutingReportDao: ScoutingReportDao,
    private val pestKnowledgeMapper: PestKnowledgeMapper
) {

    /**
     * Gets all active pest alerts (unresolved critical reports).
     * @return Flow of PestAlert objects
     */
    fun getActiveAlerts(): Flow<List<PestAlert>> {
        return scoutingReportDao.getCriticalReports().map { reports ->
            reports.map { report -> createAlert(report) }
        }
    }

    /**
     * Gets alerts for a specific plot.
     * @param plotId The plot ID to filter by
     * @return Flow of PestAlert objects
     */
    fun getAlertsForPlot(plotId: Long): Flow<List<PestAlert>> {
        return scoutingReportDao.getReportsByPlot(plotId).map { reports ->
            reports
                .filter { it.severityScore >= 4 }
                .map { report -> createAlert(report) }
        }
    }

    /**
     * Gets unresolved alert count for dashboard.
     * @return Flow of alert count
     */
    fun getUnresolvedAlertCount(): Flow<Int> {
        return scoutingReportDao.getUnresolvedCount()
    }

    /**
     * Creates a PestAlert from a ScoutingReport.
     */
    private fun createAlert(report: ScoutingReport): PestAlert {
        val protocol = pestKnowledgeMapper.getProtocol(report.pestType)
        val managementAdvice = pestKnowledgeMapper.getProtocolBySeverity(
            report.pestType,
            report.severityScore
        )

        return PestAlert(
            id = report.id,
            plotId = report.plotId,
            pestType = report.pestType,
            pestDisplayName = protocol.displayName,
            severityScore = report.severityScore,
            severityLabel = getSeverityLabel(report.severityScore),
            description = protocol.description,
            managementAdvice = managementAdvice,
            detectedAt = report.detectedAt,
            gpsLatitude = report.gpsLatitude,
            gpsLongitude = report.gpsLongitude,
            isResolved = report.isResolved,
            managementApplied = report.managementApplied
        )
    }

    private fun getSeverityLabel(score: Int): String = when (score) {
        1 -> "Low"
        2 -> "Minor"
        3 -> "Moderate"
        4 -> "Severe"
        5 -> "Critical"
        else -> "Unknown"
    }
}

/**
 * Pest alert data class for UI display.
 */
data class PestAlert(
    val id: Long,
    val plotId: Long,
    val pestType: String,
    val pestDisplayName: String,
    val severityScore: Int,
    val severityLabel: String,
    val description: String,
    val managementAdvice: String,
    val detectedAt: Long,
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    val isResolved: Boolean,
    val managementApplied: String?
)