package com.shambasmart.presentation.infrastructure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfrastructureScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Farm Infrastructure",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Farm Map Placeholder
        Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("16-Acre Farm Map")
                    Text("Korogwe, Tanga", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Infrastructure Summary
        LazyColumn {
            item {
                InfrastructureCard(
                    icon = Icons.Default.Home,
                    title = "Animal Shelters",
                    description = "Goat and sheep housing facilities"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                InfrastructureCard(
                    icon = Icons.Default.Water,
                    title = "Water Points",
                    description = "Drinking troughs and water sources"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                InfrastructureCard(
                    icon = Icons.Default.Inventory,
                    title = "Storage",
                    description = "Feed, equipment, and produce storage"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                InfrastructureCard(
                    icon = Icons.Default.Cheese,
                    title = "Cheese Room",
                    description = "Value addition production facility"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                InfrastructureCard(
                    icon = Icons.Default.Compost,
                    title = "Compost Pits",
                    description = "Manure composting areas"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                InfrastructureCard(
                    icon = Icons.Default.Agriculture,
                    title = "16 Plots",
                    description = "Crop cultivation areas"
                )
            }
        }
    }
}

@Composable
private fun InfrastructureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}