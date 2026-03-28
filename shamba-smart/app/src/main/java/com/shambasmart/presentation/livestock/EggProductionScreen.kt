package com.shambasmart.presentation.livestock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggProductionScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var selectedBird by remember { mutableStateOf<Animal?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Filter only poultry (layers and ducks)
    val poultryBirds = animals.filter { 
        it.species.contains("Layer", ignoreCase = true) || 
        it.species.contains("Duck", ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Egg Production Tracking",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Today's Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EggStat("Total Eggs", "0") // TODO: Calculate from database
                    EggStat("Birds Recorded", "${poultryBirds.size}")
                    EggStat("Avg per Bird", "0")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Bird Selection
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedBird?.let { "${it.tagId ?: "No Tag"} - ${it.species}" } ?: "Select Bird",
                onValueChange = { _ -> },
                readOnly = true,
                label = { Text("Bird") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                poultryBirds.forEach { bird ->
                    DropdownMenuItem(
                        text = { Text("${bird.tagId ?: "No Tag"} - ${bird.species}") },
                        onClick = {
                            selectedBird = bird
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Egg Production Records List
        if (poultryBirds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Egg,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No poultry birds in your herd")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add Layer chickens or Ducks to track egg production",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(poultryBirds) { bird ->
                    EggProductionCard(bird = bird)
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
            Icon(Icons.Default.Add, contentDescription = "Add Egg Record")
        }
    }

    // Add Egg Production Dialog
    if (showAddDialog) {
        AddEggProductionDialog(
            birdId = selectedBird?.id ?: 0L,
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                // TODO: Save to database
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EggStat(label: String, value: String) {
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
private fun EggProductionCard(bird: Animal) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bird.tagId ?: "No Tag"} - ${bird.species}",
                    style = MaterialTheme.typography.titleMedium
                )
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("Active")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Breed: ${bird.breed ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${bird.status}", style = MaterialTheme.typography.bodySmall)
            // TODO: Show last egg date and total eggs
        }
    }
}

@Composable
private fun AddEggProductionDialog(
    birdId: Long,
    onDismiss: () -> Unit,
    onAdd: (Any) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var eggCount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Egg Production") },
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
                    value = eggCount,
                    onValueChange = { eggCount = it },
                    label = { Text("Egg Count") },
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
                    // TODO: Create EggProduction entity and save
                    onAdd(Unit)
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