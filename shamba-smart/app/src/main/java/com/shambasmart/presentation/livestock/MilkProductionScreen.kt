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
import com.shambasmart.data.local.entity.MilkProduction
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkProductionScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val todayMilkYield by viewModel.todayMilkYield.collectAsStateWithLifecycle()
    var selectedDoe by remember { mutableStateOf<Animal?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Get milk records for selected doe
    val milkRecords by remember(selectedDoe?.id) {
        selectedDoe?.id?.let { doeId ->
            viewModel.getMilkRecordsByAnimal(doeId)
        } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Milk Production",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Today's Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Today's Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MilkStat("Total Yield", "${todayMilkYield ?: 0} L")
                    MilkStat("Does Milked", "${milkRecords.size}")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Doe Selection
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedDoe?.let { "Doe: ${it.tagId ?: "No Tag"} - ${it.species}" } ?: "Select Doe (Female Goat)",
                onValueChange = { _ -> },
                readOnly = true,
                label = { Text("Doe (Female Goat)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                animals.filter { it.sex == "female" && it.species == "goat" }.forEach { animal ->
                    DropdownMenuItem(
                        text = { Text("${animal.tagId ?: "No Tag"} - ${animal.species}") },
                        onClick = {
                            selectedDoe = animal
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Milk Production Records List
        if (milkRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Opacity,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No milk production records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Milk Record")
                    }
                }
            }
        } else {
            LazyColumn {
                items(milkRecords) { record ->
                    MilkProductionCard(record = record)
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
            Icon(Icons.Default.Add, contentDescription = "Add Milk Record")
        }
    }

    // Add Milk Production Dialog
    if (showAddDialog) {
        AddMilkProductionDialog(
            animalId = selectedDoe?.id ?: 0L,
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                viewModel.addMilkRecord(record)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MilkStat(label: String, value: String) {
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
private fun MilkProductionCard(record: MilkProduction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Doe: ${record.animalId}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = record.date.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                record.morningYield?.let {
                    MilkYieldItem("Morning", "$it L")
                }
                record.eveningYield?.let {
                    MilkYieldItem("Evening", "$it L")
                }
                MilkYieldItem("Total", "${record.totalYield} L")
            }
        }
    }
}

@Composable
private fun MilkYieldItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AddMilkProductionDialog(
    animalId: Long,
    onDismiss: () -> Unit,
    onAdd: (MilkProduction) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var morningYield by remember { mutableStateOf("") }
    var eveningYield by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milk Production") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = morningYield,
                        onValueChange = { morningYield = it },
                        label = { Text("Morning (L)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = eveningYield,
                        onValueChange = { eveningYield = it },
                        label = { Text("Evening (L)") },
                        modifier = Modifier.weight(1f)
                    )
                }
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
                    val morning = morningYield.toDoubleOrNull() ?: 0.0
                    val evening = eveningYield.toDoubleOrNull() ?: 0.0
                    onAdd(
                        MilkProduction(
                            animalId = animalId,
                            date = LocalDate.parse(date),
                            morningYield = if (morning > 0) morning else null,
                            eveningYield = if (evening > 0) evening else null,
                            totalYield = morning + evening,
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
