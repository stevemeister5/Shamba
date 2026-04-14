package com.shambasmart.presentation.livestock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.HealthRecord
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: LivestockViewModel = hiltViewModel()
) {
    // In a real app, the ID would be retrieved from SavedStateHandle in the ViewModel
    // For now, we'll assume the ViewModel has a way to get the current animal
    // Based on the NavGraph, we passed animalId. Let's look at how LivestockViewModel handles it.
    
    // We'll need to update LivestockViewModel to accept an ID or use a StateFlow for the selected animal
    // For this implementation, I'll assume we might need to fetch it.
    
    val allAnimals by viewModel.allAnimals.collectAsStateWithLifecycle()
    // Simplified logic: find animal in list. In production, use a dedicated DAO query.
    val animal = allAnimals.firstOrNull() // Placeholder, should be based on ID
    
    var selectedTab by remember { mutableStateOf("Overview") }
    var showAddHealthDialog by remember { mutableStateOf(false) }

    if (animal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Green500)
        }
        return
    }

    val healthRecords by viewModel.getHealthRecordsByAnimal(animal.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .padding(24.dp)
    ) {
        // Top Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Neutral600)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = animal.tagId ?: "No Tag",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Neutral950
                    )
                    Text(
                        text = "${animal.species} • ${animal.breed ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { /* Edit Logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceRaised, contentColor = Neutral600),
                    border = BorderStroke(1.dp, Neutral200),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                "Overview" -> OverviewTab(animal)
                "Health" -> HealthTab(
                    healthRecords = healthRecords,
                    onAddRecord = { showAddHealthDialog = true },
                    onDeleteRecord = { viewModel.deleteHealthRecord(it) }
                )
                "Milk" -> MilkTab(animal)
                "Reproduction" -> ReproductionTab(animal)
            }
        }
    }

    if (showAddHealthDialog) {
        AddHealthRecordDialog(
            animalId = animal.id,
            onDismiss = { showAddHealthDialog = false },
            onSave = { record ->
                viewModel.addHealthRecord(record)
                showAddHealthDialog = false
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
private fun OverviewTab(animal: Animal) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Neutral200),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow("Status", animal.status.uppercase())
                InfoRow("Gender", animal.sex.replaceFirstChar { it.uppercase() })
                InfoRow("Weight", animal.weight?.let { "${String.format("%.1f", it)} kg" } ?: "Not recorded")
                InfoRow("Date of Birth", animal.dateOfBirth?.toString() ?: "Unknown")
                InfoRow("Notes", animal.notes ?: "No additional notes")
            }
        }
    }
}

@Composable
private fun HealthTab(
    healthRecords: List<HealthRecord>,
    onAddRecord: () -> Unit,
    onDeleteRecord: (HealthRecord) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Records", style = MaterialTheme.typography.labelSmall, color = Neutral600)
            TextButton(onClick = onAddRecord) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Record", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (healthRecords.isEmpty()) {
            Text("No health records found.", style = MaterialTheme.typography.bodyMedium, color = Neutral600)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(healthRecords) { record ->
                    HealthRecordCard(record, onDelete = { onDeleteRecord(record) })
                }
            }
        }
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecord, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = Green400)
                Text(record.description ?: "No description", style = MaterialTheme.typography.bodyMedium, color = Neutral950)
                Text(record.date.toString(), style = MaterialTheme.typography.bodySmall, color = Neutral600)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Neutral400, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MilkTab(animal: Animal) {
    Text("Milk production history will appear here.", style = MaterialTheme.typography.bodyMedium, color = Neutral600)
}

@Composable
private fun ReproductionTab(animal: Animal) {
    Text("Reproduction history will appear here.", style = MaterialTheme.typography.bodyMedium, color = Neutral600)
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Neutral600)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Neutral950)
    }
}
