package com.shambasmart.maarifa.retrieval

/**
 * Maarifa Intent Classifier — rule-based intent and entity extraction.
 * Pure keyword and pattern matching. No ML inference.
 * Returns ranked intents with confidence scores.
 */
class IntentClassifier {

    data class IntentResult(
        val intent: String,
        val confidence: Float,
        val matchedKeywords: List<String>
    )

    data class EntityResult(
        val species: String? = null,
        val crop: String? = null,
        val drugName: String? = null,
        val animalId: String? = null,
        val plotName: String? = null,
        val symptoms: List<String> = emptyList(),
        val dateReference: String? = null,
        val quantityKg: Double? = null,
        val quantityLitres: Double? = null,
        val quantityAcres: Double? = null
    )

    data class ClassificationResult(
        val primaryIntent: String,
        val primaryConfidence: Float,
        val allIntents: List<IntentResult>,
        val entities: EntityResult,
        val isAmbiguous: Boolean
    )

    // keyword → (intent, weight)
    private val intentKeywords: Map<String, Pair<String, Float>> = mapOf(
        // SYMPTOM QUERIES
        "symptom" to ("symptom_query", 0.9f),
        "sick" to ("symptom_query", 0.85f),
        "disease" to ("symptom_query", 0.8f),
        "not eating" to ("symptom_query", 0.85f),
        "lame" to ("symptom_query", 0.8f),
        "coughing" to ("symptom_query", 0.85f),
        "diarrhea" to ("symptom_query", 0.85f),
        "diarrhoea" to ("symptom_query", 0.85f),
        "swelling" to ("symptom_query", 0.8f),
        "discharge" to ("symptom_query", 0.8f),
        "limping" to ("symptom_query", 0.8f),
        "fever" to ("symptom_query", 0.85f),
        "bloated" to ("symptom_query", 0.85f),
        "bloat" to ("symptom_query", 0.85f),
        "wheeze" to ("symptom_query", 0.8f),
        "sneezing" to ("symptom_query", 0.75f),
        "drooling" to ("symptom_query", 0.75f),
        "eye" to ("symptom_query", 0.6f),
        "skin" to ("symptom_query", 0.6f),
        "rash" to ("symptom_query", 0.8f),
        "mange" to ("symptom_query", 0.85f),
        "lice" to ("symptom_query", 0.8f),
        "wound" to ("symptom_query", 0.75f),
        "infection" to ("symptom_query", 0.8f),
        "mastitis" to ("symptom_query", 0.85f),
        "pneumonia" to ("symptom_query", 0.85f),
        "footrot" to ("symptom_query", 0.85f),
        "foot rot" to ("symptom_query", 0.85f),
        "pink eye" to ("symptom_query", 0.85f),
        "pinkeye" to ("symptom_query", 0.85f),
        "treat" to ("symptom_query", 0.6f),
        "treatment" to ("symptom_query", 0.7f),
        "diagnos" to ("symptom_query", 0.8f),
        "what is wrong" to ("symptom_query", 0.85f),
        "not well" to ("symptom_query", 0.8f),
        "sick animal" to ("symptom_query", 0.9f),
        "dying" to ("symptom_query", 0.9f),
        "dead" to ("symptom_query", 0.85f),

        // DOSAGE LOOKUPS
        "dose" to ("dosage_lookup", 0.9f),
        "dosage" to ("dosage_lookup", 0.9f),
        "how much" to ("dosage_lookup", 0.7f),
        "how many ml" to ("dosage_lookup", 0.9f),
        "how many mg" to ("dosage_lookup", 0.9f),
        "give" to ("dosage_lookup", 0.5f),
        "inject" to ("dosage_lookup", 0.7f),
        "injection" to ("dosage_lookup", 0.75f),
        "ml per" to ("dosage_lookup", 0.85f),
        "mg per" to ("dosage_lookup", 0.85f),
        "oxytetracycline" to ("dosage_lookup", 0.7f),
        "ivermectin" to ("dosage_lookup", 0.7f),
        "penicillin" to ("dosage_lookup", 0.7f),
        "amoxicillin" to ("dosage_lookup", 0.7f),
        "albendazole" to ("dosage_lookup", 0.7f),
        "levamisole" to ("dosage_lookup", 0.7f),
        "fenbendazole" to ("dosage_lookup", 0.7f),
        "deworm" to ("dosage_lookup", 0.65f),

        // WITHDRAWAL LOOKUPS
        "withdrawal" to ("withdrawal_lookup", 0.95f),
        "safe to milk" to ("withdrawal_lookup", 0.9f),
        "safe to sell" to ("withdrawal_lookup", 0.9f),
        "safe to eat" to ("withdrawal_lookup", 0.9f),
        "safe to slaughter" to ("withdrawal_lookup", 0.9f),
        "milk after" to ("withdrawal_lookup", 0.85f),
        "meat after" to ("withdrawal_lookup", 0.85f),
        "when can i milk" to ("withdrawal_lookup", 0.9f),
        "when can i sell" to ("withdrawal_lookup", 0.85f),
        "clearance" to ("withdrawal_lookup", 0.8f),

        // PLANTING ADVICE
        "plant" to ("planting_advice", 0.7f),
        "planting" to ("planting_advice", 0.85f),
        "when to plant" to ("planting_advice", 0.9f),
        "spacing" to ("planting_advice", 0.85f),
        "seed rate" to ("planting_advice", 0.85f),
        "fertilizer" to ("planting_advice", 0.8f),
        "fertiliser" to ("planting_advice", 0.8f),
        "top dress" to ("planting_advice", 0.85f),
        "basal" to ("planting_advice", 0.75f),
        "can" to ("planting_advice", 0.6f),
        "dap" to ("planting_advice", 0.75f),
        "urea" to ("planting_advice", 0.75f),
        "crop" to ("planting_advice", 0.6f),
        "harvest" to ("planting_advice", 0.65f),
        "sowing" to ("planting_advice", 0.85f),
        "growth stage" to ("planting_advice", 0.8f),
        "germination" to ("planting_advice", 0.8f),
        "flowering" to ("planting_advice", 0.75f),
        "tasseling" to ("planting_advice", 0.8f),
        "maturity" to ("planting_advice", 0.75f),
        "acre" to ("planting_advice", 0.6f),
        "hectare" to ("planting_advice", 0.6f),
        "plot" to ("planting_advice", 0.5f),
        "irrigation" to ("planting_advice", 0.75f),
        "pest" to ("planting_advice", 0.7f),
        "insect" to ("planting_advice", 0.65f),
        "fungicide" to ("planting_advice", 0.8f),
        "herbicide" to ("planting_advice", 0.8f),
        "pesticide" to ("planting_advice", 0.8f),
        "spray" to ("planting_advice", 0.65f),
        "napier" to ("planting_advice", 0.7f),
        "silage" to ("planting_advice", 0.7f),
        "sweet potato" to ("planting_advice", 0.65f),
        "tomato" to ("planting_advice", 0.65f),
        "onion" to ("planting_advice", 0.65f),
        "kale" to ("planting_advice", 0.65f),
        "sukuma" to ("planting_advice", 0.65f),
        "spinach" to ("planting_advice", 0.65f),
        "amaranth" to ("planting_advice", 0.65f),
        "watermelon" to ("planting_advice", 0.65f),
        "pumpkin" to ("planting_advice", 0.65f),
        "cowpea" to ("planting_advice", 0.65f),
        "sorghum" to ("planting_advice", 0.65f),
        "sunflower" to ("planting_advice", 0.65f),
        "maize" to ("planting_advice", 0.5f),
        "beans" to ("planting_advice", 0.5f),
        "cassava" to ("planting_advice", 0.5f),

        // BREEDING QUERIES
        "breed" to ("breeding_query", 0.6f),
        "breeding" to ("breeding_query", 0.8f),
        "mating" to ("breeding_query", 0.85f),
        "heat" to ("breeding_query", 0.7f),
        "oestrus" to ("breeding_query", 0.85f),
        "estrus" to ("breeding_query", 0.85f),
        "in heat" to ("breeding_query", 0.9f),
        "kidding" to ("breeding_query", 0.85f),
        "lambing" to ("breeding_query", 0.85f),
        "pregnant" to ("breeding_query", 0.8f),
        "pregnancy" to ("breeding_query", 0.85f),
        "due date" to ("breeding_query", 0.85f),
        "gestation" to ("breeding_query", 0.85f),
        "buck" to ("breeding_query", 0.6f),
        "ram" to ("breeding_query", 0.6f),
        "doe" to ("breeding_query", 0.5f),
        "ewe" to ("breeding_query", 0.5f),
        "kid" to ("breeding_query", 0.55f),
        "lamb" to ("breeding_query", 0.55f),
        "wean" to ("breeding_query", 0.7f),
        "colostrum" to ("breeding_query", 0.75f),
        "birth" to ("breeding_query", 0.75f),
        "dystocia" to ("breeding_query", 0.85f),
        "retained placenta" to ("breeding_query", 0.85f),
        "disbudding" to ("breeding_query", 0.8f),
        "castrat" to ("breeding_query", 0.8f),
        "tagging" to ("breeding_query", 0.7f),

        // NUTRITION QUERIES
        "feed" to ("nutrition_query", 0.7f),
        "ration" to ("nutrition_query", 0.85f),
        "nutrition" to ("nutrition_query", 0.85f),
        "mineral" to ("nutrition_query", 0.75f),
        "supplement" to ("nutrition_query", 0.75f),
        "protein" to ("nutrition_query", 0.7f),
        "energy" to ("nutrition_query", 0.6f),
        "hay" to ("nutrition_query", 0.7f),
        "concentrate" to ("nutrition_query", 0.75f),
        "lucerne" to ("nutrition_query", 0.7f),
        "alfalfa" to ("nutrition_query", 0.7f),
        "molasses" to ("nutrition_query", 0.65f),
        "salt lick" to ("nutrition_query", 0.8f),
        "deficiency" to ("nutrition_query", 0.8f),
        "thin" to ("nutrition_query", 0.65f),
        "weight loss" to ("nutrition_query", 0.75f),
        "body condition" to ("nutrition_query", 0.85f),
        "dry matter" to ("nutrition_query", 0.85f),
        "reorder" to ("nutrition_query", 0.7f),
        "days of feed" to ("nutrition_query", 0.85f),

        // CHEESE PROCESS
        "cheese" to ("cheese_process", 0.9f),
        "curd" to ("cheese_process", 0.85f),
        "rennet" to ("cheese_process", 0.9f),
        "culture" to ("cheese_process", 0.7f),
        "starter" to ("cheese_process", 0.75f),
        "coagulat" to ("cheese_process", 0.85f),
        "pasteuris" to ("cheese_process", 0.85f),
        "pasteuriz" to ("cheese_process", 0.85f),
        "brine" to ("cheese_process", 0.85f),
        "aging" to ("cheese_process", 0.75f),
        "ageing" to ("cheese_process", 0.75f),
        "milk quality" to ("cheese_process", 0.8f),
        "ph" to ("cheese_process", 0.6f),
        "acidity" to ("cheese_process", 0.75f),
        "feta" to ("cheese_process", 0.85f),
        "chevre" to ("cheese_process", 0.85f),
        "ricotta" to ("cheese_process", 0.85f),
        "halloumi" to ("cheese_process", 0.85f),
        "queso fresco" to ("cheese_process", 0.9f),
        "yield" to ("cheese_process", 0.7f),
        "defect" to ("cheese_process", 0.8f),
        "bitter" to ("cheese_process", 0.75f),
        "slimy" to ("cheese_process", 0.75f),
        "crumbly" to ("cheese_process", 0.75f),
        "gassy" to ("cheese_process", 0.75f),

        // WEATHER RISK
        "weather" to ("weather_risk", 0.8f),
        "rain" to ("weather_risk", 0.65f),
        "drought" to ("weather_risk", 0.85f),
        "flood" to ("weather_risk", 0.85f),
        "season" to ("weather_risk", 0.7f),
        "climate" to ("weather_risk", 0.8f),
        "temperature" to ("weather_risk", 0.75f),
        "humidity" to ("weather_risk", 0.7f),
        "grey leaf spot" to ("weather_risk", 0.9f),
        "downy mildew" to ("weather_risk", 0.85f),
        "blight" to ("weather_risk", 0.8f),
        "rust" to ("weather_risk", 0.75f),
        "long rains" to ("weather_risk", 0.8f),
        "short rains" to ("weather_risk", 0.8f),
        "dry season" to ("weather_risk", 0.8f),
        "lodging" to ("weather_risk", 0.8f)
    )

