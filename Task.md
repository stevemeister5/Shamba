🎭 Shamba Smart — Demo Mode Implementation Prompt
For AI Coding Agent · Android · Kotlin · Jetpack Compose

CONCEPT
Implement a Demo Mode for Shamba Smart that allows anyone to explore the full application with realistic, pre-populated farm data without going through the farm setup wizard. Demo Mode is offered as a choice on first launch, sits alongside the real onboarding flow, and can be exited at any time. It is entirely self-contained — it never touches the real farm database.

USER FLOW — WHERE DEMO MODE LIVES
After the three onboarding screens (Welcome, Features, Permissions), the user arrives at a Launch Choice Screen instead of going directly to the Farm Setup Wizard.
Onboarding Screen 3 (Permissions)
            │
            ▼
    Launch Choice Screen
    ┌─────────────────────────────┐
    │                             │
    │   [Set Up My Farm]          │  → FarmSetupScreen (existing flow)
    │   [Explore Demo Farm]       │  → DemoModeActivity / Demo Database
    │                             │
    └─────────────────────────────┘
The Launch Choice Screen is also accessible from SettingsScreen → "Try Demo Mode" for existing users who want to explore features they have not used yet, without affecting their real data.

LAUNCH CHOICE SCREEN
File: presentation/onboarding/LaunchChoiceScreen.kt
Layout: Full-screen landscape. Two large cards side by side in the centre of the screen. Above the cards: the Shamba Smart logo and the line "How would you like to get started?"
Card 1 — Set Up My Farm:

Icon: a simple farm outline SVG
Heading: "Set up my farm"
Body: "Create your farm profile and start tracking your livestock, crops, and finances."
Button: "Get started →"
Taps to: FarmSetupScreen

Card 2 — Explore Demo Farm:

Icon: a play triangle SVG
Heading: "Explore with demo data"
Body: "Tour a fully set-up farm — all modules, real data, every feature unlocked. No account needed."
Badge above the card: "No setup required"
Button: "Launch demo →"
Taps to: DemoModeManager.launchDemo()

Returning users:
If OnboardingPreferences.isOnboardingCompleted is true, the LaunchChoiceScreen is not shown on launch. It is only accessible from Settings. The existing launch route (onboarding → farm setup → dashboard) is unchanged for users who have already set up their farm.

DEMO MODE ARCHITECTURE
Demo Mode uses a completely separate in-memory database that is never persisted to the device's real shamba_smart.db. When Demo Mode is exited, the database is destroyed and all demo data is gone. The real farm database is never read, written to, or affected in any way during Demo Mode.
DemoModeManager
      │
      ├── createDemoDatabase()
      │     Room.inMemoryDatabaseBuilder(context, ShambaDatabase::class.java)
      │     → builds a fresh in-memory Room database
      │     → calls DemoDataSeeder.seedAll(db) to populate it
      │
      ├── provideDemoRepositories()
      │     → same repository interfaces as production
      │     → injected with the demo database DAOs instead of real DAOs
      │
      ├── launchDemo()
      │     → sets isDemoMode = true in DataStore
      │     → navigates to DashboardScreen with demo repositories injected
      │
      └── exitDemo()
            → sets isDemoMode = false in DataStore
            → clears in-memory database
            → navigates back to LaunchChoiceScreen
Files to create:
presentation/onboarding/LaunchChoiceScreen.kt
demo/
├── DemoModeManager.kt
├── DemoDataSeeder.kt
├── DemoModeModule.kt           ← Hilt module that provides demo repositories
├── DemoBanner.kt               ← Persistent demo mode indicator shown in top bar
└── data/
    ├── DemoAnimals.kt
    ├── DemoCrops.kt
    ├── DemoCheese.kt
    ├── DemoFeed.kt
    ├── DemoFinance.kt
    ├── DemoLabour.kt
    ├── DemoCalendar.kt
    ├── DemoMaarifa.kt
    ├── DemoMaintenance.kt
    ├── DemoWeather.kt
    └── DemoScouting.kt

