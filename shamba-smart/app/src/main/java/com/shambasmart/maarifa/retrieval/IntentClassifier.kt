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
        "symptom" to Pair("symptom_query", 0.9f),
        "sick" to Pair("symptom_query", 0.85f),
        "disease" to Pair("symptom_query", 0.8f),
        "not eating" to Pair("symptom_query", 0.85f),
        "lame" to Pair("symptom_query", 0.8f),
        "coughing" to Pair("symptom_query", 0.85f),
        "diarrhea" to Pair("symptom_query", 0.85f),
        "diarrhoea" to Pair("symptom_query", 0.85f),
        "swelling" to Pair("symptom_query", 0.8f),
        "discharge" to Pair("symptom_query", 0.8f),
        "limping" to Pair("symptom_query", 0.8f),
        "fever" to Pair("symptom_query", 0.85f),
        "bloated" to Pair("symptom_query", 0.85f),
        "bloat" to Pair("symptom_query", 0.85f),
        "wheeze" to Pair("symptom_query", 0.8f),
        "sneezing" to Pair("symptom_query", 0.75f),
        "drooling" to Pair("symptom_query", 0.75f),
        "eye" to Pair("symptom_query", 0.6f),
        "skin" to Pair("symptom_query", 0.6f),
        "rash" to Pair("symptom_query", 0.8f),
        "mange" to Pair("symptom_query", 0.85f),
        "lice" to Pair("symptom_query", 0.8f),
        "wound" to Pair("symptom_query", 0.75f),
        "infection" to Pair("symptom_query", 0.8f),
        "mastitis" to Pair("symptom_query", 0.85f),
        "pneumonia" to Pair("symptom_query", 0.85f),
        "footrot" to Pair("symptom_query", 0.85f),
        "foot rot" to Pair("symptom_query", 0.85f),
        "pink eye" to Pair("symptom_query", 0.85f),
        "pinkeye" to Pair("symptom_query", 0.85f),
        "treat" to Pair("symptom_query", 0.6f),
        "treatment" to Pair("symptom_query", 0.7f),
        "diagnos" to Pair("symptom_query", 0.8f),
        "what is wrong" to Pair("symptom_query", 0.85f),
        "not well" to Pair("symptom_query", 0.8f),
        "sick animal" to Pair("symptom_query", 0.9f),
        "dying" to Pair("symptom_query", 0.9f),
        "dead" to Pair("symptom_query", 0.85f),

        // DOSAGE LOOKUPS
        "dose" to Pair("dosage_lookup", 0.9f),
        "dosage" to Pair("dosage_lookup", 0.9f),
        "how much" to Pair("dosage_lookup", 0.7f),
        "how many ml" to Pair("dosage_lookup", 0.9f),
        "how many mg" to Pair("dosage_lookup", 0.9f),
        "give" to Pair("dosage_lookup", 0.5f),
        "inject" to Pair("dosage_lookup", 0.7f),
        "injection" to Pair("dosage_lookup", 0.75f),
        "ml per" to Pair("dosage_lookup", 0.85f),
        "mg per" to Pair("dosage_lookup", 0.85f),
        "oxytetracycline" to Pair("dosage_lookup", 0.7f),
        "ivermectin" to Pair("dosage_lookup", 0.7f),
        "penicillin" to Pair("dosage_lookup", 0.7f),
        "amoxicillin" to Pair("dosage_lookup", 0.7f),
        "albendazole" to Pair("dosage_lookup", 0.7f),
        "levamisole" to Pair("dosage_lookup", 0.7f),
        "fenbendazole" to Pair("dosage_lookup", 0.7f),
        "deworm" to Pair("dosage_lookup", 0.65f),

        // WITHDRAWAL LOOKUPS
        "withdrawal" to Pair("withdrawal_lookup", 0.95f),
        "safe to milk" to Pair("withdrawal_lookup", 0.9f),
        "safe to sell" to Pair("withdrawal_lookup", 0.9f),
        "safe to eat" to Pair("withdrawal_lookup", 0.9f),
        "safe to slaughter" to Pair("withdrawal_lookup", 0.9f),
        "milk after" to Pair("withdrawal_lookup", 0.85f),
        "meat after" to Pair("withdrawal_lookup", 0.85f),
        "when can i milk" to Pair("withdrawal_lookup", 0.9f),
        "when can i sell" to Pair("withdrawal_lookup", 0.85f),
        "clearance" to Pair("withdrawal_lookup", 0.8f),

        // PLANTING ADVICE
        "plant" to Pair("planting_advice", 0.7f),
        "planting" to Pair("planting_advice", 0.85f),
        "when to plant" to Pair("planting_advice", 0.9f),
        "spacing" to Pair("planting_advice", 0.85f),
        "seed rate" to Pair("planting_advice", 0.85f),
        "fertilizer" to Pair("planting_advice", 0.8f),
        "fertiliser" to Pair("planting_advice", 0.8f),
        "top dress" to Pair("planting_advice", 0.85f),
        "basal" to Pair("planting_advice", 0.75f),
        "can" to Pair("planting_advice", 0.6f),
        "dap" to Pair("planting_advice", 0.75f),
        "urea" to Pair("planting_advice", 0.75f),
        "crop" to Pair("planting_advice", 0.6f),
        "harvest" to Pair("planting_advice", 0.65f),
        "sowing" to Pair("planting_advice", 0.85f),
        "growth stage" to Pair("planting_advice", 0.8f),
        "germination" to Pair("planting_advice", 0.8f),
        "flowering" to Pair("planting_advice", 0.75f),
        "tasseling" to Pair("planting_advice", 0.8f),
        "maturity" to Pair("planting_advice", 0.75f),
        "acre" to Pair("planting_advice", 0.6f),
        "hectare" to Pair("planting_advice", 0.6f),
        "plot" to Pair("planting_advice", 0.5f),
        "irrigation" to Pair("planting_advice", 0.75f),
        "pest" to Pair("planting_advice", 0.7f),
        "insect" to Pair("planting_advice", 0.65f),
        "fungicide" to Pair("planting_advice", 0.8f),
        "herbicide" to Pair("planting_advice", 0.8f),
        "pesticide" to Pair("planting_advice", 0.8f),
        "spray" to Pair("planting_advice", 0.65f),
        "napier" to Pair("planting_advice", 0.7f),
        "silage" to Pair("planting_advice", 0.7f),
        "sweet potato" to Pair("planting_advice", 0.65f),
        "tomato" to Pair("planting_advice", 0.65f),
        "onion" to Pair("planting_advice", 0.65f),
        "kale" to Pair("planting_advice", 0.65f),
        "sukuma" to Pair("planting_advice", 0.65f),
        "spinach" to Pair("planting_advice", 0.65f),
        "amaranth" to Pair("planting_advice", 0.65f),
        "watermelon" to Pair("planting_advice", 0.65f),
        "pumpkin" to Pair("planting_advice", 0.65f),
        "cowpea" to Pair("planting_advice", 0.65f),
        "sorghum" to Pair("planting_advice", 0.65f),
        "sunflower" to Pair("planting_advice", 0.65f),
        "maize" to Pair("planting_advice", 0.5f),
        "beans" to Pair("planting_advice", 0.5f),
        "cassava" to Pair("planting_advice", 0.5f),

        // BREEDING QUERIES
        "breed" to Pair("breeding_query", 0.6f),
        "breeding" to Pair("breeding_query", 0.8f),
        "mating" to Pair("breeding_query", 0.85f),
        "heat" to Pair("breeding_query", 0.7f),
        "oestrus" to Pair("breeding_query", 0.85f),
        "estrus" to Pair("breeding_query", 0.85f),
        "in heat" to Pair("breeding_query", 0.9f),
        "kidding" to Pair("breeding_query", 0.85f),
        "lambing" to Pair("breeding_query", 0.85f),
        "pregnant" to Pair("breeding_query", 0.8f),
        "pregnancy" to Pair("breeding_query", 0.85f),
        "due date" to Pair("breeding_query", 0.85f),
        "gestation" to Pair("breeding_query", 0.85f),
        "buck" to Pair("breeding_query", 0.6f),
        "ram" to Pair("breeding_query", 0.6f),
        "doe" to Pair("breeding_query", 0.5f),
        "ewe" to Pair("breeding_query", 0.5f),
        "kid" to Pair("breeding_query", 0.55f),
        "lamb" to Pair("breeding_query", 0.55f),
        "wean" to Pair("breeding_query", 0.7f),
        "colostrum" to Pair("breeding_query", 0.75f),
        "birth" to Pair("breeding_query", 0.75f),
        "dystocia" to Pair("breeding_query", 0.85f),
        "retained placenta" to Pair("breeding_query", 0.85f),
        "disbudding" to Pair("breeding_query", 0.8f),
        "castrat" to Pair("breeding_query", 0.8f),
        "tagging" to Pair("breeding_query", 0.7f),

        // NUTRITION QUERIES
        "feed" to Pair("nutrition_query", 0.7f),
        "ration" to Pair("nutrition_query", 0.85f),
        "nutrition" to Pair("nutrition_query", 0.85f),
        "mineral" to Pair("nutrition_query", 0.75f),
        "supplement" to Pair("nutrition_query", 0.75f),
        "protein" to Pair("nutrition_query", 0.7f),
        "energy" to Pair("nutrition_query", 0.6f),
        "hay" to Pair("nutrition_query", 0.7f),
        "concentrate" to Pair("nutrition_query", 0.75f),
        "lucerne" to Pair("nutrition_query", 0.7f),
        "alfalfa" to Pair("nutrition_query", 0.7f),
        "molasses" to Pair("nutrition_query", 0.65f),
        "salt lick" to Pair("nutrition_query", 0.8f),
        "deficiency" to Pair("nutrition_query", 0.8f),
        "thin" to Pair("nutrition_query", 0.65f),
        "weight loss" to Pair("nutrition_query", 0.75f),
        "body condition" to Pair("nutrition_query", 0.85f),
        "dry matter" to Pair("nutrition_query", 0.85f),
        "reorder" to Pair("nutrition_query", 0.7f),
        "days of feed" to Pair("nutrition_query", 0.85f),

        // CHEESE PROCESS
        "cheese" to Pair("cheese_process", 0.9f),
        "curd" to Pair("cheese_process", 0.85f),
        "rennet" to Pair("cheese_process", 0.9f),
        "culture" to Pair("cheese_process", 0.7f),
        "starter" to Pair("cheese_process", 0.75f),
        "coagulat" to Pair("cheese_process", 0.85f),
        "pasteuris" to Pair("cheese_process", 0.85f),
        "pasteuriz" to Pair("cheese_process", 0.85f),
        "brine" to Pair("cheese_process", 0.85f),
        "aging" to Pair("cheese_process", 0.75f),
        "ageing" to Pair("cheese_process", 0.75f),
        "milk quality" to Pair("cheese_process", 0.8f),
        "ph" to Pair("cheese_process", 0.6f),
        "acidity" to Pair("cheese_process", 0.75f),
        "feta" to Pair("cheese_process", 0.85f),
        "chevre" to Pair("cheese_process", 0.85f),
        "ricotta" to Pair("cheese_process", 0.85f),
        "halloumi" to Pair("cheese_process", 0.85f),
        "queso fresco" to Pair("cheese_process", 0.9f),
        "yield" to Pair("cheese_process", 0.7f),
        "defect" to Pair("cheese_process", 0.8f),
        "bitter" to Pair("cheese_process", 0.75f),
        "slimy" to Pair("cheese_process", 0.75f),
        "crumbly" to Pair("cheese_process", 0.75f),
        "gassy" to Pair("cheese_process", 0.75f),

        // WEATHER RISK
        "weather" to Pair("weather_risk", 0.8f),
        "rain" to Pair("weather_risk", 0.65f),
        "drought" to Pair("weather_risk", 0.85f),
        "flood" to Pair("weather_risk", 0.85f),
        "season" to Pair("weather_risk", 0.7f),
        "climate" to Pair("weather_risk", 0.8f),
        "temperature" to Pair("weather_risk", 0.75f),
        "humidity" to Pair("weather_risk", 0.7f),
        "grey leaf spot" to Pair("weather_risk", 0.9f),
        "downy mildew" to Pair("weather_risk", 0.85f),
        "blight" to Pair("weather_risk", 0.8f),
        "rust" to Pair("weather_risk", 0.75f),
        "long rains" to Pair("weather_risk", 0.8f),
        "short rains" to Pair("weather_risk", 0.8f),
        "dry season" to Pair("weather_risk", 0.8f),
        "lodging" to Pair("weather_risk", 0.8f)
    )

    // Species keywords
    private val speciesKeywords = mapOf(
        "goat" to "goat", "goats" to "goat", "doe" to "goat",
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
