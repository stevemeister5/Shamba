package com.shambasmart.presentation.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
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
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Modern location provider using FusedLocationProviderClient
 * 
 * Replaces deprecated LocationListener with Google Play Services
 * location API for better accuracy and battery efficiency.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
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
    }
}

data class AveragedLocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val sampleCount: Int,
    val success: Boolean
)