    // Species keywords
    private val speciesKeywords = mapOf(
        "goat" to "goat", "goats" to "goat", "doe" to "goat", "doe" to "goat",
        "buck" to "goat", "kid" to "goat", "kids" to "goat", "caprine" to "goat",
        "sheep" to "sheep", "ewe" to "sheep", "ram" to "sheep", "lamb" to "sheep",
        "lambs" to "sheep", "ovine" to "sheep", "flock" to "sheep",
        "pig" to "pig", "pigs" to "pig", "swine" to "pig", "sow" to "pig", "boar" to "pig"
    )

    // Crop keywords
    private val cropKeywords = mapOf(
        "maize" to "maize", "corn" to "maize",
        "beans" to "beans", "bean" to "beans",
        "cassava" to "cassava",
        "sweet potato" to "sweet potato", "sweet potatoes" to "sweet potato",
        "napier" to "napier grass", "napier grass" to "napier grass",
        "sorghum" to "sorghum",
        "cowpea" to "cowpeas", "cowpeas" to "cowpeas",
        "sunflower" to "sunflower",
        "tomato" to "tomatoes", "tomatoes" to "tomatoes",
        "kale" to "kale", "sukuma wiki" to "kale", "sukuma" to "kale",
        "onion" to "onion", "onions" to "onion",
        "capsicum" to "capsicum", "pepper" to "capsicum",
        "spinach" to "spinach",
        "amaranth" to "amaranth",
        "watermelon" to "watermelon",
        "pumpkin" to "pumpkin",
        "silage maize" to "silage maize"
    )

