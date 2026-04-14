package com.shambasmart.maarifa.retrieval

import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.maarifa.contextbridge.ContextBridge
import com.shambasmart.maarifa.rules.RuleEngine
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toJavaLocalDate

/**
 * Maarifa Response Assembler — consistency checking and answer assembly.
 *
 * Design spec: "Before assembling the final answer, run a consistency checker
 * across all retrieved chunks. Species consistency, contradiction detection,
 * temporal consistency, notifiable disease check."
 *
 * Four-tier confidence model:
 * Tier 1 — RULE-GOVERNED (highest reliability)
 * Tier 2 — RETRIEVAL-CONFIRMED (high reliability)
 * Tier 3 — RETRIEVAL-PARTIAL (moderate reliability)
 * Tier 4 — NOT FOUND (honest failure)
 */
class ResponseAssembler(
    private val ruleEngine: RuleEngine,
    private val retriever: KnowledgeRetriever
) {

    data class MaarifaAnswer(
        val summary: String,
        val sections: List<AnswerSection>,
        val farmNote: String?,
        val recommendedAction: String?,
        val warnings: List<Warning>,
        val sources: List<SourceAttribution>,
        val relatedTopics: List<String>,
        val confidenceTier: Int,
        val confidenceLabel: String,
        val confidenceDescription: String
    )

    data class AnswerSection(
        val title: String,
        val content: String,
        val isCollapsible: Boolean = false
    )

    data class Warning(
        val type: WarningType,
        val message: String
    )

    enum class WarningType {
        NOTIFIABLE_DISEASE,
        VETERINARIAN_CONFIRMATION,
        WITHDRAWAL_BLOCK,
        OUTBREAK_ALERT,
        PLANTING_ADVISORY,
        DATA_QUALITY
    }

    data class SourceAttribution(
        val title: String,
        val type: String,
        val credibility: String
    )

    suspend fun assembleAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult
    ): MaarifaAnswer {
        // Consistency checks
        val filteredChunks = applyConsistencyChecks(chunks, classification)

        // Determine confidence tier
        val tier = determineConfidenceTier(filteredChunks, classification)

        // Build answer based on intent
        return when (classification.primaryIntent) {
            "dosage_lookup" -> buildDosageAnswer(query, filteredChunks, context, classification, tier)
            "withdrawal_lookup" -> buildWithdrawalAnswer(query, filteredChunks, context, classification, tier)
            "symptom_query" -> buildSymptomAnswer(query, filteredChunks, context, classification, tier)
            "planting_advice" -> buildPlantingAnswer(query, filteredChunks, context, classification, tier)
            "breeding_query" -> buildBreedingAnswer(query, filteredChunks, context, classification, tier)
            "cheese_process" -> buildCheeseAnswer(query, filteredChunks, context, classification, tier)
            "weather_risk" -> buildWeatherAnswer(query, filteredChunks, context, classification, tier)
            "nutrition_query" -> buildNutritionAnswer(query, filteredChunks, context, classification, tier)
            else -> buildGeneralAnswer(query, filteredChunks, context, classification, tier)
        }
    }

    private fun applyConsistencyChecks(
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        classification: IntentClassifier.ClassificationResult
    ): List<KnowledgeRetriever.ScoredChunk> {
        var filtered = chunks

        // Species consistency
        val species = classification.entities.species
        if (species != null) {
            val speciesMatched = filtered.filter { sc ->
                val tags = sc.chunk.getTopicTagsList()
                tags.any { it.equals(species, ignoreCase = true) } ||
                tags.any { it in listOf("general", "multi-species") }
            }
            if (speciesMatched.isNotEmpty()) filtered = speciesMatched
        }

        // Deduplicate by id
        filtered = filtered.distinctBy { it.chunk.id }

        return filtered
    }

    private fun determineConfidenceTier(
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        classification: IntentClassifier.ClassificationResult
    ): Int {
        return when {
            chunks.size >= 3 && chunks.all { it.finalScore > 0.5f } && !classification.isAmbiguous -> 2
            chunks.size >= 1 && chunks.first().finalScore > 0.3f -> 3
            chunks.isEmpty() -> 4
            classification.isAmbiguous -> 3
            else -> 3
        }
    }

    private suspend fun buildDosageAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val drug = classification.entities.drugName ?: "the drug"
        val species = classification.entities.species ?: "the animal"
        val weightKg = classification.entities.quantityKg

        val warnings = mutableListOf<Warning>()
        val sections = mutableListOf<AnswerSection>()

        // Try rule engine for precise dose
        val doseResult = if (drug != null && weightKg != null && species != null) {
            try {
                ruleEngine.calculateDose(drug, species, weightKg)
            } catch (e: Exception) { null }
        } else null

        if (doseResult != null) {
            sections.add(AnswerSection(
                "Calculated Dose",
                "Drug: ${doseResult.drugGeneric}\n" +
                "Animal weight: ${doseResult.bodyWeightKg} kg\n" +
                "Total dose: ${String.format("%.1f", doseResult.totalMg)} mg\n" +
                "Volume: ${String.format("%.1f", doseResult.recommendedMl)} ml\n" +
                "Route: ${doseResult.route}",
                false
            ))
        }

        // Add chunk content
        val guidanceChunks = chunks.take(3)
        for (sc in guidanceChunks) {
            sections.add(AnswerSection(
                sc.chunk.sourceTitle,
                sc.chunk.displayText,
                true
            ))
        }

        // Safety warnings
        warnings.add(Warning(
            WarningType.VETERINARIAN_CONFIRMATION,
            "Always confirm dosage and treatment with a licensed veterinarian before administering."
        ))

        if (doseResult != null) {
            warnings.add(Warning(
                WarningType.WITHDRAWAL_BLOCK,
                "Check withdrawal period before using milk or meat from treated animal."
            ))
        }

        val sources = chunks.take(5).map {
            SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
        }

        return MaarifaAnswer(
            summary = "Dosage information for $drug in $species.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Confirm dose with veterinarian. Check withdrawal period before administering.",
            warnings = warnings,
            sources = sources,
            relatedTopics = listOf("Withdrawal periods", "Injection technique", "Drug storage"),
            confidenceTier = if (doseResult != null) 1 else tier,
            confidenceLabel = confidenceLabel(if (doseResult != null) 1 else tier),
            confidenceDescription = confidenceDescription(if (doseResult != null) 1 else tier)
        )
    }

    private suspend fun buildWithdrawalAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val drug = classification.entities.drugName ?: "the drug"
        val species = classification.entities.species ?: "the animal"
        val treatmentDate = context?.today

        val warnings = mutableListOf<Warning>()
        val sections = mutableListOf<AnswerSection>()

        // Try rule engine
        val withdrawalResult = if (drug != null && species != null && treatmentDate != null) {
            try { ruleEngine.calculateWithdrawal(drug, species, treatmentDate.toJavaLocalDate()) } catch (e: Exception) { null }
        } else null

        if (withdrawalResult != null) {
            sections.add(AnswerSection(
                "Withdrawal Period",
                "Drug: ${withdrawalResult.drugGeneric}\n" +
                "Treatment date: ${withdrawalResult.treatmentDate}\n" +
                "Milk safe date: ${withdrawalResult.milkSafeDate ?: "Not applicable"}\n" +
                "Meat safe date: ${withdrawalResult.meatSafeDate ?: "Not applicable"}",
                false
            ))
            warnings.add(Warning(
                WarningType.WITHDRAWAL_BLOCK,
                "Do not use milk or meat until withdrawal period has passed."
            ))
        }

        for (sc in chunks.take(3)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        return MaarifaAnswer(
            summary = "Withdrawal period information for $drug in $species.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Mark animal's milk and meat collection as blocked until safe date.",
            warnings = warnings,
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Drug dosing", "Milk quality", "Meat safety"),
            confidenceTier = if (withdrawalResult != null) 1 else tier,
            confidenceLabel = confidenceLabel(if (withdrawalResult != null) 1 else tier),
            confidenceDescription = confidenceDescription(if (withdrawalResult != null) 1 else tier)
        )
    }

    private fun buildSymptomAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val species = classification.entities.species ?: "the animal"
        val symptoms = classification.entities.symptoms

        val warnings = mutableListOf<Warning>()
        val sections = mutableListOf<AnswerSection>()

        // Check for notifiable diseases
        val notifiableDiseases = listOf("PPR", "FMD", "Anthrax", "Brucellosis", "CCPP",
            "African Swine Fever", "Rabies", "Lumpy Skin Disease")

        val diseaseChunks = chunks.filter { sc ->
            val text = sc.chunk.displayText.lowercase()
            notifiableDiseases.any { text.contains(it.lowercase()) }
        }

        if (diseaseChunks.isNotEmpty()) {
            val foundDiseases = notifiableDiseases.filter { disease ->
                diseaseChunks.any { it.chunk.displayText.lowercase().contains(disease.lowercase()) }
            }
            warnings.add(Warning(
                WarningType.NOTIFIABLE_DISEASE,
                "${foundDiseases.joinToString()} ${if (foundDiseases.size == 1) "is" else "are"} a notifiable disease(s) in Tanzania. " +
                "Contact the Tanzania Veterinary Laboratory Agency (TVLA) or your district veterinary officer immediately."
            ))
        }

        // Build differential diagnoses from chunks
        val topDifferentials = chunks.take(3)
        for ((index, sc) in topDifferentials.withIndex()) {
            val confidence = when {
                index == 0 && sc.finalScore > 0.6f -> "High"
                index == 0 -> "Medium"
                index == 1 -> "Medium"
                else -> "Low"
            }
            sections.add(AnswerSection(
                "Possible cause ${index + 1}: $confidence confidence",
                sc.chunk.displayText,
                true
            ))
        }

        // Herd risk assessment
        val herdRisk = when {
            symptoms.size >= 3 -> "Monitor other animals — multiple symptoms may indicate serious condition"
            diseaseChunks.isNotEmpty() -> "Potential outbreak risk — isolate and monitor herd"
            else -> "Isolated case — monitor for 48 hours"
        }
        sections.add(AnswerSection("Herd Risk Assessment", herdRisk, false))

        warnings.add(Warning(
            WarningType.VETERINARIAN_CONFIRMATION,
            "Symptom checker output shows possible causes — not a definitive diagnosis. " +
            "Always consult a veterinarian for confirmation."
        ))

        return MaarifaAnswer(
            summary = "Possible causes of ${symptoms.joinToString(", ").ifEmpty { "reported symptoms" }} in $species.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Observe animal closely for 24 hours. If symptoms worsen or more animals are affected, call a veterinarian.",
            warnings = warnings,
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Symptom checker", "Disease prevention", "Herd biosecurity"),
            confidenceTier = tier,
            confidenceLabel = confidenceLabel(tier),
            confidenceDescription = confidenceDescription(tier)
        )
    }

    private suspend fun buildPlantingAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val crop = classification.entities.crop ?: "the crop"
        val sections = mutableListOf<AnswerSection>()
        val warnings = mutableListOf<Warning>()

        // Try planting window rule
        val plantingResult = if (crop != null) {
            try { ruleEngine.getPlantingWindow(crop, "korogwe") } catch (e: Exception) { null }
        } else null

        if (plantingResult != null) {
            sections.add(AnswerSection(
                "Planting Window — Korogwe",
                "Crop: ${plantingResult.crop}\n" +
                "Season: ${plantingResult.season}\n" +
                "Optimal months: ${plantingResult.optimalStartMonth} to ${plantingResult.optimalEndMonth}",
                false
            ))
        }

        for (sc in chunks.take(3)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        warnings.add(Warning(
            WarningType.PLANTING_ADVISORY,
            "Based on 30-year historical averages for Korogwe District. " +
            "Verify current season conditions with your local agricultural extension officer."
        ))

        return MaarifaAnswer(
            summary = "Planting guidance for $crop in Korogwe.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Prepare land and source inputs before the planting window opens.",
            warnings = warnings,
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Fertilizer schedule", "Seed varieties", "Pest management"),
            confidenceTier = if (plantingResult != null) 1 else tier,
            confidenceLabel = confidenceLabel(if (plantingResult != null) 1 else tier),
            confidenceDescription = confidenceDescription(if (plantingResult != null) 1 else tier)
        )
    }

    private suspend fun buildBreedingAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val species = classification.entities.species ?: "the animal"
        val sections = mutableListOf<AnswerSection>()
        val warnings = mutableListOf<Warning>()

        // Try gestation rule
        val gestationResult = if (species != null) {
            try { ruleEngine.calculateDueDate(species, (context?.today ?: Clock.System.todayIn(TimeZone.currentSystemDefault())).toJavaLocalDate()) } catch (e: Exception) { null }
        } else null

        if (gestationResult != null) {
            sections.add(AnswerSection(
                "Gestation Calculator",
                "Species: ${gestationResult.species}\n" +
                "Mating date: ${gestationResult.matingDate}\n" +
                "Expected due date: ${gestationResult.expectedDueDate}\n" +
                "Pre-event preparation: ${gestationResult.preEventTaskDate}",
                false
            ))
        }

        for (sc in chunks.take(3)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        return MaarifaAnswer(
            summary = "Breeding and reproduction guidance for $species.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Track oestrus cycles and body condition score for optimal mating timing.",
            warnings = warnings,
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Kidding preparation", "Colostrum protocol", "Kid management"),
            confidenceTier = if (gestationResult != null) 1 else tier,
            confidenceLabel = confidenceLabel(if (gestationResult != null) 1 else tier),
            confidenceDescription = confidenceDescription(if (gestationResult != null) 1 else tier)
        )
    }

    private fun buildCheeseAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val sections = mutableListOf<AnswerSection>()
        for (sc in chunks.take(5)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        return MaarifaAnswer(
            summary = "Cheese and dairy guidance.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Follow temperature and timing protocols carefully for consistent quality.",
            warnings = emptyList(),
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Milk quality testing", "Cheese defects", "TFDA labelling"),
            confidenceTier = tier,
            confidenceLabel = confidenceLabel(tier),
            confidenceDescription = confidenceDescription(tier)
        )
    }

    private fun buildWeatherAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val sections = mutableListOf<AnswerSection>()
        val warnings = mutableListOf<Warning>()

        for (sc in chunks.take(3)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        context?.weatherContext?.let { weather ->
            if (weather.totalRainfall7Days > 80) {
                warnings.add(Warning(
                    WarningType.DATA_QUALITY,
                    "High rainfall (${weather.totalRainfall7Days}mm in 7 days) — monitor for fungal disease risk on crops and respiratory disease in livestock."
                ))
            }
        }

        return MaarifaAnswer(
            summary = "Weather and climate guidance for Korogwe.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Check weather log and adjust management practices accordingly.",
            warnings = warnings,
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Seasonal calendar", "Disease risk calendar", "Irrigation planning"),
            confidenceTier = tier,
            confidenceLabel = confidenceLabel(tier),
            confidenceDescription = confidenceDescription(tier)
        )
    }

    private fun buildNutritionAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val sections = mutableListOf<AnswerSection>()
        for (sc in chunks.take(5)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        return MaarifaAnswer(
            summary = "Nutrition and feed guidance.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = "Check feed inventory and body condition score regularly.",
            warnings = emptyList(),
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = listOf("Ration calculator", "Feed inventory", "Body condition scoring"),
            confidenceTier = tier,
            confidenceLabel = confidenceLabel(tier),
            confidenceDescription = confidenceDescription(tier)
        )
    }

    private fun buildGeneralAnswer(
        query: String,
        chunks: List<KnowledgeRetriever.ScoredChunk>,
        context: ContextBridge.FarmContext?,
        classification: IntentClassifier.ClassificationResult,
        tier: Int
    ): MaarifaAnswer {
        val sections = mutableListOf<AnswerSection>()
        for (sc in chunks.take(5)) {
            sections.add(AnswerSection(sc.chunk.sourceTitle, sc.chunk.displayText, true))
        }

        return MaarifaAnswer(
            summary = if (chunks.isNotEmpty()) "Information found for your query." else "No specific information found.",
            sections = sections,
            farmNote = context?.let { buildFarmNote(it) },
            recommendedAction = null,
            warnings = if (chunks.isEmpty()) listOf(Warning(
                WarningType.DATA_QUALITY,
                "This topic is not covered in the current knowledge base. Consider importing a relevant document via the Knowledge Inbox."
            )) else emptyList(),
            sources = chunks.take(5).map {
                SourceAttribution(it.chunk.sourceTitle, it.chunk.sourceType, it.chunk.sourceCredibility)
            },
            relatedTopics = emptyList(),
            confidenceTier = tier,
            confidenceLabel = confidenceLabel(tier),
            confidenceDescription = confidenceDescription(tier)
        )
    }

    private fun buildFarmNote(ctx: ContextBridge.FarmContext): String {
        val notes = mutableListOf<String>()
        ctx.animalContext?.let { a ->
            notes.add("${a.tagId} (${a.species}): ${a.status}")
            a.milkYieldTrend7Day?.let { notes.add("7-day milk: ${String.format("%.1f", it)}L") }
        }
        ctx.plotContext?.let { p ->
            notes.add("${p.name}: ${p.currentCrop} — day ${p.daysSincePlanting ?: "?"} after planting")
        }
        if (ctx.alerts.isNotEmpty()) notes.addAll(ctx.alerts)
        return notes.joinToString(". ")
    }

    private fun confidenceLabel(tier: Int): String = when (tier) {
        1 -> "Calculated from verified rule"
        2 -> "Based on multiple sources"
        3 -> "Limited sources — verify"
        4 -> "Not in knowledge base"
        else -> "Unknown"
    }

    private fun confidenceDescription(tier: Int): String = when (tier) {
        1 -> "This answer is computed from a verified operational rule."
        2 -> "3+ high-scoring sources agree on this information."
        3 -> "Few sources found — verify with an extension officer or specialist."
        4 -> "Maarifa does not have reliable information on this topic."
        else -> ""
    }
}
