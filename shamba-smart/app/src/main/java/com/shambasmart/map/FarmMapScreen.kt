package com.shambasmart.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.shambasmart.map.drawing.PolygonDrawingOverlay
import com.shambasmart.map.heatmap.HeatmapRenderer
import com.shambasmart.map.heatmap.PestHeatmapOverlay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: FarmMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddMarkerDialog by remember { mutableStateOf(false) }
    var showLayerPanel by remember { mutableStateOf(false) }
    var showHeatmapPanel by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    // Messages
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Farm Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.markers.size} markers • ${uiState.visibleLayers.size} layers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // GPS toggle
                    IconButton(onClick = { viewModel.toggleGpsTracking() }) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            tint = if (uiState.isGpsTracking) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Heatmap toggle
                    IconButton(onClick = { showHeatmapPanel = !showHeatmapPanel }) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Heatmaps",
                            tint = if (uiState.activeHeatmap != null) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Layer toggle
                    IconButton(onClick = { showLayerPanel = !showLayerPanel }) {
                        Icon(Icons.Default.Visibility, contentDescription = "Layers")
                    }
                    // Drawing tools
                    IconButton(onClick = { 
                        viewModel.setDrawingMode(!uiState.isDrawingMode, DrawingTool.POLYGON) 
                    }) {
                        Icon(
                            Icons.Default.Draw,
                            contentDescription = "Draw",
                            tint = if (uiState.isDrawingMode) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Add marker FAB
                FloatingActionButton(
                    onClick = { showAddMarkerDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Marker")
                }
                
                // Center on farm FAB
                SmallFloatingActionButton(
                    onClick = { 
                        mapView?.controller?.animateTo(
                            GeoPoint(farmCenterLat, farmCenterLng)
                        )
                        mapView?.controller?.setZoom(15.0)
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center on Farm")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // OSMDroid Map
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        // Configure map
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        
                        // Set farm center
                        val farmCenter = GeoPoint(farmCenterLat, farmCenterLng)
                        controller.setCenter(farmCenter)
                        
                        // Enable GPS location overlay
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)
                        
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { map ->
                    // Clear existing overlays except location
                    val locationOverlays = map.overlays.filterIsInstance<MyLocationNewOverlay>()
                    map.overlays.clear()
                    map.overlays.addAll(locationOverlays)
                    
                    // Add polygon overlays for drawing
                    if (uiState.isDrawingMode && uiState.drawingTool == DrawingTool.POLYGON) {
                        // Polygon drawing will be handled by the overlay
                    }
                    
                    // Add marker overlays
                    uiState.markers.forEach { marker ->
                        if (marker.category in uiState.visibleLayers) {
                            val markerOverlay = Marker(map).apply {
                                position = GeoPoint(marker.latitude, marker.longitude)
                                title = marker.name
                                snippet = marker.description ?: marker.markerType
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            map.overlays.add(markerOverlay)
                            
                            // Add polygon if marker has boundary points
                            marker.boundaryPoints?.let { points ->
                                if (points.size >= 3) {
                                    val polygon = Polygon().apply {
                                        points.forEach { point ->
                                            addPoint(GeoPoint(point.latitude, point.longitude))
                                        }
                                        fillPaint.color = android.graphics.Color.parseColor(marker.color + "40")
                                        outlinePaint.color = android.graphics.Color.parseColor(marker.color)
                                        outlinePaint.strokeWidth = 3f
                                    }
                                    map.overlays.add(polygon)
                                }
                            }
                        }
                    }
                    
                    // Apply heatmap if active
                    if (uiState.activeHeatmap != null) {
                        val heatmapMarkers = uiState.markers.filter { it.category in uiState.visibleLayers }
                        HeatmapRenderer.applyHeatmapOverlay(
                            map = map,
                            heatmapType = uiState.activeHeatmap!!,
                            markers = heatmapMarkers
                        )
                    }
                    
                    map.invalidate()
                }
            )
            
            // Messages overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF44336).copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.errorMessage ?: "", color = Color.White)
                        }
                    }
                }
                
                if (uiState.successMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.successMessage ?: "", color = Color.White)
                        }
                    }
                }
            }
            
            // Polygon drawing overlay
            if (uiState.isDrawingMode && uiState.drawingTool == DrawingTool.POLYGON) {
                PolygonDrawingOverlay(
                    points = uiState.drawingPoints,
                    onPointAdded = { point ->
                        // Add point to drawing state
                        viewModel.addDrawingPoint(point)
                    },
                    onPointRemoved = {
                        // Remove last point from drawing state
                        viewModel.removeDrawingPoint()
                    },
                    onPolygonComplete = { points ->
                        // Create polygon marker from points
                        val centerLat = points.map { it.latitude }.average()
                        val centerLng = points.map { it.longitude }.average()
                        viewModel.addMarker(
                            name = "Polygon Boundary",
                            markerType = MapMarkerType.MEETING_POINT,
                            latitude = centerLat,
                            longitude = centerLng,
                            description = "Polygon with ${points.size} points"
                        )
                        viewModel.clearDrawingPoints()
                        viewModel.setDrawingMode(false)
                    },
                    onCancel = {
                        viewModel.clearDrawingPoints()
                        viewModel.setDrawingMode(false)
                    },
                    isActive = true
                )
            }
            
            // Drawing mode indicator
            if (uiState.isDrawingMode) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Draw,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Drawing Mode: ${uiState.drawingTool?.name ?: "POLYGON"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(
                            onClick = { viewModel.setDrawingMode(false) }
                        ) {
                            Text("Exit")
                        }
                    }
                }
            }
            
            // GPS tracking indicator
            if (uiState.isGpsTracking) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPS Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Layer panel
            if (showLayerPanel) {
                LayerPanel(
                    visibleLayers = uiState.visibleLayers,
                    onToggleLayer = { viewModel.toggleLayer(it) },
                    onDismiss = { showLayerPanel = false }
                )
            }
            
            // Heatmap panel
            if (showHeatmapPanel) {
                HeatmapPanel(
                    activeHeatmap = uiState.activeHeatmap,
                    onSelectHeatmap = { viewModel.setHeatmap(it) },
                    onDismiss = { showHeatmapPanel = false }
                )
            }
            
            // Marker info card
            uiState.selectedMarker?.let { marker ->
                MarkerInfoCard(
                    marker = marker,
                    onEdit = { /* Navigate to edit */ },
                    onDelete = { viewModel.deleteMarker(marker) },
                    onDismiss = { viewModel.selectMarker(null) }
                )
            }
        }
    }
    
    // Add marker dialog
    if (showAddMarkerDialog) {
        AddMarkerDialog(
            onDismiss = { showAddMarkerDialog = false },
            onAdd = { name, type, lat, lng, description ->
                viewModel.addMarker(
                    name = name,
                    markerType = type,
                    latitude = lat,
                    longitude = lng,
                    description = description
                )
                showAddMarkerDialog = false
            }
        )
    }
    
    // Link marker to entity dialog
    if (uiState.showLinkDialog) {
        LinkMarkerDialog(
            marker = uiState.markerToLink!!,
            onDismiss = { viewModel.dismissLinkDialog() },
            onLink = { entityType, entityId ->
                viewModel.linkMarkerToEntity(uiState.markerToLink!!, entityType, entityId)
            }
        )
    }
}

