package com.shambasmart.presentation.gps

import android.location.Location

/**
 * Enhanced location result with multi-constellation GNSS information
 * 
 * Provides detailed satellite constellation data from GPS, GLONASS,
 * and BeiDou for improved positioning accuracy on Xiaomi Pad 7.
 */
data class ConstellationLocationResult(
    val location: Location,
    val constellationBreakdown: ConstellationBreakdown,
    val totalSatellites: Int,
    val usedSatellites: Int,
    val hdop: Double,
    val vdop: Double,
    val pdop: Double,
    val qualityScore: Int, // 0-100
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Get human-readable accuracy assessment
     */
    fun getAccuracyLabel(): String = when {
        qualityScore >= 90 -> "Excellent"
        qualityScore >= 75 -> "Very Good"
        qualityScore >= 60 -> "Good"
        qualityScore >= 40 -> "Moderate"
        else -> "Poor"
    }

    /**
     * Check if location is suitable for boundary marking
     * Requires: HDOP < 2.0, at least 6 satellites, quality > 70
     */
    fun isSuitableForBoundaryMarking(): Boolean {
        return hdop < 2.0 && totalSatellites >= 6 && qualityScore >= 70
    }

    /**
     * Get constellation diversity score (0-100)
     * Higher score = more constellations contributing
     */
    fun getConstellationDiversityScore(): Int {
        var score = 0
        if (constellationBreakdown.gpsSatellites > 0) score += 40
        if (constellationBreakdown.glonassSatellites > 0) score += 30
        if (constellationBreakdown.beidouSatellites > 0) score += 30
        return score
    }

    /**
     * Format constellation info for display
     */
    fun formatConstellationInfo(): String {
        return buildString {
            append("GPS: ${constellationBreakdown.gpsSatellites}")
            append(" | GLONASS: ${constellationBreakdown.glonassSatellites}")
            append(" | BeiDou: ${constellationBreakdown.beidouSatellites}")
        }
    }
}

/**
 * Breakdown of satellites by constellation
 */
data class ConstellationBreakdown(
    val gpsSatellites: Int = 0,
    val gpsUsed: Int = 0,
    val gpsAvgCn0: Double = 0.0,
    val glonassSatellites: Int = 0,
    val glonassUsed: Int = 0,
    val glonassAvgCn0: Double = 0.0,
    val beidouSatellites: Int = 0,
    val beidouUsed: Int = 0,
    val beidouAvgCn0: Double = 0.0,
    val galileoSatellites: Int = 0,
    val otherSatellites: Int = 0
) {
    /**
     * Get count of active constellations (with at least 1 satellite)
     */
    fun getActiveConstellationCount(): Int {
        var count = 0
        if (gpsSatellites > 0) count++
        if (glonassSatellites > 0) count++
        if (beidouSatellites > 0) count++
        if (galileoSatellites > 0) count++
        return count
    }

    /**
     * Get weighted quality per constellation
     */
    fun getWeightedQuality(): Map<String, Double> {
        return mapOf(
            "GPS" to (gpsSatellites * gpsAvgCn0 / 50.0).coerceIn(0.0, 1.0),
            "GLONASS" to (glonassSatellites * glonassAvgCn0 / 50.0).coerceIn(0.0, 1.0),
            "BeiDou" to (beidouSatellites * beidouAvgCn0 / 50.0).coerceIn(0.0, 1.0)
        )
    }
}

/**
 * GNSS satellite information
 */
data class GnssSatelliteInfo(
    val svid: Int, // Satellite ID
    val constellationType: Int, // GnssStatus.CONSTELLATION_*
    val cn0DbHz: Float, // Signal strength
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val usedInFix: Boolean,
    val hasAlmanac: Boolean,
    val hasEphemeris: Boolean
) {
    /**
     * Get constellation name
     */
    fun getConstellationName(): String = when (constellationType) {
        1 -> "GPS"
        3 -> "GLONASS"
        5 -> "BeiDou"
        6 -> "Galileo"
        else -> "Other"
    }

    /**
     * Calculate satellite quality (0-100)
     * Based on signal strength and elevation
     */
    fun calculateQuality(): Int {
        // Signal strength component (0-60 points)
        val signalScore = when {
            cn0DbHz >= 45 -> 60
            cn0DbHz >= 40 -> 50
            cn0DbHz >= 35 -> 40
            cn0DbHz >= 30 -> 30
            cn0DbHz >= 25 -> 20
            else -> 10
        }

        // Elevation component (0-40 points) - higher elevation = less multipath
        val elevationScore = when {
            elevationDegrees >= 60 -> 40
            elevationDegrees >= 45 -> 35
            elevationDegrees >= 30 -> 30
            elevationDegrees >= 15 -> 20
            else -> 10
        }

        return signalScore + elevationScore
    }
}