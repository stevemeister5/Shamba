# Shamba Smart v1.1 — Task List

## "East Africa Pest-Guard" Upgrade

**Phase 4 (Livestock Vision) has been deferred to v1.2.**

---

### Phase 1: Architectural Hardening (Infrastructure)

#### Task 1.1: Hardware-Backed Security Refactor
- **Status:** ❌ Not Started
- **Description:** Refactor `KeystoreManager.kt` to use random AES-256 key generated in Android Keystore
- **Subtasks:**
  - [ ] Implement random AES-256 key generation in Android Keystore
  - [ ] Update SQLCipher to use the new Keystore-backed key
  - [ ] Implement encrypted daily SD card backup functionality
  - [ ] Add backup scheduling via WorkManager
  - [ ] Create backup restoration flow
- **Dependencies:** None
- **Estimated Effort:** 3 days

#### Task 1.2: Revision-Based Delta Sync
- **Status:** ❌ Not Started
- **Description:** Add revision tracking and watermark-based sync to all 28 tables
- **Subtasks:**
  - [ ] Add `revision_id` (UUID) column to all 28 entity tables
  - [ ] Add `last_modified_by` (user ID) column to all 28 entity tables
  - [ ] Add `last_updated` (LocalDateTime) column to all 28 entity tables
  - [ ] Update `SyncStatus` entity to track `local_max_timestamp` per entity type
  - [ ] Refactor `SyncWorker` to implement watermark strategy (sync only rows where `last_updated > local_max_timestamp`)
  - [ ] Update all 24 DAOs to include revision fields in queries
  - [ ] Add conflict resolution logic for concurrent edits
  - [ ] Test sync with simulated unreliable connectivity
- **Dependencies:** None
- **Estimated Effort:** 5 days

#### Task 1.3: Read-Model Optimization
- **Status:** ❌ Not Started
- **Description:** Implement Room `@DatabaseView` for Dashboard and Plot Analytics
- **Subtasks:**
  - [ ] Create `DashboardView` with pre-joined KPI data (herd size, milk today, cheese batches, tasks)
  - [ ] Create `PlotAnalyticsView` with aggregated crop/yield data
  - [ ] Update ViewModels to use DatabaseViews instead of multiple DAO queries
  - [ ] Verify 144Hz UI performance with new views
- **Dependencies:** Task 1.2
- **Estimated Effort:** 3 days

---

### Phase 2: East African Pest Intelligence (ONNX)

#### Task 2.1: ONNX Runtime Integration
- **Status:** ❌ Not Started
- **Description:** Set up ONNX Runtime for Android with NPU delegate support
- **Subtasks:**
  - [ ] Add ONNX Runtime Android dependency to `build.gradle.kts`
  - [ ] Configure QNN Delegate for Hexagon v73 NPU acceleration
  - [ ] Create `OnnxModelManager.kt` for model loading and inference
  - [ ] Implement model versioning and hot-swap capability
  - [ ] Add INT4/INT8 quantization support
  - [ ] Create benchmark utility for inference speed testing
- **Dependencies:** None
- **Estimated Effort:** 3 days

#### Task 2.2: Regional Pest Classifier Model
- **Status:** ❌ Not Started
- **Description:** Deploy YOLOv8 for East African pest detection using pretrained base model
- **Subtasks:**
  - [ ] Download `Ultralytics/YOLOv8` base model from HuggingFace
  - [ ] Fine-tune on East African pest dataset (FAW, Stalk Borer, Aphids, Blight, Locusts, Leafminer)
  - [ ] Alternatively: Adapt `HurudzaAI/plantdiseasedetection1` (Africa-focused model)
  - [ ] Quantize model to INT4/INT8 for NPU optimization
  - [ ] Bundle model in `assets/models/pest_classifier.onnx`
  - [ ] Create `PestClassifier.kt` wrapper class
  - [ ] Implement bounding box detection output parsing
  - [ ] Add species-specific confidence thresholds
- **Dependencies:** Task 2.1
- **Estimated Effort:** 7 days

