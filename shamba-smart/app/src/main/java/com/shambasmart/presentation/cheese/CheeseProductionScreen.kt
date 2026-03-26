package com.shambasmart.presentation.cheese

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
import com.shambasmart.data.local.entity.CheeseBatch
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheeseProductionScreen(
    viewModel: CheeseViewModel = hiltViewModel()
) {
    val cheeseBatches by viewModel.allCheeseBatches.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Cheese Production",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Production Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalYield = cheeseBatches.sumOf { it.yieldKg }
                    val aging = cheeseBatches.count { it.status == "aging" }
                    ProductionStat("Total Yield", "${String.format("%.1f", totalYield)} kg")
                    ProductionStat("Aging", "$aging")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Cheese Batches List
        if (cheeseBatches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Cheese,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No cheese batches")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Cheese Batch")
                    }
                }
            }
        } else {
            LazyColumn {
                items(cheeseBatches) { batch ->
                    CheeseBatchCard(batch = batch)
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
            Icon(Icons.Default.Add, contentDescription = "Add Cheese Batch")
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
private fun ProductionStat(label: String, value: String) {
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
private fun CheeseBatchCard(batch: CheeseBatch) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = batch.batchId,
                    style = MaterialTheme.typography.titleMedium
                )
                Badge(
                    containerColor = when (batch.status) {
                        "aging" -> MaterialTheme.colorScheme.primary
                        "ready" -> MaterialTheme.colorScheme.tertiary
                        "sold" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(batch.status)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Type: ${batch.cheeseType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Yield: ${batch.yieldKg} kg", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Milk used: ${batch.milkVolumeUsed} L", style = MaterialTheme.typography.bodySmall)
            batch.agingLocation?.let {
                Text(text = "Location: $it", style = MaterialTheme.typography.bodySmall)
            }
            batch.totalCost?.let {
                Text(
                    text = "Cost: TZS ${String.format("%.0f", it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
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
        title = { Text("Add Cheese Batch") },
        text = {
            Column {
                OutlinedTextField(
                    value = batchId,
                    onValueChange = { batchId = it },
                    label = { Text("Batch ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = productionDate,
                    onValueChange = { productionDate = it },
                    label = { Text("Production Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = milkVolumeUsed,
                    onValueChange = { milkVolumeUsed = it },
                    label = { Text("Milk Volume Used (L)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = cheeseType == "fresh",
                        onClick = { cheeseType = "fresh" },
                        label = { Text("Fresh") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = cheeseType == "aged",
                        onClick = { cheeseType = "aged" },
                        label = { Text("Aged") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = yieldKg,
                    onValueChange = { yieldKg = it },
                    label = { Text("Yield (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = agingLocation,
                    onValueChange = { agingLocation = it },
                    label = { Text("Aging Location") },
                    modifier = Modifier.fillMaxWidth()
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