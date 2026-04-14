package com.shambasmart.presentation.cheese

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchCreationScreen(
    onNavigateBack: () -> Unit,
    onBatchCreated: (CheeseBatch) -> Unit,
    viewModel: CheeseViewModel = hiltViewModel()
) {
    var milkVolume by remember { mutableStateOf("") }
    var cheeseType by remember { mutableStateOf("Cheddar") }
    var agingLocation by remember { mutableStateOf("") }
    var targetAgingDays by remember { mutableStateOf("") }
    var milkCost by remember { mutableStateOf("") }
    var cultureCost by remember { mutableStateOf("") }
    var rennetCost by remember { mutableStateOf("") }
    var packagingCost by remember { mutableStateOf("") }
    var labourCost by remember { mutableStateOf("") }
    var qualityNotes by remember { mutableStateOf("") }
    
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Neutral600
                )
            }
            Column {
                Text(
                    text = "Create Cheese Batch",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Start a new production batch",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
        }
        
        // Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Neutral200),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Milk Volume
                OutlinedTextField(
                    value = milkVolume,
                    onValueChange = { milkVolume = it },
                    label = { Text("Milk Volume (Liters)") },
                    placeholder = { Text("e.g., 50") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Cheese Type
                Text(
                    text = "Cheese Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("Cheddar", "Gouda", "Mozzarella", "Feta", "Cream Cheese", "Other")) { type ->
                        FilterChip(
                            selected = cheeseType == type,
                            onClick = { cheeseType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber600.copy(alpha = 0.3f),
                                selectedLabelColor = Amber300
                            )
                        )
                    }
                }
                
                // Aging Location
                OutlinedTextField(
                    value = agingLocation,
                    onValueChange = { agingLocation = it },
                    label = { Text("Aging Location") },
                    placeholder = { Text("e.g., Cellar A, Aging Room 1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Target Aging Days
                OutlinedTextField(
                    value = targetAgingDays,
                    onValueChange = { targetAgingDays = it },
                    label = { Text("Target Aging Days") },
                    placeholder = { Text("e.g., 60") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Cost Section
                Text(
                    text = "Production Costs (TZS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = milkCost,
                        onValueChange = { milkCost = it },
                        label = { Text("Milk Cost") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    OutlinedTextField(
                        value = cultureCost,
                        onValueChange = { cultureCost = it },
                        label = { Text("Culture") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = rennetCost,
                        onValueChange = { rennetCost = it },
                        label = { Text("Rennet") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    OutlinedTextField(
                        value = packagingCost,
                        onValueChange = { packagingCost = it },
                        label = { Text("Packaging") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                OutlinedTextField(
                    value = labourCost,
                    onValueChange = { labourCost = it },
                    label = { Text("Labour Cost") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Quality Notes
                OutlinedTextField(
                    value = qualityNotes,
                    onValueChange = { qualityNotes = it },
                    label = { Text("Quality Notes") },
                    placeholder = { Text("e.g., Milk pH 6.7, fresh morning milk") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
        
        // Maarifa Process Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Green800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = Green400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "MAARIFA PROCESS GUIDE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Green400
                    )
                }
                
                Text(
                    text = "For $cheeseType cheese: Heat milk to 30°C, add culture, wait 45 min, add rennet, cut curd, drain whey, press, and age for ${targetAgingDays.ifBlank { "30" }} days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800
                )
            }
        }
        
        // Create Button
        Button(
            onClick = {
                val batch = CheeseBatch(
                    batchId = java.util.UUID.randomUUID().toString(),
                    productionDate = today,
                    milkVolumeUsed = milkVolume.toDoubleOrNull() ?: 0.0,
                    cheeseType = cheeseType,
                    agingLocation = agingLocation,
                    agingStartDate = today,
                    yieldKg = 0.0,
                    milkCostTzs = (milkCost.toDoubleOrNull() ?: 0.0).toLong(),
                    cultureCostTzs = (cultureCost.toDoubleOrNull() ?: 0.0).toLong(),
                    rennetCostTzs = (rennetCost.toDoubleOrNull() ?: 0.0).toLong(),
                    packagingCostTzs = (packagingCost.toDoubleOrNull() ?: 0.0).toLong(),
                    labourCostTzs = (labourCost.toDoubleOrNull() ?: 0.0).toLong(),
                    qualityNotes = qualityNotes.ifBlank { null }
                )
                onBatchCreated(batch)
            },
            enabled = milkVolume.isNotBlank() && cheeseType.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber500,
                contentColor = Amber100,
                disabledContainerColor = Neutral200,
                disabledContentColor = Neutral600
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Create Batch",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}
