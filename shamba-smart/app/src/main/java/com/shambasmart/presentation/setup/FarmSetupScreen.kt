package com.shambasmart.presentation.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

data class SetupStep(
    val id: Int,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isCompleted: Boolean = false,
    val isRequired: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSetupScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: FarmSetupViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val setupProgress by viewModel.setupProgress.collectAsState()
    val farmName by viewModel.farmName.collectAsState()
    val farmLocation by viewModel.farmLocation.collectAsState()
    val totalAcres by viewModel.totalAcres.collectAsState()

    val setupSteps = remember {
        listOf(
            SetupStep(1, "Farm Profile", "Enter basic farm information", Icons.Default.Home),
            SetupStep(2, "Map Boundaries", "Draw farm boundaries on map", Icons.Default.Map),
            SetupStep(3, "Plan Buildings", "Plan shelters, storage, facilities", Icons.Default.Business),
            SetupStep(4, "Setup Plots", "Divide land into cultivation plots", Icons.Default.Landscape),
            SetupStep(5, "Induct Flock", "Add your initial animals", Icons.Default.Pets),
            SetupStep(6, "Plant Crops", "Plan first season crops", Icons.Default.Grass),
            SetupStep(7, "Soil Testing", "Record soil test results", Icons.Default.Science),
            SetupStep(8, "Review & Start", "Review setup and start farming", Icons.Default.CheckCircle)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Farm Setup Wizard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step $currentStep of ${setupSteps.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { setupProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Step content
            when (currentStep) {
                1 -> FarmProfileStep(
                    farmName = farmName,
                    farmLocation = farmLocation,
                    totalAcres = totalAcres,
                    onFarmNameChange = { viewModel.setFarmName(it) },
                    onFarmLocationChange = { viewModel.setFarmLocation(it) },
                    onTotalAcresChange = { viewModel.setTotalAcres(it) }
                )
                2 -> MapBoundariesStep(viewModel = viewModel)
                3 -> PlanBuildingsStep(viewModel = viewModel)
                4 -> SetupPlotsStep(viewModel = viewModel)
                5 -> InductFlockStep(viewModel = viewModel)
                6 -> PlantCropsStep(viewModel = viewModel)
                7 -> SoilTestingStep(viewModel = viewModel)
                8 -> ReviewStep(
                    farmName = farmName,
                    farmLocation = farmLocation,
                    totalAcres = totalAcres,
                    setupSteps = setupSteps,
                    onComplete = onComplete
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < setupSteps.size) {
                            viewModel.nextStep()
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (currentStep < setupSteps.size) "Next" else "Complete Setup")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (currentStep < setupSteps.size) Icons.Default.ArrowForward else Icons.Default.Check,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmProfileStep(
    farmName: String,
    farmLocation: String,
    totalAcres: String,
    onFarmNameChange: (String) -> Unit,
    onFarmLocationChange: (String) -> Unit,
    onTotalAcresChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Farm Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your farm's basic information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = farmName,
            onValueChange = onFarmNameChange,
            label = { Text("Farm Name") },
            placeholder = { Text("e.g., Shamba Smart Farm") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = farmLocation,
            onValueChange = onFarmLocationChange,
            label = { Text("Location") },
            placeholder = { Text("e.g., Korogwe, Tanga") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = totalAcres,
            onValueChange = onTotalAcresChange,
            label = { Text("Total Acres") },
            placeholder = { Text("e.g., 16") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Accurate farm size helps calculate feed requirements, plot divisions, and financial projections.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MapBoundariesStep(viewModel: FarmSetupViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Map Farm Boundaries",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Walk around your farm perimeter to mark boundaries",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AR Boundary Mapping")
                    Text(
                        "Use ARCore to mark boundaries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* Start AR mapping */ }) {
                        Icon(Icons.Default.Camera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start AR Mapping")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mapping Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Walk slowly along the farm boundary")
                Text("• Mark corners by tapping the screen")
                Text("• Ensure GPS signal is strong")
                Text("• Complete the full perimeter loop")
            }
        }
    }
}

@Composable
private fun PlanBuildingsStep(viewModel: FarmSetupViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    val buildings by viewModel.plannedBuildings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Plan Buildings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Plan your farm infrastructure",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Building")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (buildings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No buildings planned yet")
                    Text(
                        "Tap + to add shelters, storage, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(buildings) { building ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                getBuildingIcon(building.type),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = building.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = building.type,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.removeBuilding(building) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBuildingDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { building ->
                viewModel.addBuilding(building)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SetupPlotsStep(viewModel: FarmSetupViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Setup Cultivation Plots",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Divide your land into cultivation plots",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Plot Division Strategy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Allocate 60% for pasture (Napier grass)")
                Text("• Allocate 30% for food crops")
                Text("• Allocate 10% for vegetables")
                Text("• Reserve space for infrastructure")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Open plot editor */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Landscape, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Plot Editor")
        }
    }
}

@Composable
private fun InductFlockStep(viewModel: FarmSetupViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Induct Your Flock",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your initial animals to the system",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quick Add Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAddButton(
                        icon = Icons.Default.Pets,
                        label = "Add Goats",
                        onClick = { /* Add goats */ }
                    )
                    QuickAddButton(
                        icon = Icons.Default.Pets,
                        label = "Add Sheep",
                        onClick = { /* Add sheep */ }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* Import from file */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import from Spreadsheet")
        }
    }
}

@Composable
private fun QuickAddButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PlantCropsStep(viewModel: FarmSetupViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Plan First Season Crops",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Plan what to plant in your first season",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recommended First Crops",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Napier grass (for silage)")
                Text("• Maize (for food & silage)")
                Text("• Beans (nitrogen fixing)")
                Text("• Tomatoes (quick return)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Open crop planner */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Grass, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Crop Planner")
        }
    }
}

@Composable
private fun SoilTestingStep(viewModel: FarmSetupViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Soil Testing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Record soil test results for each plot",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Soil Parameters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• pH level (target: 6.0-7.0)")
                Text("• Nitrogen (N) content")
                Text("• Phosphorus (P) content")
                Text("• Potassium (K) content")
                Text("• Organic matter percentage")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Record soil test */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Science, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Soil Test")
        }
    }
}

@Composable
private fun ReviewStep(
    farmName: String,
    farmLocation: String,
    totalAcres: String,
    setupSteps: List<SetupStep>,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Review & Start",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Review your farm setup before starting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Farm Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                ReviewRow("Farm Name", farmName)
                ReviewRow("Location", farmLocation)
                ReviewRow("Total Acres", totalAcres)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Setup Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                setupSteps.forEach { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (step.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ready to Start!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your farm is set up and ready to go. You can now start managing your livestock, crops, and operations.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AddBuildingDialog(
    onDismiss: () -> Unit,
    onAdd: (PlannedBuilding) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Shelter") }

    val buildingTypes = listOf("Shelter", "Storage", "Water Point", "Cheese Room", "Compost Pit", "Fence")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Building") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Building Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Building Type:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                buildingTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(PlannedBuilding(name = name, type = selectedType))
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getBuildingIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        "Shelter" -> Icons.Default.Home
        "Storage" -> Icons.Default.Inventory
        "Water Point" -> Icons.Default.Water
        "Cheese Room" -> Icons.Default.Cheese
        "Compost Pit" -> Icons.Default.Compost
        "Fence" -> Icons.Default.Fence
        else -> Icons.Default.Business
    }
}

data class PlannedBuilding(
    val name: String,
    val type: String
)