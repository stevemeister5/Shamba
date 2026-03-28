package com.shambasmart.presentation.labour

import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.Worker
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourScreen(
    viewModel: LabourViewModel = hiltViewModel()
) {
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Labour Management",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Workforce Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val permanent = workers.count { !it.isSeasonal }
                    val seasonal = workers.count { it.isSeasonal }
                    WorkerStat("Total Workers", "${workers.size}")
                    WorkerStat("Permanent", "$permanent")
                    WorkerStat("Seasonal", "$seasonal")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Workers List
        if (workers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No workers registered")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Worker")
                    }
                }
            }
        } else {
            LazyColumn {
                items(workers) { worker ->
                    WorkerCard(worker = worker)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Floating Action Button
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Worker")
        }
    }

    // Add Worker Dialog
    if (showAddDialog) {
        AddWorkerDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { worker ->
                viewModel.addWorker(worker)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WorkerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun WorkerCard(worker: Worker) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    Badge(
                        containerColor = if (worker.isSeasonal) 
                            MaterialTheme.colorScheme.tertiary 
                        else MaterialTheme.colorScheme.primary
                    ) {
                        Text(if (worker.isSeasonal) "Seasonal" else "Permanent")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Role: ${worker.role}", style = MaterialTheme.typography.bodyMedium)
            worker.contact?.let {
                Text(text = "Contact: $it", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Hired: ${worker.hireDate}", style = MaterialTheme.typography.bodySmall)
            worker.dailyRate?.let {
                Text(
                    text = "Daily Rate: TZS ${String.format("%.0f", it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            worker.endDate?.let {
                Text(text = "End Date: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onAdd: (Worker) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var hireDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var dailyRate by remember { mutableStateOf("") }
    var isSeasonal by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Worker") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hireDate,
                    onValueChange = { hireDate = it },
                    label = { Text("Hire Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dailyRate,
                    onValueChange = { dailyRate = it },
                    label = { Text("Daily Rate (TZS)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = !isSeasonal,
                        onClick = { isSeasonal = false },
                        label = { Text("Permanent") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = isSeasonal,
                        onClick = { isSeasonal = true },
                        label = { Text("Seasonal") },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (isSeasonal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        Worker(
                            name = name,
                            role = role,
                            contact = contact.ifBlank { null },
                            hireDate = LocalDate.parse(hireDate),
                            dailyRate = dailyRate.toDoubleOrNull(),
                            isSeasonal = isSeasonal,
                            endDate = if (isSeasonal && endDate.isNotBlank()) LocalDate.parse(endDate) else null
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