package com.shambasmart.maarifa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shambasmart.maarifa.*
import com.shambasmart.maarifa.retrieval.ResponseAssembler

/**
 * Maarifa Symptom Checker — guided step-by-step diagnostic wizard.
 *
 * Design spec: "A full-screen guided diagnostic wizard launched from
 * within the Ask panel or from any animal health record. Text and touch only."
 *
 * Steps:
 * 1. Select species (Goat / Sheep)
 * 2. Select animal (from herd or Unknown)
 * 3. Body system affected
 * 4. Select symptoms present (checkboxes)
 * 5. Duration
 * 6. How many animals affected
 * 7. Recent events before symptoms
 * 8. Output — differential diagnoses with confidence
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomCheckerScreen(
    viewModel: MaarifaViewModel,
    onDismiss: () -> Unit
) {
    val symptomState by viewModel.symptomState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptom Checker") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { symptomState.currentStep / 8f },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Step ${symptomState.currentStep} of 8",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (symptomState.currentStep) {
                1 -> StepSelectSpecies(viewModel, symptomState)
                2 -> StepSelectAnimal(viewModel, symptomState)
                3 -> StepBodySystem(viewModel, symptomState)
                4 -> StepSelectSymptoms(viewModel, symptomState)
                5 -> StepDuration(viewModel, symptomState)
                6 -> StepAffectedCount(viewModel, symptomState)
                7 -> StepRecentEvents(viewModel, symptomState)
                8 -> StepResults(viewModel, symptomState, onDismiss)
            }
        }
    }
}

// === STEP 1: SELECT SPECIES ===

@Composable
private fun StepSelectSpecies(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("Which animal is affected?", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    listOf("Goat", "Sheep").forEach { species ->
        Card(
            onClick = { viewModel.symptomCheckerSelectSpecies(species.lowercase()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (species == "Goat") Icons.Default.Pets else Icons.Default.Pets,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(species, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// === STEP 2: SELECT ANIMAL ===

@Composable
private fun StepSelectAnimal(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("Select the animal", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        onClick = { viewModel.symptomCheckerSelectAnimal(null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PersonSearch, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Unknown animal", style = MaterialTheme.typography.titleMedium)
                Text("Not in herd register", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // Note: Animal selection from herd is not yet implemented
    // For now, only "Unknown animal" option is available
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Note: Selecting specific animals from your herd will be available in a future update.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline
    )
}

// === STEP 3: BODY SYSTEM ===

@Composable
private fun StepBodySystem(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("Which body system is affected?", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    val systems = listOf(
        "Respiratory" to Icons.Default.Air,
        "Digestive" to Icons.Default.Restaurant,
        "Reproductive" to Icons.Default.ChildCare,
        "Skin and Hooves" to Icons.Default.TouchApp,
        "Nervous System" to Icons.Default.Psychology,
        "General and Lethargy" to Icons.Default.SentimentDissatisfied,
        "Udder and Milk" to Icons.Default.WaterDrop
    )

    systems.forEach { (system, icon) ->
        Card(
            onClick = { viewModel.symptomCheckerSelectBodySystem(system) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(system, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// === STEP 4: SELECT SYMPTOMS ===

@Composable
private fun StepSelectSymptoms(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("Select all symptoms present", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Body system: ${state.bodySystem}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline
    )
    Spacer(modifier = Modifier.height(16.dp))

    val symptomsBySystem = mapOf(
        "Respiratory" to listOf(
            "Coughing", "Sneezing", "Nasal discharge", "Rapid breathing",
            "Laboured breathing", "Wheezing"
        ),
        "Digestive" to listOf(
            "Diarrhoea", "Not eating", "Bloating", "Drooling",
            "Abdominal pain", "Vomiting", "Constipation"
        ),
        "Reproductive" to listOf(
            "Retained placenta", "Vaginal discharge", "Prolapse",
            "Dystocia", "Abortion", "Not cycling", "Infertility"
        ),
        "Skin and Hooves" to listOf(
            "Skin lesions", "Mange", "Lice", "Wounds",
            "Footrot", "Lameness", "Swollen joint", "Rash"
        ),
        "Nervous System" to listOf(
            "Head tilt", "Circling", "Convulsion", "Tremor",
            "Paralysis", "Staggering", "Blindness"
        ),
        "General and Lethargy" to listOf(
            "Fever", "Weakness", "Weight loss", "Lethargy",
            "Shivering", "Recumbent", "Pale gums"
        ),
        "Udder and Milk" to listOf(
            "Hard udder", "Clotted milk", "Reduced milk",
            "Udder swelling", "Milk discoloration"
        )
    )

    val symptoms = symptomsBySystem[state.bodySystem] ?: emptyList()

    symptoms.forEach { symptom ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = symptom in state.selectedSymptoms,
                onCheckedChange = { viewModel.symptomCheckerToggleSymptom(symptom) }
            )
            Text(symptom, style = MaterialTheme.typography.bodyMedium)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { viewModel.symptomCheckerSetDuration("") },
        enabled = state.selectedSymptoms.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Next")
    }
}

// === STEP 5: DURATION ===

@Composable
private fun StepDuration(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("When did symptoms start?", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    listOf(
        "Started today",
        "2 to 3 days ago",
        "More than 3 days ago"
    ).forEach { duration ->
        Card(
            onClick = { viewModel.symptomCheckerSetDuration(duration) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(duration, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// === STEP 6: AFFECTED COUNT ===

@Composable
private fun StepAffectedCount(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("How many animals are affected?", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    listOf(
        "Only this animal",
        "2 to 3 others also showing signs",
        "Many animals in the herd"
    ).forEach { count ->
        Card(
            onClick = { viewModel.symptomCheckerSetAffectedCount(count) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Groups, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(count, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// === STEP 7: RECENT EVENTS ===

@Composable
private fun StepRecentEvents(viewModel: MaarifaViewModel, state: SymptomCheckerState) {
    Text("Any recent events before symptoms?", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    val events = listOf(
        "New animal introduced to herd",
        "Recent handling or stress",
        "Change in feed or water source",
        "Heavy rainfall or flooding",
        "Recent vaccination",
        "None of the above"
    )

    events.forEach { event ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = event in state.recentEvents,
                onCheckedChange = {
                    viewModel.symptomCheckerSetRecentEvents(
                        if (event in state.recentEvents) state.recentEvents - event
                        else state.recentEvents + event
                    )
                }
            )
            Text(event, style = MaterialTheme.typography.bodyMedium)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { viewModel.symptomCheckerSubmit() },
        enabled = state.recentEvents.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text("Get Results")
        }
    }
}

// === STEP 8: RESULTS ===

@Composable
private fun StepResults(viewModel: MaarifaViewModel, state: SymptomCheckerState, onDismiss: () -> Unit) {
    val answer = state.result

    if (answer == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Text("Diagnostic Results", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))

    // Confidence tier
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (answer.confidenceTier) {
                1 -> MaterialTheme.colorScheme.primaryContainer
                2 -> MaterialTheme.colorScheme.tertiaryContainer
                3 -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (answer.confidenceTier) {
                    1 -> Icons.Default.Verified
                    2 -> Icons.Default.CheckCircle
                    3 -> Icons.Default.Warning
                    else -> Icons.Default.ErrorOutline
                },
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(answer.confidenceLabel, style = MaterialTheme.typography.titleSmall)
                Text(answer.confidenceDescription, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Differential diagnoses
    answer.sections.forEach { section ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(section.content, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // Warnings
    answer.warnings.forEach { warning ->
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (warning.type) {
                    ResponseAssembler.WarningType.NOTIFIABLE_DISEASE,
                    ResponseAssembler.WarningType.OUTBREAK_ALERT ->
                        MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠ ${warning.message}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    // Recommended action
    answer.recommendedAction?.let { action ->
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Recommended Action", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(action, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedButton(
        onClick = { viewModel.closeSymptomChecker(); onDismiss() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Close Symptom Checker")
    }
}