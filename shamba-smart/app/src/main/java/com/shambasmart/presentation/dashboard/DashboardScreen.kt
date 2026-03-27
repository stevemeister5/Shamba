package com.shambasmart.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.MaarifaFloatingTab
import com.shambasmart.maarifa.ui.MaarifaSidePanel
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    maarifaViewModel: MaarifaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val herdSize by viewModel.herdSize.collectAsStateWithLifecycle()
    val goatCount by viewModel.goatCount.collectAsStateWithLifecycle()
    val sheepCount by viewModel.sheepCount.collectAsStateWithLifecycle()
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    
    // Maarifa state
    val maarifaUiState by maarifaViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        // Main Content Area
        Row(modifier = Modifier.fillMaxSize()) {
            // Main Dashboard Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Dashboard Header
                DashboardHeader()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // KPI Strip (5 horizontal KPIs)
                KPIStrip(
                    herdSize = herdSize,
                    milkToday = dashboardData?.today_milk ?: 0.0,
                    cheeseStock = 0,
                    monthRevenue = dashboardData?.month_revenue ?: 0.0,
                    openTasks = dashboardData?.pending_tasks ?: 0
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Three Column Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column (4/12) - Maarifa Briefing & Weather
                    Column(modifier = Modifier.weight(4f)) {
                        // Morning Briefing Card
                        MorningBriefingCard()
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 5-Day Weather Strip
                        WeatherStrip()
                    }
                    
                    // Center Column (5/12) - Milk Production & Alerts
                    Column(modifier = Modifier.weight(5f)) {
                        // Milk Production Card
                        MilkProductionCard()
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Alerts Panel
                        AlertsPanel(
                            hasAlerts = uiState.hasAlerts,
                            pendingTasks = dashboardData?.pending_tasks ?: 0,
                            lowFeedAlerts = dashboardData?.low_feed_alerts ?: 0
                        )
                    }
                    
                    // Right Column (3/12) - Tasks & Inventory
                    Column(modifier = Modifier.weight(3f)) {
                        // Today's Tasks Card
                        TodaysTasksCard(pendingTasks = dashboardData?.pending_tasks ?: 0)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cheese Inventory Summary
                        CheeseInventoryCard()
                    }
                }
            }
        }
        
        // Maarifa Side Panel
        MaarifaSidePanel(
            isOpen = maarifaUiState.isPanelOpen,
            selectedTab = maarifaUiState.selectedTab,
            onTabSelected = { maarifaViewModel.selectTab(it) },
            onClose = { maarifaViewModel.closePanel() },
            viewModel = maarifaViewModel
        )
        
        // Maarifa Floating Tab
        MaarifaFloatingTab(
            onClick = { maarifaViewModel.togglePanel() }
        )
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Farm Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                color = Neutral950
            )
            Text(
                text = "Welcome back to your war room",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        }
        
        // Farm Status Indicator
        FarmStatusIndicator(status = FarmStatus.HEALTHY)
    }
}

@Composable
private fun FarmStatusIndicator(status: FarmStatus) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val (backgroundColor, iconColor, statusText) = when (status) {
        FarmStatus.HEALTHY -> Triple(Green900, Green400, "Healthy")
        FarmStatus.ALERT -> Triple(Earth800, Earth300, "Alert")
        FarmStatus.CRITICAL -> Triple(Red600, Red300, "Critical")
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulse)
                .background(backgroundColor, CircleShape)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = iconColor
        )
    }
}

enum class FarmStatus {
    HEALTHY, ALERT, CRITICAL
}

