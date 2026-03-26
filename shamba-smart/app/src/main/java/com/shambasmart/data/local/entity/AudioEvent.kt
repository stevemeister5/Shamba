package com.shambasmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_event_log")
data class AudioEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val soundClass: String,
    val confidence: Double,
    val plotId: Long? = null,
    val deviceId: String = "",
    val durationMs: Long = 0,
    val audioData: ByteArray? = null, // Spectrogram data
    val healthRecordId: Long? = null, // Linked health record
    val isAnomaly: Boolean = false,
    val isSynced: Boolean = false,
    val notes: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioEvent

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (soundClass != other.soundClass) return false
        if (confidence != other.confidence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + soundClass.hashCode()
        result = 31 * result + confidence.hashCode()
        return result
    }
}

// Sound classes for livestock/distress audio events
object SoundClasses {
    const val GOAT_BLEAT = "goat_bleat"
    const val GOAT_DISTRESS = "goat_distress"
    const val SHEEP_BLEAT = "sheep_bleat"
    const val SHEEP_DISTRESS = "sheep_distress"
    const val CATTLE_MOO = "cattle_moo"
    const val CATTLE_DISTRESS = "cattle_distress"
    const val CHICKEN_CLUCK = "chicken_cluck"
    const val CHICKEN_DISTRESS = "chicken_distress"
    const val PREDATOR_DOG = "predator_dog"
    const val PREDATOR_HYENA = "predator_hyena"
    const val RAIN_HEAVY = "rain_heavy"
    const val WIND_STRONG = "wind_strong"
    const val THUNDER = "thunder"
    const val MACHINERY = "machinery"
    const val HUMAN_VOICE = "human_voice"
    const val GATE_OPEN = "gate_open"
    const val GATE_CLOSE = "gate_close"
    const val WATER_RUNNING = "water_running"
    const val FOOD_DISPENSER = "food_dispenser"
    const val SILENCE = "silence"

    val allClasses = listOf(
        GOAT_BLEAT, GOAT_DISTRESS, SHEEP_BLEAT, SHEEP_DISTRESS,
        CATTLE_MOO, CATTLE_DISTRESS, CHICKEN_CLUCK, CHICKEN_DISTRESS,
        PREDATOR_DOG, PREDATOR_HYENA, RAIN_HEAVY, WIND_STRONG,
        THUNDER, MACHINERY, HUMAN_VOICE, GATE_OPEN, GATE_CLOSE,
        WATER_RUNNING, FOOD_DISPENSER, SILENCE
    )

    val distressClasses = listOf(
        GOAT_DISTRESS, SHEEP_DISTRESS, CATTLE_DISTRESS,
        CHICKEN_DISTRESS, PREDATOR_DOG, PREDATOR_HYENA
    )

    fun isDistressEvent(soundClass: String): Boolean = distressClasses.contains(soundClass)
}