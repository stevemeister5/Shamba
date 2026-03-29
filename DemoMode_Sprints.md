# Demo Mode Implementation — Sprint Breakdown

## Overview
Implement a Demo Mode for Shamba Smart that allows anyone to explore the full application with realistic, pre-populated farm data without going through the farm setup wizard.

**Total Estimated Effort**: 8 sprints (approximately 2-3 days per sprint)

---

## Sprint 1: Foundation — Launch Choice Screen & Demo Mode Manager
**Goal**: Create the entry point and core demo mode infrastructure

### Tasks
- [ ] Create `presentation/onboarding/LaunchChoiceScreen.kt`
  - Full-screen landscape layout
  - Two large cards: "Set Up My Farm" and "Explore Demo Farm"
  - Shamba Smart logo and "How would you like to get started?" header
  - "No setup required" badge on demo card
- [ ] Create `demo/DemoModeManager.kt`
  - `createDemoDatabase()` — in-memory Room database
  - `provideDemoRepositories()` — demo repository providers
  - `launchDemo()` — sets isDemoMode = true, navigates to Dashboard
  - `exitDemo()` — sets isDemoMode = false, clears database, navigates to LaunchChoiceScreen
  - `isDemoMode` StateFlow for UI detection
- [ ] Update navigation graph to include LaunchChoiceScreen
  - After onboarding screen 3 → LaunchChoiceScreen (instead of FarmSetupScreen)
  - From Settings → LaunchChoiceScreen (for existing users)

### Deliverables
- User can see Launch Choice Screen after onboarding
- Tapping "Explore Demo Farm" navigates to Dashboard (with placeholder data for now)
- Tapping "Set Up My Farm" navigates to existing FarmSetupScreen

---

## Sprint 2: Demo Data Infrastructure — Hilt Module & Data Seeder
**Goal**: Set up the DI framework and data seeding pipeline

### Tasks
- [ ] Create `demo/DemoModeModule.kt` (Hilt module)
  - `@DemoMode` qualifier annotation
  - Provide `@DemoMode ShambaDatabase` (in-memory)
  - Provide `@DemoMode` repositories for all modules
  - Install in `ActivityRetainedComponent::class`
- [ ] Create `demo/DemoDataSeeder.kt`
  - `seedAll(db: ShambaDatabase)` — calls all seed functions
  - `seedFarmProfile(db)` — demo farm identity
  - Stub functions for all 17 modules (to be filled in subsequent sprints)
- [ ] Create `demo/DemoFarm.kt` — farm identity constants
  - FARM_NAME = "Kilimo Bora Farm"
  - OWNER_NAME = "James Makwetta"
  - LOCATION = "Korogwe, Tanga"
  - SIZE_ACRES = 16
  - Coordinates, phone, etc.

### Deliverables
- Hilt can inject demo repositories when demo mode is active
- DataSeeder can seed farm profile (minimal data)
- Demo mode launches with farm name "Kilimo Bora Farm" visible

---

## Sprint 3: Demo Data — Animals & Livestock Module
**Goal**: Populate livestock module with realistic animal data

### Tasks
- [ ] Create `demo/data/DemoAnimals.kt`
  - 11 explicitly defined key animals (G-01 through G-K1)
  - 51 programmatically generated goats
  - 25 programmatically generated sheep
  - Varied statuses: 75% Healthy, 10% Pregnant, 5% Dry, 5% Sick, 5% Kids
- [ ] Create health records data
  - Vaccination records (upcoming, overdue)
  - Treatment records (with withdrawal periods)
  - Deworming records (rotation schedule)
  - At least 3 records per key animal, 1-2 per generated animal
- [ ] Create reproduction records data
  - Pregnant doe (G-22) due in 5 days
  - Recent kidding record (G-03)
  - Heat detection (G-05)
- [ ] Create milk production data
  - 30 days of history for all lactating does
  - G-01: Peak yields ~4.6L/day (upward trend)
  - G-03: Declining trend (triggers Maarifa alert)
  - G-14: Zero yield (illness)
  - G-31: Normal yield but blocked by withdrawal
