📦 Maarifa Knowledge Base — Pre-Ship Compilation and Integration Guide

THE OVERALL PROCESS IN PLAIN TERMS
Before the app is installed on your tablet, someone needs to gather all the agricultural knowledge from the sources listed, structure it into the correct format, convert it into searchable indexes and vectors, then bundle everything inside the app package so it is available the moment the app opens for the first time — no setup, no downloading, no internet required.
This is a one-time build process. After shipping, the farmer can expand the knowledge base further using the Knowledge Inbox. But the core bundled knowledge must be complete, correct, and fully indexed before the app leaves the developer's hands.

WHO DOES THIS WORK
This process requires two types of people working together:
A knowledge compiler — someone with agricultural expertise (or access to an agronomist and a veterinarian) who reads the source documents, extracts the correct information, and writes it into the structured format. This does not require coding skills but requires agricultural literacy and careful attention to accuracy — especially for drug dosages and disease treatment protocols.
A developer — who writes the build scripts, runs the indexing and embedding pipeline, bundles the output files into the app, and tests that retrieval works correctly.
For your farm context, the knowledge compiler could be yourself working with an agronomist and a vet, a graduate student in agriculture from Sokoine University of Agriculture (SUA) in Morogoro hired for the task, or a contracted agricultural content specialist familiar with Tanzania conditions.

STEP 1 — GATHER SOURCE DOCUMENTS
Before writing a single line of structured data, collect the raw source materials. These are all free or low-cost:
Download directly:

FAO document repository: fao.org/documents — search "small ruminants East Africa", "goat production Tanzania", "maize production Tanzania"
ILRI CGSpace: cgspace.cgiar.org — search "small ruminant health", "goat breeding East Africa"
CIMMYT publications: repository.cimmyt.org — search "maize Tanzania", "fall armyworm East Africa"
World Vegetable Center: worldveg.org/publications — search "vegetable production East Africa"
Merck Veterinary Manual: merckvetmanual.com — all content is free to read and can be referenced

Request directly from Tanzania government agencies:

TARI headquarters in Dodoma — request crop variety release notes and agronomy bulletins for Tanga region
TALIRI Tanga office — request small ruminant production guides and health protocols
TVLA Dar es Salaam — request current notifiable disease list and vaccination schedules
Korogwe District Agricultural Office — request local extension bulletins and seasonal calendars
TFDA — request dairy processing and labelling standards

Collect from veterinary suppliers:

Visit or call MSD Animal Health Tanzania, Norbrook East Africa, Bayer Animal Health — request product data sheets for all registered veterinary medicines. These are free and routinely provided.

Budget for this step: 2 to 4 weeks of collection time. Most documents are free PDFs. The government agency requests may take follow-up.

