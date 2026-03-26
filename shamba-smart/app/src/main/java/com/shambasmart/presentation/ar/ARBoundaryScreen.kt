package com.shambasmart.presentation.ar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

data class BoundaryPoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARBoundaryScreen(
    onNavigateBack: () -> Unit,
    onSaveBoundary: (List<BoundaryPoint>) -> Unit,
    viewModel: ARBoundaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AR Boundary Mapping",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.boundaryPoints.size} points marked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.Help, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // AR View Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ViewInAr,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AR Boundary Mapping",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Walk around your farm perimeter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap screen to mark boundary points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // GPS Status
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.isGPSEnabled) 
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else 
                                    Color(0xFFF44336).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (uiState.isGPSEnabled) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                    contentDescription = null,
                                    tint = if (uiState.isGPSEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isGPSEnabled) "GPS Active" else "GPS Inactive",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Current Location
                        if (uiState.currentLatitude != 0.0 && uiState.currentLongitude != 0.0) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Current Location",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "Lat: ${String.format("%.6f", uiState.currentLatitude)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Lng: ${String.format("%.6f", uiState.currentLongitude)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (uiState.currentAccuracy > 0) {
                                        Text(
                                            text = "Accuracy: ${String.format("%.1f", uiState.currentAccuracy)}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (uiState.currentAccuracy <= 5.0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Mark Point Button
                        Button(
                            onClick = { viewModel.markBoundaryPoint() },
                            enabled = uiState.isGPSEnabled && !uiState.isMarking,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            if (uiState.isMarking) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Marking...")
                            } else {
                                Icon(Icons.Default.AddLocation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark Boundary Point")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Boundary Points List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Boundary Points",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (uiState.boundaryPoints.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearBoundaryPoints() }) {
                                Text("Clear All")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.boundaryPoints.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No points marked yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn {
                            items(uiState.boundaryPoints) { point ->
                                BoundaryPointItem(
                                    point = point,
                                    onDelete = { viewModel.deleteBoundaryPoint(point.id) }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.undoLastPoint() },
                    enabled = uiState.boundaryPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Undo")
                }
                
                Button(
                    onClick = { onSaveBoundary(uiState.boundaryPoints) },
                    enabled = uiState.boundaryPoints.size >= 3,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Boundary")
                }
            }
        }
    }
    
    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("AR Mapping Guide") },
            text = {
                Column {
                    Text("How to map your farm boundary:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Ensure GPS is active and accurate")
                    Text("2. Walk to each corner of your farm")
                    Text("3. Tap 'Mark Boundary Point' at each corner")
                    Text("4. Mark at least 3 points to create a boundary")
                    Text("5. Walk the full perimeter for best accuracy")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tips:", fontWeight = FontWeight.Bold)
                    Text("• Wait for GPS accuracy < 5m before marking")
                    Text("• Mark points at distinct corners")
                    Text("• Complete the full loop for accurate area calculation")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun BoundaryPointItem(
    point: BoundaryPoint,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Point #${point.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.6f", point.latitude)}, ${String.format("%.6f", point.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}