- [ ] Create weight entries data
  - 6 entries per animal over 6 months
  - G-14: Declining weight trend (triggers nutritional alert)

### Deliverables
- Livestock screen shows 87 animals with realistic data
- Filters work (species, status, search)
- Animal detail panels show health, milk, reproduction tabs with data
- Maarifa can detect milk drop and weight decline patterns

---

## Sprint 4: Demo Data — Crops, Plots & Scouting
**Goal**: Populate crops module with plot and planting data

### Tasks
- [ ] Create `demo/data/DemoCrops.kt`
  - 8 plots covering 16 acres
  - Varied soil types, irrigation types, GPS coordinates
- [ ] Create crop plantings data
  - Plot A: Maize (tasseling) — fertiliser due
  - Plot B: Napier Grass (mature) — ready to cut
  - Plot C: Beans + Maize (flowering)
  - Plot D: Tomatoes (fruiting) — harvest in 8 days
  - Plot E: Kale — ready NOW (amber alert)
  - Plot F: Cassava (early vegetative)
  - Plot G: Onions (germinating)
  - Plot H: Pasture (no planting)
- [ ] Create harvest records data
  - 2 completed harvest records from previous seasons
- [ ] Create scouting reports data
  - 12 reports across plots
  - Critical FAW on Plot A (red alert)
  - Moderate aphids on Plot C
  - Moderate leafminer on Plot D
  - Low stalk borer on Plot A
  - 8 more varied reports

### Deliverables
- Crops screen shows 8 plots with plantings at different growth stages
- Scouting heatmap shows colored circles across farm
- Maarifa can provide guidance for each crop stage
- Harvest alerts trigger correctly

---

## Sprint 5: Demo Data — Finance, Labour & Tasks
**Goal**: Populate financial, labour, and task modules

### Tasks
- [ ] Create `demo/data/DemoFinance.kt`
  - 3 months of income records (current + 2 previous)
  - 3 months of expense records
  - Current month: TZS 412,000 income, TZS 236,000 expenses
  - Net profit: TZS 176,000
  - Loan record: CRDB Bank, TZS 250,000 outstanding
- [ ] Create `demo/data/DemoLabour.kt`
  - 4 workers with varied roles
  - 26 days of attendance for current month
  - W-03: 2 absent days
  - W-04: 10 present days (started mid-month)
  - Today's attendance not yet logged
- [ ] Create tasks data
  - 9 tasks at various states
  - Completed: morning milk collection
  - Pending high: deworming, evening milk
  - Pending medium: fertiliser, weights
  - Pending low: fence check
  - Overdue: feed inventory (amber alert)
  - Maarifa-generated: kidding preparation

### Deliverables
- Financial screen shows income, expenses, P&L with real data
- Loans tab shows active loan with balance
- Labour screen shows 4 workers with attendance history
- Tasks screen shows mix of completed, pending, overdue tasks
- Calendar shows events across next 30 days

---

## Sprint 6: Demo Data — Remaining Modules
**Goal**: Populate all remaining modules with realistic data

### Tasks
- [ ] Create feed inventory data
  - 6 feed items at varied stock levels
  - Silage: LOW (amber alert)
  - Hay: CRITICAL (red alert)
  - Others: OK levels
- [ ] Create cheese batch data
  - 4 batches at different stages
  - CB-07: Aging (5/7 days) — completion in 2 days
  - CB-06: Aging semi-hard (12/21 days)
  - CB-05: Ready to package
  - CB-04: Sold (historical)
- [ ] Create silage inventory data
  - 1 pit with 4.2 tonnes remaining
  - 18 days at current draw rate (below 21-day threshold)
- [ ] Create weather data
  - 14 days of historical weather
  - 5-day forecast
  - Recent rainy spell (days 3-5)
  - Heavy rain event (day 8)
- [ ] Create maintenance data
  - 3 vehicles (pickup, generator, water pump)
  - 4 maintenance tasks (1 overdue, 2 pending, 1 complete)
- [ ] Create map data
  - 7 infrastructure markers
  - Farm boundary polygon (16 acres)