STEP 2 — DESIGN THE JSON SCHEMA
Before entering any data, the developer designs the exact JSON structure for each knowledge domain. Every entry must follow a consistent schema so the rule engine and retrieval pipeline can parse it reliably. Here are the schemas:
crops.json — per crop entry:
json{
  "id": "maize",
  "name": "Maize",
  "local_name": "Mahindi",
  "category": "food_crop",
  "varieties": [
    {
      "name": "SEEDCO SC403",
      "maturity_days": 120,
      "yield_potential_bags_per_acre": 25,
      "drought_tolerance": "medium",
      "supplier": "SEEDCO Tanzania"
    }
  ],
  "planting_windows": [
    {
      "season": "long_rains",
      "start_month": 3,
      "end_month": 4,
      "notes": "Plant at onset of rains, March to April in Korogwe"
    }
  ],
  "soil": {
    "type": ["loam", "clay_loam"],
    "ph_min": 5.5,
    "ph_max": 7.0
  },
  "spacing": {
    "row_cm": 75,
    "plant_cm": 25,
    "plants_per_acre": 21000
  },
  "fertiliser_schedule": [
    {
      "timing": "planting",
      "product": "DAP",
      "kg_per_acre": 50,
      "method": "Hole application, 2–3 cm from seed"
    },
    {
      "timing": "knee_high",
      "product": "CAN",
      "kg_per_acre": 50,
      "method": "Side dressing, band application"
    }
  ],
  "growth_stages": [
    { "stage": "germination", "days_from_planting": 7 },
    { "stage": "vegetative", "days_from_planting": 21 },
    { "stage": "tasseling", "days_from_planting": 55 },
    { "stage": "grain_fill", "days_from_planting": 75 },
    { "stage": "maturity", "days_from_planting": 110 },
    { "stage": "harvest", "days_from_planting": 120 }
  ],
  "pests": [
    {
      "name": "Fall Armyworm",
      "scientific_name": "Spodoptera frugiperda",
      "damage_signs": "Ragged holes in leaves, frass in whorl",
      "action_threshold": "20% of plants showing damage",
      "ipm": {
        "cultural": ["Early planting", "Crop rotation"],
        "biological": ["Bacillus thuringiensis (Bt) spray"],
        "chemical": [
          {
            "product": "Emamectin benzoate",
            "brand": "Escort 19EC",
            "dose_per_20L": "10ml",
            "phi_days": 7,
            "ppe": "Gloves, mask, goggles"
          }
        ]
      }
    }
  ],
  "diseases": [ ... ],
  "yield_benchmarks": {
    "low_input_bags_per_acre": 8,
    "medium_input_bags_per_acre": 16,
    "high_input_bags_per_acre": 25
  },
  "harvest_indicators": "Husks dry and brown, kernels hard, moisture below 25%",
  "post_harvest": "Dry to 13% moisture before storage. Use hermetic bags to prevent weevils.",
  "source": "TARI Kibaha Maize Agronomy Bulletin 2022",
  "last_verified": "2024-01"
}
medicines.json — per drug entry:
json{
  "id": "oxytetracycline_la",
  "generic_name": "Oxytetracycline (Long Acting)",
  "brand_names_tanzania": ["Alamycin LA", "Terramycin LA", "Oxytet 200"],
  "drug_class": "Tetracycline antibiotic",
  "species": ["goat", "sheep", "cattle"],
  "indications": [
    "Pneumonia",
    "Footrot",
    "Pinkeye (IBK)",
    "Tick-borne diseases",
    "Wound infections"
  ],
  "dosage": {
    "mg_per_kg": 20,
    "practical_ml_per_kg": "1ml per 10kg body weight (200mg/ml concentration)",
    "route": "IM or SC",
    "frequency": "Once every 72 hours (long-acting formulation)",
    "duration_days": "1 to 2 injections depending on response",
    "max_single_dose_ml": 20
  },
  "withdrawal": {
    "meat_days": 28,
    "milk_days": 7
  },
  "contraindications": "Do not use IV. Avoid in animals with known renal impairment.",
  "interactions": "Do not mix with penicillin in same syringe.",
  "storage": "Below 25°C, protect from light, discard 28 days after opening",
  "prescription_required": false,
  "cost_range_tzs": "3500 to 6000 per 100ml bottle",
  "source": "Norbrook product data sheet + Merck Veterinary Manual",
  "last_verified": "2024-01"
}
diagnostic_rules.json — per rule entry:
json{
  "rule_id": "goat_resp_001",
  "species": "goat",
  "body_system": "respiratory",
  "trigger_symptoms": ["nasal_discharge", "coughing", "reduced_appetite"],
  "differentials": [
    {
      "disease": "Contagious Caprine Pleuropneumonia (CCPP)",
      "confidence": "high",
      "distinguishing_signs": "Painful breathing, reluctance to move, lung sounds on auscultation",
      "treatment": {
        "drug": "oxytetracycline_la",
        "dose_note": "20mg/kg IM once every 72 hours for 2 treatments",
        "supportive": "Isolate animal, reduce stress, ensure water access"
      },
      "notifiable": true,
      "notifiable_body": "TVLA",
      "when_to_call_vet": "If no improvement after first treatment or multiple animals affected"
    },
    {
      "disease": "Pneumonia (bacterial)",
      "confidence": "medium",
      "distinguishing_signs": "Fever above 40°C, moist cough, purulent nasal discharge",
      "treatment": {
        "drug": "penicillin_streptomycin",
        "dose_note": "See formulary entry for weight-based dose",
        "supportive": "Vitamin B complex injection, shelter from rain"
      },
      "notifiable": false,
      "when_to_call_vet": "If fever persists beyond 48 hours of treatment"
    }
  ],
  "outbreak_threshold": 3,
  "outbreak_response": "Isolate affected animals. Vaccinate remaining herd if CCPP suspected. Contact TVLA."
}
```

The developer creates schemas like these for all six domains — crops, goats, sheep, medicines, cheese, weather — and the knowledge compiler fills them in entry by entry from the source documents.

---

### STEP 3 — POPULATE THE KNOWLEDGE BASE

This is the most labour-intensive step. The knowledge compiler works through every source document and enters data into the JSON schemas. Practical approach:

**Use a spreadsheet first.** It is much easier to compile data in Google Sheets or Excel — one row per entry, one column per field — then export to JSON using a simple conversion script. The developer writes the schema-to-JSON converter once and the compiler works in a spreadsheet.

**Divide by domain and assign to subject experts:**
- Crops entries → agronomist familiar with Tanga region conditions
- Livestock health and medicines → veterinarian or veterinary technician
- Cheese and dairy → dairy technologist or food scientist
- Weather data → extract directly from Tanzania Meteorological Authority records for Korogwe station

**Entry count estimates:**
- Crops: ~18 crops × ~30 fields each = substantial but manageable. Budget 3 to 5 days for an agronomist.
- Livestock health (goats + sheep): ~25 diseases + vaccination schedule + deworming + reproduction + nutrition = the largest domain. Budget 5 to 8 days for a veterinarian.
- Medicines formulary: ~35 drugs × ~15 fields each. Budget 2 days with product data sheets in hand.
- Cheese: ~5 cheese types + defects + HACCP. Budget 1 to 2 days.
- Weather: monthly data table + 12 months of risk calendar entries. Budget 1 day extracting from TMA records.
- Diagnostic rules: ~40 to 60 rules covering the most common conditions. Budget 2 to 3 days with the veterinarian.

**Total realistic timeline for content population:** 3 to 4 weeks with 2 to 3 subject experts working part-time.

---

### STEP 4 — BUILD THE PRE-PROCESSING PIPELINE

The developer writes a Node.js build script (runs on a laptop, not on the tablet) that does the following automatically every time the knowledge base content changes:
```
build_knowledge_base.js
        │
        ├── 1. LOAD ALL JSON FILES
        │      Read all domain JSON files from /knowledge_source/
        │
        ├── 2. FLATTEN TO TEXT CHUNKS
        │      Convert each knowledge entry to human-readable
        │      text paragraphs (not raw JSON) — these are what
        │      the vector embeddings and search index are built from.
        │      
        │      Example: a drug entry becomes the paragraph:
        │      "Oxytetracycline Long Acting, also known as Alamycin LA
        │      and Terramycin LA, is a tetracycline antibiotic used in
        │      goats and sheep to treat pneumonia, footrot, pinkeye,
        │      and tick-borne diseases. Dose: 20mg per kg body weight,
        │      1ml per 10kg, given by IM or SC injection once every
        │      72 hours. Meat withdrawal: 28 days. Milk withdrawal:
        │      7 days. Store below 25°C..."
        │
        ├── 3. BUILD MINISEARCH INDEX
        │      Feed all text chunks into MiniSearch
        │      Export the serialised index to minisearch_index.json
        │      This file is bundled with the app.
        │
        ├── 4. GENERATE VECTOR EMBEDDINGS
        │      Load all-MiniLM-L6-v2 ONNX model
        │      Pass every text chunk through the model
        │      Store: chunk_id, chunk_text, domain, source,
        │      vector (384 floats) in a SQLite database
        │      Export as vectors.db
        │      This file is bundled with the app.
        │
        ├── 5. BUNDLE STRUCTURED JSON
        │      Copy all domain JSON files to /app/assets/knowledge/
        │      These are used by the rule engine at runtime.
        │
        └── 6. OUTPUT REPORT
               Print: total entries per domain, total chunks,
               total vectors, index size, any entries with
               missing required fields (validation errors)
