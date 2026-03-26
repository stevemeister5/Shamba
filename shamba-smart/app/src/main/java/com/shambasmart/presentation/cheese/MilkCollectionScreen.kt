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
import com.shambasmart.data.local.entity.MilkCollection
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkCollectionScreen(
    viewModel: CheeseViewModel = hiltViewModel()
) {
    val milkCollections by viewModel.allMilkCollections.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Milk Collection",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Collection Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalCollected = milkCollections.sumOf { it.quantityLitres }
                    val accepted = milkCollections.count { it.accepted }
                    CollectionStat("Total Collected", "${String.format("%.1f", totalCollected)} L")
                    CollectionStat("Accepted", "$accepted")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Milk Collections List
        if (milkCollections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalDrink,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No milk collections")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Milk Collection")
                    }
                }
            }
        } else {
            LazyColumn {
                items(milkCollections) { collection ->
                    MilkCollectionCard(collection = collection)
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
            Icon(Icons.Default.Add, contentDescription = "Add Milk Collection")
        }
    }

    // Add Milk Collection Dialog
    if (showAddDialog) {
        AddMilkCollectionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { collection ->
                viewModel.addMilkCollection(collection)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CollectionStat(label: String, value: String) {
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
private fun MilkCollectionCard(collection: MilkCollection) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${collection.quantityLitres} L",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Badge(
                    containerColor = if (collection.accepted) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.error
                ) {
                    Text(if (collection.accepted) "Accepted" else "Rejected")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Date: ${collection.date}", style = MaterialTheme.typography.bodyMedium)
            collection.phLevel?.let {
                Text(text = "pH: $it", style = MaterialTheme.typography.bodySmall)
            }
            collection.rejectionReason?.let {
                Text(
                    text = "Reason: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddMilkCollectionDialog(
    onDismiss: () -> Unit,
    onAdd: (MilkCollection) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var quantityLitres by remember { mutableStateOf("") }
    var phLevel by remember { mutableStateOf("") }
    var smellOk by remember { mutableStateOf(true) }
    var colorOk by remember { mutableStateOf(true) }
    var accepted by remember { mutableStateOf(true) }
    var rejectionReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milk Collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityLitres,
                    onValueChange = { quantityLitres = it },
                    label = { Text("Quantity (litres)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phLevel,
                    onValueChange = { phLevel = it },
                    label = { Text("pH Level (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = smellOk,
                        onClick = { smellOk = !smellOk },
                        label = { Text("Smell OK") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = colorOk,
                        onClick = { colorOk = !colorOk },
                        label = { Text("Color OK") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = accepted,
                        onClick = { accepted = true },
                        label = { Text("Accept") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !accepted,
                        onClick = { accepted = false },
                        label = { Text("Reject") },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (!accepted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        MilkCollection(
                            date = LocalDate.parse(date),
                            quantityLitres = quantityLitres.toDoubleOrNull() ?: 0.0,
                            phLevel = phLevel.toDoubleOrNull(),
                            smellOk = smellOk,
                            colorOk = colorOk,
                            accepted = accepted,
                            rejectionReason = if (!accepted) rejectionReason else null
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