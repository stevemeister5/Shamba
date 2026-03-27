package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.ScoutingReportDao
import com.shambasmart.data.local.dao.ScoutingHeatmapPoint
import com.shambasmart.data.local.entity.ScoutingReport
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for pest scouting reports.
 * Provides data access layer between ViewModels and DAOs.
 */
@Singleton
class ScoutingRepository @Inject constructor(
    private val scoutingReportDao: ScoutingReportDao
) {

    fun getAllReports(): Flow<List<ScoutingReport>> = scoutingReportDao.getAllReports()

    suspend fun getReportById(id: Long): ScoutingReport? = scoutingReportDao.getReportById(id)

    fun getReportsByPlot(plotId: Long): Flow<List<ScoutingReport>> = scoutingReportDao.getReportsByPlot(plotId)

    fun getReportsByPestType(pestType: String): Flow<List<ScoutingReport>> = scoutingReportDao.getReportsByPestType(pestType)

    fun getCriticalReports(): Flow<List<ScoutingReport>> = scoutingReportDao.getCriticalReports()

    fun getReportsInDateRange(startDate: Long, endDate: Long): Flow<List<ScoutingReport>> = scoutingReportDao.getReportsInDateRange(startDate, endDate)

    fun getUnresolvedReports(): Flow<List<ScoutingReport>> = scoutingReportDao.getUnresolvedReports()

    fun getUnresolvedCount(): Flow<Int> = scoutingReportDao.getUnresolvedCount()

    suspend fun getHeatmapData(): List<ScoutingHeatmapPoint> = scoutingReportDao.getHeatmapData()

    suspend fun insertReport(report: ScoutingReport): Long = scoutingReportDao.insertReport(report)

    suspend fun updateReport(report: ScoutingReport) = scoutingReportDao.updateReport(report)

    suspend fun deleteReport(report: ScoutingReport) = scoutingReportDao.deleteReport(report)

    suspend fun markResolved(id: Long, resolved: Boolean) = scoutingReportDao.markResolved(id, resolved)

    suspend fun applyManagement(id: Long, protocol: String) = scoutingReportDao.applyManagement(id, protocol)
}