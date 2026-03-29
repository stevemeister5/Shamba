package com.shambasmart.demo

import com.shambasmart.data.local.ShambaDatabase
import com.shambasmart.data.local.entity.*
import com.shambasmart.data.local.entity.maarifa.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.random.Random

/**
 * Seeds all database tables with realistic demo data.
 * 
 * All dates are relative to LocalDate.now() so the demo
 * always feels current regardless of when it's launched.
 */
object DemoDataSeeder {
    
    suspend fun seedAll(db: ShambaDatabase) {
        seedAnimals(db)
        seedPlots(db)
        seedCropPlantings(db)
        seedHealthRecords(db)
        seedReproductionRecords(db)
        seedMilkProduction(db)
        seedWeightEntries(db)
        seedSilageInventory(db)
        seedCheeseBatches(db)
        seedFeedInventory(db)
        seedStoreItems(db)
        seedIncome(db)
        seedExpenses(db)
        seedLoans(db)
        seedWorkers(db)
        seedAttendanceRecords(db)
        seedTasks(db)
        seedCalendarEvents(db)
        seedWeatherLogs(db)
        seedMaintenanceTasks(db)
        seedVehicles(db)
        seedMapMarkers(db)
        seedScoutingReports(db)
        seedIngestedDocuments(db)
        seedKnowledgeChunks(db)
        seedOperationalRules(db)
        seedMilkCollections(db)
        seedHarvestRecords(db)
        seedCropInputs(db)
    }

    private suspend fun seedAnimals(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val animalDao = db.animalDao()
        
        // Goats - 62 total
        val goatBreeds = listOf("Toggenburg", "Saanen", "Alpine", "Boer", "Nubian")
        val femaleNames = listOf(
            "Zawadi", "Baraka", "Neema", "Furaha", "Amani", "Tumaini", "Imani",
            "Rehema", "Subira", "Upendo", "Amani", "Neema", "Furaha", "Zawadi",
            "Baraka", "Tumaini", "Rehema", "Subira", "Upendo", "Imani"
        )
        val maleNames = listOf("Simba", "Kicho", "Jabali", "Mufasa", "Kifaru")
        
        // Named does - lactating
        val lactatingDoes = listOf(
            Animal(species = "Goat", breed = "Toggenburg", sex = "Female", dateOfBirth = today.minusYears(3), weight = 42.0, status = "active"),
            Animal(species = "Goat", breed = "Saanen", sex = "Female", dateOfBirth = today.minusYears(3), weight = 44.0, status = "active"),
            Animal(species = "Goat", breed = "Alpine", sex = "Female", dateOfBirth = today.minusYears(4), weight = 46.0, status = "active"),
            Animal(species = "Goat", breed = "Nubian", sex = "Female", dateOfBirth = today.minusYears(2), weight = 38.0, status = "active"),
            Animal(species = "Goat", breed = "Toggenburg", sex = "Female", dateOfBirth = today.minusYears(3), weight = 41.0, status = "active"),
        )
        lactatingDoes.forEach { animalDao.insertAnimal(it) }
        
        // Pregnant doe
        animalDao.insertAnimal(
            Animal(species = "Goat", breed = "Alpine", sex = "Female", dateOfBirth = today.minusYears(4), weight = 52.0, status = "active")
        )
        
        // Sick doe
        animalDao.insertAnimal(
            Animal(species = "Goat", breed = "Boer", sex = "Female", dateOfBirth = today.minusYears(2), weight = 34.0, status = "active")
        )
        
        // Dry doe
        animalDao.insertAnimal(
            Animal(species = "Goat", breed = "Nubian", sex = "Female", dateOfBirth = today.minusYears(5), weight = 50.0, status = "active")
        )
        
        // Doe in withdrawal
        animalDao.insertAnimal(
            Animal(species = "Goat", breed = "Saanen", sex = "Female", dateOfBirth = today.minusYears(3), weight = 43.0, status = "active")
        )
        
        // Bucks
        repeat(3) { i ->
            animalDao.insertAnimal(
                Animal(species = "Goat", breed = goatBreeds[i % goatBreeds.size], sex = "Male", dateOfBirth = today.minusYears(4), weight = 68.0, status = "active")
            )
        }
        
        // Generate remaining goats (50 more)
        repeat(50) { i ->
            val sex = if (i < 38) "Female" else "Male"
            val ageMonths = Random.nextInt(6, 72)
            val breed = goatBreeds.random()
            val weight = if (sex == "Female") 35.0 + Random.nextDouble() * 15.0 else 55.0 + Random.nextDouble() * 20.0
            animalDao.insertAnimal(
                Animal(species = "Goat", breed = breed, sex = sex, dateOfBirth = today.minusDays(ageMonths * 30), weight = weight, status = "active")
            )
        }
        
        // Sheep - 25 total
        val sheepBreeds = listOf("Dorper", "Blackhead Persian", "Red Masai", "Merino")
        repeat(25) { i ->
            val sex = if (i < 19) "Female" else "Male"
            val ageMonths = Random.nextInt(6, 60)
            val breed = sheepBreeds.random()
            val weight = if (sex == "Female") 30.0 + Random.nextDouble() * 15.0 else 45.0 + Random.nextDouble() * 20.0
            animalDao.insertAnimal(
                Animal(species = "Sheep", breed = breed, sex = sex, dateOfBirth = today.minusDays(ageMonths * 30), weight = weight, status = "active")
            )
        }
    }

