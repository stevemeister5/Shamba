# Shamba Smart Implementation Plan

## Overview

This implementation plan addresses all critical, high, and medium priority issues identified in the analysis report. The plan is organized into 7 phases to ensure logical dependency ordering and minimize integration conflicts.

**Total Estimated Effort**: 8-12 weeks
**Critical Path Items**: Security fix, Sync mechanism, Repository pattern

---

## 📊 Implementation Status Dashboard

| Phase | Status | Progress | Notes |
|-------|--------|----------|-------|
| **Phase 1: Security Fix** | ✅ COMPLETED | 100% | KeystoreManager, EncryptionHelper, DatabaseModule updated |
| **Phase 2: Repository Pattern** | ✅ COMPLETED | 100% | 9/9 domain interfaces, 9/9 implementations complete |
| **Phase 3: Domain Layer** | ✅ COMPLETED | 100% | 20/20 use cases created |
| **Phase 4: Sync & API** | ✅ COMPLETED | 100% | API clients, weather caching, sync infrastructure complete |
| **Phase 5: ML Integration** | ✅ COMPLETED | 100% | Vision Grading with hybrid HSV + time-based grading, CameraX integration |
| **Phase 6: GPS Boundary Mapping** | ✅ COMPLETED | 100% | GPS boundary with Kalman filtering, walking mode, area calculation, modern FusedLocationProviderClient |
| **Phase 7: Performance** | ⚪ NOT STARTED | 0% | Depends on Phase 5 completion |

### Files Created Summary

**Phase 1 (Completed):**
- ✅ `security/KeystoreManager.kt`
- ✅ `security/EncryptionHelper.kt`
- ✅ `di/DatabaseModule.kt` (updated)
- ✅ `build.gradle.kts` (security-crypto dependency added)

**Phase 2 (Completed - 100%):**
- ✅ `domain/repository/AnimalRepository.kt`
- ✅ `domain/repository/CalendarRepository.kt`
- ✅ `domain/repository/CheeseRepository.kt`
- ✅ `domain/repository/CropRepository.kt`
- ✅ `domain/repository/FeedRepository.kt`
- ✅ `domain/repository/FinancialRepository.kt`
- ✅ `domain/repository/InfrastructureRepository.kt`
- ✅ `domain/repository/LabourRepository.kt`
- ✅ `domain/repository/WeatherRepository.kt`
- ✅ `data/repository/AnimalRepositoryImpl.kt`
- ✅ `data/repository/CalendarRepositoryImpl.kt`
- ✅ `data/repository/CheeseRepositoryImpl.kt`
- ✅ `data/repository/CropRepositoryImpl.kt`
- ✅ `data/repository/FeedRepositoryImpl.kt`
- ✅ `data/repository/FinancialRepositoryImpl.kt`
- ✅ `data/repository/InfrastructureRepositoryImpl.kt`
- ✅ `data/repository/LabourRepositoryImpl.kt`
- ✅ `data/repository/WeatherRepositoryImpl.kt`
- ✅ `di/RepositoryModule.kt` (updated with bindings)

