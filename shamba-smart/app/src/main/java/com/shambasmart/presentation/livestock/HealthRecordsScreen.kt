package com.shambasmart.presentation.livestock

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
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.HealthRecord
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordsScreen(
    animalId: Long? = null,
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var selectedAnimal by remember { mutableStateOf<Animal?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Get health records for selected animal
    val healthRecords by remember(selectedAnimal?.id) {
        selectedAnimal?.id?.let { animalId ->
            viewModel.getHealthRecordsByAnimal(animalId)
        } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Health Records",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Animal Selection
        if (animalId == null) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedAnimal?.let { "${it.tagId ?: "No Tag"} - ${it.species}" } ?: "Select Animal",
                    onValueChange = { _ -> },
                    readOnly = true,
                    label = { Text("Animal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    animals.forEach { animal ->
                        DropdownMenuItem(
                            text = { Text("${animal.tagId ?: "No Tag"} - ${animal.species}") },
                            onClick = {
                                selectedAnimal = animal
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Health Records List
        if (healthRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No health records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Health Record")
                    }
                }
            }
        } else {
            LazyColumn {
                items(healthRecords) { record ->
                    HealthRecordCard(record = record)
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
            Icon(Icons.Default.Add, contentDescription = "Add Health Record")
        }
    }

    // Add Health Record Dialog
    if (showAddDialog) {
        AddHealthRecordDialog(
            animalId = selectedAnimal?.id ?: animalId ?: 0L,
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                viewModel.addHealthRecord(record)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.type,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = record.date.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            record.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            record.vaccineName?.let {
                Text(text = "Vaccine: $it", style = MaterialTheme.typography.bodySmall)
            }
            record.nextDueDate?.let {
                Text(
                    text = "Next due: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AddHealthRecordDialog(
    animalId: Long,
    onDismiss: () -> Unit,
    onAdd: (HealthRecord) -> Unit
) {
    var type by remember { mutableStateOf("vaccination") }
    var description by remember { mutableStateOf("") }
    var vaccineName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var nextDueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Health Record") },
        text = {
            Column {
                // Type Selection
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("vaccination", "deworming", "treatment", "illness").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (type == "vaccination") {
                    OutlinedTextField(
                        value = vaccineName,
                        onValueChange = { vaccineName = it },
                        label = { Text("Vaccine Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Next Due Date (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        HealthRecord(
                            animalId = animalId,
                            type = type,
                            description = description.ifBlank { null },
                            vaccineName = if (type == "vaccination") vaccineName.ifBlank { null } else null,
                            date = LocalDate.parse(date),
                            nextDueDate = if (nextDueDate.isNotBlank()) LocalDate.parse(nextDueDate) else null
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
