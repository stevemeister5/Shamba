package com.shambasmart.presentation.feed

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
import com.shambasmart.data.local.entity.StoreItem
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel = hiltViewModel()
) {
    val storeItems by viewModel.allStoreItems.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalItems = storeItems.size
    val lowStock = storeItems.count { 
        it.reorderLevel != null && it.quantity <= it.reorderLevel 
    }
    val expiring = storeItems.count {
        it.expiryDate != null && it.expiryDate <= LocalDate.now().toString()
    }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            StoreHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // KPI Strip
            StoreKPIStrip(
                totalItems = totalItems,
                lowStock = lowStock,
                expiring = expiring
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Store Items List
            if (storeItems.isEmpty()) {
                EmptyStoreState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(storeItems) { item ->
                        StoreItemCard(item = item)
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
            Icon(Icons.Outlined.Add, contentDescription = "Add Store Item")
        }
    }

    // Add Store Item Dialog
    if (showAddDialog) {
        AddStoreItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                viewModel.addStoreItem(item)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StoreHeader() {
    Column {
        Text(
            text = "Store Management",
            style = MaterialTheme.typography.headlineLarge,
            color = Neutral950
        )
        Text(
            text = "Track inventory, expiry dates, and stock levels",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun StoreKPIStrip(
    totalItems: Int,
    lowStock: Int,
    expiring: Int
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
            StoreKPIItem(
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
            
            StoreKPIItem(
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
            
            StoreKPIItem(
                icon = Icons.Outlined.Schedule,
                label = "EXPIRING",
                value = "$expiring",
                valueColor = if (expiring > 0) Red400 else Green400,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StoreKPIItem(
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
private fun StoreItemCard(item: StoreItem) {
    val isLowStock = item.reorderLevel != null && item.quantity <= item.reorderLevel
    val isExpiring = item.expiryDate != null && item.expiryDate <= LocalDate.now().toString()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(
            1.dp,
            when {
                isExpiring -> Red400.copy(alpha = 0.3f)
                isLowStock -> Amber400.copy(alpha = 0.3f)
                else -> Neutral200
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isExpiring -> Red400.copy(alpha = 0.15f)
                            isLowStock -> Amber400.copy(alpha = 0.15f)
                            else -> Green400.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(item.category),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when {
                        isExpiring -> Red400
                        isLowStock -> Amber400
                        else -> Green400
                    }
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
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Neutral950
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isLowStock) {
                            Surface(
                                color = Amber400.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Amber400.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "LOW",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Amber400,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (isExpiring) {
                            Surface(
                                color = Red400.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Red400.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "EXPIRING",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Red400,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Category: ${item.category.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Quantity",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral600
                        )
                        Text(
                            text = "${item.quantity} ${item.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                            color = Neutral950
                        )
                    }
                    
                    item.expiryDate?.let {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Expires",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral600
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                                color = if (isExpiring) Red400 else Neutral950
                            )
                        }
                    }
                }
                
                item.costPerUnit?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cost: TZS $it/${item.unit}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = GeistMonoFamily),
                        color = Teal400
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "seeds" -> Icons.Outlined.Eco
        "fertilizer" -> Icons.Outlined.Science
        "chemicals" -> Icons.Outlined.Bolt
        "medicine" -> Icons.Outlined.MedicalServices
        "equipment" -> Icons.Outlined.Build
        else -> Icons.Outlined.Inventory2
    }
}

@Composable
private fun EmptyStoreState(onAddClick: () -> Unit) {
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
                    imageVector = Icons.Outlined.Store,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neutral300
                )
                Text(
                    text = "No store items",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Neutral600
                )
                Text(
                    text = "Add items to track inventory and expiry dates",
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
                    Text("Add Store Item")
                }
            }
        }
    }
}

@Composable
private fun AddStoreItemDialog(
    onDismiss: () -> Unit,
    onAdd: (StoreItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("seeds") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var expiryDate by remember { mutableStateOf("") }
    var reorderLevel by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Store Item",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Category Selector
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutral600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("seeds", "fertilizer", "medicine", "equipment").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = {
                                    Text(
                                        text = cat.replaceFirstChar { it.uppercase() },
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
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
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
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
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
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it },
                    label = { Text("Reorder Level") },
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
                        StoreItem(
                            name = name,
                            category = category,
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            expiryDate = expiryDate.ifBlank { null },
                            reorderLevel = reorderLevel.toDoubleOrNull(),
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