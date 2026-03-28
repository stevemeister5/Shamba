package com.shambasmart.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.shambasmart.data.local.dao.MapLayerDao
import com.shambasmart.data.local.dao.MapMarkerDao
import com.shambasmart.data.local.dao.ScoutingReportDao
import com.shambasmart.data.local.dao.ScoutingHeatmapPoint
import com.shambasmart.data.local.entity.LatLng
import com.shambasmart.data.local.entity.MapLayerEntity
import com.shambasmart.data.local.entity.MapMarkerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val markers: List<MapMarkerEntity> = emptyList(),
    val selectedMarker: MapMarkerEntity? = null,
    val visibleLayers: Set<String> = setOf("plots", "infrastructure", "livestock", "water", "waste", "safety", "custom"),
    val activeHeatmap: HeatmapType? = null,
    val isDrawingMode: Boolean = false,
    val drawingTool: DrawingTool? = null,
    val drawingPoints: List<org.osmdroid.util.GeoPoint> = emptyList(),
    val isOfflineMode: Boolean = false,
    val isGpsTracking: Boolean = false,
    val currentLocation: LatLng? = null,
    val cameraPosition: MapCameraPosition? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showLinkDialog: Boolean = false,
    val markerToLink: MapMarkerEntity? = null
)

data class MapCameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double = 15.0
)

enum class DrawingTool {
    POINT,      // Place a marker
    POLYGON,    // Draw a boundary
    LINE,       // Draw a fence/line
    ERASER      // Delete markers
}

