package com.shambasmart.presentation.gps

import android.content.Context
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Manages GNSS constellation tracking for multi-constellation positioning
 * 
 * Tracks GPS, GLONASS, and BeiDou satellites available on Xiaomi Pad 7.
 * Provides quality metrics and constellation breakdown for enhanced accuracy.
 */
@Singleton
class GnssConstellationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager: LocationManager = 
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val handler = Handler(Looper.getMainLooper())
    
    // Satellite tracking state
    private val _satellites = MutableStateFlow<List<GnssSatelliteInfo>>(emptyList())
    val satellites: StateFlow<List<GnssSatelliteInfo>> = _satellites.asStateFlow()

    private val _constellationBreakdown = MutableStateFlow(ConstellationBreakdown())
    val constellationBreakdown: StateFlow<ConstellationBreakdown> = _constellationBreakdown.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // GNSS status callback
    private var gnssStatusCallback: GnssStatus.Callback? = null
    private var gnssMeasurementsCallback: GnssMeasurementsEvent.Callback? = null

    // DOP (Dilution of Precision) estimates
    private var currentHdop: Double = Double.MAX_VALUE
    private var currentVdop: Double = Double.MAX_VALUE
    private var currentPdop: Double = Double.MAX_VALUE

    /**
     * Start tracking GNSS constellations
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun startTracking() {
        if (_isTracking.value) return

        try {
            // Register GNSS status callback
            gnssStatusCallback = object : GnssStatus.Callback() {
                override fun onStarted() {
                    _isTracking.value = true
                }

                override fun onStopped() {
                    _isTracking.value = false
                }

                @Deprecated("Deprecated in parent class")
                override fun onFirstFix(ttffMillis: Int) {
                    // First fix achieved
                }

                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    processGnssStatus(status)
                }
            }

            locationManager.registerGnssStatusCallback(gnssStatusCallback!!, handler)

            // Try to register measurements callback for DOP data
            try {
                gnssMeasurementsCallback = object : GnssMeasurementsEvent.Callback() {
                    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent) {
                        processGnssMeasurements(event)
                    }

                    override fun onStatusChanged(status: Int) {
                        // Measurement status changed
                    }
                }
                locationManager.registerGnssMeasurementsCallback(gnssMeasurementsCallback!!, handler)
            } catch (e: Exception) {
                // Measurements not available on this device, continue without DOP
            }

            _isTracking.value = true
        } catch (e: SecurityException) {
            _isTracking.value = false
        } catch (e: Exception) {
            _isTracking.value = false
        }
    }

    /**
     * Stop tracking GNSS constellations
     */
    fun stopTracking() {
        gnssStatusCallback?.let {
            try {
                locationManager.unregisterGnssStatusCallback(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        gnssMeasurementsCallback?.let {
            try {
                locationManager.unregisterGnssMeasurementsCallback(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        _isTracking.value = false
    }

    /**
     * Process GNSS status update
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun processGnssStatus(status: GnssStatus) {
        val satelliteList = mutableListOf<GnssSatelliteInfo>()
        
        // Group satellites by constellation
        var gpsCount = 0
        var gpsUsed = 0
        var gpsCn0Sum = 0.0
        var glonassCount = 0
        var glonassUsed = 0
        var glonassCn0Sum = 0.0
        var beidouCount = 0
        var beidouUsed = 0
        var beidouCn0Sum = 0.0
        var galileoCount = 0
        var otherCount = 0

        for (i in 0 until status.satelliteCount) {
            val satellite = GnssSatelliteInfo(
                svid = status.getSvid(i),
                constellationType = status.getConstellationType(i),
                cn0DbHz = status.getCn0DbHz(i),
                elevationDegrees = status.getElevationDegrees(i),
                azimuthDegrees = status.getAzimuthDegrees(i),
                usedInFix = status.usedInFix(i),
                hasAlmanac = false,
                hasEphemeris = false
            )
            satelliteList.add(satellite)

            // Count by constellation
            when (satellite.constellationType) {
                GnssStatus.CONSTELLATION_GPS -> {
                    gpsCount++
                    if (satellite.usedInFix) gpsUsed++
                    gpsCn0Sum += satellite.cn0DbHz
                }
                GnssStatus.CONSTELLATION_GLONASS -> {
                    glonassCount++
                    if (satellite.usedInFix) glonassUsed++
                    glonassCn0Sum += satellite.cn0DbHz
                }
                GnssStatus.CONSTELLATION_BEIDOU -> {
                    beidouCount++
                    if (satellite.usedInFix) beidouUsed++
                    beidouCn0Sum += satellite.cn0DbHz
                }
                GnssStatus.CONSTELLATION_GALILEO -> {
                    galileoCount++
                }
                else -> {
                    otherCount++
                }
            }
        }

        // Calculate average C/N0 per constellation
        val gpsAvgCn0 = if (gpsCount > 0) gpsCn0Sum / gpsCount else 0.0
        val glonassAvgCn0 = if (glonassCount > 0) glonassCn0Sum / glonassCount else 0.0
        val beidouAvgCn0 = if (beidouCount > 0) beidouCn0Sum / beidouCount else 0.0

        // Update breakdown
        _constellationBreakdown.value = ConstellationBreakdown(
            gpsSatellites = gpsCount,
            gpsUsed = gpsUsed,
            gpsAvgCn0 = gpsAvgCn0,
            glonassSatellites = glonassCount,
            glonassUsed = glonassUsed,
            glonassAvgCn0 = glonassAvgCn0,
            beidouSatellites = beidouCount,
            beidouUsed = beidouUsed,
            beidouAvgCn0 = beidouAvgCn0,
            galileoSatellites = galileoCount,
            otherSatellites = otherCount
        )

        // Update satellite list
        _satellites.value = satelliteList

        // Estimate DOP based on satellite geometry
        estimateDop(satelliteList)
    }

    /**
     * Process GNSS measurements for DOP calculation
     */
    private fun processGnssMeasurements(event: GnssMeasurementsEvent) {
        // Use measurements to refine DOP estimate
        val measurements = event.measurements
        if (measurements.size >= 4) {
            // Simple DOP estimation based on satellite count
            // Count all measurements as used (simplified approach)
            val usedCount = measurements.size
            // Rough HDOP estimate: lower with more satellites
            currentHdop = (10.0 / usedCount).coerceIn(0.5, 10.0)
            currentVdop = currentHdop * 1.5 // Vertical is typically worse
            currentPdop = sqrt(currentHdop.pow(2) + currentVdop.pow(2))
        }
    }

    /**
     * Estimate DOP from satellite positions
     */
    private fun estimateDop(satellites: List<GnssSatelliteInfo>) {
        val usedSatellites = satellites.filter { it.usedInFix }
        
        if (usedSatellites.size < 4) {
            // Not enough satellites for reliable fix
            currentHdop = 99.0
            currentVdop = 99.0
            currentPdop = 99.0
            return
        }

        // Estimate HDOP based on satellite geometry
        // Better geometry = lower DOP
        val avgElevation = usedSatellites.map { it.elevationDegrees.toDouble() }.average()
        val elevationSpread = usedSatellites.maxOf { it.elevationDegrees } - 
                             usedSatellites.minOf { it.elevationDegrees }
        
        // Calculate azimuth spread (0-360 degrees)
        val azimuths = usedSatellites.map { it.azimuthDegrees.toDouble() }
        val azimuthSpread = calculateAzimuthSpread(azimuths)

        // HDOP estimation formula (simplified)
        // Lower DOP = better geometry
        val geometryFactor = when {
            azimuthSpread > 270 && avgElevation > 30 -> 0.8  // Excellent geometry
            azimuthSpread > 180 && avgElevation > 20 -> 1.2  // Good geometry
            azimuthSpread > 90 && avgElevation > 10 -> 2.0   // Moderate geometry
            else -> 3.5                                       // Poor geometry
        }

        // Adjust for satellite count
        val countFactor = when {
            usedSatellites.size >= 12 -> 0.7
            usedSatellites.size >= 8 -> 1.0
            usedSatellites.size >= 6 -> 1.3
            else -> 2.0
        }

        // Constellation diversity bonus
        val constellations = usedSatellites.map { it.constellationType }.distinct().size
        val diversityBonus = when {
            constellations >= 3 -> 0.7  // GPS + GLONASS + BeiDou
            constellations >= 2 -> 0.85 // Two constellations
            else -> 1.0                 // Single constellation
        }

        currentHdop = (geometryFactor * countFactor * diversityBonus).coerceIn(0.5, 10.0)
        currentVdop = currentHdop * 1.5
        currentPdop = sqrt(currentHdop.pow(2) + currentVdop.pow(2))
    }

    /**
     * Calculate azimuth spread (0-360 degrees)
     */
    private fun calculateAzimuthSpread(azimuths: List<Double>): Double {
        if (azimuths.size < 2) return 0.0
        
        val sorted = azimuths.sorted()
        var maxGap = 0.0
        
        for (i in 0 until sorted.size - 1) {
            val gap = sorted[i + 1] - sorted[i]
            if (gap > maxGap) maxGap = gap
        }
        
        // Check gap wrapping around 360
        val wrapGap = 360 - sorted.last() + sorted.first()
        if (wrapGap > maxGap) maxGap = wrapGap
        
        return 360 - maxGap // Spread is total minus largest gap
    }

    /**
     * Calculate overall quality score (0-100)
     */
    fun calculateQualityScore(): Int {
        val breakdown = _constellationBreakdown.value
        val usedSatellites = _satellites.value.count { it.usedInFix }

        // Satellite count score (0-40 points)
        val satelliteScore = when {
            usedSatellites >= 12 -> 40
            usedSatellites >= 10 -> 35
            usedSatellites >= 8 -> 30
            usedSatellites >= 6 -> 25
            usedSatellites >= 4 -> 15
            else -> 5
        }

        // Constellation diversity score (0-30 points)
        val constellationCount = breakdown.getActiveConstellationCount()
        val diversityScore = when {
            constellationCount >= 3 -> 30
            constellationCount >= 2 -> 20
            constellationCount >= 1 -> 10
            else -> 0
        }

        // DOP score (0-30 points)
        val dopScore = when {
            currentHdop < 1.0 -> 30
            currentHdop < 1.5 -> 25
            currentHdop < 2.0 -> 20
            currentHdop < 3.0 -> 15
            currentHdop < 5.0 -> 10
            else -> 5
        }

        return (satelliteScore + diversityScore + dopScore).coerceIn(0, 100)
    }

    /**
     * Get current DOP values
     */
    fun getCurrentDop(): Triple<Double, Double, Double> {
        return Triple(currentHdop, currentVdop, currentPdop)
    }

    /**
     * Get total used satellites
     */
    fun getUsedSatelliteCount(): Int {
        return _satellites.value.count { it.usedInFix }
    }

    /**
     * Get total visible satellites
     */
    fun getTotalSatelliteCount(): Int {
        return _satellites.value.size
    }

    /**
     * Check if multi-constellation is available
     */
    fun isMultiConstellationAvailable(): Boolean {
        val breakdown = _constellationBreakdown.value
        return breakdown.getActiveConstellationCount() >= 2
    }
}