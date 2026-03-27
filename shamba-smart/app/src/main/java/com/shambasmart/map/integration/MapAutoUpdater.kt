package com.shambasmart.map.integration

import com.shambasmart.data.local.dao.MapMarkerDao
import com.shambasmart.data.local.entity.LatLng
import com.shambasmart.data.local.entity.MapMarkerEntity
import com.shambasmart.map.MapMarkerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Automatically creates and updates map markers when other farm entities are modified.
 * 
 * This ensures the farm map stays in sync with:
 * - Crop plantings → Plot markers
 * - Animal additions → Pen markers
 * - Feed purchases → Storage markers
 * - Cheese batches → Cheese room markers
 * - Harvest logs → Yield heatmap data
 * - Water/irrigation → Water markers
 */
@Singleton
class MapAutoUpdater @Inject constructor(
    private val mapMarkerDao: MapMarkerDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Default farm center coordinates (Korogwe)
    private val farmLat = -5.15
    private val farmLng = 38.48

    // Predefined plot positions (offsets from farm center)
    private val plotPositions = listOf(
        LatLng(-5.148, 38.478),
        LatLng(-5.152, 38.482),
        LatLng(-5.146, 38.476),
        LatLng(-5.154, 38.484),
        LatLng(-5.150, 38.480),
        LatLng(-5.148, 38.486),
        LatLng(-5.152, 38.474),
        LatLng(-5.146, 38.488)
    )

    // Predefined infrastructure positions
    private val infrastructurePositions = mapOf(
        "mainHouse" to LatLng(-5.1500, 38.4800),
        "workerQuarters" to LatLng(-5.1505, 38.4805),
        "equipmentShed" to LatLng(-5.1495, 38.4795),
        "feedStorage" to LatLng(-5.1510, 38.4810),
        "seedStorage" to LatLng(-5.1490, 38.4815),
        "cheeseRoom" to LatLng(-5.1502, 38.4808),
        "milkCollection" to LatLng(-5.1508, 38.4802),
        "dippingTank" to LatLng(-5.1512, 38.4812)
    )

    // Predefined livestock positions
    private val livestockPositions = mapOf(
        "goatPen1" to LatLng(-5.1520, 38.4820),
        "goatPen2" to LatLng(-5.1525, 38.4825),
        "sheepPen" to LatLng(-5.1515, 38.4815),
        "poultryHouse" to LatLng(-5.1530, 38.4830),
        "isolationPen" to LatLng(-5.1535, 38.4835),
        "breedingPen" to LatLng(-5.1540, 38.4840)
    )

    // Water source positions
    private val waterPositions = mapOf(
        "borehole" to LatLng(-5.1485, 38.4790),
        "waterTrough1" to LatLng(-5.1518, 38.4818),
        "waterTrough2" to LatLng(-5.1528, 38.4828),
        "waterTrough3" to LatLng(-5.1538, 38.4838),
        "irrigation1" to LatLng(-5.1475, 38.4775),
        "irrigation2" to LatLng(-5.1485, 38.4785),
        "rainwaterTank" to LatLng(-5.1498, 38.4798),
        "solarPanel" to LatLng(-5.1492, 38.4792)
    )

    /**
     * Called when a new crop is planted.
     * Creates a plot marker on the map.
     */
    fun onCropPlanted(
        cropName: String,
        cropType: String,
        plotIndex: Int = 0,
        areaSquareMeters: Double? = null,
        linkedEntityId: Long? = null
    ) {
        scope.launch {
            val markerType = when (cropType.lowercase()) {
                "maize", "corn" -> MapMarkerType.MAIZE_PLOT
                "beans", "bean" -> MapMarkerType.BEAN_PLOT
                "tomatoes", "tomato" -> MapMarkerType.TOMATO_PLOT
                "kale", "sukuma wiki" -> MapMarkerType.KALE_PLOT
                "onions", "onion" -> MapMarkerType.ONION_PLOT
                "napier", "napier grass" -> MapMarkerType.NAPIER_GRASS
                "cassava" -> MapMarkerType.CASSAVA_PLOT
                "sweet potato", "sweetpotato" -> MapMarkerType.SWEET_POTATO
                else -> MapMarkerType.MAIZE_PLOT
            }

            val position = plotPositions.getOrElse(plotIndex % plotPositions.size) {
                LatLng(farmLat + (plotIndex * 0.002), farmLng + (plotIndex * 0.002))
            }

            // Create boundary points for the plot (simple rectangle)
            val boundaryPoints = if (areaSquareMeters != null) {
                createPlotBoundary(position, areaSquareMeters)
            } else null

            // Check if marker already exists for this entity
            val existingMarker = linkedEntityId?.let {
                mapMarkerDao.getMarkerByLinkedEntity("crop", it)
            }

            if (existingMarker == null) {
                val marker = MapMarkerEntity(
                    name = "$cropName Plot",
                    markerType = markerType.name,
                    category = markerType.category.name,
                    latitude = position.latitude,
                    longitude = position.longitude,
                    icon = markerType.icon,
                    color = markerType.color,
                    description = "Crop: $cropType",
                    linkedEntityType = "crop",
                    linkedEntityId = linkedEntityId,
                    boundaryPoints = boundaryPoints,
                    areaSquareMeters = areaSquareMeters,
                    metadata = mapOf(
                        "cropType" to cropType,
                        "plotIndex" to plotIndex.toString()
                    )
                )
                mapMarkerDao.insertMarker(marker)
            } else {
                // Update existing marker
                val updated = existingMarker.copy(
                    name = "$cropName Plot",
                    areaSquareMeters = areaSquareMeters ?: existingMarker.areaSquareMeters,
                    boundaryPoints = boundaryPoints ?: existingMarker.boundaryPoints,
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updated)
            }
        }
    }

    /**
     * Called when a harvest is logged.
     * Updates marker metadata with yield data.
     */
    fun onHarvestLogged(
        cropType: String,
        yieldKg: Double,
        plotIndex: Int = 0,
        linkedEntityId: Long? = null
    ) {
        scope.launch {
            val marker = linkedEntityId?.let {
                mapMarkerDao.getMarkerByLinkedEntity("crop", it)
            }

            if (marker != null) {
                val updated = marker.copy(
                    metadata = marker.metadata + mapOf(
                        "lastHarvestKg" to yieldKg.toString(),
                        "lastHarvestDate" to System.currentTimeMillis().toString()
                    ),
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updated)
            }
        }
    }

    /**
     * Called when a new animal is added.
     * Creates/updates pen marker on the map.
     */
    fun onAnimalAdded(
        animalType: String,
        penName: String? = null,
        animalCount: Int = 1,
        linkedEntityId: Long? = null
    ) {
        scope.launch {
            val markerType = when (animalType.lowercase()) {
                "goat" -> MapMarkerType.GOAT_PEN
                "sheep" -> MapMarkerType.SHEEP_PEN
                "chicken", "poultry" -> MapMarkerType.POULTRY_HOUSE
                else -> MapMarkerType.GOAT_PEN
            }

            val penKey = when (animalType.lowercase()) {
                "goat" -> if (penName?.contains("2") == true) "goatPen2" else "goatPen1"
                "sheep" -> "sheepPen"
                "chicken", "poultry" -> "poultryHouse"
                else -> "goatPen1"
            }

            val position = livestockPositions[penKey] ?: LatLng(farmLat, farmLng)

            // Check if marker already exists for this pen
            val existingMarkers = mutableListOf<MapMarkerEntity>()
            mapMarkerDao.getMarkersByType(markerType.name).collect { markers ->
                existingMarkers.addAll(markers)
            }

            if (existingMarkers.isEmpty()) {
                val marker = MapMarkerEntity(
                    name = penName ?: "${markerType.displayName}",
                    markerType = markerType.name,
                    category = markerType.category.name,
                    latitude = position.latitude,
                    longitude = position.longitude,
                    icon = markerType.icon,
                    color = markerType.color,
                    description = "Livestock: $animalType",
                    linkedEntityType = "animal",
                    linkedEntityId = linkedEntityId,
                    metadata = mapOf(
                        "animalType" to animalType,
                        "animalCount" to animalCount.toString()
                    )
                )
                mapMarkerDao.insertMarker(marker)
            } else {
                // Update existing pen with new count
                val existing = existingMarkers.first()
                val currentCount = existing.metadata["animalCount"]?.toIntOrNull() ?: 0
                val updated = existing.copy(
                    metadata = existing.metadata + mapOf(
                        "animalCount" to (currentCount + animalCount).toString()
                    ),
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updated)
            }
        }
    }

    /**
     * Called when feed is purchased/added.
     * Creates feed storage marker.
     */
    fun onFeedAdded(
        feedType: String,
        quantityKg: Double,
        linkedEntityId: Long? = null
    ) {
        scope.launch {
            val position = infrastructurePositions["feedStorage"] ?: LatLng(farmLat, farmLng)

            val existingMarker = linkedEntityId?.let {
                mapMarkerDao.getMarkerByLinkedEntity("feed", it)
            }

            if (existingMarker == null) {
                val marker = MapMarkerEntity(
                    name = "Feed Storage",
                    markerType = MapMarkerType.FEED_STORAGE.name,
                    category = MapMarkerType.FEED_STORAGE.category.name,
                    latitude = position.latitude,
                    longitude = position.longitude,
                    icon = MapMarkerType.FEED_STORAGE.icon,
                    color = MapMarkerType.FEED_STORAGE.color,
                    description = "Feed storage facility",
                    linkedEntityType = "feed",
                    linkedEntityId = linkedEntityId,
                    metadata = mapOf(
                        "feedType" to feedType,
                        "quantityKg" to quantityKg.toString()
                    )
                )
                mapMarkerDao.insertMarker(marker)
            } else {
                val currentQty = existingMarker.metadata["quantityKg"]?.toDoubleOrNull() ?: 0.0
                val updated = existingMarker.copy(
                    metadata = existingMarker.metadata + mapOf(
                        "quantityKg" to (currentQty + quantityKg).toString()
                    ),
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updated)
            }
        }
    }

    /**
     * Called when cheese is made.
     * Creates/updates cheese room marker.
     */
    fun onCheeseBatchMade(
        batchName: String,
        milkLiters: Double,
        cheeseKg: Double,
        linkedEntityId: Long? = null
    ) {
        scope.launch {
            val position = infrastructurePositions["cheeseRoom"] ?: LatLng(farmLat, farmLng)

            val existingMarker = linkedEntityId?.let {
                mapMarkerDao.getMarkerByLinkedEntity("cheese", it)
            }

            if (existingMarker == null) {
                val marker = MapMarkerEntity(
                    name = "Cheese Room",
                    markerType = MapMarkerType.CHEESE_ROOM.name,
                    category = MapMarkerType.CHEESE_ROOM.category.name,
                    latitude = position.latitude,
                    longitude = position.longitude,
                    icon = MapMarkerType.CHEESE_ROOM.icon,
                    color = MapMarkerType.CHEESE_ROOM.color,
                    description = "Cheese production facility",
                    linkedEntityType = "cheese",
                    linkedEntityId = linkedEntityId,
                    metadata = mapOf(
                        "lastBatch" to batchName,
                        "lastMilkLiters" to milkLiters.toString(),
                        "lastCheeseKg" to cheeseKg.toString()
                    )
                )
                mapMarkerDao.insertMarker(marker)
            } else {
                val updated = existingMarker.copy(
                    metadata = existingMarker.metadata + mapOf(
                        "lastBatch" to batchName,
                        "lastMilkLiters" to milkLiters.toString(),
                        "lastCheeseKg" to cheeseKg.toString()
                    ),
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updated)
            }
        }
    }

    /**
     * Called when water/irrigation is used.
     * Updates water marker metadata.
     */
    fun onWaterUsed(
        waterSource: String,
        litersUsed: Double
    ) {
        scope.launch {
            val position = waterPositions[waterSource] ?: waterPositions["borehole"] ?: LatLng(farmLat, farmLng)
            val markerType = when {
                waterSource.contains("borehole", true) -> MapMarkerType.BOREHOLE
                waterSource.contains("trough", true) -> MapMarkerType.WATER_TROUGH
                waterSource.contains("irrigation", true) -> MapMarkerType.IRRIGATION_POINT
                waterSource.contains("rainwater", true) -> MapMarkerType.RAINWATER_TANK
                else -> MapMarkerType.BOREHOLE
            }

            val existingMarker = mapMarkerDao.getMarkerByLinkedEntity("water", 0L)

            if (existingMarker != null) {
                val currentUsage = existingMarker.metadata["waterUsage"]?.toDoubleOrNull() ?: 0.0
                val updated = existingMarker.copy(
                    metadata = existingMarker.metadata + mapOf(
                        "waterUsage" to (currentUsage + litersUsed).toString(),
                        "lastWaterDate" to System.currentTimeMillis().toString()
                    ),
                    updatedAt = System.currentTimeMillis()
                )
                mapMarkerDao.updateMarker(updated)
            }
        }
    }

    /**
     * Create a simple rectangular boundary for a plot
     */
    private fun createPlotBoundary(center: LatLng, areaSquareMeters: Double): List<LatLng> {
        // Calculate side length for a square plot
        val sideLength = kotlin.math.sqrt(areaSquareMeters)
        
        // Convert to degrees (approximate)
        val latDelta = sideLength / 111320.0 / 2
        val lngDelta = sideLength / (111320.0 * kotlin.math.cos(Math.toRadians(center.latitude))) / 2

        return listOf(
            LatLng(center.latitude - latDelta, center.longitude - lngDelta),
            LatLng(center.latitude - latDelta, center.longitude + lngDelta),
            LatLng(center.latitude + latDelta, center.longitude + lngDelta),
            LatLng(center.latitude + latDelta, center.longitude - lngDelta)
        )
    }

    /**
     * Initialize default farm infrastructure markers
     */
    fun initializeDefaultMarkers() {
        scope.launch {
            // Main house
            val mainHousePos = infrastructurePositions["mainHouse"] ?: LatLng(farmLat, farmLng)
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Main House",
                markerType = MapMarkerType.MAIN_HOUSE.name,
                category = MapMarkerType.MAIN_HOUSE.category.name,
                latitude = mainHousePos.latitude,
                longitude = mainHousePos.longitude,
                icon = MapMarkerType.MAIN_HOUSE.icon,
                color = MapMarkerType.MAIN_HOUSE.color,
                description = "Farm main residence"
            ))

            // Borehole
            val boreholePos = waterPositions["borehole"] ?: LatLng(farmLat, farmLng)
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Main Borehole",
                markerType = MapMarkerType.BOREHOLE.name,
                category = MapMarkerType.BOREHOLE.category.name,
                latitude = boreholePos.latitude,
                longitude = boreholePos.longitude,
                icon = MapMarkerType.BOREHOLE.icon,
                color = MapMarkerType.BOREHOLE.color,
                description = "Primary water source"
            ))

            // Goat pen
            val goatPenPos = livestockPositions["goatPen1"] ?: LatLng(farmLat, farmLng)
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Goat Pen 1",
                markerType = MapMarkerType.GOAT_PEN.name,
                category = MapMarkerType.GOAT_PEN.category.name,
                latitude = goatPenPos.latitude,
                longitude = goatPenPos.longitude,
                icon = MapMarkerType.GOAT_PEN.icon,
                color = MapMarkerType.GOAT_PEN.color,
                description = "Primary goat pen"
            ))

            // Feed storage
            val feedPos = infrastructurePositions["feedStorage"] ?: LatLng(farmLat, farmLng)
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Feed Storage",
                markerType = MapMarkerType.FEED_STORAGE.name,
                category = MapMarkerType.FEED_STORAGE.category.name,
                latitude = feedPos.latitude,
                longitude = feedPos.longitude,
                icon = MapMarkerType.FEED_STORAGE.icon,
                color = MapMarkerType.FEED_STORAGE.color,
                description = "Feed and grain storage"
            ))

            // Cheese room
            val cheesePos = infrastructurePositions["cheeseRoom"] ?: LatLng(farmLat, farmLng)
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Cheese Room",
                markerType = MapMarkerType.CHEESE_ROOM.name,
                category = MapMarkerType.CHEESE_ROOM.category.name,
                latitude = cheesePos.latitude,
                longitude = cheesePos.longitude,
                icon = MapMarkerType.CHEESE_ROOM.icon,
                color = MapMarkerType.CHEESE_ROOM.color,
                description = "Cheese production facility"
            ))

            // Compost pit
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Compost Pit A",
                markerType = MapMarkerType.COMPOST_PIT_A.name,
                category = MapMarkerType.COMPOST_PIT_A.category.name,
                latitude = -5.1530,
                longitude = 38.4830,
                icon = MapMarkerType.COMPOST_PIT_A.icon,
                color = MapMarkerType.COMPOST_PIT_A.color,
                description = "Organic waste composting"
            ))

            // Main gate
            mapMarkerDao.insertMarker(MapMarkerEntity(
                name = "Main Gate",
                markerType = MapMarkerType.GATE_ENTRANCE.name,
                category = MapMarkerType.GATE_ENTRANCE.category.name,
                latitude = -5.1495,
                longitude = 38.4795,
                icon = MapMarkerType.GATE_ENTRANCE.icon,
                color = MapMarkerType.GATE_ENTRANCE.color,
                description = "Farm entrance"
            ))
        }
    }
}