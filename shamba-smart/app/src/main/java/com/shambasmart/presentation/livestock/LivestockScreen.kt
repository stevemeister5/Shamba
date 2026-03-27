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
import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val herdSize by viewModel.herdSize.collectAsStateWithLifecycle()
    val goatCount by viewModel.goatCount.collectAsStateWithLifecycle()
    val sheepCount by viewModel.sheepCount.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Livestock Management",
                style = MaterialTheme.typography.headlineMedium
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Animal")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Herd Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Herd Overview", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HerdStat("Total", herdSize.toString())
                    HerdStat("Goats", goatCount.toString())
                    HerdStat("Sheep", sheepCount.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animals List
        if (animals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No animals registered")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add First Animal")
                    }
                }
            }
        } else {
            LazyColumn {
                items(animals) { animal ->
                    AnimalCard(
                        animal = animal,
                        onClick = { selectedAnimal = animal },
                        onDelete = { viewModel.deleteAnimal(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Add Animal Dialog
    if (showAddDialog) {
        AddAnimalDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { animal ->
                viewModel.addAnimal(animal)
                showAddDialog = false
            }
        )
    }

    // Animal Detail Dialog
    selectedAnimal?.let { animal ->
        AnimalDetailDialog(
            animal = animal,
            onDismiss = { selectedAnimal = null },
            onUpdate = { updatedAnimal ->
                viewModel.updateAnimal(updatedAnimal)
                selectedAnimal = null
            }
        )
    }
}

@Composable
private fun HerdStat(label: String, value: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalCard(
    animal: Animal,
    onClick: () -> Unit,
    onDelete: (Animal) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.tagId ?: "No Tag",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${animal.species} - ${animal.breed ?: "Unknown breed"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${animal.sex} | ${animal.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onDelete(animal) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun AddAnimalDialog(
    onDismiss: () -> Unit,
    onAdd: (Animal) -> Unit
) {
    var tagId by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("goat") }
    var breed by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("female") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Animal") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagId,
                    onValueChange = { tagId = it },
                    label = { Text("Tag ID (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = species == "goat",
                        onClick = { species = "goat" },
                        label = { Text("Goat") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = species == "sheep",
                        onClick = { species = "sheep" },
                        label = { Text("Sheep") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = sex == "female",
                        onClick = { sex = "female" },
                        label = { Text("Female") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = sex == "male",
                        onClick = { sex = "male" },
                        label = { Text("Male") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        Animal(
                            tagId = tagId.ifBlank { null },
                            species = species,
                            breed = breed.ifBlank { null },
                            sex = sex
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

@Composable
private fun AnimalDetailDialog(
    animal: Animal,
    onDismiss: () -> Unit,
    onUpdate: (Animal) -> Unit
) {
    var tagId by remember { mutableStateOf(animal.tagId ?: "") }
    var breed by remember { mutableStateOf(animal.breed ?: "") }
    var weight by remember { mutableStateOf(animal.weight?.toString() ?: "") }
    var status by remember { mutableStateOf(animal.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Animal") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagId,
                    onValueChange = { tagId = it },
                    label = { Text("Tag ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
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
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("active", "sold", "deceased", "culled").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        animal.copy(
                            tagId = tagId.ifBlank { null },
                            breed = breed.ifBlank { null },
                            weight = weight.toDoubleOrNull(),
                            status = status
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}