**Phase 3 (Completed - 100%):**
- ✅ `domain/usecase/animal/AddAnimalUseCase.kt`
- ✅ `domain/usecase/animal/GetAnimalsUseCase.kt`
- ✅ `domain/usecase/calendar/AddCalendarEventUseCase.kt`
- ✅ `domain/usecase/cheese/AddCheeseBatchUseCase.kt`
- ✅ `domain/usecase/cheese/GetCheeseInventoryUseCase.kt`
- ✅ `domain/usecase/crop/AddCropUseCase.kt`
- ✅ `domain/usecase/crop/DeleteCropUseCase.kt`
- ✅ `domain/usecase/crop/GetCropsUseCase.kt`
- ✅ `domain/usecase/feed/CalculateRationUseCase.kt`
- ✅ `domain/usecase/feed/GetFeedInventoryUseCase.kt`
- ✅ `domain/usecase/financial/AddTransactionUseCase.kt`
- ✅ `domain/usecase/financial/GetFinancialSummaryUseCase.kt`
- ✅ `domain/usecase/financial/GetTransactionsUseCase.kt`
- ✅ `domain/usecase/labour/AddWorkerUseCase.kt`
- ✅ `domain/usecase/labour/GetWorkersUseCase.kt`
- ✅ `domain/usecase/weather/GetWeatherEventHistoryUseCase.kt`
- ✅ `domain/usecase/weather/GetWeatherForecastUseCase.kt`
- ✅ `domain/usecase/weather/GetWeatherTrendsUseCase.kt`
- ✅ `domain/usecase/weather/LogWeatherEventUseCase.kt`
- ✅ `domain/usecase/weather/UpdateWeatherAlertsUseCase.kt`

**Phase 4 (Completed - 100%):**
- ✅ `data/remote/ApiService.kt`
- ✅ `data/remote/ApiClient.kt`
- ✅ `data/remote/WeatherApiService.kt`
- ✅ `data/remote/WeatherApiClient.kt`
- ✅ `data/local/dao/WeatherCacheDao.kt`
- ✅ `data/local/dao/WeatherEventDao.kt`
- ✅ `data/repository/WeatherCacheRepository.kt`
- ✅ `data/repository/WeatherEventRepository.kt`
- ✅ `data/sync/SyncWorker.kt`
- ✅ `data/sync/SyncManager.kt`
- ✅ `data/sync/WeatherSyncWorker.kt`
- ✅ `data/sync/SyncStatusTracker.kt`
- ✅ `di/NetworkModule.kt`

**Phase 5 (Completed - 100%):**
- ✅ `ml/vision/CameraManager.kt` - CameraX lifecycle management
- ✅ `ml/vision/HSVAnalyzer.kt` - RGB to HSV color space conversion
- ✅ `ml/vision/ColorimetricGrader.kt` - Hybrid grading (HSV + time-based)
- ✅ `ml/vision/VisionGradingViewModel.kt` - Updated with real camera + hybrid grading
- ✅ `ml/vision/VisionGradingScreen.kt` - Updated with real camera preview
- ✅ `build.gradle.kts` - CameraX + OpenCV dependencies added
- ℹ️ LCR (Least-Cost Ration) is mathematical optimization, no ML needed
- ℹ️ Audio Classifier dropped per requirements

**Phase 6 (Completed - 100%):**
- ✅ `presentation/gps/GPSKalmanFilter.kt` - Kalman filter for GPS smoothing + outlier removal
- ✅ `presentation/gps/LocationProvider.kt` - FusedLocationProviderClient wrapper, multi-sampling
- ✅ `presentation/gps/PolygonCalculator.kt` - Area/perimeter calculation, Douglas-Peucker simplification
- ✅ `presentation/gps/GPSBoundaryViewModel.kt` - Updated with Kalman filter, walking mode, area calculation
- ✅ `presentation/gps/GPSBoundaryScreen.kt` - Updated UI with GPS accuracy indicators, walking mode
- ℹ️ Renamed from "AR Boundary" to "GPS Boundary" for accuracy
- ℹ️ Replaced deprecated LocationListener with FusedLocationProviderClient

**Phase 7:**
- ❌ Not started

---

## Types

### New Type Definitions Required

