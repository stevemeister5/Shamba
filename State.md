# Shamba Smart — Complete Technical Specification

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
| **Version Code** | 2 |
| **Min SDK** | Android 10 (API 29) |
| **Target SDK** | Android 14 (API 34) |
| **Compile SDK** | 34 |

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
| **Language** | Kotlin | 1.9.x |
| **UI Framework** | Jetpack Compose | BOM 2024.01.00 |
| **Database** | Room | 2.6.1 |
| **Encryption** | SQLCipher | 4.5.4 |
| **Dependency Injection** | Hilt (Dagger) | 2.50 |
| **Navigation** | Compose Navigation | 2.7.6 |
| **Background Work** | WorkManager | 2.9.0 |
| **Preferences** | DataStore | 1.0.0 |
| **JSON** | Gson | 2.10.1 |
| **Image Loading** | Coil | 2.5.0 |
| **Charts** | Vico | 1.13.1 |
| **Date/Time** | kotlinx-datetime | 0.5.0 |
| **Coroutines** | kotlinx-coroutines | 1.7.3 |
| **Security** | AndroidX Security Crypto | 1.1.0-alpha06 |
| **PDF** | iText | 8.0.2 |
| **Camera** | CameraX | 1.3.1 |
| **Computer Vision** | OpenCV | 4.9.0 |
| **Maps** | OSMDroid | 6.1.18 |
| **GPS** | Google Play Services Location | 21.1.0 |
| **Heatmap** | OSMBonusPack | 6.9.0 |

### Compiler Configuration

- **Kotlin Compiler Extension**: 1.5.8
- **JVM Target**: 17
- **Java Compatibility**: 17
- **KSP**: Enabled for Room and Hilt annotation processing
- **Core Library Desugaring**: Enabled for java.time API on older Android

### Compose Configuration

- **Adaptive Layouts**: `androidx.compose.material3.adaptive` 1.0.0-alpha05
- **Material Design 3**: Enabled
- **Experimental APIs**: LayoutApi, ExperimentalMaterial3Api opted-in

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
        └── MaarifaModule (Knowledge engine)
