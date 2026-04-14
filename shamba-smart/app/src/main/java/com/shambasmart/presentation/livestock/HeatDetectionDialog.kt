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
import androidx.compose.ui.unit.dp
import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.ReproductionRecord
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatDetectionDialog(
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onSave: (ReproductionRecord) -> Unit
) {
    var selectedAnimalId by remember { mutableStateOf<Long?>(null) }
    var heatSigns by remember { mutableStateOf("") }
    var buckIntroduced by remember { mutableStateOf(false) }
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
                    imageVector = Icons.Outlined.Pets,
                    contentDescription = null,
                    tint = Amber400
                )
                Text(
                    text = "Heat Detection",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Select Doe
                Text(
                    text = "Select Doe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                
                val does = animals.filter { it.sex == "female" }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(does) { doe ->
                        FilterChip(
                            selected = selectedAnimalId == doe.id,
                            onClick = { selectedAnimalId = doe.id },
                            label = { 
                                Text(doe.tagId ?: doe.breed ?: "Doe #${doe.id}") 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber600.copy(alpha = 0.3f),
                                selectedLabelColor = Amber300
                            )
                        )
                    }
                }
                
                // Heat Signs
                OutlinedTextField(
                    value = heatSigns,
                    onValueChange = { heatSigns = it },
                    label = { Text("Heat Signs Observed") },
                    placeholder = { Text("e.g., Mounting, restlessness, vocalization") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Buck Introduced
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Buck introduced?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral800
                    )
                    Switch(
                        checked = buckIntroduced,
                        onCheckedChange = { buckIntroduced = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Green400,
                            checkedTrackColor = Green800.copy(alpha = 0.3f),
                            uncheckedThumbColor = Neutral400,
                            uncheckedTrackColor = Neutral200
                        )
                    )
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAnimalId != null) {
                        onSave(
                            ReproductionRecord(
                                damId = selectedAnimalId!!,
                                type = "heat_detected",
                                matingDate = today,
                                notes = "Heat signs: $heatSigns. Buck introduced: ${if (buckIntroduced) "Yes" else "No"}. ${notes.ifBlank { "" }}"
                            )
                        )
                    }
                },
                enabled = selectedAnimalId != null && heatSigns.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = Amber50,
                    disabledContainerColor = Neutral200,
                    disabledContentColor = Neutral600
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Record Heat")
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