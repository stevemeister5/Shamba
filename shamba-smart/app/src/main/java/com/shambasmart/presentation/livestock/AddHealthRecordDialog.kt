package com.shambasmart.presentation.livestock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shambasmart.data.local.entity.HealthRecord
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthRecordDialog(
    animalId: Long,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit,
    onOpenSymptomChecker: () -> Unit = {}
) {
    var recordType by remember { mutableStateOf("vaccination") }
    var description by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var vetName by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    tint = Green400
                )
                Text(
                    text = "Add Health Record",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Record Type Selection
                Text(
                    text = "Record Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("vaccination", "treatment", "illness", "vet_visit")) { type ->
                        FilterChip(
                            selected = recordType == type,
                            onClick = { recordType = type },
                            label = {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() }.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Green300
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Neutral200,
                                selectedBorderColor = Green700,
                                enabled = true,
                                selected = recordType == type
                            )
                        )
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g., CDT vaccination, deworming") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Treatment
                OutlinedTextField(
                    value = treatment,
                    onValueChange = { treatment = it },
                    label = { Text("Treatment/Drug") },
                    placeholder = { Text("e.g., Ivermectin 1ml SC") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Vet Name (optional)
                OutlinedTextField(
                    value = vetName,
                    onValueChange = { vetName = it },
                    label = { Text("Vet Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Cost
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost (TZS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Symptom Checker Link
                if (recordType == "illness") {
                    TextButton(
                        onClick = onOpenSymptomChecker,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Green400
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Symptom Checker")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        HealthRecord(
                            animalId = animalId,
                            type = recordType,
                            description = description,
                            vaccineName = if (recordType == "vaccination") treatment.ifBlank { null } else null,
                            date = today,
                            veterinarian = vetName.ifBlank { null },
                            cost = cost.toDoubleOrNull(),
                            notes = notes.ifBlank { null }
                        )
                    )
                },
                enabled = description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50,
                    disabledContainerColor = Neutral200,
                    disabledContentColor = Neutral600
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Neutral600
                )
            }
        },
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp)
    )
}