package com.shambasmart.presentation.gps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GPSBoundaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: GPSBoundaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    // Clear messages after 3 seconds
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GPS Boundary Mapping",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Help")
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
            // Messages
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }

            if (uiState.successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.successMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Main GPS View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.GpsFixed,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "GPS Boundary Mapping",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Walk around your farm perimeter to map the boundary",
                            style = MaterialTheme.typography.bodyMedium,
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

                        // Current Location with Accuracy
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
                                    val accuracyColor = when {
                                        uiState.currentAccuracy <= 5.0 -> Color(0xFF4CAF50)
                                        uiState.currentAccuracy <= 10.0 -> Color(0xFFFF9800)
                                        else -> Color(0xFFF44336)
                                    }
                                    Text(
                                        text = "Accuracy: ${String.format("%.1f", uiState.currentAccuracy)}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = accuracyColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Mode Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Mark Point Button
                            Button(
                                onClick = { viewModel.markBoundaryPoint() },
                                enabled = uiState.isGPSEnabled && !uiState.isMarking && !uiState.isRecording,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isMarking) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sampling...")
                                } else {
                                    Icon(Icons.Default.AddLocation, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark Point")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Walking Mode Button
                        if (!uiState.isRecording) {
                            OutlinedButton(
                                onClick = { viewModel.startWalkingMode() },
                                enabled = uiState.isGPSEnabled && !uiState.isMarking,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Walking Mode")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.stopWalkingModeAndProcess() },
                                enabled = !uiState.isProcessing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF44336)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Processing...")
                                } else {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop & Process (${uiState.recordedLocations.size} points)")
                                }
                            }
                        }

                        // Area Calculation
                        if (uiState.areaAcres > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Calculated Area",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "${String.format("%.2f", uiState.areaAcres)} acres",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "(${String.format("%.1f", uiState.perimeterMeters)}m perimeter)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
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
                    .padding(horizontal = 16.dp)
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
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Undo")
                }

                Button(
                    onClick = { viewModel.saveBoundary() },
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
            title = { Text("GPS Boundary Mapping Guide") },
            text = {
                Column {
                    Text("How to map your farm boundary:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Ensure GPS is active and accuracy is good (<10m)")
                    Text("2. Walk to each corner of your farm")
                    Text("3. Tap 'Mark Boundary Point' at each corner")
                    Text("4. Or use 'Walking Mode' for continuous recording")
                    Text("5. Mark at least 3 points to create a boundary")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tips for better accuracy:", fontWeight = FontWeight.Bold)
                    Text("• Wait for GPS accuracy < 10m before marking")
                    Text("• Move to open area away from trees/buildings")
                    Text("• Use Walking Mode for better boundary shape")
                    Text("• The app uses Kalman filtering to smooth GPS noise")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GPS Accuracy Guide:", fontWeight = FontWeight.Bold)
                    Text("• Green (<10m): Good - mark points now")
                    Text("• Yellow (10-20m): Acceptable - wait if possible")
                    Text("• Red (>20m): Poor - move to open area")
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
                if (point.accuracy > 0) {
                    Text(
                        text = "Accuracy: ${String.format("%.1f", point.accuracy)}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (point.accuracy <= 10.0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
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