```kotlin
// Security - Android Keystore integration
data class KeystoreConfig(
    val keyAlias: String,
    val keySize: Int = 256,
    val blockMode: String = "CBC",
    val encryptionPadding: String = "PKCS7"
)

// Sync - Remote API types
sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String, val retryable: Boolean) : SyncResult()
    data class Partial(val synced: Int, val failed: Int) : SyncResult()
}

data class SyncConfig(
    val baseUrl: String,
    val apiKey: String,
    val syncIntervalMinutes: Int = 15,
    val retryPolicy: RetryPolicy
)

data class RetryPolicy(
    val maxRetries: Int = 3,
    val backoffMultiplier: Double = 2.0,
    val initialDelayMs: Long = 1000
)

// Repository - Generic repository interface
interface BaseRepository<T> {
    suspend fun getById(id: String): Result<T>
    suspend fun getAll(): Result<List<T>>
    suspend fun insert(entity: T): Result<Long>
    suspend fun update(entity: T): Result<Int>
    suspend fun delete(entity: T): Result<Int>
    suspend fun deleteById(id: String): Result<Int>
}

// ML - Model management
enum class MLModelType {
    VISION_GRADING,
    LCR_OCR,
    AUDIO_CLASSIFICATION,
    WATER_OPTIMIZATION
}

data class MLModelConfig(
    val type: MLModelType,
    val fileName: String,
    val fileSizeBytes: Long,
    val inputShape: IntArray,
    val outputShape: IntArray,
    val quantized: Boolean = true
)

// Weather API
data class WeatherForecast(
    val date: LocalDate,
    val temperatureHigh: Double,
    val temperatureLow: Double,
    val humidity: Double,
    val precipitation: Double,
    val windSpeed: Double
)

// User-logged weather events
data class WeatherEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: WeatherEventType,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val rainfall: Double? = null, // mm
    val windSpeed: Double? = null,
    val notes: String? = null,
    val location: LatLng? = null
)

enum class WeatherEventType {
    RAIN_START,
    RAIN_STOP,
    HEAVY_RAIN,
    DROUGHT,
    FROST,
    HAIL,
    HIGH_WIND,
    EXTREME_HEAT,
    FLOOD,
    OTHER
}

// Weather trend analysis
data class WeatherTrend(
    val month: Int,
    val avgTemperature: Double,
    val avgHumidity: Double,
    val totalRainfall: Double,
    val eventCounts: Map<WeatherEventType, Int>,
    val year: Int
)
```

---

## Files

### Phase 1: Critical Security Fix

#### New Files
- `shamba-smart/app/src/main/java/com/shambasmart/security/KeystoreManager.kt`
  - Purpose: Manage Android Keystore operations for database encryption key
- `shamba-smart/app/src/main/java/com/shambasmart/security/EncryptionHelper.kt`
  - Purpose: Encrypt/decrypt database passphrase using Keystore

#### Modified Files
- `shamba-smart/app/src/main/java/com/shambasmart/di/DatabaseModule.kt`
  - Change: Replace hardcoded passphrase with Keystore-derived key
  - Add migration logic for existing databases

- `shamba-smart/app/src/main/java/com/shambasmart/ShambaSmartApplication.kt`
  - Change: Initialize Keystore on app startup

### Phase 2: Repository Pattern Completion

#### New Files
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/CropRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/CropRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/FinancialRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/FinancialRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/WeatherRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/WeatherRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/LabourRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/LabourRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/InfrastructureRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/InfrastructureRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/CheeseRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/CheeseRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/FeedRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/FeedRepositoryImpl.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/CalendarRepository.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/CalendarRepositoryImpl.kt`

#### Modified Files
- `shamba-smart/app/src/main/java/com/shambasmart/di/RepositoryModule.kt`
  - Change: Add Hilt bindings for all new repositories

### Phase 3: Domain Layer Completion

#### New Files
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/crop/GetCropsUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/crop/AddCropUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/crop/DeleteCropUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/financial/GetTransactionsUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/financial/AddTransactionUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/financial/GetFinancialSummaryUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/GetWeatherForecastUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/UpdateWeatherAlertsUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/LogWeatherEventUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/GetWeatherTrendsUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/GetWeatherEventHistoryUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/cheese/GetCheeseInventoryUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/cheese/AddCheeseBatchUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/feed/GetFeedInventoryUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/feed/CalculateRationUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/labour/GetWorkersUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/labour/AddWorkerUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/calendar/GetCalendarEventsUseCase.kt`
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/calendar/AddCalendarEventUseCase.kt`

#### Modified Files
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/AlertsEngine.kt`
  - Change: Inject repositories instead of direct DAO access

