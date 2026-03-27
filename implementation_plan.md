# Implementation Plan: Shamba Smart v1.1 "East Africa Pest-Guard"

## Overview

Upgrade the Shamba Smart Android application to v1.1, adding hardware-backed security, revision-based delta sync, ONNX-powered pest detection for East African crops, and automated scouting data recording with farm map heatmap visualization. Phase 4 (Livestock Vision) has been deferred to v1.2.

## Scope

This implementation covers 14 tasks across 3 phases plus cross-cutting concerns:

**Phase 1: Architectural Hardening** — Security refactor, delta sync, read-model optimization
**Phase 2: Pest Intelligence** — ONNX runtime, pest classifier, inference pipeline, Maarifa integration
**Phase 3: Data Recording & Scouting** — ScoutingReport entity, vision-to-database mapping, heatmap, alerts
**Cross-Cutting** — CameraX enhancement, ML model management, comprehensive testing

The target hardware is Xiaomi Pad 7 with Snapdragon 7+ Gen 3 (Hexagon v73 NPU). All ML inference must run offline with <50ms latency on NPU.

## Types

### New Entity: ScoutingReport
```kotlin
@Entity(tableName = "scouting_reports")
data class ScoutingReport(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "plot_id") val plotId: String,  // FK to Plot
    @ColumnInfo(name = "pest_type") val pestType: String,  // FAW, StalkBorer, Aphids, etc.
    @ColumnInfo(name = "severity_score") val severityScore: Int,  // 1-5 scale
    @ColumnInfo(name = "gps_latitude") val gpsLatitude: Double,
    @ColumnInfo(name = "gps_longitude") val gpsLongitude: Double,
    @ColumnInfo(name = "image_uri") val imageUri: String,  // Encrypted local path
    @ColumnInfo(name = "detected_at") val detectedAt: LocalDateTime,
    @ColumnInfo(name = "revision_id") val revisionId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "last_modified_by") val lastModifiedBy: String,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
)
```

### New Enums
```kotlin
enum class PestType {
    FALL_ARMYWORM, MAIZE_STALK_BORER, MAIZE_STREAK_VIRUS,
    BEAN_FLY, APHIDS, BLIGHT,
    DESERT_LOCUSTS, TOMATO_LEAFMINER
}

enum class SeverityLevel(val score: Int) {
    LOW(1), MINOR(2), MODERATE(3), SEVERE(4), CRITICAL(5)
}

enum class InferenceStage {
    DETECTION, CLASSIFICATION, SEVERITY
}
```

### Revision Fields (added to all 28 entities)
```kotlin
@ColumnInfo(name = "revision_id") val revisionId: String = UUID.randomUUID().toString(),
@ColumnInfo(name = "last_modified_by") val lastModifiedBy: String = "",
@ColumnInfo(name = "last_updated") val lastUpdated: LocalDateTime = LocalDateTime.now()
```

### Database Views
```kotlin
@DatabaseView("""
    SELECT 
        (SELECT COUNT(*) FROM animals WHERE status = 'ACTIVE') as herdSize,
        (SELECT COALESCE(SUM(morningYield + eveningYield), 0) FROM milk_production WHERE date = date('now')) as milkToday,
        (SELECT COUNT(*) FROM cheese_batches WHERE status = 'AGING') as cheeseBatches,
        (SELECT COUNT(*) FROM tasks WHERE isCompleted = 0) as pendingTasks
""", viewName = "dashboard_view")
data class DashboardView(
    val herdSize: Int,
    val milkToday: Double,
    val cheeseBatches: Int,
    val pendingTasks: Int
)
```

## Files

### New Files to Create

| File Path | Purpose |
|-----------|---------|
| `data/local/entity/ScoutingReport.kt` | ScoutingReport entity |
| `data/local/dao/ScoutingReportDao.kt` | CRUD + analytics queries for scouting |
| `data/local/view/DashboardView.kt` | Dashboard read-model |
| `data/local/view/PlotAnalyticsView.kt` | Plot analytics read-model |
| `data/repository/ScoutingRepository.kt` | Scouting data repository interface |
| `data/repository/ScoutingRepositoryImpl.kt` | Scouting repository implementation |
| `security/HardwareKeyManager.kt` | Android Keystore AES-256 key management |
| `security/BackupManager.kt` | Encrypted SD card backup |
| `ml/OnnxModelManager.kt` | ONNX Runtime model loading + NPU delegate |
| `ml/PestClassifier.kt` | Pest detection wrapper |
| `ml/InferencePipeline.kt` | Multi-stage inference orchestrator |
| `ml/ModelManager.kt` | Centralized model asset management |
| `maarifa/PestKnowledgeMapper.kt` | Link detections to Maarifa protocols |
| `presentation/scouting/ScoutingCaptureScreen.kt` | Camera + detection UI |
| `presentation/scouting/ScoutingViewModel.kt` | Scouting state management |
| `presentation/map/PestHeatmapOverlay.kt` | OSMDroid heatmap overlay |
| `presentation/alerts/PestAlertGenerator.kt` | Critical pest alert system |
| `assets/models/pest_classifier.onnx` | Bundled pest detection model |