    // Drug keywords
    private val drugKeywords = mapOf(
        "oxytetracycline" to "oxytetracycline", "la" to "oxytetracycline_long_acting",
        "terramycin" to "oxytetracycline",
        "ivermectin" to "ivermectin", "ivomec" to "ivermectin",
        "penicillin" to "procaine_penicillin",
        "amoxicillin" to "amoxicillin",
        "albendazole" to "albendazole", "valbazen" to "albendazole",
        "levamisole" to "levamisole", "levasole" to "levamisole",
        "fenbendazole" to "fenbendazole", "panacur" to "fenbendazole",
        "oxfendazole" to "oxfendazole", "synanthic" to "oxfendazole",
        "closantel" to "closantel", "flukiver" to "closantel",
        "diminazene" to "diminazene_aceturate", "berenil" to "diminazene_aceturate",
        "imidocarb" to "imidocarb_dipropionate", "imizol" to "imidocarb_dipropionate",
        "dexamethasone" to "dexamethasone",
        "oxytocin" to "oxytocin",
        "calcium" to "calcium_borogluconate",
        "enrofloxacin" to "enrofloxacin", "baytril" to "enrofloxacin",
        "tylosin" to "tylosin",
        "meloxicam" to "meloxicam",
        "flunixin" to "flunixin_meglumine", "finadyne" to "flunixin_meglumine"
    )

