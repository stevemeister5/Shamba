# SHAMBA SMART - Project Context

> **Purpose:** This file tracks the current state of the Shamba Smart project. It is updated after each completed task to provide context for subsequent development work.

---

## Project Information

| Field | Value |
|-------|-------|
| **Project Name** | Shamba Smart |
| **Type** | Android Mobile Application (Kotlin + Jetpack Compose) |
| **Target Device** | Xiaomi Pad 7 (SM7675-AB SoC) |
| **Farm Location** | Korogwe, Tanga, Tanzania |
| **Farm Size** | 16 acres |
| **Primary Language** | Kotlin |
| **UI Framework** | Jetpack Compose Adaptive |
| **Database** | Room + SQLCipher (encrypted) |
| **Architecture** | Single Source of Truth (SSOT) with UDF |

---

## Farm Operations Context

### Livestock
- **Goats:** Dairy-focused breeding
- **Sheep:** Secondary livestock
- **Key Metrics:** Milk yield, herd growth, feed conversion

### Crops
- **Silage:** Napier grass, maize
- **Food Crops:** Maize, beans, cassava, sweet potato
- **Vegetables:** Tomatoes, kale, onions, capsicum

### Value Addition
- **Product:** Goat milk cheese
- **Market:** Local and regional sales
- **Focus:** Quality grading, batch tracking, cost optimization

---

## Technical Stack

### Core Technologies
- **Language:** Kotlin
- **UI:** Jetpack Compose with Adaptive Layouts
- **Database:** Room with SQLCipher encryption
- **DI:** Hilt (Dagger)
- **Sync:** WorkManager
- **Navigation:** Compose Navigation

### Hardware-Specific (Xiaomi Pad 7)
- **SoC:** Qualcomm SM7675-AB (Snapdragon 7+ Gen 3)
- **NPU:** Hexagon v73 NPU
- **Display:** 3.2K @ 144Hz
- **ISP:** 18-bit Triple ISP (Cognitive ISP)
- **Audio:** Qualcomm Sensing Hub

### ML/AI Integration
- **QNN Delegate:** For NPU acceleration
- **OpenCV:** For computer vision tasks
- **INT4 Quantization:** For optimized inference
- **Volatile RAM:** For privacy-sensitive vision processing

---

## Architecture Decisions

### Data Flow
```
UI (Compose) → ViewModel → Repository → Room DB
                                    ↕
                              WorkManager (Sync)
                                    ↕
                              Remote Server (when online)
```

### Offline-First Strategy
- Local Room DB is the source of truth
- WorkManager handles background sync
- Delta sync using last_synced_timestamp
- Conflict resolution: Last-write-wins with audit trail

### User Roles
| Role | Access Level |
|------|--------------|
| Owner | Full access to all modules |
| Farm Manager | All modules except financials |
| Worker | Tasks and daily logs only |

---

## Development Environment

### Current Setup
- **IDE:** Android Studio (recommended)
- **Version Control:** Git
- **Branch Strategy:** Feature branches from main
- **Target SDK:** Android 14 (API 34)
- **Min SDK:** Android 10 (API 29)

### Build Configuration
- **Gradle:** 8.x
- **Kotlin:** 1.9.x
- **Compose BOM:** 2024.x
- **Room:** 2.6.x
- **SQLCipher:** 4.5.x

---

## Project Structure (Target)

```
app/
├── src/main/
│   ├── java/com/shambasmart/
│   │   ├── di/                    # Dependency injection modules
│   │   ├── data/
│   │   │   ├── local/            # Room database, DAOs
│   │   │   ├── remote/           # API services
│   │   │   ├── repository/       # Repository implementations
│   │   │   └── sync/             # WorkManager sync workers
│   │   ├── domain/
│   │   │   ├── model/            # Domain models
│   │   │   ├── repository/       # Repository interfaces
│   │   │   └── usecase/          # Business logic use cases
│   │   ├── presentation/
│   │   │   ├── dashboard/        # Home dashboard
│   │   │   ├── livestock/        # Livestock management
│   │   │   ├── crops/            # Crop management
│   │   │   ├── cheese/           # Cheese production
│   │   │   ├── feed/             # Feed management
│   │   │   ├── financial/        # Financial tracking
│   │   │   ├── labour/           # Worker management
│   │   │   ├── calendar/         # Task planner
│   │   │   ├── infrastructure/   # Farm map
│   │   │   ├── settings/         # App settings
│   │   │   └── common/           # Shared UI components
│   │   ├── ml/                   # ML models and processing
│   │   │   ├── lcr/              # Least-Cost Ration solver
│   │   │   ├── vision/           # Computer vision
│   │   │   ├── water/            # ET optimizer
│   │   │   └── audio/            # Acoustic detection
│   │   └── util/                 # Utility classes
│   ├── res/
│   │   ├── values/               # English strings
│   │   ├── values-sw/            # Swahili strings
│   │   └── ...
│   └── AndroidManifest.xml
├── build.gradle.kts
└── ...
```

