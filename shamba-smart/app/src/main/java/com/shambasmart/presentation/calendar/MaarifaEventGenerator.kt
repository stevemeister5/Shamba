package com.shambasmart.presentation.calendar

import com.shambasmart.data.local.entity.*
import kotlinx.datetime.*

object MaarifaEventGenerator {

    fun generateEvents(
        healthRecords: List<HealthRecord>,
        reproductionRecords: List<ReproductionRecord>,
        animals: List<Animal>
    ): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Generate vaccination due dates
        healthRecords.filter { it.type == "vaccination" }.forEach { record ->
            val animal = animals.find { it.id == record.animalId }
            // Next vaccination due in 6 months
            val nextDue = record.date.plus(DatePeriod(months = 6))
            if (nextDue >= today) {
                events.add(
                    CalendarEvent(
                        title = "${animal?.tagId ?: "Animal"} - Vaccination Due",
                        description = "Next ${record.description} vaccination due",
                        date = nextDue,
                        type = "health"
                    )
                )
            }
        }

        // Generate expected kidding dates (150 days after mating)
        reproductionRecords.filter { it.type == "mating" }.forEach { record ->
            val animal = animals.find { it.id == record.damId }
            val matingDate = record.matingDate ?: return@forEach
            val expectedKidding = matingDate.plus(DatePeriod(days = 150))
            if (expectedKidding >= today) {
                events.add(
                    CalendarEvent(
                        title = "${animal?.tagId ?: "Animal"} - Expected Kidding",
                        description = "Expected kidding date based on mating on $matingDate",
                        date = expectedKidding,
                        type = "reproduction"
                    )
                )
            }
        }

        // Generate pregnancy check dates (30 days after mating)
        reproductionRecords.filter { it.type == "mating" }.forEach { record ->
            val animal = animals.find { it.id == record.damId }
            val matingDate = record.matingDate ?: return@forEach
            val pregnancyCheck = matingDate.plus(DatePeriod(days = 30))
            if (pregnancyCheck >= today) {
                events.add(
                    CalendarEvent(
                        title = "${animal?.tagId ?: "Animal"} - Pregnancy Check",
                        description = "Schedule pregnancy check for ${animal?.tagId}",
                        date = pregnancyCheck,
                        type = "reproduction"
                    )
                )
            }
        }

        // Generate deworming reminders (every 3 months)
        animals.forEach { animal ->
            val lastDeworming = healthRecords
                .filter { it.animalId == animal.id && it.description?.contains("deworm", ignoreCase = true) == true }
                .maxByOrNull { it.date }
            
            if (lastDeworming != null) {
                val nextDeworming = lastDeworming.date.plus(DatePeriod(months = 3))
                if (nextDeworming >= today) {
                    val tagDisplay = animal.tagId ?: "Animal"
                    events.add(
                        CalendarEvent(
                            title = "$tagDisplay - Deworming Due",
                            description = "Quarterly deworming due",
                            date = nextDeworming,
                            type = "health"
                        )
                    )
                }
            }
        }

        return events.sortedBy { it.date }
    }
}