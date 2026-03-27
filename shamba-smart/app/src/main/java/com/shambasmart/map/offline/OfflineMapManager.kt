package com.shambasmart.map.offline

import android.content.Context
import com.mapbox.bindgen.Expected
import com.mapbox.common.Cancelable
import com.mapbox.common.NetworkRestriction
import com.mapbox.common.OfflineSwitch
import com.mapbox.geojson.BoundingBox
import com.mapbox.maps.*
import com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class OfflineRegionState(
    val regionId: String,
    val name: String,
    val progress: Float = 0f, // 0.0 to 1.0
    val isComplete: Boolean = false,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val downloadSpeed: Double = 0.0, // bytes per second
    val estimatedTimeRemaining: Long = 0, // seconds
    val error: String? = null
)

data class OfflineMapState(
    val isDownloading: Boolean = false,
    val activeDownloads: Map<String, OfflineRegionState> = emptyMap(),
    val cachedRegions: List<OfflineRegionState> = emptyList(),
    val totalCacheSize: Long = 0,
    val errorMessage: String? = null
)

/**
 * Manages offline map tile downloads using Mapbox OfflineManager.
 * 
 * Supports downloading tiles for offline use in areas with poor connectivity.
 * The farm in Korogwe has limited connectivity, so offline maps are essential.
 */
