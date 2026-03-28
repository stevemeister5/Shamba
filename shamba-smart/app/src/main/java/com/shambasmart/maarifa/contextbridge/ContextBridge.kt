package com.shambasmart.maarifa.contextbridge

import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.*
import com.shambasmart.maarifa.retrieval.IntentClassifier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

/**
 * Maarifa Context Bridge — reads live farm SQLite data and injects
 * context into every query before retrieval.
 *
 * Design spec: "Read live farm SQLite database and inject context.
 * If animal named → profile, health history, weight trend, milk records,
 * current treatment and withdrawal status. If plot named → crop type,
 * planting date, last inputs applied, current growth stage, plot size in acres.
 * Always inject → today's date, current month, current season,
 * last 7 days of manually logged rainfall."
 *
 * The Context Bridge reads but NEVER modifies farm data.
 */
class ContextBridge(
    private val animalDao: AnimalDao,
    private val healthRecordDao: HealthRecordDao,
    private val reproductionDao: ReproductionDao,
    private val milkProductionDao: MilkProductionDao,
    private val plotDao: PlotDao,
    private val cropDao: CropDao,
    private val weatherDao: WeatherDao,
    private val feedDao: FeedDao,
    private val taskDao: TaskDao,
    private val calendarDao: CalendarDao
) {

    data class FarmContext(
        val today: LocalDate,
        val currentMonth: Int,
        val currentSeason: String,
        val animalContext: AnimalContext? = null,
        val plotContext: PlotContext? = null,
        val herdContext: HerdContext,
        val weatherContext: WeatherContext,
        val feedContext: FeedContext,
        val alerts: List<String> = emptyList()
    )

    data class AnimalContext(
        val animalId: Long,
        val tagId: String,
        val species: String,
        val breed: String?,
        val sex: String,
        val age: Int?,
        val weightKg: Double?,
        val status: String,
        val lastHealthEvent: String?,
        val lastHealthDate: String?,
        val currentTreatments: List<TreatmentInfo>,
        val milkYieldTrend7Day: Double?,
        val reproductiveStatus: String?,
        val pregnancyStage: String?
    )

    data class TreatmentInfo(
        val drug: String,
        val startDate: String,
        val milkWithdrawalEndDate: String?,
        val meatWithdrawalEndDate: String?,
        val isInWithdrawal: Boolean
    )

    data class PlotContext(
        val plotId: Long,
        val name: String,
        val sizeAcres: Double?,
        val currentCrop: String?,
        val variety: String?,
        val plantingDate: String?,
        val daysSincePlanting: Long?,
        val growthStage: String?,
        val lastInputsApplied: List<String>
    )

    data class HerdContext(
        val totalAnimals: Int,
        val goatCount: Int,
        val sheepCount: Int,
        val animalsInWithdrawal: Int,
        val recentHealthEvents: List<String>
    )

    data class WeatherContext(
        val last7DaysRainfall: List<Pair<String, Double>>,
        val totalRainfall7Days: Double,
        val averageTemperature: Double?,
        val humidityPercent: Double?
    )

    data class FeedContext(
        val lowStockItems: List<String>,
        val daysOfFeedRemaining: Int?
    )

    /**
     * Build farm context from live data. Called before every retrieval.
     */
    suspend fun buildContext(
        entities: IntentClassifier.EntityResult
    ): FarmContext {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val currentMonth = today.monthNumber
        val currentSeason = deriveSeason(currentMonth)

        // Animal context if named or detected
        val animalCtx = entities.species?.let { species ->
            entities.animalId?.let { findAnimalContext(it, species) }
        }

        // Plot context if named
        val plotCtx = entities.plotName?.let { findPlotContext(it) }

        // Herd-level context
        val herdCtx = buildHerdContext()

        // Weather context
        val weatherCtx = buildWeatherContext(today)

        // Feed context
        val feedCtx = buildFeedContext()

        // Alerts
        val alerts = buildAlerts(herdCtx, weatherCtx, feedCtx, currentSeason)

        return FarmContext(
            today = today,
            currentMonth = currentMonth,
            currentSeason = currentSeason,
            animalContext = animalCtx,
            plotContext = plotCtx,
            herdContext = herdCtx,
            weatherContext = weatherCtx,
            feedContext = feedCtx,
            alerts = alerts
        )
    }

    private fun deriveSeason(month: Int): String = when (month) {
        3, 4, 5 -> "long_rains"
        6, 7, 8, 9 -> "cool_dry"
        10, 11, 12 -> "short_rains"
        else -> "hot_dry" // Jan, Feb
    }

    private suspend fun findAnimalContext(tagIdOrName: String, species: String): AnimalContext? {
        // getAllActiveAnimals() returns Flow, so we need to collect it
        val animals = mutableListOf<Animal>()
        animalDao.getAllActiveAnimals().collect { animals.addAll(it) }
        
        val animal = animals.firstOrNull { a ->
            a.tagId?.equals(tagIdOrName, ignoreCase = true) == true
        } ?: return null

        val healthRecords = healthRecordDao.getRecordsByAnimal(animal.id)
        val lastHealth = healthRecords.maxByOrNull { it.date }

        val milkRecords = milkProductionDao.getRecordsByAnimal(animal.id)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val last7DaysMilk = milkRecords
            .filter { record -> record.date.daysUntil(today) <= 7 }
            .sumOf { (it.morningYield ?: 0.0) + (it.eveningYield ?: 0.0) }

        val treatments = healthRecords
            .filter { it.type == "treatment" && it.vaccineName != null }
            .mapNotNull { record ->
                val drug = record.vaccineName ?: return@mapNotNull null
                val startDate = record.date.toString()
                // Withdrawal lookup would need rules — simplified here
                TreatmentInfo(
                    drug = drug,
                    startDate = startDate,
                    milkWithdrawalEndDate = null,
                    meatWithdrawalEndDate = null,
                    isInWithdrawal = false
                )
            }

        val reproRecords = reproductionDao.getRecordsByAnimal(animal.id)
        val latestRepro = reproRecords.filter { it.matingDate != null }.maxByOrNull { it.matingDate!! }
        val pregStatus = if (latestRepro?.pregnancyConfirmed == true) "pregnant" else null

        // Calculate age from dateOfBirth
        val age = animal.dateOfBirth?.let { dob ->
            (dob.daysUntil(today) / 365)
        }

        return AnimalContext(
            animalId = animal.id,
            tagId = animal.tagId ?: "No tag",
            species = animal.species,
            breed = animal.breed,
            sex = animal.sex,
            age = age,
            weightKg = animal.weight,
            status = animal.status,
            lastHealthEvent = lastHealth?.type,
            lastHealthDate = lastHealth?.date?.toString(),
            currentTreatments = treatments,
            milkYieldTrend7Day = if (last7DaysMilk > 0) last7DaysMilk else null,
            reproductiveStatus = pregStatus,
            pregnancyStage = null
        )
    }

    private suspend fun findPlotContext(plotName: String): PlotContext? {
        val plots = mutableListOf<Plot>()
        plotDao.getAllPlots().collect { plots.addAll(it) }
        val plot = plots.firstOrNull {
            it.name.equals(plotName, ignoreCase = true)
        } ?: return null

        val plantings = cropDao.getPlantingsByPlot(plot.id)
        val currentPlanting = plantings.maxByOrNull { it.plantingDate }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val plantingDate = currentPlanting?.plantingDate
        val daysSince: Long? = if (plantingDate != null) {
            plantingDate.daysUntil(today).toLong()
        } else {
            null
        }

        return PlotContext(
            plotId = plot.id,
            name = plot.name,
            sizeAcres = plot.sizeAcres,
            currentCrop = currentPlanting?.cropType,
            variety = currentPlanting?.variety,
            plantingDate = currentPlanting?.plantingDate?.toString(),
            daysSincePlanting = daysSince,
            growthStage = currentPlanting?.status,
            lastInputsApplied = emptyList()
        )
    }

    private suspend fun buildHerdContext(): HerdContext {
        // getAllActiveAnimals() returns Flow, so we need to collect it
        val animals = mutableListOf<Animal>()
        animalDao.getAllActiveAnimals().collect { animals.addAll(it) }
        
        val activeAnimals = animals.filter { a -> a.status == "active" }
        val goats = activeAnimals.count { a -> a.species.lowercase() == "goat" }
        val sheep = activeAnimals.count { a -> a.species.lowercase() == "sheep" }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val recentEvents = healthRecordDao.getAllRecords()
            .filter { record -> record.date.daysUntil(today) <= 7 }
            .map { record -> "${record.animalId}: ${record.type} - ${record.description ?: "No description"}" }
            .take(5)

        return HerdContext(
            totalAnimals = activeAnimals.size,
            goatCount = goats,
            sheepCount = sheep,
            animalsInWithdrawal = 0, // Simplified — needs rule engine integration
            recentHealthEvents = recentEvents
        )
    }

    private suspend fun buildWeatherContext(today: LocalDate): WeatherContext {
        val logs = weatherDao.getAllLogs()
            .filter { log -> log.date.daysUntil(today) <= 7 }

        val rainfallPairs = logs.map { log -> log.date.toString() to (log.rainfallMm ?: 0.0) }
        val totalRain = rainfallPairs.sumOf { pair -> pair.second }
        val avgTemp = logs.mapNotNull { log -> log.maxTemp }.average().takeIf { !it.isNaN() }

        return WeatherContext(
            last7DaysRainfall = rainfallPairs,
            totalRainfall7Days = totalRain,
            averageTemperature = avgTemp,
            humidityPercent = null
        )
    }

    private suspend fun buildFeedContext(): FeedContext {
        val feeds = feedDao.getAllFeeds()
        val lowStock = feeds.filter { feed ->
            feed.stockLevel <= (feed.reorderThreshold ?: 0.0)
        }.map { feed -> feed.feedType }

        return FeedContext(
            lowStockItems = lowStock,
            daysOfFeedRemaining = null
        )
    }

    private fun buildAlerts(
        herd: HerdContext,
        weather: WeatherContext,
        feed: FeedContext,
        season: String
    ): List<String> {
        val alerts = mutableListOf<String>()
        if (herd.animalsInWithdrawal > 0) alerts.add("${herd.animalsInWithdrawal} animal(s) in withdrawal period")
        if (feed.lowStockItems.isNotEmpty()) alerts.add("Low feed stock: ${feed.lowStockItems.joinToString()}")
        if (weather.totalRainfall7Days > 80) alerts.add("High rainfall in last 7 days (${weather.totalRainfall7Days}mm) — monitor for grey leaf spot on maize")
        if (season == "hot_dry") alerts.add("Hot dry season — ensure adequate water and shade for livestock")
        return alerts
    }

    /**
     * Format context as text for injection into retrieval scoring.
     */
    fun formatForRetrieval(ctx: FarmContext): String {
        val sb = StringBuilder()
        sb.appendLine("Farm context: Date ${ctx.today}, Season: ${ctx.currentSeason}")
        sb.appendLine("Herd: ${ctx.herdContext.totalAnimals} animals (${ctx.herdContext.goatCount} goats, ${ctx.herdContext.sheepCount} sheep)")
        ctx.animalContext?.let { a ->
            sb.appendLine("Animal: ${a.tagId} (${a.species}, ${a.breed}, ${a.status})")
            a.lastHealthEvent?.let { sb.appendLine("Last health event: $it on ${a.lastHealthDate}") }
            a.milkYieldTrend7Day?.let { sb.appendLine("7-day milk yield: ${it}L") }
        }
        ctx.plotContext?.let { p ->
            sb.appendLine("Plot: ${p.name} (${p.sizeAcres} acres) — ${p.currentCrop} planted ${p.daysSincePlanting} days ago")
        }
        if (ctx.weatherContext.totalRainfall7Days > 0) {
            sb.appendLine("Rainfall last 7 days: ${ctx.weatherContext.totalRainfall7Days}mm")
        }
        return sb.toString().trim()
    }
}