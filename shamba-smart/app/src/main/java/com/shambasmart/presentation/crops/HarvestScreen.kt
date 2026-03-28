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
import com.shambasmart.data.local.entity.HarvestRecord
import com.shambasmart.data.local.entity.Plot
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    viewModel: CropsViewModel = hiltViewModel()
) {
    val cropPlantings by viewModel.allCropPlantings.collectAsStateWithLifecycle()
    var selectedCropPlanting by remember { mutableStateOf<CropPlanting?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Get harvest records for selected crop planting
    val harvestRecords by remember(selectedCropPlanting?.id) {
        derivedStateOf {
            selectedCropPlanting?.id?.let { cropPlantingId ->
                viewModel.getHarvestRecordsByCropPlanting(cropPlantingId)
            } ?: flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Harvest & Silage",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Harvest Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalHarvested = harvestRecords.sumOf { it.quantityKg }
                    HarvestStat("Total Harvested", "${String.format("%.0f", totalHarvested)} kg")
                    HarvestStat("Records", "${harvestRecords.size}")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Crop Planting Selection
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCropPlanting?.let { "${it.cropType} - ${it.variety ?: "No Variety"}" } ?: "Select Crop Planting",
                onValueChange = { _ -> },
                readOnly = true,
                label = { Text("Crop Planting") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                cropPlantings.forEach { cropPlanting ->
                    DropdownMenuItem(
                        text = { Text("${cropPlanting.cropType} - ${cropPlanting.variety ?: "No Variety"}") },
                        onClick = {
                            selectedCropPlanting = cropPlanting
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Harvest Records List
        if (harvestRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Agriculture,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No harvest records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Harvest Record")
                    }
                }
            }
        } else {
            LazyColumn {
                items(harvestRecords) { record ->
                    HarvestRecordCard(record = record)
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
            Icon(Icons.Default.Add, contentDescription = "Add Harvest Record")
        }
    }

    // Add Harvest Dialog
    if (showAddDialog) {
        AddHarvestDialog(
            cropPlantingId = selectedCropPlanting?.id ?: 0L,
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                viewModel.addHarvest(record)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HarvestStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HarvestRecordCard(record: HarvestRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.quantityKg} kg",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = record.harvestDate.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            record.qualityGrade?.let {
                Text(text = "Grade: $it", style = MaterialTheme.typography.bodyMedium)
            }
            record.destination?.let {
                Text(text = "Destination: $it", style = MaterialTheme.typography.bodySmall)
            }
            record.pricePerKg?.let {
                Text(
                    text = "Price: TZS $it/kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun AddHarvestDialog(
    cropPlantingId: Long,
    onDismiss: () -> Unit,
    onAdd: (HarvestRecord) -> Unit
) {
    var harvestDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var quantityKg by remember { mutableStateOf("") }
    var qualityGrade by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var pricePerKg by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Harvest Record") },
        text = {
            Column {
                OutlinedTextField(
                    value = harvestDate,
                    onValueChange = { harvestDate = it },
                    label = { Text("Harvest Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityKg,
                    onValueChange = { quantityKg = it },
                    label = { Text("Quantity (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qualityGrade,
                    onValueChange = { qualityGrade = it },
                    label = { Text("Quality Grade") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pricePerKg,
                    onValueChange = { pricePerKg = it },
                    label = { Text("Price per kg (TZS)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        HarvestRecord(
                            cropPlantingId = cropPlantingId,
                            harvestDate = LocalDate.parse(harvestDate),
                            quantityKg = quantityKg.toDoubleOrNull() ?: 0.0,
                            qualityGrade = qualityGrade.ifBlank { null },
                            destination = destination.ifBlank { null },
                            pricePerKg = pricePerKg.toDoubleOrNull(),
                            notes = notes.ifBlank { null }
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
