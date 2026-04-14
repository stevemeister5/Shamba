package com.shambasmart.presentation.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Modern location provider using FusedLocationProviderClient with multi-constellation GNSS support
 * 
 * Leverages GPS, GLONASS, and BeiDou constellations on Xiaomi Pad 7
 * for enhanced positioning accuracy in rural Tanzania.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gnssConstellationManager: GnssConstellationManager
) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Get current location as a Flow
     * Emits location updates with configurable interval
     */
    fun getLocationUpdates(
        intervalMs: Long = 1000L,
        fastestIntervalMs: Long = 500L,
        priority: Int = Priority.PRIORITY_HIGH_ACCURACY
    ): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        val locationRequest = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(fastestIntervalMs)
            .setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Get last known location (single shot)
     */
    suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }

    /**
     * Multi-sampling: Collect multiple GPS readings and average them
     * Improves accuracy by reducing random GPS errors
     */
    suspend fun getAveragedLocation(
        sampleCount: Int = 15,
        intervalMs: Long = 300L
    ): AveragedLocationResult {
        val readings = mutableListOf<Pair<Double, Double>>()
        var totalAccuracy = 0.0
        var count = 0

        // Collect samples
        getLocationUpdates(intervalMs = intervalMs, fastestIntervalMs = intervalMs)
            .collect { location ->
                readings.add(Pair(location.latitude, location.longitude))
                totalAccuracy += location.accuracy
                count++
                
                if (count >= sampleCount) {
                    return@collect
                }
            }

        if (readings.isEmpty()) {
            return AveragedLocationResult(
                latitude = 0.0,
                longitude = 0.0,
                accuracy = 0.0,
                sampleCount = 0,
                success = false
            )
        }

        // Remove outliers
        val filteredReadings = GPSKalmanFilter.removeOutliers(readings)
        
        // Average remaining readings
        val (avgLat, avgLng) = GPSKalmanFilter.averageReadings(
            if (filteredReadings.isNotEmpty()) filteredReadings else readings
        )

        return AveragedLocationResult(
            latitude = avgLat,
            longitude = avgLng,
            accuracy = if (count > 0) totalAccuracy / count else 0.0,
            sampleCount = readings.size,
            success = true
        )
    }

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if GPS is enabled
     */
    fun isGPSEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) 
            as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        // Location updates are automatically removed when Flow is cancelled
        gnssConstellationManager.stopTracking()
    }

    /**
     * Get enhanced location updates with multi-constellation GNSS data
     * Combines FusedLocationProvider with GNSS constellation tracking
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun getConstellationLocationUpdates(
        intervalMs: Long = 1000L,
        fastestIntervalMs: Long = 500L,
        priority: Int = Priority.PRIORITY_HIGH_ACCURACY
    ): Flow<ConstellationLocationResult> = callbackFlow {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        // Start GNSS constellation tracking
        gnssConstellationManager.startTracking()

        val locationRequest = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(fastestIntervalMs)
            .setWaitForAccurateLocation(true) // Wait for accurate location when using multi-constellation
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Build constellation-enhanced result
                    val constellationResult = buildConstellationResult(location)
                    trySend(constellationResult)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            gnssConstellationManager.stopTracking()
        }
    }

    /**
     * Build constellation-enhanced location result
     */
    private fun buildConstellationResult(location: Location): ConstellationLocationResult {
        val breakdown = gnssConstellationManager.constellationBreakdown.value
        val (hdop, vdop, pdop) = gnssConstellationManager.getCurrentDop()
        val qualityScore = gnssConstellationManager.calculateQualityScore()
        val usedSatellites = gnssConstellationManager.getUsedSatelliteCount()
        val totalSatellites = gnssConstellationManager.getTotalSatelliteCount()

        return ConstellationLocationResult(
            location = location,
            constellationBreakdown = breakdown,
            totalSatellites = totalSatellites,
            usedSatellites = usedSatellites,
            hdop = hdop,
            vdop = vdop,
            pdop = pdop,
            qualityScore = qualityScore
        )
    }

    /**
     * Multi-sampling with constellation data
     * Collects multiple readings and returns enhanced result with constellation info
     */
    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getConstellationAveragedLocation(
        sampleCount: Int = 15,
        intervalMs: Long = 300L
    ): ConstellationAveragedResult {
        val readings = mutableListOf<ConstellationLocationResult>()

        // Collect samples with constellation data
        getConstellationLocationUpdates(intervalMs = intervalMs, fastestIntervalMs = intervalMs)
            .collect { result ->
                readings.add(result)
                
                if (readings.size >= sampleCount) {
                    return@collect
                }
            }

        if (readings.isEmpty()) {
            return ConstellationAveragedResult(
                latitude = 0.0,
                longitude = 0.0,
                accuracy = 0.0,
                sampleCount = 0,
                qualityScore = 0,
                constellationBreakdown = ConstellationBreakdown(),
                hdop = 99.0,
                vdop = 99.0,
                success = false
            )
        }

        // Weight readings by quality score
        val weightedReadings = readings.map { result ->
            val weight = result.qualityScore / 100.0
            Triple(result.location.latitude, result.location.longitude, weight)
        }

        // Calculate weighted average
        val totalWeight = weightedReadings.sumOf { it.third }
        val avgLat = weightedReadings.sumOf { it.first * it.third } / totalWeight
        val avgLng = weightedReadings.sumOf { it.second * it.third } / totalWeight

        // Use best quality result for constellation info
        val bestResult = readings.maxByOrNull { it.qualityScore } ?: readings.last()
        val avgAccuracy = readings.map { it.location.accuracy.toDouble() }.average()

        return ConstellationAveragedResult(
            latitude = avgLat,
            longitude = avgLng,
            accuracy = avgAccuracy,
            sampleCount = readings.size,
            qualityScore = bestResult.qualityScore,
            constellationBreakdown = bestResult.constellationBreakdown,
            hdop = bestResult.hdop,
            vdop = bestResult.vdop,
            success = true
        )
    }

    /**
     * Check if multi-constellation is available on this device
     */
    fun isMultiConstellationAvailable(): Boolean {
        return gnssConstellationManager.isMultiConstellationAvailable()
    }

    /**
     * Get current GNSS status without location
     */
    fun getCurrentGnssStatus(): GnssStatusInfo {
        return GnssStatusInfo(
            isTracking = gnssConstellationManager.isTracking.value,
            totalSatellites = gnssConstellationManager.getTotalSatelliteCount(),
            usedSatellites = gnssConstellationManager.getUsedSatelliteCount(),
            constellationBreakdown = gnssConstellationManager.constellationBreakdown.value,
            qualityScore = gnssConstellationManager.calculateQualityScore(),
            hdop = gnssConstellationManager.getCurrentDop().first
        )
    }
}

/**
 * Averaged location result with constellation data
 */
data class ConstellationAveragedResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val sampleCount: Int,
    val qualityScore: Int,
    val constellationBreakdown: ConstellationBreakdown,
    val hdop: Double,
    val vdop: Double,
    val success: Boolean
) {
    /**
     * Get human-readable accuracy assessment
     */
    val qualityLabel: String
        get() = when {
            qualityScore >= 90 -> "Excellent"
            qualityScore >= 75 -> "Very Good"
            qualityScore >= 60 -> "Good"
            qualityScore >= 40 -> "Moderate"
            else -> "Poor"
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
 * GNSS status information
 */
data class GnssStatusInfo(
    val isTracking: Boolean,
    val totalSatellites: Int,
    val usedSatellites: Int,
    val constellationBreakdown: ConstellationBreakdown,
    val qualityScore: Int,
    val hdop: Double
)

data class AveragedLocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val sampleCount: Int,
    val success: Boolean
)