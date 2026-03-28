package com.shambasmart.presentation.ml.npu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.ml.DelegateConfig
import com.shambasmart.ml.ModelInfo
import com.shambasmart.ml.NpuCapabilities
import com.shambasmart.ml.PerformanceMetrics
import com.shambasmart.ml.QuantizationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpuConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: NpuConfigViewModel = hiltViewModel()
) {
    val npuCapabilities by viewModel.npuCapabilities.collectAsStateWithLifecycle()
    val delegateConfig by viewModel.delegateConfig.collectAsStateWithLifecycle()
    val performanceMetrics by viewModel.performanceMetrics.collectAsStateWithLifecycle()
    val isProfiling by viewModel.isProfiling.collectAsStateWithLifecycle()
    val modelInfo by viewModel.modelInfo.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NPU Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Delegate Selection & Optimization",
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
                    IconButton(onClick = { viewModel.runBenchmark() }) {
                        Icon(Icons.Default.Speed, contentDescription = "Benchmark")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                
                // NPU Capabilities Card
                NpuCapabilitiesCard(capabilities = npuCapabilities)
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Delegate Selection Card
                DelegateSelectionCard(
                    config = delegateConfig,
                    capabilities = npuCapabilities,
                    onConfigChange = { viewModel.updateDelegateConfig(it) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Model Quantization Card
                ModelQuantizationCard(
                    onQuantize = { viewModel.quantizeModel(it) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Performance Overlay Card
                PerformanceOverlayCard(
                    metrics = performanceMetrics,
                    isProfiling = isProfiling,
                    onToggleProfiling = { viewModel.toggleProfiling() }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Model Info Card
                modelInfo?.let { info ->
                    ModelInfoCard(info = info)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Battery Impact Card
                BatteryImpactCard(metrics = performanceMetrics)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun NpuCapabilitiesCard(capabilities: NpuCapabilities) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Device Capabilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CapabilityChip(
                    label = "QNN",
                    available = capabilities.hasQnn,
                    color = if (capabilities.hasQnn) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                )
                
                CapabilityChip(
                    label = "NNAPI",
                    available = capabilities.hasNnapi,
                    color = if (capabilities.hasNnapi) Color(0xFF2196F3) else Color(0xFF9E9E9E)
                )
                
                CapabilityChip(
                    label = "GPU",
                    available = capabilities.hasGpu,
                    color = if (capabilities.hasGpu) Color(0xFFFF9800) else Color(0xFF9E9E9E)
                )
                
                CapabilityChip(
                    label = "XNNPACK",
                    available = capabilities.hasXnnpack,
                    color = if (capabilities.hasXnnpack) Color(0xFF9C27B0) else Color(0xFF9E9E9E)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = capabilities.recommendedDelegate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CapabilityChip(
    label: String,
    available: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (available) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun DelegateSelectionCard(
    config: DelegateConfig,
    capabilities: NpuCapabilities,
    onConfigChange: (DelegateConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Delegate Selection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // QNN Toggle
            DelegateToggle(
                label = "Qualcomm QNN",
                description = "Neural Network SDK for Snapdragon devices",
                enabled = capabilities.hasQnn,
                checked = config.useQnn,
                onCheckedChange = { onConfigChange(config.copy(useQnn = it)) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // NNAPI Toggle
            DelegateToggle(
                label = "Android NNAPI",
                description = "Neural Networks API for hardware acceleration",
                enabled = capabilities.hasNnapi,
                checked = config.useNnapi,
                onCheckedChange = { onConfigChange(config.copy(useNnapi = it)) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GPU Toggle
            DelegateToggle(
                label = "GPU Acceleration",
                description = "Use GPU for inference",
                enabled = capabilities.hasGpu,
                checked = config.useGpu,
                onCheckedChange = { onConfigChange(config.copy(useGpu = it)) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // XNNPACK Toggle
            DelegateToggle(
                label = "XNNPACK",
                description = "Optimized CPU inference engine",
                enabled = capabilities.hasXnnpack,
                checked = config.useXnnpack,
                onCheckedChange = { onConfigChange(config.copy(useXnnpack = it)) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Thread Count Slider
            Text(
                text = "CPU Threads: ${config.numThreads}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Slider(
                value = config.numThreads.toFloat(),
                onValueChange = { onConfigChange(config.copy(numThreads = it.toInt())) },
                valueRange = 1f..4f,
                steps = 2,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "More threads = faster, but higher battery usage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DelegateToggle(
    label: String,
    description: String,
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked && enabled,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun ModelQuantizationCard(
    onQuantize: (QuantizationType) -> Unit
) {
    var selectedType by remember { mutableStateOf(QuantizationType.INT8) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Model Quantization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Reduce model size and improve performance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuantizationChip(
                    type = QuantizationType.INT4,
                    label = "INT4",
                    description = "Smallest",
                    isSelected = selectedType == QuantizationType.INT4,
                    onClick = { selectedType = QuantizationType.INT4 }
                )
                
                QuantizationChip(
                    type = QuantizationType.INT8,
                    label = "INT8",
                    description = "Balanced",
                    isSelected = selectedType == QuantizationType.INT8,
                    onClick = { selectedType = QuantizationType.INT8 }
                )
                
                QuantizationChip(
                    type = QuantizationType.FLOAT16,
                    label = "FP16",
                    description = "Highest",
                    isSelected = selectedType == QuantizationType.FLOAT16,
                    onClick = { selectedType = QuantizationType.FLOAT16 }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onQuantize(selectedType) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Compress,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply ${selectedType.name} Quantization")
            }
        }
    }
}

@Composable
private fun QuantizationChip(
    type: QuantizationType,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PerformanceOverlayCard(
    metrics: PerformanceMetrics?,
    isProfiling: Boolean,
    onToggleProfiling: () -> Unit
) {
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
                    text = "Performance Overlay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isProfiling,
                    onCheckedChange = { onToggleProfiling() }
                )
            }
            
            if (isProfiling && metrics != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                PerformanceMetricRow(
                    label = "Inference Time",
                    value = "${metrics.inferenceTimeMs} ms",
                    icon = Icons.Default.Timer
                )
                
                PerformanceMetricRow(
                    label = "FPS",
                    value = "%.1f".format(metrics.fps),
                    icon = Icons.Default.Speed
                )
                
                PerformanceMetricRow(
                    label = "CPU Usage",
                    value = "%.1f%%".format(metrics.cpuUtilization),
                    icon = Icons.Default.Memory
                )
                
                PerformanceMetricRow(
                    label = "NPU Usage",
                    value = "%.1f%%".format(metrics.npuUtilization),
                    icon = Icons.Default.Psychology
                )
                
                PerformanceMetricRow(
                    label = "Memory",
                    value = "${metrics.memoryUsageMb} MB",
                    icon = Icons.Default.Storage
                )
                
                PerformanceMetricRow(
                    label = "Battery Impact",
                    value = "%.2f mAh".format(metrics.batteryImpactMah),
                    icon = Icons.Default.BatteryFull
                )
            } else if (isProfiling) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Run inference to see performance metrics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModelInfoCard(info: ModelInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Model Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Model", info.modelName)
            InfoRow("Input Size", "${info.inputSize}")
            InfoRow("Output Size", "${info.outputSize}")
            InfoRow("Quantization", info.quantizationType)
            InfoRow("Delegate", info.delegateUsed)
            InfoRow("Optimized", if (info.isOptimized) "Yes" else "No")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BatteryImpactCard(metrics: PerformanceMetrics?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Battery Optimization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Current delegate: ${metrics?.let { "Active" } ?: "Idle"}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Average battery impact: ${metrics?.batteryImpactMah?.let { "%.2f mAh".format(it) } ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tip: Use XNNPACK for best balance of speed and battery life",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}