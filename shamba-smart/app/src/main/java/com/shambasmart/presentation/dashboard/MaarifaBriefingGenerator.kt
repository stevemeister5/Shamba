package com.shambasmart.presentation.dashboard

import com.shambasmart.data.local.entity.Animal
import com.shambasmart.data.local.entity.CalendarEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt

object MaarifaBriefingGenerator {

    fun generateMorningBriefing(
        animals: List<Animal>,
        milkToday: Double,
        herdSize: Int,
        pendingTasks: Int,
        lowFeedAlerts: Int,
        upcomingEvents: List<CalendarEvent>,
        currentSeason: String = getCurrentSeason()
    ): BriefingResult {
        val briefingParts = mutableListOf<String>()

        // Herd status
        if (herdSize > 0) {
            briefingParts.add("Your herd of $herdSize animals is active.")
        }

        // Milk production
        if (milkToday > 0) {
            briefingParts.add("Today's milk yield: ${String.format("%.1f", milkToday)}L.")
        }

        // Seasonal tips
        val seasonalTip = getSeasonalTip(currentSeason)
        if (seasonalTip.isNotEmpty()) {
            briefingParts.add(seasonalTip)
        }

        // Alerts
        if (pendingTasks > 0) {
            briefingParts.add("You have $pendingTasks pending task(s) to complete.")
        }

        if (lowFeedAlerts > 0) {
            briefingParts.add("Warning: $lowFeedAlerts feed item(s) running low.")
        }

        // Upcoming events
        if (upcomingEvents.isNotEmpty()) {
            val nextEvent = upcomingEvents.first()
            briefingParts.add("Upcoming: ${nextEvent.title}")
        }

        val briefing = if (briefingParts.isNotEmpty()) {
            briefingParts.joinToString(" ")
        } else {
            "Welcome to Shamba Smart. Your farm dashboard is ready for the day."
        }

        return BriefingResult(
            text = briefing,
            confidenceTier = ConfidenceTier.RULE_GOVERNED,
            confidenceLabel = "Rule-governed"
        )
    }

    private fun getCurrentSeason(): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val month = today.monthNumber
        return when (month) {
            in 3..5 -> "long_rains"
            in 6..8 -> "cool_dry"
            in 9..11 -> "short_rains"
            else -> "hot_dry"
        }
    }

    private fun getSeasonalTip(season: String): String {
        return when (season) {
            "long_rains" -> "Long rains season: Ensure drainage in plots is adequate and monitor for increased pest activity."
            "short_rains" -> "Short rains approaching: Prepare irrigation systems and consider planting quick-maturing crops."
            "hot_dry" -> "Hot season: Increase water availability for livestock and consider supplemental feeding."
            "cool_dry" -> "Cool dry season: Ideal time for animal health checks and infrastructure maintenance."
            else -> ""
        }
    }
}

data class BriefingResult(
    val text: String,
    val confidenceTier: ConfidenceTier,
    val confidenceLabel: String
)

enum class ConfidenceTier {
    RULE_GOVERNED,
    MULTI_SOURCE,
    LIMITED_SOURCES,
    NOT_FOUND
}