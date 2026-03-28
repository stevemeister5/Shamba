package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.MapTileCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapTileCacheDao {
    
    @Query("SELECT * FROM map_tile_cache ORDER BY downloadedAt DESC")
    fun getAllCachedRegions(): Flow<List<MapTileCacheEntity>>
    
    @Query("SELECT * FROM map_tile_cache WHERE regionId = :regionId")
    suspend fun getRegionById(regionId: String): MapTileCacheEntity?
    
    @Query("""
        SELECT * FROM map_tile_cache 
        WHERE minLatitude <= :maxLat 
        AND maxLatitude >= :minLat 
        AND minLongitude <= :maxLon 
        AND maxLongitude >= :minLon
    """)
    suspend fun getRegionsInBounds(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<MapTileCacheEntity>
    
    @Query("SELECT SUM(tileSizeBytes) FROM map_tile_cache")
    fun getTotalCacheSize(): Flow<Long?>
    
    @Query("SELECT COUNT(*) FROM map_tile_cache")
    fun getCachedRegionCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: MapTileCacheEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegions(regions: List<MapTileCacheEntity>)
    
    @Update
    suspend fun updateRegion(region: MapTileCacheEntity)
    
    @Delete
    suspend fun deleteRegion(region: MapTileCacheEntity)
    
    @Query("DELETE FROM map_tile_cache WHERE regionId = :regionId")
    suspend fun deleteRegionById(regionId: String)
    
    @Query("DELETE FROM map_tile_cache WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun deleteExpiredRegions(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM map_tile_cache")
    suspend fun deleteAllRegions()
}