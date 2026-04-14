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
fun MatingRecordDialog(
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onSave: (ReproductionRecord) -> Unit
) {
    var selectedDamId by remember { mutableStateOf<Long?>(null) }
    var selectedSireId by remember { mutableStateOf<Long?>(null) }
    var matingType by remember { mutableStateOf("natural") }
    var notes by remember { mutableStateOf("") }
    
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val does = animals.filter { it.sex == "female" }
    val bucks = animals.filter { it.sex == "male" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = null,
                    tint = Red400
                )
                Text(
                    text = "Record Mating",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral950
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Select Dam
                Text(
                    text = "Select Dam (Female)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(does) { doe ->
                        FilterChip(
                            selected = selectedDamId == doe.id,
                            onClick = { selectedDamId = doe.id },
                            label = { 
                                Text(doe.tagId ?: doe.breed ?: "Doe #${doe.id}") 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Red600.copy(alpha = 0.3f),
                                selectedLabelColor = Red300
                            )
                        )
                    }
                }
                
                // Select Sire
                Text(
                    text = "Select Sire (Male)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bucks) { buck ->
                        FilterChip(
                            selected = selectedSireId == buck.id,
                            onClick = { selectedSireId = buck.id },
                            label = { 
                                Text(buck.tagId ?: buck.breed ?: "Buck #${buck.id}") 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Blue600.copy(alpha = 0.3f),
                                selectedLabelColor = Blue300
                            )
                        )
                    }
                }
                
                // Mating Type
                Text(
                    text = "Mating Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("natural", "AI").forEach { type ->
                        FilterChip(
                            selected = matingType == type,
                            onClick = { matingType = type },
                            label = { 
                                Text(if (type == "AI") "Artificial Insemination" else "Natural") 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
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
                    if (selectedDamId != null) {
                        onSave(
                            ReproductionRecord(
                                damId = selectedDamId!!,
                                type = "mating",
                                matingDate = today,
                                sireId = selectedSireId,
                                notes = "Type: $matingType. ${notes.ifBlank { "" }}"
                            )
                        )
                    }
                },
                enabled = selectedDamId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red500,
                    contentColor = Red50,
                    disabledContainerColor = Neutral200,
                    disabledContentColor = Neutral600
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Record Mating")
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