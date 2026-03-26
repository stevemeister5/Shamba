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
import com.shambasmart.data.local.entity.WeatherLog
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: CropsViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var weatherLogs by remember { mutableStateOf<List<WeatherLog>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Weather & Analytics",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Weather Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weather Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalRainfall = weatherLogs.sumOf { it.rainfallMm ?: 0.0 }
                    WeatherStat("Total Rainfall", "${String.format("%.1f", totalRainfall)} mm")
                    WeatherStat("Records", "${weatherLogs.size}")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Weather Logs List
        if (weatherLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No weather records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Weather Log")
                    }
                }
            }
        } else {
            LazyColumn {
                items(weatherLogs) { log ->
                    WeatherLogCard(log = log)
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
            Icon(Icons.Default.Add, contentDescription = "Add Weather Log")
        }
    }

    // Add Weather Dialog
    if (showAddDialog) {
        AddWeatherDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { log ->
                // TODO: Save to database
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WeatherStat(label: String, value: String) {
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
private fun WeatherLogCard(log: WeatherLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.date.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                log.rainfallMm?.let {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("${it} mm")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                log.maxTemp?.let {
                    TempItem("Max", "$it°C")
                }
                log.minTemp?.let {
                    TempItem("Min", "$it°C")
                }
                log.windLevel?.let {
                    Text(text = "Wind: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            log.unusualEvents?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Events: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun TempItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AddWeatherDialog(
    onDismiss: () -> Unit,
    onAdd: (WeatherLog) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var rainfallMm by remember { mutableStateOf("") }
    var maxTemp by remember { mutableStateOf("") }
    var minTemp by remember { mutableStateOf("") }
    var windLevel by remember { mutableStateOf("") }
    var unusualEvents by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weather Log") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rainfallMm,
                        onValueChange = { rainfallMm = it },
                        label = { Text("Rainfall (mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxTemp,
                        onValueChange = { maxTemp = it },
                        label = { Text("Max Temp (°C)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = minTemp,
                        onValueChange = { minTemp = it },
                        label = { Text("Min Temp (°C)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = windLevel,
                        onValueChange = { windLevel = it },
                        label = { Text("Wind Level") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unusualEvents,
                    onValueChange = { unusualEvents = it },
                    label = { Text("Unusual Events") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        WeatherLog(
                            date = LocalDate.parse(date),
                            rainfallMm = rainfallMm.toDoubleOrNull(),
                            maxTemp = maxTemp.toDoubleOrNull(),
                            minTemp = minTemp.toDoubleOrNull(),
                            windLevel = windLevel.ifBlank { null },
                            unusualEvents = unusualEvents.ifBlank { null },
                            notes = notes.ifBlank { null }
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