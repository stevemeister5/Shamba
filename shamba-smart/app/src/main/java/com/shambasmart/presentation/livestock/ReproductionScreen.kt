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
import com.shambasmart.data.local.entity.ReproductionRecord
import com.shambasmart.data.local.entity.Animal
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReproductionScreen(
    viewModel: LivestockViewModel = hiltViewModel()
) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var selectedDam by remember { mutableStateOf<Animal?>(null) }
    var selectedSire by remember { mutableStateOf<Animal?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var damDropdownExpanded by remember { mutableStateOf(false) }
    var sireDropdownExpanded by remember { mutableStateOf(false) }

    // Get reproduction records for selected dam
    val reproductionRecords by remember(selectedDam?.id) {
        selectedDam?.id?.let { damId ->
            viewModel.getReproductionRecordsByDam(damId)
        } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Reproduction Tracking",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Dam Selection
        ExposedDropdownMenuBox(
            expanded = damDropdownExpanded,
            onExpandedChange = { damDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedDam?.let { "Dam: ${it.tagId ?: "No Tag"} - ${it.species}" } ?: "Select Dam (Female)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Dam (Female)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = damDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = damDropdownExpanded,
                onDismissRequest = { damDropdownExpanded = false }
            ) {
                animals.filter { it.sex == "female" }.forEach { animal ->
                    DropdownMenuItem(
                        text = { Text("${animal.tagId ?: "No Tag"} - ${animal.species}") },
                        onClick = {
                            selectedDam = animal
                            damDropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Sire Selection
        ExposedDropdownMenuBox(
            expanded = sireDropdownExpanded,
            onExpandedChange = { sireDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedSire?.let { "Sire: ${it.tagId ?: "No Tag"} - ${it.species}" } ?: "Select Sire (Male) - Optional",
                onValueChange = {},
                readOnly = true,
                label = { Text("Sire (Male)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sireDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = sireDropdownExpanded,
                onDismissRequest = { sireDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        selectedSire = null
                        sireDropdownExpanded = false
                    }
                )
                animals.filter { it.sex == "male" }.forEach { animal ->
                    DropdownMenuItem(
                        text = { Text("${animal.tagId ?: "No Tag"} - ${animal.species}") },
                        onClick = {
                            selectedSire = animal
                            sireDropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Reproduction Records List
        if (reproductionRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PregnantWoman,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No reproduction records")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Reproduction Record")
                    }
                }
            }
        } else {
            LazyColumn {
                items(reproductionRecords) { record ->
                    ReproductionRecordCard(record = record)
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
            Icon(Icons.Default.Add, contentDescription = "Add Reproduction Record")
        }
    }

    // Add Reproduction Record Dialog
    if (showAddDialog) {
        AddReproductionRecordDialog(
            damId = selectedDam?.id ?: 0L,
            sireId = selectedSire?.id,
            onDismiss = { showAddDialog = false },
            onAdd = { record ->
                viewModel.addReproductionRecord(record)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ReproductionRecordCard(record: ReproductionRecord) {
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
                record.pregnancyConfirmed?.let { confirmed ->
                    Badge(
                        containerColor = if (confirmed) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(if (confirmed) "Pregnant" else "Not Pregnant")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            record.matingDate?.let {
                Text(text = "Mating: $it", style = MaterialTheme.typography.bodyMedium)
            }
            record.expectedDueDate?.let {
                Text(
                    text = "Expected: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            record.actualBirthDate?.let {
                Text(text = "Born: $it", style = MaterialTheme.typography.bodyMedium)
            }
            record.numberOfKids?.let {
                Text(text = "Kids: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AddReproductionRecordDialog(
    damId: Long,
    sireId: Long?,
    onDismiss: () -> Unit,
    onAdd: (ReproductionRecord) -> Unit
) {
    var type by remember { mutableStateOf("heat_detection") }
    var matingDate by remember { mutableStateOf("") }
    var pregnancyConfirmed by remember { mutableStateOf<Boolean?>(null) }
    var expectedDueDate by remember { mutableStateOf("") }
    var actualBirthDate by remember { mutableStateOf("") }
    var numberOfKids by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reproduction Record") },
        text = {
            Column {
                // Type Selection
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("heat_detection", "mating", "pregnancy", "birth").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.replace("_", " ")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (type == "mating" || type == "pregnancy") {
                    OutlinedTextField(
                        value = matingDate,
                        onValueChange = { matingDate = it },
                        label = { Text("Mating Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (type == "pregnancy") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = pregnancyConfirmed == true,
                            onClick = { pregnancyConfirmed = true },
                            label = { Text("Confirmed") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = pregnancyConfirmed == false,
                            onClick = { pregnancyConfirmed = false },
                            label = { Text("Not Confirmed") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (type == "pregnancy" || type == "birth") {
                    OutlinedTextField(
                        value = expectedDueDate,
                        onValueChange = { expectedDueDate = it },
                        label = { Text("Expected Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (type == "birth") {
                    OutlinedTextField(
                        value = actualBirthDate,
                        onValueChange = { actualBirthDate = it },
                        label = { Text("Actual Birth Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = numberOfKids,
                        onValueChange = { numberOfKids = it },
                        label = { Text("Number of Kids") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        ReproductionRecord(
                            damId = damId,
                            sireId = sireId,
                            type = type,
                            matingDate = if (matingDate.isNotBlank()) LocalDate.parse(matingDate) else null,
                            pregnancyConfirmed = pregnancyConfirmed,
                            expectedDueDate = if (expectedDueDate.isNotBlank()) LocalDate.parse(expectedDueDate) else null,
                            actualBirthDate = if (actualBirthDate.isNotBlank()) LocalDate.parse(actualBirthDate) else null,
                            numberOfKids = numberOfKids.toIntOrNull()
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