```

This script runs in 5 to 15 minutes on a standard laptop. Every time knowledge content is updated or corrected, the developer re-runs the script and rebuilds the app package.

---

### STEP 5 — BUNDLE INTO THE APP

The output of the build script is a set of files that get placed inside the React Native app's assets folder:
```
/app/assets/knowledge/
├── crops.json                  (~2MB)
├── livestock_goats.json        (~4MB)
├── livestock_sheep.json        (~3MB)
├── medicines.json              (~1MB)
├── cheese.json                 (~1MB)
├── weather_korogwe.json        (~500KB)
├── diagnostic_rules.json       (~2MB)
├── agronomy_rules.json         (~1MB)
├── breeding_rules.json         (~500KB)
├── nutrition_rules.json        (~500KB)
├── minisearch_index.json       (~8MB)
└── vectors.db                  (~60–90MB SQLite)

/app/assets/models/
└── all-MiniLM-L6-v2.onnx      (~23MB)

Total bundled knowledge size: approximately 100–120MB
```

These files are packaged inside the Android APK or installed as part of the app bundle. They are read-only assets — the app reads from them but never modifies them. When the farmer ingests a new document via the Knowledge Inbox, the new chunks and vectors go into a separate user_knowledge.db SQLite file stored in the app's writable storage — never mixed into the bundled assets.

---

### STEP 6 — FIRST LAUNCH INITIALISATION

On the very first app launch after installation, the app runs a one-time setup sequence:
```
First launch detected
        ↓