---

## Task Completion Log

> **Instructions:** After completing each task from Task.md, add an entry below.

### Template Entry
```
#### Task X: [Task Name]
- **Status:** ✅ Completed / 🔄 In Progress / ❌ Blocked
- **Date:** YYYY-MM-DD
- **Commit:** [commit hash]
- **Changes:**
  - [Summary of changes made]
- **Notes:**
  - [Any issues, decisions, or important context]
- **Next Steps:**
  - [What needs to happen next]
```

---

### Phase 1: Project Setup & Foundation

#### Task 1: Initialize Android Project
- **Status:** ✅ Completed
- **Date:** 2026-03-26
- **Commit:** 07a86ac
- **Changes:**
  - Created Kotlin Android project with Jetpack Compose
  - Configured Gradle with Room, SQLCipher, Hilt, WorkManager, Navigation
  - Set up clean architecture (data, domain, presentation layers)
  - Created dual-pane War Room UI theme with farm-themed colors
  - Added English string resources
  - Configured Android manifest with required permissions
  - Created project directory structure
  - Initialized Git repository
- **Notes:**
  - All 15 files committed successfully
  - Project structure follows clean architecture
  - Ready for Task 2: Configure Database Layer
- **Next Steps:**
  - Proceed to Task 2: Configure Database Layer with Room + SQLCipher

#### Task 2: Configure Database Layer
- **Status:** ✅ Completed
- **Date:** 2026-03-27
- **Commit:** 82fef04
- **Changes:**
  - Created ShambaDatabase with 22 entity tables
  - Created TypeConverters for date/time handling (LocalDate, LocalDateTime, Instant)
  - Created 22 entity classes (Animal, HealthRecord, ReproductionRecord, MilkProduction, WeightEntry, Plot, CropPlanting, HarvestRecord, SilageInventory, WeatherLog, MilkCollection, CheeseBatch, FeedInventory, StoreItem, Income, Expense, Loan, Worker, AttendanceRecord, Task, CalendarEvent, SyncStatus)
  - Created 17 DAOs with CRUD operations and sync support
  - Created DatabaseModule for Hilt dependency injection
  - Configured SQLCipher encryption for secure data storage
- **Notes:**
  - All entities have isSynced field for offline sync support
  - Foreign key relationships established between related entities
  - DAOs include queries for unsynced data retrieval
- **Next Steps:**
  - Proceed to Task 3: Implement Data Sync Architecture

#### Task 3: Implement Data Sync Architecture
- **Status:** ✅ Completed
- **Date:** 2026-03-27
- **Commit:** 01198f8
- **Changes:**
  - Created SyncWorker with HiltWorker annotation for WorkManager
  - Created SyncManager with network connectivity checking, delta sync, sync status tracking
  - Created SyncModule for Hilt dependency injection
  - Periodic sync every 15 minutes (when online)
  - Exponential backoff retry policy
- **Notes:**
  - Sync architecture follows offline-first strategy
  - Conflict resolution: Last-write-wins with audit trail
  - Support for all 22 entity types
- **Next Steps:**
  - Proceed to Task 4: Build Unidirectional Data Flow (UDF)

#### Task 4: Build Unidirectional Data Flow (UDF)
- **Status:** ⬜ Not Started
- **Date:** -
- **Commit:** -
- **Changes:**
  - Pending
- **Notes:**
  - Pending
- **Next Steps:**
  - Pending

---

## Current Sprint / Focus

**Current Phase:** Not Started
**Active Task:** None
**Blockers:** None

---

## Key Decisions Log

| Date | Decision | Rationale |
|------|----------|-----------|
| - | - | - |

---

## Environment Variables & Configuration

### API Endpoints
- **Weather API:** https://api.openweathermap.org/data/2.5/
- **Farm Coordinates:** -5.15, 38.48 (Korogwe, Tanga)

### Sync Configuration
- **Sync Interval:** 15 minutes (when online)
- **Retry Policy:** Exponential backoff (max 3 retries)
- **Conflict Strategy:** Last-write-wins with audit log

---

## Performance Targets

| Metric | Target |
|--------|--------|
| App Launch | < 2 seconds |
| Screen Transition | < 300ms |
| Database Query | < 50ms |
| ML Inference | < 50ms (NPU) |
| UI Frame Rate | 144 Hz (stable) |
| Offline Storage | < 500 MB |

---

## Notes & Reminders

- All financial calculations must be accurate to 2 decimal places (TZS)
- Date/time handling must account for East Africa Time (EAT, UTC+3)
- Support both portrait and landscape orientations
- Handle device rotation gracefully
- Preserve data during configuration changes
- Test with slow/unreliable network conditions
- Ensure accessibility for users with basic smartphone literacy