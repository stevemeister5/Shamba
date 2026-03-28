package com.shambasmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID

/**
 * Map marker entity for storing farm markers
 * 
 * Supports 35 marker types across 7 categories:
 * - Crops (8 types)
 * - Infrastructure (8 types)
 * - Water & Energy (5 types)
 * - Livestock (5 types)
 * - Waste & Compost (3 types)
 * - Safety & Boundaries (3 types)
 * - Custom (3 types)
 */
@Entity(tableName = "map_markers")
@TypeConverters(MapMarkerConverters::class)
data class MapMarkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val markerType: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val icon: String,
    val color: String,
    val description: String? = null,
    val linkedEntityType: String? = null,
    val linkedEntityId: Long? = null,
    val areaSquareMeters: Double? = null,
    val boundaryPoints: List<LatLng>? = null,
    val photos: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    // Revision-based delta sync fields
    @ColumnInfo(name = "revision_id")
    val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by")
    val lastModifiedBy: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Map layer visibility preferences
 */
@Entity(tableName = "map_layers")
data class MapLayerEntity(
    @PrimaryKey
    val layerId: String, // "plots", "infrastructure", "livestock", "fences", "heatmap_yield", etc.
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f,
    val zIndex: Int = 0
)

/**
 * Offline map tile cache metadata
 */
@Entity(tableName = "map_tile_cache")
data class MapTileCacheEntity(
    @PrimaryKey
    val regionId: String, // Unique ID for cached region
    val name: String,
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double,
    val minZoom: Int,
    val maxZoom: Int,
    val tileSizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

/**
 * Simple latitude/longitude data class for boundary points
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

/**
 * Room type converters for MapMarker
 */
class MapMarkerConverters {
    @TypeConverter
    fun fromLatLngList(value: List<LatLng>?): String? {
        return value?.joinToString(";") { "${it.latitude},${it.longitude}" }
    }

    @TypeConverter
    fun toLatLngList(value: String?): List<LatLng>? {
        return value?.split(";")?.map {
            val parts = it.split(",")
            LatLng(parts[0].toDouble(), parts[1].toDouble())
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return value.entries.joinToString(";") { "${it.key}=${it.value}" }
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        if (value.isEmpty()) return emptyMap()
        return value.split(";").associate {
            val parts = it.split("=")
            parts[0] to parts[1]
        }
    }
}