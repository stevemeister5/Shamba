package com.shambasmart.presentation.ml.acoustic

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.AudioEvent
import com.shambasmart.data.local.entity.SoundClasses
import com.shambasmart.ml.AudioClassificationResult
import com.shambasmart.ml.SpectrogramData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioAlertScreen(
    onNavigateBack: () -> Unit,
    viewModel: AudioAlertViewModel = hiltViewModel()
) {
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val lastClassification by viewModel.lastClassification.collectAsStateWithLifecycle()
    val spectrogram by viewModel.spectrogram.collectAsStateWithLifecycle()
    val recentEvents by viewModel.recentEvents.collectAsStateWithLifecycle()
    val isCalibrating by viewModel.isCalibrating.collectAsStateWithLifecycle()
    val batteryOptimized by viewModel.batteryOptimized.collectAsStateWithLifecycle()

    var showCalibrationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Acoustic Guard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sound Classification",
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
                    IconButton(onClick = { viewModel.toggleBatteryOptimization() }) {
                        Icon(
                            if (batteryOptimized) Icons.Default.BatterySaver else Icons.Default.BatteryFull,
                            contentDescription = "Battery Mode"
                        )
                    }
                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isListening) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Listening Status
                ListeningStatusCard(
                    isListening = isListening,
                    batteryOptimized = batteryOptimized,
                    onToggleListening = { viewModel.toggleListening() }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Spectrogram Visualization
                SpectrogramCard(spectrogram = spectrogram)
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Last Classification Result
                lastClassification?.let { result ->
                    ClassificationResultCard(result = result)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Quick Statistics
                QuickStatsCard(viewModel = viewModel)
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Recent Events Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Audio Events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.clearEvents() }) {
                        Text("Clear All")
                    }
                }
            }

            if (recentEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.MicOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No audio events recorded",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(recentEvents) { event ->
                    AudioEventCard(event = event)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Calibration Dialog
    if (showCalibrationDialog) {
        CalibrationDialog(
            onDismiss = { showCalibrationDialog = false },
            onCalibrate = { viewModel.startCalibration() },
            isCalibrating = isCalibrating
        )
    }
}

@Composable
private fun ListeningStatusCard(
    isListening: Boolean,
    batteryOptimized: Boolean,
    onToggleListening: () -> Unit
) {
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Microphone Icon with Animation
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isListening) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isListening) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isListening) "Listening..." else "Not Listening",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (batteryOptimized) "Battery Optimized Mode" else "High Performance Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggleListening,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) 
                        MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (isListening) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isListening) "Stop Listening" else "Start Listening")
            }
        }
    }
}

@Composable
private fun SpectrogramCard(spectrogram: SpectrogramData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spectrogram",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (spectrogram != null) {
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spectrogram Visualization
            val density = LocalDensity.current
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
            ) {
                if (spectrogram != null) {
                    drawSpectrogram(spectrogram)
                } else {
                    // Placeholder when no data
                    val paint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        alpha = 128
                        textSize = with(density) { 14.sp.toPx() }
                        typeface = Typeface.DEFAULT
                        textAlign = Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "Waiting for audio input...",
                        size.width / 2,
                        size.height / 2,
                        paint
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpectrogramLegendItem("0 Hz", Color(0xFF0000FF))
                SpectrogramLegendItem("4 kHz", Color(0xFF00FF00))
                SpectrogramLegendItem("8 kHz", Color(0xFFFFFF00))
                SpectrogramLegendItem("16 kHz", Color(0xFFFF0000))
            }
        }
    }
}

@Composable
private fun SpectrogramLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun DrawScope.drawSpectrogram(spectrogram: SpectrogramData) {
    val width = size.width
    val height = size.height
    val frequencies = spectrogram.frequencies
    val magnitudes = spectrogram.magnitudes

    if (frequencies.isEmpty() || magnitudes.isEmpty()) return

    // Normalize magnitudes
    val maxMagnitude = magnitudes.maxOrNull() ?: 1f
    val normalizedMagnitudes = magnitudes.map { it / maxMagnitude }

    // Draw frequency bins as colored bars
    val barWidth = width / frequencies.size
    frequencies.forEachIndexed { index, frequency ->
        val magnitude = normalizedMagnitudes.getOrElse(index) { 0f }
        val barHeight = magnitude * height

        // Color based on frequency
        val color = when {
            frequency < 4000 -> Color(0xFF0000FF).copy(alpha = magnitude) // Blue for low
            frequency < 8000 -> Color(0xFF00FF00).copy(alpha = magnitude) // Green for mid
            frequency < 12000 -> Color(0xFFFFFF00).copy(alpha = magnitude) // Yellow for high-mid
            else -> Color(0xFFFF0000).copy(alpha = magnitude) // Red for high
        }

        drawRect(
            color = color,
            topLeft = Offset(index * barWidth, height - barHeight),
            size = androidx.compose.ui.geometry.Size(barWidth - 1, barHeight)
        )
    }
}

