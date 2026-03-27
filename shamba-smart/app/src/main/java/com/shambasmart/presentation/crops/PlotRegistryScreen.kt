package com.shambasmart.presentation.crops

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shambasmart.data.local.entity.Plot
import com.shambasmart.presentation.common.theme.*
import com.shambasmart.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotRegistryScreen(
    navController: NavController = rememberNavController(),
    viewModel: CropsViewModel = hiltViewModel()
) {
    val plots by viewModel.allPlots.collectAsStateWithLifecycle()
    val totalAcres by viewModel.totalAcres.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPlot by remember { mutableStateOf<Plot?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            CropsHeader(
                plotCount = plots.size,
                totalAcres = totalAcres ?: 0.0,
                onAnalyticsClick = { navController.navigate(Screen.PlotAnalytics.route) },
                onAddClick = { showAddDialog = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Farm Map (Hero Element)
            FarmMapSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Plot Grid
            PlotGridSection(
                plots = plots,
                onPlotClick = { selectedPlot = it },
                onDeletePlot = { viewModel.deletePlot(it) }
            )
        }
    }

    // Add Plot Dialog
    if (showAddDialog) {
        AddPlotDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { plot ->
                viewModel.addPlot(plot)
                showAddDialog = false
            }
        )
    }

    // Edit Plot Dialog
    selectedPlot?.let { plot ->
        EditPlotDialog(
            plot = plot,
            onDismiss = { selectedPlot = null },
            onUpdate = { updatedPlot ->
                viewModel.updatePlot(updatedPlot)
                selectedPlot = null
            }
        )
    }
}

@Composable
private fun CropsHeader(
    plotCount: Int,
    totalAcres: Double,
    onAnalyticsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Crop Management",
                style = MaterialTheme.typography.headlineLarge,
                color = Neutral950
            )
            Text(
                text = "$plotCount plots • ${String.format("%.1f", totalAcres)} acres total",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Analytics Button
            OutlinedButton(
                onClick = onAnalyticsClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Neutral800
                ),
                border = BorderStroke(1.dp, Neutral300),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            
            // Add Plot Button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Plot",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun FarmMapSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Green950.copy(alpha = 0.2f),
                            SurfaceRaised
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Neutral600
                        )
                        Text(
                            text = "FARM MAP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                    }
                    
                    // Map Style Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MapStyleChip("Satellite", true)
                        MapStyleChip("Terrain", false)
                        MapStyleChip("Hybrid", false)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Map Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Green900.copy(alpha = 0.3f),
                                    Green950.copy(alpha = 0.5f),
                                    SurfaceBase
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Terrain,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Neutral400
                        )
                        Text(
                            text = "Interactive Farm Map",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "Tap to view plot boundaries and crop status",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapStyleChip(text: String, isSelected: Boolean) {
    val backgroundColor = if (isSelected) Green800.copy(alpha = 0.3f) else Color.Transparent
    val textColor = if (isSelected) Green300 else Neutral600
    
    Surface(
        color = backgroundColor,
        border = BorderStroke(1.dp, if (isSelected) Green700 else Neutral200),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PlotGridSection(
    plots: List<Plot>,
    onPlotClick: (Plot) -> Unit,
    onDeletePlot: (Plot) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PLOT GRID",
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600
            )
            
            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem("Grass/Silage", Green500.copy(alpha = 0.6f))
                LegendItem("Grain Crops", Amber400.copy(alpha = 0.6f))
                LegendItem("Vegetables", Teal400.copy(alpha = 0.6f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (plots.isEmpty()) {
            EmptyPlotsState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(plots) { plot ->
                    PlotCard(
                        plot = plot,
                        onClick = { onPlotClick(plot) },
                        onDelete = { onDeletePlot(plot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600
        )
    }
}

@Composable
private fun PlotCard(
    plot: Plot,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cropColor = when (plot.currentUse?.lowercase()) {
        "grass", "silage", "pasture" -> Green500
        "maize", "wheat", "grain", "barley" -> Amber400
        "vegetables", "tomatoes", "onions", "cabbage" -> Teal400
        else -> Neutral400
    }
    
    val cropEmoji = when (plot.currentUse?.lowercase()) {
        "grass", "silage", "pasture" -> "🌿"
        "maize", "wheat", "grain", "barley" -> "🌾"
        "vegetables", "tomatoes", "onions", "cabbage" -> "🥬"
        else -> "🌱"
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Plot Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More",
                        modifier = Modifier.size(16.dp),
                        tint = Neutral600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Crop Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = cropEmoji,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = plot.currentUse ?: "No crop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Acreage
            Text(
                text = "${String.format("%.1f", plot.sizeAcres)} acres",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = GeistMonoFamily
                ),
                color = Neutral600
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Growth Stage Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Growth Stage",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "65%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral800
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(SurfaceSunken, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(cropColor.copy(alpha = 0.6f), cropColor)
                                ),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Last Activity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Neutral400
                )
                Text(
                    text = "Last activity: 2 days ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral400
                )
            }
        }
    }
}

@Composable
private fun EmptyPlotsState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Landscape,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Neutral300
                )
                Text(
                    text = "Register your plots to start tracking crops",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Add your first plot to begin monitoring growth stages and crop health",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral400,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AddPlotDialog(
    onDismiss: () -> Unit,
    onAdd: (Plot) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sizeAcres by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var currentUse by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Plot",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plot Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = sizeAcres,
                    onValueChange = { sizeAcres = it },
                    label = { Text("Size (acres)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Crop Type Selector
                Column {
                    Text(
                        text = "Current Crop",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Grass", "Maize", "Vegetables", "Other").forEach { crop ->
                            FilterChip(
                                selected = currentUse == crop,
                                onClick = { currentUse = crop },
                                label = {
                                    Text(
                                        text = crop,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceSunken,
                                    selectedContainerColor = Green800.copy(alpha = 0.3f),
                                    labelColor = Neutral800,
                                    selectedLabelColor = Green300
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        Plot(
                            name = name,
                            sizeAcres = sizeAcres.toDoubleOrNull() ?: 0.0,
                            soilType = soilType.ifBlank { null },
                            currentUse = currentUse.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Plot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Neutral600
                )
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun EditPlotDialog(
    plot: Plot,
    onDismiss: () -> Unit,
    onUpdate: (Plot) -> Unit
) {
    var name by remember { mutableStateOf(plot.name) }
    var sizeAcres by remember { mutableStateOf(plot.sizeAcres.toString()) }
    var soilType by remember { mutableStateOf(plot.soilType ?: "") }
    var currentUse by remember { mutableStateOf(plot.currentUse ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Plot",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plot Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = sizeAcres,
                    onValueChange = { sizeAcres = it },
                    label = { Text("Size (acres)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Crop Type Selector
                Column {
                    Text(
                        text = "Current Crop",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Grass", "Maize", "Vegetables", "Other").forEach { crop ->
                            FilterChip(
                                selected = currentUse == crop,
                                onClick = { currentUse = crop },
                                label = {
                                    Text(
                                        text = crop,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceSunken,
                                    selectedContainerColor = Green800.copy(alpha = 0.3f),
                                    labelColor = Neutral800,
                                    selectedLabelColor = Green300
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        plot.copy(
                            name = name,
                            sizeAcres = sizeAcres.toDoubleOrNull() ?: plot.sizeAcres,
                            soilType = soilType.ifBlank { null },
                            currentUse = currentUse.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Neutral600
                )
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}