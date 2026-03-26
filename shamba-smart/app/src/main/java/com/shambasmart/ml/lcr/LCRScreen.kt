package com.shambasmart.ml.lcr

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
fun LCRScreen(
    viewModel: LCRViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var silageKg by remember { mutableStateOf("") }
    var napierKg by remember { mutableStateOf("") }
    var concentrateKg by remember { mutableStateOf("") }
    var silageCost by remember { mutableStateOf("") }
    var napierCost by remember { mutableStateOf("") }
    var concentrateCost by remember { mutableStateOf("") }
    var targetDM by remember { mutableStateOf("4.5") } // Typical for 150lb doe
    var targetCP by remember { mutableStateOf("0.15") } // 15% CP

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Least-Cost Ration Solver",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Optimize feed mix for maximum profit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Input Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feed Quantities (kg)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = silageKg,
                        onValueChange = { silageKg = it },
                        label = { Text("Silage") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = napierKg,
                        onValueChange = { napierKg = it },
                        label = { Text("Napier") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = concentrateKg,
                    onValueChange = { concentrateKg = it },
                    label = { Text("Concentrate (Sunflower/Cottonseed)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cost Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feed Costs (TZS/kg)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = silageCost,
                        onValueChange = { silageCost = it },
                        label = { Text("Silage Cost") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = napierCost,
                        onValueChange = { napierCost = it },
                        label = { Text("Napier Cost") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = concentrateCost,
                    onValueChange = { concentrateCost = it },
                    label = { Text("Concentrate Cost") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Requirements Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Target Requirements", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = targetDM,
                        onValueChange = { targetDM = it },
                        label = { Text("Target DM (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = targetCP,
                        onValueChange = { targetCP = it },
                        label = { Text("Target CP (%)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calculate Button
        Button(
            onClick = {
                viewModel.calculateOptimalRation(
                    silageKg = silageKg.toDoubleOrNull() ?: 0.0,
                    napierKg = napierKg.toDoubleOrNull() ?: 0.0,
                    concentrateKg = concentrateKg.toDoubleOrNull() ?: 0.0,
                    silageCost = silageCost.toDoubleOrNull() ?: 0.0,
                    napierCost = napierCost.toDoubleOrNull() ?: 0.0,
                    concentrateCost = concentrateCost.toDoubleOrNull() ?: 0.0,
                    targetDM = targetDM.toDoubleOrNull() ?: 4.5,
                    targetCP = targetCP.toDoubleOrNull() ?: 0.15
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isCalculating
        ) {
            if (uiState.isCalculating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate Optimal Ration")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results Section
        if (uiState.totalDM > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.meetsRequirements) 
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Results", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStat("Total DM", "${String.format("%.2f", uiState.totalDM)} kg")
                        ResultStat("Total CP", "${String.format("%.1f", uiState.totalCP * 100)}%")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStat("Total Cost", "TZS ${String.format("%.0f", uiState.totalCost)}")
                        ResultStat("Cost/kg DM", "TZS ${String.format("%.0f", uiState.costPerKgDM)}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.meetsRequirements) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
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
                    text = "• Reduces purchased feed costs by 15-20%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Maximizes use of home-grown silage",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Optimizes protein supplementation",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String) {
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