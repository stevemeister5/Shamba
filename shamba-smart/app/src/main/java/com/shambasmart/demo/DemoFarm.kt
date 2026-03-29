package com.shambasmart.demo

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object DemoFarm {
    const val FARM_NAME = "Kilimo Bora Farm"
    const val OWNER_NAME = "James Makwetta"
    const val LOCATION = "Korogwe, Tanga"
    const val SIZE_ACRES = 16
    const val PHONE = "+255 754 123 456"
    val LATITUDE = -5.15
    val LONGITUDE = 38.48
    
    // Today's date for relative calculations
    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}