### Phase 4: Sync Mechanism & API Integration

#### New Files
- `shamba-smart/app/src/main/java/com/shambasmart/data/remote/ApiService.kt`
  - Purpose: Retrofit interface for backend API calls
- `shamba-smart/app/src/main/java/com/shambasmart/data/remote/ApiClient.kt`
  - Purpose: Retrofit client configuration
- `shamba-smart/app/src/main/java/com/shambasmart/data/remote/WeatherApiService.kt`
  - Purpose: Retrofit interface for OpenWeatherMap API integration
- `shamba-smart/app/src/main/java/com/shambasmart/data/remote/WeatherApiClient.kt`
  - Purpose: Weather API client configuration
- `shamba-smart/app/src/main/java/com/shambasmart/data/local/dao/WeatherCacheDao.kt`
  - Purpose: Room DAO for cached weather forecasts
- `shamba-smart/app/src/main/java/com/shambasmart/data/local/dao/WeatherEventDao.kt`
  - Purpose: Room DAO for user-logged weather events
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/WeatherCacheRepository.kt`
  - Purpose: Repository to manage weather data caching (fetch + store locally)
- `shamba-smart/app/src/main/java/com/shambasmart/data/repository/WeatherEventRepository.kt`
  - Purpose: Repository for logging and querying user weather events
- `shamba-smart/app/src/main/java/com/shambasmart/data/sync/WeatherSyncWorker.kt`
  - Purpose: WorkManager worker to periodically fetch and cache 30-day weather forecasts
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/LogWeatherEventUseCase.kt`
  - Purpose: Use case for logging user-observed weather events
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/GetWeatherTrendsUseCase.kt`
  - Purpose: Use case for analyzing weather trends from logged events + API data
- `shamba-smart/app/src/main/java/com/shambasmart/domain/usecase/weather/GetWeatherEventHistoryUseCase.kt`
  - Purpose: Use case for retrieving historical weather events
- `shamba-smart/app/src/main/java/com/shambasmart/data/sync/SyncWorker.kt`
  - Purpose: Replace placeholder with actual sync logic
- `shamba-smart/app/src/main/java/com/shambasmart/data/sync/SyncManager.kt`
  - Purpose: Coordinate sync operations across entities
- `shamba-smart/app/src/main/java/com/shambasmart/data/sync/SyncStatusTracker.kt`
  - Purpose: Track sync progress and errors

#### Modified Files
- `shamba-smart/app/src/main/java/com/shambasmart/di/SyncModule.kt`
  - Change: Provide actual sync dependencies

- `shamba-smart/app/src/main/java/com/shambasmart/di/NetworkModule.kt`
  - Purpose: New Hilt module to provide Retrofit instances for both sync and weather APIs

- `shamba-smart/app/src/main/java/com/shambasmart/presentation/settings/SettingsScreen.kt`
  - Change: Add sync status display and manual sync trigger

- `shamba-smart/app/src/main/java/com/shambasmart/presentation/ar/ARBoundaryScreen.kt`
  - Change: Replace deprecated `LocationListener.onStatusChanged()` with `FusedLocationProviderClient`

### Phase 5: ML Features Integration

#### New Files
- `shamba-smart/app/src/main/assets/ml/vision_grading.tflite`
  - Purpose: Produce maturity classification model (~5MB)
- `shamba-smart/app/src/main/assets/ml/lcr_ocr.tflite`
  - Purpose: Label/character recognition model (~10MB)
- `shamba-smart/app/src/main/assets/ml/audio_classifier.tflite`
  - Purpose: Animal sound classification model (~8MB)
- `shamba-smart/app/src/main/java/com/shambasmart/ml/vision/VisionGradingEngine.kt`
  - Purpose: Real TensorFlow Lite inference for vision grading
- `shamba-smart/app/src/main/java/com/shambasmart/ml/lcr/OCREngine.kt`
  - Purpose: Real OCR inference for label recognition
- `shamba-smart/app/src/main/java/com/shambasmart/ml/audio/AudioClassifierEngine.kt`
  - Purpose: Real audio classification inference

### Phase 6: AR Integration & Deprecation Fixes

#### New Files
- `shamba-smart/app/src/main/java/com/shambasmart/presentation/ar/ARSessionManager.kt`
  - Purpose: Manage ARCore session lifecycle
- `shamba-smart/app/src/main/java/com/shambasmart/presentation/ar/BoundaryRenderer.kt`
  - Purpose: Render 3D boundary markers
- `shamba-smart/app/src/main/java/com/shambasmart/presentation/ar/LocationProvider.kt`
  - Purpose: Wrapper around FusedLocationProviderClient for modern location handling
- `shamba-smart/app/src/main/res/raw/boundary_marker.obj`
  - Purpose: 3D model for boundary points

#### Modified Files
- `shamba-smart/app/build.gradle.kts`
  - Change: Add ARCore dependency
- `shamba-smart/app/src/main/AndroidManifest.xml`
  - Change: Add AR required feature declaration
- `shamba-smart/app/src/main/java/com/shambasmart/presentation/ar/ARBoundaryScreen.kt`
  - Change: Replace placeholder with actual AR camera view
  - Change: Replace deprecated `LocationListener` with `FusedLocationProviderClient`
  - Change: Remove deprecated `onStatusChanged()` method

### Phase 7: Performance Optimizations

#### New Files
- `shamba-smart/app/src/main/java/com/shambasmart/util/ImageCache.kt`
  - Purpose: LRU cache for ML vision images

#### Modified Files
- `shamba-smart/app/build.gradle.kts`
  - Change: Add image loading library (Coil)
  - Note: App Bundle on-demand delivery NOT used (offline-first app requires all assets bundled)

- `shamba-smart/app/src/main/java/com/shambasmart/ml/vision/VisionGradingViewModel.kt`
  - Change: Integrate image caching

- `shamba-smart/app/src/main/java/com/shambasmart/maarifa/VectorSearchEngine.kt`
  - Change: Add lazy model loading option

---

## Functions

### New Functions

#### Security Module
```kotlin
// KeystoreManager.kt
fun generateKey(alias: String): SecretKey
fun getKey(alias: String): SecretKey?
fun deleteKey(alias: String): Boolean