    // Symptom keywords
    private val symptomKeywords = listOf(
        "coughing", "sneezing", "wheeze", "nasal discharge", "eye discharge",
        "diarrhea", "diarrhoea", "bloated", "bloat", "not eating", "off feed",
        "lame", "limping", "swollen joint", "footrot", "foot rot",
        "fever", "high temperature", "hot", "shivering",
        "mange", "lice", "rash", "skin lesions", "wound",
        "mastitis", "hard udder", "clotted milk",
        "pneumonia", "rapid breathing", "laboured breathing",
        "drooling", "mouth lesions", "orf", "scabby mouth",
        "pale gums", "anaemia", "weak", "recumbent",
        "head tilt", "circling", "convulsion", "fit",
        "pink eye", "pinkeye", "corneal opacity", "tearing",
        "swollen jaw", "lumpy jaw", "caseous lymphadenitis",
        "retained placenta", "prolapse", "dystocia",
        "ketosis", "sweet breath", "acetone",
        "hypocalcaemia", "milk fever", "down", "cannot stand",
        "tetanus", "lockjaw", "stiff", "sawhorse stance"
    )

    fun classify(query: String, species: String? = null, animalId: String? = null): ClassificationResult {
        val lower = query.lowercase().trim()

        // Score each intent
        val intentScores = mutableMapOf<String, Float>()
        val matchedKeywords = mutableMapOf<String, MutableList<String>>()

        for ((keyword, pair) in intentKeywords) {
            if (lower.contains(keyword)) {
                val (intent, weight) = pair
                intentScores[intent] = (intentScores[intent] ?: 0f) + weight
                matchedKeywords.getOrPut(intent) { mutableListOf() }.add(keyword)
            }
        }

        // Boost symptom_query if animal named
        if (animalId != null && !intentScores.containsKey("symptom_query")) {
            intentScores["symptom_query"] = 0.3f
        }

        // Extract entities
        val entities = extractEntities(lower, species)

        // Build ranked list
        val sortedIntents = intentScores.entries
            .sortedByDescending { it.value }
            .map { (intent, score) ->
                IntentResult(intent, minOf(score, 1.0f), matchedKeywords[intent] ?: emptyList())
            }

        val primaryIntent = sortedIntents.firstOrNull()?.intent ?: "general_lookup"
        val primaryConfidence = sortedIntents.firstOrNull()?.confidence ?: 0f

        // Ambiguous if top intent < 0.70
        val isAmbiguous = primaryConfidence < 0.70f && sortedIntents.size >= 2

        return ClassificationResult(
            primaryIntent = primaryIntent,
            primaryConfidence = primaryConfidence,
            allIntents = sortedIntents,
            entities = entities,
            isAmbiguous = isAmbiguous
        )
    }

