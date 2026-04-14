package com.shambasmart.presentation.cheese

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.CheeseBatch
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheeseScreen(
    viewModel: CheeseViewModel = hiltViewModel()
) {
    val batches by viewModel.allCheeseBatches.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Production") }
    var showBatchCreation by remember { mutableStateOf(false) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var selectedBatch by remember { mutableStateOf<CheeseBatch?>(null) }

    if (showBatchCreation) {
        BatchCreationScreen(
            onNavigateBack = { showBatchCreation = false },
            onBatchCreated = { batch ->
                viewModel.addCheeseBatch(batch)
                showBatchCreation = false
            }
        )
    } else {
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
                        text = "Cheese Production",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Neutral950
                    )
                    Text(
                        text = "Track batches, aging, and sales",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                Button(
                    onClick = { showBatchCreation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = Amber50
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
                        text = "New Batch",
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
                listOf("Production", "Inventory", "Sales").forEach { tab ->
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
                "Production" -> ProductionTab(
                    batches = batches.filter { it.status in listOf("active", "aging") },
                    onViewBatch = { selectedBatch = it }
                )
                "Inventory" -> InventoryTab(
                    batches = batches.filter { it.status in listOf("ready", "packaged") },
                    onRecordSale = {
                        selectedBatch = it
                        showSaleDialog = true
                    }
                )
                "Sales" -> SalesTab(batches = batches.filter { it.status == "sold" })
            }
        }
    }
    
    // Sale Dialog
    if (showSaleDialog && selectedBatch != null) {
        CheeseSaleDialog(
            batch = selectedBatch!!,
            onDismiss = {
                showSaleDialog = false
                selectedBatch = null
            },
            onSave = { quantityKg, pricePerKg, buyerName ->
                viewModel.sellCheeseBatch(selectedBatch!!, quantityKg, pricePerKg, buyerName)
                showSaleDialog = false
                selectedBatch = null
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
    val backgroundColor = if (isSelected) Amber800.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) Amber300 else Neutral600
    
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
private fun ProductionTab(
    batches: List<CheeseBatch>,
    onViewBatch: (CheeseBatch) -> Unit
) {
    if (batches.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.Inventory2,
            title = "No Active Batches",
            description = "Start your first cheese batch to begin production tracking."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(batches) { batch ->
                BatchCard(
                    batch = batch,
                    onViewClick = { onViewBatch(batch) }
                )
            }
        }
    }
}

@Composable
private fun InventoryTab(
    batches: List<CheeseBatch>,
    onRecordSale: (CheeseBatch) -> Unit
) {
    if (batches.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.Inventory,
            title = "No Ready Batches",
            description = "Batches will appear here once aging is complete."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(batches) { batch ->
                InventoryCard(
                    batch = batch,
                    onRecordSale = { onRecordSale(batch) }
                )
            }
        }
    }
}

@Composable
private fun SalesTab(batches: List<CheeseBatch>) {
    if (batches.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Outlined.AttachMoney,
            title = "No Sales Recorded",
            description = "Sales will appear here once you record cheese transactions."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(batches) { batch ->
                SoldBatchCard(batch = batch)
            }
        }
    }
}

@Composable
private fun SoldBatchCard(batch: CheeseBatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${batch.cheeseType} Batch #${batch.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral950
                )
                StatusChip(status = batch.status)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Yield: ${batch.yieldKg} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
                Text(
                    text = batch.saleDate?.toString() ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }
        }
    }
}

@Composable
private fun BatchCard(
    batch: CheeseBatch,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
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
                        text = "${batch.cheeseType} Batch #${batch.id}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = "${batch.milkVolumeUsed}L milk • ${batch.agingLocation ?: "Not set"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                StatusChip(status = batch.status)
            }
            
            // Costs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Cost: TZS ${String.format("%,.0f", batch.milkCostTzs + batch.cultureCostTzs + batch.rennetCostTzs + batch.packagingCostTzs + batch.labourCostTzs)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = GeistMonoFamily
                    ),
                    color = Neutral600
                )
            }
        }
    }
}

@Composable
private fun InventoryCard(
    batch: CheeseBatch,
    onRecordSale: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
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
                        text = "${batch.cheeseType} Batch #${batch.id}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral950
                    )
                    Text(
                        text = "Yield: ${batch.yieldKg} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600
                    )
                }
                
                Button(
                    onClick = onRecordSale,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green500,
                        contentColor = Green50
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Record Sale",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "active" -> Pair(Green800.copy(alpha = 0.3f), Green300)
        "aging" -> Pair(Amber800.copy(alpha = 0.3f), Amber300)
        "ready" -> Pair(Blue800.copy(alpha = 0.3f), Blue300)
        "packaged" -> Pair(Purple800.copy(alpha = 0.3f), Purple300)
        "sold" -> Pair(Neutral800.copy(alpha = 0.3f), Neutral300)
        else -> Pair(Neutral800.copy(alpha = 0.3f), Neutral300)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
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
private fun CheeseSaleDialog(
    batch: CheeseBatch,
    onDismiss: () -> Unit,
    onSave: (quantityKg: Double, pricePerKg: Double, buyerName: String) -> Unit
) {
    var buyerName by remember { mutableStateOf("") }
    var quantityKg by remember { mutableStateOf("") }
    var pricePerKg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Sale",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = quantityKg,
                    onValueChange = { quantityKg = it },
                    label = { Text("Quantity (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = pricePerKg,
                    onValueChange = { pricePerKg = it },
                    label = { Text("Price per kg (TZS)") },
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
                    val quantity = quantityKg.toDoubleOrNull() ?: 0.0
                    val price = pricePerKg.toDoubleOrNull() ?: 0.0
                    onSave(quantity, price, buyerName)
                },
                enabled = buyerName.isNotBlank() && quantityKg.isNotBlank() && pricePerKg.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Sale")
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
