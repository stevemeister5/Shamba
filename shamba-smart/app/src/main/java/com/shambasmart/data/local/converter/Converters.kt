package com.shambasmart.data.local.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun localDateTimeToString(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value == null) return null
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list == null) return null
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromDoubleMap(value: String?): Map<String, Double>? {
        if (value == null) return null
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun toDoubleMap(map: Map<String, Double>?): String? {
        if (map == null) return null
        return json.encodeToString(map)
    }
}