```

### Project Structure

```
app/src/main/java/com/shambasmart/
├── MainActivity.kt
├── ShambaSmartApplication.kt
├── data/
│   ├── local/
│   │   ├── ShambaDatabase.kt
│   │   ├── converter/Converters.kt
│   │   ├── dao/ (24 DAOs)
│   │   └── entity/ (28 entities)
│   ├── remote/ (API services)
│   ├── repository/ (Repository implementations)
│   └── sync/ (SyncWorker)
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── SyncModule.kt
│   ├── NetworkModule.kt
│   └── MapModule.kt
├── domain/
│   ├── model/Alert.kt
│   ├── repository/ (Interfaces)
│   └── usecase/ (Business logic)
├── maarifa/
│   ├── MaarifaModule.kt
│   ├── MaarifaViewModel.kt
│   ├── chunker/SemanticChunker.kt
│   ├── contextbridge/ContextBridge.kt
│   ├── ingestion/
│   │   ├── KnowledgeBootstrapper.kt
│   │   └── KnowledgeIngestionPipeline.kt
│   ├── retrieval/
│   │   ├── IntentClassifier.kt
│   │   ├── KnowledgeRetriever.kt
│   │   ├── ResponseAssembler.kt
│   │   └── VectorSearchEngine.kt
│   ├── rules/RuleEngine.kt
│   └── ui/
│       ├── KnowledgeInboxScreen.kt
│       ├── MaarifaContextCard.kt
│       ├── MaarifaFloatingTab.kt
│       ├── MaarifaSidePanel.kt
│       └── SymptomCheckerScreen.kt
├── map/
│   ├── FarmMapScreen.kt
│   ├── FarmMapViewModel.kt
│   ├── MapMarkerType.kt
│   ├── drawing/
│   ├── heatmap/
│   ├── integration/
│   └── offline/
├── ml/
│   ├── ModelOptimizer.kt
│   ├── audio/
│   ├── lcr/
│   ├── vision/
│   └── water/
├── presentation/
│   ├── alerts/
│   ├── ar/
│   ├── calendar/
│   ├── cheese/
│   ├── common/
│   ├── crops/
│   ├── dashboard/
│   ├── feed/
│   ├── financial/
│   ├── gps/
│   ├── infrastructure/
│   ├── labour/
│   ├── livestock/
│   ├── maintenance/
│   ├── ml/
│   ├── navigation/
│   ├── settings/
│   └── setup/
├── security/
│   ├── EncryptionHelper.kt
│   └── KeystoreManager.kt
└── util/
```

---

## Database Schema

### Entity Overview (28 Tables)

#### Livestock Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **Animal** | Individual animal records | id, tagId, species, breed, sex, birthDate, status, weight, damId, sireId |
| **HealthRecord** | Veterinary treatments & vaccinations | animalId, type, date, description, vaccineName, nextDueDate |
| **ReproductionRecord** | Breeding & kidding records | damId, sireId, type, matingDate, pregnancyStatus, expectedDueDate, actualBirthDate, kidsCount |
| **MilkProduction** | Daily milk yield per doe | animalId, date, morningYield, eveningYield, notes |
| **WeightEntry** | Growth tracking | animalId, date, weight, notes |

#### Crop Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **Plot** | Field/plot registry | id, name, sizeAcres, currentUse, soilType, location |
| **CropPlanting** | Planting records | plotId, cropType, variety, plantingDate, expectedHarvestDate, status |
| **HarvestRecord** | Harvest tracking | cropPlantingId, date, quantityKg, grade, destination, pricePerKg |
| **SilageInventory** | Silage stock | date, quantity, quality, pitLocation, fermentationDays |
| **WeatherLog** | Weather data | date, rainfallMm, maxTemp, minTemp, humidity, windSpeed, notes |

#### Cheese Production Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **MilkCollection** | Raw milk for cheese | date, quantityLitres, pH, smellTest, colorTest, accepted, sourceAnimalId |
| **CheeseBatch** | Production batches | batchId, milkVolume, cheeseType, yieldKg, startDate, agingLocation, status |

#### Feed & Store Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **FeedInventory** | Feed stock levels | feedType, quantity, unit, reorderThreshold, costPerUnit, lastRestocked |
| **StoreItem** | Farm supplies inventory | itemName, category, quantity, unit, expiryDate, reorderThreshold |

#### Financial Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **Income** | Revenue records | date, category, description, amount, paymentMethod |
| **Expense** | Cost records | date, category, description, amount, paymentMethod |
| **Loan** | Credit tracking | lender, principalAmount, interestRate, startDate, dueDate, status, amountPaid |

#### Labour Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **Worker** | Employee records | name, role, contact, hireDate, dailyRate, isSeasonal, isActive |
| **AttendanceRecord** | Daily attendance | workerId, date, isPresent, overtimeHours, notes |

#### Calendar & Tasks

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **CalendarEvent** | Farm events | title, date, type, description, isMaarifaGenerated |
| **Task** | To-do items | title, dueDate, isCompleted, priority, assignedWorkerId |

#### Infrastructure & Map

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **FarmBoundary** | GPS boundary data | boundaryJson, areaAcres, lastUpdated |
| **MapMarker** | Custom map markers | latitude, longitude, type, label, notes |
| **MaintenanceTask** | Equipment/infrastructure maintenance | equipmentType, description, scheduledDate, status |

#### System Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **SyncStatus** | Sync tracking | entityType, entityId, lastSyncedAt, isSynced |
| **Vehicle** | Equipment registry | name, type, fuelType, purchaseDate |
| **VehiclePart** | Maintenance parts | vehicleId, partName, lastReplacedDate, nextServiceDate |

### Maarifa Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **KnowledgeChunk** | Text chunks from documents | content, domainTag, sourceDocument, embedding, keywords |
| **OperationalRule** | Computable rules | ruleType, condition, action, priority |
| **IngestedDocument** | Imported knowledge files | title, domainTag, filePath, chunkCount, dateIngested |

### DAOs (24 Data Access Objects)

| DAO | Purpose |
|-----|---------|
| AnimalDao | CRUD for animals, queries by species/status |
| HealthRecordDao | Health records by animal, vaccination schedules |
| ReproductionDao | Breeding records, gestation tracking |
| MilkProductionDao | Milk yield queries, daily summaries |
| WeightEntryDao | Growth tracking queries |
| PlotDao | Plot management |
| CropDao | Planting records, harvest tracking |
| HarvestDao | Harvest records, yield analytics |
| SilageDao | Silage inventory |
| WeatherDao | Weather logs |
| WeatherCacheDao | Cached weather data |
| WeatherEventDao | Weather alerts |
| CheeseDao | Milk collection, cheese batches |
| FeedDao | Feed inventory |
| StoreDao | Store items |
| FinancialDao | Income, expenses, loans |
| WorkerDao | Worker management |
| TaskDao | Task management |
| CalendarDao | Calendar events |
| SyncDao | Sync status tracking |
| BoundaryDao | Farm boundaries |
| MapMarkerDao | Map markers |
| MaintenanceTaskDao | Maintenance tracking |
| AudioEventDao | Acoustic detection events |

---

## Application Modules

### 1. Dashboard Module

**Files**: `DashboardScreen.kt`, `DashboardViewModel.kt`

**Features**:
- Live KPI cards: Herd Size, Milk Today, Cheese Batches, Pending Tasks
- Alert section: Pending tasks, low feed stock, health flags
- Morning briefing from Maarifa (seasonal tips, urgent actions)
- Real-time data from Room database via StateFlow

### 2. Livestock Module

**Files**: `LivestockScreen.kt`, `LivestockViewModel.kt`, `HealthRecordsScreen.kt`, `ReproductionScreen.kt`, `MilkProductionScreen.kt`, `GrowthTrackingScreen.kt`

**Features**:
- **Animal Profile System**: CRUD operations, tag management, species tracking
- **Health Records**: Vaccination schedules, treatments, illness logging
- **Reproduction Tracking**: Heat detection, mating, pregnancy, kidding
- **Milk Production**: AM/PM yield logging per doe, daily summaries
- **Growth Tracking**: Weight entries, gain analytics, trend visualization

**Maarifa Integration**:
- Context cards per animal (breed-specific guidance)
- Automatic symptom checker on health record entry
- Vaccine dose auto-fill from formulary
- Withdrawal period calculation and blocking flags
- Milk drop alert (20% below 7-day average)
- Outbreak detection (3+ similar symptoms in 7 days)

### 3. Crops Module

**Files**: `PlotRegistryScreen.kt`, `CropPlantingScreen.kt`, `HarvestScreen.kt`, `WeatherScreen.kt`, `PlotAnalyticsScreen.kt`, `CropsViewModel.kt`

**Features**:
- **Plot Registry**: 16-acre farm plot management
- **Crop Planting**: Planting records, growth stages, rotation planning
- **Harvest Tracking**: Quantity, quality grading, pricing
- **Weather Logging**: Rainfall, temperature, wind, events
- **Analytics**: Yield trends, input costs, profitability

**Maarifa Integration**:
- Crop status cards (growth stage, due inputs)
- Pesticide PHI, mixing rates, safety info
- Post-harvest handling guidance
- Planting recommendations (spacing, seed rate, fertiliser schedule)
- Silage quality assessment checklist

### 4. Cheese Module

**Files**: `MilkCollectionScreen.kt`, `CheeseProductionScreen.kt`, `CheeseInventoryScreen.kt`, `CheeseViewModel.kt`

**Features**:
- **Milk Collection**: Quality checks (smell, color, pH), acceptance/rejection
- **Cheese Production**: Batch tracking, yield, aging status
- **Inventory & Sales**: Ready batches, sale recording, revenue tracking

**Maarifa Integration**:
- Complete process guides per cheese type
- Defect diagnosis from quality notes
- Milk quality card (pH/acidity alerts)
- TFDA labelling requirements checklist
- Conversion efficiency benchmarking

### 5. Feed Module

**Files**: `FeedInventoryScreen.kt`, `StoreScreen.kt`, `FeedViewModel.kt`, `StoreViewModel.kt`

**Features**:
- **Feed Inventory**: Stock levels, reorder thresholds, cost tracking
- **Store Management**: Seeds, fertiliser, chemicals, medicine, equipment
- Low stock and expiring item alerts

**Maarifa Integration**:
- Feed gap risk cards (days remaining, nutritional impact)
- Ration calculator (DM intake, energy, protein by animal class)
- Silage quality scoring

### 6. Financial Module

**Files**: `FinancialScreen.kt`, `FinancialViewModel.kt`

**Features**:
- Income tracking (milk, cheese, animals, crops)
- Expense tracking (feed, labour, vet, medicine, seeds)
- Loan/credit management
- Balance calculation with color coding
- Tab layout for Income/Expenses/Loans

**Maarifa Integration**:
- Drug price benchmarking (flag overpriced purchases)
- Enterprise P&L benchmarks for herd size

### 7. Labour Module

**Files**: `LabourScreen.kt`, `LabourViewModel.kt`

**Features**:
- Worker management (permanent/seasonal)
- Attendance tracking
- Payroll calculation
- Task assignment

**Maarifa Integration**:
- Task knowledge cards (protocols, safety, records)
- Observation parsing (symptom keyword detection)

### 8. Calendar Module

**Files**: `CalendarScreen.kt`, `CalendarViewModel.kt`

**Features**:
- Event management (planting, harvest, vaccination, market)
- Task list with completion tracking
- Tab layout for Events/Tasks

**Maarifa Integration**:
- Auto-generated events (vaccination due dates, deworming, kidding dates)
- Crop harvest windows
- Seasonal planting calendars

### 9. Infrastructure Module

**Files**: `InfrastructureScreen.kt`

**Features**:
- Farm map placeholder
- Infrastructure cards: shelters, water points, storage, cheese room, compost pits
- Plot overview

### 10. Settings Module

**Files**: `SettingsScreen.kt`, `SettingsViewModel.kt`

**Features**:
- Language selection (English/Swahili)
- User role management (Owner, Farm Manager, Worker)
- Farm profile editing
- Notifications toggle
- Data export and backup

### 11. Alerts Module

**Files**: `AlertsScreen.kt`, `AlertsViewModel.kt`

**Features**:
- Centralized alert dashboard
- Health alerts, feed low stock, task reminders
- Priority-based sorting

### 12. Maintenance Module

**Files**: `MaintenanceScreen.kt`, `MaintenanceViewModel.kt`

**Features**:
- Equipment maintenance tracking
- Scheduled maintenance tasks
- Vehicle and parts management

### 13. Setup Module

**Files**: `FarmSetupScreen.kt`, `FarmSetupViewModel.kt`

**Features**:
- Initial farm configuration wizard
- First-time user onboarding

---

## Maarifa Knowledge Engine

### Overview

Maarifa is a fully offline agricultural knowledge engine embedded in Shamba Smart. It provides contextual guidance throughout the application via:
1. **Context Cards**: Inline knowledge cards within each module
2. **Ask Maarifa Panel**: Persistent floating tab accessible from any screen
3. **Symptom Checker**: Guided diagnostic wizard for livestock health

### Architecture (Three Layers)

```
┌─────────────────────────────────────────────────────────────┐
│                   Layer 1: Prose Knowledge                  │
│  Plain English text chunks with metadata                    │
│  BM25 keyword search + 384-dim vector embeddings            │
│  Schema-free, infinitely extensible                         │
├─────────────────────────────────────────────────────────────┤
│                 Layer 2: Operational Rules                  │
│  Structured, computable rules                               │
│  Types: withdrawal, dose, gestation, planting, etc.         │
│  Deterministic calculations (no hallucination)              │
├─────────────────────────────────────────────────────────────┤
│                  Layer 3: Live Farm Data                    │
│  Reads from Room DB (animals, plots, treatments, etc.)      │
│  Context injection into every query                         │
│  Never modifies farm data                                   │
└─────────────────────────────────────────────────────────────┘
```

### Components

| Component | File | Purpose |
|-----------|------|---------|
| SemanticChunker | `chunker/SemanticChunker.kt` | Section-aware document chunking (400 words, 50-word overlap) |
| IntentClassifier | `retrieval/IntentClassifier.kt` | Rule-based intent + entity extraction |
| KnowledgeRetriever | `retrieval/KnowledgeRetriever.kt` | BM25 + metadata weighted retrieval |
| VectorSearchEngine | `retrieval/VectorSearchEngine.kt` | ONNX all-MiniLM-L6-v2 embeddings (384 dimensions) |
| ResponseAssembler | `retrieval/ResponseAssembler.kt` | Consistency checking + answer assembly |
| RuleEngine | `rules/RuleEngine.kt` | Withdrawal, dose, gestation, planting calculations |
| ContextBridge | `contextbridge/ContextBridge.kt` | Live farm data injection |
| KnowledgeIngestionPipeline | `ingestion/KnowledgeIngestionPipeline.kt` | Quality gates + conflict detection |
| KnowledgeBootstrapper | `ingestion/KnowledgeBootstrapper.kt` | Initial knowledge base loading |
| MaarifaViewModel | `MaarifaViewModel.kt` | Orchestrates full retrieval pipeline |

### Retrieval Pipeline

1. **Intent Classification** — Rule-based keyword matching → ranked intents
2. **Entity Extraction** — Species, crop, drug, symptoms, quantities
3. **Context Injection** — Live farm data (herd, weather, feed, season)
4. **Pre-filter** — Metadata filters reduce search space
5. **Knowledge Retrieval** — BM25 + vector cosine + metadata weighted fusion
6. **Rule Engine Overlay** — Deterministic calculations
7. **Consistency Checking** — Species matching, contradiction detection, notifiable disease scan
8. **Response Assembly** — Structured answer with confidence model

### Four-Tier Confidence Model

| Tier | Label | Meaning |
|------|-------|---------|
| 1 | Calculated from verified rule | Highest — computed from operational rules |
| 2 | Based on multiple sources | 3+ high-scoring sources agree |
| 3 | Limited sources — verify | Few sources or ambiguous query |
| 4 | Not in knowledge base | Honest failure — no reliable info |

### Knowledge Domains

- **Crops**: 17+ crop types with planting, fertiliser, pest/disease, harvest data
- **Livestock Goats**: Breeds, health, reproduction, nutrition, kid management
- **Livestock Sheep**: Breeds, health, reproduction, nutrition, lamb management
- **Medicines**: Formulary with dosages, withdrawal periods, Tanzania availability
- **Cheese**: Process guides, quality control, defect diagnosis, TFDA compliance
- **Weather**: 30-year Korogwe climate data, seasonal calendars, risk indicators

### Symptom Checker

8-step guided diagnostic wizard:
1. Select species (Goat/Sheep)
2. Select animal (from herd list or Unknown)
3. Body system affected (Respiratory/Digestive/Reproductive/Skin & Hooves/Nervous/General/Udder)
4. Select symptoms (checkbox list filtered by body system)
5. Duration (Today/2-3 days/3+ days)
6. Number affected (This animal/2-3/Many)
7. Recent events (New animal/Stress/Feed change/Rainfall/Vaccination/None)

Output: Top 3 differential diagnoses with confidence, treatment, herd risk, urgency level, notifiable disease flags

### Knowledge Inbox

Import new documents to expand knowledge base:
- **Supported formats**: PDF (text-based), DOCX, TXT, pasted text
- **Ingestion pipeline**: Extract → Chunk → Index → Embed → Store
- **Processing**: Background thread with progress indicator
- **Management**: Library view, delete documents, source citations

---

## GPS & Mapping System

### Components

| Component | File | Purpose |
|-----------|------|---------|
| FarmMapScreen | `map/FarmMapScreen.kt` | Main map interface |
| FarmMapViewModel | `map/FarmMapViewModel.kt` | Map state management |
| GPSBoundaryScreen | `presentation/gps/GPSBoundaryScreen.kt` | Boundary drawing |
| GPSBoundaryViewModel | `presentation/gps/GPSBoundaryViewModel.kt` | Boundary logic |
| GPSKalmanFilter | `presentation/gps/GPSKalmanFilter.kt` | GPS noise filtering |
| LocationProvider | `presentation/gps/LocationProvider.kt` | GPS access |
| PolygonCalculator | `presentation/gps/PolygonCalculator.kt` | Area calculations |

### Map Features

- **OSMDroid**: OpenStreetMap-based, no API token required
- **Offline Maps**: Pre-cached tile support
- **Custom Markers**: Shelters, water points, plots, livestock areas
- **Boundary Drawing**: GPS-based farm boundary capture
- **Heatmap Overlay**: Crop health, soil moisture visualization
- **Area Calculation**: Polygon-based acreage computation

---

## Data Synchronization

### Sync Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Room DB    │ ←→  │ SyncWorker   │ ←→  │ Remote API  │
│ (Source of  │     │ (WorkManager)│     │ (When       │
│  Truth)     │     │              │     │  online)    │
└─────────────┘     └──────────────┘     └─────────────┘
```