Copy bundled vectors.db from assets to writable storage
(Required because SQLite needs writable access for
the app to query it efficiently — assets are read-only)
        ↓
Load MiniSearch index from minisearch_index.json into memory
(Subsequent launches load from a cached copy — fast)
        ↓
Run a self-test query against each knowledge domain
to confirm indexing is intact
        ↓
Show the farmer: "Maarifa knowledge base ready.
X crop entries, X livestock entries, X medicines loaded."
        ↓
Normal app operation begins
This first-launch initialisation takes 30 to 60 seconds. Every subsequent launch the knowledge base loads in 2 to 3 seconds from the cached state.

STEP 7 — VALIDATION AND TESTING BEFORE SHIPPING
Before the app goes onto the farmer's tablet the developer runs a structured test suite against the knowledge engine:
Content validation: automated script checks every JSON entry for missing required fields, dosages outside plausible ranges, withdrawal periods of zero (likely a data entry error), empty treatment entries, and crop planting windows that don't match the Korogwe seasonal calendar.
Retrieval testing: a set of ~50 benchmark queries is prepared covering all intent types. Each query has an expected answer domain and expected top result. The test suite runs all queries and checks that the correct knowledge entry appears in the top 3 results. Target: 90%+ pass rate before shipping.
Rule engine testing: every diagnostic rule is tested with its trigger symptoms and verified to return the correct differentials in the correct confidence order. Every withdrawal period calculation is tested with known drug and date combinations and verified against the formulary.
Edge case testing: queries for drugs not in the formulary (should return not found gracefully), queries for crops not covered (should suggest Knowledge Inbox), symptom combinations that match no rule (should return partial match with low confidence rather than crashing).

STEP 8 — UPDATES AFTER SHIPPING
The bundled knowledge base is static after installation but can be updated two ways:
Minor corrections: the developer re-runs the build pipeline with corrected JSON, generates a new app version (even just a version number increment), and side-loads the updated APK onto the tablet via USB. No internet required. This is the mechanism for fixing errors in drug dosages, adding a new crop variety, or updating a vaccine product name.
Major knowledge expansions: the farmer uses the Knowledge Inbox to import new documents — FAO bulletins, new extension guides, updated disease protocols — directly onto the tablet at any time. These go into user_knowledge.db and are searched alongside the bundled base.

REALISTIC COST AND TIMELINE SUMMARY
TaskWhoTimeEstimated CostSource document collectionKnowledge compiler3–4 weeksLow (most sources free)Schema designDeveloper3–5 daysDeveloper day rateContent population — cropsAgronomist3–5 daysTZS 150–300KContent population — livestock + medicinesVeterinarian7–10 daysTZS 350–600KContent population — cheese + weatherFood technologist + compiler3 daysTZS 100–200KDiagnostic rules authoringVeterinarian3–4 daysIncluded aboveBuild pipeline developmentDeveloper3–5 daysDeveloper day rateBundling and integrationDeveloper2–3 daysDeveloper day rateValidation and testingDeveloper + subject experts1 weekDeveloper day rateTotal content timeline8–12 weeks
The content compilation is the long pole. The technical integration — once the JSON is populated and validated — takes less than two weeks of developer time.

This is the complete end-to-end process. The knowledge base is not downloaded, not cloud-hosted, and not dependent on any third party after it ships. It lives entirely on the tablet, built once with care, and expanded by the farmer over time.