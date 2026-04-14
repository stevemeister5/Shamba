package com.shambasmart.presentation.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.presentation.common.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val languages = listOf("English", "Kiswahili")
    val roles = listOf("Owner", "Farm Manager", "Worker")
    val snackbarHostState = remember { SnackbarHostState() }

    // Show messages
    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBase)) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            SettingsHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // User Role Section
            SettingsSection(
                title = "User Role",
                icon = Icons.Outlined.Person
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Role",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral950
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = uiState.userRole,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green500,
                                unfocusedBorderColor = Neutral200,
                                focusedContainerColor = SurfaceSunken,
                                unfocusedContainerColor = SurfaceSunken
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(SurfaceElevated)
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = role,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Neutral950
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.updateUserRole(role)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Farm Profile Section
            SettingsSection(
                title = "Farm Profile",
                icon = Icons.Outlined.Agriculture
            ) {
                OutlinedTextField(
                    value = uiState.farmProfile.name,
                    onValueChange = { viewModel.updateFarmProfile(uiState.farmProfile.copy(name = it)) },
                    label = { Text("Farm Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.farmProfile.location,
                    onValueChange = { viewModel.updateFarmProfile(uiState.farmProfile.copy(location = it)) },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200,
                        focusedContainerColor = SurfaceSunken,
                        unfocusedContainerColor = SurfaceSunken
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.farmProfile.size,
                    onValueChange = { viewModel.updateFarmProfile(uiState.farmProfile.copy(size = it)) },
                    label = { Text("Farm Size") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Section
            SettingsSection(
                title = "Notifications",
                icon = Icons.Outlined.Notifications
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral950
                        )
                        Text(
                            text = "Receive alerts for important events",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral600
                        )
                    }
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotificationsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Green500,
                            checkedTrackColor = Green800.copy(alpha = 0.5f),
                            uncheckedThumbColor = Neutral400,
                            uncheckedTrackColor = Neutral200
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Section
            SettingsSection(
                title = "Data Management",
                icon = Icons.Outlined.CloudSync
            ) {
                OutlinedButton(
                    onClick = { viewModel.exportData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Neutral800
                    ),
                    border = BorderStroke(1.dp, Neutral300),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Data")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.backupData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Backup,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup & Restore")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SettingsSection(
                title = "About",
                icon = Icons.Outlined.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Shamba Smart",
                        style = MaterialTheme.typography.titleLarge,
                        color = Neutral950
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = GeistMonoFamily),
                        color = Neutral600
                    )
                    Text(
                        text = "Farm Management Application",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral800
                    )
                    Text(
                        text = "Korogwe, Tanga, Tanzania",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsHeader() {
    Column {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = Neutral950
        )
        Text(
            text = "Configure your app preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Green400.copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Green400
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Neutral950
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}