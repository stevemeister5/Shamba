package com.shambasmart.map.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

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
                    Text(
                        text = "Offline Maps",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF8FAF9)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF8A9E96)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color(0xFF8A9E96)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1210)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0D1210))
        ) {
            // Map preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF202C27), RoundedCornerShape(14.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)
                            controller.setCenter(GeoPoint(-5.15, 38.48))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cache info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF141A17)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Cache Information",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF8FAF9)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Cached Tiles",
                                fontSize = 13.sp,
                                color = Color(0xFF8A9E96)
                            )
                            Text(
                                text = "${viewModel.getCachedTileCount()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFFF8FAF9),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cache Size",
                                fontSize = 13.sp,
                                color = Color(0xFF8A9E96)
                            )
                            Text(
                                text = viewModel.getCacheSizeFormatted(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFFF8FAF9),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cached regions list
            if (uiState.cachedRegions.isNotEmpty()) {
                Text(
                    text = "Cached Regions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF8FAF9),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.cachedRegions) { region ->
                        OfflineRegionItem(
                            region = region,
                            onDelete = { viewModel.deleteRegion(region.regionId) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF2E3D37)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No Cached Maps",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC4CEC9)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Download map regions for offline use",
                            fontSize = 15.sp,
                            color = Color(0xFF8A9E96)
                        )
                    }
                }
            }
        }
    }

    // Download dialog
    if (showDownloadDialog) {
        DownloadMapDialog(
            onDismiss = { showDownloadDialog = false },
            onDownload = { name, bounds ->
                viewModel.downloadRegion(name, bounds)
                showDownloadDialog = false
            }
        )
    }
}

@Composable
fun OfflineRegionItem(
    region: OfflineRegionState,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141A17)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = region.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF8FAF9)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${region.totalBytes / (1024 * 1024)} MB",
                    fontSize = 13.sp,
                    color = Color(0xFF8A9E96)
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF8A9E96)
                )
            }
        }
    }
}

@Composable
fun DownloadMapDialog(
    onDismiss: () -> Unit,
    onDownload: (String, BoundingBox) -> Unit
) {
    var regionName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141A17),
        title = {
            Text(
                text = "Download Map Region",
                color = Color(0xFFF8FAF9)
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a name for this map region:",
                    color = Color(0xFF8A9E96)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = regionName,
                    onValueChange = { regionName = it },
                    label = { Text("Region Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E9E58),
                        unfocusedBorderColor = Color(0xFF202C27)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Placeholder - would need actual bounds calculation
                    val bounds = BoundingBox(-5.2, 38.5, -5.1, 38.4)
                    onDownload(regionName, bounds)
                },
                enabled = regionName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E9E58)
                )
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8A9E96))
            }
        }
    )
}

// Placeholder for BoundingBox - would need OSMDroid equivalent
data class BoundingBox(
    val latNorth: Double,
    val lonEast: Double,
    val latSouth: Double,
    val lonWest: Double
)