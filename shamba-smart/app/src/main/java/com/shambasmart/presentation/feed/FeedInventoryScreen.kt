package com.shambasmart.presentation.feed

import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*

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
import com.shambasmart.data.local.entity.FeedInventory
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedInventoryScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val feedInventory by viewModel.allFeedInventory.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Feed Inventory",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feed Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val lowStock = feedInventory.count { 
                        it.reorderThreshold != null && it.stockLevel <= it.reorderThreshold 
                    }
                    FeedStat("Total Items", "${feedInventory.size}")
                    FeedStat("Low Stock", "$lowStock")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Feed Inventory List
        if (feedInventory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No feed inventory")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Feed Item")
                    }
                }
            }
        } else {
            LazyColumn {
                items(feedInventory) { feed ->
                    FeedInventoryCard(feed = feed)
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
            Icon(Icons.Default.Add, contentDescription = "Add Feed Item")
        }
    }

    // Add Feed Dialog
    if (showAddDialog) {
        AddFeedDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { feed ->
                viewModel.addFeed(feed)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FeedStat(label: String, value: String) {
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
private fun FeedInventoryCard(feed: FeedInventory) {
    val isLowStock = feed.reorderThreshold != null && feed.stockLevel <= feed.reorderThreshold

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feed.feedType,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isLowStock) {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text("Low Stock")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Stock: ${feed.stockLevel} ${feed.unit}", style = MaterialTheme.typography.bodyMedium)
            feed.reorderThreshold?.let {
                Text(text = "Reorder at: $it ${feed.unit}", style = MaterialTheme.typography.bodySmall)
            }
            feed.costPerUnit?.let {
                Text(
                    text = "Cost: TZS $it/${feed.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun AddFeedDialog(
    onDismiss: () -> Unit,
    onAdd: (FeedInventory) -> Unit
) {
    var feedType by remember { mutableStateOf("") }
    var stockLevel by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var reorderThreshold by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Feed Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = feedType,
                    onValueChange = { feedType = it },
                    label = { Text("Feed Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = stockLevel,
                        onValueChange = { stockLevel = it },
                        label = { Text("Stock Level") },
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
                    value = reorderThreshold,
                    onValueChange = { reorderThreshold = it },
                    label = { Text("Reorder Threshold") },
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
                        FeedInventory(
                            feedType = feedType,
                            stockLevel = stockLevel.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            reorderThreshold = reorderThreshold.toDoubleOrNull(),
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