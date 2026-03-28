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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheeseInventoryScreen(
    viewModel: CheeseViewModel = hiltViewModel()
) {
    val cheeseBatches by viewModel.allCheeseBatches.collectAsStateWithLifecycle()
    var showSaleDialog by remember { mutableStateOf(false) }
    var selectedBatch by remember { mutableStateOf<CheeseBatch?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Cheese Inventory",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Inventory Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Inventory Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val available = cheeseBatches.filter { it.status != "sold" }
                    val totalKg = available.sumOf { it.yieldKg }
                    val readyToSell = available.count { it.status == "ready" }
                    InventoryStat("Available", "${String.format("%.1f", totalKg)} kg")
                    InventoryStat("Ready to Sell", "$readyToSell")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Cheese Inventory List
        if (cheeseBatches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No cheese inventory")
                }
            }
        } else {
            LazyColumn {
                items(cheeseBatches) { batch ->
                    InventoryCard(
                        batch = batch,
                        onSell = { 
                            selectedBatch = batch
                            showSaleDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Sale Dialog
    if (showSaleDialog && selectedBatch != null) {
        SaleDialog(
            batch = selectedBatch!!,
            onDismiss = { 
                showSaleDialog = false
                selectedBatch = null
            },
            onSell = { quantity, price, buyer ->
                viewModel.sellCheeseBatch(selectedBatch!!, quantity, price, buyer)
                showSaleDialog = false
                selectedBatch = null
            }
        )
    }
}

@Composable
private fun InventoryStat(label: String, value: String) {
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
private fun InventoryCard(
    batch: CheeseBatch,
    onSell: () -> Unit
) {
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
                Row {
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
                    if (batch.status == "ready") {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onSell) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Sell")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Type: ${batch.cheeseType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Yield: ${batch.yieldKg} kg", style = MaterialTheme.typography.bodyMedium)
            batch.unitsPacked?.let {
                Text(text = "Units: $it", style = MaterialTheme.typography.bodySmall)
            }
            batch.weightPerUnit?.let {
                Text(text = "Weight per unit: ${it} kg", style = MaterialTheme.typography.bodySmall)
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
private fun SaleDialog(
    batch: CheeseBatch,
    onDismiss: () -> Unit,
    onSell: (Double, Double, String) -> Unit
) {
    var quantity by remember { mutableStateOf(batch.yieldKg.toString()) }
    var pricePerKg by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sell Cheese Batch ${batch.batchId}") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pricePerKg,
                    onValueChange = { pricePerKg = it },
                    label = { Text("Price per kg (TZS)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSell(
                        quantity.toDoubleOrNull() ?: 0.0,
                        pricePerKg.toDoubleOrNull() ?: 0.0,
                        buyerName
                    )
                }
            ) {
                Text("Sell")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}