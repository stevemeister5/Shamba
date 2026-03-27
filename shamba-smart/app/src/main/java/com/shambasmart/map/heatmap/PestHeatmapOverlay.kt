package com.shambasmart.map.heatmap

import android.graphics.Color
import com.shambasmart.data.local.dao.ScoutingHeatmapPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Renders pest scouting heatmap overlay on OSMDroid map.
 * 
 * Uses color-coded markers based on severity:
 * - Green (score 1-2): Low/Minor severity
 * - Yellow (score 3): Moderate severity
 * - Orange (score 4): Severe
 * - Red (score 5): Critical
 */
object PestHeatmapOverlay {

    private const val OVERLAY_ID = "pest_heatmap"

    /**
     * Color scheme for severity levels
     */
    private val severityColors = mapOf(
        1 to Pair("#4CAF50", "Low"),      // Green
        2 to Pair("#8BC34A", "Minor"),    // Light Green
        3 to Pair("#FFEB3B", "Moderate"), // Yellow
        4 to Pair("#FF9800", "Severe"),   // Orange
        5 to Pair("#F44336", "Critical")  // Red
    )

    /**
     * Applies pest heatmap overlay to OSMDroid map.
     * 
     * @param map The OSMDroid MapView
     * @param heatmapPoints List of scouting heatmap points
     */
    fun applyPestHeatmap(
        map: MapView,
        heatmapPoints: List<ScoutingHeatmapPoint>
    ) {
        // Remove existing pest heatmap overlays
        val existingOverlays = map.overlays.filter {
            it is Polygon && it.id?.startsWith(OVERLAY_ID) == true
        }
        map.overlays.removeAll(existingOverlays)

        // Create heatmap circles for each scouting point
        heatmapPoints.forEach { point ->
            val colorPair = severityColors[point.severityScore] ?: severityColors[1]!!
            val heatmapCircle = createHeatmapCircle(
                center = GeoPoint(point.gpsLatitude, point.gpsLongitude),
                severityScore = point.severityScore,
                pestType = point.pestType,
                colorHex = colorPair.first,
                severityLabel = colorPair.second
            )
            map.overlays.add(heatmapCircle)
        }

        map.invalidate()
    }

    /**
     * Removes pest heatmap overlay from map.
     */
    fun removePestHeatmap(map: MapView) {
        val heatmapOverlays = map.overlays.filter {
            it is Polygon && it.id?.startsWith(OVERLAY_ID) == true
        }
        map.overlays.removeAll(heatmapOverlays)
        map.invalidate()
    }

    /**
     * Creates a heatmap circle overlay for a scouting point.
     */
    private fun createHeatmapCircle(
        center: GeoPoint,
        severityScore: Int,
        pestType: String,
        colorHex: String,
        severityLabel: String
    ): Polygon {
        val baseColor = Color.parseColor(colorHex)
        val alpha = (40 + (severityScore * 25)).coerceIn(40, 150)
        val fillColor = Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        val strokeColor = Color.argb(200, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        // Radius based on severity (50-200 meters)
        val radiusMeters = 50.0 + (severityScore * 30.0)
        val numPoints = 36
        val circlePoints = mutableListOf<GeoPoint>()

        for (i in 0 until numPoints) {
            val angle = Math.toRadians((i * 360.0 / numPoints))
            val latOffset = radiusMeters * Math.cos(angle) / 111320.0
            val lngOffset = radiusMeters * Math.sin(angle) / (111320.0 * Math.cos(Math.toRadians(center.latitude)))
            circlePoints.add(GeoPoint(center.latitude + latOffset, center.longitude + lngOffset))
        }

        return Polygon().apply {
            id = "${OVERLAY_ID}_${System.currentTimeMillis()}"
            points = circlePoints
            fillPaint.color = fillColor
            outlinePaint.color = strokeColor
            outlinePaint.strokeWidth = 2f
            title = pestType.replace("_", " ").replaceFirstChar { it.uppercase() }
            snippet = "Severity: $severityLabel ($severityScore/5)"
        }
    }

    /**
     * Gets the severity color for a given severity score.
     */
    fun getSeverityColor(score: Int): String {
        return severityColors[score]?.first ?: "#4CAF50"
    }

    /**
     * Gets the severity label for a given severity score.
     */
    fun getSeverityLabel(score: Int): String {
        return severityColors[score]?.second ?: "Low"
    }
}