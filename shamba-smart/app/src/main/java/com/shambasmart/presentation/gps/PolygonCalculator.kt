package com.shambasmart.presentation.gps

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Polygon calculator for boundary area and perimeter
 * 
 * Uses Haversine formula for accurate distance calculations
 * on Earth's curved surface (important for GPS coordinates).
 */
object PolygonCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Calculate area of polygon in square meters
     * Uses the Shoelace formula with spherical correction
     */
    fun calculateArea(points: List<BoundaryPoint>): Double {
        if (points.size < 3) return 0.0

        var area = 0.0

        for (i in points.indices) {
            val j = (i + 1) % points.size
            area += points[i].longitude * points[j].latitude
            area -= points[j].longitude * points[i].latitude
        }

        area = abs(area) / 2.0

        // Convert from degrees to square meters
        // Average latitude for correction
        val avgLat = points.map { it.latitude }.average()
        val latCorrection = cos(Math.toRadians(avgLat))
        
        // Each degree of latitude ≈ 111,320 meters
        // Each degree of longitude ≈ 111,320 * cos(latitude) meters
        val metersPerDegreeLat = 111_320.0
        val metersPerDegreeLng = 111_320.0 * latCorrection

        // Convert area from degrees² to meters²
        area = area * metersPerDegreeLat * metersPerDegreeLng

        return area
    }

    /**
     * Calculate area in acres
     */
    fun calculateAreaAcres(points: List<BoundaryPoint>): Double {
        val areaMeters = calculateArea(points)
        return areaMeters / 4046.86 // 1 acre = 4046.86 m²
    }

    /**
     * Calculate area in hectares
     */
    fun calculateAreaHectares(points: List<BoundaryPoint>): Double {
        val areaMeters = calculateArea(points)
        return areaMeters / 10_000.0 // 1 hectare = 10,000 m²
    }

    /**
     * Calculate perimeter of polygon in meters
     * Uses Haversine formula for accurate distances
     */
    fun calculatePerimeter(points: List<BoundaryPoint>): Double {
        if (points.size < 2) return 0.0

        var perimeter = 0.0

        for (i in points.indices) {
            val j = (i + 1) % points.size
            perimeter += haversineDistance(
                points[i].latitude, points[i].longitude,
                points[j].latitude, points[j].longitude
            )
        }

        return perimeter
    }

    /**
     * Calculate distance between two GPS points using Haversine formula
     */
    fun haversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Smooth boundary polygon using Douglas-Peucker algorithm
     * Removes redundant points while preserving shape
     */
    fun simplifyPolygon(
        points: List<BoundaryPoint>,
        toleranceMeters: Double = 5.0
    ): List<BoundaryPoint> {
        if (points.size < 3) return points

        return douglasPeucker(points, toleranceMeters)
    }

    private fun douglasPeucker(
        points: List<BoundaryPoint>,
        tolerance: Double
    ): List<BoundaryPoint> {
        if (points.size < 3) return points

        // Find the point with maximum distance from line between first and last
        var maxDistance = 0.0
        var maxIndex = 0

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(
                points[i],
                points.first(),
                points.last()
            )
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        // If max distance is greater than tolerance, recursively simplify
        return if (maxDistance > tolerance) {
            val left = douglasPeucker(points.subList(0, maxIndex + 1), tolerance)
            val right = douglasPeucker(points.subList(maxIndex, points.size), tolerance)

            left.dropLast(1) + right
        } else {
            listOf(points.first(), points.last())
        }
    }

    private fun perpendicularDistance(
        point: BoundaryPoint,
        lineStart: BoundaryPoint,
        lineEnd: BoundaryPoint
    ): Double {
        val area = abs(
            (lineEnd.longitude - lineStart.longitude) * (lineStart.latitude - point.latitude) -
            (lineStart.longitude - point.longitude) * (lineEnd.latitude - lineStart.latitude)
        )

        val lineLength = haversineDistance(
            lineStart.latitude, lineStart.longitude,
            lineEnd.latitude, lineEnd.longitude
        )

        return if (lineLength > 0) area / lineLength else 0.0
    }

    /**
     * Check if polygon is closed (first and last points are close)
     */
    fun isPolygonClosed(points: List<BoundaryPoint>, thresholdMeters: Double = 20.0): Boolean {
        if (points.size < 3) return false
        
        val first = points.first()
        val last = points.last()
        val distance = haversineDistance(
            first.latitude, first.longitude,
            last.latitude, last.longitude
        )
        
        return distance <= thresholdMeters
    }

    /**
     * Get centroid of polygon
     */
    fun getCentroid(points: List<BoundaryPoint>): Pair<Double, Double> {
        val avgLat = points.map { it.latitude }.average()
        val avgLng = points.map { it.longitude }.average()
        return Pair(avgLat, avgLng)
    }
}