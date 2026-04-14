package com.shambasmart.presentation.crops

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Plot
import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.data.local.entity.HarvestRecord
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsScreen(
    onNavigateToCropPlanting: () -> Unit = {},
    viewModel: CropsViewModel = hiltViewModel()
) {
    val plots by viewModel.allPlots.collectAsStateWithLifecycle()
    val plantings by viewModel.allCropPlantings.collectAsStateWithLifecycle()
    val harvests by viewModel.harvests.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Plots") }
    var showAddPlot by remember { mutableStateOf(false) }
    var showAddPlanting by remember { mutableStateOf(false) }
    var showAddHarvest by remember { mutableStateOf(false) }
    var showInputLog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
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
                    text = "Track plots, plantings, inputs, and harvests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddPlot = true },
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Plot")
                }
                
                Button(
                    onClick = { showInputLog = true },
                    colors = ButtonDefaults.buttonColors(
                    containerColor = Teal500,
                    contentColor = Teal100
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Input")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Plots", "Plantings", "Harvests").forEach { tab ->
                TabButton(
                    text = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tab Content
        when (selectedTab) {
            "Plots" -> PlotsTab(
                plots = plots,
                onDelete = { viewModel.deletePlot(it) }
            )
            "Plantings" -> PlantingsTab(
                plantings = plantings,
                plots = plots,
                onAddPlanting = { showAddPlanting = true },
                onDelete = { viewModel.deleteCropPlanting(it) }
            )
            "Harvests" -> HarvestsTab(
                harvests = harvests,
                plantings = plantings,
                onAddHarvest = { showAddHarvest = true },
                onDelete = { viewModel.deleteHarvest(it) }
            )
        }
    }
    
    // Dialogs
    if (showAddPlot) {
        AddPlotDialog(
            onDismiss = { showAddPlot = false },
            onSave = { plot ->
                viewModel.addPlot(plot)
                showAddPlot = false
            }
        )
    }
    
    if (showAddPlanting) {
        AddPlantingDialog(
            plots = plots,
            onDismiss = { showAddPlanting = false },
            onSave = { planting ->
                viewModel.addCropPlanting(planting)
                showAddPlanting = false
            }
        )
    }
    
    if (showAddHarvest) {
        AddHarvestDialog(
            plantings = plantings,
            onDismiss = { showAddHarvest = false },
            onSave = { harvest ->
                viewModel.addHarvest(harvest)
                showAddHarvest = false
            }
        )
    }
    
    if (showInputLog) {
        CropInputDialog(
            plantings = plantings,
            onDismiss = { showInputLog = false },
            onSave = { input ->
                // CropInput entity doesn't exist - show message
                showInputLog = false
            }
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Green800.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) Green300 else Neutral600
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PlotsTab(
    plots: List<Plot>,
    onDelete: (Plot) -> Unit
) {
    if (plots.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.Landscape,
            title = "No Plots Registered",
            description = "Add plots to start tracking your crop areas."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plots) { plot ->
                PlotCard(plot = plot, onDelete = { onDelete(plot) })
            }
        }
    }
}

@Composable
private fun PlotCard(plot: Plot, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plot.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = "Plot #${plot.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = Neutral600
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Area",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "${String.format("%.2f", plot.sizeAcres)} acres",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Soil Type",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = plot.soilType ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Current Use",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = plot.currentUse ?: "Fallow",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral950
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantingsTab(
    plantings: List<CropPlanting>,
    plots: List<Plot>,
    onAddPlanting: () -> Unit,
    onDelete: (CropPlanting) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CROP PLANTINGS",
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600
            )
            
            Button(
                onClick = onAddPlanting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Planting")
            }
        }
        
        if (plantings.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Grass,
                title = "No Plantings",
                description = "Add plantings to track your crops."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plantings) { planting ->
                    PlantingCard(
                        planting = planting,
                        plot = plots.find { it.id == planting.plotId },
                        onDelete = { onDelete(planting) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantingCard(
    planting: CropPlanting,
    plot: Plot?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = planting.cropType,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = plot?.name ?: "Unknown Plot",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = planting.status)
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Neutral600
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Planted",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = planting.plantingDate.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Expected Harvest",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = planting.expectedHarvestDate?.toString() ?: "TBD",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Planted Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = planting.plantingDate.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
            }
        }
    }
}

