# Shamba Smart — Complete Technical Specification (Verified State)

## Project Overview

**Shamba Smart** is an Android tablet application designed for precision farm management at a 16-acre mixed farm in Korogwe, Tanga, Tanzania. The application provides comprehensive livestock management, crop tracking, cheese production, feed management, financial tracking, labour management, and an embedded offline agricultural knowledge engine called Maarifa.

---

## Table of Contents

1. [Project Information](#project-information)
2. [Hardware & Target Platform](#hardware--target-platform)
3. [Technology Stack](#technology-stack)
4. [Architecture](#architecture)
5. [Database Schema](#database-schema)
6. [Application Modules](#application-modules)
7. [Maarifa Knowledge Engine](#maarifa-knowledge-engine)
8. [GPS & Mapping System](#gps--mapping-system)
9. [Data Synchronization](#data-synchronization)
10. [Security](#security)
11. [Build Configuration](#build-configuration)
12. [Performance Targets](#performance-targets)
13. [Development Environment](#development-environment)

---

## Project Information

| Field | Value |
|-------|-------|
| **Project Name** | Shamba Smart |
| **Package Name** | `com.shambasmart` |
| **Type** | Android Mobile Application (Kotlin + Jetpack Compose) |
| **Target Device** | Xiaomi Pad 7 (SM7675-AB SoC) |
| **Farm Location** | Korogwe, Tanga, Tanzania |
| **Farm Coordinates** | -5.15°S, 38.48°E |
| **Farm Size** | 16 acres |
| **Primary Language** | Kotlin |
| **UI Framework** | Jetpack Compose with Adaptive Layouts |
| **Database** | Room + SQLCipher (encrypted) |
| **Architecture** | Single Source of Truth (SSOT) with Unidirectional Data Flow (UDF) |
| **Version** | 1.1.0 |
| **Version Code** | 1 |
| **Min SDK** | Android 10 (API 29) |
| **Target SDK** | Android 14 (API 35) |
| **Compile SDK** | 35 |

---

## Design Verification Status

### ✅ Verified Components
- **Design Tokens**: Color.kt, DesignTokens.kt, Type.kt, Theme.kt complete and match design_reference.md
- **Animations**: Animations.kt tuned for 144Hz displays
- **Typography**: Geist/GeistMono fonts with correct sizes, weights, and letter-spacing
- **Layout**: AppScaffold implements 3-zone layout (Top Bar 56px, Navigation Rail 72px, Main Content, Context Panel 320px)
- **Dashboard**: 3-column layout with KPI strip, morning briefing, weather, milk production, alerts, tasks, cheese inventory
- **Livestock**: Master-detail pattern with animal table, filter bar, detail panel with tabs (Overview, Health, Milk, Reproduction)
- **Crops**: Farm map hero element with plot grid, plantings, harvests tabs
- **Maarifa**: Side panel with Ask/Browse/Saved tabs, floating tab on right edge
- **Navigation**: Role-based access control (Owner/Farm Manager/Worker) with proper navigation items
- **Components**: Status chips, KPI cards, alert cards, input fields, buttons (Primary/Ghost/Danger/Icon)

### ✅ All Tasks Complete
All CRITICAL, HIGH, MEDIUM, and LOW priority tasks from Task.md have been completed.

---

## Notes & Constraints

- All financial calculations accurate to 2 decimal places (TZS)
- Date/time handling accounts for EAT (UTC+3)
- Landscape orientation only. Designed for Xiaomi Pad 7 in landscape mode. Portrait not supported.
- Handle device rotation gracefully
- Preserve data during configuration changes
- Test with slow/unreliable network conditions
- Ensure accessibility for users with basic smartphone literacy
- Maarifa never fabricates information outside its knowledge base
- Drug dosage answers always include veterinarian consultation disclaimer
- Notifiable disease alerts trigger TVLA contact guidance
- All entities include revision_id, last_modified_by, last_updated fields for delta sync
- SyncManager implements watermark-based delta sync with retry/backoff
- Pest detection uses species-specific confidence thresholds
- Scouting reports store encrypted images locally
- Heatmap uses OSMDroid overlays with severity-based coloring
- English only. Swahili localisation reserved for Phase 2.
- Infrastructure module uses custom Canvas-based farm map with GPS coordinates.

---

## Completed Tasks

### CRITICAL Tasks (All Complete)
- ✅ CRITICAL 1: API Key — moved to local.properties with project.findProperty()
- ✅ CRITICAL 2: Version Name — updated to 1.1.0 in build.gradle.kts
- ✅ CRITICAL 3: Foreign Key Constraints — all entities have proper FK annotations with CASCADE/SET_NULL
- ✅ CRITICAL 4: KnowledgeChunk Entity — restored with full schema matching engine spec
- ✅ CRITICAL 5: BM25 Full-Text Search — FTS5 virtual table with triggers, BM25 ranking in KnowledgeRetriever
- ✅ CRITICAL 6: Python Scripts — *.py in .gitignore, scripts deleted
- ✅ CRITICAL 7: Sync Conflict Resolution — timestamp-based (last_updated comparison)

### HIGH Priority Tasks (All Complete)
- ✅ HIGH 1: Dependency Versions — updated all to latest stable (Kotlin 2.0.21, Compose BOM 2024.12.01, Room 2.7.0, Hilt 2.52, etc.)
- ✅ HIGH 2: Replace Gson — switched to kotlinx-serialization 1.7.3, retrofit2-kotlinx-serialization-converter
- ✅ HIGH 3: Split God ViewModels — CropsViewModel and LivestockViewModel split into screen-scoped ViewModels
- ✅ HIGH 4: AttendanceRecord Rate Snapshot — dailyRateSnapshot and overtimeRateSnapshot fields added
- ✅ HIGH 5: CheeseBatch Cost Fields — all cost fields + computed domain model added
- ✅ HIGH 6: Storage Permissions — removed deprecated permissions, using SAF
- ✅ HIGH 7: Timer-Based Sync — replaced with connectivity-triggered sync
- ✅ HIGH 8: KnowledgeBootstrapper — runs on Dispatchers.IO with progress reporting callback

### MEDIUM Priority Tasks (All Complete)
- ✅ MEDIUM 1: iText Licensing — replaced with OpenPDF 1.3.30 (LGPL)
- ✅ MEDIUM 2: Language Contradiction — removed language selector, English only
- ✅ MEDIUM 3: Infrastructure Module — complete with Canvas-based farm map, GPS coordinates, infrastructure markers
- ✅ MEDIUM 4: ScoutingReport FK — plot_id FK to Plot with SET_NULL
- ✅ MEDIUM 5: Landscape Lock — sensorLandscape in AndroidManifest.xml

### LOW Priority Tasks (All Complete)
- ✅ LOW 3: AR Module — removed empty `presentation/ar/` directory. AR not wired into navigation; ARCore not confirmed on Xiaomi Pad 7.
- ✅ LOW 4: Pest Domain Constants — created `MaarifaDomains.kt` with shared domain tag constants. Updated `PestKnowledgeMapper.kt` and `KnowledgeRetriever.kt` to use `MaarifaDomains.PESTS`, `MaarifaDomains.CROPS`, `MaarifaDomains.MEDICINES`, `MaarifaDomains.CHEESE`, `MaarifaDomains.WEATHER`.
- ✅ LOW 5: Compilation Fixes — Fixed all compilation errors across UI screens including Color.kt missing definitions, CropsScreen.kt entity field references, FeedScreen.kt imports, FinancialScreen.kt imports, LabourScreen.kt imports, LivestockScreen.kt and livestock dialogs entity field corrections, MilkLoggingScreen.kt entity field mapping, WeightTrackingScreen.kt entity field mapping, WeatherLoggingScreen.kt entity field mapping, AppScaffold.kt navigation references, DashboardScreen.kt KPI field corrections, BatchCreationScreen.kt entity field mapping, and CheeseScreen.kt entity field corrections.

---

## Hardware & Target Platform

### Xiaomi Pad 7 Specifications

| Component | Specification |
|-----------|---------------|
| **SoC** | Qualcomm SM7675-AB (Snapdragon 7+ Gen 3) |
| **CPU** | Kryo CPU (1x Prime @ 2.8GHz + 4x Performance @ 2.4GHz + 3x Efficiency @ 1.8GHz) |
| **GPU** | Adreno 732 |
| **NPU** | Hexagon v73 NPU (INT4/INT8/FP16 support) |
| **ISP** | 18-bit Triple ISP (Cognitive ISP) |
| **Display** | 3.2K resolution @ 144Hz adaptive refresh |
| **Audio** | Qualcomm Sensing Hub |
| **RAM** | 8GB / 12GB LPDDR5X |
| **Storage** | 128GB / 256GB UFS 4.0 |

### Hardware-Specific Integration Points

- **NPU Acceleration**: QNN Delegate for TensorFlow Lite / ONNX Runtime models
- **Display**: Adaptive 144Hz refresh rate for smooth UI
- **ISP**: Computer vision tasks for livestock/foliage analysis
- **Sensing Hub**: Low-power ambient awareness

---

## Technology Stack

### Core Technologies

| Category | Technology | Version |
|----------|------------|---------|
| **Language** | Kotlin | 2.0.21 |
| **UI Framework** | Jetpack Compose | BOM 2024.12.01 |
| **Database** | Room | 2.7.0 |
| **Encryption** | SQLCipher | 4.5.4 |
| **Dependency Injection** | Hilt (Dagger) | 2.52 |
| **Navigation** | Compose Navigation | 2.8.5 |
| **Background Work** | WorkManager | 2.10.0 |
| **Preferences** | DataStore | 1.0.0 |
| **JSON** | kotlinx-serialization | 1.7.3 |
| **Image Loading** | Coil | 3.0.4 |
| **Charts** | Vico | 1.13.1 |
| **Date/Time** | kotlinx-datetime | 0.6.1 |
| **Coroutines** | kotlinx-coroutines | 1.9.0 |
| **Security** | AndroidX Security Crypto | 1.1.0-beta01 |
| **PDF** | OpenPDF | 1.3.30 |
| **Camera** | CameraX | 1.4.1 |
| **Computer Vision** | OpenCV | 4.13.0 |
| **Maps** | OSMDroid | 6.1.18 |
| **GPS** | Google Play Services Location | 21.1.0 |
| **Heatmap** | OSMBonusPack | 6.9.0 |
| **ONNX Runtime** | Microsoft ONNX Runtime | 1.20.0 |
| **TensorFlow Lite** | TensorFlow Lite | 2.16.1 |
| **Network** | Retrofit + OkHttp | 2.9.0 / 4.12.0 |

### Compiler Configuration

- **Kotlin Compiler**: 2.0.21 (K2 compiler)
- **JVM Target**: 17
- **Java Compatibility**: 17
- **KSP**: 2.0.21-1.0.28 (Room and Hilt annotation processing)
- **Core Library Desugaring**: Enabled for java.time API on older Android
- **Serialization Plugin**: kotlin("plugin.serialization") 2.0.21 for kotlinx.serialization
- **Hilt Plugin**: 2.52
- **Android Gradle Plugin**: 8.7.2

### Compose Configuration

- **Adaptive Layouts**: `androidx.compose.material3.adaptive` 1.0.0
- **Material Design 3**: Enabled
- **Experimental APIs**: LayoutApi, ExperimentalMaterial3Api opted-in
- **Compose BOM**: 2024.12.01

---

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  (Compose UI, ViewModels, Screens, Navigation)              │
├─────────────────────────────────────────────────────────────┤
│                        Domain Layer                          │
│  (Models, Repository Interfaces, Use Cases)                 │
├─────────────────────────────────────────────────────────────┤
│                         Data Layer                           │
│  (Room DB, DAOs, Repository Implementations, Sync Workers)  │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow (Unidirectional)

```
UI (Compose) → ViewModel → Repository → DAO → Room DB
                    ↓
              StateFlow ← UI observes
```

### Dependency Injection Graph

```
ShambaSmartApplication
        │
        ├── DatabaseModule (Room + SQLCipher)
        ├── RepositoryModule (All repositories)
        ├── SyncModule (WorkManager + SyncManager)
        ├── NetworkModule (API services)
        ├── MapModule (OSMDroid configuration)
        ├── MaarifaModule (Knowledge engine)
        └── PreferencesModule (DataStore)
```

---

## Database Schema

### Entity Relationships (Foreign Keys)

```
Farm (1) ──→ (N) Plot
Plot (1) ──→ (N) CropPlanting
Plot (1) ──→ (N) HarvestRecord
Plot (1) ──→ (N) ScoutingReport (FK→Plot SET_NULL)

Animal (1) ──→ (N) HealthRecord (FK→Animal CASCADE)
Animal (1) ──→ (N) WeightEntry (FK→Animal CASCADE)
Animal (1) ──→ (N) ReproductionRecord (damId CASCADE, sireId SET_NULL)
Animal (1) ──→ (N) MilkProduction (FK→Animal CASCADE)
Animal (1) ──→ (N) MilkCollection (sourceAnimalId SET_NULL)

Vehicle (1) ──→ (N) VehiclePart (FK→Vehicle CASCADE)

Worker (1) ──→ (N) AttendanceRecord (FK→Worker CASCADE)
Worker (1) ──→ (N) Task (assignedWorkerId SET_NULL)

IngestedDocument (1) ──→ (N) KnowledgeChunk (FK→IngestedDocument CASCADE)
```

### Key Entities

| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Animal** | Individual animals | species, breed, tag_number, birth_date, weight |
| **HealthRecord** | Animal health records | animal_id (FK→Animal CASCADE), diagnosis, treatment |
| **WeightEntry** | Weight tracking | animal_id (FK→Animal CASCADE), date, weight_kg |
| **ReproductionRecord** | Breeding management | dam_id (FK→Animal CASCADE), sire_id (FK→Animal SET_NULL) |
| **MilkProduction** | Milk yield tracking | animal_id (FK→Animal CASCADE), date, morning_yield, evening_yield |
| **MilkCollection** | Milk collection for cheese | source_animal_id (FK→Animal SET_NULL), collection_date, quantity_liters |
| **Plot** | Individual plots within farm | name, size_acres, soil_type, irrigation_type, status |
| **CropPlanting** | Crop plantings in plots | crop_type, variety, planting_date, expected_harvest_date |
| **ScoutingReport** | Pest scouting reports | pest_type, severity_score, plot_id (FK→Plot SET_NULL) |
| **Vehicle** | Farm vehicles/equipment | name, type, registration, purchase_date |
| **VehiclePart** | Vehicle parts inventory | vehicle_id (FK→Vehicle CASCADE), part_name, part_number |
| **Worker** | Farm workers | name, role, phone, hire_date, salary |
| **AttendanceRecord** | Attendance tracking | worker_id (FK→Worker CASCADE), date, status, daily_rate_snapshot |
| **Task** | To-do items | assigned_worker_id (FK→Worker SET_NULL), title, due_date, priority |
| **KnowledgeChunk** | Maarifa knowledge | source_document_id (FK→IngestedDocument CASCADE), content, domain |
| **IngestedDocument** | Imported knowledge documents | title, domain_tag, source_credibility, processing_status |

---

## Demo Mode

| Field | Value |
|-------|-------|
| **Entry point** | LaunchChoiceScreen (after onboarding) or Settings → Try Demo Mode |
| **Database** | Room in-memory database — never persists |
| **Data seeder** | DemoDataSeeder.kt — seeds all 17 modules with realistic data |
| **Animals** | 62 goats + 25 sheep with varied statuses |
| **Plots** | 8 plots across 16 acres, 7 active crop plantings |
| **Cheese batches** | 4 batches at varied aging stages |
| **Financial history** | 3 months of income and expense records |
| **Workers** | 4 workers with 26 days of attendance |
| **Alerts** | 9 active alerts across all priority levels |
| **Maarifa chunks** | 50 representative chunks (not full 500+) |
| **Launch target** | Ready within 5 seconds of tapping "Launch demo" |
| **Indicator** | Amber banner on every screen: "Demo mode — all data is simulated" |
| **Exit** | "Exit demo" button in banner → confirmation → LaunchChoiceScreen |
| **Disabled features** | Backup export/restore, remote sync, weather API sync |

### Implementation Status
- [ ] Sprint 1: Foundation — Launch Choice Screen & Demo Mode Manager
- [ ] Sprint 2: Demo Data Infrastructure — Hilt Module & Data Seeder
- [ ] Sprint 3: Demo Data — Animals & Livestock Module
- [ ] Sprint 4: Demo Data — Crops, Plots & Scouting
- [ ] Sprint 5: Demo Data — Finance, Labour & Tasks
- [ ] Demo Data — Remaining Modules
- [ ] Sprint 7: Demo Banner & Exit Flow
- [ ] Sprint 8: Integration, Testing & Polish

### Key Files
- `presentation/onboarding/LaunchChoiceScreen.kt` — Entry point screen
- `demo/DemoModeManager.kt` — Core demo mode logic
- `demo/DemoModeModule.kt` — Hilt DI module for demo repositories
- `demo/DemoDataSeeder.kt` — Seeds all demo data
- `demo/DemoBanner.kt` — Persistent demo indicator
- `demo/DemoFarm.kt` — Farm identity constants
- `demo/data/` — Individual data files per module

---

## Notes & Constraints (Updated)

- All financial calculations accurate to 2 decimal places (TZS)
- Date/time handling accounts for EAT (UTC+3)
- Landscape orientation only. Designed for Xiaomi Pad 7 in landscape mode. Portrait not supported.
- Handle device rotation gracefully
- Preserve data during configuration changes
- Test with slow/unreliable network conditions
- Ensure accessibility for users with basic smartphone literacy
- Maarifa never fabricates information outside its knowledge base
- Drug dosage answers always include veterinarian consultation disclaimer
- Notifiable disease alerts trigger TVLA contact guidance
- All entities include revision_id, last_modified_by, last_updated fields for delta sync
- SyncManager implements watermark-based delta sync with retry/backoff
- Pest detection uses species-specific confidence thresholds
- Scouting reports store encrypted images locally
- Heatmap uses OSMDroid overlays with severity-based coloring
- English only. Swahili localisation reserved for Phase 2.
- Infrastructure module uses custom Canvas-based farm map with GPS coordinates.