### Sync Configuration

| Setting | Value |
|---------|-------|
| **Strategy** | Offline-first |
| **Sync Interval** | 15 minutes (when online) |
| **Retry Policy** | Exponential backoff (max 3 retries) |
| **Conflict Resolution** | Last-write-wins with audit trail |
| **Delta Sync** | Using `last_synced_timestamp` |

### SyncWorker

- **Annotation**: `@HiltWorker`
- **Network Check**: `SyncManager` verifies connectivity before sync
- **Entity Support**: All 22+ entity types
- **Status Tracking**: `SyncStatus` entity tracks per-entity sync state

---

## Security

### Database Encryption

- **SQLCipher**: 4.5.4 with AES-256 encryption
- **Key Management**: Android Keystore integration
- **Passphrase**: Derived from device-specific hardware ID

### Security Components

| Component | File | Purpose |
|-----------|------|---------|
| EncryptionHelper | `security/EncryptionHelper.kt` | AES encryption/decryption utilities |
| KeystoreManager | `security/KeystoreManager.kt` | Android KeyStore operations |

### Permissions

- `INTERNET` — Weather API, sync
- `ACCESS_FINE_LOCATION` — GPS boundary capture
- `ACCESS_COARSE_LOCATION` — Approximate location
- `CAMERA` — Computer vision tasks
- `RECORD_AUDIO` — Acoustic detection
- `WRITE_EXTERNAL_STORAGE` — Data export
- `READ_EXTERNAL_STORAGE` — Document import

