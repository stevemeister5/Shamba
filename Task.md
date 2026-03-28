# Shamba Smart — Placeholders & Unimplemented Features

**Total Identified: 155 placeholders across the codebase**

Organized by priority (highest value first):

---

## 🔴 CRITICAL PRIORITY (Data Loss / Core Functionality Broken)

### 1. Database Save Operations — All Data Entry Forms
**Impact**: Users can enter data but it's never saved to database
**Files Affected**: 8 screens
**Value**: CRITICAL — Without this, the entire app is non-functional

| File | TODO | Current Behavior |
|------|------|------------------|
| `HealthRecordsScreen.kt` | `// TODO: Save to database` | Health records disappear on dialog close |
| `ReproductionScreen.kt` | `// TODO: Save to database` | Breeding records lost |
| `MilkProductionScreen.kt` | `// TODO: Save to database` | Milk yields not recorded |
| `GrowthTrackingScreen.kt` | `// TODO: Save to database` | Weight entries lost |
| `CropPlantingScreen.kt` | `// TODO: Save to database` | Planting records lost |
| `HarvestScreen.kt` | `// TODO: Save to database` | Harvest data lost |
| `WeatherScreen.kt` | `// TODO: Save to database` | Weather logs lost |
| `CheeseInventoryScreen.kt` | `// TODO: Process sale` | Sales not recorded |

**Root Cause**: Dialog forms use local `remember { mutableStateOf() }` instead of calling ViewModel/repository to persist to Room.

### 2. Foreign Key References — All Forms Use ID=0
**Impact**: Records created with invalid foreign keys, breaking relationships
**Files Affected**: 8 screens
**Value**: CRITICAL — Even if save worked, data integrity broken

| File | TODO | Issue |
|------|------|-------|
| `HealthRecordsScreen.kt` | `// TODO: Get from selected animal` | animalId = 0 |
| `ReproductionScreen.kt` | `// TODO: Get from selected dam` | damId = 0 |
| `ReproductionScreen.kt` | `// TODO: Get from selected sire` | sireId = null |
| `MilkProductionScreen.kt` | `// TODO: Get from selected doe` | animalId = 0 |
| `GrowthTrackingScreen.kt` | `// TODO: Get from selected animal` | animalId = 0 |
| `CropPlantingScreen.kt` | `// TODO: Get from selected plot` | plotId = 0 |
| `HarvestScreen.kt` | `// TODO: Get from selected crop` | cropPlantingId = 0 |

**Root Cause**: Add dialogs don't have dropdown selectors for parent entities (animals, plots, crops).

### 3. Settings Persistence — All Settings Lost on App Restart
**Impact**: Language, role, notifications, farm profile reset every launch
**Files Affected**: `SettingsViewModel.kt`
**Value**: CRITICAL — Settings UI exists but does nothing

| Setting | TODO |
|---------|------|
| Language selection | `// TODO: Save to DataStore` |
| User role | `// TODO: Save to DataStore` |
| Farm profile | `// TODO: Save to DataStore` |
| Notifications toggle | `// TODO: Save to DataStore` |

**Root Cause**: ViewModel updates local StateFlow but never calls DataStore to persist.

---

## 🟠 HIGH PRIORITY (Major Features Non-Functional)

### 4. Livestock Expansion — Limited to Goats/Sheep Only
**Impact**: Farm manages cattle, chickens, and other livestock with no tracking
**Files Affected**: `Animal.kt`, `LivestockScreen.kt`, `LivestockViewModel.kt`
**Value**: HIGH — Core farm management gap

**Current Scope**: Only goats and sheep supported

**Required Expansion**:
| Livestock Type | Modules Needed |
|----------------|----------------|
| **Cattle (Dairy/Beef)** | Milk recording (higher volumes), BCS scoring, breed management, AI/breeding records |
| **Chickens (Layers)** | Egg production tracking, flock management, feed conversion ratios |
| **Chickens (Broilers)** | Weight gain tracking, batch management, market weight scheduling |
| **Pigs** | Farrowing records, feed efficiency, weight tracking |
| **Ducks** | Egg production, foraging management |

