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
import com.shambasmart.data.local.entity.LivestockType
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockScreen(
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
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel - Animal Table (7/12)
            Column(
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight()
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
                    selectedAnimal = selectedAnimal,
                    onAnimalClick = { selectedAnimal = it },
                    onDelete = { viewModel.deleteAnimal(it) }
                )
            }
            
            // Right Panel - Animal Detail (5/12)
            if (selectedAnimal != null) {
                AnimalDetailPanel(
                    animal = selectedAnimal!!,
                    onClose = { selectedAnimal = null },
                    onUpdate = { updatedAnimal ->
                        viewModel.updateAnimal(updatedAnimal)
                        selectedAnimal = null
                    }
                )
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
                text = "Total: $herdSize | Goats: $goatCount | Sheep: $sheepCount | Cattle: $cattleCount | Layers: $chickenLayerCount | Broilers: $chickenBroilerCount | Pigs: $pigCount | Ducks: $duckCount",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        }
        
        // Add Animal Button
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
                text = "Add Animal",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Search by tag ID or breed...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Neutral600
                    )
                },
                modifier = Modifier.weight(1f).height(42.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green500,
                    unfocusedBorderColor = Neutral200,
                    focusedContainerColor = SurfaceSunken,
                    unfocusedContainerColor = SurfaceSunken
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Species Filter
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSpecies == "All",
                        onClick = { onSpeciesChange("All") },
                        label = {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceSunken,
                            selectedContainerColor = Green800.copy(alpha = 0.3f),
                            labelColor = Neutral800,
                            selectedLabelColor = Green300
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Neutral200,
                            selectedBorderColor = Green700,
                            enabled = true,
                            selected = selectedSpecies == "All"
                        )
                    )
                }
                items(LivestockType.getAllSpecies()) { species ->
                    FilterChip(
                        selected = selectedSpecies == species,
                        onClick = { onSpeciesChange(species) },
                        label = {
                            Text(
                                text = species,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceSunken,
                            selectedContainerColor = Green800.copy(alpha = 0.3f),
                            labelColor = Neutral800,
                            selectedLabelColor = Green300
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Neutral200,
                            selectedBorderColor = Green700,
                            enabled = true,
                            selected = selectedSpecies == species
                        )
                    )
                }
            }
            
            // Status Filter
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "active", "pregnant", "sick", "dry").forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusChange(status) },
                        label = {
                            Text(
                                text = status.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceSunken,
                            selectedContainerColor = when (status) {
                                "pregnant" -> Amber600.copy(alpha = 0.3f)
                                "sick" -> Red600.copy(alpha = 0.3f)
                                "dry" -> Teal600.copy(alpha = 0.3f)
                                else -> Green800.copy(alpha = 0.3f)
                            },
                            labelColor = Neutral800,
                            selectedLabelColor = when (status) {
                                "pregnant" -> Amber300
                                "sick" -> Red300
                                "dry" -> Teal300
                                else -> Green300
                            }
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Neutral200,
                            selectedBorderColor = when (status) {
                                "pregnant" -> Amber600
                                "sick" -> Red500
                                "dry" -> Teal600
                                else -> Green700
                            },
                            enabled = true,
                            selected = selectedStatus == status
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimalsTable(
    animals: List<Animal>,
    selectedAnimal: Animal?,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Neutral100.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral400,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "TAG ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral400,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "SPECIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral400,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "BREED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral400,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "SEX",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral400,
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    text = "WEIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral400,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }
            
            // Table Rows
            LazyColumn {
                items(animals) { animal ->
                    AnimalTableRow(
                        animal = animal,
                        isSelected = animal == selectedAnimal,
                        onClick = { onAnimalClick(animal) },
                        onDelete = { onDelete(animal) }
                    )
                    HorizontalDivider(
                        color = Neutral100,
                        thickness = 0.5.dp
                    )
                }
            }
            
            if (animals.isEmpty()) {
                EmptyState()
            }
        }
    }
}

