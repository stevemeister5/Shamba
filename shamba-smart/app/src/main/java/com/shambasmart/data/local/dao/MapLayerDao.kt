package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.MapLayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapLayerDao {
    
    @Query("SELECT * FROM map_layers ORDER BY zIndex ASC")
    fun getAllLayers(): Flow<List<MapLayerEntity>>
    
    @Query("SELECT * FROM map_layers WHERE layerId = :layerId")
    suspend fun getLayerById(layerId: String): MapLayerEntity?
    
    @Query("SELECT * FROM map_layers WHERE isVisible = 1 ORDER BY zIndex ASC")
    fun getVisibleLayers(): Flow<List<MapLayerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayer(layer: MapLayerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayers(layers: List<MapLayerEntity>)
    
    @Update
    suspend fun updateLayer(layer: MapLayerEntity)
    
    @Query("UPDATE map_layers SET isVisible = :isVisible WHERE layerId = :layerId")
    suspend fun updateVisibility(layerId: String, isVisible: Boolean)
    
    @Query("UPDATE map_layers SET opacity = :opacity WHERE layerId = :layerId")
    suspend fun updateOpacity(layerId: String, opacity: Float)
    
    @Delete
    suspend fun deleteLayer(layer: MapLayerEntity)
    
    @Query("DELETE FROM map_layers WHERE layerId = :layerId")
    suspend fun deleteLayerById(layerId: String)
    
    @Query("DELETE FROM map_layers")
    suspend fun deleteAllLayers()
}