@Composable
private fun KPIStrip(
    herdSize: Int,
    milkToday: Double,
    cheeseStock: Int,
    monthRevenue: Double,
    openTasks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KPIItem(
                icon = Icons.Outlined.Pets,
                label = "TOTAL ANIMALS",
                value = "$herdSize",
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            KPIItem(
                icon = Icons.Outlined.WaterDrop,
                label = "MILK TODAY",
                value = "${String.format("%.1f", milkToday)}L",
                valueColor = Teal400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            KPIItem(
                icon = Icons.Outlined.Inventory,
                label = "CHEESE STOCK",
                value = "$cheeseStock kg",
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            KPIItem(
                icon = Icons.Outlined.AttachMoney,
                label = "MONTH REVENUE",
                value = "TZS ${String.format("%,.0f", monthRevenue)}",
                valueColor = Green400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            KPIItem(
                icon = Icons.Outlined.Task,
                label = "OPEN TASKS",
                value = "$openTasks",
                valueColor = if (openTasks > 0) Amber400 else Neutral800,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KPIItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Neutral950
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Neutral600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Light,
                fontSize = 20.sp
            ),
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MorningBriefingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceRaised
        ),
        border = BorderStroke(1.dp, Green800),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Green950.copy(alpha = 0.3f),
                            SurfaceRaised
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Maarifa leaf icon
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = "Maarifa",
                        modifier = Modifier.size(20.dp),
                        tint = Green400
                    )
                    Text(
                        text = "MORNING BRIEFING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Green400
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Your herd is performing well today. Milk production is up 12% from yesterday. Consider checking the grazing area in Plot 3 - soil moisture levels indicate optimal conditions for planting.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Neutral950,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confidence indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(3.dp)
                            .background(Green500, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = "Rule-governed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral600
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherStrip() {
    val weatherData = listOf(
        WeatherDay("Today", Icons.Outlined.WbSunny, "24°", "0mm"),
        WeatherDay("Tue", Icons.Outlined.Cloud, "22°", "2mm"),
        WeatherDay("Wed", Icons.Outlined.WbSunny, "26°", "0mm"),
        WeatherDay("Thu", Icons.Outlined.Thunderstorm, "20°", "8mm"),
        WeatherDay("Fri", Icons.Outlined.WbSunny, "25°", "0mm")
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Neutral600
                )
                Text(
                    text = "5-DAY FORECAST",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weatherData.forEach { day ->
                    WeatherDayItem(day)
                }
            }
        }
    }
}

data class WeatherDay(
    val day: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val temp: String,
    val rain: String
)

@Composable
private fun WeatherDayItem(day: WeatherDay) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        Text(
            text = day.day,
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        Spacer(modifier = Modifier.height(6.dp))
        Icon(
            imageVector = day.icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Neutral800
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = day.temp,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral950
        )
        Text(
            text = day.rain,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Teal400
        )
    }
}

@Composable
private fun MilkProductionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MILK PRODUCTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "7-Day Overview",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                }
                
                // Mini chart
                MiniBarChart()
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Per-doe breakdown
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DoeProductionBar("Doe 001", 4.2f, 5.0f)
                DoeProductionBar("Doe 002", 3.8f, 5.0f)
                DoeProductionBar("Doe 003", 4.5f, 5.0f)
                DoeProductionBar("Doe 004", 2.1f, 5.0f)
            }
        }
    }
}

@Composable
private fun MiniBarChart() {
    val barData = listOf(0.8f, 0.9f, 0.7f, 1.0f, 0.85f, 0.95f, 1.0f)
    
    Row(
        modifier = Modifier
            .width(120.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barData.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(value)
                    .background(
                        color = if (index == barData.size - 1) Green400 else Green800,
                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                    )
            )
        }
    }
}

@Composable
private fun DoeProductionBar(name: String, current: Float, max: Float) {
    val progress = current / max
    val barColor = when {
        progress > 0.8f -> Teal400
        progress > 0.5f -> Green400
        else -> Amber400
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral600,
            modifier = Modifier.width(56.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(SurfaceSunken, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(3.dp))
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${String.format("%.1f", current)}L",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral950,
            modifier = Modifier.width(44.dp)
        )
    }
}

@Composable
private fun AlertsPanel(
    hasAlerts: Boolean,
    pendingTasks: Int,
    lowFeedAlerts: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALERTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                
                if (hasAlerts) {
                    Surface(
                        color = Red400.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Text(
                            text = "${pendingTasks + lowFeedAlerts}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Red300,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (!hasAlerts) {
                Text(
                    text = "No alerts today. Your farm is running smoothly!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800
                )
            } else {
                if (pendingTasks > 0) {
                    AlertItem(
                        icon = Icons.Outlined.Task,
                        text = "$pendingTasks pending task(s)",
                        type = AlertType.WARNING
                    )
                }
                if (lowFeedAlerts > 0) {
                    AlertItem(
                        icon = Icons.Outlined.Inventory,
                        text = "$lowFeedAlerts low feed stock alert(s)",
                        type = AlertType.CRITICAL
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    type: AlertType
) {
    val (backgroundColor, borderColor, iconColor) = when (type) {
        AlertType.CRITICAL -> Triple(
            Red400.copy(alpha = 0.05f),
            Red400.copy(alpha = 0.25f),
            Red300
        )
        AlertType.WARNING -> Triple(
            Amber400.copy(alpha = 0.04f),
            Amber400.copy(alpha = 0.2f),
            Amber300
        )
        AlertType.INFO -> Triple(
            Color(0xFF42A5F5).copy(alpha = 0.04f),
            Color(0xFF42A5F5).copy(alpha = 0.2f),
            Color(0xFF42A5F5)
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral950
            )
        }
    }
}

enum class AlertType {
    CRITICAL, WARNING, INFO
}

@Composable
private fun TodaysTasksCard(pendingTasks: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S TASKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                Text(
                    text = "$pendingTasks pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (pendingTasks == 0) {
                Text(
                    text = "No tasks pending for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800
                )
            } else {
                // Sample tasks
                TaskItem("Morning milking", true)
                TaskItem("Check Plot 3 irrigation", false)
                TaskItem("Feed inventory check", false)
            }
        }
    }
}

@Composable
private fun TaskItem(text: String, completed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = completed,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = Green500,
                uncheckedColor = Neutral400,
                checkmarkColor = Green950
            )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (completed) Neutral600 else Neutral950
        )
    }
}

@Composable
private fun CheeseInventoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "CHEESE INVENTORY",
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "0 active batches",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral800
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start your first cheese batch to begin tracking aging progress.",
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600
            )
        }
    }
}