    private fun extractEntities(lower: String, providedSpecies: String?): EntityResult {
        // Species
        val detectedSpecies = providedSpecies ?: speciesKeywords.entries
            .firstOrNull { lower.contains(it.key) }?.value

        // Crop
        val detectedCrop = cropKeywords.entries
            .firstOrNull { lower.contains(it.key) }?.value

        // Drug
        val detectedDrug = drugKeywords.entries
            .firstOrNull { lower.contains(it.key) }?.value

        // Symptoms
        val detectedSymptoms = symptomKeywords.filter { lower.contains(it) }

        // Quantity - weight in kg
        val kgMatch = Regex("""(\d+\.?\d*)\s*kg""").find(lower)
        val quantityKg = kgMatch?.groupValues?.get(1)?.toDoubleOrNull()

        // Quantity - volume in litres
        val litreMatch = Regex("""(\d+\.?\d*)\s*[lL](?:itre|iter)?""").find(lower)
        val quantityLitres = litreMatch?.groupValues?.get(1)?.toDoubleOrNull()

        // Quantity - area in acres
        val acreMatch = Regex("""(\d+\.?\d*)\s*acre""").find(lower)
        val quantityAcres = acreMatch?.groupValues?.get(1)?.toDoubleOrNull()

        // Date reference
        val dateRef = when {
            lower.contains("today") -> "today"
            lower.contains("yesterday") -> "yesterday"
            lower.contains("last week") -> "last_week"
            lower.contains("next week") -> "next_week"
            lower.contains("this month") -> "this_month"
            Regex("""\d{4}-\d{2}-\d{2}""").containsMatchIn(lower) ->
                Regex("""\d{4}-\d{2}-\d{2}""").find(lower)?.value
            else -> null
        }

        return EntityResult(
            species = detectedSpecies,
            crop = detectedCrop,
            drugName = detectedDrug,
            animalId = null,
            plotName = null,
            symptoms = detectedSymptoms,
            dateReference = dateRef,
            quantityKg = quantityKg,
            quantityLitres = quantityLitres,
            quantityAcres = quantityAcres
        )
    }
}