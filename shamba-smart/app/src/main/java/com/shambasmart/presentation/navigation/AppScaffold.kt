package com.shambasmart.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shambasmart.maarifa.ui.MaarifaSidePanel
import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.presentation.common.theme.*

@Composable
fun AppScaffold(
    currentRoute: String,
    farmName: String = "Shamba Smart",
    weatherSummary: String = "24°C Sunny",
    userRole: UserRole = UserRole.OWNER,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    maarifaViewModel: MaarifaViewModel,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isCompact = configuration.screenWidthDp < 600 // Simple adaptive check
    val maarifaUiState by maarifaViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (isCompact) {
                AdaptiveBottomBar(currentRoute = currentRoute, onNavigate = onNavigate)
            }
        }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Left: Navigation Rail (Only for non-compact)
            if (!isCompact) {
                NavigationRail(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate
                )
            }
            
            // Center: Main Content Area
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TopBar(
                    farmName = farmName,
                    currentModule = getModuleLabel(currentRoute),
                    weatherSummary = weatherSummary,
                    onSearch = onSearch,
                    onNotificationsClick = onNotificationsClick,
                    onMaarifaClick = { maarifaViewModel.togglePanel() }
                )
                
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(SurfaceBase)) {
                    content()
                }
            }
            
            // Right: Maarifa Side Panel
            AnimatedVisibility(
                visible = maarifaUiState.isPanelOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                MaarifaSidePanel(
                    isOpen = maarifaUiState.isPanelOpen,
                    selectedTab = maarifaUiState.selectedTab,
                    onTabSelected = { maarifaViewModel.selectTab(it) },
                    onClose = { maarifaViewModel.closePanel() },
                    viewModel = maarifaViewModel
                )
            }
        }
    }
}

@Composable
private fun AdaptiveBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceRaised,
        tonalElevation = 8.dp
    ) {
        // Show top 5 items on bottom bar for compact view
        allNavigationItems.take(5).forEach { item ->
            val isSelected = currentRoute.startsWith(item.route.split("/")[0])
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(if (isSelected) item.selectedIcon else item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Green400,
                    indicatorColor = Green900.copy(alpha = 0.2f)
                )
            )
        }
    }
}

private fun getModuleLabel(route: String): String {
    return allNavigationItems.find { route.startsWith(it.route.split("/")[0]) }?.label ?: "Dashboard"
}

@Composable
private fun NavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier.width(72.dp).fillMaxHeight().background(SurfaceRaised).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Green500), contentAlignment = Alignment.Center) {
            Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        allNavigationItems.forEach { item ->
            val isSelected = currentRoute.startsWith(item.route.split("/")[0])
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(if (isSelected) item.selectedIcon else item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp)) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Green400,
                    selectedTextColor = Green400,
                    unselectedIconColor = Neutral600,
                    unselectedTextColor = Neutral600,
                    indicatorColor = Green900.copy(alpha = 0.2f)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TopBar(
    farmName: String,
    currentModule: String,
    weatherSummary: String,
    onSearch: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMaarifaClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth().height(56.dp), color = SurfaceRaised, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = farmName, style = MaterialTheme.typography.titleMedium, color = Neutral950)
                Text(text = "›", color = Neutral400)
                Text(text = currentModule, style = MaterialTheme.typography.bodyMedium, color = Neutral600)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SyncStatusIndicator()
                Surface(shape = RoundedCornerShape(8.dp), color = Neutral100) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp), tint = Amber400)
                        Text(text = weatherSummary, style = MaterialTheme.typography.bodySmall.copy(fontFamily = GeistMonoFamily), color = Neutral800)
                    }
                }
                IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Neutral600) }
                IconButton(onClick = onNotificationsClick) { Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Neutral600) }
                IconButton(onClick = onMaarifaClick) { Icon(Icons.Outlined.Eco, contentDescription = "Maarifa", tint = Green400) }
            }
        }
    }
}

@Composable
private fun SyncStatusIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(Green400, CircleShape))
        Text(text = "Synced", style = MaterialTheme.typography.labelSmall, color = Neutral600)
    }
}
