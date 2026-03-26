package com.shambasmart.ml.vision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionGradingScreen(
    viewModel: VisionGradingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Computer Vision Grading",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "HSV Colorimetric Analysis for Quality Grading",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Product Type Selection
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Product Type",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Crop types
                Text("Crops:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductType.values().filter { !it.name.startsWith("CHEESE") }.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedProduct == type,
                            onClick = { viewModel.setProductType(type) },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Cheese types
                Text("Cheese:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductType.values().filter { it.name.startsWith("CHEESE") }.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedProduct == type,
                            onClick = { viewModel.setProductType(type) },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera Placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Camera Preview")
                    Text(
                        "Point at ${uiState.selectedProduct.name.replace("_", " ")} for grading",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capture Button
        Button(
            onClick = { viewModel.captureAndAnalyze() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isAnalyzing
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing...")
            } else {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture & Grade")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Harvest Window Alert
        if (uiState.grade != null && uiState.isInHarvestWindow) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "✓ Harvest Window Active",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = uiState.harvestStatus,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results Section
        if (uiState.grade != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState.grade) {
                        "A" -> MaterialTheme.colorScheme.primaryContainer
                        "B" -> MaterialTheme.colorScheme.secondaryContainer
                        "C" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Grading Result", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.grade,
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Grade",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.0f", uiState.maturityScore)}%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Maturity",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        
                        Column {
                            Text(
                                text = "H: ${String.format("%.0f", uiState.hue)}°",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "S: ${String.format("%.0f", uiState.saturation * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "V: ${String.format("%.0f", uiState.value * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Premium indicator
                    if (uiState.premiumMultiplier > 1.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${((uiState.premiumMultiplier - 1) * 100).toInt()}% Price Premium",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFD700)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = uiState.analysis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QR Invoice Button
        if (uiState.grade != null) {
            Button(
                onClick = { viewModel.generateQRInvoice() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate QR Invoice")
            }
            
            if (uiState.qrData != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "QR Code Generated",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Contains: Grade, HSV values, maturity score, harvest window status, and digital signature",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Clear Button
        if (uiState.grade != null) {
            OutlinedButton(
                onClick = { viewModel.clearResults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze Another")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profit Impact Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Profit Impact", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Grade A produce commands 25-50% price premium",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Harvest window alerts prevent quality loss",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Digital grading certificate builds buyer trust",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• QR invoices enable traceability and branding",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // How It Works
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How It Works", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Select product type (crop or cheese)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "2. Capture image using Xiaomi Pad 7 camera",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "3. Convert to HSV color space for analysis",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "4. Compare against optimal maturity profiles",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "5. Generate grade and harvest window status",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "6. Create signed QR invoice with metadata",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}