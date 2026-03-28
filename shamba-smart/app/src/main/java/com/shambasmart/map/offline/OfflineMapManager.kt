package com.shambasmart.map.offline

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.io.File
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

@Singleton
class OfflineMapManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(OfflineMapState())
    val state: StateFlow<OfflineMapState> = _state.asStateFlow()

    private val cacheDir: File = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
        "osmdroid_tiles"
    )

    init {
        cacheDir.mkdirs()
        loadCachedRegions()
    }

    private fun loadCachedRegions() {
        val regions = cacheDir.listFiles()?.filter { it.isDirectory }?.map { dir ->
            OfflineRegionState(
                regionId = dir.name,
                name = dir.name.replace("_", " ").capitalize(),
                isComplete = true,
                totalBytes = dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            )
        } ?: emptyList()
        
        val totalSize = regions.sumOf { it.totalBytes }
        
        _state.value = _state.value.copy(
            cachedRegions = regions,
            totalCacheSize = totalSize
        )
    }

    fun getOfflineTileProvider(): org.osmdroid.tileprovider.IRegisterReceiver? {
        return try {
            // OSMDroid offline tile provider using local tile archive
            val archiveFiles = cacheDir.listFiles()?.filter { it.extension == "sqlite" }
            if (archiveFiles.isNullOrEmpty()) {
                null
            } else {
                // Return null for now - offline tile loading would need custom implementation
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getTileSource(): org.osmdroid.tileprovider.tilesource.ITileSource {
        return TileSourceFactory.DEFAULT_TILE_SOURCE
    }

    fun getMapCenter(): GeoPoint {
        // Default to Korogwe, Tanzania
        return GeoPoint(-5.15, 38.48)
    }

    fun getZoomLevel(): Double {
        return 14.0
    }

    fun isOfflineAvailable(): Boolean {
        return cacheDir.exists() && cacheDir.listFiles()?.isNotEmpty() == true
    }

    fun getCachedTileCount(): Int {
        return cacheDir.listFiles()?.sumOf { dir ->
            dir.walkTopDown().filter { it.isFile }.count()
        } ?: 0
    }

    fun getCacheSizeFormatted(): String {
        val bytes = _state.value.totalCacheSize
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    fun clearCache() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        loadCachedRegions()
    }
}