    private suspend fun seedPlots(db: ShambaDatabase) {
        val plotDao = db.plotDao()
        val plots = listOf(
            Plot(name = "Plot A", sizeAcres = 3.0, latitude = -5.148, longitude = 38.479, soilType = "Clay loam", currentUse = "Crop"),
            Plot(name = "Plot B", sizeAcres = 2.5, latitude = -5.151, longitude = 38.482, soilType = "Loam", currentUse = "Silage"),
            Plot(name = "Plot C", sizeAcres = 2.0, latitude = -5.153, longitude = 38.477, soilType = "Loam", currentUse = "Crop"),
            Plot(name = "Plot D", sizeAcres = 1.5, latitude = -5.155, longitude = 38.481, soilType = "Sandy loam", currentUse = "Crop"),
            Plot(name = "Plot E", sizeAcres = 1.5, latitude = -5.149, longitude = 38.484, soilType = "Clay loam", currentUse = "Crop"),
            Plot(name = "Plot F", sizeAcres = 2.0, latitude = -5.157, longitude = 38.476, soilType = "Loam", currentUse = "Crop"),
            Plot(name = "Plot G", sizeAcres = 1.0, latitude = -5.152, longitude = 38.485, soilType = "Sandy loam", currentUse = "Crop"),
            Plot(name = "Plot H", sizeAcres = 2.5, latitude = -5.146, longitude = 38.480, soilType = "Clay", currentUse = "Pasture"),
        )
        plots.forEach { plotDao.insertPlot(it) }
    }

    private suspend fun seedCropPlantings(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cropDao = db.cropDao()
        
        // Get plot IDs (1-8 based on insertion order)
        val plantings = listOf(
            CropPlanting(plotId = 1, cropType = "Maize", variety = "SEEDCO SC403", plantingDate = today.minusDays(55), status = "growing"),
            CropPlanting(plotId = 2, cropType = "Napier Grass", variety = "Clone 13", plantingDate = today.minusDays(60), status = "growing"),
            CropPlanting(plotId = 3, cropType = "Beans", variety = "Lyamungu 85", plantingDate = today.minusDays(45), status = "growing"),
            CropPlanting(plotId = 4, cropType = "Tomatoes", variety = "Cal-J", plantingDate = today.minusDays(75), status = "growing"),
            CropPlanting(plotId = 5, cropType = "Kale", variety = "Sukuma Wiki", plantingDate = today.minusDays(55), status = "growing"),
            CropPlanting(plotId = 6, cropType = "Cassava", variety = "Kiroba", plantingDate = today.minusDays(30), status = "growing"),
            CropPlanting(plotId = 7, cropType = "Onion", variety = "Red Pinoy", plantingDate = today.minusDays(14), status = "growing"),
        )
        plantings.forEach { cropDao.insertCropPlanting(it) }
    }

    private suspend fun seedHealthRecords(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val healthDao = db.healthRecordDao()
        
        // Vaccination due in 3 days (animal 1)
        healthDao.insertHealthRecord(
            HealthRecord(animalId = 1, type = "Vaccination", date = today.minusDays(180), vaccineName = "PPR Vaccine", nextDueDate = today.plusDays(3), notes = "Annual booster")
        )
        // Vaccination overdue (animal 7)
        healthDao.insertHealthRecord(
            HealthRecord(animalId = 7, type = "Vaccination", date = today.minusDays(185), vaccineName = "Brucellosis", nextDueDate = today.minusDays(5))
        )
        // Treatment with withdrawal (animal 9)
        healthDao.insertHealthRecord(
            HealthRecord(animalId = 9, type = "Treatment", date = today.minusDays(4), description = "Oxytetracycline LA 8ml IM", notes = "Milk withdrawal 7 days")
        )
        // Illness record (animal 7)
        healthDao.insertHealthRecord(
            HealthRecord(animalId = 7, type = "Illness", date = today.minusDays(2), description = "Suspected CCPP — nasal discharge, reduced appetite", notes = "Isolated. Pending vet confirmation.")
        )
        // Deworming for flock
        healthDao.insertHealthRecord(
            HealthRecord(animalId = 0, type = "Deworming", date = today.minusDays(45), description = "Whole flock — Albendazole", notes = "Flock-wide deworming. Next due in 45 days.")
        )
    }

    private suspend fun seedReproductionRecords(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val reproDao = db.reproductionDao()
        
        // Pregnant doe - due in 5 days
        reproDao.insertReproductionRecord(
            ReproductionRecord(damId = 6, sireId = 11, type = "Pregnancy", matingDate = today.minusDays(145), pregnancyConfirmed = true, expectedDueDate = today.plusDays(5))
        )
        // Recent birth
        reproDao.insertReproductionRecord(
            ReproductionRecord(damId = 3, sireId = 11, type = "Birth", actualBirthDate = today.minusDays(60), numberOfKids = 2, numberOfAlive = 2, numberOfStillborn = 0)
        )
        // Heat detection yesterday
        reproDao.insertReproductionRecord(
            ReproductionRecord(damId = 5, type = "HeatDetection", matingDate = today.minusDays(1), notes = "Standing heat observed. Buck introduced.")
        )
    }

