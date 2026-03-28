package com.shambasmart.map.heatmap

import android.graphics.Color
import com.shambasmart.data.local.entity.MapMarkerEntity
import com.shambasmart.map.HeatmapType
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Renders heatmap overlays on the OSMDroid map based on marker data.
 * 
 * Each heatmap type aggregates data differently:
 * - Yield/Cost/Revenue: Based on linked crop plot data
 * - Animal Density: Based on livestock pen markers
 * - Soil Moisture: Based on irrigation/water markers
 * - Crop Health: Based on grading data
 * - Water/Feed Usage: Based on consumption markers
 */
object HeatmapRenderer {

    /**
     * Heatmap color schemes for each type (start and end colors)
     */
    private val heatmapColorSchemes = mapOf(
        HeatmapType.YIELD_PER_ACRE to Pair("#FFFFEB3B", "#FFF44336"),  // Yellow to Red
        HeatmapType.COST_PER_ACRE to Pair("#FF4CAF50", "#FFB71C1C"),   // Green to Dark Red
        HeatmapType.ANIMAL_DENSITY to Pair("#FF2196F3", "#FF673AB7"),  // Blue to Purple
        HeatmapType.SOIL_MOISTURE to Pair("#FF795548", "#FF2196F3"),   // Brown to Blue
        HeatmapType.CROP_HEALTH to Pair("#FFF44336", "#FF4CAF50"),     // Red to Green
        HeatmapType.WATER_USAGE to Pair("#FFE3F2FD", "#FF1565C0"),     // Light Blue to Dark Blue
        HeatmapType.FEED_CONSUMPTION to Pair("#FFFFF3E0", "#FFE65100"), // Light Orange to Dark Orange
        HeatmapType.REVENUE_PER_ACRE to Pair("#FFE8F5E9", "#FF2E7D32") // Light Green to Dark Green
    )

    /**
     * Apply heatmap overlay to OSMDroid map
     */
    fun applyHeatmapOverlay(
        map: MapView,
        heatmapType: HeatmapType,
        markers: List<MapMarkerEntity>
    ) {
        val colorScheme = heatmapColorSchemes[heatmapType] ?: return
        
        // Remove existing heatmap overlays
        val heatmapOverlays = map.overlays.filter { it is Polygon && it.id?.startsWith("heatmap_") == true }
        map.overlays.removeAll(heatmapOverlays)
        
        // Generate heatmap circles for each marker
        markers.forEach { marker ->
            val weight = calculateHeatmapWeight(heatmapType, marker)
            if (weight > 0) {
                val heatmapCircle = createHeatmapCircle(
                    center = GeoPoint(marker.latitude, marker.longitude),
                    weight = weight,
                    colorScheme = colorScheme,
                    markerName = marker.name
                )
                map.overlays.add(heatmapCircle)
            }
        }
        
        map.invalidate()
    }

    /**
     * Remove heatmap overlay from map
     */
    fun removeHeatmapOverlay(map: MapView) {
        val heatmapOverlays = map.overlays.filter { it is Polygon && it.id?.startsWith("heatmap_") == true }
        map.overlays.removeAll(heatmapOverlays)
        map.invalidate()
    }

    /**
     * Create a heatmap circle overlay
     */
    private fun createHeatmapCircle(
        center: GeoPoint,
        weight: Double,
        colorScheme: Pair<String, String>,
        markerName: String
    ): Polygon {
        val startColor = Color.parseColor(colorScheme.first)
        val endColor = Color.parseColor(colorScheme.second)
        
        // Interpolate color based on weight
        val alpha = (weight * 180).toInt().coerceIn(30, 180)
        val r = (Color.red(startColor) + (Color.red(endColor) - Color.red(startColor)) * weight).toInt()
        val g = (Color.green(startColor) + (Color.green(endColor) - Color.green(startColor)) * weight).toInt()
        val b = (Color.blue(startColor) + (Color.blue(endColor) - Color.blue(startColor)) * weight).toInt()
        val fillColor = Color.argb(alpha, r, g, b)
        
        // Create circle polygon
        val circlePoints = mutableListOf<GeoPoint>()
        val radiusMeters = 100 + (weight * 200) // 100-300 meters based on weight
        val numPoints = 36
        
        for (i in 0 until numPoints) {
            val angle = Math.toRadians((i * 360.0 / numPoints))
            val latOffset = radiusMeters * Math.cos(angle) / 111320.0
            val lngOffset = radiusMeters * Math.sin(angle) / (111320.0 * Math.cos(Math.toRadians(center.latitude)))
            circlePoints.add(GeoPoint(center.latitude + latOffset, center.longitude + lngOffset))
        }
        
        return Polygon().apply {
            id = "heatmap_${markerName}_${System.currentTimeMillis()}"
            points = circlePoints
            fillPaint.color = fillColor
            outlinePaint.color = Color.argb(200, r, g, b)
            outlinePaint.strokeWidth = 2f
            title = "Heatmap: $markerName"
            snippet = "Weight: ${String.format("%.2f", weight)}"
        }
    }

    /**
     * Calculate heatmap weight based on marker data and heatmap type
     */
    private fun calculateHeatmapWeight(heatmapType: HeatmapType, marker: MapMarkerEntity): Double {
        return when (heatmapType) {
            HeatmapType.YIELD_PER_ACRE -> {
                if (marker.category == "CROP" && marker.areaSquareMeters != null) {
                    // Weight based on plot size (larger plots = more yield potential)
                    (marker.areaSquareMeters ?: 0.0) / 10000.0 // Normalize to hectares
                } else 0.0
            }
            HeatmapType.COST_PER_ACRE -> {
                if (marker.category == "CROP") {
                    // Weight based on metadata cost if available
                    marker.metadata["cost"]?.toDoubleOrNull() ?: 0.5
                } else 0.0
            }
            HeatmapType.ANIMAL_DENSITY -> {
                if (marker.category == "LIVESTOCK") {
                    // Weight based on animal count if available
                    marker.metadata["animalCount"]?.toDoubleOrNull() ?: 1.0
                } else 0.0
            }
            HeatmapType.SOIL_MOISTURE -> {
                if (marker.category == "WATER") {
                    // Weight based on proximity to water sources
                    0.8
                } else if (marker.category == "CROP") {
                    0.3
                } else 0.0
            }
            HeatmapType.CROP_HEALTH -> {
                if (marker.category == "CROP") {
                    // Weight based on health score if available
                    marker.metadata["healthScore"]?.toDoubleOrNull() ?: 0.5
                } else 0.0
            }
            HeatmapType.WATER_USAGE -> {
                if (marker.category == "WATER" || marker.category == "CROP") {
                    marker.metadata["waterUsage"]?.toDoubleOrNull() ?: 0.5
                } else 0.0
            }
            HeatmapType.FEED_CONSUMPTION -> {
                if (marker.category == "LIVESTOCK") {
                    marker.metadata["feedConsumption"]?.toDoubleOrNull() ?: 0.5
                } else 0.0
            }
            HeatmapType.REVENUE_PER_ACRE -> {
                if (marker.category == "CROP" && marker.areaSquareMeters != null) {
                    marker.metadata["revenue"]?.toDoubleOrNull() ?: 
                        ((marker.areaSquareMeters ?: 0.0) / 10000.0 * 0.5)
                } else 0.0
            }
            else -> 0.0
        }
    }
}