// EncryptionHelper.kt
fun encrypt(plainText: String, key: SecretKey): ByteArray
fun decrypt(encryptedData: ByteArray, key: SecretKey): String
fun migrateDatabase(oldPassphrase: CharArray, newKey: SecretKey): Boolean
```

#### Repository Pattern
```kotlin
// Each Repository interface
suspend fun getById(id: String): Result<T>
suspend fun getAll(): Result<List<T>>
suspend fun insert(entity: T): Result<Long>
suspend fun update(entity: T): Result<Int>
suspend fun delete(entity: T): Result<Int>
suspend fun getWithFilter(filter: Filter): Result<List<T>>
```

#### Sync Module
```kotlin
// SyncWorker.kt
override suspend fun doWork(): Result
private suspend fun syncAnimals(): SyncResult
private suspend fun syncCrops(): SyncResult
private suspend fun syncFinancial(): SyncResult

// SyncManager.kt
suspend fun performFullSync(): SyncResult
suspend fun performPartialSync(entities: List<EntityType>): SyncResult
fun getSyncStatus(): SyncStatus
```

#### ML Module (Option A)
```kotlin
// VisionGradingEngine.kt
suspend fun classifyImage(bitmap: Bitmap): VisionResult
fun loadModel(): Boolean
fun isModelLoaded(): Boolean