    private suspend fun seedMilkProduction(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val milkDao = db.milkProductionDao()
        
        // 30 days of milk data for lactating does (IDs 1-5)
        for (daysAgo in 0..30) {
            val date = today.minusDays(daysAgo.toLong())
            
            // G-1: Peak yields ~4.6L/day
            milkDao.insertMilkProduction(
                MilkProduction(animalId = 1, date = date, morningYield = 2.4 + Random.nextDouble() * 0.4, eveningYield = 2.1 + Random.nextDouble() * 0.3, totalYield = 4.6)
            )
            // G-2: Steady ~3.8L/day
            milkDao.insertMilkProduction(
                MilkProduction(animalId = 2, date = date, morningYield = 2.0 + Random.nextDouble() * 0.3, eveningYield = 1.7 + Random.nextDouble() * 0.3, totalYield = 3.8)
            )
            // G-3: Declining last 5 days
            val neemaYield = if (daysAgo < 5) 2.8 else (3.8 - daysAgo * 0.03)
            milkDao.insertMilkProduction(
                MilkProduction(animalId = 3, date = date, morningYield = neemaYield / 2, eveningYield = neemaYield / 2, totalYield = neemaYield)
            )
            // G-4: New lactation, moderate
            milkDao.insertMilkProduction(
                MilkProduction(animalId = 4, date = date, morningYield = 1.8 + Random.nextDouble() * 0.3, eveningYield = 1.6 + Random.nextDouble() * 0.3, totalYield = 3.5)
            )
            // G-5: Good yield
            milkDao.insertMilkProduction(
                MilkProduction(animalId = 5, date = date, morningYield = 2.2 + Random.nextDouble() * 0.3, eveningYield = 2.0 + Random.nextDouble() * 0.3, totalYield = 4.3)
            )
        }
    }

