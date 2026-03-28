package com.shambasmart.map.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                        cachedRegions = managerState.cachedRegions,
                        totalCacheSize = managerState.totalCacheSize,
                        errorMessage = managerState.errorMessage
                    )
                }
            }
        }
    }

    fun downloadRegion(name: String, bounds: BoundingBox) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isOfflineMode = true) }
                // Placeholder for download implementation
                // In a real implementation, this would use OSMDroid's tile download capabilities
                _uiState.update { 
                    it.copy(
                        isOfflineMode = false,
                        errorMessage = "Download functionality not yet implemented"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOfflineMode = false,
                        errorMessage = "Download failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteRegion(regionId: String) {
        viewModelScope.launch {
            try {
                offlineMapManager.clearCache()
                _uiState.update {
                    it.copy(errorMessage = "Cache cleared")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Delete failed: ${e.message}")
                }
            }
        }
    }

    fun getCachedTileCount(): Int {
        return offlineMapManager.getCachedTileCount()
    }

    fun getCacheSizeFormatted(): String {
        return offlineMapManager.getCacheSizeFormatted()
    }

    fun isOfflineAvailable(): Boolean {
        return offlineMapManager.isOfflineAvailable()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}