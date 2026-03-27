package com.shambasmart.presentation.gps

import kotlin.math.sqrt

/**
 * Kalman Filter for GPS coordinate smoothing
 * 
 * Reduces GPS noise by combining multiple measurements
 * with an estimated state to produce more accurate position.
 * 
 * Particularly effective for rural Tanzania where GPS
 * accuracy can be 15-30m due to poor satellite visibility.
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