@Composable
private fun AnimalTableRow(
    animal: Animal,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Green800.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Chip
        StatusChip(status = animal.status)
        
        // Tag ID
        Text(
            text = animal.tagId ?: "No Tag",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral950,
            modifier = Modifier.weight(1f)
        )
        
        // Species
        Text(
            text = animal.species.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        // Breed
        Text(
            text = animal.breed ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        // Sex
        Icon(
            imageVector = if (animal.sex == "female") Icons.Outlined.Female else Icons.Outlined.Male,
            contentDescription = animal.sex,
            modifier = Modifier.size(16.dp).weight(0.5f),
            tint = Neutral600
        )
        
        // Weight
        Text(
            text = animal.weight?.let { "${String.format("%.1f", it)} kg" } ?: "—",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral800,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        
        // Actions
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp).weight(0.5f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(14.dp),
                tint = Neutral600
            )
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, borderColor, textColor) = when (status) {
        "active" -> Triple(
            Green400.copy(alpha = 0.15f),
            Green700,
            Green300
        )
        "pregnant" -> Triple(
            Amber400.copy(alpha = 0.15f),
            Amber600,
            Amber300
        )
        "sick" -> Triple(
            Red400.copy(alpha = 0.15f),
            Red500,
            Red300
        )
        "dry" -> Triple(
            Teal400.copy(alpha = 0.12f),
            Teal600,
            Teal300
        )
        else -> Triple(
            Neutral400.copy(alpha = 0.12f),
            Neutral400,
            Neutral600
        )
    }
    
    Surface(
        color = backgroundColor,
        border = BorderStroke(0.5.dp, borderColor),
        shape = RoundedCornerShape(9999.dp),
        modifier = Modifier.height(22.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Neutral300
            )
            Text(
                text = "Add your first animal to begin tracking your herd",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnimalDetailPanel(
    animal: Animal,
    onClose: () -> Unit,
    onUpdate: (Animal) -> Unit
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(400.dp)
            .background(SurfaceRaised)
            .border(BorderStroke(1.dp, Neutral200))
    ) {
        // Panel Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = animal.tagId ?: "No Tag",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = GeistMonoFamily
                    ),
                    color = Neutral950
                )
                Text(
                    text = "${animal.species} • ${animal.breed ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = Neutral600
                )
            }
        }
        
        HorizontalDivider(color = Neutral200)
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Overview", "Health", "Milk", "Reproduction").forEach { tab ->
                TabButton(
                    text = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            "Overview" -> OverviewTab(animal)
            "Health" -> HealthTab(animal)
            "Milk" -> MilkTab(animal)
            "Reproduction" -> ReproductionTab(animal)
        }
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun OverviewTab(animal: Animal) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animal Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            border = BorderStroke(1.dp, Neutral200),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow("Species", animal.species.replaceFirstChar { it.uppercase() })
                InfoRow("Breed", animal.breed ?: "Unknown")
                InfoRow("Sex", animal.sex.replaceFirstChar { it.uppercase() })
                InfoRow("Weight", animal.weight?.let { "${String.format("%.1f", it)} kg" } ?: "Not recorded")
                InfoRow("Status", animal.status.replaceFirstChar { it.uppercase() })
                // Age calculated from dateOfBirth would require Clock import - showing dateOfBirth instead
                InfoRow("Date of Birth", animal.dateOfBirth?.toString() ?: "Unknown")
            }
        }
    }
}

@Composable
private fun HealthTab(animal: Animal) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Health Records",
            style = MaterialTheme.typography.headlineMedium,
            color = Neutral950
        )
        Text(
            text = "No health records available for this animal.",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun MilkTab(animal: Animal) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Milk Production",
            style = MaterialTheme.typography.headlineMedium,
            color = Neutral950
        )
        Text(
            text = "No milk production data available.",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun ReproductionTab(animal: Animal) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reproduction History",
            style = MaterialTheme.typography.headlineMedium,
            color = Neutral950
        )
        Text(
            text = "No reproduction records available.",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = if (value.contains("kg")) GeistMonoFamily else FontFamily.Default
            ),
            color = Neutral950
        )
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
        title = {
            Text(
                text = "Add Animal",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = tagId,
                    onValueChange = { tagId = it },
                    label = { Text("Tag ID (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Species Selection
                Text(
                    text = "Species",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LivestockType.getAllSpecies()) { s ->
                        FilterChip(
                            selected = species == s,
                            onClick = { species = s },
                            label = { Text(s) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("female", "male").forEach { s ->
                        FilterChip(
                            selected = sex == s,
                            onClick = { sex = s },
                            label = { Text(s.replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f),
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
                    onAdd(
                        Animal(
                            tagId = tagId.ifBlank { null },
                            species = species,
                            breed = breed.ifBlank { null },
                            sex = sex
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Animal")
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