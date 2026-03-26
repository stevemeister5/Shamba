package com.shambasmart.presentation.maintenance

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.MaintenanceTask
import com.shambasmart.data.local.entity.MaintenanceType
import com.shambasmart.data.local.entity.MaintenancePriority
import com.shambasmart.data.local.entity.MaintenanceStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val overdueCount by viewModel.overdueCount.collectAsStateWithLifecycle()
    val upcomingCount by viewModel.upcomingCount.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<MaintenanceTask?>(null) }
    var selectedFilter by remember { mutableStateOf<MaintenanceType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Maintenance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$upcomingCount upcoming • $overdueCount overdue",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (overdueCount > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
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
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All") }
                )
                MaintenanceType.values().take(4).forEach { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = type },
                        label = { Text(type.name.replace("_", " ").take(10)) }
                    )
                }
            }

            // Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Overdue",
                    count = overdueCount,
                    color = Color(0xFFF44336),
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Upcoming",
                    count = upcomingCount,
                    color = Color(0xFFFFC107),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Completed",
                    count = tasks.count { it.status == MaintenanceStatus.COMPLETED },
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tasks List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredTasks = if (selectedFilter != null) {
                    tasks.filter { it.type == selectedFilter }
                } else {
                    tasks
                }

                if (filteredTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No maintenance tasks",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTasks) { task ->
                        MaintenanceTaskCard(
                            task = task,
                            onClick = { selectedTask = task },
                            onStatusChange = { newStatus ->
                                viewModel.updateTaskStatus(task.id, newStatus)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        AddMaintenanceTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { task ->
                viewModel.addTask(task)
                showAddDialog = false
            }
        )
    }

    // Task Detail Dialog
    selectedTask?.let { task ->
        MaintenanceTaskDetailDialog(
            task = task,
            onDismiss = { selectedTask = null },
            onStatusChange = { newStatus ->
                viewModel.updateTaskStatus(task.id, newStatus)
                selectedTask = null
            }
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    count: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                text = "$count",
                style = MaterialTheme.typography.headlineSmall,
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
private fun MaintenanceTaskCard(
    task: MaintenanceTask,
    onClick: () -> Unit,
    onStatusChange: (MaintenanceStatus) -> Unit
) {
    val priorityColor = when (task.priority) {
        MaintenancePriority.CRITICAL -> Color(0xFFF44336)
        MaintenancePriority.HIGH -> Color(0xFFFF9800)
        MaintenancePriority.MEDIUM -> Color(0xFFFFC107)
        MaintenancePriority.LOW -> Color(0xFF4CAF50)
    }

    val statusColor = when (task.status) {
        MaintenanceStatus.SCHEDULED -> Color(0xFF2196F3)
        MaintenanceStatus.IN_PROGRESS -> Color(0xFFFF9800)
        MaintenanceStatus.COMPLETED -> Color(0xFF4CAF50)
        MaintenanceStatus.CANCELLED -> Color(0xFF9E9E9E)
        MaintenanceStatus.OVERDUE -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getMaintenanceIcon(task.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(task.scheduledDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (task.assignedTo != null) {
                        Text(
                            text = "Assigned: ${task.assignedTo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMaintenanceTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (MaintenanceTask) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MaintenanceType.PREVENTIVE_MAINTENANCE) }
    var selectedPriority by remember { mutableStateOf(MaintenancePriority.MEDIUM) }
    var scheduledDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var assignedTo by remember { mutableStateOf("") }
    var estimatedHours by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Type Selection
                Text("Type:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MaintenanceType.values().take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.replace("_", " ").take(8)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Priority Selection
                Text("Priority:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MaintenancePriority.values().forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Assigned To") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = estimatedHours,
                    onValueChange = { estimatedHours = it },
                    label = { Text("Estimated Hours") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        MaintenanceTask(
                            title = title,
                            description = description,
                            type = selectedType,
                            priority = selectedPriority,
                            scheduledDate = scheduledDate,
                            assignedTo = assignedTo.ifBlank { null },
                            estimatedDurationHours = estimatedHours.toDoubleOrNull() ?: 1.0
                        )
                    )
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MaintenanceTaskDetailDialog(
    task: MaintenanceTask,
    onDismiss: () -> Unit,
    onStatusChange: (MaintenanceStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.title) },
        text = {
            Column {
                DetailRow("Type", task.type.name.replace("_", " "))
                DetailRow("Priority", task.priority.name)
                DetailRow("Status", task.status.name)
                DetailRow("Scheduled", formatTimestamp(task.scheduledDate))
                task.assignedTo?.let { DetailRow("Assigned To", it) }
                DetailRow("Est. Hours", "${task.estimatedDurationHours}")
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Description:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(task.description, style = MaterialTheme.typography.bodySmall)
                }

                if (task.status == MaintenanceStatus.SCHEDULED || task.status == MaintenanceStatus.IN_PROGRESS) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Update Status:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (task.status == MaintenanceStatus.SCHEDULED) {
                            Button(
                                onClick = { onStatusChange(MaintenanceStatus.IN_PROGRESS) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start")
                            }
                        }
                        Button(
                            onClick = { onStatusChange(MaintenanceStatus.COMPLETED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Complete")
                        }
                    }
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

private fun getMaintenanceIcon(type: MaintenanceType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        MaintenanceType.DIPPING_TANK_CLEANING -> Icons.Default.WaterDrop
        MaintenanceType.EQUIPMENT_SERVICING -> Icons.Default.Build
        MaintenanceType.INFRASTRUCTURE_REPAIR -> Icons.Default.HomeRepairService
        MaintenanceType.WATER_SYSTEM_MAINTENANCE -> Icons.Default.Water
        MaintenanceType.SHELTER_CLEANING -> Icons.Default.CleaningServices
        MaintenanceType.FENCE_REPAIR -> Icons.Default.Fence
        MaintenanceType.TOOL_MAINTENANCE -> Icons.Default.Handyman
        MaintenanceType.PREVENTIVE_MAINTENANCE -> Icons.Default.Settings
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}