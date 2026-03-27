package com.shambasmart.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.maarifa.ui.MaarifaFloatingTab
import com.shambasmart.maarifa.ui.MaarifaSidePanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    maarifaViewModel: MaarifaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val herdSize by viewModel.herdSize.collectAsStateWithLifecycle()
    val goatCount by viewModel.goatCount.collectAsStateWithLifecycle()
    val sheepCount by viewModel.sheepCount.collectAsStateWithLifecycle()
    val todayMilkYield by viewModel.todayMilkYield.collectAsStateWithLifecycle()

    // Maarifa state
    val maarifaUiState by maarifaViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Farm Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { viewModel.refreshDashboard() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Alerts Section
        if (uiState.hasAlerts) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.pendingTaskCount > 0) {
                        Text("• ${uiState.pendingTaskCount} pending task(s)")
                    }
                    if (uiState.lowFeedAlerts > 0) {
                        Text("• ${uiState.lowFeedAlerts} low feed stock alert(s)")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // KPI Cards
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KPICard(
                title = "Herd Size",
                value = "$herdSize",
                subtitle = "Goats: $goatCount | Sheep: $sheepCount",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            KPICard(
                title = "Milk Today",
                value = "${todayMilkYield ?: 0} L",
                subtitle = "Total yield",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KPICard(
                title = "Cheese",
                value = "0 kg",
                subtitle = "In inventory",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            KPICard(
                title = "Tasks",
                value = "${uiState.pendingTaskCount}",
                subtitle = "Pending today",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Tasks Section
        Text(
            text = "Today's Tasks",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (uiState.pendingTaskCount > 0) {
                    Text("You have ${uiState.pendingTaskCount} task(s) pending for today")
                } else {
                    Text("No tasks pending for today")
                }
            }
        }
    }

    // Maarifa Side Panel
    MaarifaSidePanel(
        isOpen = maarifaUiState.isPanelOpen,
        selectedTab = maarifaUiState.selectedTab,
        onTabSelected = { maarifaViewModel.selectTab(it) },
        onClose = { maarifaViewModel.closePanel() },
        viewModel = maarifaViewModel
    )

    // Maarifa Floating Tab
    MaarifaFloatingTab(
        onClick = { maarifaViewModel.togglePanel() }
    )
    } // Close Box
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}