#### Task 2.3: Multi-Stage Inference Pipeline
- **Status:** ❌ Not Started
- **Description:** Implement Stage 1 (Detection) and Stage 2 (Severity Classification)
- **Subtasks:**
  - [ ] Create `InferencePipeline.kt` orchestrator
  - [ ] Implement Stage 1: Pest/symptom detection with bounding box
  - [ ] Implement Stage 2: Severity classification (Low, Moderate, Critical)
  - [ ] Add leaf area percentage calculation for severity scoring
  - [ ] Add pest density estimation for severity scoring
  - [ ] Create `SeverityLevel` enum and scoring logic
  - [ ] Optimize pipeline for <50ms inference on NPU
- **Dependencies:** Task 2.2
- **Estimated Effort:** 5 days

#### Task 2.4: Maarifa Knowledge Mapping
- **Status:** ❌ Not Started
- **Description:** Link pest detections to Maarifa engine for management protocols
- **Subtasks:**
  - [ ] Create `PestKnowledgeMapper.kt` to link detections to knowledge base
  - [ ] Add Tanzania Ministry of Agriculture approved management protocols for each pest
  - [ ] Implement automatic protocol surfacing on detection
  - [ ] Add pesticide recommendations with dosing and PHI
  - [ ] Add biological control alternatives
  - [ ] Integrate with Maarifa's existing retrieval pipeline
- **Dependencies:** Task 2.3
- **Estimated Effort:** 4 days

---

### Phase 3: Automated Data Recording & Scouting

#### Task 3.1: ScoutingReport Entity & DAO
- **Status:** ❌ Not Started
- **Description:** Create new database entity for pest scouting data
- **Subtasks:**
  - [ ] Create `ScoutingReport.kt` entity with fields:
    - `id` (UUID)
    - `plot_id` (FK to Plot)
    - `pest_type` (String)
    - `severity_score` (1-5 scale)
    - `gps_latitude` (Double)
    - `gps_longitude` (Double)
    - `image_uri` (String - encrypted local path)
    - `detected_at` (LocalDateTime)
    - `revision_id` (UUID)
    - `last_modified_by` (String)
    - `is_synced` (Boolean)
  - [ ] Create `ScoutingReportDao.kt` with CRUD and analytics queries
  - [ ] Add `ScoutingReport` to `ShambaDatabase` (version migration)
  - [ ] Create `ScoutingRepository.kt` interface and implementation
- **Dependencies:** Task 1.2
- **Estimated Effort:** 2 days

#### Task 3.2: Vision-to-Database Mapping
- **Status:** ❌ Not Started
- **Description:** Auto-populate ScoutingReport on confirmed detection
- **Subtasks:**
  - [ ] Create `ScoutingCaptureScreen.kt` with camera integration
  - [ ] Implement GPS-based plot detection (nearest plot to current coordinates)
  - [ ] Create `ScoutingViewModel.kt` to orchestrate capture flow
  - [ ] Implement image compression and encryption before storage
  - [ ] Auto-populate fields from ONNX model output
  - [ ] Add confirmation dialog before saving
  - [ ] Store encrypted image reference in `image_uri`
- **Dependencies:** Task 2.3, Task 3.1
- **Estimated Effort:** 4 days

#### Task 3.3: Scouting Heatmap on Farm Map
- **Status:** ❌ Not Started
- **Description:** Display pest heatmap on OSMDroid Farm Map
- **Subtasks:**
  - [ ] Create `PestHeatmapOverlay.kt` for OSMDroid
  - [ ] Implement color-coded markers (Green→Yellow→Orange→Red) based on severity
  - [ ] Add time-based filtering (last 7 days, 30 days, all time)
  - [ ] Add pest-type filtering dropdown
  - [ ] Implement marker clustering for dense areas
  - [ ] Add tap-to-view details popup
  - [ ] Update `FarmMapScreen.kt` to include heatmap toggle
- **Dependencies:** Task 3.1, Task 3.2
- **Estimated Effort:** 4 days

#### Task 3.4: Actionable Alerts System
- **Status:** ❌ Not Started
- **Description:** Generate dashboard notifications for critical pest detections
- **Subtasks:**
  - [ ] Create `PestAlertGenerator.kt` to monitor ScoutingReports
  - [ ] Implement "Critical" severity detection trigger
  - [ ] Generate dashboard notification with pest details and location
  - [ ] Add "Authorize Treatment" action button
  - [ ] Link notification to relevant Maarifa management protocol
  - [ ] Add notification persistence in `Alert` entity
  - [ ] Update `AlertsScreen.kt` to display pest alerts
