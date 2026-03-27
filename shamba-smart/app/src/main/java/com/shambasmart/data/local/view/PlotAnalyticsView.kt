package com.shambasmart.data.local.view

import androidx.room.DatabaseView

/**
 * Pre-joined view for Plot Analytics data.
 * Aggregates crop/yield data per plot for efficient analytics queries.
 */
@DatabaseView(
    """
    SELECT 
        p.id as plot_id,
        p.name as plot_name,
        p.sizeAcres,
        p.latitude,
        p.longitude,
        p.healthScore,
        p.performanceIndex,
        cp.cropType,
        cp.variety,
        cp.plantingDate,
        cp.expectedHarvestDate,
        cp.status as planting_status,
        (SELECT COALESCE(SUM(h.yieldKg), 0) FROM harvest_records h 
         WHERE h.plotId = p.id AND h.harvestDate >= date('now', '-365 days')) as annual_yield_kg,
        (SELECT COUNT(*) FROM scouting_reports sr 
         WHERE sr.plotId = p.id AND sr.isResolved = 0) as active_pest_reports,
        (SELECT MAX(sr.severityScore) FROM scouting_reports sr 
         WHERE sr.plotId = p.id AND sr.isResolved = 0) as max_pest_severity,
        (SELECT COUNT(*) FROM crop_plantings cp2 
         WHERE cp2.plotId = p.id AND cp2.status = 'harvested' 
         AND cp2.actualHarvestDate >= date('now', '-365 days')) as harvests_this_year
    FROM plots p
    LEFT JOIN crop_plantings cp ON cp.plotId = p.id AND cp.status = 'growing'
    """
)
data class PlotAnalyticsView(
    val plot_id: Long = 0,
    val plot_name: String = "",
    val sizeAcres: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val healthScore: Double? = null,
    val performanceIndex: Double? = null,
    val cropType: String? = null,
    val variety: String? = null,
    val plantingDate: String? = null,
    val expectedHarvestDate: String? = null,
    val planting_status: String? = null,
    val annual_yield_kg: Double = 0.0,
    val active_pest_reports: Int = 0,
    val max_pest_severity: Int? = null,
    val harvests_this_year: Int = 0
)