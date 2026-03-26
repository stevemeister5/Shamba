package com.shambasmart.presentation.crops

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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shambasmart.data.local.entity.Plot
import com.shambasmart.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotRegistryScreen(
    navController: NavController = rememberNavController(),
    viewModel: CropsViewModel = hiltViewModel()
) {
    val plots by viewModel.allPlots.collectAsStateWithLifecycle()
    val totalAcres by viewModel.totalAcres.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPlot by remember { mutableStateOf<Plot?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Field Registry",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Analytics Button
                FilledTonalButton(
                    onClick = { navController.navigate(Screen.PlotAnalytics.route) },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = "Analytics",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analytics")
                }
                // Add Plot Button
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Plot")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Farm Overview", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlotStat("Total Plots", "${plots.size}")
                    PlotStat("Total Acres", "${totalAcres ?: 0}")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Plots List
        if (plots.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Landscape,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No plots registered")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add First Plot")
                    }
                }
            }
        } else {
            LazyColumn {
                items(plots) { plot ->
                    PlotCard(
                        plot = plot,
                        onClick = { selectedPlot = plot },
                        onDelete = { viewModel.deletePlot(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Add Plot Dialog
    if (showAddDialog) {
        AddPlotDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { plot ->
                viewModel.addPlot(plot)
                showAddDialog = false
            }
        )
    }

    // Edit Plot Dialog
    selectedPlot?.let { plot ->
        EditPlotDialog(
            plot = plot,
            onDismiss = { selectedPlot = null },
            onUpdate = { updatedPlot ->
                viewModel.updatePlot(updatedPlot)
                selectedPlot = null
            }
        )
    }
}

@Composable
private fun PlotStat(label: String, value: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlotCard(
    plot: Plot,
    onClick: () -> Unit,
    onDelete: (Plot) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${plot.sizeAcres} acres",
                    style = MaterialTheme.typography.bodyMedium
                )
                plot.currentUse?.let {
                    Text(
                        text = "Current use: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { onDelete(plot) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun AddPlotDialog(
    onDismiss: () -> Unit,
    onAdd: (Plot) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sizeAcres by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var currentUse by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Plot") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plot Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sizeAcres,
                    onValueChange = { sizeAcres = it },
                    label = { Text("Size (acres)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentUse,
                    onValueChange = { currentUse = it },
                    label = { Text("Current Use") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        Plot(
                            name = name,
                            sizeAcres = sizeAcres.toDoubleOrNull() ?: 0.0,
                            soilType = soilType.ifBlank { null },
                            currentUse = currentUse.ifBlank { null }
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

@Composable
private fun EditPlotDialog(
    plot: Plot,
    onDismiss: () -> Unit,
    onUpdate: (Plot) -> Unit
) {
    var name by remember { mutableStateOf(plot.name) }
    var sizeAcres by remember { mutableStateOf(plot.sizeAcres.toString()) }
    var soilType by remember { mutableStateOf(plot.soilType ?: "") }
    var currentUse by remember { mutableStateOf(plot.currentUse ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Plot") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plot Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sizeAcres,
                    onValueChange = { sizeAcres = it },
                    label = { Text("Size (acres)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentUse,
                    onValueChange = { currentUse = it },
                    label = { Text("Current Use") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        plot.copy(
                            name = name,
                            sizeAcres = sizeAcres.toDoubleOrNull() ?: plot.sizeAcres,
                            soilType = soilType.ifBlank { null },
                            currentUse = currentUse.ifBlank { null }
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}