- **Dependencies:** Task 3.1, Task 3.2
- **Estimated Effort:** 3 days

---

### Cross-Cutting Concerns

#### Task 5.1: CameraX Integration Enhancement
- **Status:** ❌ Not Started
- **Description:** Enhance camera pipeline for vision tasks
- **Subtasks:**
  - [ ] Optimize CameraX frame capture for ML inference
  - [ ] Implement frame throttling (1-5 FPS for inference)
  - [ ] Add flash control for consistent lighting
  - [ ] Implement image stabilization hints
  - [ ] Add manual focus for close-up pest shots
- **Dependencies:** None
- **Estimated Effort:** 2 days

#### Task 5.2: ML Model Asset Management
- **Status:** ❌ Not Started
- **Description:** Centralized model loading and version management
- **Subtasks:**
  - [ ] Create `ModelManager.kt` singleton for all ONNX models
  - [ ] Implement lazy loading to reduce startup time
  - [ ] Add model version tracking and update mechanism
  - [ ] Create model integrity verification (checksum)
  - [ ] Add memory management for multiple loaded models
  - [ ] Implement model download from remote for updates (when online)
- **Dependencies:** Task 2.1
- **Estimated Effort:** 3 days

#### Task 5.3: Testing & Validation
- **Status:** ❌ Not Started
- **Description:** Comprehensive testing of all v1.1 features
- **Subtasks:**
  - [ ] Unit tests for all new ViewModels and Repositories
  - [ ] Integration tests for sync with revision tracking
  - [ ] UI tests for scouting capture flow
  - [ ] Performance benchmarks for ONNX inference (<50ms target)
  - [ ] Field testing with real pest images on Xiaomi Pad 7
  - [ ] Heatmap rendering performance (<100ms for 1000 markers)
  - [ ] Sync performance testing (<5s for 1000 delta rows)
- **Dependencies:** All previous tasks
- **Estimated Effort:** 15 days (3 weeks)

---

## Summary (Phase 4 Dropped)

| Phase | Tasks | Estimated Effort |
|-------|-------|------------------|
| Phase 1: Architectural Hardening | 3 tasks | 11 days |
| Phase 2: Pest Intelligence | 4 tasks | 19 days |
| Phase 3: Data Recording & Scouting | 4 tasks | 13 days |
| Cross-Cutting | 3 tasks | 20 days |
| **Total** | **14 tasks** | **63 days** |

---

## Restructured Sprint Plan

| Sprint | Duration | Tasks | Focus |
|--------|----------|-------|-------|
| Sprint 1 | 2 weeks | 1.1, 1.2, 2.1, 5.1 | Infrastructure + ONNX setup |
| Sprint 2 | 2 weeks | 1.3, 2.2, 5.2 | Read-models + Pest model |
| Sprint 3 | 2 weeks | 2.3, 2.4, 3.1 | Inference pipeline + Scouting entity |
| Sprint 4 | 2 weeks | 3.2, 3.3 | Scouting UI + Heatmap |
| Sprint 5 | 2 weeks | 3.4, finish remaining | Alerts + Maarifa integration |
| Sprint 6 | 2 weeks | Polish + optimization | Performance + UI refinement |
| Sprint 7 | 3 weeks | 5.3 | Extended testing + field validation |

**Key Changes from Original Plan:**
- Phase 4 (Livestock Vision) dropped — deferred to v1.2
- Task 3.1 dependency fixed: now only depends on Task 1.2 (removed circular Task 3.2 dependency)
- Task 5.3 (Testing) extended from 5 days to 15 days (3 weeks)
- Added Sprint 6 for polish/optimization
- Total timeline: ~15 weeks (down from ~12 weeks with Phase 4)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.1.0 | 2026-03-27 | Task list created for East Africa Pest-Guard upgrade |
| 1.1.1 | 2026-03-27 | Dropped Phase 4, fixed circular dependency, extended testing |