// Farm center coordinates (Korogwe, Tanga)
private const val farmCenterLat = -5.15
private const val farmCenterLng = 38.48

@Composable
private fun LayerPanel(
    visibleLayers: Set<String>,
    onToggleLayer: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Map Layers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val layers = listOf(
                "plots" to "🌾 Plots",
                "infrastructure" to "🏠 Infrastructure",
                "livestock" to "🐐 Livestock",
                "water" to "💧 Water",
                "waste" to "♻️ Waste",
                "safety" to "🛡️ Safety",
                "custom" to "📍 Custom"
            )
            
            layers.forEach { (id, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name)
                    Switch(
                        checked = id in visibleLayers,
                        onCheckedChange = { onToggleLayer(id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapPanel(
    activeHeatmap: HeatmapType?,
    onSelectHeatmap: (HeatmapType?) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Heatmap Overlays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // None option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🚫 None")
                RadioButton(
                    selected = activeHeatmap == null,
                    onClick = { onSelectHeatmap(null) }
                )
            }
            
            HeatmapType.values().forEach { heatmap ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = heatmap.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = heatmap.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RadioButton(
                        selected = activeHeatmap == heatmap,
                        onClick = { onSelectHeatmap(heatmap) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkerInfoCard(
    marker: com.shambasmart.data.local.entity.MapMarkerEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = marker.icon, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = marker.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = marker.markerType.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            marker.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lat: ${String.format("%.6f", marker.latitude)}, Lng: ${String.format("%.6f", marker.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            marker.areaSquareMeters?.let { area ->
                Text(
                    text = "Area: ${String.format("%.1f", area)} m² (${String.format("%.2f", area / 4046.86)} acres)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Show linked entity if any
            marker.linkedEntityType?.let { entityType ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Linked to: $entityType #${marker.linkedEntityId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun LinkMarkerDialog(
    marker: com.shambasmart.data.local.entity.MapMarkerEntity,
    onDismiss: () -> Unit,
    onLink: (entityType: String, entityId: Long) -> Unit
) {
    var selectedEntityType by remember { mutableStateOf("crop") }
    var entityId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Marker to Entity") },
        text = {
            Column {
                Text(
                    text = "Link '${marker.name}' to a farm entity",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Entity type selector
                Text("Entity Type:", style = MaterialTheme.typography.labelMedium)
                val entityTypes = listOf("crop", "animal", "feed", "cheese", "harvest")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(entityTypes) { type ->
                        FilterChip(
                            selected = selectedEntityType == type,
                            onClick = { selectedEntityType = type },
                            label = { Text(type.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = entityId,
                    onValueChange = { entityId = it },
                    label = { Text("Entity ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    entityId.toLongOrNull()?.let { id ->
                        onLink(selectedEntityType, id)
                    }
                },
                enabled = entityId.isNotBlank()
            ) {
                Text("Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddMarkerDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, type: MapMarkerType, lat: Double, lng: Double, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MapMarkerType.MEETING_POINT) }
    var latitude by remember { mutableStateOf("-5.15") }
    var longitude by remember { mutableStateOf("38.48") }
    var description by remember { mutableStateOf("") }
    var showTypeSelector by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Map Marker") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Marker Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Marker type selector
                OutlinedButton(
                    onClick = { showTypeSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "${selectedType.icon} ${selectedType.displayName}")
                }
                
                if (showTypeSelector) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(MapMarkerType.values().toList()) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    showTypeSelector = false
                                },
                                label = { Text("${type.icon} ${type.displayName}") }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        name,
                        selectedType,
                        latitude.toDoubleOrNull() ?: -5.15,
                        longitude.toDoubleOrNull() ?: 38.48,
                        description.ifBlank { null }
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add Marker")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}