**Files to Modify/Create**:
- `Animal.kt` — Add livestock type enum (Goat, Sheep, Cattle, Chicken, Pig, Duck)
- `LivestockScreen.kt` — Species filter tabs for all types
- `LivestockViewModel.kt` — Queries per species
- `MilkProductionScreen.kt` — Support cattle milk volumes (litres vs ml)
- New: `EggProductionScreen.kt` — Daily egg count per flock/bird
- New: `FlockManagementScreen.kt` — Batch tracking for poultry
- New: `BroilerScreen.kt` — Weight gain curves for broilers
- `DashboardView.kt` — KPIs per livestock type

### 5. Data Export & Backup Buttons — Non-Functional
**Impact**: Users see export/backup options but nothing happens on click
**Files Affected**: `SettingsScreen.kt`
**Value**: HIGH — Data portability completely missing

| Button | TODO |
|--------|------|
| Export Data | `onClick = { /* TODO: Export data */ }` |
| Backup Data | `onClick = { /* TODO: Backup data */ }` |

### 6. Cheese Sale Recording — Button Stub
**Impact**: Can't record cheese sales from inventory screen
**Files Affected**: `CheeseScreen.kt`, `CheeseInventoryScreen.kt`
**Value**: HIGH — Revenue tracking broken

| Component | TODO |
|-----------|------|
| Sell button | `onClick = { /* TODO */ }` |
| Sale processing | `// TODO: Process sale` |

### 7. Livestock Module — Milk Tracking Stub
**Impact**: Dashboard shows "Milk Today = 0" always
**Files Affected**: `LivestockViewModel.kt`
**Value**: HIGH — Key dashboard metric non-functional

| Component | TODO |
|-----------|------|
| Today's milk yield | `// TODO: Implement milk production tracking with repository` |
| Current implementation | `val todayMilkYield: StateFlow<Double?> = flow<Double?> { emit(0.0) }` |

### 8. Acoustic Alert System — No Notification Trigger
**Impact**: Audio detection works but never alerts user
**Files Affected**: `AudioAlertViewModel.kt`
**Value**: HIGH — Safety feature non-functional

| Component | TODO |
|-----------|------|
| Alert notification | `// TODO: Show notification or alert dialog` |
| Calibration save | `// TODO: Save calibration settings to preferences` |

---

## 🟡 MEDIUM PRIORITY (Incomplete Features)

### 9. Model Manager — All Remote Operations Stubbed
**Impact**: Can't update ML models from remote
**Files Affected**: `ModelManager.kt`
**Value**: MEDIUM — Models work locally but can't be updated

| Function | Implementation |
|----------|----------------|
| `checkForUpdates()` | `// For now, return empty list` |
| `downloadModelUpdate()` | `// For now, return false` |
| `getModelMetadata()` | `// For now, initialize with default metadata` |
| `saveModelMetadata()` | `// For now, just create the directory structure` |

### 10. Model Optimizer — Quantization Simulated
**Impact**: NPU optimization UI exists but doesn't actually quantize
**Files Affected**: `ModelOptimizer.kt`
**Value**: MEDIUM — Performance optimization unavailable

| Function | Implementation |
|----------|----------------|
| `quantizeModel()` | `// This would typically call TFLite's quantization API` |
| `getNpuUtilization()` | `// For now, return a simulated value` (returns 50f) |

### 11. Maarifa Browse Tree — Empty Leaf Nodes
**Impact**: Browse navigation works but leaf topics have no content
**Files Affected**: `MaarifaViewModel.kt`
**Value**: MEDIUM — Browse feature incomplete