@Composable
private fun ClassificationResultCard(result: AudioClassificationResult) {
    val isDistress = SoundClasses.isDistressEvent(result.soundClass)
    val backgroundColor = if (isDistress) 
        MaterialTheme.colorScheme.errorContainer 
    else MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last Classification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isDistress) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text("DISTRESS")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatSoundClass(result.soundClass),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Confidence: ${(result.confidence * 100).formatOneDecimal()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Confidence indicator
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (result.confidence > 0.8) Color(0xFF4CAF50)
                            else if (result.confidence > 0.5) Color(0xFFFFC107)
                            else Color(0xFFF44336)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(result.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatTimestamp(result.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickStatsCard(viewModel: AudioAlertViewModel) {
    val totalEvents by viewModel.totalEvents.collectAsStateWithLifecycle()
    val distressCount by viewModel.distressCount.collectAsStateWithLifecycle()
    val averageConfidence by viewModel.averageConfidence.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Events",
                    value = "$totalEvents",
                    icon = Icons.Default.GraphicEq,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    label = "Distress",
                    value = "$distressCount",
                    icon = Icons.Default.Warning,
                    color = if (distressCount > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                )
                
                StatItem(
                    label = "Avg Confidence",
                    value = "${(averageConfidence * 100).formatOneDecimal()}%",
                    icon = Icons.Default.Speed,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AudioEventCard(event: AudioEvent) {
    val isDistress = SoundClasses.isDistressEvent(event.soundClass)
    val backgroundColor = if (isDistress) 
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.surface
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDistress) 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (event.soundClass) {
                        SoundClasses.GOAT_BLEAT, SoundClasses.GOAT_DISTRESS -> Icons.Default.Pets
                        SoundClasses.SHEEP_BLEAT, SoundClasses.SHEEP_DISTRESS -> Icons.Default.Pets
                        SoundClasses.CATTLE_MOO, SoundClasses.CATTLE_DISTRESS -> Icons.Default.Pets
                        SoundClasses.CHICKEN_CLUCK, SoundClasses.CHICKEN_DISTRESS -> Icons.Default.Pets
                        SoundClasses.PREDATOR_DOG, SoundClasses.PREDATOR_HYENA -> Icons.Default.Warning
                        SoundClasses.RAIN_HEAVY, SoundClasses.THUNDER -> Icons.Default.Thunderstorm
                        SoundClasses.WIND_STRONG -> Icons.Default.Air
                        SoundClasses.MACHINERY -> Icons.Default.Build
                        SoundClasses.HUMAN_VOICE -> Icons.Default.Person
                        SoundClasses.GATE_OPEN, SoundClasses.GATE_CLOSE -> Icons.Default.DoorFront
                        SoundClasses.WATER_RUNNING -> Icons.Default.WaterDrop
                        SoundClasses.FOOD_DISPENSER -> Icons.Default.Restaurant
                        else -> Icons.Default.GraphicEq
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isDistress) 
                        MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatSoundClass(event.soundClass),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Confidence: ${(event.confidence * 100).formatOneDecimal()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = formatTimestamp(event.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isDistress) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text("ALERT")
                }
            }
        }
    }
}

@Composable
private fun CalibrationDialog(
    onDismiss: () -> Unit,
    onCalibrate: () -> Unit,
    isCalibrating: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Audio Calibration")
        },
        text = {
            Column {
                if (isCalibrating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recording sample...")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Please make sounds you want to detect (e.g., goat bleats)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Record a 3-second audio sample to calibrate the detection threshold. This helps reduce false positives.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Stand near your livestock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Make typical farm sounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Keep environment as quiet as possible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (!isCalibrating) {
                Button(onClick = onCalibrate) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Sample")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions
private fun formatSoundClass(soundClass: String): String {
    return soundClass.split("_").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun Double.formatOneDecimal(): String {
    return String.format("%.1f", this)
}
