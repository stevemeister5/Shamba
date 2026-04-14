package com.shambasmart.presentation.livestock

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.WeightEntry
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackingScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weight Tracking",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Monitor growth and weight trends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Button(
                onClick = { showAddWeightDialog = true },
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
                    text = "Record Weight",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        // Animals with Weight Data
        Text(
            text = "ANIMALS",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        if (animals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Neutral200),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MonitorWeight,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Neutral300
                        )
                        Text(
                            text = "No animals registered yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                    }
                }
            }
        } else {
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
                            text = "CURRENT WT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Table Rows
                    animals.forEach { animal ->
                        WeightTableRow(
                            animal = animal,
                            onRecordWeight = {
                                selectedAnimal = animal
                                showAddWeightDialog = true
                            }
                        )
                        HorizontalDivider(
                            color = Neutral100,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
    
    // Add Weight Dialog
    if (showAddWeightDialog) {
        AddWeightDialog(
            animal = selectedAnimal,
            animals = animals,
            onDismiss = { 
                showAddWeightDialog = false
                selectedAnimal = null
            },
            onSave = { weightEntry ->
                viewModel.addWeightEntry(weightEntry)
                showAddWeightDialog = false
                selectedAnimal = null
            }
        )
    }
}

@Composable
private fun WeightTableRow(
    animal: Animal,
    onRecordWeight: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = animal.tagId ?: "No Tag",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral950,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = animal.species.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = animal.weight?.let { "${String.format("%.1f", it)} kg" } ?: "—",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = GeistMonoFamily
                ),
                color = Neutral800
            )
            
            IconButton(
                onClick = onRecordWeight,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Record weight",
                    modifier = Modifier.size(14.dp),
                    tint = Green400
                )
            }
        }
        
        Text(
            text = animal.status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = when (animal.status) {
                "active" -> Green400
                "pregnant" -> Amber400
                "sick" -> Red400
                else -> Neutral600
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AddWeightDialog(
    animal: Animal?,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onSave: (WeightEntry) -> Unit
) {
    var selectedAnimalId by remember { mutableStateOf(animal?.id) }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MonitorWeight,
                    contentDescription = null,
                    tint = Teal400
                )
                Text(
                    text = "Record Weight",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Select Animal
                if (animal == null) {
                    Text(
                        text = "Select Animal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                    // Simplified - would use dropdown in real implementation
                    OutlinedTextField(
                        value = animals.find { it.id == selectedAnimalId }?.tagId ?: "",
                        onValueChange = { },
                        label = { Text("Animal") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                // Weight Input
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    placeholder = { Text("e.g., 45.5") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weightValue = weight.toDoubleOrNull()
                    if (selectedAnimalId != null && weightValue != null) {
                        onSave(
                            WeightEntry(
                                animalId = selectedAnimalId!!,
                                date = today,
                                weight = weightValue,
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = selectedAnimalId != null && weight.isNotBlank() && weight.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50,
                    disabledContainerColor = Neutral200,
                    disabledContentColor = Neutral600
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
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