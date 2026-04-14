package com.shambasmart.presentation.gps

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Kalman Filter for GPS coordinate smoothing with multi-constellation fusion
 * 
 * Reduces GPS noise by combining multiple measurements
 * with an estimated state to produce more accurate position.
 * 
 * Supports multi-constellation fusion from GPS, GLONASS, and BeiDou
 * for enhanced accuracy on Xiaomi Pad 7 in rural Tanzania.
 */
class GPSKalmanFilter(
    private val processNoise: Double = 1.0,
    private val initialMeasurementNoise: Double = 10.0
) {
    // State estimates
    private var estimatedLat: Double = 0.0
    private var estimatedLng: Double = 0.0
    
    // Error covariance
    private var errorCovarianceLat: Double = 1.0
    private var errorCovarianceLng: Double = 1.0
    
    // Measurement noise (adapted based on GPS accuracy)
    private var measurementNoise: Double = initialMeasurementNoise
    
    private var isInitialized = false

    // Multi-constellation state tracking
    private data class ConstellationState(
        var estimatedLat: Double = 0.0,
        var estimatedLng: Double = 0.0,
        var errorCovarianceLat: Double = 1.0,
        var errorCovarianceLng: Double = 1.0,
        var weight: Double = 1.0
    )

    private val constellationStates = mutableMapOf<String, ConstellationState>()
    private var useMultiConstellation = false

    /**
     * Update filter with new GPS measurement
     * 
     * @param measuredLat Measured latitude
     * @param measuredLng Measured longitude
     * @param accuracy GPS accuracy in meters (from Location.accuracy)
     * @return Smoothed (latitude, longitude) pair
     */
    fun update(
        measuredLat: Double,
        measuredLng: Double,
        accuracy: Double
    ): Pair<Double, Double> {
        // Adapt measurement noise based on reported GPS accuracy
        measurementNoise = accuracy.coerceAtLeast(5.0) // Minimum 5m noise
        
        if (!isInitialized) {
            // First measurement - initialize state
            estimatedLat = measuredLat
            estimatedLng = measuredLng
            errorCovarianceLat = measurementNoise * measurementNoise
            errorCovarianceLng = measurementNoise * measurementNoise
            isInitialized = true
            return Pair(estimatedLat, estimatedLng)
        }

        // Prediction step
        // State prediction: previous state (no motion model for stationary measurement)
        // Error covariance prediction: add process noise
        val predictedErrorCovarianceLat = errorCovarianceLat + processNoise
        val predictedErrorCovarianceLng = errorCovarianceLng + processNoise

        // Update step - Latitude
        val kalmanGainLat = predictedErrorCovarianceLat / 
            (predictedErrorCovarianceLat + measurementNoise * measurementNoise)
        estimatedLat += kalmanGainLat * (measuredLat - estimatedLat)
        errorCovarianceLat = (1 - kalmanGainLat) * predictedErrorCovarianceLat

        // Update step - Longitude
        val kalmanGainLng = predictedErrorCovarianceLng / 
            (predictedErrorCovarianceLng + measurementNoise * measurementNoise)
        estimatedLng += kalmanGainLng * (measuredLng - estimatedLng)
        errorCovarianceLng = (1 - kalmanGainLng) * predictedErrorCovarianceLng

        return Pair(estimatedLat, estimatedLng)
    }

    /**
     * Get current estimated accuracy (1-sigma)
     */
    fun getEstimatedAccuracy(): Double {
        return sqrt(errorCovarianceLat * errorCovarianceLat + 
                   errorCovarianceLng * errorCovarianceLng)
    }

    /**
     * Reset filter state
     */
    fun reset() {
        estimatedLat = 0.0
        estimatedLng = 0.0
        errorCovarianceLat = 1.0
        errorCovarianceLng = 1.0
        isInitialized = false
        constellationStates.clear()
        useMultiConstellation = false
    }

    /**
     * Update filter with multi-constellation data
     * Fuses measurements from GPS, GLONASS, and BeiDou
     * 
     * @param result Constellation location result with satellite data
     * @return Smoothed (latitude, longitude) pair
     */
    fun updateWithConstellation(result: ConstellationLocationResult): Pair<Double, Double> {
        val breakdown = result.constellationBreakdown
        
        // Enable multi-constellation mode if we have multiple constellations
        val activeConstellations = breakdown.getActiveConstellationCount()
        useMultiConstellation = activeConstellations >= 2

        if (!useMultiConstellation) {
            // Fall back to standard update
            return update(
                result.location.latitude,
                result.location.longitude,
                result.location.accuracy.toDouble()
            )
        }

        // Calculate constellation weights based on satellite count and quality
        val gpsWeight = calculateConstellationWeight(
            breakdown.gpsSatellites,
            breakdown.gpsAvgCn0,
            breakdown.gpsUsed
        )
        val glonassWeight = calculateConstellationWeight(
            breakdown.glonassSatellites,
            breakdown.glonassAvgCn0,
            breakdown.glonassUsed
        )
        val beidouWeight = calculateConstellationWeight(
            breakdown.beidouSatellites,
            breakdown.beidouAvgCn0,
            breakdown.beidouUsed
        )

        // Normalize weights
        val totalWeight = gpsWeight + glonassWeight + beidouWeight
        val normalizedGpsWeight = if (totalWeight > 0) gpsWeight / totalWeight else 0.0
        val normalizedGlonassWeight = if (totalWeight > 0) glonassWeight / totalWeight else 0.0
        val normalizedBeidouWeight = if (totalWeight > 0) beidouWeight / totalWeight else 0.0

        // Update constellation-specific states
        updateConstellationState("GPS", result, normalizedGpsWeight)
        updateConstellationState("GLONASS", result, normalizedGlonassWeight)
        updateConstellationState("BeiDou", result, normalizedBeidouWeight)

        // Fuse constellation estimates using weighted average
        return fuseConstellationEstimates()
    }

    /**
     * Calculate constellation weight based on satellite metrics
     */
    private fun calculateConstellationWeight(
        satelliteCount: Int,
        avgCn0: Double,
        usedCount: Int
    ): Double {
        if (satelliteCount == 0 || usedCount == 0) return 0.0

        // Weight based on satellite count (more satellites = better)
        val countWeight = when {
            satelliteCount >= 8 -> 1.0
            satelliteCount >= 6 -> 0.8
            satelliteCount >= 4 -> 0.6
            satelliteCount >= 2 -> 0.4
            else -> 0.2
        }

        // Weight based on signal strength (higher C/N0 = better)
        val signalWeight = when {
            avgCn0 >= 45 -> 1.0
            avgCn0 >= 40 -> 0.9
            avgCn0 >= 35 -> 0.7
            avgCn0 >= 30 -> 0.5
            else -> 0.3
        }

        // Weight based on used in fix ratio
        val usedRatio = usedCount.toDouble() / satelliteCount
        val usedWeight = usedRatio.coerceIn(0.0, 1.0)

        return countWeight * signalWeight * usedWeight
    }

    /**
     * Update state for a specific constellation
     */
    private fun updateConstellationState(
        constellation: String,
        result: ConstellationLocationResult,
        weight: Double
    ) {
        if (weight <= 0.0) return

        val state = constellationStates.getOrPut(constellation) {
            ConstellationState(weight = weight)
        }

        // Update weight based on current quality
        state.weight = weight

        // Calculate measurement noise based on constellation quality
        val constellationNoise = result.location.accuracy.toDouble() / 
            (result.qualityScore / 100.0).coerceAtLeast(0.1)

        if (state.estimatedLat == 0.0 && state.estimatedLng == 0.0) {
            // First measurement for this constellation
            state.estimatedLat = result.location.latitude
            state.estimatedLng = result.location.longitude
            state.errorCovarianceLat = constellationNoise * constellationNoise
            state.errorCovarianceLng = constellationNoise * constellationNoise
        } else {
            // Kalman update for this constellation
            val predictedErrorLat = state.errorCovarianceLat + processNoise
            val predictedErrorLng = state.errorCovarianceLng + processNoise

            val kalmanGainLat = predictedErrorLat / 
                (predictedErrorLat + constellationNoise * constellationNoise)
            state.estimatedLat += kalmanGainLat * (result.location.latitude - state.estimatedLat)
            state.errorCovarianceLat = (1 - kalmanGainLat) * predictedErrorLat

            val kalmanGainLng = predictedErrorLng / 
                (predictedErrorLng + constellationNoise * constellationNoise)
            state.estimatedLng += kalmanGainLng * (result.location.longitude - state.estimatedLng)
            state.errorCovarianceLng = (1 - kalmanGainLng) * predictedErrorLng
        }
    }

    /**
     * Fuse estimates from all constellations
     */
    private fun fuseConstellationEstimates(): Pair<Double, Double> {
        if (constellationStates.isEmpty()) {
            return Pair(estimatedLat, estimatedLng)
        }

        // Calculate total weight
        val totalWeight = constellationStates.values.sumOf { it.weight }
        
        if (totalWeight <= 0.0) {
            return Pair(estimatedLat, estimatedLng)
        }

        // Weighted average of constellation estimates
        var fusedLat = 0.0
        var fusedLng = 0.0
        var fusedErrorLat = 0.0
        var fusedErrorLng = 0.0

        for ((_, state) in constellationStates) {
            val normalizedWeight = state.weight / totalWeight
            fusedLat += state.estimatedLat * normalizedWeight
            fusedLng += state.estimatedLng * normalizedWeight
            fusedErrorLat += state.errorCovarianceLat * normalizedWeight * normalizedWeight
            fusedErrorLng += state.errorCovarianceLng * normalizedWeight * normalizedWeight
        }

        // Update main state with fused estimate
        estimatedLat = fusedLat
        estimatedLng = fusedLng
        errorCovarianceLat = fusedErrorLat
        errorCovarianceLng = fusedErrorLng
        isInitialized = true

        return Pair(estimatedLat, estimatedLng)
    }

    /**
     * Get constellation diversity score (0-100)
     * Higher score indicates better multi-constellation coverage
     */
    fun getConstellationDiversityScore(): Int {
        if (!useMultiConstellation || constellationStates.isEmpty()) {
            return 0
        }

        val activeCount = constellationStates.count { it.value.weight > 0 }
        return when {
            activeCount >= 3 -> 100
            activeCount >= 2 -> 70
            activeCount >= 1 -> 40
            else -> 0
        }
    }

    /**
     * Check if multi-constellation mode is active
     */
    fun isMultiConstellationActive(): Boolean {
        return useMultiConstellation
    }

    /**
     * Multi-sampling: Average multiple GPS readings
     * Simple but effective technique for improving accuracy
     */
    companion object {
        fun averageReadings(readings: List<Pair<Double, Double>>): Pair<Double, Double> {
            if (readings.isEmpty()) return Pair(0.0, 0.0)
            
            val avgLat = readings.map { it.first }.average()
            val avgLng = readings.map { it.second }.average()
            
            return Pair(avgLat, avgLng)
        }

        /**
         * Remove outliers using Median Absolute Deviation (MAD)
         * Helps filter out GPS spikes
         */
        fun removeOutliers(
            readings: List<Pair<Double, Double>>,
            threshold: Double = 2.5
        ): List<Pair<Double, Double>> {
            if (readings.size < 3) return readings
            
            val lats = readings.map { it.first }
            val lngs = readings.map { it.second }
            
            val medianLat = lats.sorted()[lats.size / 2]
            val medianLng = lngs.sorted()[lngs.size / 2]
            
            val madLat = lats.map { kotlin.math.abs(it - medianLat) }.sorted()[lats.size / 2]
            val madLng = lngs.map { kotlin.math.abs(it - medianLng) }.sorted()[lngs.size / 2]
            
            return readings.filter { (lat, lng) ->
                val zScoreLat = if (madLat > 0) kotlin.math.abs(lat - medianLat) / madLat else 0.0
                val zScoreLng = if (madLng > 0) kotlin.math.abs(lng - medianLng) / madLng else 0.0
                zScoreLat < threshold && zScoreLng < threshold
            }
        }
    }
}