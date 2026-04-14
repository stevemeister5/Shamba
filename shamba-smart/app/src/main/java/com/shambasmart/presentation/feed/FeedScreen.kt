package com.shambasmart.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.shambasmart.data.local.entity.FeedInventory
import com.shambasmart.data.local.entity.SilageInventory
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val feedInventory by viewModel.allFeed.collectAsStateWithLifecycle()
    val silageInventory by viewModel.allSilage.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Feed") }
    var showAddFeed by remember { mutableStateOf(false) }
    var showAddSilage by remember { mutableStateOf(false) }

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
                    text = "Feed Management",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Track feed inventory and silage stocks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Button(
                onClick = { 
                    if (selectedTab == "Feed") showAddFeed = true 
                    else showAddSilage = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green500,
                    contentColor = Green50
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedTab == "Feed") "Add Feed" else "Add Silage",
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
            listOf("Feed", "Silage", "Ration Calculator").forEach { tab ->
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
            "Feed" -> FeedTab(
                feedInventory = feedInventory,
                onDelete = { viewModel.deleteFeed(it) }
            )
            "Silage" -> SilageTab(
                silageInventory = silageInventory,
                onDelete = { viewModel.deleteSilage(it) }
            )
            "Ration Calculator" -> RationCalculatorTab()
        }
    }
    
    // Dialogs
    if (showAddFeed) {
        AddFeedDialog(
            onDismiss = { showAddFeed = false },
            onSave = { feed ->
                viewModel.addFeed(feed)
                showAddFeed = false
            }
        )
    }
    
    if (showAddSilage) {
        AddSilageDialog(
            onDismiss = { showAddSilage = false },
            onSave = { silage ->
                viewModel.addSilage(silage)
                showAddSilage = false
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
private fun FeedTab(
    feedInventory: List<FeedInventory>,
    onDelete: (FeedInventory) -> Unit
) {
    if (feedInventory.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.Restaurant,
            title = "No Feed Inventory",
            description = "Add feed items to track your stock levels."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(feedInventory) { feed ->
                FeedCard(feed = feed, onDelete = { onDelete(feed) })
            }
        }
    }
}

@Composable
private fun FeedCard(feed: FeedInventory, onDelete: () -> Unit) {
    val isLowStock = feed.reorderThreshold?.let { feed.stockLevel <= it } ?: false
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, if (isLowStock) Amber600 else Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = feed.feedType,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = feed.unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLowStock) {
                        Surface(
                            color = Amber600.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LOW STOCK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Amber300,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Neutral600
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Stock Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "${String.format("%.1f", feed.stockLevel)} ${feed.unit}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                feed.reorderThreshold?.let { threshold ->
                    Column {
                        Text(
                            text = "Reorder At",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${String.format("%.1f", threshold)} ${feed.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Neutral950
                        )
                    }
                }
                
                feed.costPerUnit?.let { cost ->
                    Column {
                        Text(
                            text = "Cost/unit",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "TZS ${String.format("%,.0f", cost)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Neutral950
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SilageTab(
    silageInventory: List<SilageInventory>,
    onDelete: (SilageInventory) -> Unit
) {
    if (silageInventory.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.Grass,
            title = "No Silage Inventory",
            description = "Add silage pits to track your stocks."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(silageInventory) { silage ->
                SilageCard(silage = silage, onDelete = { onDelete(silage) })
            }
        }
    }
}

@Composable
private fun SilageCard(silage: SilageInventory, onDelete: () -> Unit) {
    val currentTonnage = silage.currentTonnage ?: silage.estimatedTonnage
    val tonnageColor = if (currentTonnage < 1.0) Amber400 else Neutral950
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, if (currentTonnage < 1.0) Amber600 else Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = silage.cropType,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = "Pit: ${silage.pitId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentTonnage < 1.0) {
                        Surface(
                            color = Amber600.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LOW STOCK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Amber300,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Neutral600
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Tonnage",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "${String.format("%.1f", currentTonnage)} tons",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = tonnageColor
                    )
                }
                
                Column {
                    Text(
                        text = "Estimated",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = "${String.format("%.1f", silage.estimatedTonnage)} tons",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
                
                Column {
                    Text(
                        text = "Fill Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                    Text(
                        text = silage.fillDate.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                }
            }
            
            silage.qualityNotes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }
        }
    }
}

@Composable
private fun RationCalculatorTab() {
    var animalWeight by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("goat") }
    var productionStage by remember { mutableStateOf("maintenance") }
    
    val dmPercentage = when (species) {
        "goat" -> 3.0
        "sheep" -> 3.0
        "cattle" -> 2.5
        else -> 3.0
    }
    
    val dmRequired = animalWeight.toDoubleOrNull()?.let { it * dmPercentage / 100 } ?: 0.0
    
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "RATION CALCULATOR",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
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
                // Species
                Text(
                    text = "Species",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("goat", "sheep", "cattle")) { s ->
                        FilterChip(
                            selected = species == s,
                            onClick = { species = s },
                            label = { Text(s.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                // Weight
                OutlinedTextField(
                    value = animalWeight,
                    onValueChange = { animalWeight = it },
                    label = { Text("Animal Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Production Stage
                Text(
                    text = "Production Stage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("maintenance", "lactation", "growth", "gestation")) { stage ->
                        FilterChip(
                            selected = productionStage == stage,
                            onClick = { productionStage = stage },
                            label = { Text(stage.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
            }
        }
        
        // Results
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, Green800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = Green400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RATION RESULTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Green400
                    )
                }
                
                if (animalWeight.isNotBlank() && animalWeight.toDoubleOrNull() != null) {
                    Text(
                        text = "Daily DM Required: ${String.format("%.1f", dmRequired)} kg",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                    
                    Text(
                        text = "For a ${animalWeight}kg $species in $productionStage stage, provide approximately ${String.format("%.1f", dmRequired)}kg of dry matter daily. Split into 2-3 feedings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral800
                    )
                } else {
                    Text(
                        text = "Enter animal weight to calculate daily ration requirements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
            }
        }
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
private fun AddFeedDialog(
    onDismiss: () -> Unit,
    onSave: (FeedInventory) -> Unit
) {
    var feedType by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var stockLevel by remember { mutableStateOf("") }
    var reorderThreshold by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Feed Item",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = feedType,
                    onValueChange = { feedType = it },
                    label = { Text("Feed Type") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("kg", "tons", "bales", "bags")) { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = stockLevel,
                    onValueChange = { stockLevel = it },
                    label = { Text("Stock Level") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = reorderThreshold,
                    onValueChange = { reorderThreshold = it },
                    label = { Text("Reorder Threshold") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost per unit (TZS)") },
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
                        FeedInventory(
                            feedType = feedType,
                            unit = unit,
                            stockLevel = stockLevel.toDoubleOrNull() ?: 0.0,
                            reorderThreshold = reorderThreshold.toDoubleOrNull(),
                            costPerUnit = costPerUnit.toDoubleOrNull()
                        )
                    )
                },
                enabled = feedType.isNotBlank() && stockLevel.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
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
private fun AddSilageDialog(
    onDismiss: () -> Unit,
    onSave: (SilageInventory) -> Unit
) {
    var cropType by remember { mutableStateOf("Maize Silage") }
    var pitId by remember { mutableStateOf("") }
    var estimatedTonnage by remember { mutableStateOf("") }
    var qualityNotes by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Silage",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Maize Silage", "Grass Silage", "Legume Silage", "Other")) { t ->
                        FilterChip(
                            selected = cropType == t,
                            onClick = { cropType = t },
                            label = { Text(t) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = pitId,
                    onValueChange = { pitId = it },
                    label = { Text("Pit ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = estimatedTonnage,
                    onValueChange = { estimatedTonnage = it },
                    label = { Text("Estimated Tonnage") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = qualityNotes,
                    onValueChange = { qualityNotes = it },
                    label = { Text("Quality Notes") },
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
                        SilageInventory(
                            pitId = pitId.ifBlank { "PIT-${System.currentTimeMillis()}" },
                            fillDate = today,
                            cropType = cropType,
                            estimatedTonnage = estimatedTonnage.toDoubleOrNull() ?: 0.0,
                            qualityNotes = qualityNotes.ifBlank { null }
                        )
                    )
                },
                enabled = estimatedTonnage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
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
