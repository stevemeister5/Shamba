package com.shambasmart.maarifa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shambasmart.maarifa.contextbridge.ContextBridge

/**
 * Maarifa Context Card — inline knowledge card for any module.
 *
 * Design spec: "Each animal's profile page has a persistent right-panel
 * Maarifa context card that reads that animal's breed, age, lactation stage,
 * weight trend, and last health event — and outputs contextual guidance."
 */
@Composable
fun MaarifaContextCard(
    title: String,
    contextText: String,
    recommendations: List<String> = emptyList(),
    warnings: List<String> = emptyList(),
    onAskMore: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Maarifa: $title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(contextText, style = MaterialTheme.typography.bodySmall)

            if (recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                recommendations.forEach { rec ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("→ ", color = MaterialTheme.colorScheme.primary)
                        Text(rec, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                warnings.forEach { warning ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "⚠ $warning",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (onAskMore != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onAskMore) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ask Maarifa more")
                }
            }
        }
    }
}

/**
 * Livestock-specific context card showing animal-specific guidance.
 */
@Composable
fun MaarifaLivestockContextCard(
    animalTag: String,
    species: String,
    breed: String?,
    age: Int?,
    weightKg: Double?,
    lastHealthEvent: String?,
    isInWithdrawal: Boolean,
    withdrawalEndDate: String?,
    onAskMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contextParts = mutableListOf<String>()
    val recommendations = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    contextParts.add("Animal: $animalTag ($species, ${breed ?: "unknown breed"})")
    age?.let { contextParts.add("Age: $it months") }
    weightKg?.let { contextParts.add("Weight: ${String.format("%.1f", it)} kg") }

    lastHealthEvent?.let {
        contextParts.add("Last health event: $it")
        recommendations.add("Monitor for recurrence if treated recently")
    }

    if (isInWithdrawal) {
        warnings.add("Animal is in withdrawal period until $withdrawalEndDate. Milk and meat must not be used.")
    }

    if (age != null && age < 12 && species == "goat") {
        recommendations.add("Young goat — ensure vaccination schedule is up to date (PPR at 3-4 months, FMD at 4 months)")
    }

    if (weightKg != null && species == "goat" && weightKg < 20) {
        recommendations.add("Underweight — check for internal parasites (FAMACHA scoring recommended)")
    }

    MaarifaContextCard(
        title = "Animal Guidance",
        contextText = contextParts.joinToString(". "),
        recommendations = recommendations,
        warnings = warnings,
        onAskMore = onAskMore,
        modifier = modifier
    )
}

/**
 * Crop-specific context card showing plot-specific guidance.
 */
@Composable
fun MaarifaCropContextCard(
    plotName: String,
    crop: String,
    daysSincePlanting: Int?,
    currentSeason: String,
    onAskMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contextParts = mutableListOf<String>()
    val recommendations = mutableListOf<String>()

    contextParts.add("Plot: $plotName — $crop")
    daysSincePlanting?.let {
        contextParts.add("Day $it after planting")
        when {
            it < 14 -> recommendations.add("Germination/emergence stage — ensure adequate moisture")
            it < 30 -> recommendations.add("Early vegetative — scout for pests, first weeding needed")
            it < 60 -> recommendations.add("Vegetative growth — top dressing with CAN if not yet applied")
            it < 90 -> recommendations.add("Flowering/tasseling — monitor for pests and diseases")
            else -> recommendations.add("Approaching maturity — reduce irrigation, prepare for harvest")
        }
    }

    contextParts.add("Current season: $currentSeason")
    when (currentSeason) {
        "long_rains" -> recommendations.add("Long rains — monitor for grey leaf spot and fungal diseases if humid")
        "short_rains" -> recommendations.add("Short rains — watch for armyworm outbreaks")
        "hot_dry" -> recommendations.add("Hot dry season — ensure irrigation is available")
    }

    MaarifaContextCard(
        title = "Crop Status",
        contextText = contextParts.joinToString(". "),
        recommendations = recommendations,
        onAskMore = onAskMore,
        modifier = modifier
    )
}

/**
 * Cheese-specific context card showing batch guidance.
 */
@Composable
fun MaarifaCheeseContextCard(
    cheeseType: String,
    stage: String,
    onAskMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recommendations = mutableListOf<String>()

    when (stage) {
        "setting" -> recommendations.add("Monitor temperature — do not disturb curd during set time")
        "cutting" -> recommendations.add("Cut curd to uniform size for even moisture release")
        "draining" -> recommendations.add("Turn moulds every 2 hours for even drainage")
        "aging" -> recommendations.add("Maintain 10-12°C and 85% humidity, turn daily")
        "packaging" -> recommendations.add("Check TFDA labelling requirements before packaging")
    }

    MaarifaContextCard(
        title = "Cheese Process",
        contextText = "Current batch: $cheeseType — $stage stage",
        recommendations = recommendations,
        onAskMore = onAskMore,
        modifier = modifier
    )
}

/**
 * Feed-specific context card showing stock guidance.
 */
@Composable
fun MaarifaFeedContextCard(
    feedName: String,
    currentStock: Double,
    reorderThreshold: Double,
    dailyConsumption: Double?,
    onAskMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recommendations = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    val daysRemaining = if (dailyConsumption != null && dailyConsumption > 0) {
        (currentStock / dailyConsumption).toInt()
    } else null

    daysRemaining?.let {
        if (it < 14) {
            warnings.add("Only $it days of $feedName remaining at current consumption rate")
        }
        if (it < 7) {
            warnings.add("URGENT: $feedName stock critically low — reorder immediately")
        }
    }

    if (currentStock <= reorderThreshold) {
        warnings.add("$feedName is below reorder threshold — restock soon")
    }

    MaarifaContextCard(
        title = "Feed Stock",
        contextText = "$feedName: ${String.format("%.0f", currentStock)} units in stock" +
            (daysRemaining?.let { " — approximately $it days supply" } ?: ""),
        recommendations = recommendations,
        warnings = warnings,
        onAskMore = onAskMore,
        modifier = modifier
    )
}