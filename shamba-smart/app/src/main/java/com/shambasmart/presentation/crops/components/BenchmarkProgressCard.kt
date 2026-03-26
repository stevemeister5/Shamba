package com.shambasmart.presentation.crops.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shambasmart.data.local.entity.Plot
import kotlin.math.roundToInt

data class BenchmarkData(
    val plotName: String,
    val currentYieldKg: Double,
    val baselineYieldKg: Double,
    val targetYieldKg: Double,
    val sizeAcres: Double,
    val healthScore: Double,
    val performanceIndex: Double
)

@Composable
fun BenchmarkProgressCard(
    plot: Plot,
    harvestYieldKg: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val benchmarkData = remember(plot, harvestYieldKg) {
        val baselineYield = plot.baselineCropsPerM2?.let { 
            it * plot.sizeAcres * 4047.0 // Convert acres to m²
        } ?: 0.0
        
        val targetYield = plot.targetYieldKg ?: (baselineYield * 1.2) // 20% improvement target
        
        BenchmarkData(
            plotName = plot.name,
            currentYieldKg = harvestYieldKg,
            baselineYieldKg = baselineYield,
            targetYieldKg = targetYield,
            sizeAcres = plot.sizeAcres,
            healthScore = plot.healthScore ?: 0.0,
            performanceIndex = plot.performanceIndex ?: calculatePerformanceIndex(harvestYieldKg, baselineYield)
        )
    }
    
    val deltaPercentage = remember(benchmarkData) {
        if (benchmarkData.baselineYieldKg > 0) {
            ((benchmarkData.currentYieldKg - benchmarkData.baselineYieldKg) / benchmarkData.baselineYieldKg) * 100
        } else {
            0.0
        }
    }
    
    val progressAnimated by animateFloatAsState(
        targetValue = if (benchmarkData.targetYieldKg > 0) {
            (benchmarkData.currentYieldKg / benchmarkData.targetYieldKg).toFloat().coerceIn(0f, 1f)
        } else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = benchmarkData.plotName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                HealthBadge(score = benchmarkData.healthScore)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Ring and Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularBenchmarkIndicator(
                        progress = progressAnimated,
                        deltaPercentage = deltaPercentage
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Metrics Column
                Column(modifier = Modifier.weight(1f)) {
                    YieldMetricRow(
                        label = "Current",
                        value = benchmarkData.currentYieldKg,
                        unit = "kg"
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    YieldMetricRow(
                        label = "Baseline",
                        value = benchmarkData.baselineYieldKg,
                        unit = "kg"
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    YieldMetricRow(
                        label = "Target",
                        value = benchmarkData.targetYieldKg,
                        unit = "kg"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Performance Index Bar
            PerformanceIndexBar(
                performanceIndex = benchmarkData.performanceIndex
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Delta Badge
            DeltaBadge(deltaPercentage = deltaPercentage)
        }
    }
}

@Composable
private fun CircularBenchmarkIndicator(
    progress: Float,
    deltaPercentage: Double,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        deltaPercentage >= 10 -> Color(0xFF4CAF50) // Green - above target
        deltaPercentage >= 0 -> Color(0xFFFFC107) // Yellow - near target
        else -> Color(0xFFF44336) // Red - below baseline
    }
    
    val strokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
    
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                color = indicatorColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = indicatorColor
            )
            Text(
                text = "of target",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun HealthBadge(
    score: Double,
    modifier: Modifier = Modifier
) {
    val (color, text) = when {
        score >= 0.8 -> Color(0xFF4CAF50) to "Excellent"
        score >= 0.6 -> Color(0xFFFFC107) to "Good"
        score >= 0.4 -> Color(0xFFFF9800) to "Fair"
        else -> Color(0xFFF44336) to "Needs Care"
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun YieldMetricRow(
    label: String,
    value: Double,
    unit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = "${value.formatOneDecimal()} $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PerformanceIndexBar(
    performanceIndex: Double,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (performanceIndex / 100).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "performance"
    )
    
    val barColor = when {
        performanceIndex >= 80 -> Color(0xFF4CAF50)
        performanceIndex >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Performance Index",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = "${performanceIndex.formatOneDecimal()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun DeltaBadge(
    deltaPercentage: Double,
    modifier: Modifier = Modifier
) {
    val isPositive = deltaPercentage >= 0
    val badgeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val indicator = if (isPositive) "▲" else "▼"
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.1f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$indicator ${if (isPositive) "+" else ""}${deltaPercentage.formatOneDecimal()}% vs baseline",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = badgeColor,
            textAlign = TextAlign.Center
        )
    }
}

// Helper function
private fun Double.formatOneDecimal(): String {
    return String.format("%.1f", this)
}

private fun calculatePerformanceIndex(currentYield: Double, baselineYield: Double): Double {
    return if (baselineYield > 0) {
        (currentYield / baselineYield * 100).coerceIn(0.0, 100.0)
    } else {
        0.0
    }
}