**Empty Leaf Nodes** (30+ entries):
- All crop topics (Maize, Beans, Cassava, etc.) → `emptyList()`
- All livestock topics (Health, Breeds, Reproduction, etc.) → `emptyList()`
- All medicine topics (Antibiotics, Dewormers, etc.) → `emptyList()`
- All cheese topics (Chevre, Feta, etc.) → `emptyList()`
- All weather topics (Calendar, Seasons, etc.) → `emptyList()`

### 12. Symptom Checker — Animal List Not Loaded
**Impact**: Can't select specific animal from herd
**Files Affected**: `SymptomCheckerScreen.kt`
**Value**: MEDIUM — Feature partially functional

| Component | TODO |
|-----------|------|
| Animal dropdown | `// TODO: Load actual animals from herd and display here` |

### 13. Offline Map Downloads — Not Implemented
**Impact**: Offline map UI exists but can't actually download tiles
**Files Affected**: `OfflineMapManager.kt`, `OfflineMapViewModel.kt`
**Value**: MEDIUM — Core offline feature missing

| Component | Implementation |
|-----------|----------------|
| Download tiles | `// In a real implementation, this would use OSMDroid's tile download capabilities` |

---

## 🟢 LOW PRIORITY (Nice-to-Have / Polish)

### 14. Placeholder Text in UI
**Impact**: Minor UX issue — example text shows in empty fields
**Files Affected**: Multiple screens

| File | Placeholder Text |
|------|------------------|
| `FarmSetupScreen.kt` | `placeholder = { Text("e.g., Shamba Smart Farm") }` |
| `FarmSetupScreen.kt` | `placeholder = { Text("e.g., Korogwe, Tanga") }` |
| `FarmSetupScreen.kt` | `placeholder = { Text("e.g., 16") }` |
| `MaarifaSidePanel.kt` | `placeholder = { Text("Ask Maarifa anything...") }` |
| `MaarifaSidePanel.kt` | `placeholder = { Text("Search knowledge base...") }` |

### 15. Vision Grading — Detection Boxes Not Rendered
**Impact**: Camera works but bounding boxes not drawn on image
**Files Affected**: `VisionGradingScreen.kt`
**Value**: LOW — Visual feedback missing

| Component | Implementation |
|-----------|----------------|
| Detection overlay | `// Detection boxes overlay would go here` |

### 16. Enhanced Camera — Stabilization Placeholder
**Impact**: Image stabilization hint exists but doesn't do anything
**Files Affected**: `EnhancedCameraManager.kt`
**Value**: LOW — Camera works, just no stabilization optimization

| Component | Implementation |
|-----------|----------------|
| Stabilization | `// This is a placeholder for future implementation` |

### 17. Navigation Callbacks — Empty Dropdown Handlers
**Impact**: Dropdowns expand/collapse but onExpandedChange does nothing
**Files Affected**: 7 screens
**Value**: LOW — UI works, just no-op callbacks

| File | Implementation |
|------|----------------|
| `CropPlantingScreen.kt` | `onExpandedChange = {}` |
| `HarvestScreen.kt` | `onExpandedChange = {}` |
| `HealthRecordsScreen.kt` | `onExpandedChange = {}` |
| `ReproductionScreen.kt` | `onExpandedChange = {}` |
| `GrowthTrackingScreen.kt` | `onExpandedChange = {}` |
| `MilkProductionScreen.kt` | `onExpandedChange = {}` |
| `SettingsScreen.kt` | `onValueChange = {}` (readOnly fields) |

### 18. Maarifa Saved Entries — Never Loaded
**Impact**: Bookmark feature exists but saved entries always empty
**Files Affected**: `MaarifaViewModel.kt`
**Value**: LOW — Feature exists but doesn't persist

| Component | Implementation |
|-----------|----------------|
| Load bookmarks | `_savedEntries.value = emptyList()` |

---

## Summary by Module

