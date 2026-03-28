package com.shambasmart.presentation.infrastructure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

data class InfrastructureMarker(
    val id: String,
    val name: String,
    val type: InfrastructureType,
    val latitude: Double,
    val longitude: Double,
    val condition: InfrastructureCondition = InfrastructureCondition.GOOD
)

enum class InfrastructureType {
    ANIMAL_SHELTER, WATER_POINT, STORAGE, CHEESE_ROOM, COMPOST_PIT, PLOT
}

enum class InfrastructureCondition {
    EXCELLENT, GOOD, FAIR, POOR, NEEDS_REPAIR
}

data class PlotBoundary(
    val id: Long,
    val name: String,
    val points: List<Offset>,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfrastructureScreen() {
    var selectedMarker by remember { mutableStateOf<InfrastructureMarker?>(null) }
    
    // Sample infrastructure data for 16-acre farm
    val infrastructureMarkers = remember {
        listOf(
            InfrastructureMarker("shelter1", "Goat Shelter A", InfrastructureType.ANIMAL_SHELTER, -5.152, 38.485, InfrastructureCondition.GOOD),
            InfrastructureMarker("shelter2", "Sheep Shelter B", InfrastructureType.ANIMAL_SHELTER, -5.153, 38.486, InfrastructureCondition.EXCELLENT),
            InfrastructureMarker("water1", "Main Water Trough", InfrastructureType.WATER_POINT, -5.151, 38.484, InfrastructureCondition.GOOD),
            InfrastructureMarker("water2", "Secondary Trough", InfrastructureType.WATER_POINT, -5.154, 38.487, InfrastructureCondition.FAIR),
            InfrastructureMarker("storage1", "Feed Storage", InfrastructureType.STORAGE, -5.150, 38.483, InfrastructureCondition.GOOD),
            InfrastructureMarker("storage2", "Equipment Shed", InfrastructureType.STORAGE, -5.149, 38.482, InfrastructureCondition.EXCELLENT),
            InfrastructureMarker("cheese", "Cheese Room", InfrastructureType.CHEESE_ROOM, -5.148, 38.481, InfrastructureCondition.EXCELLENT),
            InfrastructureMarker("compost1", "Compost Pit A", InfrastructureType.COMPOST_PIT, -5.155, 38.488, InfrastructureCondition.GOOD),
            InfrastructureMarker("compost2", "Compost Pit B", InfrastructureType.COMPOST_PIT, -5.156, 38.489, InfrastructureCondition.FAIR)
        )
    }
    
    // Sample plot boundaries (simplified rectangles for demo)
    val plotBoundaries = remember {
        listOf(
            PlotBoundary(1, "Plot 1 - Napier", listOf(Offset(0.1f, 0.1f), Offset(0.3f, 0.1f), Offset(0.3f, 0.3f), Offset(0.1f, 0.3f)), Color(0xFF4CAF50)),
            PlotBoundary(2, "Plot 2 - Maize", listOf(Offset(0.35f, 0.1f), Offset(0.55f, 0.1f), Offset(0.55f, 0.3f), Offset(0.35f, 0.3f)), Color(0xFFFFC107)),
            PlotBoundary(3, "Plot 3 - Beans", listOf(Offset(0.6f, 0.1f), Offset(0.8f, 0.1f), Offset(0.8f, 0.3f), Offset(0.6f, 0.3f)), Color(0xFF2196F3)),
            PlotBoundary(4, "Plot 4 - Tomatoes", listOf(Offset(0.1f, 0.35f), Offset(0.3f, 0.35f), Offset(0.3f, 0.55f), Offset(0.1f, 0.55f)), Color(0xFFF44336)),
            PlotBoundary(5, "Plot 5 - Kale", listOf(Offset(0.35f, 0.35f), Offset(0.55f, 0.35f), Offset(0.55f, 0.55f), Offset(0.35f, 0.55f)), Color(0xFF9C27B0))
        )
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Farm Infrastructure",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "16-Acre Farm • Korogwe, Tanga",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Farm Map with GPS coordinates
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Canvas for farm map visualization
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawFarmMap(this, plotBoundaries, infrastructureMarkers)
                }
                
                // Legend overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Legend",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LegendItem("🏠", "Shelter", Color(0xFF4CAF50))
                    LegendItem("💧", "Water", Color(0xFF2196F3))
                    LegendItem("📦", "Storage", Color(0xFFFF9800))
                    LegendItem("🧀", "Cheese", Color(0xFFFFEB3B))
                    LegendItem("🌱", "Compost", Color(0xFF795548))
                }
                
                // Farm center marker
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Farm Center",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    tint = Color.Red
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Infrastructure List
        LazyColumn {
            item {
                Text(
                    text = "Infrastructure Points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(infrastructureMarkers) { marker ->
                InfrastructureMarkerCard(
                    marker = marker,
                    onClick = { selectedMarker = marker }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Plot Boundaries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(plotBoundaries) { plot ->
                PlotBoundaryCard(plot = plot)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    
    // Selected marker detail dialog
    selectedMarker?.let { marker ->
        InfrastructureDetailDialog(
            marker = marker,
            onDismiss = { selectedMarker = null }
        )
    }
}

@Composable
private fun LegendItem(emoji: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun InfrastructureMarkerCard(
    marker: InfrastructureMarker,
    onClick: () -> Unit
) {
    val conditionColor = when (marker.condition) {
        InfrastructureCondition.EXCELLENT -> Color(0xFF4CAF50)
        InfrastructureCondition.GOOD -> Color(0xFF8BC34A)
        InfrastructureCondition.FAIR -> Color(0xFFFFC107)
        InfrastructureCondition.POOR -> Color(0xFFFF9800)
        InfrastructureCondition.NEEDS_REPAIR -> Color(0xFFF44336)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getInfrastructureIcon(marker.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = marker.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Lat: ${marker.latitude}, Lng: ${marker.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Condition Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(conditionColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = marker.condition.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = conditionColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PlotBoundaryCard(plot: PlotBoundary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(plot.color.copy(alpha = 0.3f))
                    .border(2.dp, plot.color, RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${plot.points.size} boundary points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfrastructureDetailDialog(
    marker: InfrastructureMarker,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(marker.name)
        },
        text = {
            Column {
                DetailRow("Type", marker.type.name.replace("_", " "))
                DetailRow("Condition", marker.condition.name)
                DetailRow("Latitude", "%.6f".format(marker.latitude))
                DetailRow("Longitude", "%.6f".format(marker.longitude))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getInfrastructureIcon(type: InfrastructureType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        InfrastructureType.ANIMAL_SHELTER -> Icons.Default.Home
        InfrastructureType.WATER_POINT -> Icons.Default.Water
        InfrastructureType.STORAGE -> Icons.Default.Inventory
        InfrastructureType.CHEESE_ROOM -> Icons.Default.Kitchen
        InfrastructureType.COMPOST_PIT -> Icons.Default.Grass
        InfrastructureType.PLOT -> Icons.Default.Agriculture
    }
}

private fun drawFarmMap(
    drawScope: DrawScope,
    plots: List<PlotBoundary>,
    markers: List<InfrastructureMarker>
) {
    with(drawScope) {
        // Draw farm boundary
        drawRect(
            color = Color(0xFFE8F5E9),
            topLeft = Offset(0f, 0f),
            size = size
        )
        
        // Draw plot boundaries
        plots.forEach { plot ->
            val path = Path()
            plot.points.forEachIndexed { index, point ->
                val x = point.x * size.width
                val y = point.y * size.height
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            
            drawPath(
                path = path,
                color = plot.color.copy(alpha = 0.3f),
                style = Fill
            )
            
            drawPath(
                path = path,
                color = plot.color,
                style = Stroke(width = 2f)
            )
        }
        
        // Draw infrastructure markers
        markers.forEach { marker ->
            val x = ((marker.longitude - 38.480) / 0.010 * size.width).toFloat()
            val y = ((-marker.latitude + 5.160) / 0.010 * size.height).toFloat()
            
            drawCircle(
                color = Color.Red,
                radius = 8f,
                center = Offset(x, y)
            )
        }
    }
}
