package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.LatLng
import com.shambasmart.data.local.entity.MapLayerEntity
import com.shambasmart.data.local.entity.MapMarkerEntity
import com.shambasmart.data.local.entity.MapTileCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapMarkerDao {
    // Basic CRUD
    @Query("SELECT * FROM map_markers ORDER BY updatedAt DESC")
    fun getAllMarkers(): Flow<List<MapMarkerEntity>>

    @Query("SELECT * FROM map_markers WHERE id = :id")
    suspend fun getMarkerById(id: Long): MapMarkerEntity?

    @Query("SELECT * FROM map_markers WHERE linkedEntityType = :entityType AND linkedEntityId = :entityId")
    suspend fun getMarkerByLinkedEntity(entityType: String, entityId: Long): MapMarkerEntity?

    @Query("SELECT * FROM map_markers WHERE linkedEntityType = :entityType AND linkedEntityId = :entityId")
    fun getMarkerByLinkedEntityFlow(entityType: String, entityId: Long): Flow<MapMarkerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MapMarkerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMarkers(markers: List<MapMarkerEntity>)

    @Update
    suspend fun updateMarker(marker: MapMarkerEntity)

    @Delete
    suspend fun deleteMarker(marker: MapMarkerEntity)

    @Query("DELETE FROM map_markers WHERE id = :id")
    suspend fun deleteMarkerById(id: Long)

    // Category queries
    @Query("SELECT * FROM map_markers WHERE category = :category ORDER BY name ASC")
    fun getMarkersByCategory(category: String): Flow<List<MapMarkerEntity>>

    @Query("SELECT * FROM map_markers WHERE markerType = :markerType ORDER BY name ASC")
    fun getMarkersByType(markerType: String): Flow<List<MapMarkerEntity>>

    // Bounding box queries (for map viewport)
    @Query("""
        SELECT * FROM map_markers 
        WHERE latitude >= :minLat AND latitude <= :maxLat 
        AND longitude >= :minLng AND longitude <= :maxLng
        ORDER BY name ASC
    """)
    fun getMarkersInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<List<MapMarkerEntity>>

    // Linked entity queries
    @Query("SELECT * FROM map_markers WHERE linkedEntityType = :entityType ORDER BY updatedAt DESC")
    fun getMarkersByLinkedEntityType(entityType: String): Flow<List<MapMarkerEntity>>

    // Sync queries
    @Query("UPDATE map_markers SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM map_markers WHERE isSynced = 0")
    suspend fun getUnsyncedMarkers(): List<MapMarkerEntity>

    // Aggregate queries
    @Query("SELECT COUNT(*) FROM map_markers WHERE category = :category")
    suspend fun getMarkerCountByCategory(category: String): Int

    @Query("SELECT SUM(areaSquareMeters) FROM map_markers WHERE category = 'CROP' AND areaSquareMeters IS NOT NULL")
    suspend fun getTotalCropArea(): Double?

    // Search
    @Query("SELECT * FROM map_markers WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchMarkers(query: String): Flow<List<MapMarkerEntity>>
}
