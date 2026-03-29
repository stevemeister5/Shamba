package com.shambasmart.demo

import com.shambasmart.data.local.ShambaDatabase
import com.shambasmart.data.local.entity.*
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DemoDataSeeder @Inject constructor() {
    
    suspend fun seedAll(database: ShambaDatabase) {
        seedFarmProfile(database)
        seedAnimals(database)
        seedHealthRecords(database)
        seedReproduction(database)
        seedMilkProduction(database)
        seedWeightEntries(database)
        seedPlots(database)
        seedCropPlantings(database)
        seedHarvestRecords(database)
        seedSilageInventory(database)
        seedWeatherLog(database)
        seedScoutingReports(database)
        seedMilkCollection(database)
        seedCheeseBatches(database)
        seedFeedInventory(database)
        seedStoreItems(database)
        seedFinancials(database)
        seedLoans(database)
        seedWorkers(database)
        seedAttendance(database)
        seedTasks(database)
        seedCalendarEvents(database)
        seedMaintenanceTasks(database)
        seedVehicles(database)
        seedMapData(database)
        seedMaarifaKnowledge(database)
        seedAlerts(database)
    }
    
    private suspend fun seedFarmProfile(database: ShambaDatabase) {
        // Seed farm profile
        val farm = Farm(
            id = 1,
            name = DemoFarm.FARM_NAME,
            ownerName = DemoFarm.OWNER_NAME,
            location = DemoFarm.LOCATION,
            sizeAcres = DemoFarm.SIZE_ACRES.toDouble(),
            latitude = DemoFarm.LATITUDE,
            longitude = DemoFarm.LONGITUDE,
            phone = DemoFarm.PHONE
        )
        database.farmDao().insertFarm(farm)
    }
    
    private suspend fun seedAnimals(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        // Explicitly defined key animals
        val keyAnimals = listOf(
            // Healthy lactating does
            Animal(tagId = "G-01", name = "Zawadi", species = "Goat", breed = "Toggenburg", 
                   sex = "Female", birthDate = today.minusYears(3), status = "Healthy", weight = 42.0),
            Animal(tagId = "G-02", name = "Baraka", species = "Goat", breed = "Saanen", 
                   sex = "Female", birthDate = today.minusYears(3), status = "Healthy", weight = 44.0),
            Animal(tagId = "G-03", name = "Neema", species = "Goat", breed = "Alpine", 
                   sex = "Female", birthDate = today.minusYears(4), status = "Healthy", weight = 46.0),
            Animal(tagId = "G-04", name = "Furaha", species = "Goat", breed = "Nubian", 
                   sex = "Female", birthDate = today.minusYears(2), status = "Healthy", weight = 38.0),
            Animal(tagId = "G-05", name = "Amani", species = "Goat", breed = "Toggenburg", 
                   sex = "Female", birthDate = today.minusYears(3), status = "Healthy", weight = 41.0),
            
            // Pregnant doe
            Animal(tagId = "G-22", name = "Tumaini", species = "Goat", breed = "Alpine", 
                   sex = "Female", birthDate = today.minusYears(4), status = "Pregnant", weight = 52.0),
            
            // Sick animal
            Animal(tagId = "G-14", name = "Imani", species = "Goat", breed = "Boer cross", 
                   sex = "Female", birthDate = today.minusYears(2), status = "Sick", weight = 34.0),
            
            // Dry doe
            Animal(tagId = "G-09", name = "Rehema", species = "Goat", breed = "Nubian", 
                   sex = "Female", birthDate = today.minusYears(5), status = "Dry", weight = 50.0),
            
            // Doe in withdrawal period
            Animal(tagId = "G-31", name = "Subira", species = "Goat", breed = "Saanen", 
                   sex = "Female", birthDate = today.minusYears(3), status = "Healthy", weight = 43.0),
            
            // Buck
            Animal(tagId = "G-B1", name = "Simba", species = "Goat", breed = "Toggenburg", 
                   sex = "Male", birthDate = today.minusYears(4), status = "Healthy", weight = 68.0),
            
            // Newborn kid
            Animal(tagId = "G-K1", name = null, species = "Goat", breed = "Alpine", 
                   sex = "Female", birthDate = today.minusDays(3), status = "Healthy", weight = 2.8)
        )
        
        // Insert key animals
        keyAnimals.forEach { animal ->
            database.animalDao().insertAnimal(animal)
        }
        
        // Generate remaining 51 goats
        val goatBreeds = listOf("Toggenburg", "Saanen", "Alpine", "Nubian", "Boer cross")
        val statuses = listOf("Healthy" to 0.75, "Pregnant" to 0.10, "Dry" to 0.05, "Sick" to 0.05, "Kids" to 0.05)
        
        for (i in 12..62) {
            val tagId = "G-${String.format("%02d", i)}"
            val breed = goatBreeds.random()
            val status = statuses.randomByWeight().first
            val ageMonths = Random.nextInt(6, 72)
            val birthDate = today.minusMonths(ageMonths)
            val weight = when (breed) {
                "Toggenburg" -> Random.nextDouble(38.0, 50.0)
                "Saanen" -> Random.nextDouble(40.0, 52.0)
                "Alpine" -> Random.nextDouble(36.0, 48.0)
                "Nubian" -> Random.nextDouble(35.0, 47.0)
                "Boer cross" -> Random.nextDouble(32.0, 45.0)
                else -> Random.nextDouble(35.0, 48.0)
            }
            
            val animal = Animal(
                tagId = tagId,
                name = if (status != "Kids") "Goat $i" else null,
                species = "Goat",
                breed = breed,
                sex = if (Random.nextBoolean()) "Female" else "Male",
                birthDate = birthDate,
                status = status,
                weight = weight
            )
            database.animalDao().insertAnimal(animal)
        }
        
        // Generate 25 sheep
        val sheepBreeds = listOf("Dorper", "Blackhead Persian", "Red Maasai", "Fat-tailed")
        for (i in 1..25) {
            val tagId = "S-${String.format("%02d", i)}"
            val breed = sheepBreeds.random()
            val status = statuses.randomByWeight().first
            val ageMonths = Random.nextInt(6, 72)
            val birthDate = today.minusMonths(ageMonths)
            val weight = Random.nextDouble(35.0, 60.0)
            
            val animal = Animal(
                tagId = tagId,
                name = "Sheep $i",
                species = "Sheep",
                breed = breed,
                sex = if (Random.nextBoolean()) "Female" else "Male",
                birthDate = birthDate,
                status = status,
                weight = weight
            )
            database.animalDao().insertAnimal(animal)
        }
    }
    
    private fun List<Pair<String, Double>>.randomByWeight(): Pair<String, Double> {
        val random = Random.nextDouble()
        var cumulative = 0.0
        for ((item, weight) in this) {
            cumulative += weight
            if (random <= cumulative) return item to weight
        }
        return last()
    }
    
    private suspend fun seedHealthRecords(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        // Health records for key animals
        val healthRecords = listOf(
            // G-01 Zawadi - vaccination due in 3 days
            HealthRecord(
                animalId = "G-01",
                type = "Vaccination",
                date = today.minusDays(180),
                vaccineName = "PPR Vaccine",
                nextDueDate = today.plusDays(3),
                notes = "Annual booster"
            ),
            
            // G-14 Imani - vaccination OVERDUE by 5 days
            HealthRecord(
                animalId = "G-14",
                type = "Vaccination",
                date = today.minusDays(185),
                vaccineName = "Brucellosis",
                nextDueDate = today.minusDays(5)
            ),
            
            // G-31 Subira - treatment with withdrawal period
            HealthRecord(
                animalId = "G-31",
                type = "Treatment",
                date = today.minusDays(4),
                drugName = "Oxytetracycline LA",
                dose = "8ml IM",
                milkWithdrawalEndDate = today.plusDays(3),
                meatWithdrawalEndDate = today.plusDays(24)
            ),
            
            // G-14 Imani - sick animal
            HealthRecord(
                animalId = "G-14",
                type = "Illness",
                date = today.minusDays(2),
                symptoms = "Nasal discharge, reduced appetite, lethargy",
                diagnosis = "Suspected CCPP — pending vet confirmation",
                treatment = "Oxytetracycline LA 8ml IM. Isolate from herd."
            ),
            
            // Deworming records for whole flock
            HealthRecord(
                animalId = "G-01",
                type = "Deworming",
                date = today.minusDays(45),
                drugName = "Albendazole",
                notes = "Flock deworming - all goats"
            )
        )
        
        healthRecords.forEach { record ->
            database.healthDao().insertHealthRecord(record)
        }
        
        // Additional health records for other animals
        val allAnimals = database.animalDao().getAllAnimalsSync()
        allAnimals.filter { it.tagId != "G-01" && it.tagId != "G-14" && it.tagId != "G-31" }.forEach { animal ->
            // 1-2 health records per generated animal
            val numRecords = Random.nextInt(1, 3)
            for (i in 1..numRecords) {
                val record = HealthRecord(
                    animalId = animal.tagId!!,
                    type = listOf("Vaccination", "Deworming", "Checkup").random(),
                    date = today.minusDays(Random.nextInt(30, 180).toLong()),
                    notes = "Routine health maintenance"
                )
                database.healthDao().insertHealthRecord(record)
            }
        }
    }
    
    private suspend fun seedReproduction(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val reproductionRecords = listOf(
            // G-22 Tumaini - pregnant, due in 5 days
            ReproductionRecord(
                animalId = "G-22",
                type = "Pregnancy",
                matingDate = today.minusDays(145),
                sireId = "G-B1",
                pregnancyStatus = "Confirmed",
                expectedDueDate = today.plusDays(5)
            ),
            
            // G-03 Neema - kidded 60 days ago
            ReproductionRecord(
                animalId = "G-03",
                type = "Birth",
                matingDate = today.minusDays(210),
                actualBirthDate = today.minusDays(60),
                kidsCount = 2,
                kidsAlive = 2,
                kidsStillborn = 0
            ),
            
            // G-05 Amani - heat signs logged yesterday
            ReproductionRecord(
                animalId = "G-05",
                type = "HeatDetection",
                date = today.minusDays(1),
                notes = "Standing heat observed. Buck introduced."
            )
        )
        
        reproductionRecords.forEach { record ->
            database.reproductionDao().insertReproductionRecord(record)
        }
    }
    
    private suspend fun seedMilkProduction(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val lactatingDoes = listOf("G-01", "G-02", "G-03", "G-04", "G-05", "G-31")
        
        lactatingDoes.forEach { animalId ->
            // Seed 30 days of milk production
            for (daysAgo in 0..30) {
                val date = today.minusDays(daysAgo.toLong())
                val (morningYield, eveningYield) = when (animalId) {
                    "G-01" -> { // Zawadi - peak yields, upward trend
                        val baseMorning = 2.4 + (30 - daysAgo) * 0.02
                        val baseEvening = 2.1 + (30 - daysAgo) * 0.015
                        Pair(
                            baseMorning + Random.nextDouble(-0.2, 0.3),
                            baseEvening + Random.nextDouble(-0.15, 0.2)
                        )
                    }
                    "G-03" -> { // Neema - declining trend (last 5 days)
                        if (daysAgo <= 5) {
                            Pair(2.8 - (5 - daysAgo) * 0.15, 2.5 - (5 - daysAgo) * 0.12)
                        } else {
                            Pair(2.8 + Random.nextDouble(-0.2, 0.2), 2.5 + Random.nextDouble(-0.15, 0.15))
                        }
                    }
                    "G-14" -> { // Imani - zero yield for last 2 days
                        if (daysAgo <= 2) Pair(0.0, 0.0)
                        else Pair(2.2 + Random.nextDouble(-0.2, 0.2), 1.9 + Random.nextDouble(-0.15, 0.15))
                    }
                    else -> { // Normal does
                        Pair(
                            2.0 + Random.nextDouble(-0.3, 0.4),
                            1.8 + Random.nextDouble(-0.2, 0.3)
                        )
                    }
                }
                
                val record = MilkProduction(
                    animalId = animalId,
                    date = date,
                    morningYield = morningYield,
                    eveningYield = eveningYield,
                    notes = when {
                        animalId == "G-14" && daysAgo <= 2 -> "Animal sick - no milk"
                        animalId == "G-31" && daysAgo <= 3 -> "Withdrawal period - milk blocked"
                        else -> null
                    }
                )
                database.milkDao().insertMilkProduction(record)
            }
        }
    }
    
    private suspend fun seedWeightEntries(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val allAnimals = database.animalDao().getAllAnimalsSync()
        
        allAnimals.forEach { animal ->
            // 6 weight entries over 6 months
            val baseWeight = animal.weight ?: 40.0
            for (i in 0..5) {
                val date = today.minusMonths(i.toLong())
                val weight = if (animal.tagId == "G-14" && i <= 2) {
                    // Declining weight for Imani
                    baseWeight - (3 - i) * 1.5
                } else {
                    baseWeight + Random.nextDouble(-2.0, 2.0)
                }
                
                val entry = WeightEntry(
                    animalId = animal.tagId!!,
                    date = date,
                    weightKg = weight
                )
                database.weightDao().insertWeightEntry(entry)
            }
        }
    }
    
    private suspend fun seedPlots(database: ShambaDatabase) {
        val plots = listOf(
            Plot(name = "Plot A", sizeAcres = 3.0, soilType = "Clay loam", 
                 currentUse = "Crop", irrigationType = "Rain-fed", 
                 latitude = -5.148, longitude = 38.479),
            Plot(name = "Plot B", sizeAcres = 2.5, soilType = "Loam", 
                 currentUse = "Silage", irrigationType = "Rain-fed", 
                 latitude = -5.151, longitude = 38.482),
            Plot(name = "Plot C", sizeAcres = 2.0, soilType = "Loam", 
                 currentUse = "Crop", irrigationType = "Rain-fed", 
                 latitude = -5.153, longitude = 38.477),
            Plot(name = "Plot D", sizeAcres = 1.5, soilType = "Sandy loam", 
                 currentUse = "Crop", irrigationType = "Manual", 
                 latitude = -5.155, longitude = 38.481),
            Plot(name = "Plot E", sizeAcres = 1.5, soilType = "Clay loam", 
                 currentUse = "Crop", irrigationType = "Rain-fed", 
                 latitude = -5.149, longitude = 38.484),
            Plot(name = "Plot F", sizeAcres = 2.0, soilType = "Loam", 
                 currentUse = "Crop", irrigationType = "Rain-fed", 
                 latitude = -5.157, longitude = 38.476),
            Plot(name = "Plot G", sizeAcres = 1.0, soilType = "Sandy loam", 
                 currentUse = "Crop", irrigationType = "Manual", 
                 latitude = -5.152, longitude = 38.485),
            Plot(name = "Plot H", sizeAcres = 2.5, soilType = "Clay", 
                 currentUse = "Pasture", irrigationType = "Rain-fed", 
                 latitude = -5.146, longitude = 38.480)
        )
        
        plots.forEach { plot ->
            database.plotDao().insertPlot(plot)
        }
    }
    
    private suspend fun seedCropPlantings(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val plots = database.plotDao().getAllPlotsSync()
        
        val plantings = listOf(
            // Plot A - Silage Maize, tasseling stage
            CropPlanting(plotId = plots[0].id, cropType = "Maize", variety = "SEEDCO SC403",
                        plantingDate = today.minusDays(55), status = "Active"),
            
            // Plot B - Napier Grass, mature
            CropPlanting(plotId = plots[1].id, cropType = "Napier Grass", variety = "Clone 13",
                        plantingDate = today.minusDays(60), status = "Active"),
            
            // Plot C - Beans intercropped with maize
            CropPlanting(plotId = plots[2].id, cropType = "Beans", variety = "Lyamungu 85",
                        plantingDate = today.minusDays(45), status = "Active"),
            
            // Plot D - Tomatoes
            CropPlanting(plotId = plots[3].id, cropType = "Tomatoes", variety = "Cal-J",
                        plantingDate = today.minusDays(75), status = "Active"),
            
            // Plot E - Kale
            CropPlanting(plotId = plots[4].id, cropType = "Kale", variety = "Sukuma Wiki",
                        plantingDate = today.minusDays(55), status = "Active"),
            
            // Plot F - Cassava
            CropPlanting(plotId = plots[5].id, cropType = "Cassava", variety = "Kiroba",
                        plantingDate = today.minusDays(30), status = "Active"),
            
            // Plot G - Onions
            CropPlanting(plotId = plots[6].id, cropType = "Onion", variety = "Red Pinoy",
                        plantingDate = today.minusDays(14), status = "Active")
        )
        
        plantings.forEach { planting ->
            database.cropDao().insertCropPlanting(planting)
        }
    }
    
    private suspend fun seedHarvestRecords(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val plots = database.plotDao().getAllPlotsSync()
        
        val harvests = listOf(
            HarvestRecord(plotId = plots[4].id, cropType = "Kale", 
                         harvestDate = today.minusDays(60), quantityKg = 180.0,
                         notes = "Previous kale harvest"),
            HarvestRecord(plotId = plots[0].id, cropType = "Maize", 
                         harvestDate = today.minusDays(120), quantityKg = 1200.0,
                         notes = "Previous maize season")
        )
        
        harvests.forEach { harvest ->
            database.cropDao().insertHarvestRecord(harvest)
        }
    }
    
    private suspend fun seedSilageInventory(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val silage = SilageInventory(
            pitLocation = "Pit 1 — Main silage pit",
            cropType = "Maize Silage",
            fillDate = today.minusDays(90),
            estimatedTonnage = 12.0,
            currentQuantityTonnes = 4.2,
            fermentationDays = 21,
            quality = "Good",
            dailyDrawdownKg = 230.0
        )
        
        database.feedDao().insertSilageInventory(silage)
    }
    
    private suspend fun seedWeatherLog(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        // 14 days of historical weather
        for (daysAgo in 1..14) {
            val date = today.minusDays(daysAgo.toLong())
            val rainfallMm = when {
                daysAgo in 3..5 -> 18.0 + Random.nextDouble() * 8.0
                daysAgo == 8 -> 34.0
                else -> Random.nextDouble() * 4.0
            }
            
            val weather = WeatherLog(
                date = date,
                rainfallMm = rainfallMm,
                maxTemp = 30.0 + Random.nextDouble() * 3.0,
                minTemp = 22.0 + Random.nextDouble() * 2.0,
                humidity = 65.0 + Random.nextDouble() * 15.0,
                windSpeed = 8.0 + Random.nextDouble() * 6.0
            )
            
            database.weatherDao().insertWeatherLog(weather)
        }
        
        // 5-day forecast
        val forecasts = listOf(
            WeatherForecast(date = today.plusDays(1), maxTemp = 31.0, minTemp = 23.0,
                           condition = "Partly cloudy", rainfallMm = 2.0),
            WeatherForecast(date = today.plusDays(2), maxTemp = 29.0, minTemp = 22.0,
                           condition = "Cloudy", rainfallMm = 8.0),
            WeatherForecast(date = today.plusDays(3), maxTemp = 27.0, minTemp = 21.0,
                           condition = "Rain", rainfallMm = 22.0),
            WeatherForecast(date = today.plusDays(4), maxTemp = 26.0, minTemp = 21.0,
                           condition = "Heavy rain", rainfallMm = 35.0),
            WeatherForecast(date = today.plusDays(5), maxTemp = 29.0, minTemp = 22.0,
                           condition = "Partly cloudy", rainfallMm = 6.0)
        )
        
        forecasts.forEach { forecast ->
            database.weatherDao().insertWeatherForecast(forecast)
        }
    }
    
    private suspend fun seedScoutingReports(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val plots = database.plotDao().getAllPlotsSync()
        
        val reports = listOf(
            // Critical FAW on Plot A
            ScoutingReport(plotId = plots[0].id, pestType = "Fall Armyworm",
                          severityScore = 0.85f, severity = "Critical",
                          gpsLatitude = -5.148, gpsLongitude = 38.479,
                          detectedAt = today.minusDays(1).atStartOfDay(),
                          notes = "Heavy infestation in whorl. Frass visible. ~40% of plants affected."),
            
            // Moderate aphids on Plot C
            ScoutingReport(plotId = plots[2].id, pestType = "Aphids",
                          severityScore = 0.45f, severity = "Moderate",
                          gpsLatitude = -5.153, gpsLongitude = 38.477,
                          detectedAt = today.minusDays(3).atStartOfDay()),
            
            // Low stalk borer on Plot A
            ScoutingReport(plotId = plots[0].id, pestType = "Maize Stalk Borer",
                          severityScore = 0.2f, severity = "Low",
                          gpsLatitude = -5.148, gpsLongitude = 38.480,
                          detectedAt = today.minusDays(14).atStartOfDay()),
            
            // Moderate leafminer on Plot D
            ScoutingReport(plotId = plots[3].id, pestType = "Leafminer",
                          severityScore = 0.55f, severity = "Moderate",
                          gpsLatitude = -5.155, gpsLongitude = 38.481,
                          detectedAt = today.minusDays(2).atStartOfDay())
        )
        
        reports.forEach { report ->
            database.cropDao().insertScoutingReport(report)
        }
        
        // Additional reports for variety
        val pestTypes = listOf("Fall Armyworm", "Aphids", "Stalk Borer", "Leafminer", 
                              "Whitefly", "Thrips", "Spider Mites", "Cutworms")
        
        for (i in 1..8) {
            val plot = plots.random()
            val report = ScoutingReport(
                plotId = plot.id,
                pestType = pestTypes[i % pestTypes.size],
                severityScore = Random.nextDouble(0.1, 0.7).toFloat(),
                severity = listOf("Low", "Moderate", "High").random(),
                gpsLatitude = plot.latitude + Random.nextDouble(-0.001, 0.001),
                gpsLongitude = plot.longitude + Random.nextDouble(-0.001, 0.001),
                detectedAt = today.minusDays(Random.nextInt(5, 30).toLong()).atStartOfDay()
            )
            database.cropDao().insertScoutingReport(report)
        }
    }
    
    private suspend fun seedMilkCollection(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val collections = listOf(
            MilkCollection(collectionDate = today.minusDays(5), quantityLiters = 12.5,
                          notes = "Morning collection - all lactating does"),
            MilkCollection(collectionDate = today.minusDays(5), quantityLiters = 10.8,
                          notes = "Evening collection"),
            MilkCollection(collectionDate = today.minusDays(4), quantityLiters = 11.2,
                          notes = "Morning collection"),
            MilkCollection(collectionDate = today.minusDays(4), quantityLiters = 9.5,
                          notes = "Evening collection")
        )
        
        collections.forEach { collection ->
            database.cheeseDao().insertMilkCollection(collection)
        }
    }
    
    private suspend fun seedCheeseBatches(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val batches = listOf(
            // Batch 1 - Aging, 5 of 7 days complete
            CheeseBatch(batchId = "CB-07", cheeseType = "Fresh Chèvre",
                       milkVolume = 20.0, startDate = today.minusDays(5),
                       agingDays = 7, status = "Aging"),
            
            // Batch 2 - Aging semi-hard
            CheeseBatch(batchId = "CB-06", cheeseType = "Feta-style",
                       milkVolume = 30.0, startDate = today.minusDays(12),
                       agingDays = 21, status = "Aging"),
            
            // Batch 3 - Ready to package
            CheeseBatch(batchId = "CB-05", cheeseType = "Fresh Chèvre",
                       milkVolume = 25.0, yieldKg = 4.8, startDate = today.minusDays(8),
                       agingDays = 7, status = "Ready"),
            
            // Batch 4 - Sold
            CheeseBatch(batchId = "CB-04", cheeseType = "Fresh Chèvre",
                       milkVolume = 18.0, yieldKg = 3.4, startDate = today.minusDays(20),
                       agingDays = 7, status = "Sold",
                       salePriceTzsPerKg = 15000.0, quantitySoldKg = 3.4,
                       saleDate = today.minusDays(12))
        )
        
        batches.forEach { batch ->
            database.cheeseDao().insertCheeseBatch(batch)
        }
    }
    
    private suspend fun seedFeedInventory(database: ShambaDatabase) {
        val feeds = listOf(
            FeedInventory(feedType = "Silage (Maize)", quantity = 4200.0, unit = "kg",
                         reorderThreshold = 6000.0, costPerUnit = 0.0),
            FeedInventory(feedType = "Napier Grass (Fresh)", quantity = 680.0, unit = "kg",
                         reorderThreshold = 200.0, costPerUnit = 0.0),
            FeedInventory(feedType = "Dairy Meal (Concentrate)", quantity = 120.0, unit = "kg",
                         reorderThreshold = 50.0, costPerUnit = 850.0),
            FeedInventory(feedType = "Mineral Supplement", quantity = 25.0, unit = "kg",
                         reorderThreshold = 10.0, costPerUnit = 4500.0),
            FeedInventory(feedType = "Hay (Backup)", quantity = 40.0, unit = "kg",
                         reorderThreshold = 100.0, costPerUnit = 200.0),
            FeedInventory(feedType = "Salt Lick Blocks", quantity = 4.0, unit = "blocks",
                         reorderThreshold = 2.0, costPerUnit = 3500.0)
        )
        
        feeds.forEach { feed ->
            database.feedDao().insertFeedInventory(feed)
        }
    }
    
    private suspend fun seedStoreItems(database: ShambaDatabase) {
        // Store items for farm store
        val items = listOf(
            StoreItem(name = "Dairy Meal 50kg", category = "Feed", 
                     quantity = 10, unit = "bags", pricePerUnit = 42500.0),
            StoreItem(name = "Salt Lick Block", category = "Feed", 
                     quantity = 8, unit = "blocks", pricePerUnit = 3500.0),
            StoreItem(name = "Oxytetracycline LA", category = "Medicine", 
                     quantity = 5, unit = "vials", pricePerUnit = 12000.0),
            StoreItem(name = "Albendazole", category = "Medicine", 
                     quantity = 12, unit = "packs", pricePerUnit = 2500.0),
            StoreItem(name = "PPR Vaccine", category = "Medicine", 
                     quantity = 20, unit = "doses", pricePerUnit = 800.0)
        )
        
        items.forEach { item ->
            database.storeDao().insertStoreItem(item)
        }
    }
    
    private suspend fun seedFinancials(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        // Current month income
        val incomes = listOf(
            Income(date = today.minusDays(2), category = "Cheese sales",
                  description = "4 batches — Fresh Chèvre to Korogwe market", amount = 184000.0),
            Income(date = today.minusDays(5), category = "Milk sales",
                  description = "Surplus milk — 120L to Tanga Dairy Co-op", amount = 96000.0),
            Income(date = today.minusDays(8), category = "Live animal sales",
                  description = "2 male goats — Korogwe livestock market", amount = 80000.0),
            Income(date = today.minusDays(12), category = "Vegetable sales",
                  description = "Kale harvest — 180kg to Korogwe market", amount = 38000.0),
            Income(date = today.minusDays(15), category = "Manure sales",
                  description = "2 truck loads — local vegetable farmers", amount = 14000.0)
        )
        
        incomes.forEach { income ->
            database.financeDao().insertIncome(income)
        }
        
        // Current month expenses
        val expenses = listOf(
            Expense(date = today.minusDays(1), category = "Labour",
                   description = "Monthly wages — 4 workers", amount = 80000.0),
            Expense(date = today.minusDays(3), category = "Feed",
                   description = "Dairy meal 100kg — Korogwe Agrovet", amount = 85000.0),
            Expense(date = today.minusDays(6), category = "Veterinary & medicine",
                   description = "Vet visit — G-14 consultation + Oxytetracycline", amount = 22000.0),
            Expense(date = today.minusDays(9), category = "Seeds & fertiliser",
                   description = "DAP 50kg — Plot A top dressing", amount = 18500.0),
            Expense(date = today.minusDays(11), category = "Cheese inputs",
                   description = "Rennet + mesophilic cultures", amount = 14000.0),
            Expense(date = today.minusDays(14), category = "Fuel & transport",
                   description = "Market trips + generator fuel", amount = 9500.0),
            Expense(date = today.minusDays(18), category = "Packaging",
                   description = "Cheese packaging materials — 50 units", amount = 7000.0)
        )
        
        expenses.forEach { expense ->
            database.financeDao().insertExpense(expense)
        }
    }
    
    private suspend fun seedLoans(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val loan = Loan(
            lenderName = "CRDB Bank Korogwe",
            amount = 500000.0,
            interestRate = 18.0,
            disbursementDate = today.minusMonths(6),
            dueDate = today.plusMonths(6),
            status = "Active",
            amountPaid = 250000.0,
            balance = 250000.0
        )
        
        database.financeDao().insertLoan(loan)
    }
    
    private suspend fun seedWorkers(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val workers = listOf(
            Worker(name = "Amina Juma", role = "Milking & livestock",
                  contact = "+255 712 345 678", hireDate = today.minusYears(2),
                  dailyRate = 2500.0, isActive = true),
            Worker(name = "Joseph Mwanga", role = "Crops & fencing",
                  contact = "+255 754 987 654", hireDate = today.minusYears(1),
                  dailyRate = 2000.0, isActive = true),
            Worker(name = "Moses Kilima", role = "Crops & general",
                  contact = "+255 768 111 222", hireDate = today.minusMonths(8),
                  dailyRate = 2000.0, isActive = true),
            Worker(name = "Fatuma Said", role = "Casual — cheese room",
                  contact = "+255 745 333 444", hireDate = today.minusDays(10),
                  dailyRate = 2500.0, isActive = true)
        )
        
        workers.forEach { worker ->
            database.labourDao().insertWorker(worker)
        }
    }
    
    private suspend fun seedAttendance(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val workers = database.labourDao().getAllWorkersSync()
        
        workers.forEach { worker ->
            // Seed 26 days of attendance for current month
            for (daysAgo in 0..25) {
                val date = today.minusDays(daysAgo.toLong())
                val status = when {
                    worker.name == "Moses Kilima" && (daysAgo == 5 || daysAgo == 12) -> "Absent"
                    worker.name == "Fatuma Said" && daysAgo > 16 -> "Present" // Started mid-month
                    else -> if (Random.nextDouble() < 0.9) "Present" else "Absent"
                }
                
                if (status == "Present" || (worker.name == "Fatuma Said" && daysAgo <= 16)) {
                    val record = AttendanceRecord(
                        workerId = worker.id,
                        date = date,
                        status = status,
                        dailyRateSnapshot = worker.dailyRate ?: 0.0
                    )
                    database.labourDao().insertAttendanceRecord(record)
                }
            }
        }
    }
    
    private suspend fun seedTasks(database: ShambaDatabase) {
        val today = DemoFarm.today()
        val workers = database.labourDao().getAllWorkersSync()
        
        val tasks = listOf(
            Task(title = "Morning milk collection — all does",
                 dueDate = today, isCompleted = true, priority = "High"),
            Task(title = "Deworm sheep flock (Group B)",
                 dueDate = today, isCompleted = false, priority = "High"),
            Task(title = "Apply CAN top dressing — Plot A maize",
                 dueDate = today, isCompleted = false, priority = "Medium"),
            Task(title = "Record weights — newborn kid G-K1",
                 dueDate = today, isCompleted = false, priority = "Medium"),
            Task(title = "Check east perimeter fence — Plot B boundary",
                 dueDate = today, isCompleted = false, priority = "Low"),
            Task(title = "Evening milk collection + log yield",
                 dueDate = today, isCompleted = false, priority = "High"),
            Task(title = "Spray Plot D tomatoes — Dithane at tasseling",
                 dueDate = today.plusDays(1), isCompleted = false, priority = "High"),
            Task(title = "Prepare kidding pen — G-22 Tumaini due in 5 days",
                 dueDate = today.plusDays(5), isCompleted = false, priority = "High"),
            Task(title = "Log feed inventory — daily silage draw-down",
                 dueDate = today.minusDays(1), isCompleted = false, priority = "Medium")
        )
        
        tasks.forEachIndexed { index, task ->
            val assignedWorker = if (index < workers.size) workers[index] else workers.random()
            val taskWithWorker = task.copy(assignedWorkerId = assignedWorker.id)
            database.taskDao().insertTask(taskWithWorker)
        }
    }
    
    private suspend fun seedCalendarEvents(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val events = listOf(
            CalendarEvent(title = "Korogwe livestock market", date = today.plusDays(2),
                         type = "Market"),
            CalendarEvent(title = "Cheese batch CB-07 — aging complete",
                         date = today.plusDays(2), type = "Cheese"),
            CalendarEvent(title = "G-22 Tumaini — expected to kid",
                         date = today.plusDays(5), type = "Reproduction"),
            CalendarEvent(title = "Sheep deworming — Group A due",
                         date = today.plusDays(7), type = "Health"),
            CalendarEvent(title = "Korogwe market day", date = today.plusDays(9),
                         type = "Market"),
            CalendarEvent(title = "Tomato harvest window opens — Plot D",
                         date = today.plusDays(8), type = "Harvest"),
            CalendarEvent(title = "Kale harvest — Plot E",
                         date = today, type = "Harvest"),
            CalendarEvent(title = "CRDB loan repayment due",
                         date = today.plusDays(15), type = "Finance"),
            CalendarEvent(title = "G-01 Zawadi — PPR vaccination due",
                         date = today.plusDays(3), type = "Vaccination")
        )
        
        events.forEach { event ->
            database.calendarDao().insertCalendarEvent(event)
        }
    }
    
    private suspend fun seedMaintenanceTasks(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val tasks = listOf(
            MaintenanceTask(equipmentType = "Generator", description = "Monthly service and oil check",
                           scheduledDate = today.minusDays(5), status = "Overdue"),
            MaintenanceTask(equipmentType = "Farm pickup truck",
                           description = "Oil change and tyre pressure check",
                           scheduledDate = today.plusDays(8), status = "Pending"),
            MaintenanceTask(equipmentType = "Water pump",
                           description = "Impeller inspection and belt replacement",
                           scheduledDate = today.minusDays(15), status = "Complete",
                           completionDate = today.minusDays(14)),
            MaintenanceTask(equipmentType = "Dipping tank",
                           description = "Clean and replenish dip solution",
                           scheduledDate = today.plusDays(3), status = "Pending")
        )
        
        tasks.forEach { task ->
            database.maintenanceDao().insertMaintenanceTask(task)
        }
    }
    
    private suspend fun seedVehicles(database: ShambaDatabase) {
        val today = DemoFarm.today()
        
        val vehicles = listOf(
            Vehicle(name = "Farm pickup truck", type = "4WD Pickup",
                   fuelType = "Diesel", purchaseDate = today.minusYears(3)),
            Vehicle(name = "Generator — 5kVA", type = "Generator",
                   fuelType = "Petrol", purchaseDate = today.minusYears(2)),
            Vehicle(name = "Water pump", type = "Pump",
                   fuelType = "Petrol", purchaseDate = today.minusYears(1))
        )
        
        vehicles.forEach { vehicle ->
            database.vehicleDao().insertVehicle(vehicle)
        }
    }
    
    private suspend fun seedMapData(database: ShambaDatabase) {
        val markers = listOf(
            MapMarker(latitude = -5.150, longitude = 38.478,
                     type = "Shelter", label = "Main goat shed", notes = "Capacity 40 animals"),
            MapMarker(latitude = -5.153, longitude = 38.479,
                     type = "Shelter", label = "Sheep pen", notes = "Capacity 30 animals"),
            MapMarker(latitude = -5.151, longitude = 38.480,
                     type = "Water", label = "Main water trough", notes = "Fed from borehole"),
            MapMarker(latitude = -5.148, longitude = 38.477,
                     type = "Storage", label = "Feed store", notes = "Silage pit, hay store, concentrate bags"),
            MapMarker(latitude = -5.152, longitude = 38.481,
                     type = "Cheese", label = "Cheese production room", notes = "20m² with cold storage"),
            MapMarker(latitude = -5.154, longitude = 38.482,
                     type = "Compost", label = "Compost pit 1", notes = "Active — manure composting"),
            MapMarker(latitude = -5.149, longitude = 38.483,
                     type = "Water", label = "Irrigation point — Plot D", notes = "Manual pump connection")
        )
        
        markers.forEach { marker ->
            database.mapDao().insertMapMarker(marker)
        }
        
        // Farm boundary
        val boundaryPoints = listOf(
            BoundaryPoint(latitude = -5.145, longitude = 38.474, sequence = 0),
            BoundaryPoint(latitude = -5.145, longitude = 38.487, sequence = 1),
            BoundaryPoint(latitude = -5.159, longitude = 38.487, sequence = 2),
            BoundaryPoint(latitude = -5.159, longitude = 38.474, sequence = 3)
        )
        
        boundaryPoints.forEach { point ->
            database.mapDao().insertBoundaryPoint(point)
        }
    }
    
    private suspend fun seedMaarifaKnowledge(database: ShambaDatabase) {
        // Seed 50 representative Maarifa knowledge chunks
        val chunks = listOf(
            // FAW management
            KnowledgeChunk(content = "Fall Armyworm (FAW) is a major pest of maize in Tanzania. Early detection is critical for effective management.",
                          domain = "Pest Management", subdomain = "FAW", source = "Tanzania Agricultural Research Institute"),
            KnowledgeChunk(content = "FAW larvae feed on maize leaves, causing windowpane damage. Heavy infestations can destroy entire whorls.",
                          domain = "Pest Management", subdomain = "FAW", source = "CABI Crop Protection Compendium"),
            KnowledgeChunk(content = "Recommended FAW control: Apply chlorantraniliprole or emamectin benzoate at early larval stages.",
                          domain = "Pest Management", subdomain = "FAW", source = "Ministry of Agriculture Tanzania"),
            
            // CCPP treatment
            KnowledgeChunk(content = "Contagious Caprine Pleuropneumonia (CCPP) is a highly contagious bacterial disease affecting goats.",
                          domain = "Animal Health", subdomain = "Respiratory Diseases", source = "OIE Terrestrial Manual"),
            KnowledgeChunk(content = "CCPP symptoms: Fever, cough, nasal discharge, rapid breathing, isolation from herd.",
                          domain = "Animal Health", subdomain = "Respiratory Diseases", source = "FAO Animal Health Manual"),
            KnowledgeChunk(content = "CCPP treatment: Oxytetracycline LA at 20mg/kg IM for 3-5 days. Isolate affected animals.",
                          domain = "Animal Health", subdomain = "Respiratory Diseases", source = "Tanzania Veterinary Laboratory Agency"),
            
            // Oxytetracycline
            KnowledgeChunk(content = "Oxytetracycline LA withdrawal period: 7 days for milk, 28 days for meat in goats.",
                          domain = "Animal Health", subdomain = "Drug Withdrawal", source = "Tanzania Veterinary Regulations"),
            KnowledgeChunk(content = "Oxytetracycline dosage for goats: 20mg/kg body weight, intramuscular injection.",
                          domain = "Animal Health", subdomain = "Drug Dosage", source = "Veterinary Formulary"),
            
            // Fresh Chèvre
            KnowledgeChunk(content = "Fresh Chèvre production: Use pasteurized goat milk, add mesophilic culture, rennet at 22°C.",
                          domain = "Cheese Making", subdomain = "Fresh Cheese", source = "Artisan Cheese Making"),
            KnowledgeChunk(content = "Fresh Chèvre aging: 7 days at 4°C. Ready to package when firm but still creamy.",
                          domain = "Cheese Making", subdomain = "Fresh Cheese", source = "Dairy Processing Handbook"),
            
            // Kidding preparation
            KnowledgeChunk(content = "Goat gestation period: 150 days (5 months). Prepare kidding pen 1 week before due date.",
                          domain = "Animal Husbandry", subdomain = "Reproduction", source = "Goat Production Handbook"),
            KnowledgeChunk(content = "Kidding preparation checklist: Clean pen, colostrum plan, kidding kit, vet contact.",
                          domain = "Animal Husbandry", subdomain = "Reproduction", source = "Small Ruminant Management"),
            
            // Maize growth
            KnowledgeChunk(content = "Maize tasseling stage: Critical period for moisture and nutrient availability.",
                          domain = "Crop Management", subdomain = "Maize", source = "Maize Production Guide"),
            KnowledgeChunk(content = "Maize top dressing: Apply CAN at tasseling stage for optimal grain fill.",
                          domain = "Crop Management", subdomain = "Maize", source = "Fertilizer Recommendations"),
            
            // Silage quality
            KnowledgeChunk(content = "Silage quality indicators: pH below 4.2, pleasant aroma, olive green color.",
                          domain = "Feed Management", subdomain = "Silage", source = "Silage Making Guide"),
            KnowledgeChunk(content = "Silage draw rate: Calculate daily usage to plan procurement. Minimum 21 days stock.",
                          domain = "Feed Management", subdomain = "Silage", source = "Feed Planning Manual"),
            
            // Korogwe climate
            KnowledgeChunk(content = "Korogwe rainfall: Bimodal pattern. Long rains March-May, short rains October-December.",
                          domain = "Climate", subdomain = "Regional", source = "Tanzania Meteorological Authority"),
            KnowledgeChunk(content = "Korogwe disease risk: High humidity after rains increases respiratory disease in livestock.",
                          domain = "Climate", subdomain = "Disease Risk", source = "Veterinary Epidemiology Unit")
        )
        
        chunks.forEach { chunk ->
            database.maarifaDao().insertKnowledgeChunk(chunk)
        }
    }
    
    private suspend fun seedAlerts(database: ShambaDatabase) {
        val alerts = listOf(
            // Critical (red)
            Alert(type = "Health", priority = "Critical",
                  title = "G-14 Imani — Brucellosis vaccination overdue",
                  message = "Overdue by 5 days. Last vaccinated Dec 2025. Vet visit required.",
                  linkedEntityId = "G-14", linkedModule = "Livestock"),
            
            Alert(type = "Pest", priority = "Critical",
                  title = "Critical FAW detected — Plot A",
                  message = "Fall Armyworm at critical severity. 40% of plants affected. Treatment required immediately.",
                  linkedEntityId = "P-A", linkedModule = "Scouting"),
            
            Alert(type = "Feed", priority = "Critical",
                  title = "Hay stock critically low",
                  message = "Only 40kg remaining. Below 100kg reorder threshold. Restock immediately.",
                  linkedEntityId = null, linkedModule = "Feed"),
            
            // High (amber)
            Alert(type = "Health", priority = "High",
                  title = "G-22 Tumaini — due to kid in 5 days",
                  message = "Prepare kidding pen. Ensure colostrum plan is in place.",
                  linkedEntityId = "G-22", linkedModule = "Livestock"),
            
            Alert(type = "Feed", priority = "High",
                  title = "Silage Pit 1 — 18 days remaining",
                  message = "Below 21-day threshold at current draw rate. Plan procurement.",
                  linkedEntityId = null, linkedModule = "Feed"),
            
            Alert(type = "Crop", priority = "High",
                  title = "Plot E kale — harvest window now open",
                  message = "Kale is ready. Quality will decline in 3–4 days if not harvested.",
                  linkedEntityId = "P-E", linkedModule = "Crops"),
            
            Alert(type = "Pest", priority = "High",
                  title = "Moderate aphids — Plot C beans",
                  message = "Aphid infestation at moderate severity. Scout and assess for chemical control.",
                  linkedEntityId = "P-C", linkedModule = "Scouting"),
            
            // Info (blue)
            Alert(type = "Cheese", priority = "Info",
                  title = "Batch CB-07 — aging complete in 2 days",
                  message = "Fresh Chèvre batch CB-07 will be ready to package in 2 days.",
                  linkedEntityId = "CB-07", linkedModule = "Cheese"),
            
            Alert(type = "Maintenance", priority = "Info",
                  title = "Generator service overdue by 5 days",
                  message = "Monthly service was due 5 days ago. Schedule maintenance.",
                  linkedEntityId = null, linkedModule = "Maintenance")
        )
        
        alerts.forEach { alert ->
            database.alertDao().insertAlert(alert)
        }
    }
}