### Existing Files to Modify

| File Path | Changes |
|-----------|---------|
| `security/KeystoreManager.kt` | Refactor to use random AES-256 key in Android Keystore |
| `data/sync/SyncWorker.kt` | Implement watermark-based delta sync |
| `data/sync/SyncManager.kt` | Add revision tracking, conflict resolution |
| `data/local/ShambaDatabase.kt` | Add ScoutingReport entity, bump version, add views |
| `data/local/entity/*.kt` (all 28 entities) | Add revision_id, last_modified_by, last_updated fields |
| `data/local/dao/*.kt` (all 24 DAOs) | Update queries for revision fields |
| `di/DatabaseModule.kt` | Add ScoutingReportDao, views |
| `di/RepositoryModule.kt` | Add ScoutingRepository binding |
| `di/SyncModule.kt` | Update sync configuration |
| `presentation/dashboard/DashboardViewModel.kt` | Use DashboardView |
| `presentation/dashboard/DashboardScreen.kt` | Display pest alerts |
| `presentation/crops/CropsViewModel.kt` | Use PlotAnalyticsView |
| `map/FarmMapScreen.kt` | Add heatmap toggle |
| `map/FarmMapViewModel.kt` | Integrate heatmap data |
| `presentation/alerts/AlertsScreen.kt` | Display pest alerts |
| `presentation/alerts/AlertsViewModel.kt` | Include pest alert types |
| `app/build.gradle.kts` | Add ONNX Runtime dependency |

### Files to Delete or Move

None.

### Configuration Updates

| File | Changes |
|------|---------|
| `app/build.gradle.kts` | Add `onnxruntime-android` dependency, QNN delegate |
| `proguard-rules.pro` | Add ONNX Runtime keep rules |

## Functions

### New Functions

| Function | File | Signature | Purpose |
|----------|------|-----------|---------|
| `generateHardwareKey()` | `HardwareKeyManager.kt` | `fun generateHardwareKey(alias: String): SecretKey` | Generate AES-256 key in Android Keystore |
| `createEncryptedBackup()` | `BackupManager.kt` | `suspend fun createEncryptedBackup(): File` | Create encrypted daily backup to SD card |
| `restoreFromBackup()` | `BackupManager.kt` | `suspend fun restoreFromBackup(file: File): Boolean` | Restore database from backup |
| `loadModel()` | `OnnxModelManager.kt` | `fun loadModel(modelName: String, useNpu: Boolean): OrtSession` | Load ONNX model with optional NPU delegate |
| `detectPests()` | `PestClassifier.kt` | `suspend fun detectPests(bitmap: Bitmap): List<Detection>` | Run pest detection on image |
| `classifySeverity()` | `InferencePipeline.kt` | `fun classifySeverity(detection: Detection, leafArea: Float): SeverityLevel` | Classify pest severity |
| `runInferencePipeline()` | `InferencePipeline.kt` | `suspend fun runInferencePipeline(bitmap: Bitmap, gpsLocation: LatLng): ScoutingReport?` | Full inference pipeline |
| `mapToMaarifaProtocol()` | `PestKnowledgeMapper.kt` | `fun mapToMaarifaProtocol(pestType: PestType): KnowledgeChunk?` | Link pest to management protocol |
| `generatePestHeatmap()` | `PestHeatmapOverlay.kt` | `fun generateHeatmap(reports: List<ScoutingReport>): List<HeatmapPoint>` | Generate heatmap points |
| `checkCriticalAlerts()` | `PestAlertGenerator.kt` | `suspend fun checkCriticalAlerts(): List<PestAlert>` | Check for critical pest detections |

### Modified Functions

| Function | File | Changes |
|----------|------|---------|
| `getDatabase()` | `DatabaseModule.kt` | Use hardware-backed key for SQLCipher |
| `doWork()` | `SyncWorker.kt` | Implement watermark strategy, sync only `last_updated > local_max_timestamp` |
| `syncEntity()` | `SyncManager.kt` | Add revision conflict resolution |
| `getDashboardData()` | `DashboardViewModel.kt` | Query DashboardView instead of multiple DAOs |

### Removed Functions

None.

## Classes

### New Classes

| Class | File | Inheritance | Purpose |
|-------|------|-------------|---------|
| `HardwareKeyManager` | `security/HardwareKeyManager.kt` | None | Android Keystore key operations |
| `BackupManager` | `security/BackupManager.kt` | None | Encrypted backup/restore |
| `OnnxModelManager` | `ml/OnnxModelManager.kt` | None | ONNX Runtime session management |
| `PestClassifier` | `ml/PestClassifier.kt` | None | Pest detection inference |
| `InferencePipeline` | `ml/InferencePipeline.kt` | None | Multi-stage ML pipeline |
| `ModelManager` | `ml/ModelManager.kt` | None | Model asset lifecycle |
| `PestKnowledgeMapper` | `maarifa/PestKnowledgeMapper.kt` | None | Pest-to-protocol mapping |
| `ScoutingCaptureScreen` | `presentation/scouting/ScoutingCaptureScreen.kt` | None | Compose UI for scouting |
| `ScoutingViewModel` | `presentation/scouting/ScoutingViewModel.kt` | ViewModel | Scouting state management |
| `PestHeatmapOverlay` | `presentation/map/PestHeatmapOverlay.kt` | Overlay | OSMDroid heatmap |
| `PestAlertGenerator` | `presentation/alerts/PestAlertGenerator.kt` | None | Alert generation |

