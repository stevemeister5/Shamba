package com.shambasmart.ml.vision

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionGradingScreen(
    viewModel: VisionGradingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // Permission granted, camera will be set up in the preview
        }
    }

    // Determine if current product uses HSV or time-based grading
    val usesHSV = uiState.selectedProduct in listOf(
        ProductType.TOMATO, ProductType.ONION, ProductType.KALE,
        ProductType.CHEESE_FRESH, ProductType.CHEESE_AGED
    )

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
            text = if (usesHSV) "HSV Colorimetric Analysis for Quality Grading" 
                   else "Time-Based Maturity Calculation",
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

                // Grading method indicator
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (usesHSV) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (usesHSV) Icons.Default.CameraAlt else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (usesHSV) "Grading Method: HSV Color Analysis" 
                                   else "Grading Method: Time-Based Maturity",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera Preview or Placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            if (usesHSV) {
                // HSV products - show camera
                if (!hasCameraPermission) {
                    // Request camera permission
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Camera permission required")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                } else {
                    // Camera preview
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    viewModel.setupCamera(lifecycleOwner, this)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Camera status overlay
                        if (!uiState.isCameraReady) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Initializing camera...", color = Color.White)
                                }
                            }
                        }

                        // Camera error overlay
                        if (uiState.cameraError != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        uiState.cameraError ?: "Camera error",
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.clearCameraError() }) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Time-based products - show placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Time-Based Grading")
                        Text(
                            "Maturity calculated from planting date",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capture/Analyze Button
        Button(
            onClick = { viewModel.captureAndAnalyze() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isAnalyzing && (if (usesHSV) uiState.isCameraReady else true)
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing...")
            } else {
                Icon(
                    if (usesHSV) Icons.Default.Camera else Icons.Default.Calculate,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (usesHSV) "Capture & Grade" else "Calculate Maturity")
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
        val grade = uiState.grade
        if (grade != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (grade) {
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
                                text = grade,
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
                        
                        if (usesHSV) {
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
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Grading method badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (usesHSV) Icons.Default.CameraAlt else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.gradingMethod ?: if (usesHSV) "HSV_ANALYSIS" else "TIME_BASED",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Premium indicator
                    if (uiState.premiumMultiplier > 1.0) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                            text = "Contains: Grade, ${if (usesHSV) "HSV values, " else ""}maturity score, harvest window status, and digital signature",
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
                if (usesHSV) {
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
                } else {
                    Text(
                        text = "1. Select product type (maize, beans, cassava)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2. Log planting date in Crop Management module",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "3. System calculates days since planting",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "4. Compare against crop-specific maturity days",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "5. Generate grade based on maturity percentage",
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
}