DEMO MODE INDICATOR
When Demo Mode is active, a persistent banner is shown in the top bar of every screen. It must never be possible to forget that you are in Demo Mode.
Implementation — DemoBanner.kt:
kotlin@Composable
fun DemoBanner(onExitDemo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0A820))   // amber — always visible
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Demo mode — all data is simulated",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF412402)
        )
        TextButton(onClick = onExitDemo) {
            Text(
                text = "Exit demo",
                fontSize = 12.sp,
                color = Color(0xFF412402),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
The banner appears directly below the existing top bar in MainActivity. It pushes content down — it does not overlap anything. When the user taps "Exit demo", a confirmation dialog appears: "Exit demo mode? All demo data will be lost." → "Exit" or "Stay in demo".

HILT DEPENDENCY INJECTION FOR DEMO MODE
Demo Mode needs to inject demo repositories wherever production repositories are used, without touching the production DI graph.
Approach: Use a Hilt custom component qualifier to distinguish demo from production repositories.
kotlin// DemoModeModule.kt
@Module
@InstallIn(ActivityRetainedComponent::class)
object DemoModeModule {

    @Provides
    @DemoMode
    fun provideDemoDatabase(@ApplicationContext context: Context): ShambaDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            ShambaDatabase::class.java
        ).build()
    }

    @Provides
    @DemoMode
    fun provideDemoAnimalRepository(
        @DemoMode db: ShambaDatabase
    ): AnimalRepository = AnimalRepositoryImpl(db.animalDao())

    // Repeat for all repositories...
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DemoMode
Each ViewModel that participates in Demo Mode receives its repository via constructor injection. When Demo Mode is active, DemoModeManager provides the @DemoMode annotated repositories. When in production mode, the standard @Production repositories are used. The ViewModels themselves do not know which mode they are in — they just use the repository interface.

DEMO DATA SEEDER
File: demo/DemoDataSeeder.kt
The seeder populates every table in the database with realistic data that exercises every feature. All data is set relative to LocalDate.now() so alerts, calendar events, and upcoming tasks always feel current regardless of when the demo is launched.
kotlinobject DemoDataSeeder {
    suspend fun seedAll(db: ShambaDatabase) {
        seedFarmProfile(db)
        seedAnimals(db)
        seedHealthRecords(db)
        seedReproduction(db)
        seedMilkProduction(db)
        seedWeightEntries(db)
        seedPlots(db)
        seedCropPlantings(db)
        seedHarvestRecords(db)
        seedSilageInventory(db)
        seedWeatherLog(db)
        seedScoutingReports(db)
        seedMilkCollection(db)
        seedCheeseBatches(db)
        seedFeedInventory(db)
        seedStoreItems(db)
        seedFinancials(db)
        seedLoans(db)
        seedWorkers(db)
        seedAttendance(db)
        seedTasks(db)
        seedCalendarEvents(db)
        seedMaintenanceTasks(db)
        seedVehicles(db)
        seedMapData(db)
        seedMaarifaKnowledge(db)
        seedAlerts(db)
    }
}

DEMO FARM IDENTITY
The demo farm has a complete identity used throughout the UI:
kotlinobject DemoFarm {
    const val FARM_NAME = "Kilimo Bora Farm"
    const val OWNER_NAME = "James Makwetta"
    const val LOCATION = "Korogwe, Tanga"
    const val SIZE_ACRES = 16
    const val PHONE = "+255 754 123 456"
    val LATITUDE = -5.15
    val LONGITUDE = 38.48
}

DEMO ANIMALS DATA
File: demo/data/DemoAnimals.kt
Seed 62 goats and 25 sheep with varied statuses that exercise every filter, view, and Maarifa integration point.
Goat roster (key animals — seed these explicitly, generate the rest programmatically):
kotlinval demoGoats = listOf(
    // Healthy lactating does — exercises milk production module
    Animal(id="G-01", tagId="G-01", name="Zawadi", species="Goat", breed="Toggenburg",
           sex="Female", birthDate=today.minusYears(3), status="Healthy",
           weight=42f, damId=null, sireId=null),
    Animal(id="G-02", tagId="G-02", name="Baraka", species="Goat", breed="Saanen",
           sex="Female", birthDate=today.minusYears(3), status="Healthy", weight=44f),
    Animal(id="G-03", tagId="G-03", name="Neema", species="Goat", breed="Alpine",
           sex="Female", birthDate=today.minusYears(4), status="Healthy", weight=46f),
    Animal(id="G-04", tagId="G-04", name="Furaha", species="Goat", breed="Nubian",
           sex="Female", birthDate=today.minusYears(2), status="Healthy", weight=38f),
    Animal(id="G-05", tagId="G-05", name="Amani", species="Goat", breed="Toggenburg",
           sex="Female", birthDate=today.minusYears(3), status="Healthy", weight=41f),

    // Pregnant doe — exercises reproduction module and kidding calendar
    Animal(id="G-22", tagId="G-22", name="Tumaini", species="Goat", breed="Alpine",
           sex="Female", birthDate=today.minusYears(4), status="Pregnant", weight=52f),

    // Sick animal — exercises health records, symptom checker, and alerts
    Animal(id="G-14", tagId="G-14", name="Imani", species="Goat", breed="Boer cross",
           sex="Female", birthDate=today.minusYears(2), status="Sick", weight=34f),

    // Dry doe — exercises dry period tracking
    Animal(id="G-09", tagId="G-09", name="Rehema", species="Goat", breed="Nubian",
           sex="Female", birthDate=today.minusYears(5), status="Dry", weight=50f),

    // Doe in withdrawal period — exercises withdrawal flag on milk log
    Animal(id="G-31", tagId="G-31", name="Subira", species="Goat", breed="Saanen",
           sex="Female", birthDate=today.minusYears(3), status="Healthy", weight=43f),

    // Buck
    Animal(id="G-B1", tagId="G-B1", name="Simba", species="Goat", breed="Toggenburg",
           sex="Male", birthDate=today.minusYears(4), status="Healthy", weight=68f),

    // Newborn kid — exercises newborn/untagged flow
    Animal(id="G-K1", tagId=null, name=null, species="Goat", breed="Alpine",
           sex="Female", birthDate=today.minusDays(3), status="Healthy", weight=2.8f),
)
Generate the remaining 50 goats and 25 sheep programmatically using (1..50).map { i -> Animal(...) } with randomised weights within breed norms, randomised ages between 6 months and 6 years, and statuses distributed as: 75% Healthy, 10% Pregnant, 5% Dry, 5% Sick, 5% Kids.

DEMO HEALTH RECORDS
Seed the following to exercise all health record types:
Vaccination records (exercising auto-fill and calendar events):
kotlin// G-01 Zawadi — upcoming vaccination due in 3 days
HealthRecord(animalId="G-01", type="Vaccination", date=today.minusDays(180),
    vaccineName="PPR Vaccine", nextDueDate=today.plusDays(3), notes="Annual booster")

// G-14 Imani — vaccination OVERDUE by 5 days → triggers red alert
HealthRecord(animalId="G-14", type="Vaccination", date=today.minusDays(185),
    vaccineName="Brucellosis", nextDueDate=today.minusDays(5))
Treatment records (exercising withdrawal period blocking):
kotlin// G-31 Subira — treated 4 days ago with Oxytetracycline LA
// Milk withdrawal = 7 days → 3 days remaining → milk log is blocked
HealthRecord(animalId="G-31", type="Treatment", date=today.minusDays(4),
    drugName="Oxytetracycline LA", dose="8ml IM", milkWithdrawalEndDate=today.plusDays(3),
    meatWithdrawalEndDate=today.plusDays(24))

// G-14 Imani — sick, under treatment
HealthRecord(animalId="G-14", type="Illness", date=today.minusDays(2),
    symptoms="Nasal discharge, reduced appetite, lethargy",
    diagnosis="Suspected CCPP — pending vet confirmation",
    treatment="Oxytetracycline LA 8ml IM. Isolate from herd.")
Deworming records (exercising rotation schedule):
kotlin// Whole flock dewormed 45 days ago — next due in 45 days
// This appears as a calendar event and a Maarifa periodic reminder
Seed at least 3 health records per key named animal and 1–2 per generated animal.

DEMO MILK PRODUCTION
Seed 30 days of historical milk data for all lactating does.
Key data patterns to include:

G-01 Zawadi: Peak yields ~4.6L/day, gradual upward trend (new lactation peak)
G-02 Baraka: Steady ~3.8L/day
G-03 Neema: Declining trend — drops 25% in the last 5 days → triggers Maarifa milk drop alert
G-14 Imani: Zero yield for last 2 days due to illness
G-31 Subira: Normal yield but milk is blocked by withdrawal period flag
G-09 Rehema: No entries (dry)
G-22 Tumaini: No entries (pregnant, dry)

kotlin// Seed using a loop for each lactating doe across 30 days
// Today's entry uses today.atStartOfDay() timestamp
// AM yield slightly higher than PM (typical for dairy goats)
val zawadiFeed = (0..30).map { daysAgo ->
    MilkProduction(
        animalId = "G-01",
        date = today.minusDays(daysAgo.toLong()),
        morningYield = 2.4f + (Random.nextFloat() * 0.4f),
        eveningYield = 2.1f + (Random.nextFloat() * 0.3f)
    )
}
Total herd milk yield for today: approximately 34–38 litres.

DEMO REPRODUCTION RECORDS
kotlin// G-22 Tumaini — pregnant, due in 5 days → triggers kidding preparation alert
ReproductionRecord(damId="G-22", sireId="G-B1", type="Pregnancy",
    matingDate=today.minusDays(145),
    pregnancyStatus="Confirmed",
    expectedDueDate=today.plusDays(5))

// G-03 Neema — kidded 60 days ago (recent birth record)
ReproductionRecord(damId="G-03", sireId="G-B1", type="Birth",
    actualBirthDate=today.minusDays(60),
    kidsCount=2, kidsAlive=2, kidsStillborn=0)

// G-05 Amani — heat signs logged yesterday
ReproductionRecord(damId="G-05", type="HeatDetection",
    date=today.minusDays(1),
    notes="Standing heat observed. Buck introduced.")

DEMO WEIGHT ENTRIES
Seed 6 weight entries per animal over the past 6 months, spaced 4 weeks apart. Include one animal (G-14 Imani) with a declining weight trend over the last 3 entries to trigger Maarifa's weight-decline nutritional card.

DEMO PLOTS DATA
File: demo/data/DemoCrops.kt
Seed 8 plots covering the 16 acres, each at a different crop stage to exercise every state in the Crops module:
kotlinval demoPlots = listOf(
    Plot(id="P-A", name="Plot A", sizeAcres=3.0f, soilType="Clay loam",
         currentUse="Crop", irrigationType="Rain-fed",
         latitude=-5.148, longitude=38.479),

    Plot(id="P-B", name="Plot B", sizeAcres=2.5f, soilType="Loam",
         currentUse="Silage", irrigationType="Rain-fed",
         latitude=-5.151, longitude=38.482),

    Plot(id="P-C", name="Plot C", sizeAcres=2.0f, soilType="Loam",
         currentUse="Crop", irrigationType="Rain-fed",
         latitude=-5.153, longitude=38.477),

    Plot(id="P-D", name="Plot D", sizeAcres=1.5f, soilType="Sandy loam",
         currentUse="Crop", irrigationType="Manual",
         latitude=-5.155, longitude=38.481),

    Plot(id="P-E", name="Plot E", sizeAcres=1.5f, soilType="Clay loam",
         currentUse="Crop", irrigationType="Rain-fed",
         latitude=-5.149, longitude=38.484),

    Plot(id="P-F", name="Plot F", sizeAcres=2.0f, soilType="Loam",
         currentUse="Crop", irrigationType="Rain-fed",
         latitude=-5.157, longitude=38.476),

    Plot(id="P-G", name="Plot G", sizeAcres=1.0f, soilType="Sandy loam",
         currentUse="Crop", irrigationType="Manual",
         latitude=-5.152, longitude=38.485),

    Plot(id="P-H", name="Plot H", sizeAcres=2.5f, soilType="Clay",
         currentUse="Pasture", irrigationType="Rain-fed",
         latitude=-5.146, longitude=38.480),
)
Crop plantings — one per plot, at varied growth stages:
kotlinval demoCropPlantings = listOf(
    // Plot A — Silage Maize, tasseling stage → fertiliser due reminder from Maarifa
    CropPlanting(plotId="P-A", cropType="Maize", variety="SEEDCO SC403",
        plantingDate=today.minusDays(55), status="Active"),

    // Plot B — Napier Grass, mature and ready to cut → harvest alert
    CropPlanting(plotId="P-B", cropType="Napier Grass", variety="Clone 13",
        plantingDate=today.minusDays(60), status="Active"),

    // Plot C — Beans intercropped with maize, flowering stage
    CropPlanting(plotId="P-C", cropType="Beans", variety="Lyamungu 85",
        plantingDate=today.minusDays(45), status="Active"),

    // Plot D — Tomatoes, fruiting, harvest window opens in 8 days
    CropPlanting(plotId="P-D", cropType="Tomatoes", variety="Cal-J",
        plantingDate=today.minusDays(75), status="Active"),

    // Plot E — Kale (Sukuma Wiki), ready to harvest NOW → amber alert
    CropPlanting(plotId="P-E", cropType="Kale", variety="Sukuma Wiki",
        plantingDate=today.minusDays(55), status="Active"),

    // Plot F — Cassava, early vegetative stage
    CropPlanting(plotId="P-F", cropType="Cassava", variety="Kiroba",
        plantingDate=today.minusDays(30), status="Active"),

    // Plot G — Onions, germinating
    CropPlanting(plotId="P-G", cropType="Onion", variety="Red Pinoy",
        plantingDate=today.minusDays(14), status="Active"),

    // Plot H — Pasture (no planting record — grazing area)
)
Also seed 2 completed harvest records from Plot E (previous kale harvest) and Plot A (previous maize season) to populate the harvest history and analytics screens.

DEMO SCOUTING REPORTS
Seed 12 scouting reports across the plots to populate the pest heatmap with variety:
kotlinval demoScoutingReports = listOf(
    // Critical FAW on Plot A — triggers red alert
    ScoutingReport(plotId="P-A", pestType="Fall Armyworm",
        severityScore=0.85f, severity="Critical",
        gpsLatitude=-5.148, gpsLongitude=38.479,
        detectedAt=today.minusDays(1).atStartOfDay(),
        notes="Heavy infestation in whorl. Frass visible. ~40% of plants affected."),

    // Moderate aphids on Plot C
    ScoutingReport(plotId="P-C", pestType="Aphids",
        severityScore=0.45f, severity="Moderate",
        gpsLatitude=-5.153, gpsLongitude=38.477,
        detectedAt=today.minusDays(3).atStartOfDay()),

    // Low stalk borer on Plot A from 2 weeks ago
    ScoutingReport(plotId="P-A", pestType="Maize Stalk Borer",
        severityScore=0.2f, severity="Low",
        gpsLatitude=-5.148, gpsLongitude=38.480,
        detectedAt=today.minusDays(14).atStartOfDay()),

    // Moderate leafminer on Plot D tomatoes
    ScoutingReport(plotId="P-D", pestType="Leafminer",
        severityScore=0.55f, severity="Moderate",
        gpsLatitude=-5.155, gpsLongitude=38.481,
        detectedAt=today.minusDays(2).atStartOfDay()),

    // 8 more reports spread across plots, varying severity, over the last 30 days
    // Include at least one of each of the 8 pest types
)
The heatmap will show a rich spread of coloured circles across the farm — red on Plot A, orange on Plot D, yellow on Plot C, and faded green circles for older low-severity reports.

DEMO SILAGE INVENTORY
kotlinSilageInventory(
    pitLocation="Pit 1 — Main silage pit",
    cropType="Maize Silage",
    fillDate=today.minusDays(90),
    estimatedTonnage=12.0f,
    currentQuantityTonnes=4.2f,    // LOW — triggers amber alert
    fermentationDays=21,
    quality="Good",
    dailyDrawdownKg=230f
    // At 230kg/day: 4200kg / 230 = ~18 days remaining → below 21-day threshold
)

DEMO CHEESE BATCHES
Seed 4 cheese batches at different stages to populate every view in the Cheese module:
kotlinval demoCheeseBatches = listOf(
    // Batch 1 — Aging, 5 of 7 days complete → completion alert in 2 days (blue)
    CheeseBatch(batchId="CB-07", cheeseType="Fresh Chèvre",
        milkVolume=20f, yieldKg=null, startDate=today.minusDays(5),
        agingDays=7, status="Aging",
        milkCostTzs=16000, cultureCostTzs=1200, rennetCostTzs=800,
        packagingCostTzs=1500, labourCostTzs=0, otherInputCostTzs=500),

    // Batch 2 — Aging semi-hard, 12 of 21 days (57%)
    CheeseBatch(batchId="CB-06", cheeseType="Feta-style",
        milkVolume=30f, yieldKg=null, startDate=today.minusDays(12),
        agingDays=21, status="Aging",
        milkCostTzs=24000, cultureCostTzs=2000, rennetCostTzs=1200,
        packagingCostTzs=2200, labourCostTzs=0, otherInputCostTzs=800),

    // Batch 3 — Ready to package (aging complete)
    CheeseBatch(batchId="CB-05", cheeseType="Fresh Chèvre",
        milkVolume=25f, yieldKg=4.8f, startDate=today.minusDays(8),
        agingDays=7, status="Ready",
        milkCostTzs=20000, cultureCostTzs=1500, rennetCostTzs=1000,
        packagingCostTzs=1800, labourCostTzs=0, otherInputCostTzs=600),

    // Batch 4 — Sold (historical record)
    CheeseBatch(batchId="CB-04", cheeseType="Fresh Chèvre",
        milkVolume=18f, yieldKg=3.4f, startDate=today.minusDays(20),
        agingDays=7, status="Sold",
        salePriceTzsPerKg=15000, quantitySoldKg=3.4f,
        saleDate=today.minusDays(12),
        milkCostTzs=14400, cultureCostTzs=1000, rennetCostTzs=700,
        packagingCostTzs=1200, labourCostTzs=0, otherInputCostTzs=400)
)

DEMO FEED INVENTORY
Seed inventory items at varied stock levels to exercise the reorder alert system:
kotlinval demoFeedItems = listOf(
    FeedInventory(feedType="Silage (Maize)", quantity=4200f, unit="kg",
        reorderThreshold=6000f, costPerUnit=0f),        // LOW — amber alert

    FeedInventory(feedType="Napier Grass (Fresh)", quantity=680f, unit="kg",
        reorderThreshold=200f, costPerUnit=0f),          // OK

    FeedInventory(feedType="Dairy Meal (Concentrate)", quantity=120f, unit="kg",
        reorderThreshold=50f, costPerUnit=850f),         // OK

    FeedInventory(feedType="Mineral Supplement", quantity=25f, unit="kg",
        reorderThreshold=10f, costPerUnit=4500f),        // OK

    FeedInventory(feedType="Hay (Backup)", quantity=40f, unit="kg",
        reorderThreshold=100f, costPerUnit=200f),        // CRITICAL — red alert

    FeedInventory(feedType="Salt Lick Blocks", quantity=4f, unit="blocks",
        reorderThreshold=2f, costPerUnit=3500f),         // OK
)

DEMO FINANCIAL DATA
File: demo/data/DemoFinance.kt
Seed 3 months of financial history (current month + 2 previous months) to populate charts, P&L summaries, and enterprise benchmarks.
Current month income (realistic for this farm scale):
kotlinval currentMonthIncome = listOf(
    Income(date=today.minusDays(2), category="Cheese sales",
        description="4 batches — Fresh Chèvre to Korogwe market", amount=184000),
    Income(date=today.minusDays(5), category="Milk sales",
        description="Surplus milk — 120L to Tanga Dairy Co-op", amount=96000),
    Income(date=today.minusDays(8), category="Live animal sales",
        description="2 male goats — Korogwe livestock market", amount=80000),
    Income(date=today.minusDays(12), category="Vegetable sales",
        description="Kale harvest — 180kg to Korogwe market", amount=38000),
    Income(date=today.minusDays(15), category="Manure sales",
        description="2 truck loads — local vegetable farmers", amount=14000),
)
// Total current month income: TZS 412,000

val currentMonthExpenses = listOf(
    Expense(date=today.minusDays(1), category="Labour",
        description="Monthly wages — 4 workers", amount=80000),
    Expense(date=today.minusDays(3), category="Feed",
        description="Dairy meal 100kg — Korogwe Agrovet", amount=85000),
    Expense(date=today.minusDays(6), category="Veterinary & medicine",
        description="Vet visit — G-14 consultation + Oxytetracycline", amount=22000),
    Expense(date=today.minusDays(9), category="Seeds & fertiliser",
        description="DAP 50kg — Plot A top dressing", amount=18500),
    Expense(date=today.minusDays(11), category="Cheese inputs",
        description="Rennet + mesophilic cultures", amount=14000),
    Expense(date=today.minusDays(14), category="Fuel & transport",
        description="Market trips + generator fuel", amount=9500),
    Expense(date=today.minusDays(18), category="Packaging",
        description="Cheese packaging materials — 50 units", amount=7000),
)
// Total current month expenses: TZS 236,000
// Net profit: TZS 176,000
Also seed the previous 2 months with similar data at slightly lower revenue (showing an upward trend on the monthly chart).
Loan record:
kotlinLoan(lender="CRDB Bank Korogwe", principalAmount=500000,
    interestRate=18f, startDate=today.minusMonths(6),
    dueDate=today.plusMonths(6), status="Active",
    amountPaid=250000)
// Outstanding balance: TZS 250,000 — appears in Financial module Loans tab

DEMO WORKERS AND ATTENDANCE
File: demo/data/DemoLabour.kt
kotlinval demoWorkers = listOf(
    Worker(id="W-01", name="Amina Juma", role="Milking & livestock",
        contact="+255 712 345 678", hireDate=today.minusYears(2),
        dailyRate=2500, isSeasonal=false, isActive=true),

    Worker(id="W-02", name="Joseph Mwanga", role="Crops & fencing",
        contact="+255 754 987 654", hireDate=today.minusYears(1),
        dailyRate=2000, isSeasonal=false, isActive=true),

    Worker(id="W-03", name="Moses Kilima", role="Crops & general",
        contact="+255 768 111 222", hireDate=today.minusMonths(8),
        dailyRate=2000, isSeasonal=false, isActive=true),

    Worker(id="W-04", name="Fatuma Said", role="Casual — cheese room",
        contact="+255 745 333 444", hireDate=today.minusDays(10),
        dailyRate=2500, isSeasonal=true,
        seasonStart=today.minusDays(10), seasonEnd=today.plusMonths(2),
        isActive=true),
)
Seed 26 days of attendance for the current month for each worker. W-03 has 2 absent days. W-04 has 10 present days (started mid-month). Today's attendance is not yet logged — this gives the user something to do when they open the Labour module.

DEMO TASKS
Seed a mix of tasks to exercise every state in the task and calendar modules:
kotlinval demoTasks = listOf(
    // Completed — already done today
    Task(title="Morning milk collection — all does",
        dueDate=today, isCompleted=true, priority="High",
        assignedWorkerId="W-01"),

    // Pending high priority — today
    Task(title="Deworm sheep flock (Group B)",
        dueDate=today, isCompleted=false, priority="High",
        assignedWorkerId="W-02"),

    // Pending medium priority — today
    Task(title="Apply CAN top dressing — Plot A maize",
        dueDate=today, isCompleted=false, priority="Medium",
        assignedWorkerId="W-03"),

    // Pending medium — today
    Task(title="Record weights — newborn kid G-K1",
        dueDate=today, isCompleted=false, priority="Medium",
        assignedWorkerId="W-01"),

    // Pending low — today
    Task(title="Check east perimeter fence — Plot B boundary",
        dueDate=today, isCompleted=false, priority="Low",
        assignedWorkerId="W-02"),

    // Pending — evening
    Task(title="Evening milk collection + log yield",
        dueDate=today, isCompleted=false, priority="High",
        assignedWorkerId="W-01"),

    // Upcoming — tomorrow
    Task(title="Spray Plot D tomatoes — Dithane at tasseling",
        dueDate=today.plusDays(1), isCompleted=false, priority="High",
        assignedWorkerId="W-03"),

    // Upcoming — in 5 days (kidding preparation)
    Task(title="Prepare kidding pen — G-22 Tumaini due in 5 days",
        dueDate=today.plusDays(5), isCompleted=false, priority="High",
        isMaarifaGenerated=true, assignedWorkerId="W-01"),

    // Overdue — was due yesterday (triggers amber alert)
    Task(title="Log feed inventory — daily silage draw-down",
        dueDate=today.minusDays(1), isCompleted=false, priority="Medium",
        assignedWorkerId="W-02"),
)

DEMO CALENDAR EVENTS
Seed events that appear across the next 30 days and the past 14 days:
kotlinval demoCalendarEvents = listOf(
    // Past events (history)
    CalendarEvent(title="Maize planting — Plot A", date=today.minusDays(55),
        type="Planting", isMaarifaGenerated=true),
    CalendarEvent(title="Cheese batch CB-06 started", date=today.minusDays(12),
        type="Cheese", isMaarifaGenerated=false),

    // This week
    CalendarEvent(title="Korogwe livestock market", date=today.plusDays(2),
        type="Market", isMaarifaGenerated=false),
    CalendarEvent(title="Cheese batch CB-07 — aging complete",
        date=today.plusDays(2), type="Cheese", isMaarifaGenerated=true),

    // Next week
    CalendarEvent(title="G-22 Tumaini — expected to kid",
        date=today.plusDays(5), type="Reproduction", isMaarifaGenerated=true),
    CalendarEvent(title="Sheep deworming — Group A due",
        date=today.plusDays(7), type="Health", isMaarifaGenerated=true),
    CalendarEvent(title="Korogwe market day", date=today.plusDays(9),
        type="Market", isMaarifaGenerated=false),

    // This month
    CalendarEvent(title="Tomato harvest window opens — Plot D",
        date=today.plusDays(8), type="Harvest", isMaarifaGenerated=true),
    CalendarEvent(title="Napier grass silage — chopping day",
        date=today.plusDays(12), type="Silage", isMaarifaGenerated=false),
    CalendarEvent(title="Kale harvest — Plot E",
        date=today.plusDays(0), type="Harvest", isMaarifaGenerated=true),
    CalendarEvent(title="CRDB loan repayment due",
        date=today.plusDays(15), type="Finance", isMaarifaGenerated=false),
    CalendarEvent(title="G-01 Zawadi — PPR vaccination due",
        date=today.plusDays(3), type="Vaccination", isMaarifaGenerated=true),
    CalendarEvent(title="End-of-month payroll — all workers",
        date=today.plusDays(today.lengthOfMonth() - today.dayOfMonth),
        type="Labour", isMaarifaGenerated=false),
)

DEMO WEATHER LOG
Seed 14 days of weather history and a 5-day forecast:
kotlin// Historical — 14 days
val demoWeatherLog = (1..14).map { daysAgo ->
    WeatherLog(
        date = today.minusDays(daysAgo.toLong()),
        rainfallMm = when {
            daysAgo in 3..5 -> 18f + Random.nextFloat() * 8f  // rainy spell
            daysAgo == 8 -> 34f                                 // heavy rain event
            else -> Random.nextFloat() * 4f                    // dry / trace
        },
        maxTemp = 30f + Random.nextFloat() * 3f,
        minTemp = 22f + Random.nextFloat() * 2f,
        humidity = 65f + Random.nextFloat() * 15f,
        windSpeed = 8f + Random.nextFloat() * 6f
    )
}
// Total rainfall last 14 days: ~85mm → triggers Maarifa grey leaf spot risk card on Plot A

// 5-day forecast (cached from API or manually seeded)
val demoForecast = listOf(
    WeatherForecast(date=today.plusDays(1), maxTemp=31f, minTemp=23f,
        condition="Partly cloudy", rainfallMm=2f),
    WeatherForecast(date=today.plusDays(2), maxTemp=29f, minTemp=22f,
        condition="Cloudy", rainfallMm=8f),
    WeatherForecast(date=today.plusDays(3), maxTemp=27f, minTemp=21f,
        condition="Rain", rainfallMm=22f),
    WeatherForecast(date=today.plusDays(4), maxTemp=26f, minTemp=21f,
        condition="Heavy rain", rainfallMm=35f),
    WeatherForecast(date=today.plusDays(5), maxTemp=29f, minTemp=22f,
        condition="Partly cloudy", rainfallMm=6f),
)

DEMO MAINTENANCE
kotlinval demoVehicles = listOf(
    Vehicle(name="Farm pickup truck", type="4WD Pickup",
        fuelType="Diesel", purchaseDate=today.minusYears(3)),
    Vehicle(name="Generator — 5kVA", type="Generator",
        fuelType="Petrol", purchaseDate=today.minusYears(2)),
    Vehicle(name="Water pump", type="Pump",
        fuelType="Petrol", purchaseDate=today.minusYears(1)),
)

val demoMaintenanceTasks = listOf(
    // Overdue — triggers amber alert
    MaintenanceTask(equipmentType="Generator", description="Monthly service and oil check",
        scheduledDate=today.minusDays(5), status="Overdue"),

    // Due next week
    MaintenanceTask(equipmentType="Farm pickup truck",
        description="Oil change and tyre pressure check",
        scheduledDate=today.plusDays(8), status="Pending"),

    // Completed
    MaintenanceTask(equipmentType="Water pump",
        description="Impeller inspection and belt replacement",
        scheduledDate=today.minusDays(15), status="Complete",
        completionDate=today.minusDays(14)),

    // Dipping tank
    MaintenanceTask(equipmentType="Dipping tank",
        description="Clean and replenish dip solution",
        scheduledDate=today.plusDays(3), status="Pending"),
)

DEMO MAP DATA
kotlinval demoMapMarkers = listOf(
    MapMarkerEntity(latitude=-5.150, longitude=38.478,
        type="Shelter", label="Main goat shed", notes="Capacity 40 animals"),
    MapMarkerEntity(latitude=-5.153, longitude=38.479,
        type="Shelter", label="Sheep pen", notes="Capacity 30 animals"),
    MapMarkerEntity(latitude=-5.151, longitude=38.480,
        type="Water", label="Main water trough", notes="Fed from borehole"),
    MapMarkerEntity(latitude=-5.148, longitude=38.477,
        type="Storage", label="Feed store", notes="Silage pit, hay store, concentrate bags"),
    MapMarkerEntity(latitude=-5.152, longitude=38.481,
        type="Cheese", label="Cheese production room", notes="20m² with cold storage"),
    MapMarkerEntity(latitude=-5.154, longitude=38.482,
        type="Compost", label="Compost pit 1", notes="Active — manure composting"),
    MapMarkerEntity(latitude=-5.149, longitude=38.483,
        type="Water", label="Irrigation point — Plot D", notes="Manual pump connection"),
)

// Farm boundary — a realistic 16-acre polygon around the plot markers
val demoBoundaryPoints = listOf(
    BoundaryPointEntity(latitude=-5.145, longitude=38.474, sequence=0),
    BoundaryPointEntity(latitude=-5.145, longitude=38.487, sequence=1),
    BoundaryPointEntity(latitude=-5.159, longitude=38.487, sequence=2),
    BoundaryPointEntity(latitude=-5.159, longitude=38.474, sequence=3),
    // Close the polygon: last point connects back to first
)

DEMO ALERTS
Seed the following alerts explicitly so the Alerts module is fully populated on demo launch:
kotlinval demoAlerts = listOf(
    // Red — critical
    Alert(type="Health", priority="Critical",
        title="G-14 Imani — Brucellosis vaccination overdue",
        message="Overdue by 5 days. Last vaccinated Dec 2025. Vet visit required.",
        linkedEntityId="G-14", linkedModule="Livestock"),

    Alert(type="Pest", priority="Critical",
        title="Critical FAW detected — Plot A",
        message="Fall Armyworm at critical severity. 40% of plants affected. Treatment required immediately.",
        linkedEntityId="P-A", linkedModule="Scouting"),

    Alert(type="Feed", priority="Critical",
        title="Hay stock critically low",
        message="Only 40kg remaining. Below 100kg reorder threshold. Restock immediately.",
        linkedEntityId=null, linkedModule="Feed"),

    // Amber — warnings
    Alert(type="Health", priority="High",
        title="G-22 Tumaini — due to kid in 5 days",
        message="Prepare kidding pen. Ensure colostrum plan is in place.",
        linkedEntityId="G-22", linkedModule="Livestock"),

    Alert(type="Feed", priority="High",
        title="Silage Pit 1 — 18 days remaining",
        message="Below 21-day threshold at current draw rate. Plan procurement.",
        linkedEntityId=null, linkedModule="Feed"),

    Alert(type="Crop", priority="High",
        title="Plot E kale — harvest window now open",
        message="Kale is ready. Quality will decline in 3–4 days if not harvested.",
        linkedEntityId="P-E", linkedModule="Crops"),

    Alert(type="Pest", priority="High",
        title="Moderate aphids — Plot C beans",
        message="Aphid infestation at moderate severity. Scout and assess for chemical control.",
        linkedEntityId="P-C", linkedModule="Scouting"),

    // Blue — informational
    Alert(type="Cheese", priority="Info",
        title="Batch CB-07 — aging complete in 2 days",
        message="Fresh Chèvre batch CB-07 will be ready to package on ${today.plusDays(2)}.",
        linkedEntityId="CB-07", linkedModule="Cheese"),

    Alert(type="Maintenance", priority="Info",
        title="Generator service overdue by 5 days",
        message="Monthly service was due ${today.minusDays(5)}. Schedule maintenance.",
        linkedEntityId=null, linkedModule="Maintenance"),
)

DEMO MAARIFA DATA
File: demo/data/DemoMaarifa.kt
For the demo, seed the Maarifa knowledge base with a small set of representative chunks that cover the farm's situation. This allows Ask Maarifa queries to return real answers during the demo without requiring the full 500+ chunk knowledge base to be seeded into the in-memory database (which would slow demo launch significantly).
Seed at minimum 50 representative chunks covering:

Fall Armyworm management (matches the active pest alert)
CCPP symptoms and treatment (matches G-14's illness)
Oxytetracycline dosage and withdrawal (matches G-31's withdrawal flag)
Fresh Chèvre process guide (matches active cheese batch)
Kidding preparation checklist (matches G-22's upcoming due date)
Maize growth stage guidance at tasseling (matches Plot A)
Silage quality assessment (matches the low stock alert)
Korogwe rainfall and disease risk calendar (matches the recent weather data)

These 50 chunks are sufficient to demonstrate the full Maarifa retrieval pipeline during a demo — real answers, real sources, real confidence tiers — without the 2–4 minute full knowledge base seeding time.
Also seed 5 representative operational rules:
kotlinlistOf(
    OperationalRule(ruleType="withdrawal_period",
        condition="""{"drug":"oxytetracycline_la","species":["goat","sheep"]}""",
        action="""{"milk_withdrawal_days":7,"meat_withdrawal_days":28}""", priority=1),

    OperationalRule(ruleType="gestation",
        condition="""{"species":"goat"}""",
        action="""{"gestation_days":150,"pre_event_task_days":7}""", priority=1),

    OperationalRule(ruleType="gestation",
        condition="""{"species":"sheep"}""",
        action="""{"gestation_days":147,"pre_event_task_days":7}""", priority=1),

    OperationalRule(ruleType="planting_window",
        condition="""{"crop":"maize","location":"korogwe","season":"long_rains"}""",
        action="""{"start_month":3,"end_month":4}""", priority=1),

    OperationalRule(ruleType="notifiable_disease",
        condition="""{"disease":"CCPP","species":["goat"]}""",
        action="""{"reporting_body":"TVLA","action":"isolate_and_report"}""", priority=1),
)

DEMO MODE UX BEHAVIOURS
What works normally in demo mode:
Every module, every screen, every filter, every Maarifa query, every chart, every form. The user can create new records, edit existing ones, and explore fully.
What is intentionally disabled in demo mode:
kotlinobject DemoRestrictions {
    // These features are disabled with a tooltip explaining why
    val DISABLED_FEATURES = setOf(
        "BACKUP_EXPORT",      // "Not available in demo mode — no real data to back up"
        "BACKUP_RESTORE",     // "Not available in demo mode"
        "WEATHER_API_SYNC",   // "Weather data is simulated in demo mode"
        "REMOTE_SYNC",        // "Sync requires a real farm account"
        "KNOWLEDGE_IMPORT",   // Allow this — let users try importing a document
        "CAMERA_INFERENCE",   // Allow this — let users try pest detection
    )
}
```

Tapping a disabled feature shows a bottom sheet: "This feature is not available in demo mode. [Exit demo and set up your farm →]"

**What is visually different in demo mode:**
- The amber demo banner at the top of every screen
- "DEMO" watermark on any exported PDF (if export is enabled)
- All financial figures use realistic but clearly illustrative amounts

**Seeded data created by the user during demo:**
Any records the user creates during demo (new animals, new tasks, new health records) are saved to the in-memory database and persist for the duration of the demo session. They disappear when the user exits demo mode. This allows the user to genuinely interact with every form and see their input reflected in the UI.

---

### DEMO MODE EXIT FLOW

When the user taps "Exit demo" in the banner:
```
Confirmation dialog:
"Exit demo mode?"
"All demo data will be cleared. Your real farm data is unaffected."
[Cancel]  [Exit demo]
        │
        ▼
DemoModeManager.exitDemo()
        │
        ├── Sets isDemoMode = false in DataStore
        ├── Closes in-memory Room database
        │   (database is garbage collected — all demo data destroyed)
        └── Navigates to LaunchChoiceScreen
            with a snackbar: "Demo ended. Set up your farm to get started."

DEMO MODE DETECTION IN VIEWMODELS
Every ViewModel that needs to know whether it is in demo mode injects DemoModeManager and reads isDemoMode:
kotlin@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    val isDemoMode: StateFlow<Boolean> = demoModeManager.isDemoMode

    // The repository itself is already the demo or production version
    // depending on which Hilt module provided it.
    // isDemoMode is only needed for UI decisions like showing the banner
    // or disabling certain features.
}

DEMO MODE LAUNCH PERFORMANCE TARGET
The demo must be ready to use within 5 seconds of tapping "Launch demo". To meet this:

Seed only the 50 representative Maarifa chunks (not the full 500+)
Run DemoDataSeeder.seedAll() in a coroutine on Dispatchers.IO
Show a loading screen: "Preparing demo farm…" with an animated progress indicator
Navigate to DashboardScreen as soon as seeding completes
The Maarifa vector embeddings for the 50 demo chunks are pre-computed at build time and bundled as a small binary asset — they are not computed at demo launch


STATE.MD UPDATE
After implementing demo mode, add the following section to State.md:
markdown## Demo Mode

| Field | Value |
|-------|-------|
| Entry point | LaunchChoiceScreen (after onboarding) or Settings → Try Demo Mode |
| Database | Room in-memory database — never persists |
| Data seeder | DemoDataSeeder.kt — seeds all 17 modules with realistic data |
| Animals | 62 goats + 25 sheep with varied statuses |
| Plots | 8 plots across 16 acres, 7 active crop plantings |
| Cheese batches | 4 batches at varied aging stages |
| Financial history | 3 months of income and expense records |
| Workers | 4 workers with 26 days of attendance |
| Alerts | 9 active alerts across all priority levels |
| Maarifa chunks | 50 representative chunks (not full 500+) |
| Launch target | Ready within 5 seconds of tapping "Launch demo" |
| Indicator | Amber banner on every screen: "Demo mode — all data is simulated" |
| Exit | "Exit demo" button in banner → confirmation → LaunchChoiceScreen |
| Disabled features | Backup export/restore, remote sync, weather API sync |

Do not modify any existing production data flow, database, or repository to implement Demo Mode. The production path must remain completely unchanged. Demo Mode is an additive feature that uses the same interfaces with a separate in-memory implementation.