---

## Build Configuration

### Gradle Configuration

```kotlin
android {
    namespace = "com.shambasmart"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.shambasmart"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        buildConfigField("String", "WEATHER_API_BASE_URL", 
            "\"https://api.openweathermap.org/data/2.5/\"")
        buildConfigField("Double", "FARM_LATITUDE", "-5.15")
        buildConfigField("Double", "FARM_LONGITUDE", "38.48")
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

### ProGuard

- Enabled for release builds
- Custom rules for Room, Hilt, Gson, Compose

---

## Performance Targets

| Metric | Target |
|--------|--------|
| App Launch | < 2 seconds |
| Screen Transition | < 300ms |
| Database Query | < 50ms |
| ML Inference (NPU) | < 50ms |
| UI Frame Rate | 144 Hz (stable) |
| Offline Storage | < 500 MB |
| Vector Embedding | 2-4 min per 100-page PDF |

---

## Development Environment

### IDE & Tools

| Tool | Version/Details |
|------|-----------------|
| **IDE** | Android Studio |
| **Version Control** | Git |
| **Branch Strategy** | Feature branches from main |
| **Build System** | Gradle 8.x with Kotlin DSL |
| **Annotation Processing** | KSP (Kotlin Symbol Processing) |

### Environment Configuration

| Setting | Value |
|---------|-------|
| **Farm Coordinates** | -5.15°S, 38.48°E (Korogwe, Tanga) |
| **Timezone** | EAT (UTC+3) |
| **Primary Language** | English |
| **Secondary Language** | Swahili (strings prepared) |
| **Currency** | TZS (Tanzanian Shilling) |
| **Weather API** | OpenWeatherMap |

---

## User Roles & Access Control

| Role | Access Level |
|------|--------------|
| **Owner** | Full access to all modules including financials |
| **Farm Manager** | All modules except detailed financial reports |
| **Worker** | Tasks, daily logs, and assigned activities only |

---

## String Resources

- **English**: `res/values/strings.xml` (Primary)
- **Swahili**: `res/values-sw/strings.xml` (Secondary)

---

## API Integration

### Weather API

- **Provider**: OpenWeatherMap
- **Endpoint**: `https://api.openweathermap.org/data/2.5/`
- **Coordinates**: -5.15, 38.48 (Korogwe)
- **Data**: Current weather, 5-day forecast, historical data
- **Caching**: `WeatherCacheDao` for offline access