    private suspend fun seedWeightEntries(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val weightDao = db.weightDao()
        
        // 6 weight entries per key animal over 6 months
        val keyAnimals = listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L)
        for (animalId in keyAnimals) {
            for (i in 0..5) {
                val date = today.minusDays(i.toLong() * 30)
                val baseWeight = 35.0 + Random.nextDouble() * 20.0
                val trend = if (animalId == 7L && i < 3) -1.0 else 0.0 // G-14 declining
                weightDao.insertWeightEntry(
                    WeightEntry(animalId = animalId, date = date, weight = baseWeight + i * trend)
                )
            }
        }
    }

    private suspend fun seedSilageInventory(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val silageDao = db.silageDao()
        
        silageDao.insertSilageInventory(
            SilageInventory(
                pitId = "Pit-1",
                fillDate = today.minusDays(90),
                cropType = "Maize Silage",
                estimatedTonnage = 12.0,
                currentTonnage = 4.2,
                fermentationComplete = true,
                qualityNotes = "Good fermentation. At 230kg/day: ~18 days remaining."
            )
        )
    }

    private suspend fun seedCheeseBatches(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cheeseDao = db.cheeseDao()
        
        // Batch 1 - Aging, 5 of 7 days
        cheeseDao.insertCheeseBatch(
            CheeseBatch(
                batchId = "CB-07",
                productionDate = today.minusDays(5),
                milkVolumeUsed = 20.0,
                cheeseType = "Fresh Chèvre",
                yieldKg = 3.8,
                agingStartDate = today.minusDays(5),
                status = "aging",
                milkCostTzs = 16000L,
                cultureCostTzs = 1200L,
                rennetCostTzs = 800L,
                packagingCostTzs = 1500L,
                otherInputCostTzs = 500L
            )
        )
        // Batch 2 - Aging semi-hard, 12 of 21 days
        cheeseDao.insertCheeseBatch(
            CheeseBatch(
                batchId = "CB-06",
                productionDate = today.minusDays(12),
                milkVolumeUsed = 30.0,
                cheeseType = "Feta-style",
                yieldKg = 5.2,
                agingStartDate = today.minusDays(12),
                status = "aging",
                milkCostTzs = 24000L,
                cultureCostTzs = 2000L,
                rennetCostTzs = 1200L,
                packagingCostTzs = 2200L,
                otherInputCostTzs = 800L
            )
        )
        // Batch 3 - Ready to package
        cheeseDao.insertCheeseBatch(
            CheeseBatch(
                batchId = "CB-05",
                productionDate = today.minusDays(8),
                milkVolumeUsed = 25.0,
                cheeseType = "Fresh Chèvre",
                yieldKg = 4.8,
                agingStartDate = today.minusDays(8),
                packagingDate = today,
                status = "ready",
                milkCostTzs = 20000L,
                cultureCostTzs = 1500L,
                rennetCostTzs = 1000L,
                packagingCostTzs = 1800L,
                otherInputCostTzs = 600L
            )
        )
        // Batch 4 - Sold
        cheeseDao.insertCheeseBatch(
            CheeseBatch(
                batchId = "CB-04",
                productionDate = today.minusDays(20),
                milkVolumeUsed = 18.0,
                cheeseType = "Fresh Chèvre",
                yieldKg = 3.4,
                agingStartDate = today.minusDays(20),
                status = "sold",
                salePriceTzsPerKg = 15000L,
                quantitySoldKg = 3.4f,
                saleDate = today.minusDays(12).toEpochDays().toLong() * 86400000L,
                milkCostTzs = 14400L,
                cultureCostTzs = 1000L,
                rennetCostTzs = 700L,
                packagingCostTzs = 1200L,
                otherInputCostTzs = 400L
            )
        )
    }

    private suspend fun seedFeedInventory(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val feedDao = db.feedDao()
        
        feedDao.insertFeedInventory(
            FeedInventory(feedType = "Silage (Maize)", stockLevel = 4200.0, unit = "kg", reorderThreshold = 6000.0, costPerUnit = 0.0)
        )
        feedDao.insertFeedInventory(
            FeedInventory(feedType = "Napier Grass (Fresh)", stockLevel = 680.0, unit = "kg", reorderThreshold = 200.0, costPerUnit = 0.0)
        )
        feedDao.insertFeedInventory(
            FeedInventory(feedType = "Dairy Meal (Concentrate)", stockLevel = 120.0, unit = "kg", reorderThreshold = 50.0, costPerUnit = 850.0)
        )
        feedDao.insertFeedInventory(
            FeedInventory(feedType = "Mineral Supplement", stockLevel = 25.0, unit = "kg", reorderThreshold = 10.0, costPerUnit = 4500.0)
        )
        feedDao.insertFeedInventory(
            FeedInventory(feedType = "Hay (Backup)", stockLevel = 40.0, unit = "kg", reorderThreshold = 100.0, costPerUnit = 200.0)
        )
        feedDao.insertFeedInventory(
            FeedInventory(feedType = "Salt Lick Blocks", stockLevel = 4.0, unit = "blocks", reorderThreshold = 2.0, costPerUnit = 3500.0)
        )
    }

    private suspend fun seedStoreItems(db: ShambaDatabase) {
        val storeDao = db.storeDao()
        
        storeDao.insertStoreItem(
            StoreItem(name = "Cheese packaging", category = "Packaging", quantity = 45.0, unit = "units", costPerUnit = 300.0)
        )
        storeDao.insertStoreItem(
            StoreItem(name = "Oxytetracycline LA", category = "Veterinary", quantity = 2.0, unit = "bottles", costPerUnit = 11000.0)
        )
        storeDao.insertStoreItem(
            StoreItem(name = "DAP Fertilizer", category = "Crop inputs", quantity = 30.0, unit = "kg", costPerUnit = 370.0)
        )
        storeDao.insertStoreItem(
            StoreItem(name = "CAN Fertilizer", category = "Crop inputs", quantity = 20.0, unit = "kg", costPerUnit = 450.0)
        )
        storeDao.insertStoreItem(
            StoreItem(name = "Dithane M-45", category = "Pesticide", quantity = 3.0, unit = "kg", costPerUnit = 5000.0)
        )
    }

    private suspend fun seedIncome(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val financialDao = db.financialDao()
        
        // Current month income
        financialDao.insertIncome(Income(date = today.minusDays(2), category = "Cheese sales", description = "4 batches — Fresh Chèvre to Korogwe market", amount = 184000.0))
        financialDao.insertIncome(Income(date = today.minusDays(5), category = "Milk sales", description = "Surplus milk — 120L to Tanga Dairy Co-op", amount = 96000.0))
        financialDao.insertIncome(Income(date = today.minusDays(8), category = "Live animal sales", description = "2 male goats — Korogwe livestock market", amount = 80000.0))
        financialDao.insertIncome(Income(date = today.minusDays(12), category = "Vegetable sales", description = "Kale harvest — 180kg to Korogwe market", amount = 38000.0))
        financialDao.insertIncome(Income(date = today.minusDays(15), category = "Manure sales", description = "2 truck loads — local vegetable farmers", amount = 14000.0))
        
        // Previous month
        financialDao.insertIncome(Income(date = today.minusDays(35), category = "Milk sales", description = "150L to Tanga Dairy Co-op", amount = 120000.0))
        financialDao.insertIncome(Income(date = today.minusDays(40), category = "Cheese sales", description = "3 batches", amount = 135000.0))
        financialDao.insertIncome(Income(date = today.minusDays(45), category = "Vegetable sales", description = "Tomatoes", amount = 45000.0))
    }

    private suspend fun seedExpenses(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val financialDao = db.financialDao()
        
        // Current month expenses
        financialDao.insertExpense(Expense(date = today.minusDays(1), category = "Labour", description = "Monthly wages — 4 workers", amount = 80000.0))
        financialDao.insertExpense(Expense(date = today.minusDays(3), category = "Feed", description = "Dairy meal 100kg — Korogwe Agrovet", amount = 85000.0))
        financialDao.insertExpense(Expense(date = today.minusDays(6), category = "Veterinary & medicine", description = "Vet visit — G-14 consultation + Oxytetracycline", amount = 22000.0))
        financialDao.insertExpense(Expense(date = today.minusDays(9), category = "Seeds & fertiliser", description = "DAP 50kg — Plot A top dressing", amount = 18500.0))
        financialDao.insertExpense(Expense(date = today.minusDays(11), category = "Cheese inputs", description = "Rennet + mesophilic cultures", amount = 14000.0))
        financialDao.insertExpense(Expense(date = today.minusDays(14), category = "Fuel & transport", description = "Market trips + generator fuel", amount = 9500.0))
        financialDao.insertExpense(Expense(date = today.minusDays(18), category = "Packaging", description = "Cheese packaging materials — 50 units", amount = 7000.0))
    }

    private suspend fun seedLoans(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val loanDao = db.loanDao()
        
        loanDao.insertLoan(
            Loan(
                lenderName = "CRDB Bank Korogwe",
                amount = 500000.0,
                disbursementDate = today.minusMonths(6),
                interestRate = 18.0,
                totalRepaid = 250000.0,
                balance = 250000.0,
                dueDate = today.plusMonths(6),
                status = "active"
            )
        )
    }

    private suspend fun seedWorkers(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val workerDao = db.workerDao()
        
        workerDao.insertWorker(Worker(name = "Amina Juma", role = "Milking & livestock", contact = "+255 712 345 678", hireDate = today.minusYears(2), dailyRate = 2500.0, status = "active"))
        workerDao.insertWorker(Worker(name = "Joseph Mwanga", role = "Crops & fencing", contact = "+255 754 987 654", hireDate = today.minusYears(1), dailyRate = 2000.0, status = "active"))
        workerDao.insertWorker(Worker(name = "Moses Kilima", role = "Crops & general", contact = "+255 768 111 222", hireDate = today.minusMonths(8), dailyRate = 2000.0, status = "active"))
        workerDao.insertWorker(Worker(name = "Fatuma Said", role = "Casual — cheese room", contact = "+255 745 333 444", hireDate = today.minusDays(10), dailyRate = 2500.0, isSeasonal = true, status = "active"))
    }

    private suspend fun seedAttendanceRecords(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        // Attendance seeding will be done when attendance DAO is available
    }

    private suspend fun seedTasks(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val taskDao = db.taskDao()
        
        // Completed today
        taskDao.insertTask(Task(title = "Morning milk collection — all does", assignedTo = 1, dueDate = today, status = "completed"))
        // Pending high priority
        taskDao.insertTask(Task(title = "Deworm sheep flock (Group B)", assignedTo = 2, dueDate = today, status = "pending"))
        // Pending medium
        taskDao.insertTask(Task(title = "Apply CAN top dressing — Plot A maize", assignedTo = 3, dueDate = today, status = "pending"))
        taskDao.insertTask(Task(title = "Record weights — newborn kid", assignedTo = 1, dueDate = today, status = "pending"))
        taskDao.insertTask(Task(title = "Check east perimeter fence", assignedTo = 2, dueDate = today, status = "pending"))
        // Evening
        taskDao.insertTask(Task(title = "Evening milk collection + log yield", assignedTo = 1, dueDate = today, status = "pending"))
        // Tomorrow
        taskDao.insertTask(Task(title = "Spray Plot D tomatoes — Dithane", assignedTo = 3, dueDate = today.plusDays(1), status = "pending"))
        // Kidding prep in 5 days
        taskDao.insertTask(Task(title = "Prepare kidding pen — G-22 Tumaini due", assignedTo = 1, dueDate = today.plusDays(5), status = "pending"))
        // Overdue
        taskDao.insertTask(Task(title = "Log feed inventory — daily silage draw-down", assignedTo = 2, dueDate = today.minusDays(1), status = "pending"))
    }

    private suspend fun seedCalendarEvents(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val calendarDao = db.calendarDao()
        
        calendarDao.insertCalendarEvent(CalendarEvent(title = "Maize planting — Plot A", date = today.minusDays(55), type = "Planting"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "Cheese batch CB-06 started", date = today.minusDays(12), type = "Cheese"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "Korogwe livestock market", date = today.plusDays(2), type = "Market"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "G-22 Tumaini — expected to kid", date = today.plusDays(5), type = "Reproduction"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "Sheep deworming — Group A due", date = today.plusDays(7), type = "Health"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "Tomato harvest window opens", date = today.plusDays(8), type = "Harvest"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "G-01 Zawadi — PPR vaccination due", date = today.plusDays(3), type = "Vaccination"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "Kale harvest — Plot E", date = today, type = "Harvest"))
        calendarDao.insertCalendarEvent(CalendarEvent(title = "CRDB loan repayment due", date = today.plusDays(15), type = "Finance"))
    }

    private suspend fun seedWeatherLogs(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val weatherDao = db.weatherDao()
        
        for (daysAgo in 1..14) {
            val date = today.minusDays(daysAgo.toLong())
            val rainfall = when {
                daysAgo in 3..5 -> 18.0 + Random.nextDouble() * 8.0
                daysAgo == 8 -> 34.0
                else -> Random.nextDouble() * 4.0
            }
            weatherDao.insertWeatherLog(
                WeatherLog(
                    date = date,
                    rainfallMm = rainfall,
                    maxTemp = 30.0 + Random.nextDouble() * 3.0,
                    minTemp = 22.0 + Random.nextDouble() * 2.0
                )
            )
        }
    }

    private suspend fun seedMaintenanceTasks(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val maintenanceDao = db.maintenanceTaskDao()
        
        // Overdue
        maintenanceDao.insertMaintenanceTask(
            MaintenanceTask(
                title = "Generator monthly service",
                description = "Oil check and general service",
                type = MaintenanceType.EQUIPMENT_SERVICING,
                priority = MaintenancePriority.HIGH,
                scheduledDate = today.minusDays(5).toEpochDays().toLong() * 86400000L,
                status = MaintenanceStatus.OVERDUE
            )
        )
        // Due next week
        maintenanceDao.insertMaintenanceTask(
            MaintenanceTask(
                title = "Farm pickup oil change",
                description = "Oil change and tyre pressure check",
                type = MaintenanceType.VEHICLE_MAINTENANCE,
                priority = MaintenancePriority.MEDIUM,
                scheduledDate = today.plusDays(8).toEpochDays().toLong() * 86400000L,
                status = MaintenanceStatus.SCHEDULED
            )
        )
        // Completed
        maintenanceDao.insertMaintenanceTask(
            MaintenanceTask(
                title = "Water pump inspection",
                description = "Impeller inspection and belt replacement",
                type = MaintenanceType.WATER_SYSTEM_MAINTENANCE,
                priority = MaintenancePriority.MEDIUM,
                scheduledDate = today.minusDays(15).toEpochDays().toLong() * 86400000L,
                completedDate = today.minusDays(14).toEpochDays().toLong() * 86400000L,
                status = MaintenanceStatus.COMPLETED
            )
        )
    }

    private suspend fun seedVehicles(db: ShambaDatabase) {
        // No VehicleDao exists in database — skip
    }

    private suspend fun seedMapMarkers(db: ShambaDatabase) {
        val markerDao = db.mapMarkerDao()
        
        markerDao.insertMarker(
            MapMarkerEntity(name = "Main goat shed", markerType = "Shelter", category = "Infrastructure", latitude = -5.150, longitude = 38.478, icon = "shelter", color = "#0F3320", description = "Capacity 40 animals")
        )
        markerDao.insertMarker(
            MapMarkerEntity(name = "Sheep pen", markerType = "Shelter", category = "Infrastructure", latitude = -5.153, longitude = 38.479, icon = "shelter", color = "#0F3320", description = "Capacity 30 animals")
        )
        markerDao.insertMarker(
            MapMarkerEntity(name = "Main water trough", markerType = "Water", category = "Water", latitude = -5.151, longitude = 38.480, icon = "water", color = "#0D6B62", description = "Fed from borehole")
        )
        markerDao.insertMarker(
            MapMarkerEntity(name = "Feed store", markerType = "Storage", category = "Infrastructure", latitude = -5.148, longitude = 38.477, icon = "storage", color = "#7A3F0D", description = "Silage pit, hay store, concentrate bags")
        )
        markerDao.insertMarker(
            MapMarkerEntity(name = "Cheese production room", markerType = "Cheese", category = "Infrastructure", latitude = -5.152, longitude = 38.481, icon = "cheese", color = "#D4751F", description = "20m² with cold storage")
        )
        markerDao.insertMarker(
            MapMarkerEntity(name = "Compost pit 1", markerType = "Compost", category = "Waste", latitude = -5.154, longitude = 38.482, icon = "compost", color = "#4A2508", description = "Active — manure composting")
        )
        markerDao.insertMarker(
            MapMarkerEntity(name = "Irrigation point — Plot D", markerType = "Water", category = "Water", latitude = -5.149, longitude = 38.483, icon = "water", color = "#0D6B62", description = "Manual pump connection")
        )
    }

    private suspend fun seedScoutingReports(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val scoutingDao = db.scoutingReportDao()
        
        // Critical FAW on Plot A
        scoutingDao.insertReport(
            ScoutingReport(plotId = 1, pestType = "Fall Armyworm", severityScore = 5, gpsLatitude = -5.148, gpsLongitude = 38.479, notes = "Heavy infestation in whorl. Frass visible. ~40% of plants affected.", detectedAt = today.minusDays(1).toEpochDays().toLong() * 86400000L)
        )
        // Moderate aphids on Plot C
        scoutingDao.insertReport(
            ScoutingReport(plotId = 3, pestType = "Aphids", severityScore = 3, gpsLatitude = -5.153, gpsLongitude = 38.477, notes = "Aphid colonies on bean leaves. Moderate infestation.", detectedAt = today.minusDays(3).toEpochDays().toLong() * 86400000L)
        )
        // Low stalk borer
        scoutingDao.insertReport(
            ScoutingReport(plotId = 1, pestType = "Maize Stalk Borer", severityScore = 2, gpsLatitude = -5.148, gpsLongitude = 38.480, notes = "Few plants affected. Monitoring.", detectedAt = today.minusDays(14).toEpochDays().toLong() * 86400000L)
        )
        // Moderate leafminer on Plot D
        scoutingDao.insertReport(
            ScoutingReport(plotId = 4, pestType = "Leafminer", severityScore = 3, gpsLatitude = -5.155, gpsLongitude = 38.481, notes = "Tunnels visible on tomato leaves.", detectedAt = today.minusDays(2).toEpochDays().toLong() * 86400000L)
        )
    }

    private suspend fun seedIngestedDocuments(db: ShambaDatabase) {
        val docDao = db.ingestedDocumentDao()
        val now = System.currentTimeMillis()
        
        docDao.insert(IngestedDocument(
            id = "doc_demo_001",
            title = "FAW Management Guide — Tanzania",
            domainTag = "pests",
            sourceCredibility = "government_research",
            chunkCount = 8,
            dateIngested = now,
            processingStatus = "complete"
        ))
        docDao.insert(IngestedDocument(
            id = "doc_demo_002",
            title = "CCPP Prevention and Treatment",
            domainTag = "livestock",
            sourceCredibility = "government_research",
            chunkCount = 6,
            dateIngested = now,
            processingStatus = "complete"
        ))
        docDao.insert(IngestedDocument(
            id = "doc_demo_003",
            title = "Fresh Chèvre Production Guide",
            domainTag = "cheese",
            sourceCredibility = "extension_bulletin",
            chunkCount = 5,
            dateIngested = now,
            processingStatus = "complete"
        ))
        docDao.insert(IngestedDocument(
            id = "doc_demo_004",
            title = "Goat Kidding Management",
            domainTag = "livestock",
            sourceCredibility = "academic",
            chunkCount = 4,
            dateIngested = now,
            processingStatus = "complete"
        ))
        docDao.insert(IngestedDocument(
            id = "doc_demo_005",
            title = "Maize Growth Stages — Coastal Tanzania",
            domainTag = "crops",
            sourceCredibility = "extension_bulletin",
            chunkCount = 7,
            dateIngested = now,
            processingStatus = "complete"
        ))
    }

    private suspend fun seedKnowledgeChunks(db: ShambaDatabase) {
        val chunkDao = db.knowledgeChunkDao()
        val now = System.currentTimeMillis()
        
        // FAW chunks
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_faw_001",
            displayText = "Fall Armyworm (Spodoptera frugiperda) is the most destructive pest of maize in Tanzania. Larvae feed on leaves, creating characteristic windowpane damage. Heavy infestations can cause 80-100% yield loss. Early detection through regular scouting (at least weekly) is critical for effective management.",
            embeddingText = "Fall Armyworm management in Tanzania. Spodoptera frugiperda identification and control.",
            sourceDocumentId = "doc_demo_001",
            sourceTitle = "FAW Management Guide — Tanzania",
            sourceType = "bundled",
            sourceCredibility = "government_research",
            domainTag = "pests",
            topicTags = "maize,fall_armyworm,pest_control",
            chunkIndex = 0,
            totalChunks = 8,
            keywords = "fall armyworm,maize,larvae,scouting",
            dateAdded = now
        ))
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_faw_002",
            displayText = "For Fall Armyworm control in maize at tasseling stage, apply Emamectin Benzoate (19g/ha) or Spinetoram (75ml/ha) when 20% of plants show damage. Apply in the evening when larvae are actively feeding. A second application may be needed if infestation persists after 7 days.",
            embeddingText = "FAW chemical control at tasseling. Emamectin Benzoate and Spinetoram application rates.",
            sourceDocumentId = "doc_demo_001",
            sourceTitle = "FAW Management Guide — Tanzania",
            sourceType = "bundled",
            sourceCredibility = "government_research",
            domainTag = "pests",
            topicTags = "maize,fall_armyworm,pesticide",
            chunkIndex = 1,
            totalChunks = 8,
            keywords = "emamectin,spinetoram,tasseling,treatment",
            dateAdded = now
        ))
        
        // CCPP chunks
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_ccpp_001",
            displayText = "Contagious Caprine Pleuropneumonia (CCPP) is a highly infectious respiratory disease of goats caused by Mycoplasma capricolum subsp. capripneumoniae. Clinical signs include nasal discharge, coughing, fever (>40.5°C), and laboured breathing. Mortality can reach 60-100% in naive flocks. Immediate isolation and veterinary confirmation are required.",
            embeddingText = "CCPP in goats. Mycoplasma capripneumoniae symptoms and diagnosis.",
            sourceDocumentId = "doc_demo_002",
            sourceTitle = "CCPP Prevention and Treatment",
            sourceType = "bundled",
            sourceCredibility = "government_research",
            domainTag = "livestock",
            topicTags = "goat,disease,respiratory,CCPP",
            chunkIndex = 0,
            totalChunks = 6,
            keywords = "CCPP,goat,respiratory,isolation,veterinary",
            dateAdded = now
        ))
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_ccpp_002",
            displayText = "Oxytetracycline LA (long-acting) is the first-line treatment for CCPP in goats. Dose: 20mg/kg body weight intramuscularly, repeated every 48 hours for 3-5 days. Milk withdrawal period is 7 days after the last injection. Meat withdrawal is 28 days. Report suspected CCPP cases to the nearest veterinary office — it is a notifiable disease in Tanzania.",
            embeddingText = "Oxytetracycline LA treatment for CCPP. Dosage, withdrawal periods, reporting requirements.",
            sourceDocumentId = "doc_demo_002",
            sourceTitle = "CCPP Prevention and Treatment",
            sourceType = "bundled",
            sourceCredibility = "government_research",
            domainTag = "livestock",
            topicTags = "goat,CCPP,treatment,withdrawal",
            chunkIndex = 1,
            totalChunks = 6,
            medicalContent = true,
            keywords = "oxytetracycline,CCPP,withdrawal,dose,TZS",
            dateAdded = now
        ))
        
        // Cheese chunks
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_cheese_001",
            displayText = "Fresh Chèvre (soft goat cheese) production: Use fresh goat milk within 2 hours of milking. Heat milk to 22°C. Add mesophilic culture (MA 4001 or equivalent) at 1U per 100L. Add liquid rennet (2ml per 100L) diluted in 10ml cool water. Stir gently for 1 minute. Allow to set for 16-24 hours at room temperature (20-24°C). Drain in muslin cloth for 6-12 hours. Season with salt (1% by weight) and optional herbs.",
            embeddingText = "Fresh Chèvre goat cheese recipe. Culture, rennet, temperature, timing, draining.",
            sourceDocumentId = "doc_demo_003",
            sourceTitle = "Fresh Chèvre Production Guide",
            sourceType = "bundled",
            sourceCredibility = "extension_bulletin",
            domainTag = "cheese",
            topicTags = "cheese,chevre,goat_milk,soft_cheese",
            chunkIndex = 0,
            totalChunks = 5,
            keywords = "chèvre,goat cheese,rennet,culture,draining",
            dateAdded = now
        ))
        
        // Kidding chunks
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_kidding_001",
            displayText = "Goat kidding preparation checklist (7 days before expected due date): 1) Clean and disinfect the kidding pen. 2) Prepare clean bedding (straw or hay). 3) Ensure colostrum plan is in place (either from dam or frozen colostrum). 4) Have iodine solution for navel dipping. 5) Prepare kidding kit: clean towels, lubricant, obstetric gloves, scissors, iodine. 6) Increase monitoring frequency to every 4-6 hours. 7) Confirm vet contact details for emergencies.",
            embeddingText = "Goat kidding preparation. Kidding pen, colostrum, monitoring, emergency contacts.",
            sourceDocumentId = "doc_demo_004",
            sourceTitle = "Goat Kidding Management",
            sourceType = "bundled",
            sourceCredibility = "academic",
            domainTag = "livestock",
            topicTags = "goat,kidding,preparation,birth",
            chunkIndex = 0,
            totalChunks = 4,
            keywords = "kidding,preparation,colostrum,pen,cleaning",
            dateAdded = now
        ))
        
        // Maize growth chunks
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_maize_001",
            displayText = "Maize tasseling stage (VT) in coastal Tanzania typically occurs 55-65 days after planting, depending on variety and rainfall. At this stage, the plant is most vulnerable to drought stress and pest attack. Key management: 1) Ensure adequate soil moisture through supplemental irrigation if available. 2) Apply top-dressing fertilizer (CAN) if not already done. 3) Scout for Fall Armyworm and stalk borer weekly. 4) Monitor for grey leaf spot if rainfall has been above average.",
            embeddingText = "Maize tasseling stage management. CAN fertilizer, FAW scouting, disease monitoring.",
            sourceDocumentId = "doc_demo_005",
            sourceTitle = "Maize Growth Stages — Coastal Tanzania",
            sourceType = "bundled",
            sourceCredibility = "extension_bulletin",
            domainTag = "crops",
            topicTags = "maize,tasseling,growth_stage,fertilizer",
            chunkIndex = 2,
            totalChunks = 7,
            keywords = "maize,tasseling,CAN,fertilizer,scouting",
            dateAdded = now
        ))
        chunkDao.insert(KnowledgeChunk(
            id = "chunk_maize_002",
            displayText = "Grey leaf spot (Cercospora zeae-maydis) risk increases when cumulative rainfall in the last 14 days exceeds 80mm and temperatures are above 25°C. Symptoms: rectangular grey to tan lesions on leaves, 2-5mm wide. Management: Apply Dithane M-45 (mancozeb) at 2.5kg/ha or Propiconazole at 500ml/ha at first sign of disease. Avoid late planting to reduce exposure. Rotate with non-cereal crops.",
            embeddingText = "Grey leaf spot risk assessment. Rainfall threshold, symptoms, fungicide application.",
            sourceDocumentId = "doc_demo_005",
            sourceTitle = "Maize Growth Stages — Coastal Tanzania",
            sourceType = "bundled",
            sourceCredibility = "extension_bulletin",
            domainTag = "crops",
            topicTags = "maize,grey_leaf_spot,disease,fungicide",
            chunkIndex = 3,
            totalChunks = 7,
            keywords = "grey leaf spot,dithane,mancozeb,rainfall,disease",
            dateAdded = now
        ))
    }

    private suspend fun seedOperationalRules(db: ShambaDatabase) {
        val ruleDao = db.operationalRuleDao()
        
        ruleDao.insert(OperationalRule(
            ruleId = "withdrawal_oxytet_la_goat",
            ruleType = "withdrawal_period",
            species = "goat,sheep",
            parametersJson = """{"milk_withdrawal_days":7,"meat_withdrawal_days":28}""",
            source = "Norbrook product data sheet 2023",
            lastVerified = "2025-01-15"
        ))
        ruleDao.insert(OperationalRule(
            ruleId = "gestation_goat",
            ruleType = "gestation",
            species = "goat",
            parametersJson = """{"gestation_days":150,"pre_event_task_days":7}""",
            source = "Veterinary textbook reference",
            lastVerified = "2024-06-01"
        ))
        ruleDao.insert(OperationalRule(
            ruleId = "gestation_sheep",
            ruleType = "gestation",
            species = "sheep",
            parametersJson = """{"gestation_days":147,"pre_event_task_days":7}""",
            source = "Veterinary textbook reference",
            lastVerified = "2024-06-01"
        ))
        ruleDao.insert(OperationalRule(
            ruleId = "planting_maize_korogwe",
            ruleType = "planting_window",
            crop = "maize",
            location = "korogwe",
            parametersJson = """{"start_month":3,"end_month":4}""",
            source = "Tanzania Meteorological Authority long rains forecast",
            lastVerified = "2025-02-01"
        ))
        ruleDao.insert(OperationalRule(
            ruleId = "notifiable_ccpp",
            ruleType = "notifiable_disease",
            species = "goat",
            parametersJson = """{"disease":"CCPP","reporting_body":"TVLA","action":"isolate_and_report"}""",
            source = "Tanzania Veterinary Laboratory Agency guidelines",
            lastVerified = "2024-09-01"
        ))
    }

    private suspend fun seedMilkCollections(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cheeseDao = db.cheeseDao()
        
        // Recent milk collections for cheese production
        for (daysAgo in 1..10) {
            val date = today.minusDays(daysAgo.toLong())
            cheeseDao.insertMilkCollection(
                MilkCollection(date = date, quantityLitres = 28.0 + Random.nextDouble() * 8.0, accepted = true, qualityCheck = "pass")
            )
        }
    }

    private suspend fun seedHarvestRecords(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val harvestDao = db.harvestDao()
        
        // Previous kale harvest from Plot E
        harvestDao.insertHarvestRecord(
            HarvestRecord(cropPlantingId = 5, harvestDate = today.minusDays(30), quantityKg = 180.0, qualityGrade = "A", destination = "Korogwe market", pricePerKg = 200.0)
        )
        // Previous maize harvest from Plot A
        harvestDao.insertHarvestRecord(
            HarvestRecord(cropPlantingId = 1, harvestDate = today.minusDays(120), quantityKg = 1200.0, qualityGrade = "A", destination = "Storage", pricePerKg = 0.0)
        )
    }

    private suspend fun seedCropInputs(db: ShambaDatabase) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cropDao = db.cropDao()
        
        // DAP applied to Plot A
        cropDao.insertCropInput(
            CropInput(plantingId = 1, inputType = "Fertilizer", productName = "DAP", quantity = 50.0, unit = "kg", cost = 18500.0, date = today.minusDays(20))
        )
        // CAN top dressing
        cropDao.insertCropInput(
            CropInput(plantingId = 1, inputType = "Fertilizer", productName = "CAN", quantity = 30.0, unit = "kg", cost = 13500.0, date = today.minusDays(5))
        )
    }
}