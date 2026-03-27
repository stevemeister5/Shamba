package com.shambasmart.map.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.maps.Style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: OfflineMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDownloadDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Offline Maps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isOfflineMode) "Offline Mode ON" else "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isOfflineMode) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Offline mode toggle
                    IconButton(onClick = { viewModel.toggleOfflineMode() }) {
                        Icon(
                            if (uiState.isOfflineMode) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = "Toggle Offline",
                            tint = if (uiState.isOfflineMode) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDownloadDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download Region")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status card
            item {
                OfflineStatusCard(
                    isOfflineMode = uiState.isOfflineMode,
                    cachedRegionsCount = uiState.cachedRegions.size,
                    totalCacheSize = formatBytes(uiState.totalCacheSize)
                )
            }
            
            // Active downloads section
            if (uiState.activeDownloads.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Downloads",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.activeDownloads.values.toList()) { region ->
                    ActiveDownloadCard(
                        region = region,
                        onCancel = { viewModel.cancelDownload(region.regionId) }
                    )
                }
            }
            
            // Cached regions section
            item {
                Text(
                    text = "Downloaded Regions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (uiState.cachedRegions.isEmpty()) {
                item {
                    EmptyCacheCard()
                }
            } else {
                items(uiState.cachedRegions) { region ->
                    CachedRegionCard(
                        region = region,
                        onDelete = { viewModel.deleteCachedRegion(region.regionId) }
                    )
                }
                
                item {
                    OutlinedButton(
                        onClick = { viewModel.clearAllCache() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Cache")
                    }
                }
            }
        }
    }
    
    // Download dialog
    if (showDownloadDialog) {
        DownloadRegionDialog(
            onDismiss = { showDownloadDialog = false },
            onDownload = { name, minZoom, maxZoom ->
                viewModel.downloadFarmRegion(name, minZoom, maxZoom)
                showDownloadDialog = false
            }
        )
    }
}

@Composable
private fun OfflineStatusCard(
    isOfflineMode: Boolean,
    cachedRegionsCount: Int,
    totalCacheSize: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isOfflineMode) 
                Color(0xFFFF9800).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (isOfflineMode) Color(0xFFFF9800) else Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isOfflineMode) "Offline Mode Active" else "Connected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isOfflineMode) 
                            "Using cached map tiles only" 
                        else 
                            "Online maps + cached tiles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Regions",
                    value = cachedRegionsCount.toString()
                )
                StatItem(
                    label = "Cache Size",
                    value = totalCacheSize
                )
                StatItem(
                    label = "Status",
                    value = if (isOfflineMode) "Offline" else "Online"
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActiveDownloadCard(
    region: OfflineRegionState,
    onCancel: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = region.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatBytes(region.downloadedBytes)} / ${formatBytes(region.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = region.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${(region.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CachedRegionCard(
    region: OfflineRegionState,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = region.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Downloaded • ${formatBytes(region.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyCacheCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Offline Maps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Download map tiles for offline use in areas with poor connectivity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadRegionDialog(
    onDismiss: () -> Unit,
    onDownload: (name: String, minZoom: Int, maxZoom: Int) -> Unit
) {
    var name by remember { mutableStateOf("Farm Area (Korogwe)") }
    var minZoom by remember { mutableStateOf(10f) }
    var maxZoom by remember { mutableStateOf(17f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download Map Region") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Region Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Zoom Levels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lower zoom = overview, Higher zoom = detail",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Min Zoom: ${minZoom.toInt()}")
                Slider(
                    value = minZoom,
                    onValueChange = { minZoom = it },
                    valueRange = 5f..15f,
                    steps = 10
                )
                
                Text("Max Zoom: ${maxZoom.toInt()}")
                Slider(
                    value = maxZoom,
                    onValueChange = { maxZoom = it },
                    valueRange = 12f..19f,
                    steps = 7
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Farm Area: ~10km × 10km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDownload(name, minZoom.toInt(), maxZoom.toInt()) },
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}