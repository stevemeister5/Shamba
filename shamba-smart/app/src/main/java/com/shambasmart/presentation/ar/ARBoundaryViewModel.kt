package com.shambasmart.presentation.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ARBoundaryUiState(
    val boundaryPoints: List<BoundaryPoint> = emptyList(),
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentAccuracy: Double = 0.0,
    val isGPSEnabled: Boolean = false,
    val isMarking: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ARBoundaryViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ARBoundaryUiState())
    val uiState: StateFlow<ARBoundaryUiState> = _uiState.asStateFlow()

    private var locationManager: LocationManager? = null
    private var pointCounter = 0

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _uiState.update {
                it.copy(
                    currentLatitude = location.latitude,
                    currentLongitude = location.longitude,
                    currentAccuracy = location.accuracy.toDouble(),
                    isGPSEnabled = true
                )
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}

        override fun onProviderEnabled(provider: String) {
            _uiState.update { it.copy(isGPSEnabled = true) }
        }

        override fun onProviderDisabled(provider: String) {
            _uiState.update { it.copy(isGPSEnabled = false) }
        }
    }

    init {
        initializeGPS()
    }

    private fun initializeGPS() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L, // 1 second
                    1f, // 1 meter
                    locationListener
                )

                // Get last known location
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location ->
                    _uiState.update {
                        it.copy(
                            currentLatitude = location.latitude,
                            currentLongitude = location.longitude,
                            currentAccuracy = location.accuracy.toDouble(),
                            isGPSEnabled = true
                        )
                    }
                }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(errorMessage = "GPS permission denied") }
            }
        } else {
            _uiState.update { it.copy(errorMessage = "GPS permission required") }
        }
    }

    fun markBoundaryPoint() {
        val currentState = uiState.value

        if (!currentState.isGPSEnabled) {
            _uiState.update { it.copy(errorMessage = "GPS not available") }
            return
        }

        if (currentState.currentAccuracy > 10.0) {
            _uiState.update { it.copy(errorMessage = "GPS accuracy too low (${currentState.currentAccuracy.toInt()}m). Wait for better signal.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMarking = true) }

            // Simulate marking delay for better UX
            kotlinx.coroutines.delay(500)

            pointCounter++
            val newPoint = BoundaryPoint(
                id = pointCounter,
                latitude = currentState.currentLatitude,
                longitude = currentState.currentLongitude,
                accuracy = currentState.currentAccuracy,
                timestamp = System.currentTimeMillis()
            )

            _uiState.update {
                it.copy(
                    boundaryPoints = it.boundaryPoints + newPoint,
                    isMarking = false,
                    errorMessage = null
                )
            }
        }
    }

    fun deleteBoundaryPoint(pointId: Int) {
        _uiState.update {
            it.copy(boundaryPoints = it.boundaryPoints.filter { point -> point.id != pointId })
        }
    }

    fun undoLastPoint() {
        val currentPoints = uiState.value.boundaryPoints
        if (currentPoints.isNotEmpty()) {
            _uiState.update {
                it.copy(boundaryPoints = currentPoints.dropLast(1))
            }
        }
    }

    fun clearBoundaryPoints() {
        pointCounter = 0
        _uiState.update { it.copy(boundaryPoints = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: SecurityException) {
            // Ignore
        }
    }
}