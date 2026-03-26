package com.shambasmart.presentation.feed

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
import com.shambasmart.data.local.entity.StoreItem
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel = hiltViewModel()
) {
    val storeItems by viewModel.allStoreItems.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Store Management",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Store Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val lowStock = storeItems.count { 
                        it.reorderLevel != null && it.quantity <= it.reorderLevel 
                    }
                    val expiring = storeItems.count {
                        it.expiryDate != null && it.expiryDate <= LocalDate.now().toString()
                    }
                    StoreStat("Total Items", "${storeItems.size}")
                    StoreStat("Low Stock", "$lowStock")
                    StoreStat("Expiring", "$expiring")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Store Items List
        if (storeItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No store items")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Store Item")
                    }
                }
            }
        } else {
            LazyColumn {
                items(storeItems) { item ->
                    StoreItemCard(item = item)
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
            Icon(Icons.Default.Add, contentDescription = "Add Store Item")
        }
    }

    // Add Store Item Dialog
    if (showAddDialog) {
        AddStoreItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                viewModel.addStoreItem(item)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StoreStat(label: String, value: String) {
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
private fun StoreItemCard(item: StoreItem) {
    val isLowStock = item.reorderLevel != null && item.quantity <= item.reorderLevel
    val isExpiring = item.expiryDate != null && item.expiryDate <= LocalDate.now().toString()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    if (isLowStock) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("Low")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isExpiring) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                            Text("Expiring")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Category: ${item.category}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Quantity: ${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodyMedium)
            item.expiryDate?.let {
                Text(text = "Expires: $it", style = MaterialTheme.typography.bodySmall)
            }
            item.costPerUnit?.let {
                Text(
                    text = "Cost: TZS $it/${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun AddStoreItemDialog(
    onDismiss: () -> Unit,
    onAdd: (StoreItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("seeds") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var expiryDate by remember { mutableStateOf("") }
    var reorderLevel by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Store Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("seeds", "fertilizer", "chemicals", "medicine", "equipment").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it },
                    label = { Text("Reorder Level") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost per Unit (TZS)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        StoreItem(
                            name = name,
                            category = category,
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            expiryDate = expiryDate.ifBlank { null },
                            reorderLevel = reorderLevel.toDoubleOrNull(),
                            costPerUnit = costPerUnit.toDoubleOrNull()
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