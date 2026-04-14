package com.shambasmart.presentation.livestock

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.HealthRecord
import com.shambasmart.data.local.entity.LivestockType
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockScreen(
    onNavigateToAnimalDetail: (Long) -> Unit,
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val herdSize by viewModel.herdSize.collectAsStateWithLifecycle()
    val goatCount by viewModel.goatCount.collectAsStateWithLifecycle()
    val sheepCount by viewModel.sheepCount.collectAsStateWithLifecycle()
    val cattleCount by viewModel.cattleCount.collectAsStateWithLifecycle()
    val chickenLayerCount by viewModel.chickenLayerCount.collectAsStateWithLifecycle()
    val chickenBroilerCount by viewModel.chickenBroilerCount.collectAsStateWithLifecycle()
    val pigCount by viewModel.pigCount.collectAsStateWithLifecycle()
    val duckCount by viewModel.duckCount.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .padding(24.dp)
    ) {
        // Header
        LivestockHeader(
            herdSize = herdSize,
            goatCount = goatCount,
            sheepCount = sheepCount,
            cattleCount = cattleCount,
            chickenLayerCount = chickenLayerCount,
            chickenBroilerCount = chickenBroilerCount,
            pigCount = pigCount,
            duckCount = duckCount,
            onAddClick = { showAddDialog = true }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Filter Bar
        FilterBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            selectedSpecies = selectedSpecies,
            onSpeciesChange = { selectedSpecies = it },
            selectedStatus = selectedStatus,
            onStatusChange = { selectedStatus = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Animals Table
        AnimalsTable(
            animals = animals.filter { animal ->
                (selectedSpecies == "All" || animal.species == selectedSpecies) &&
                (selectedStatus == "All" || animal.status == selectedStatus) &&
                (searchQuery.isEmpty() || 
                 animal.tagId?.contains(searchQuery, ignoreCase = true) == true ||
                 animal.breed?.contains(searchQuery, ignoreCase = true) == true)
            },
            onAnimalClick = { onNavigateToAnimalDetail(it.id) },
            onDelete = { viewModel.deleteAnimal(it) }
        )
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
}

@Composable
private fun LivestockHeader(
    herdSize: Int,
    goatCount: Int,
    sheepCount: Int,
    cattleCount: Int,
    chickenLayerCount: Int,
    chickenBroilerCount: Int,
    pigCount: Int,
    duckCount: Int,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Livestock Management",
                style = MaterialTheme.typography.headlineLarge,
                color = Neutral950
            )
            Text(
                text = "Total: $herdSize | G: $goatCount | S: $sheepCount | C: $cattleCount | L: $chickenLayerCount | B: $chickenBroilerCount",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        }
        
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = Green500, contentColor = Green50),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Animal")
        }
    }
}

@Composable
private fun FilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSpecies: String,
    onSpeciesChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search tag ID or breed...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f).height(42.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green500,
                    unfocusedBorderColor = Neutral200,
                    focusedContainerColor = SurfaceSunken,
                    unfocusedContainerColor = SurfaceSunken
                ),
                shape = RoundedCornerShape(10.dp)
            )
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSpecies == "All",
                        onClick = { onSpeciesChange("All") },
                        label = { Text("All") }
                    )
                }
                items(LivestockType.getAllSpecies()) { species ->
                    FilterChip(
                        selected = selectedSpecies == species,
                        onClick = { onSpeciesChange(species) },
                        label = { Text(species) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimalsTable(
    animals: List<Animal>,
    onAnimalClick: (Animal) -> Unit,
    onDelete: (Animal) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth().background(Neutral100.copy(alpha = 0.3f)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("STATUS", style = MaterialTheme.typography.labelSmall, color = Neutral400, modifier = Modifier.weight(1f))
                Text("TAG ID", style = MaterialTheme.typography.labelSmall, color = Neutral400, modifier = Modifier.weight(1f))
                Text("SPECIES", style = MaterialTheme.typography.labelSmall, color = Neutral400, modifier = Modifier.weight(1f))
                Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = Neutral400, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(0.5f))
            }
            
            LazyColumn {
                items(animals) { animal ->
                    AnimalTableRow(animal, onClick = { onAnimalClick(animal) }, onDelete = { onDelete(animal) })
                    HorizontalDivider(color = Neutral100, thickness = 0.5.dp)
                }
            }
            
            if (animals.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No animals found", color = Neutral600)
                }
            }
        }
    }
}

@Composable
private fun AnimalTableRow(
    animal: Animal,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusChip(status = animal.status)
        Text(animal.tagId ?: "No Tag", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(animal.species, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("${animal.weight ?: "--"} kg", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = Neutral600)
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    Surface(
        color = when(status) {
            "active" -> Green800.copy(alpha = 0.2f)
            "sick" -> Red800.copy(alpha = 0.2f)
            else -> Neutral800.copy(alpha = 0.2f)
        },
        shape = RoundedCornerShape(99.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(status.uppercase(), style = MaterialTheme.typography.labelSmall, color = if(status == "sick") Red300 else Green300, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = tagId, onValueChange = { tagId = it }, label = { Text("Tag ID") })
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Breed") })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(Animal(tagId = tagId, species = species, breed = breed, sex = sex)) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