@Singleton
class OfflineMapManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(OfflineMapState())
    val state: StateFlow<OfflineMapState> = _state.asStateFlow()
    
    private var offlineManager: OfflineManager? = null
    private var activeDownload: Cancelable? = null
    
    // Farm bounds (Korogwe area - roughly 10km x 10km)
    val farmBoundingBox = BoundingBox(
        -5.25, // south
        38.38, // west
        -5.05, // north
        38.58  // east
    )
    
    /**
     * Initialize the offline manager
     */
    fun initialize(resourceOptions: ResourceOptions) {
        offlineManager = OfflineManager.createInstance(resourceOptions)
    }
    
    /**
     * Download map tiles for the farm area.
     * 
     * @param regionId Unique identifier for the download region
     * @param name Human-readable name for the region
     * @param minZoom Minimum zoom level (default: 10 for overview)
     * @param maxZoom Maximum zoom level (default: 17 for detail)
     * @param styleUri Mapbox style URI to download
     */
    fun downloadRegion(
        regionId: String,
        name: String,
        minZoom: Int = 10,
        maxZoom: Int = 17,
        styleUri: String = Style.MAPBOX_STREETS
    ) {
        val manager = offlineManager ?: return
        
        // Check if already downloading or downloaded
        if (_state.value.activeDownloads.containsKey(regionId)) {
            _state.update { it.copy(errorMessage = "Region '$name' is already downloading") }
            return
        }
        
        // Create tile region definition
        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .geometry(farmBoundingBox.toGeometry())
            .descriptors(
                listOf(
                    TilesetDescriptorOptions.Builder()
                        .styleURI(styleUri)
                        .minZoom(minZoom)
                        .maxZoom(maxZoom)
                        .build()
                )
            )
            .acceptExpiredTiles(true)
            .networkRestriction(NetworkRestriction.NONE)
            .build()
        
        // Update state to show download started
        val initialState = OfflineRegionState(
            regionId = regionId,
            name = name,
            progress = 0f
        )
        _state.update { 
            it.copy(
                isDownloading = true,
                activeDownloads = it.activeDownloads + (regionId to initialState)
            )
        }
        
        // Start download
        activeDownload = manager.loadTileRegion(
            regionId,
            tileRegionLoadOptions,
            { progress ->
                // Progress callback
                val downloadedBytes = progress.completedResourceCount
                val totalBytes = progress.requiredResourceCount
                val progressPercent = if (totalBytes > 0) {
                    downloadedBytes.toFloat() / totalBytes.toFloat()
                } else 0f
                
                val regionState = OfflineRegionState(
                    regionId = regionId,
                    name = name,
                    progress = progressPercent,
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes
                )
                
                _state.update { 
                    it.copy(activeDownloads = it.activeDownloads + (regionId to regionState))
                }
            },
            { result ->
                // Completion callback
                if (result.isValue) {
                    val completedState = OfflineRegionState(
                        regionId = regionId,
                        name = name,
                        progress = 1f,
                        isComplete = true
                    )
                    
                    _state.update {
                        it.copy(
                            isDownloading = it.activeDownloads.any { entry -> 
                                entry.key != regionId && !entry.value.isComplete 
                            },
                            activeDownloads = it.activeDownloads - regionId,
                            cachedRegions = it.cachedRegions + completedState,
                            errorMessage = null
                        )
                    }
                } else {
                    val errorState = OfflineRegionState(
                        regionId = regionId,
                        name = name,
                        error = result.error?.message ?: "Download failed"
                    )
                    
                    _state.update {
                        it.copy(
                            isDownloading = it.activeDownloads.any { entry -> 
                                entry.key != regionId && !entry.value.isComplete 
                            },
                            activeDownloads = it.activeDownloads - regionId,
                            errorMessage = "Failed to download '$name': ${result.error?.message}"
                        )
                    }
                }
                activeDownload = null
            }
        )
    }
    
    /**
     * Cancel an active download
     */
    fun cancelDownload(regionId: String) {
        activeDownload?.cancel()
        activeDownload = null
        
        _state.update {
            it.copy(
                isDownloading = it.activeDownloads.any { entry -> 
                    entry.key != regionId && !entry.value.isComplete 
                },
                activeDownloads = it.activeDownloads - regionId
            )
        }
    }
    
    /**
     * Delete a cached region to free up storage
     */
    fun deleteCachedRegion(regionId: String) {
        val manager = offlineManager ?: return
        
        manager.removeTileRegion(regionId) { result ->
            if (result.isValue) {
                _state.update {
                    it.copy(
                        cachedRegions = it.cachedRegions.filter { r -> r.regionId != regionId },
                        errorMessage = null
                    )
                }
            } else {
                _state.update {
                    it.copy(errorMessage = "Failed to delete region: ${result.error?.message}")
                }
            }
        }
    }
    
    /**
     * Clear all cached regions
     */
    fun clearAllCache() {
        val manager = offlineManager ?: return
        
        // Clear all tile regions
        manager.getAllTileRegions { result ->
            if (result.isValue) {
                result.value?.forEach { region ->
                    manager.removeTileRegion(region.id) { }
                }
                _state.update {
                    it.copy(cachedRegions = emptyList())
                }
            }
        }
    }
    
    /**
     * Get estimated download size for a region
     */
    fun estimateDownloadSize(
        minZoom: Int = 10,
        maxZoom: Int = 17,
        styleUri: String = Style.MAPBOX_STREETS,
        onResult: (Long) -> Unit
    ) {
        val manager = offlineManager ?: return
        
        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .geometry(farmBoundingBox.toGeometry())
            .descriptors(
                listOf(
                    TilesetDescriptorOptions.Builder()
                        .styleURI(styleUri)
                        .minZoom(minZoom)
                        .maxZoom(maxZoom)
                        .build()
                )
            )
            .build()
        
        manager.estimateTileRegion(
            "estimate_${System.currentTimeMillis()}",
            tileRegionLoadOptions,
            { /* Progress - ignore for estimate */ },
            { result ->
                if (result.isValue) {
                    onResult(result.value?.byteCount ?: 0)
                } else {
                    onResult(0)
                }
            }
        )
    }
    
    /**
     * Enable/disable offline mode
     */
    fun setOfflineMode(enabled: Boolean) {
        OfflineSwitch.getInstance().setMapboxStackConnected(!enabled)
    }
    
    /**
     * Check if currently in offline mode
     */
    fun isOfflineMode(): Boolean {
        return !OfflineSwitch.getInstance().isMapboxStackConnected
    }
    
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}