@HiltViewModel
class FarmMapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mapMarkerDao: MapMarkerDao,
    private val mapLayerDao: MapLayerDao,
    private val scoutingReportDao: ScoutingReportDao,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var locationCallback: LocationCallback? = null

    // Farm center coordinates (Korogwe, Tanga)
    val farmCenterLat = -5.15
    val farmCenterLng = 38.48

    init {
        loadMarkers()
        loadLayerPreferences()
        initializeDefaultLayers()
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            mapMarkerDao.getAllMarkers()
                .collect { markers ->
                    _uiState.update { it.copy(markers = markers) }
                }
        }
    }

    private fun loadLayerPreferences() {
        viewModelScope.launch {
            mapLayerDao.getAllLayers()
                .collect { layers ->
                    val visibleLayers = layers
                        .filter { it.isVisible }
                        .map { it.layerId }
                        .toSet()
                    _uiState.update { it.copy(visibleLayers = visibleLayers) }
                }
        }
    }

    private fun initializeDefaultLayers() {
        viewModelScope.launch {
            val defaultLayers = listOf(
                MapLayerEntity("plots", true, 1.0f, 1),
                MapLayerEntity("infrastructure", true, 1.0f, 2),
                MapLayerEntity("livestock", true, 1.0f, 3),
                MapLayerEntity("water", true, 1.0f, 4),
                MapLayerEntity("waste", true, 1.0f, 5),
                MapLayerEntity("safety", true, 1.0f, 6),
                MapLayerEntity("custom", true, 1.0f, 7),
                MapLayerEntity("heatmap_yield", false, 0.7f, 8),
                MapLayerEntity("heatmap_cost", false, 0.7f, 9),
                MapLayerEntity("heatmap_animals", false, 0.7f, 10),
                MapLayerEntity("heatmap_moisture", false, 0.7f, 11),
                MapLayerEntity("heatmap_health", false, 0.7f, 12),
                MapLayerEntity("heatmap_water", false, 0.7f, 13),
                MapLayerEntity("heatmap_feed", false, 0.7f, 14),
                MapLayerEntity("heatmap_revenue", false, 0.7f, 15)
            )
            mapLayerDao.insertLayers(defaultLayers)
        }
    }

    fun addMarker(
        name: String,
        markerType: MapMarkerType,
        latitude: Double,
        longitude: Double,
        description: String? = null,
        linkedEntityType: String? = null,
        linkedEntityId: Long? = null,
        boundaryPoints: List<LatLng>? = null
    ) {
        viewModelScope.launch {
            try {
                val marker = MapMarkerEntity(
                    name = name,
                    markerType = markerType.name,
                    category = markerType.category.name,
                    latitude = latitude,
                    longitude = longitude,
                    icon = markerType.icon,
                    color = markerType.color,
                    description = description,
                    linkedEntityType = linkedEntityType,
                    linkedEntityId = linkedEntityId,
                    boundaryPoints = boundaryPoints,
                    areaSquareMeters = boundaryPoints?.let { calculatePolygonArea(it) }
                )
                val id = mapMarkerDao.insertMarker(marker)
                _uiState.update {
                    it.copy(successMessage = "Marker '${name}' added successfully")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to add marker: ${e.message}")
                }
            }
        }
    }

    fun updateMarker(marker: MapMarkerEntity) {
        viewModelScope.launch {
            try {
                mapMarkerDao.updateMarker(marker.copy(updatedAt = System.currentTimeMillis()))
                _uiState.update {
                    it.copy(successMessage = "Marker updated successfully")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update marker: ${e.message}")
                }
            }
        }
    }

    fun deleteMarker(marker: MapMarkerEntity) {
        viewModelScope.launch {
            try {
                mapMarkerDao.deleteMarker(marker)
                _uiState.update {
                    it.copy(
                        selectedMarker = null,
                        successMessage = "Marker deleted successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete marker: ${e.message}")
                }
            }
        }
    }

    fun selectMarker(marker: MapMarkerEntity?) {
        _uiState.update { it.copy(selectedMarker = marker) }
    }

    fun toggleLayer(layerId: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            val isVisible = layerId in currentState.visibleLayers
            mapLayerDao.updateVisibility(layerId, !isVisible)
            
            val newVisibleLayers = if (isVisible) {
                currentState.visibleLayers - layerId
            } else {
                currentState.visibleLayers + layerId
            }
            _uiState.update { it.copy(visibleLayers = newVisibleLayers) }
        }
    }

    fun setHeatmap(heatmap: HeatmapType?) {
        _uiState.update { it.copy(activeHeatmap = heatmap) }
    }

    fun setDrawingMode(enabled: Boolean, tool: DrawingTool? = null) {
        _uiState.update {
            it.copy(
                isDrawingMode = enabled,
                drawingTool = if (enabled) tool else null
            )
        }
    }

    fun addDrawingPoint(point: org.osmdroid.util.GeoPoint) {
        _uiState.update {
            it.copy(drawingPoints = it.drawingPoints + point)
        }
    }

    fun removeDrawingPoint() {
        _uiState.update {
            if (it.drawingPoints.isNotEmpty()) {
                it.copy(drawingPoints = it.drawingPoints.dropLast(1))
            } else {
                it
            }
        }
    }

    fun clearDrawingPoints() {
        _uiState.update { it.copy(drawingPoints = emptyList()) }
    }

    fun setCameraPosition(position: MapCameraPosition) {
        _uiState.update { it.copy(cameraPosition = position) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // GPS Tracking
    fun toggleGpsTracking() {
        if (uiState.value.isGpsTracking) {
            stopGpsTracking()
        } else {
            startGpsTracking()
        }
    }

    private fun startGpsTracking() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            _uiState.update { it.copy(errorMessage = "Location permission not granted") }
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _uiState.update { 
                        it.copy(
                            currentLocation = LatLng(location.latitude, location.longitude),
                            isGpsTracking = true
                        )
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                null
            )
            _uiState.update { it.copy(isGpsTracking = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Failed to start GPS: ${e.message}") }
        }
    }

    private fun stopGpsTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        _uiState.update { it.copy(isGpsTracking = false, currentLocation = null) }
    }

    // Marker-Entity Linking
    fun showLinkDialog(marker: MapMarkerEntity) {
        _uiState.update { it.copy(showLinkDialog = true, markerToLink = marker) }
    }

    fun dismissLinkDialog() {
        _uiState.update { it.copy(showLinkDialog = false, markerToLink = null) }
    }

    fun linkMarkerToEntity(marker: MapMarkerEntity, entityType: String, entityId: Long) {
        viewModelScope.launch {
            try {
                val updatedMarker = marker.copy(
                    linkedEntityType = entityType,
                    linkedEntityId = entityId,
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updatedMarker)
                _uiState.update {
                    it.copy(
                        successMessage = "Marker linked to $entityType #$entityId",
                        showLinkDialog = false,
                        markerToLink = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to link marker: ${e.message}") }
            }
        }
    }

    fun getMarkersByCategory(category: MarkerCategory): Flow<List<MapMarkerEntity>> {
        return mapMarkerDao.getMarkersByCategory(category.name)
    }

    fun getMarkersByType(markerType: MapMarkerType): Flow<List<MapMarkerEntity>> {
        return mapMarkerDao.getMarkersByType(markerType.name)
    }

    fun searchMarkers(query: String): Flow<List<MapMarkerEntity>> {
        return mapMarkerDao.searchMarkers(query)
    }

    fun getMarkersInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<List<MapMarkerEntity>> {
        return mapMarkerDao.getMarkersInBounds(minLat, maxLat, minLng, maxLng)
    }

    fun getMarkerByLinkedEntity(entityType: String, entityId: Long): Flow<MapMarkerEntity?> {
        return mapMarkerDao.getMarkerByLinkedEntityFlow(entityType, entityId)
    }

    /**
     * Gets pest scouting heatmap data for the pest heatmap overlay.
     * @return List of ScoutingHeatmapPoint for rendering
     */
    suspend fun getPestHeatmapData(): List<ScoutingHeatmapPoint> {
        return try {
            scoutingReportDao.getHeatmapData()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun calculatePolygonArea(points: List<LatLng>): Double {
        if (points.size < 3) return 0.0
        
        var area = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            area += points[i].longitude * points[j].latitude
            area -= points[j].longitude * points[i].latitude
        }
        area = kotlin.math.abs(area) / 2.0
        
        // Convert to square meters (approximate)
        val avgLat = points.map { it.latitude }.average()
        val metersPerDegreeLat = 111320.0
        val metersPerDegreeLng = 111320.0 * kotlin.math.cos(Math.toRadians(avgLat))
        
        return area * metersPerDegreeLat * metersPerDegreeLng
    }

    override fun onCleared() {
        super.onCleared()
        stopGpsTracking()
    }
}