package com.shambasmart.presentation.alerts

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.domain.model.Alert
import com.shambasmart.domain.model.AlertPriority
import com.shambasmart.domain.model.AlertType
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            AlertsHeader(
                activeAlertsCount = alerts.count { !it.isDismissed },
                onNavigateBack = onNavigateBack,
                onDismissAll = { viewModel.dismissAllAlerts() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Green400
                    )
                }
            } else if (alerts.isEmpty()) {
                EmptyAlertsState()
            } else {
                // Active Alerts
                val activeAlerts = alerts.filter { !it.isDismissed }
                val dismissedAlerts = alerts.filter { it.isDismissed }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeAlerts.isNotEmpty()) {
                        item {
                            Text(
                                text = "ACTIVE ALERTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                        }
                        
                        items(activeAlerts) { alert ->
                            AlertCard(
                                alert = alert,
                                onDismiss = { viewModel.dismissAlert(alert.id) }
                            )
                        }
                    }
                    
                    if (dismissedAlerts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "DISMISSED ALERTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                        }
                        
                        items(dismissedAlerts) { alert ->
                            AlertCard(
                                alert = alert,
                                onDismiss = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertsHeader(
    activeAlertsCount: Int,
    onNavigateBack: () -> Unit,
    onDismissAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Neutral800
                )
            }
            
            Column {
                Text(
                    text = "Alerts",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "$activeAlertsCount active alert(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
        }
        
        if (activeAlertsCount > 0) {
            OutlinedButton(
                onClick = onDismissAll,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Neutral800
                ),
                border = BorderStroke(1.dp, Neutral300),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DoneAll,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dismiss All",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    onDismiss: (() -> Unit)?
) {
    val (backgroundColor, borderColor, iconColor) = when (alert.priority) {
        AlertPriority.CRITICAL -> Triple(
            Red400.copy(alpha = 0.05f),
            Red400.copy(alpha = 0.25f),
            Red300
        )
        AlertPriority.HIGH -> Triple(
            Amber400.copy(alpha = 0.04f),
            Amber400.copy(alpha = 0.2f),
            Amber300
        )
        AlertPriority.MEDIUM -> Triple(
            Amber400.copy(alpha = 0.03f),
            Amber400.copy(alpha = 0.15f),
            Amber400
        )
        AlertPriority.LOW -> Triple(
            Color(0xFF42A5F5).copy(alpha = 0.04f),
            Color(0xFF42A5F5).copy(alpha = 0.2f),
            Color(0xFF42A5F5)
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAlertIcon(alert.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    
                    // Priority Badge
                    Surface(
                        color = iconColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, iconColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = alert.priority.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral800,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = formatTimestamp(alert.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }
            
            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(18.dp),
                        tint = Neutral600
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAlertsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Green900),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Green400
                )
            }
            
            Text(
                text = "All Clear!",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
            
            Text(
                text = "No active alerts. Your farm is running smoothly.",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        }
    }
}

private fun getAlertIcon(type: AlertType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        AlertType.VACCINATION_OVERDUE -> Icons.Outlined.MedicalServices
        AlertType.ANIMAL_NOT_WEIGHED -> Icons.Outlined.MonitorWeight
        AlertType.LOW_FEED_STOCK -> Icons.Outlined.Inventory
        AlertType.HARVEST_READY -> Icons.Outlined.Grass
        AlertType.CHEESE_AGING_COMPLETE -> Icons.Outlined.LunchDining
        AlertType.LOAN_REPAYMENT_DUE -> Icons.Outlined.Payment
        AlertType.MAINTENANCE_DUE -> Icons.Outlined.Build
        AlertType.WEATHER_WARNING -> Icons.Outlined.Cloud
        AlertType.FINANCIAL_THRESHOLD -> Icons.Outlined.AttachMoney
        AlertType.TASK_OVERDUE -> Icons.Outlined.AssignmentLate
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}