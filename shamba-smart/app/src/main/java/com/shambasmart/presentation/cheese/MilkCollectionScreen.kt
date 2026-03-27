package com.shambasmart.presentation.cheese

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.shambasmart.data.local.entity.MilkCollection
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkCollectionScreen(
    viewModel: CheeseViewModel = hiltViewModel()
) {
    val milkCollections by viewModel.allMilkCollections.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalCollected = milkCollections.sumOf { it.quantityLitres }
    val accepted = milkCollections.count { it.accepted }
    val rejected = milkCollections.count { !it.accepted }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            MilkCollectionHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // KPI Strip
            MilkCollectionKPIStrip(
                totalCollected = totalCollected,
                accepted = accepted,
                rejected = rejected
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Milk Collections List
            if (milkCollections.isEmpty()) {
                EmptyMilkCollectionState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(milkCollections) { collection ->
                        MilkCollectionCard(collection = collection)
                    }
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Green500,
            contentColor = Green950,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Milk Collection")
        }
    }

    // Add Milk Collection Dialog
    if (showAddDialog) {
        AddMilkCollectionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { collection ->
                viewModel.addMilkCollection(collection)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MilkCollectionHeader() {
    Column {
        Text(
            text = "Milk Collection",
            style = MaterialTheme.typography.headlineLarge,
            color = Neutral950
        )
        Text(
            text = "Record daily milk collections and quality checks",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun MilkCollectionKPIStrip(
    totalCollected: Double,
    accepted: Int,
    rejected: Int
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
            MilkCollectionKPIItem(
                icon = Icons.Outlined.WaterDrop,
                label = "TOTAL COLLECTED",
                value = "${String.format("%.1f", totalCollected)} L",
                valueColor = Teal400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            MilkCollectionKPIItem(
                icon = Icons.Outlined.CheckCircle,
                label = "ACCEPTED",
                value = "$accepted",
                valueColor = Green400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            MilkCollectionKPIItem(
                icon = Icons.Outlined.Cancel,
                label = "REJECTED",
                value = "$rejected",
                valueColor = if (rejected > 0) Red400 else Neutral600,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MilkCollectionKPIItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
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
            color = Neutral600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = GeistMonoFamily),
            color = valueColor
        )
    }
}

@Composable
private fun MilkCollectionCard(collection: MilkCollection) {
    val (statusColor, statusText) = if (collection.accepted) {
        Green400 to "ACCEPTED"
    } else {
        Red400 to "REJECTED"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(
            1.dp,
            if (collection.accepted) Neutral200 else Red400.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Collection Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = statusColor
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
                        text = "${collection.quantityLitres} L",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = GeistMonoFamily),
                        color = Neutral950
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Date: ${collection.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    collection.phLevel?.let {
                        Text(
                            text = "pH: $it",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = GeistMonoFamily),
                            color = Neutral600
                        )
                    }
                    Text(
                        text = if (collection.smellOk) "Smell: OK" else "Smell: Issue",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (collection.smellOk) Green400 else Amber400
                    )
                    Text(
                        text = if (collection.colorOk) "Color: OK" else "Color: Issue",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (collection.colorOk) Green400 else Amber400
                    )
                }
                
                collection.rejectionReason?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reason: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Red400
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMilkCollectionState(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = "No milk collections",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral600
                )
                Text(
                    text = "Start recording daily milk collections",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral400
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Milk Collection")
                }
            }
        }
    }
}

@Composable
private fun AddMilkCollectionDialog(
    onDismiss: () -> Unit,
    onAdd: (MilkCollection) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var quantityLitres by remember { mutableStateOf("") }
    var phLevel by remember { mutableStateOf("") }
    var smellOk by remember { mutableStateOf(true) }
    var colorOk by remember { mutableStateOf(true) }
    var accepted by remember { mutableStateOf(true) }
    var rejectionReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Milk Collection",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = quantityLitres,
                    onValueChange = { quantityLitres = it },
                    label = { Text("Quantity (litres)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = phLevel,
                    onValueChange = { phLevel = it },
                    label = { Text("pH Level (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Quality Checks
                Column {
                    Text(
                        text = "Quality Checks",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = smellOk,
                            onClick = { smellOk = !smellOk },
                            label = {
                                Text(
                                    text = "Smell OK",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Green300
                            )
                        )
                        FilterChip(
                            selected = colorOk,
                            onClick = { colorOk = !colorOk },
                            label = {
                                Text(
                                    text = "Color OK",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                // Accept/Reject
                Column {
                    Text(
                        text = "Decision",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = accepted,
                            onClick = { accepted = true },
                            label = {
                                Text(
                                    text = "Accept",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Green300
                            )
                        )
                        FilterChip(
                            selected = !accepted,
                            onClick = { accepted = false },
                            label = {
                                Text(
                                    text = "Reject",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceSunken,
                                selectedContainerColor = Red600.copy(alpha = 0.3f),
                                labelColor = Neutral800,
                                selectedLabelColor = Red300
                            )
                        )
                    }
                }
                
                if (!accepted) {
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Red500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        MilkCollection(
                            date = LocalDate.parse(date),
                            quantityLitres = quantityLitres.toDoubleOrNull() ?: 0.0,
                            phLevel = phLevel.toDoubleOrNull(),
                            smellOk = smellOk,
                            colorOk = colorOk,
                            accepted = accepted,
                            rejectionReason = if (!accepted) rejectionReason else null
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Collection")
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