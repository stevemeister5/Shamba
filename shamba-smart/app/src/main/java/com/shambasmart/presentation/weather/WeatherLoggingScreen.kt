package com.shambasmart.presentation.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shambasmart.data.local.entity.WeatherLog
import com.shambasmart.presentation.common.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherLoggingScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val weatherLogs by viewModel.allWeatherLogs.collectAsStateWithLifecycle()
    var showLogDialog by remember { mutableStateOf(false) }

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
                    text = "Weather Logging",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Neutral950
                )
                Text(
                    text = "Record daily weather observations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
            }
            
            Button(
                onClick = { showLogDialog = true },
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
                    text = "Log Weather",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Today's Weather
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
                Text(
                    text = "TODAY'S CONDITIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Amber400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Temperature",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "24°C",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Neutral950
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Teal400
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rainfall",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "0 mm",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Neutral950
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Air,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Neutral600
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Wind",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                        Text(
                            text = "12 km/h",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = GeistMonoFamily
                            ),
                            color = Neutral950
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Recent Logs
        Text(
            text = "RECENT LOGS",
            style = MaterialTheme.typography.labelSmall,
            color = Neutral600
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (weatherLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, Neutral200),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Neutral300
                        )
                        Text(
                            text = "No weather logs yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutral600
                        )
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                weatherLogs.take(5).forEach { log ->
                    WeatherLogCard(log = log)
                }
            }
        }
    }
    
    // Log Dialog
    if (showLogDialog) {
        LogWeatherDialog(
            onDismiss = { showLogDialog = false },
            onSave = { log ->
                viewModel.addWeatherLog(log)
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun WeatherLogCard(log: WeatherLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, Neutral200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = log.date.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral950
                )
                Text(
                    text = log.unusualEvents ?: "Normal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = log.maxTemp?.let { "${String.format("%.1f", it)}°C" } ?: "—",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Neutral950
                    )
                    Text(
                        text = "Max",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", log.rainfallMm ?: 0.0)} mm",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GeistMonoFamily
                        ),
                        color = Teal400
                    )
                    Text(
                        text = "Rain",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600
                    )
                }
            }
        }
    }
}

@Composable
private fun LogWeatherDialog(
    onDismiss: () -> Unit,
    onSave: (WeatherLog) -> Unit
) {
    var tempMax by remember { mutableStateOf("") }
    var tempMin by remember { mutableStateOf("") }
    var rainfall by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var windSpeed by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("Clear") }
    var notes by remember { mutableStateOf("") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Weather",
                style = MaterialTheme.typography.headlineMedium,
                color = Neutral950
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Conditions
                Text(
                    text = "Conditions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Clear", "Cloudy", "Rainy", "Stormy", "Foggy", "Windy")) { cond ->
                        FilterChip(
                            selected = conditions == cond,
                            onClick = { conditions = cond },
                            label = { Text(cond) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800.copy(alpha = 0.3f),
                                selectedLabelColor = Green300
                            )
                        )
                    }
                }
                
                // Temperature
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempMax,
                        onValueChange = { tempMax = it },
                        label = { Text("Max Temp (°C)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = tempMin,
                        onValueChange = { tempMin = it },
                        label = { Text("Min Temp (°C)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                // Rainfall
                OutlinedTextField(
                    value = rainfall,
                    onValueChange = { rainfall = it },
                    label = { Text("Rainfall (mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        unfocusedBorderColor = Neutral200
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                // Humidity & Wind
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = humidity,
                        onValueChange = { humidity = it },
                        label = { Text("Humidity (%)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = windSpeed,
                        onValueChange = { windSpeed = it },
                        label = { Text("Wind (km/h)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green500,
                            unfocusedBorderColor = Neutral200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
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
                        WeatherLog(
                            date = today,
                            maxTemp = tempMax.toDoubleOrNull(),
                            minTemp = tempMin.toDoubleOrNull(),
                            rainfallMm = rainfall.toDoubleOrNull(),
                            windLevel = windSpeed.ifBlank { null },
                            unusualEvents = if (conditions != "Clear") conditions else null,
                            notes = notes.ifBlank { null }
                        )
                    )
                },
                enabled = tempMax.isNotBlank(),
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