---

## Testing

### Test Configuration

| Type | Framework |
|------|-----------|
| **Unit Tests** | JUnit 4.13.2 |
| **Coroutines Test** | kotlinx-coroutines-test 1.7.3 |
| **Flow Testing** | Turbine 1.0.0 |
| **UI Tests** | Compose UI Test JUnit4 |
| **Integration** | AndroidX Test + Espresso |

### Test Structure

```
app/src/
├── test/java/          # Unit tests
└── androidTest/java/   # Instrumented tests
```

---

## Future Roadmap

### Phase 11: ML/AI Integration (Planned)
- NPU-accelerated livestock health monitoring
- Computer vision for crop disease detection
- Acoustic detection for respiratory diseases
- ET-based irrigation optimization
- Least-Cost Ration (LCR) solver

### Phase 12: Advanced Analytics (Planned)
- Predictive analytics for yield forecasting
- Financial trend analysis
- Automated breeding recommendations
- Climate risk assessment

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-03-27 | Initial release with core modules |
| 1.1.0 | 2026-03-27 | East Africa Pest-Guard upgrade: Hardware-backed security, watermark delta sync, ONNX pest detection (8 pests), ScoutingReport entity, pest heatmap, Maarifa knowledge mapping, enhanced CameraX, ML model management |

---

## Technical Decisions Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-03-27 | Offline-first architecture | Farm location has unreliable connectivity |
| 2026-03-27 | SQLCipher encryption | Protect sensitive farm and financial data |
| 2026-03-27 | OSMDroid over Google Maps | No API token required, offline tile support |
| 2026-03-27 | Maarifa: No on-device LLM | Prevent hallucination, ensure deterministic answers |
| 2026-03-27 | Maarifa: ONNX embeddings only | Vector search without language generation |
| 2026-03-27 | Schema-free knowledge storage | Extensible without migrations |
| 2026-03-27 | Triple retrieval (BM25 + vector + rules) | No single method trusted alone |
| 2026-03-27 | Four-tier confidence model | Communicate reliability honestly |

---

## Notes & Constraints

- All financial calculations accurate to 2 decimal places (TZS)
- Date/time handling accounts for EAT (UTC+3)
- Support both portrait and landscape orientations
- Handle device rotation gracefully
- Preserve data during configuration changes
- Test with slow/unreliable network conditions
- Ensure accessibility for users with basic smartphone literacy
- Maarifa never fabricates information outside its knowledge base
- Drug dosage answers always include veterinarian consultation disclaimer
- Notifiable disease alerts trigger TVLA contact guidance