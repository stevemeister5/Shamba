package com.shambasmart.presentation.labour

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Worker
import com.shambasmart.data.local.entity.AttendanceRecord
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourScreen(
    viewModel: LabourViewModel = hiltViewModel()
) {
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val todayAttendance by viewModel.todayAttendance.collectAsStateWithLifecycle()
    val payrollData by viewModel.payrollData.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableStateOf("Workers") }
    var showAddWorker by remember { mutableStateOf(false) }
    var showAttendance by remember { mutableStateOf(false) }
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Labour Management",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Manage workers, attendance, and payroll",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Button(
                onClick = { showAddWorker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Worker",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Workers", "Attendance", "Payroll").forEach { tab ->
                TabButton(
                    text = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tab Content
        when (selectedTab) {
            "Workers" -> WorkersTab(
                workers = workers,
                onEditWorker = { selectedWorker = it },
                onDeleteWorker = { viewModel.deleteWorker(it) }
            )
            "Attendance" -> AttendanceTab(
                workers = workers,
                todayAttendance = todayAttendance,
                onMarkAttendance = { showAttendance = true }
            )
            "Payroll" -> PayrollTab(
                payrollEntries = payrollData
            )
        }
    }
    
    // Add Worker Dialog
    if (showAddWorker) {
        AddWorkerDialog(
            onDismiss = { showAddWorker = false },
            onSave = { worker ->
                viewModel.addWorker(worker)
                showAddWorker = false
            }
        )
    }
    
    // Attendance Dialog
    if (showAttendance) {
        AttendanceDialog(
            workers = workers,
            onDismiss = { showAttendance = false },
            onSave = { records ->
                records.forEach { viewModel.addAttendance(it) }
                showAttendance = false
            }
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Green800.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) Green300 else Neutral600
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun WorkersTab(
    workers: List<Worker>,
    onEditWorker: (Worker) -> Unit,
    onDeleteWorker: (Worker) -> Unit
) {
    if (workers.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.People,
            title = "No Workers Registered",
            description = "Add workers to start tracking attendance and payroll."
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Neutral200),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Neutral100.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral400,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "ROLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral400,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "RATE/DAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral400,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "PHONE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral400,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(0.5f))
                }
                
                // Table Rows
                workers.forEach { worker ->
                    WorkerRow(
                        worker = worker,
                        onEdit = { onEditWorker(worker) },
                        onDelete = { onDeleteWorker(worker) }
                    )
                    HorizontalDivider(
                        color = Neutral100,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerRow(
    worker: Worker,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = worker.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral950,
            modifier = Modifier.weight(2f)
        )
        
        Text(
            text = worker.role,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", worker.dailyRate)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
                    Text(
                        text = worker.contact ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral800,
                        modifier = Modifier.weight(1f)
                    )
        
        Row(
            modifier = Modifier.weight(0.5f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(14.dp),
                    tint = Neutral600
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(14.dp),
                    tint = Neutral600
                )
            }
        }
    }
}

@Composable
private fun AttendanceTab(
    workers: List<Worker>,
    todayAttendance: List<AttendanceRecord>,
    onMarkAttendance: () -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Neutral200),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S ATTENDANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = today.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral800
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Present",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "${todayAttendance.count { it.status == "present" }}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Green400
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Absent",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "${todayAttendance.count { it.status == "absent" }}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Red400
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Half-day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "${todayAttendance.count { it.status == "half_day" }}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Amber400
                        )
                    }
                }
                
                Button(
                    onClick = onMarkAttendance,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green500,
                        contentColor = Green50
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HowToReg,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark Attendance")
                }
            }
        }
        
        // Attendance History
        Text(
            text = "ATTENDANCE HISTORY",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        if (todayAttendance.isEmpty()) {
            Text(
                text = "No attendance records for today yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Neutral200),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    todayAttendance.forEach { record ->
                        val worker = workers.find { it.id == record.workerId }
                        AttendanceRow(
                            workerName = worker?.name ?: "Unknown",
                            status = record.status
                        )
                        HorizontalDivider(color = Neutral100, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(
    workerName: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = workerName,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral950
        )
        
        StatusChip(status = status)
    }
}

@Composable
private fun PayrollTab(
    payrollEntries: List<PayrollEntry>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MONTHLY PAYROLL",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        if (payrollEntries.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.AttachMoney,
                title = "No Data",
                description = "Payroll data will appear here once workers and attendance are tracked."
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Neutral200),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Neutral100.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "WORKER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "DAYS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "GROSS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "ADVANCES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "NET",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Table Rows
                    payrollEntries.forEach { entry ->
                        PayrollRow(entry = entry)
                        HorizontalDivider(color = Neutral100, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PayrollRow(entry: PayrollEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.worker.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral950,
            modifier = Modifier.weight(2f)
        )
        
        Text(
            text = "${entry.daysWorked}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", entry.grossPay)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Neutral800,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", entry.advances)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily
            ),
            color = Red400,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "TZS ${String.format("%,.0f", entry.netPay)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Medium
            ),
            color = Green400,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "present" -> Pair(Green800.copy(alpha = 0.3f), Green300)
        "absent" -> Pair(Red800.copy(alpha = 0.3f), Red300)
        "half_day" -> Pair(Amber800.copy(alpha = 0.3f), Amber300)
        else -> Pair(Neutral800.copy(alpha = 0.3f), Neutral300)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.replace("_", " ").uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral800,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onSave: (Worker) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Farm Worker") }
    var dailyRate by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Worker",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = dailyRate,
                    onValueChange = { dailyRate = it },
                    label = { Text("Daily Rate (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Worker(
                            name = name,
                            role = role,
                            hireDate = today,
                            dailyRate = dailyRate.toDoubleOrNull(),
                            contact = contact.ifBlank { null }
                        )
                    )
                },
                enabled = name.isNotBlank() && dailyRate.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Worker")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AttendanceDialog(
    workers: List<Worker>,
    onDismiss: () -> Unit,
    onSave: (List<AttendanceRecord>) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val attendanceMap = remember { mutableStateMapOf<Long, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Mark Attendance",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Date: $today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                
                workers.forEach { worker: Worker ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral950,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("present", "absent", "half_day").forEach { attendanceStatus: String ->
                                FilterChip(
                                    selected = attendanceMap[worker.id] == attendanceStatus,
                                    onClick = { attendanceMap[worker.id] = attendanceStatus },
                                    label = { 
                                        Text(
                                            attendanceStatus.replace("_", " ").take(3),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (attendanceStatus) {
                                            "present" -> Green800.copy(alpha = 0.3f)
                                            "absent" -> Red800.copy(alpha = 0.3f)
                                            else -> Amber800.copy(alpha = 0.3f)
                                        },
                                        selectedLabelColor = when (attendanceStatus) {
                                            "present" -> Green300
                                            "absent" -> Red300
                                            else -> Amber300
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val records = workers.mapNotNull { worker: Worker ->
                        attendanceMap[worker.id]?.let { attendanceStatus: String ->
                            AttendanceRecord(
                                workerId = worker.id,
                                date = today,
                                status = attendanceStatus
                            )
                        }
                    }
                    onSave(records)
                },
                enabled = attendanceMap.size == workers.size,
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Attendance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Neutral600)
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}
