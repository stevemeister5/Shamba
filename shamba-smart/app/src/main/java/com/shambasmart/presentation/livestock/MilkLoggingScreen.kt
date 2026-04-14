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
import com.shambasmart.data.local.entity.MilkProduction
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkLoggingScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedAnimalId by remember { mutableStateOf<Long?>(null) }

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
                    text = "Milk Logging",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Log AM/PM milk yield per doe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Button(
                onClick = { showLogDialog = true },
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
                    text = "Log Milk",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        // Today's Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Neutral200),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TODAY'S SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // AM Yield
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AM Yield",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "0.0L",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Teal400
                        )
                    }
                    
                    // PM Yield
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PM Yield",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "0.0L",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Teal400
                        )
                    }
                    
                    // Total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "0.0L",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Green400
                        )
                    }
                }
            }
        }
        
        // Milking Animals List
        Text(
            text = "LACTATING DOES",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        val lactatingAnimals = animals.filter { 
            it.sex == "female" && it.status in listOf("active", "dry") 
        }
        
        if (lactatingAnimals.isEmpty()) {
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
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Neutral300
                        )
                        Text(
                            text = "No lactating does found",
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
                            text = "BREED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "AM (L)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text(
                            text = "PM (L)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    
                    // Table Rows
                    lactatingAnimals.forEach { animal ->
                        MilkTableRow(
                            tagId = animal.tagId ?: "No Tag",
                            breed = animal.breed ?: "Unknown",
                            amYield = 0.0,
                            pmYield = 0.0,
                            onLogClick = {
                                selectedAnimalId = animal.id
                                showLogDialog = true
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
    
    // Log Milk Dialog
    if (showLogDialog) {
        LogMilkDialog(
            animalId = selectedAnimalId,
            animals = animals.filter { it.sex == "female" },
            onDismiss = { 
                showLogDialog = false
                selectedAnimalId = null
            },
            onSave = { record ->
                viewModel.addMilkRecord(record)
                showLogDialog = false
                selectedAnimalId = null
            }
        )
    }
}

@Composable
private fun MilkTableRow(
    tagId: String,
    breed: String,
    amYield: Double,
    pmYield: Double,
    onLogClick: () -> Unit
) {
    val total = amYield + pmYield
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tagId,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral950,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = breed,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = if (amYield > 0) String.format("%.1f", amYield) else "—",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral800,
            modifier = Modifier.weight(0.7f)
        )
        
        Text(
            text = if (pmYield > 0) String.format("%.1f", pmYield) else "—",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral800,
            modifier = Modifier.weight(0.7f)
        )
        
        Row(
            modifier = Modifier.weight(0.7f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (total > 0) String.format("%.1f", total) else "—",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = if (total > 0) Teal400 else Neutral800
            )
            
            IconButton(
                onClick = onLogClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Log milk",
                    modifier = Modifier.size(14.dp),
                    tint = Green400
                )
            }
        }
    }
}

@Composable
private fun LogMilkDialog(
    animalId: Long?,
    animals: List<com.shambasmart.data.local.entity.Animal>,
    onDismiss: () -> Unit,
    onSave: (MilkProduction) -> Unit
) {
    var selectedAnimal by remember { mutableStateOf(animalId) }
    var amYield by remember { mutableStateOf("") }
    var pmYield by remember { mutableStateOf("") }
    var session by remember { mutableStateOf("AM") }
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
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = Teal400
                )
                Text(
                    text = "Log Milk Yield",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Animal Selection
                if (animalId == null) {
                    Text(
                        text = "Select Doe",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                    // Dropdown would go here - simplified for now
                    OutlinedTextField(
                        value = animals.find { it.id == selectedAnimal }?.tagId ?: "",
                        onValueChange = { },
                        label = { Text("Doe") },
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
                
                // Session Selection
                Text(
                    text = "Session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AM", "PM").forEach { s ->
                        FilterChip(
                            selected = session == s,
                            onClick = { session = s },
                            label = { Text(s) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                // Yield Input
                OutlinedTextField(
                    value = if (session == "AM") amYield else pmYield,
                    onValueChange = { 
                        if (session == "AM") amYield = it else pmYield = it
                    },
                    label = { Text("Yield (Liters)") },
                    placeholder = { Text("e.g., 2.5") },
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
                    val amValue = amYield.toDoubleOrNull() ?: 0.0
                    val pmValue = pmYield.toDoubleOrNull() ?: 0.0
                    if (selectedAnimal != null && (amValue > 0 || pmValue > 0)) {
                        onSave(
                            MilkProduction(
                                animalId = selectedAnimal!!,
                                date = today,
                                morningYield = if (amValue > 0) amValue else null,
                                eveningYield = if (pmValue > 0) pmValue else null,
                                totalYield = amValue + pmValue,
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = selectedAnimal != null && 
                    ((session == "AM" && amYield.isNotBlank()) || (session == "PM" && pmYield.isNotBlank())),
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