@Composable
private fun HarvestsTab(
    harvests: List<HarvestRecord>,
    plantings: List<CropPlanting>,
    onAddHarvest: () -> Unit,
    onDelete: (HarvestRecord) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HARVEST RECORDS",
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600
            )
            
            Button(
                onClick = onAddHarvest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Harvest")
            }
        }
        
        if (harvests.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Agriculture,
                title = "No Harvests Recorded",
                description = "Record harvests to track your crop yields."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(harvests) { harvest ->
                    HarvestCard(
                        harvest = harvest,
                        planting = plantings.find { it.id == harvest.cropPlantingId },
                        onDelete = { onDelete(harvest) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HarvestCard(
    harvest: HarvestRecord,
    planting: CropPlanting?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = planting?.cropType ?: "Unknown Crop",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = harvest.harvestDate.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = Neutral600
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "${String.format("%.1f", harvest.quantityKg)} kg",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Grade",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = harvest.qualityGrade ?: "A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Destination",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = harvest.destination ?: "Storage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral950
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "growing" -> Pair(Green800.copy(alpha = 0.3f), Green300)
        "harvested" -> Pair(Amber600.copy(alpha = 0.3f), Amber300)
        "failed" -> Pair(Red600.copy(alpha = 0.3f), Red300)
        else -> Pair(Neutral800.copy(alpha = 0.3f), Neutral300)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral800,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AddPlotDialog(
    onDismiss: () -> Unit,
    onSave: (Plot) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var areaAcres by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("Loam") }
    var irrigationType by remember { mutableStateOf("Rainfed") }

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
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = areaAcres,
                    onValueChange = { areaAcres = it },
                    label = { Text("Area (acres)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Text(
                    text = "Soil Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Loam", "Clay", "Sandy", "Silt", "Peat")) { soil ->
                        FilterChip(
                            selected = soilType == soil,
                            onClick = { soilType = soil },
                            label = { Text(soil) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                Text(
                    text = "Irrigation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Rainfed", "Drip", "Sprinkler", "Flood")) { irrigation ->
                        FilterChip(
                            selected = irrigationType == irrigation,
                            onClick = { irrigationType = irrigation },
                            label = { Text(irrigation) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Plot(
                            name = name,
                            sizeAcres = areaAcres.toDoubleOrNull() ?: 0.0,
                            soilType = soilType,
                            currentUse = irrigationType
                        )
                    )
                },
                enabled = name.isNotBlank() && areaAcres.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddPlantingDialog(
    plots: List<Plot>,
    onDismiss: () -> Unit,
    onSave: (CropPlanting) -> Unit
) {
    var cropName by remember { mutableStateOf("") }
    var selectedPlotId by remember { mutableStateOf<Long?>(null) }
    var areaAcres by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Planting",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Crop Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Text(
                    text = "Select Plot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(plots) { plot ->
                        FilterChip(
                            selected = selectedPlotId == plot.id,
                            onClick = { selectedPlotId = plot.id },
                            label = { Text(plot.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = areaAcres,
                    onValueChange = { areaAcres = it },
                    label = { Text("Area (acres)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPlotId != null) {
                        onSave(
                            CropPlanting(
                                plotId = selectedPlotId!!,
                                cropType = cropName,
                                plantingDate = today,
                                status = "growing"
                            )
                        )
                    }
                },
                enabled = cropName.isNotBlank() && selectedPlotId != null,
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddHarvestDialog(
    plantings: List<CropPlanting>,
    onDismiss: () -> Unit,
    onSave: (HarvestRecord) -> Unit
) {
    var selectedPlantingId by remember { mutableStateOf<Long?>(null) }
    var quantityKg by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("A") }
    var destination by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Harvest",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Select Crop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(plantings.filter { it.status == "growing" }) { planting ->
                        FilterChip(
                            selected = selectedPlantingId == planting.id,
                            onClick = { selectedPlantingId = planting.id },
                            label = { Text(planting.cropType) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = quantityKg,
                    onValueChange = { quantityKg = it },
                    label = { Text("Quantity (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Text(
                    text = "Grade",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A", "B", "C").forEach { g ->
                        FilterChip(
                            selected = grade == g,
                            onClick = { grade = g },
                            label = { Text("Grade $g") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    placeholder = { Text("e.g., Market, Storage, Processing") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPlantingId != null) {
                        onSave(
                            HarvestRecord(
                                cropPlantingId = selectedPlantingId!!,
                                harvestDate = today,
                                quantityKg = quantityKg.toDoubleOrNull() ?: 0.0,
                                qualityGrade = grade,
                                destination = destination.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = selectedPlantingId != null && quantityKg.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun CropInputDialog(
    plantings: List<CropPlanting>,
    onDismiss: () -> Unit,
    onSave: (com.shambasmart.data.local.entity.CropInput) -> Unit
) {
    var selectedPlantingId by remember { mutableStateOf<Long?>(null) }
    var inputType by remember { mutableStateOf("Fertilizer") }
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Crop Input",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Select Crop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(plantings.filter { it.status == "growing" }) { planting ->
                        FilterChip(
                            selected = selectedPlantingId == planting.id,
                            onClick = { selectedPlantingId = planting.id },
                            label = { Text(planting.cropType) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                Text(
                    text = "Input Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Fertilizer", "Pesticide", "Herbicide", "Irrigation", "Other")) { type ->
                        FilterChip(
                            selected = inputType == type,
                            onClick = { inputType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(0.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPlantingId != null) {
                        onSave(
                            com.shambasmart.data.local.entity.CropInput(
                                plantingId = selectedPlantingId!!,
                                inputType = inputType,
                                productName = productName,
                                quantity = quantity.toDoubleOrNull() ?: 0.0,
                                unit = unit,
                                cost = cost.toDoubleOrNull(),
                                date = today,
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = selectedPlantingId != null && productName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}