### Modified Classes

| Class | File | Changes |
|-------|------|---------|
| `KeystoreManager` | `security/KeystoreManager.kt` | Refactor to generate random AES-256 keys |
| `SyncWorker` | `data/sync/SyncWorker.kt` | Implement watermark delta sync |
| `SyncManager` | `data/sync/SyncManager.kt` | Add revision tracking |
| `ShambaDatabase` | `data/local/ShambaDatabase.kt` | Add ScoutingReport, views, bump version |
| `DashboardViewModel` | `presentation/dashboard/DashboardViewModel.kt` | Use DashboardView |
| `FarmMapScreen` | `map/FarmMapScreen.kt` | Add heatmap toggle |
| `FarmMapViewModel` | `map/FarmMapViewModel.kt` | Integrate heatmap data |

### Removed Classes

None.

## Dependencies

### New Dependencies

```kotlin
// ONNX Runtime with NPU support
implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")

// Android Keystore enhancements
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

### Existing Dependencies (No Changes)

All existing dependencies remain unchanged. ONNX Runtime will use the existing CameraX for frame capture and OSMDroid for heatmap visualization.

## Testing

### Unit Tests

| Test File | Tests |
|-----------|-------|
| `HardwareKeyManagerTest.kt` | Key generation, encryption/decryption |
| `BackupManagerTest.kt` | Backup creation, restoration |
| `PestClassifierTest.kt` | Detection accuracy, severity classification |
| `InferencePipelineTest.kt` | Pipeline stages, error handling |
| `ScoutingReportDaoTest.kt` | CRUD operations, analytics queries |
| `SyncWorkerTest.kt` | Watermark sync, conflict resolution |

### Integration Tests

| Test File | Tests |
|-----------|-------|
| `DeltaSyncIntegrationTest.kt` | Full sync flow with revision tracking |
| `ScoutingFlowTest.kt` | Camera → Detection → Database → Heatmap |
| `MaarifaPestIntegrationTest.kt` | Pest detection → Protocol surfacing |

### Performance Tests

| Test | Target |
|------|--------|
| Pest inference latency | <50ms on NPU |
| Heatmap rendering | <100ms for 1000 markers |
| Sync performance | <5s for 1000 delta rows |

### Field Testing

- Test with real pest images (FAW, aphids, blight) captured on Xiaomi Pad 7
- Validate GPS accuracy for plot detection
- Verify heatmap visualization across 16-acre farm

## Implementation Order

### Step 1: Infrastructure Foundation (Sprint 1)
1. Refactor KeystoreManager for hardware-backed keys
2. Add revision fields to all 28 entities
3. Update all 24 DAOs for revision queries
4. Set up ONNX Runtime with NPU delegate
5. Enhance CameraX for ML inference

### Step 2: Security & Sync (Sprint 2)
1. Implement encrypted backup system
2. Refactor SyncWorker for watermark delta sync
3. Create DashboardView and PlotAnalyticsView
4. Update ViewModels to use database views
5. Set up ModelManager for ONNX models

### Step 3: Pest Detection Core (Sprint 3)
1. Download and bundle YOLOv8 pest detection model
2. Implement PestClassifier wrapper
3. Build multi-stage InferencePipeline
4. Create ScoutingReport entity and DAO
5. Implement severity classification

### Step 4: Scouting UI & Mapping (Sprint 4)
1. Build ScoutingCaptureScreen with camera
2. Implement GPS-based plot detection
3. Create vision-to-database mapping flow
4. Build PestHeatmapOverlay for OSMDroid
5. Integrate heatmap into FarmMapScreen

### Step 5: Alerts & Maarifa Integration (Sprint 5)
1. Implement PestAlertGenerator
2. Build PestKnowledgeMapper for Maarifa
3. Update AlertsScreen for pest alerts
4. Add "Authorize Treatment" action
5. Surface management protocols on detection

### Step 6: Polish & Optimization (Sprint 6)
1. Optimize inference pipeline for <50ms
2. Implement marker clustering on heatmap
3. Add time/pest-type filtering
4. Performance profiling and optimization
5. UI polish and accessibility

### Step 7: Testing & Validation (Sprint 7)
1. Unit tests for all new components
2. Integration tests for sync and scouting flows
3. Performance benchmarks on Xiaomi Pad 7
4. Field testing with real pest images
5. Bug fixes and final hardening