| Module | Critical | High | Medium | Low | Total |
|--------|----------|------|--------|-----|-------|
| Livestock | 4 | 2 | 1 | 2 | 9 |
| Crops | 3 | 0 | 0 | 1 | 4 |
| Cheese | 1 | 1 | 0 | 0 | 2 |
| Settings | 1 | 1 | 0 | 1 | 3 |
| ML/Vision | 0 | 0 | 3 | 2 | 5 |
| Maarifa | 0 | 0 | 2 | 1 | 3 |
| Maps | 0 | 0 | 1 | 0 | 1 |
| Acoustic | 0 | 1 | 0 | 0 | 1 |
| **Total** | **9** | **5** | **7** | **7** | **28** |

---

## Global Requirements

### Design Compliance — Organic Dark Precision
**All UI code MUST conform to `design_reference.md`**

| Requirement | Specification |
|-------------|---------------|
| **Colors** | Use design tokens only: `--surface-base`, `--green-500`, `--earth-500`, etc. No hardcoded hex |
| **Typography** | Geist font for body, Geist Mono for numerics. Size scale: hero(52px), display(32px), title(22px), heading(17px), body(15px), label(13px), caption(12px), micro(10px) |
| **Spacing** | 4px base unit: `Space.space1` (4px) through `Space.space16` (64px) |
| **Borders** | No shadows. Border-based elevation: Level 0 (sunken) → Level 4 (overlay) |
| **Radius** | Scale: `Radius.sm`(6px), `md`(10px), `lg`(14px), `xl`(20px), `full`(9999px) |
| **Motion** | 144Hz optimized: Micro 60-80ms, State 120-150ms, Page 180ms. No bounce easing |
| **Components** | KPI cards, data tables, status chips, input fields, buttons all follow design tokens |
| **Empty States** | Custom SVG illustrations per module, not generic "No data" |
| **Loading** | Skeleton shimmer (1200ms loop), never full-page spinner |

### ML Model Bundling — All Models in APK
**No remote model downloads. All models bundled in `assets/models/`**

| Model | File | Size (Est.) | Purpose |
|-------|------|-------------|---------|
| Pest Classifier | `pest_classifier.onnx` | ~12MB | YOLOv8 pest detection (8 classes) |
| Vector Embeddings | `all-miniLM-l6-v2.onnx` | ~80MB | Maarifa semantic search (384-dim) |
| Future: Livestock BCS | `bcs_scorer.onnx` | ~15MB | Body condition scoring |
| Future: Crop Disease | `crop_disease.onnx` | ~12MB | Crop disease detection |
| Future: Acoustic | `respiratory_detector.tflite` | ~5MB | Respiratory disease audio |

**Constraints**:
- Total model bundle < 150MB
- All models quantized to INT8 where possible
- Models loaded lazily on first use
- Checksum verification on load
- No network dependency for model initialization

---

## Recommended Implementation Order

### Sprint 1: Make Data Entry Work (Critical) ✅ COMPLETE
1. ✅ Wire up all 8 data entry screens to actually save to Room database
2. ✅ Add dropdown selectors for foreign key references (animals, plots, crops)
3. ✅ Verify data persists after app restart

### Sprint 2: Settings & Export (High)
4. Implement DataStore persistence for all 4 settings
5. Wire up Export Data button (CSV/PDF export via iText)
6. Wire up Backup Data button (encrypted JSON to SD card)

### Sprint 3: Livestock Expansion (High)
7. Add livestock type enum (Goat, Sheep, Cattle, Chicken, Pig, Duck)
8. Update all livestock screens with species filter tabs
9. Expand milk recording to support cattle volumes (litres)
10. Create EggProductionScreen for poultry egg tracking
11. Create FlockManagementScreen for batch poultry tracking
12. Update DashboardView with per-species KPIs

### Sprint 4: Complete Core Features (High)
13. Implement milk production tracking in LivestockViewModel
14. Wire up cheese sale recording
15. Implement acoustic alert notifications

