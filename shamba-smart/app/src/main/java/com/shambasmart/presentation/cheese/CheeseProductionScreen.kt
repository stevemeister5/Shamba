package com.shambasmart.presentation.cheese

import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheeseProductionScreen(
    viewModel: CheeseViewModel = hiltViewModel()
) {
    val cheeseBatches by viewModel.allCheeseBatches.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalYield = cheeseBatches.sumOf { it.yieldKg }
    val aging = cheeseBatches.count { it.status == "aging" }
    val ready = cheeseBatches.count { it.status == "ready" }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            CheeseProductionHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // KPI Strip
            CheeseKPIStrip(
                totalYield = totalYield,
                aging = aging,
                ready = ready
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Cheese Batches List
            if (cheeseBatches.isEmpty()) {
                EmptyCheeseState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cheeseBatches) { batch ->
                        CheeseBatchCard(batch = batch)
                    }
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Green500,
            contentColor = Green950,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Cheese Batch")
        }
    }

    // Add Cheese Batch Dialog
    if (showAddDialog) {
        AddCheeseBatchDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { batch ->
                viewModel.addCheeseBatch(batch)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CheeseProductionHeader() {
    Column {
        Text(
            text = "Cheese Production",
            style = MaterialTheme.typography.headlineLarge,
            color = Neutral950
        )
        Text(
            text = "Track cheese batches and aging progress",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun CheeseKPIStrip(
    totalYield: Double,
    aging: Int,
    ready: Int
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
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CheeseKPIItem(
                icon = Icons.Outlined.LunchDining,
                label = "TOTAL YIELD",
                value = "${String.format("%.1f", totalYield)} kg",
                valueColor = Amber400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            CheeseKPIItem(
                icon = Icons.Outlined.HourglassEmpty,
                label = "AGING",
                value = "$aging",
                valueColor = Teal400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            CheeseKPIItem(
                icon = Icons.Outlined.CheckCircle,
                label = "READY",
                value = "$ready",
                valueColor = Green400,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CheeseKPIItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Neutral600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = GeistMonoFamily),
            color = valueColor
        )
    }
}

@Composable
private fun CheeseBatchCard(batch: CheeseBatch) {
    val (statusColor, statusText) = when (batch.status) {
        "aging" -> Teal400 to "AGING"
        "ready" -> Green400 to "READY"
        "sold" -> Neutral600 to "SOLD"
        else -> Neutral400 to batch.status.uppercase()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Batch Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LunchDining,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = statusColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = batch.batchId,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = GeistMonoFamily,
                        color = Neutral950
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Type: ${batch.cheeseType.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Yield",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${batch.yieldKg} kg",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                            color = Neutral950
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Milk Used",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${batch.milkVolumeUsed} L",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                            color = Neutral950
                        )
                    }
                    
                    batch.agingLocation?.let {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Neutral950
                            )
                        }
                    }
                }
                
                batch.totalCost?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cost: TZS ${String.format("%,.0f", it)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = GeistMonoFamily),
                        color = Amber400
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCheeseState(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LunchDining,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = "No cheese batches",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral600
                )
                Text(
                    text = "Start your first cheese batch to begin tracking aging progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral400
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Cheese Batch")
                }
            }
        }
    }
}

@Composable
private fun AddCheeseBatchDialog(
    onDismiss: () -> Unit,
    onAdd: (CheeseBatch) -> Unit
) {
    var batchId by remember { mutableStateOf("") }
    var productionDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var milkVolumeUsed by remember { mutableStateOf("") }
    var cheeseType by remember { mutableStateOf("fresh") }
    var yieldKg by remember { mutableStateOf("") }
    var agingLocation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Cheese Batch",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = batchId,
                    onValueChange = { batchId = it },
                    label = { Text("Batch ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = productionDate,
                    onValueChange = { productionDate = it },
                    label = { Text("Production Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = milkVolumeUsed,
                    onValueChange = { milkVolumeUsed = it },
                    label = { Text("Milk Volume Used (L)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Cheese Type Selector
                Column {
                    Text(
                        text = "Cheese Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = cheeseType == "fresh",
                            onClick = { cheeseType = "fresh" },
                            label = {
                                Text(
                                    text = "Fresh",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Green300
                            )
                        )
                        FilterChip(
                            selected = cheeseType == "aged",
                            onClick = { cheeseType = "aged" },
                            label = {
                                Text(
                                    text = "Aged",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = yieldKg,
                    onValueChange = { yieldKg = it },
                    label = { Text("Yield (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = agingLocation,
                    onValueChange = { agingLocation = it },
                    label = { Text("Aging Location") },
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
                    onAdd(
                        CheeseBatch(
                            batchId = batchId,
                            productionDate = LocalDate.parse(productionDate),
                            milkVolumeUsed = milkVolumeUsed.toDoubleOrNull() ?: 0.0,
                            cheeseType = cheeseType,
                            yieldKg = yieldKg.toDoubleOrNull() ?: 0.0,
                            agingStartDate = if (cheeseType == "aged") LocalDate.parse(productionDate) else null,
                            agingLocation = agingLocation.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Batch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}