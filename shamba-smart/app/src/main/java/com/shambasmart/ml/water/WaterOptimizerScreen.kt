package com.shambasmart.ml.water

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterOptimizerScreen(
    viewModel: WaterOptimizerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var maxTemp by remember { mutableStateOf("") }
    var minTemp by remember { mutableStateOf("") }
    var windLevel by remember { mutableStateOf("medium") }
    var soilMoisture by remember { mutableStateOf("50") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Water Optimizer",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Penman-Monteith Evapotranspiration Calculator",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Input Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Today's Weather", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = maxTemp,
                        onValueChange = { maxTemp = it },
                        label = { Text("Max Temp (°C)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minTemp,
                        onValueChange = { minTemp = it },
                        label = { Text("Min Temp (°C)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Wind Level", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("low", "medium", "high").forEach { level ->
                        FilterChip(
                            selected = windLevel == level,
                            onClick = { windLevel = level },
                            label = { Text(level) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = soilMoisture,
                    onValueChange = { soilMoisture = it },
                    label = { Text("Soil Moisture (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calculate Button
        Button(
            onClick = {
                viewModel.calculateET0(
                    maxTemp = maxTemp.toDoubleOrNull() ?: 30.0,
                    minTemp = minTemp.toDoubleOrNull() ?: 20.0,
                    windLevel = windLevel,
                    soilMoisture = soilMoisture.toDoubleOrNull() ?: 50.0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isCalculating
        ) {
            if (uiState.isCalculating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Water, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate Water Needs")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results Section
        if (uiState.et0 > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.shouldIrrigate) 
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Water Analysis", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WaterStat("ET₀", "${String.format("%.1f", uiState.et0)} mm")
                        WaterStat("Water Lost", "${String.format("%.1f", uiState.waterLost)} mm")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (uiState.shouldIrrigate) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Irrigation Recommended",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Estimated water needed: ${String.format("%.0f", uiState.irrigationNeeded)} mm",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No Irrigation Needed",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Text(
                            text = "Soil moisture is adequate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profit Impact Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Profit Impact", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Saves fuel/electricity by preventing unnecessary pumping",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Prevents nutrient leaching from over-irrigation",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Optimizes water use during dry seasons",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formula Explanation
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How It Works", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Uses Penman-Monteith (FAO-56) formula to calculate reference evapotranspiration (ET₀).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If soil moisture > threshold, the app prevents wasteful irrigation.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun WaterStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}