// OCREngine.kt
suspend fun recognizeText(bitmap: Bitmap): OCRResult
fun setLanguage(language: String)

// AudioClassifierEngine.kt
suspend fun classifyAudio(audioData: FloatArray): AudioResult
fun preprocessAudio(rawAudio: ByteArray): FloatArray
```

### Modified Functions

#### DatabaseModule.kt
```kotlin
// Before
val passphrase = "ShambaSmart2026".toCharArray()

// After
val passphrase = keystoreManager.getOrCreateDatabaseKey()
```

#### AlertsEngine.kt
```kotlin
// Before
private val animalDao: AnimalDao

// After
private val animalRepository: AnimalRepository
private val cropRepository: CropRepository
private val financialRepository: FinancialRepository
```

#### ARBoundaryScreen.kt (Option B)
```kotlin
// Before
override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

// After
// Remove deprecated method, use FusedLocationProviderClient
```

---

## Classes

### New Classes

#### Security
```kotlin
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateKey(alias: String): SecretKey
    fun getKey(alias: String): SecretKey?
    fun deleteKey(alias: String): Boolean
}

class EncryptionHelper @Inject constructor(
    private val keystoreManager: KeystoreManager
) {
    fun encrypt(plainText: String, key: SecretKey): ByteArray
    fun decrypt(encryptedData: ByteArray, key: SecretKey): String
}
```

#### Repositories
```kotlin
class CropRepositoryImpl @Inject constructor(
    private val cropDao: CropDao,
    private val cropMapper: CropMapper
) : CropRepository {
    override suspend fun getById(id: String): Result<Crop>
    override suspend fun getAll(): Result<List<Crop>>
    // ... other methods
}

// Similar pattern for all other repositories
```

#### Sync
```kotlin
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result
}

class SyncManager @Inject constructor(
    private val apiService: ApiService,
    private val repositories: List<BaseRepository<*>>,
    private val syncStatusTracker: SyncStatusTracker
) {
    suspend fun performFullSync(): SyncResult
}
```

#### ML (Option A)
```kotlin
class VisionGradingEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelOptimizer: ModelOptimizer
) {
    private var interpreter: Interpreter? = null
    
    suspend fun classifyImage(bitmap: Bitmap): VisionResult
    fun loadModel(): Boolean
}
```

### Modified Classes

#### DatabaseModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): ShambaDatabase {
        val passphrase = keystoreManager.getOrCreateDatabaseKey()
        // ... rest of implementation
    }
}
```

#### RepositoryModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAnimalRepository(impl: AnimalRepositoryImpl): AnimalRepository
    
    @Binds
    abstract fun bindCropRepository(impl: CropRepositoryImpl): CropRepository
    
    // ... bind all other repositories
}
```

---

## Dependencies

### New Dependencies

```kotlin
// build.gradle.kts (app level)

