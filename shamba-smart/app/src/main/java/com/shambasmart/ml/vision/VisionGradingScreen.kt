package com.shambasmart.ml.vision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.IIcons
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
                        "Point at crop or cheese for grading",
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

        // Results Section
        if (uiState.grade != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState.grade) {
                        "A" -> MaterialTheme.colorScheme.primaryContainer
                        "B" -> MaterialTheme.colorScheme.secondaryContainer
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
                        Text(
                            text = "Grade: ${uiState.grade}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Hue: ${String.format("%.0f", uiState.hue)}°",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Saturation: ${String.format("%.0f", uiState.saturation * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Value: ${String.format("%.0f", uiState.value * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profit Impact Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Profit Impact", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Grade A produce commands 25%+ price premium",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Ensures quality consistency for buyers",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Digital grading certificate builds trust",
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
                    text = "• Captures image using Xiaomi Pad 7 camera",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Converts to HSV color space",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Analyzes color against 'Peak Value' database",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Generates signed QR invoice with metadata",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}