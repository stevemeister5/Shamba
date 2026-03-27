package com.shambasmart.presentation.gps

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.BoundaryDao
import com.shambasmart.data.local.entity.BoundaryPointEntity
import com.shambasmart.data.local.entity.FarmBoundary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BoundaryPoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class GPSBoundaryUiState(
    val boundaryPoints: List<BoundaryPoint> = emptyList(),
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentAccuracy: Double = 0.0,
    val isGPSEnabled: Boolean = false,
    val isMarking: Boolean = false,
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val recordedLocations: List<Location> = emptyList(),
    val smoothedPoints: List<BoundaryPoint> = emptyList(),
    val areaAcres: Double = 0.0,
    val perimeterMeters: Double = 0.0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class GPSBoundaryViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val boundaryDao: BoundaryDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val plotId: Long = savedStateHandle.get<Long>("plotId") ?: 0L
    private val plotName: String = savedStateHandle.get<String>("plotName") ?: "Farm Plot"

    private val _uiState = MutableStateFlow(GPSBoundaryUiState())
    val uiState: StateFlow<GPSBoundaryUiState> = _uiState.asStateFlow()

    private val kalmanFilter = GPSKalmanFilter()
    private var pointCounter = 0
    private var lastLocation: Location? = null

    init {
        checkGPSStatus()
        startLocationUpdates()
        loadExistingBoundary()
    }

    private fun loadExistingBoundary() {
        if (plotId > 0) {
            viewModelScope.launch {
                val boundary = boundaryDao.getBoundaryByPlotId(plotId)
                if (boundary != null) {
                    val pointEntities = boundaryDao.getPointsByBoundaryId(boundary.id)
                    val points = pointEntities.map { entity ->
                        BoundaryPoint(
                            id = entity.pointIndex,
                            latitude = entity.latitude,
                            longitude = entity.longitude,
                            altitude = entity.altitude ?: 0.0,
                            accuracy = entity.accuracy,
                            timestamp = entity.timestamp
                        )
                    }
                    pointCounter = points.size
                    _uiState.update {
                        it.copy(
                            boundaryPoints = points,
                            areaAcres = boundary.areaAcres,
                            perimeterMeters = boundary.perimeterMeters,
                            successMessage = "Loaded existing boundary with ${points.size} points"
                        )
                    }
                }
            }
        }
    }

    private fun checkGPSStatus() {
        _uiState.update { it.copy(isGPSEnabled = locationProvider.isGPSEnabled()) }
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            try {
                locationProvider.getLocationUpdates(intervalMs = 1000L)
                    .collect { location ->
                        // Apply Kalman filter
                        val (smoothedLat, smoothedLng) = kalmanFilter.update(
                            location.latitude,
                            location.longitude,
                            location.accuracy.toDouble()
                        )

                        lastLocation = location

                        _uiState.update {
                            it.copy(
                                currentLatitude = smoothedLat,
                                currentLongitude = smoothedLng,
                                currentAccuracy = location.accuracy.toDouble(),
                                isGPSEnabled = true,
                                errorMessage = null
                            )
                        }

                        // If recording, add to recorded locations
                        if (uiState.value.isRecording) {
                            _uiState.update {
                                it.copy(recordedLocations = it.recordedLocations + location)
                            }
                        }
                    }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(errorMessage = "GPS permission denied") }
            }
        }
    }

    /**
     * Mark boundary point with multi-sampling for better accuracy
     */
    fun markBoundaryPoint() {
        val currentState = uiState.value

        if (!currentState.isGPSEnabled) {
            _uiState.update { it.copy(errorMessage = "GPS not available") }
            return
        }

        if (currentState.currentAccuracy > 15.0) {
            _uiState.update { 
                it.copy(errorMessage = "GPS accuracy too low (${currentState.currentAccuracy.toInt()}m). Move to open area and wait.") 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMarking = true, errorMessage = null) }

            // Multi-sampling: Collect 15 readings over ~5 seconds
            val result = locationProvider.getAveragedLocation(
                sampleCount = 15,
                intervalMs = 300L
            )

            if (result.success) {
                pointCounter++
                val newPoint = BoundaryPoint(
                    id = pointCounter,
                    latitude = result.latitude,
                    longitude = result.longitude,
                    accuracy = result.accuracy,
                    timestamp = System.currentTimeMillis()
                )

                _uiState.update {
                    it.copy(
                        boundaryPoints = it.boundaryPoints + newPoint,
                        isMarking = false,
                        errorMessage = null,
                        successMessage = "Point #$pointCounter marked (${result.sampleCount} samples, ${String.format("%.1f", result.accuracy)}m accuracy)"
                    )
                }

                // Calculate area if we have 3+ points
                if (uiState.value.boundaryPoints.size >= 3) {
                    updateAreaCalculation()
                }
            } else {
                _uiState.update {
                    it.copy(
                        isMarking = false,
                        errorMessage = "Failed to get GPS reading. Try again."
                    )
                }
            }
        }
    }

    /**
     * Start walking mode - continuous GPS recording
     */
    fun startWalkingMode() {
        _uiState.update {
            it.copy(
                isRecording = true,
                recordedLocations = emptyList(),
                errorMessage = null,
                successMessage = "Walking mode started. Walk around your farm perimeter."
            )
        }
    }

    /**
     * Stop walking mode and process recorded points
     */
    fun stopWalkingModeAndProcess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, isRecording = false) }

            val locations = uiState.value.recordedLocations

            if (locations.size < 10) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Not enough GPS points recorded. Walk for at least 30 seconds."
                    )
                }
                return@launch
            }

            // Convert locations to boundary points
            val points = locations.mapIndexed { index, location ->
                BoundaryPoint(
                    id = index + 1,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy.toDouble(),
                    timestamp = location.time
                )
            }

            // Apply Douglas-Peucker simplification
            val simplifiedPoints = PolygonCalculator.simplifyPolygon(
                points,
                toleranceMeters = 5.0
            )

            // Update point counter
            pointCounter = simplifiedPoints.size

            _uiState.update {
                it.copy(
                    boundaryPoints = simplifiedPoints,
                    smoothedPoints = simplifiedPoints,
                    isProcessing = false,
                    successMessage = "Processed ${locations.size} GPS points into ${simplifiedPoints.size} boundary points"
                )
            }

            // Calculate area
            updateAreaCalculation()
        }
    }

    private fun updateAreaCalculation() {
        val points = uiState.value.boundaryPoints
        if (points.size >= 3) {
            val areaAcres = PolygonCalculator.calculateAreaAcres(points)
            val perimeterMeters = PolygonCalculator.calculatePerimeter(points)

            _uiState.update {
                it.copy(
                    areaAcres = areaAcres,
                    perimeterMeters = perimeterMeters
                )
            }
        }
    }

    fun deleteBoundaryPoint(pointId: Int) {
        _uiState.update {
            it.copy(boundaryPoints = it.boundaryPoints.filter { point -> point.id != pointId })
        }
        updateAreaCalculation()
    }

    fun undoLastPoint() {
        val currentPoints = uiState.value.boundaryPoints
        if (currentPoints.isNotEmpty()) {
            pointCounter--
            _uiState.update {
                it.copy(boundaryPoints = currentPoints.dropLast(1))
            }
            updateAreaCalculation()
        }
    }

    fun clearBoundaryPoints() {
        pointCounter = 0
        kalmanFilter.reset()
        _uiState.update {
            it.copy(
                boundaryPoints = emptyList(),
                smoothedPoints = emptyList(),
                areaAcres = 0.0,
                perimeterMeters = 0.0
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    /**
     * Save boundary to database
     */
    fun saveBoundary() {
        val currentState = uiState.value
        
        if (currentState.boundaryPoints.size < 3) {
            _uiState.update { it.copy(errorMessage = "Need at least 3 points to save boundary") }
            return
        }

        if (plotId <= 0) {
            _uiState.update { it.copy(errorMessage = "No plot selected. Please select a plot first.") }
            return
        }

        viewModelScope.launch {
            try {
                val points = currentState.boundaryPoints
                
                // Calculate centroid
                val centroidLat = points.map { it.latitude }.average()
                val centroidLng = points.map { it.longitude }.average()
                
                // Calculate bounds
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLng = points.minOf { it.longitude }
                val maxLng = points.maxOf { it.longitude }
                
                // Calculate area in square meters
                val areaSquareMeters = PolygonCalculator.calculateArea(points)
                
                // Create or update boundary
                val existingBoundary = boundaryDao.getBoundaryByPlotId(plotId)
                val boundaryId = if (existingBoundary != null) {
                    // Update existing
                    val updated = existingBoundary.copy(
                        name = plotName,
                        areaAcres = currentState.areaAcres,
                        areaSquareMeters = areaSquareMeters,
                        perimeterMeters = currentState.perimeterMeters,
                        pointCount = points.size,
                        centroidLatitude = centroidLat,
                        centroidLongitude = centroidLng,
                        minLatitude = minLat,
                        maxLatitude = maxLat,
                        minLongitude = minLng,
                        maxLongitude = maxLng,
                        updatedAt = System.currentTimeMillis()
                    )
                    boundaryDao.updateBoundary(updated)
                    // Delete old points
                    boundaryDao.deletePointsByBoundaryId(existingBoundary.id)
                    existingBoundary.id
                } else {
                    // Insert new
                    val newBoundary = FarmBoundary(
                        plotId = plotId,
                        name = plotName,
                        areaAcres = currentState.areaAcres,
                        areaSquareMeters = areaSquareMeters,
                        perimeterMeters = currentState.perimeterMeters,
                        pointCount = points.size,
                        centroidLatitude = centroidLat,
                        centroidLongitude = centroidLng,
                        minLatitude = minLat,
                        maxLatitude = maxLat,
                        minLongitude = minLng,
                        maxLongitude = maxLng
                    )
                    boundaryDao.insertBoundary(newBoundary)
                }
                
                // Save all points
                val pointEntities = points.mapIndexed { index, point ->
                    BoundaryPointEntity(
                        boundaryId = boundaryId,
                        pointIndex = index,
                        latitude = point.latitude,
                        longitude = point.longitude,
                        altitude = if (point.altitude != 0.0) point.altitude else null,
                        accuracy = point.accuracy,
                        timestamp = point.timestamp
                    )
                }
                boundaryDao.insertAllPoints(pointEntities)
                
                _uiState.update {
                    it.copy(
                        successMessage = "Boundary saved! ${points.size} points, ${String.format("%.2f", currentState.areaAcres)} acres"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to save boundary: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationProvider.stopLocationUpdates()
    }
}