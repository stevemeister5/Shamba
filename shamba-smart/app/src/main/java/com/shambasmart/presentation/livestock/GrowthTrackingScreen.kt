package com.shambasmart.presentation.livestock

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
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.WeightEntry
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthTrackingScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Get weight entries for selected animal
    val weightRecords by remember(selectedAnimal?.id) {
        selectedAnimal?.id?.let { animalId ->
            viewModel.getWeightEntriesByAnimal(animalId)
        } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Growth Tracking",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Animal Selection
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedAnimal?.let { "${it.tagId ?: "No Tag"} - ${it.species}" } ?: "Select Animal",
                onValueChange = { _ -> },
                readOnly = true,
                label = { Text("Animal") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                animals.forEach { animal ->
                    DropdownMenuItem(
                        text = { Text("${animal.tagId ?: "No Tag"} - ${animal.species}") },
                        onClick = {
                            selectedAnimal = animal
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Growth Summary Card
        if (weightRecords.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Growth Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val latestWeight = weightRecords.lastOrNull()?.weight
                        val firstWeight = weightRecords.firstOrNull()?.weight
                        val weightGain = if (latestWeight != null && firstWeight != null) {
                            latestWeight - firstWeight
                        } else 0.0

                        GrowthStat("Current", "${latestWeight ?: 0} kg")
                        GrowthStat("Total Gain", "${String.format("%.1f", weightGain)} kg")
                        GrowthStat("Entries", "${weightRecords.size}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Weight Records List
        if (weightRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MonitorWeight,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No weight records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Weight Entry")
                    }
                }
            }
        } else {
            LazyColumn {
                items(weightRecords) { record ->
                    WeightEntryCard(record = record)
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
            Icon(Icons.Default.Add, contentDescription = "Add Weight Entry")
        }
    }

    // Add Weight Entry Dialog
    if (showAddDialog) {
        AddWeightEntryDialog(
            animalId = selectedAnimal?.id ?: 0L,
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                viewModel.addWeightEntry(entry)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GrowthStat(label: String, value: String) {
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
private fun WeightEntryCard(record: WeightEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.weight} kg",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = record.date.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            record.notes?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AddWeightEntryDialog(
    animalId: Long,
    onDismiss: () -> Unit,
    onAdd: (WeightEntry) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        WeightEntry(
                            animalId = animalId,
                            date = LocalDate.parse(date),
                            weight = weight.toDoubleOrNull() ?: 0.0,
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
