# Implementation Plan

## [Overview]

Create an onboarding flow that displays 3 welcome intro screens on first app launch before routing users to the existing Farm Setup Wizard. The onboarding screens will introduce Shamba Smart, showcase key app features, and request necessary permissions (camera, location, notifications). The onboarding state will be persisted using DataStore so it only shows once per device.

## [Types]

### Data Classes and State

```kotlin
// Onboarding state managed via DataStore
data class OnboardingState(
    val hasCompletedOnboarding: Boolean = false,
    val currentStep: Int = 0
)

// Onboarding screen definitions
enum class OnboardingStep(val index: Int) {
    WELCOME(0),
    FEATURES(1),
    PERMISSIONS(2)
}
```

### Navigation Route

```kotlin
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    // ... existing routes
}
```

## [Files]

### New Files to Create

1. **`shamba-smart/app/src/main/java/com/shambasmart/presentation/onboarding/OnboardingScreen.kt`**
   - Main onboarding composable with horizontal pager
   - Handles step navigation (next/skip)
   - Dot indicator for current step
   - "Get Started" button on final step

2. **`shamba-smart/app/src/main/java/com/shambasmart/presentation/onboarding/OnboardingViewModel.kt`**
   - Manages onboarding state
   - Persists completion status to DataStore
   - Handles step transitions

3. **`shamba-smart/app/src/main/java/com/shambasmart/presentation/onboarding/screens/WelcomeScreen.kt`**
   - Shamba Smart logo/branding
   - Tagline: "Smart Farming, Smarter Decisions"
   - Brief app introduction
   - Illustration or animated element

4. **`shamba-smart/app/src/main/java/com/shambasmart/presentation/onboarding/screens/FeaturesScreen.kt`**
   - Grid/list of key features:
     - Livestock Management
     - Crop Tracking
     - Cheese Production
     - AI-Powered Insights (Maarifa)
     - Farm Mapping
   - Each feature with icon + brief description

5. **`shamba-smart/app/src/main/java/com/shambasmart/presentation/onboarding/screens/PermissionsScreen.kt`**
   - Explanation of why permissions are needed
   - Individual permission request buttons:
     - Camera (for crop/pest scanning)
     - Location (for farm mapping)
     - Notifications (for alerts)
   - "Continue" button to proceed to Farm Setup

6. **`shamba-smart/app/src/main/java/com/shambasmart/data/local/preferences/OnboardingPreferences.kt`**
   - DataStore wrapper for onboarding state
   - `hasCompletedOnboarding: Flow<Boolean>`
   - `markOnboardingComplete()` suspend function

### Existing Files to Modify

1. **`shamba-smart/app/src/main/java/com/shambasmart/presentation/navigation/ShambaNavGraph.kt`**
   - Add `Screen.Onboarding` route
   - Update `startDestination` logic to check onboarding state
   - Add composable for OnboardingScreen

2. **`shamba-smart/app/src/main/java/com/shambasmart/MainActivity.kt`**
   - Inject OnboardingPreferences
   - Pass onboarding state to navigation graph

3. **`shamba-smart/app/src/main/java/com/shambasmart/di/RepositoryModule.kt`**
   - Add DataStore<Preferences> binding for onboarding

## [Functions]

### New Functions

1. **OnboardingScreen.kt**
   ```kotlin
   @Composable
   fun OnboardingScreen(
       onComplete: () -> Unit,
       viewModel: OnboardingViewModel = hiltViewModel()
   )
   ```

2. **OnboardingViewModel.kt**
   ```kotlin
   @HiltViewModel
   class OnboardingViewModel @Inject constructor(
       private val onboardingPreferences: OnboardingPreferences
   ) : ViewModel() {
       fun nextStep()
       fun previousStep()
       fun completeOnboarding()
   }
   ```

3. **OnboardingPreferences.kt**
   ```kotlin
   class OnboardingPreferences @Inject constructor(
       private val dataStore: DataStore<Preferences>
   ) {
       val hasCompletedOnboarding: Flow<Boolean>
       suspend fun markOnboardingComplete()
   }
   ```

### Modified Functions

1. **ShambaNavGraph.kt**
   - Modify `ShambaNavGraph` composable to accept `hasCompletedOnboarding` parameter
   - Update startDestination logic

## [Classes]

### New Classes

1. **OnboardingViewModel** - HiltViewModel managing onboarding state
2. **OnboardingPreferences** - DataStore wrapper for persistence

### Modified Classes

1. **MainActivity** - Add DataStore injection and state observation

## [Dependencies]

No new dependencies required. The implementation uses existing libraries:
- `androidx.datastore:datastore-preferences:1.0.0` (already in build.gradle.kts)
- `androidx.compose.animation:animation` (already in build.gradle.kts)
- `androidx.navigation:navigation-compose:2.7.6` (already in build.gradle.kts)
- `com.google.dagger:hilt-android:2.50` (already in build.gradle.kts)

## [Testing]

### Unit Tests

1. **OnboardingViewModelTest.kt**
   - Test step navigation logic
   - Test onboarding completion triggers DataStore update

2. **OnboardingPreferencesTest.kt**
   - Test DataStore read/write operations
   - Test default state (hasCompletedOnboarding = false)

### Manual Testing Checklist

- [ ] Onboarding shows on fresh install
- [ ] Onboarding does NOT show on subsequent launches
- [ ] Next/Previous buttons navigate correctly
- [ ] Dot indicator updates with step
- [ ] Permission requests work on Permissions screen
- [ ] "Get Started" completes onboarding and navigates to FarmSetup
- [ ] App crashes-free during onboarding flow

## [Implementation Order]

1. **Step 1**: Create `OnboardingPreferences.kt` - DataStore wrapper for persisting onboarding completion state

2. **Step 2**: Create `OnboardingViewModel.kt` - ViewModel managing step state and completion logic

3. **Step 3**: Create `WelcomeScreen.kt` - First onboarding screen with branding

4. **Step 4**: Create `FeaturesScreen.kt` - Second onboarding screen showcasing app features

5. **Step 5**: Create `PermissionsScreen.kt` - Third onboarding screen for permission requests

6. **Step 6**: Create `OnboardingScreen.kt` - Main composable combining all 3 screens with pager navigation

7. **Step 7**: Update `ShambaNavGraph.kt` - Add onboarding route and conditional start destination

8. **Step 8**: Update `MainActivity.kt` - Inject preferences and pass state to navigation

9. **Step 9**: Update DI modules - Ensure DataStore is properly provided for onboarding

10. **Step 10**: Test and verify - Run app, verify onboarding shows on first launch only