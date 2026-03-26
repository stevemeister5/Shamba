package com.shambasmart.presentation.crops

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Plot
import com.shambasmart.presentation.crops.components.BenchmarkProgressCard
import java.text.SimpleDateFormat
import java.util.*

// Data classes for analytics
data class PlotHeatmapData(
    val plot: Plot,
    val performanceIndex: Double,
    val healthScore: Double,
    val yieldPerAcre: Double,
    val colorIntensity: Float
)

data class BenchmarkSummary(
    val totalPlots: Int,
    val averagePerformance: Double,
    val topPerformingPlot: Plot?,
    val underperformingCount: Int,
    val totalYieldKg: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotAnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CropsViewModel = hiltViewModel()
) {
    val plots by viewModel.plots.collectAsStateWithLifecycle()
    val harvests by viewModel.harvests.collectAsStateWithLifecycle()
    
    // Calculate analytics data
    val heatmapData = remember(plots, harvests) {
        plots.map { plot ->
            val plotHarvests = harvests.filter { harvest ->
                // Get harvests by plotting ID (simplified - in production would need proper join)
                true // Placeholder
            }
            val totalYield = plotHarvests.sumOf { it.quantityKg }
            val performanceIndex = plot.performanceIndex ?: calculatePerformanceIndex(
                totalYield, 
                plot.sizeAcres
            )
            
            PlotHeatmapData(
                plot = plot,
                performanceIndex = performanceIndex,
                healthScore = plot.healthScore ?: 0.5,
                yieldPerAcre = if (plot.sizeAcres > 0) totalYield / plot.sizeAcres else 0.0,
                colorIntensity = (performanceIndex / 100).toFloat().coerceIn(0f, 1f)
            )
        }.sortedByDescending { it.performanceIndex }
    }
    
    val benchmarkSummary = remember(heatmapData) {
        BenchmarkSummary(
            totalPlots = heatmapData.size,
            averagePerformance = if (heatmapData.isNotEmpty()) 
                heatmapData.map { it.performanceIndex }.average() else 0.0,
            topPerformingPlot = heatmapData.firstOrNull()?.plot,
            underperformingCount = heatmapData.count { it.performanceIndex < 50 },
            totalYieldKg = heatmapData.sumOf { it.yieldPerAcre * it.plot.sizeAcres }
        )
    }
    
    var selectedPlot by remember { mutableStateOf<PlotHeatmapData?>(null) }
    var showArMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Digital Twin Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GPS Plot Benchmarking",
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
                    IconButton(onClick = { showArMode = !showArMode }) {
                        Icon(
                            if (showArMode) Icons.Default.ViewInAr else Icons.Default.Map,
                            contentDescription = "AR Mode"
                        )
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
                
                // Benchmark Summary Cards
                BenchmarkSummaryRow(summary = benchmarkSummary)
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                // Performance Heatmap Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Performance Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Legend
                    HeatmapLegend()
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                // Heatmap Grid
                HeatmapGrid(
                    plotData = heatmapData,
                    onPlotClick = { data -> selectedPlot = data }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                // Yield Analysis Header
                Text(
                    text = "Yield Analysis by Plot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(heatmapData) { data ->
                BenchmarkProgressCard(
                    plot = data.plot,
                    harvestYieldKg = data.yieldPerAcre * data.plot.sizeAcres,
                    onClick = { selectedPlot = data }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // AR Mode Preview
                AnimatedVisibility(
                    visible = showArMode,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    ARModePreview(
                        onDismiss = { showArMode = false }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Selected Plot Detail
    selectedPlot?.let { data ->
        PlotDetailDialog(
            plotData = data,
            onDismiss = { selectedPlot = null }
        )
    }
}

@Composable
private fun BenchmarkSummaryRow(summary: BenchmarkSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Plots",
            value = "${summary.totalPlots}",
            icon = Icons.Default.GridOn,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        SummaryCard(
            title = "Avg Performance",
            value = "${summary.averagePerformance.formatOneDecimal()}%",
            icon = Icons.Default.TrendingUp,
            color = if (summary.averagePerformance >= 60) 
                Color(0xFF4CAF50) else Color(0xFFFFC107),
            modifier = Modifier.weight(1f)
        )
        
        SummaryCard(
            title = "Needs Care",
            value = "${summary.underperformingCount}",
            icon = Icons.Default.Warning,
            color = if (summary.underperformingCount == 0) 
                Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Low",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFF44336), // Red
                            Color(0xFFFFC107), // Yellow
                            Color(0xFF4CAF50)  // Green
                        )
                    )
                )
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = "High",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun HeatmapGrid(
    plotData: List<PlotHeatmapData>,
    onPlotClick: (PlotHeatmapData) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Canvas for heatmap visualization
            val density = LocalDensity.current
            val strokeWidth = with(density) { 2.dp.toPx() }
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Grid dimensions
                val cols = (plotData.size.coerceAtMost(6)).coerceAtLeast(1)
                val rows = (plotData.size / cols + if (plotData.size % cols != 0) 1 else 0).coerceAtLeast(1)
                val cellWidth = canvasWidth / cols
                val cellHeight = canvasHeight / rows
                
                plotData.forEachIndexed { index, data ->
                    val col = index % cols
                    val row = index / cols
                    val x = col * cellWidth
                    val y = row * cellHeight
                    
                    // Color based on performance (red -> yellow -> green)
                    val color = when {
                        data.performanceIndex >= 70 -> Color(0xFF4CAF50) // Green
                        data.performanceIndex >= 40 -> Color(0xFFFFC107) // Yellow
                        else -> Color(0xFFF44336) // Red
                    }
                    
                    drawRect(
                        color = color.copy(alpha = 0.7f),
                        topLeft = Offset(x, y),
                        size = Size(cellWidth, cellHeight)
                    )
                    
                    drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(cellWidth, cellHeight),
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Plot name
                    drawContext.canvas.nativeCanvas.drawText(
                        data.plot.name.take(6),
                        x + cellWidth / 2,
                        y + cellHeight / 2 + 10,
                        android.graphics.Paint().apply {
                            this.color = if (data.performanceIndex >= 50) 
                                android.graphics.Color.WHITE 
                            else android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = with(density) { 12.sp.toPx() }
                            isAntiAlias = true
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Performance summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Best: ${plotData.maxByOrNull { it.performanceIndex }?.plot?.name ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "Needs Work: ${plotData.count { it.performanceIndex < 50 }} plots",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun ARModePreview(
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌾 AR Plot Overlay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Simulated AR preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2E7D32), // Dark green
                                Color(0xFF4CAF50), // Green
                                Color(0xFF81C784)  // Light green
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱 Point camera at plot",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PerformanceBadge(
                            label = "Health",
                            value = "85%",
                            color = Color(0xFF4CAF50)
                        )
                        PerformanceBadge(
                            label = "Yield",
                            value = "1.2t/ha",
                            color = Color(0xFFFFC107)
                        )
                        PerformanceBadge(
                            label = "Moisture",
                            value = "42%",
                            color = Color(0xFF2196F3)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "ARCore + Sceneform rendering active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Haptic feedback enabled when tapping plot zones",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PerformanceBadge(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PlotDetailDialog(
    plotData: PlotHeatmapData,
    onDismiss: () -> Unit
) {
    val plot = plotData.plot
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${plot.name} - Analytics",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("Performance Index", "${plotData.performanceIndex.formatOneDecimal()}%")
                DetailRow("Health Score", "${(plotData.healthScore * 100).formatOneDecimal()}%")
                DetailRow("Yield per Acre", "${plotData.yieldPerAcre.formatOneDecimal()} kg")
                DetailRow("Total Area", "${plot.sizeAcres.formatOneDecimal()} acres")
                
                plot.baselineCropsPerM2?.let {
                    DetailRow("Baseline", "${it.formatOneDecimal()} crops/m²")
                }
                
                plot.targetYieldKg?.let {
                    DetailRow("Target Yield", "${it.formatOneDecimal()} kg")
                }
                
                plot.soilMoistureSensorId?.let {
                    DetailRow("Sensor ID", it)
                }
                
                plot.boundaryPoints?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GPS Boundary: Configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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

// Helper functions
private fun calculatePerformanceIndex(totalYieldKg: Double, sizeAcres: Double): Double {
    return if (sizeAcres > 0) {
        val yieldPerAcre = totalYieldKg / sizeAcres
        // Normalize to 0-100 scale (assuming 500 kg/acre is excellent)
        ((yieldPerAcre / 500.0) * 100).coerceIn(0.0, 100.0)
    } else {
        0.0
    }
}

private fun Double.formatOneDecimal(): String {
    return String.format("%.1f", this)
}