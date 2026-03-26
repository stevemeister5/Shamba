package com.shambasmart.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarRoomScaffold(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        // Single pane layout for phones
        Scaffold(
            bottomBar = {
                WarRoomBottomBar(navController = navController)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                listContent()
            }
        }
    } else {
        // Dual pane layout for tablets (Xiaomi Pad 7)
        Row(modifier = Modifier.fillMaxSize()) {
            // Left pane - List (40% width)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f),
                tonalElevation = 1.dp
            ) {
                Column {
                    // Left pane header
                    WarRoomLeftPaneHeader()
                    // List content
                    Box(modifier = Modifier.weight(1f)) {
                        listContent()
                    }
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Right pane - Detail (60% width)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f),
                tonalElevation = 0.dp
            ) {
                detailContent()
            }
        }
    }
}

@Composable
private fun WarRoomLeftPaneHeader() {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Shamba Smart",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "War Room Dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WarRoomBottomBar(navController: NavHostController) {
    NavigationBar {
        // Dashboard
        NavigationBarItem(
            icon = { /* Dashboard Icon */ },
            label = { Text("Dashboard") },
            selected = true,
            onClick = { /* Navigate */ }
        )
        // Livestock
        NavigationBarItem(
            icon = { /* Livestock Icon */ },
            label = { Text("Livestock") },
            selected = false,
            onClick = { /* Navigate */ }
        )
        // Crops
        NavigationBarItem(
            icon = { /* Crops Icon */ },
            label = { Text("Crops") },
            selected = false,
            onClick = { /* Navigate */ }
        )
        // More
        NavigationBarItem(
            icon = { /* More Icon */ },
            label = { Text("More") },
            selected = false,
            onClick = { /* Navigate */ }
        )
    }
}