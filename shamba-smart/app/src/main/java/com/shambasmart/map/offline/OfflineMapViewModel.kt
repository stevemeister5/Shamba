package com.shambasmart.map.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.common.OfflineSwitch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OfflineMapUiState(
    val isOfflineMode: Boolean = false,
    val activeDownloads: Map<String, OfflineRegionState> = emptyMap(),
    val cachedRegions: List<OfflineRegionState> = emptyList(),
    val totalCacheSize: Long = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class OfflineMapViewModel @Inject constructor(
    private val offlineMapManager: OfflineMapManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineMapUiState())
    val uiState: StateFlow<OfflineMapUiState> = _uiState.asStateFlow()

    init {
        observeManagerState()
    }

    private fun observeManagerState() {
        viewModelScope.launch {
            offlineMapManager.state.collect { managerState ->
                _uiState.update {
                    it.copy(
                        isOfflineMode = offlineMapManager.isOfflineMode(),
                        activeDownloads = managerState.activeDownloads,
                        cachedRegions = managerState.cachedRegions,
                        totalCacheSize = managerState.totalCacheSize,
                        errorMessage = managerState.errorMessage
                    )
                }
            }
        }
    }

    /**
     * Download the default farm region
     */
    fun downloadFarmRegion(name: String, minZoom: Int = 10, maxZoom: Int = 17) {
        val regionId = "farm_${System.currentTimeMillis()}"
        offlineMapManager.downloadRegion(
            regionId = regionId,
            name = name,
            minZoom = minZoom,
            maxZoom = maxZoom
        )
    }

    /**
     * Cancel an active download
     */
    fun cancelDownload(regionId: String) {
        offlineMapManager.cancelDownload(regionId)
    }

    /**
     * Delete a cached region
     */
    fun deleteCachedRegion(regionId: String) {
        offlineMapManager.deleteCachedRegion(regionId)
    }

    /**
     * Clear all cached regions
     */
    fun clearAllCache() {
        offlineMapManager.clearAllCache()
    }

    /**
     * Toggle offline mode
     */
    fun toggleOfflineMode() {
        val currentMode = offlineMapManager.isOfflineMode()
        offlineMapManager.setOfflineMode(!currentMode)
        _uiState.update { it.copy(isOfflineMode = !currentMode) }
    }

    fun clearError() {
        offlineMapManager.clearError()
        _uiState.update { it.copy(errorMessage = null) }
    }
}