- [ ] Create Maarifa knowledge data
  - 50 representative chunks covering:
    - FAW management
    - CCPP treatment
    - Oxytetracycline dosage/withdrawal
    - Fresh Chèvre process
    - Kidding preparation
    - Maize growth stages
    - Silage quality
    - Korogwe rainfall/disease calendar
  - 5 operational rules
- [ ] Create alerts data
  - 9 alerts across all priority levels
  - 3 Critical (red): vaccination overdue, FAW, hay low
  - 4 High (amber): kidding due, silage low, kale harvest, aphids
  - 2 Info (blue): cheese aging, generator service

### Deliverables
- All modules populated with realistic data
- Alerts screen shows 9 active alerts
- Maarifa can answer questions about farm-specific situations
- Calendar shows events across all modules

---

## Sprint 7: Demo Banner & Exit Flow
**Goal**: Implement persistent demo indicator and exit mechanism

### Tasks
- [ ] Create `demo/DemoBanner.kt`
  - Amber banner: "Demo mode — all data is simulated"
  - "Exit demo" text button
  - Full width, below top bar
- [ ] Integrate banner into `MainActivity.kt`
  - Show banner when `isDemoMode = true`
  - Push content down (no overlap)
- [ ] Implement exit confirmation dialog
  - "Exit demo mode?"
  - "All demo data will be cleared. Your real farm data is unaffected."
  - [Cancel] [Exit demo] buttons
- [ ] Implement `DemoModeManager.exitDemo()`
  - Set `isDemoMode = false` in DataStore
  - Close in-memory Room database
  - Navigate to LaunchChoiceScreen
  - Show snackbar: "Demo ended. Set up your farm to get started."
- [ ] Implement demo restrictions
  - Disable backup export/restore with tooltip
  - Disable remote sync with tooltip
  - Disable weather API sync with tooltip
  - Allow knowledge import and camera inference (for demo exploration)

### Deliverables
- Amber banner visible on every screen during demo mode
- "Exit demo" shows confirmation dialog
- Exiting demo clears all data and returns to LaunchChoiceScreen
- Disabled features show helpful tooltips

---

## Sprint 8: Integration, Testing & Polish
**Goal**: Wire everything together, test, and polish

### Tasks
- [ ] Wire DemoMode detection in all ViewModels
  - Inject `DemoModeManager` where needed
  - Use `isDemoMode` for UI decisions
- [ ] Implement demo launch loading screen
  - "Preparing demo farm…" with animated progress
  - Target: ready within 5 seconds
- [ ] Test full user flow
  - Onboarding → LaunchChoiceScreen → Demo → Explore all modules → Exit → LaunchChoiceScreen
- [ ] Test returning user flow
  - Settings → Try Demo Mode → Demo → Exit → Back to real farm
- [ ] Test demo restrictions
  - Backup export shows tooltip
  - Remote sync shows tooltip
  - Weather API sync shows tooltip
- [ ] Polish UI
  - Ensure demo banner doesn't overlap content
  - Ensure loading screen is smooth
  - Ensure exit flow is clear
- [ ] Update State.md with Demo Mode section
- [ ] Final git commit

### Deliverables
- Demo Mode fully functional end-to-end
- All modules work with demo data
- Exit flow clears data cleanly
- No impact on production data flow
- State.md updated with Demo Mode documentation

---

## Dependencies Between Sprints

```
Sprint 1 (Foundation)
    ↓
Sprint 2 (Infrastructure)
    ↓
Sprint 3 (Animals) ──┐
Sprint 4 (Crops) ────┤
Sprint 5 (Finance) ──┼── Can be done in parallel
Sprint 6 (Remaining) ┘
    ↓
Sprint 7 (Banner & Exit)
    ↓
Sprint 8 (Integration & Testing)
```

## Success Criteria

- [ ] User can launch demo within 5 seconds of tapping "Launch demo"
- [ ] All 17 modules populated with realistic data
- [ ] Maarifa can answer farm-specific questions using seeded knowledge
- [ ] Amber banner visible on every screen during demo
- [ ] Exiting demo clears all data and returns to LaunchChoiceScreen
- [ ] No impact on production data flow or database
- [ ] Demo data is entirely self-contained (in-memory database)