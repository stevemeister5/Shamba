package com.shambasmart.presentation.crops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.data.local.entity.Plot
import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPlantingScreen(
    viewModel: CropsViewModel = hiltViewModel()
) {
    val plots by viewModel.allPlots.collectAsStateWithLifecycle()
    var selectedPlot by remember { mutableStateOf<Plot?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var cropRecords by remember { mutableStateOf<List<CropPlanting>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Crop Planting",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Plot Selection
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = {}
        ) {
            OutlinedTextField(
                value = selectedPlot?.let { "${it.name} - ${it.sizeAcres} acres" } ?: "Select Plot",
                onValueChange = {},
                readOnly = true,
                label = { Text("Plot") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Crop Records List
        if (cropRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Grass,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No crop planting records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Crop Planting")
                    }
                }
            }
        } else {
            LazyColumn {
                items(cropRecords) { record ->
                    CropPlantingCard(record = record)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Floating Action Button
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Crop Planting")
        }
    }

    // Add Crop Planting Dialog
    if (showAddDialog) {
        AddCropPlantingDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                // TODO: Save to database
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CropPlantingCard(record: CropPlanting) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.cropType,
                    style = MaterialTheme.typography.titleMedium
                )
                Badge(
                    containerColor = when (record.status) {
                        "growing" -> MaterialTheme.colorScheme.primary
                        "harvested" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(record.status)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            record.variety?.let {
                Text(text = "Variety: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Planted: ${record.plantingDate}", style = MaterialTheme.typography.bodyMedium)
            record.expectedHarvestDate?.let {
                Text(
                    text = "Expected harvest: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            record.seedSource?.let {
                Text(text = "Source: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AddCropPlantingDialog(
    onDismiss: () -> Unit,
    onAdd: (CropPlanting) -> Unit
) {
    var cropType by remember { mutableStateOf("") }
    var variety by remember { mutableStateOf("") }
    var plantingDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var seedSource by remember { mutableStateOf("") }
    var seedQuantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Crop Planting") },
        text = {
            Column {
                OutlinedTextField(
                    value = cropType,
                    onValueChange = { cropType = it },
                    label = { Text("Crop Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variety") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = plantingDate,
                    onValueChange = { plantingDate = it },
                    label = { Text("Planting Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = seedSource,
                    onValueChange = { seedSource = it },
                    label = { Text("Seed Source") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = seedQuantity,
                    onValueChange = { seedQuantity = it },
                    label = { Text("Seed Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        CropPlanting(
                            plotId = 0, // TODO: Get from selected plot
                            cropType = cropType,
                            variety = variety.ifBlank { null },
                            plantingDate = LocalDate.parse(plantingDate),
                            seedSource = seedSource.ifBlank { null },
                            seedQuantity = seedQuantity.toDoubleOrNull()
                        )
                    )
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}