### Sprint 5: Maarifa Knowledge Population (Major)
16. **Populate Maarifa with open-source livestock knowledge (English sources)**
    - Goats: Breeds (Boer, Toggenburg, Saanen, etc.), health, nutrition, reproduction
    - Sheep: Breeds (Dorper, Merino, etc.), health, nutrition, wool management
    - Cattle: Dairy breeds (Holstein, Jersey), beef (Angus, Brahman), health, nutrition
    - Chickens: Layer breeds (ISA Brown, Leghorn), broiler (Cobb 500, Ross 308), health
    - Pigs: Breeds (Large White, Landrace), farrowing, nutrition, health
    - Ducks: Pekin, Khaki Campbell, egg production, management
17. Populate crop knowledge (maize, beans, cassava, napier, tomatoes, etc.)
18. Populate medicine formulary (antibiotics, dewormers, vaccines)
19. Populate cheese production guides (all cheese types)
20. Populate weather/climate knowledge for Korogwe
21. Load animal list in Symptom Checker from actual herd

### Sprint 6: ML & Maarifa Polish (Medium)
22. Remove stub model operations (implement actual quantization if needed)
23. Populate Maarifa browse tree leaf nodes with ingested content
24. Implement bookmark persistence in Maarifa

### Sprint 7: Polish & Edge Cases (Low)
25. Remove placeholder text from UI
26. Implement vision detection box rendering
27. Empty dropdown handler cleanup

---

## Maarifa Knowledge Population Plan

### Data Sources (Open Source, English)
| Domain | Sources |
|--------|---------|
| **Goats** | FAO Goat Production Manual, ILRI publications, USDA Animal Health |
| **Sheep** | FAO Sheep & Goat Production, Sheep Ireland Health Guides |
| **Cattle** | FAO Dairy Production, USDA Beef Cattle Handbook, ILRI |
| **Chickens** | FAO Poultry Production, Ross/Cobb management guides, USDA |
| **Pigs** | FAO Pig Production, PIC management guides |
| **Crops** | FAO Crop Production Guides, ICRISAT publications, local extension manuals |
| **Medicines** | WHO Veterinary Formulary, Tanzania TFDA approved list |
| **Cheese** | FAO Dairy Processing, artisan cheese making guides |
| **Weather** | TMA (Tanzania Meteorological Agency) historical data, IPCC regional projections |

### Knowledge File Structure
```
assets/knowledge_base/
├── livestock/
│   ├── goats/
│   │   ├── breeds.json
│   │   ├── health.json
│   │   ├── nutrition.json
│   │   └── reproduction.json
│   ├── sheep/
│   ├── cattle/
│   ├── chickens/
│   ├── pigs/
│   └── ducks/
├── crops/
│   ├── maize.json
│   ├── beans.json
│   └── ...
├── medicines/
│   ├── antibiotics.json
│   ├── dewormers.json
│   └── vaccines.json
├── cheese/
│   ├── chevre.json
│   ├── feta.json
│   └── ...
├── weather/
│   ├── korogwe_climate.json
│   └── seasonal_calendar.json
└── pest_knowledge.json (existing)
```

### Ingestion Process
1. Source English documents (PDF, TXT, web pages)
2. Run through `KnowledgeIngestionPipeline.kt`
3. SemanticChunker splits into 400-word chunks
4. VectorSearchEngine generates 384-dim embeddings
5. Store in `KnowledgeChunk` entity with domain tags
6. Generate `OperationalRule` entities for computable knowledge
7. Verify no hallucinations via `ResponseAssembler` consistency checks
8. Bundle pre-indexed knowledge in APK (not generated at runtime)

### Farmer Extensibility
- Knowledge Inbox allows importing local documents (Swahili, local practices)
- Local knowledge supplements (not replaces) bundled English knowledge
- Conflict detection flags contradictions between sources
- Local data always tagged with source for traceability