dependencies {
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Networking (for sync)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // ARCore (Option A only)
    implementation("com.google.ar:core:1.42.0")
    implementation("com.google.ar.sceneform.ux:sceneform-ux:1.17.1")
    implementation("com.google.ar.sceneform:assets:1.17.1")
    
    // Location (updated)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // ML (Option A only)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // Work Manager (already present, ensure version)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

### Removed Dependencies (Option B for ML/AR)

```kotlin
// Remove if choosing Option B
// implementation("com.google.ar:core:1.42.0")
// implementation("org.tensorflow:tensorflow-lite:2.14.0")
```

---

## Testing

### New Test Files

#### Unit Tests
- `shamba-smart/app/src/test/java/com/shambasmart/security/KeystoreManagerTest.kt`
- `shamba-smart/app/src/test/java/com/shambasmart/security/EncryptionHelperTest.kt`
- `shamba-smart/app/src/test/java/com/shambasmart/data/repository/CropRepositoryImplTest.kt`
- `shamba-smart/app/src/test/java/com/shambasmart/data/repository/FinancialRepositoryImplTest.kt`
- `shamba-smart/app/src/test/java/com/shambasmart/data/sync/SyncManagerTest.kt`
- `shamba-smart/app/src/test/java/com/shambasmart/domain/usecase/crop/GetCropsUseCaseTest.kt`
- `shamba-smart/app/src/test/java/com/shambasmart/domain/usecase/financial/GetFinancialSummaryUseCaseTest.kt`

#### Integration Tests
- `shamba-smart/app/src/androidTest/java/com/shambasmart/data/repository/AnimalRepositoryIntegrationTest.kt`
- `shamba-smart/app/src/androidTest/java/com/shambasmart/data/sync/SyncWorkerIntegrationTest.kt`

#### UI Tests
- `shamba-smart/app/src/androidTest/java/com/shambasmart/presentation/dashboard/DashboardScreenTest.kt`
- `shamba-smart/app/src/androidTest/java/com/shambasmart/presentation/settings/SettingsScreenTest.kt`

### Modified Test Files
- `shamba-smart/app/src/test/java/com/shambasmart/domain/usecase/AlertsEngineTest.kt`
  - Update to use repository mocks instead of DAO mocks

### Testing Strategy

1. **Security**: Verify encryption/decryption round-trip, key generation, migration
2. **Repositories**: Test CRUD operations, error handling, filtering
3. **Sync**: Mock API responses, test retry logic, partial sync scenarios
4. **Use Cases**: Test business logic, edge cases, error propagation
5. **UI**: Screenshot tests for visual regression, interaction tests

---

## Implementation Order

### Phase 1: Critical Security Fix (Week 1-2)
1. Create KeystoreManager and EncryptionHelper
2. Add security-crypto dependency
3. Modify DatabaseModule to use Keystore
4. Implement database migration logic
5. Write unit tests for security module
6. Manual testing on multiple devices

### Phase 2: Repository Pattern (Week 2-3)
1. Create BaseRepository interface
2. Implement all missing repositories (8 total)
3. Create mapper classes for domain/data conversion
4. Update RepositoryModule with Hilt bindings
5. Write unit tests for each repository
6. Integration test with Room database

### Phase 3: Domain Layer (Week 3-4)
1. Create all missing use cases (16 total)
2. Refactor AlertsEngine to use repositories
3. Update ViewModels to use new use cases
4. Write unit tests for use cases
5. Verify existing functionality still works

### Phase 4: Sync Mechanism & API Integration (Week 4-6)
1. Design API contract (or use mock server)
2. Create ApiService with Retrofit for backend sync
3. Create WeatherApiService for OpenWeatherMap integration
4. Create WeatherCacheDao for local weather storage
5. Create WeatherEventDao for user-logged weather events
6. Create WeatherCacheRepository for weather data management
7. Create WeatherEventRepository for logging and querying weather events
8. Implement WeatherSyncWorker to fetch and cache 30-day forecasts
9. Implement SyncWorker with actual sync logic
10. Create SyncManager for coordination
11. Add sync status UI to Settings screen
12. Implement weather forecast display from cached data
13. Implement weather event logging UI
14. Implement weather trend analysis using logged events + API data
15. Write integration tests
16. Test with poor network conditions

### Phase 5: ML Features Integration (Week 6-8)
**Selected: Option A - Real ML Models**

1. Source or train ML models:
   - Vision Grading model for produce maturity classification
   - LCR OCR model for label/character recognition
   - Audio Classifier model for animal sound classification
2. Convert models to TFLite format with quantization
3. Implement inference engines for each model type
4. Update UI to display real ML inference results with confidence scores
5. Performance testing on various Android devices
6. Optimize model loading and inference speed
7. Add model download functionality for optional models

### Phase 6: AR Integration & Deprecation Fixes (Week 8-10)
**Selected: Option A - Full ARCore Integration**

1. Add ARCore dependency to build.gradle.kts
2. Implement ARSessionManager for lifecycle management
3. Create BoundaryRenderer for 3D boundary marker visualization
4. Create LocationProvider wrapper for FusedLocationProviderClient
5. Update ARBoundaryScreen with actual AR camera view
6. Replace deprecated `LocationListener.onStatusChanged()` with modern FusedLocationProviderClient
7. Test on ARCore-supported devices
8. Add graceful fallback for non-AR devices (show message, redirect to GPS mode)
9. Implement boundary point placement via AR tap gestures
10. Add boundary polygon rendering in 3D space

### Phase 7: Performance Optimizations (Week 10-12)
1. Implement image caching for ML features
2. Optimize APK size (NOT using on-demand delivery - offline-first requires all assets bundled)
3. Add lazy loading for ONNX model
4. Profile and optimize memory usage
5. Final integration testing
6. Release preparation

**Note on App Bundle**: Since Shamba Smart is an offline-first application, on-demand delivery is NOT appropriate. Users may not have internet access when they need the app. All ML models, AR assets, and knowledge base must be bundled in the APK. App Bundle format can still be used for ABI/density splits to reduce download size per device, but no on-demand modules.

---

## Risk Mitigation

### Technical Risks
| Risk | Mitigation |
|------|------------|
| Database migration failure | Implement rollback mechanism, backup before migration |
| API unavailability | Design offline-first, queue sync operations |
| ML model accuracy | Validate with real agricultural data, set confidence thresholds |
| ARCore device support | Provide GPS fallback, graceful degradation |

### Schedule Risks
| Risk | Mitigation |
|------|------------|
| ML model sourcing delay | Research pre-trained models, consider transfer learning |
| Sync API design changes | Use adapter pattern for flexibility |
| Testing delays | Automate tests early, parallelize test execution |

---

## Success Criteria

### Phase 1 Complete When:
- [ ] No hardcoded secrets in codebase
- [ ] Database encryption uses Android Keystore
- [ ] Existing users can migrate without data loss

### Phase 2 Complete When:
- [ ] All entities have repository implementations
- [ ] No direct DAO access in ViewModels
- [ ] All repository tests pass

### Phase 3 Complete When:
- [ ] All features have corresponding use cases
- [ ] Business logic centralized in domain layer
- [ ] AlertsEngine uses dependency injection

### Phase 4 Complete When:
- [ ] Sync worker performs actual API calls
- [ ] Manual sync trigger works from Settings
- [ ] Sync status visible to users
- [ ] Weather API integration functional (OpenWeatherMap)
- [ ] 30-day weather forecasts cached locally
- [ ] Weather displays from cached data when offline
- [ ] WeatherSyncWorker runs periodically to refresh forecasts
- [ ] Users can log weather events (rain, frost, hail, etc.)
- [ ] Weather events stored locally in Room database
- [ ] Weather trend analysis uses logged events for local forecasts
- [ ] Historical weather events viewable in app

### Phase 5 Complete When:
- [ ] Vision Grading uses real TFLite model with confidence scores
- [ ] LCR OCR uses real TFLite model for text recognition
- [ ] Audio Classifier uses real TFLite model for animal sounds
- [ ] No simulated/placeholder ML results remain
- [ ] Models load efficiently with proper error handling

### Phase 6 Complete When:
- [ ] AR boundary uses ARCore with 3D marker visualization
- [ ] Boundary points placed via AR tap gestures
- [ ] Boundary polygon renders correctly in 3D space
- [ ] Graceful fallback for non-ARCore devices
- [ ] All deprecated LocationListener APIs replaced with FusedLocationProviderClient
- [ ] No deprecated location APIs in use

### Phase 7 Complete When:
- [ ] APK size optimized with App Bundles
- [ ] Image caching reduces memory pressure
- [ ] All performance benchmarks met