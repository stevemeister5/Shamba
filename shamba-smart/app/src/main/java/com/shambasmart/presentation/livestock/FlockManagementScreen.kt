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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlockManagementScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var selectedFlock by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Group poultry by species
    val poultryGroups = animals
        .filter { 
            it.species.contains("Layer", ignoreCase = true) || 
            it.species.contains("Broiler", ignoreCase = true) ||
            it.species.contains("Duck", ignoreCase = true)
        }
        .groupBy { it.species }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Flock Management",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Flock Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FlockStat("Total Flocks", "${poultryGroups.size}")
                    FlockStat("Total Birds", "${poultryGroups.values.sumOf { it.size }}")
                    FlockStat("Avg Flock Size", 
                        if (poultryGroups.isNotEmpty()) 
                            "${poultryGroups.values.sumOf { it.size } / poultryGroups.size}" 
                        else "0"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Flock List
        if (poultryGroups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No poultry flocks")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add Layer chickens, Broilers, or Ducks to manage flocks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(poultryGroups.entries.toList()) { (species, birds) ->
                    FlockCard(
                        species = species,
                        birds = birds,
                        isSelected = selectedFlock == species,
                        onClick = { selectedFlock = if (selectedFlock == species) null else species }
                    )
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
            Icon(Icons.Default.Add, contentDescription = "Add Flock")
        }
    }

    // Add Flock Dialog
    if (showAddDialog) {
        AddFlockDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { species, count, breed ->
                // TODO: Add multiple animals to database
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FlockStat(label: String, value: String) {
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
private fun FlockCard(
    species: String,
    birds: List<Animal>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = species,
                    style = MaterialTheme.typography.titleMedium
                )
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("${birds.size} birds")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Flock statistics
            val activeBirds = birds.count { it.status == "active" }
            val femaleBirds = birds.count { it.sex == "female" }
            val maleBirds = birds.count { it.sex == "male" }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlockDetail("Active", "$activeBirds")
                FlockDetail("Female", "$femaleBirds")
                FlockDetail("Male", "$maleBirds")
            }
            
            // Show individual birds if selected
            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Individual Birds",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                birds.forEach { bird ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bird.tagId ?: "No Tag",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${bird.sex} - ${bird.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlockDetail(label: String, value: String) {
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
private fun AddFlockDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, String) -> Unit
) {
    var species by remember { mutableStateOf("Chicken (Layer)") }
    var count by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Flock") },
        text = {
            Column {
                // Species Selection
                Text(
                    text = "Species",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Chicken (Layer)", "Chicken (Broiler)", "Duck").forEach { s ->
                        FilterChip(
                            selected = species == s,
                            onClick = { species = s },
                            label = { Text(s) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("Number of Birds") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(species, count.toIntOrNull() ?: 0, breed)
                }
            ) {
                Text("Add Flock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}