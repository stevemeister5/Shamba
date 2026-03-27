package com.shambasmart.presentation.feed

import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.*

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
import com.shambasmart.data.local.entity.FeedInventory
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedInventoryScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val feedInventory by viewModel.allFeedInventory.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalItems = feedInventory.size
    val lowStock = feedInventory.count { 
        it.reorderThreshold != null && it.stockLevel <= it.reorderThreshold 
    }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            FeedInventoryHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // KPI Strip
            FeedKPIStrip(
                totalItems = totalItems,
                lowStock = lowStock
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Feed Inventory List
            if (feedInventory.isEmpty()) {
                EmptyFeedState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feedInventory) { feed ->
                        FeedInventoryCard(feed = feed)
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
            Icon(Icons.Outlined.Add, contentDescription = "Add Feed Item")
        }
    }

    // Add Feed Dialog
    if (showAddDialog) {
        AddFeedDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { feed ->
                viewModel.addFeed(feed)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FeedInventoryHeader() {
    Column {
        Text(
            text = "Feed Inventory",
            style = MaterialTheme.typography.headlineLarge,
            color = Neutral950
        )
        Text(
            text = "Monitor stock levels and manage feed supplies",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun FeedKPIStrip(
    totalItems: Int,
    lowStock: Int
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
            FeedKPIItem(
                icon = Icons.Outlined.Inventory2,
                label = "TOTAL ITEMS",
                value = "$totalItems",
                valueColor = Neutral950,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            FeedKPIItem(
                icon = Icons.Outlined.Warning,
                label = "LOW STOCK",
                value = "$lowStock",
                valueColor = if (lowStock > 0) Amber400 else Green400,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = Neutral200
            )
            
            FeedKPIItem(
                icon = Icons.Outlined.CheckCircle,
                label = "ADEQUATE STOCK",
                value = "${totalItems - lowStock}",
                valueColor = Green400,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeedKPIItem(
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
private fun FeedInventoryCard(feed: FeedInventory) {
    val isLowStock = feed.reorderThreshold != null && feed.stockLevel <= feed.reorderThreshold

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(
            1.dp,
            if (isLowStock) Amber400.copy(alpha = 0.3f) else Neutral200
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feed Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isLowStock) Amber400.copy(alpha = 0.15f)
                        else Green400.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Grass,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isLowStock) Amber400 else Green400
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
                        text = feed.feedType,
                        style = MaterialTheme.typography.titleLarge,
                        color = Neutral950
                    )
                    if (isLowStock) {
                        Surface(
                            color = Amber400.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, Amber400.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "LOW STOCK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Amber400,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Current Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${feed.stockLevel} ${feed.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                            color = Neutral950
                        )
                    }
                    
                    feed.reorderThreshold?.let {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Reorder At",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                            Text(
                                text = "$it ${feed.unit}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                                color = Amber400
                            )
                        }
                    }
                }
                
                feed.costPerUnit?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cost: TZS $it/${feed.unit}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = GeistMonoFamily),
                        color = Teal400
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(onAddClick: () -> Unit) {
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
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = "No feed inventory",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral600
                )
                Text(
                    text = "Add feed items to track stock levels",
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
                    Text("Add Feed Item")
                }
            }
        }
    }
}

@Composable
private fun AddFeedDialog(
    onDismiss: () -> Unit,
    onAdd: (FeedInventory) -> Unit
) {
    var feedType by remember { mutableStateOf("") }
    var stockLevel by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
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
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = stockLevel,
                        onValueChange = { stockLevel = it },
                        label = { Text("Stock Level") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(0.5f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200,
                            focusedContainerColor = SurfaceSunken,
                            unfocusedContainerColor = SurfaceSunken
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                OutlinedTextField(
                    value = reorderThreshold,
                    onValueChange = { reorderThreshold = it },
                    label = { Text("Reorder Threshold") },
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
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost per Unit (TZS)") },
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
                    onAdd(
                        FeedInventory(
                            feedType = feedType,
                            stockLevel = stockLevel.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            reorderThreshold = reorderThreshold.toDoubleOrNull(),
                            costPerUnit = costPerUnit.toDoubleOrNull()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Item")
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