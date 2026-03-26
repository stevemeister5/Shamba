package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.AudioEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioEventDao {
    @Query("SELECT * FROM audio_event_log ORDER BY timestamp DESC")
    fun getAllAudioEvents(): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAudioEvents(limit: Int = 50): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log WHERE soundClass = :soundClass ORDER BY timestamp DESC")
    fun getEventsByClass(soundClass: String): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log WHERE isAnomaly = 1 ORDER BY timestamp DESC")
    fun getAnomalyEvents(): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log WHERE plotId = :plotId ORDER BY timestamp DESC")
    fun getEventsByPlot(plotId: Long): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log WHERE soundClass IN (:soundClasses) ORDER BY timestamp DESC")
    fun getEventsByClasses(soundClasses: List<String>): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEventsByTimeRange(startTime: Long, endTime: Long): Flow<List<AudioEvent>>

    @Query("SELECT * FROM audio_event_log WHERE healthRecordId = :healthRecordId")
    suspend fun getEventsByHealthRecord(healthRecordId: Long): List<AudioEvent>

    @Query("SELECT * FROM audio_event_log WHERE id = :id")
    suspend fun getAudioEventById(id: Long): AudioEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioEvent: AudioEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audioEvents: List<AudioEvent>)

    @Update
    suspend fun update(audioEvent: AudioEvent)

    @Delete
    suspend fun delete(audioEvent: AudioEvent)

    @Query("DELETE FROM audio_event_log WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM audio_event_log")
    suspend fun deleteAll()

    @Query("UPDATE audio_event_log SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE audio_event_log SET healthRecordId = :healthRecordId WHERE id = :id")
    suspend fun linkToHealthRecord(id: Long, healthRecordId: Long)

    @Query("SELECT * FROM audio_event_log WHERE isSynced = 0")
    suspend fun getUnsyncedAudioEvents(): List<AudioEvent>

    @Query("SELECT COUNT(*) FROM audio_event_log WHERE soundClass = :soundClass AND timestamp >= :since")
    suspend fun getEventCountByClassSince(soundClass: String, since: Long): Int

    @Query("SELECT * FROM audio_event_log WHERE soundClass IN (:distressClasses) AND timestamp >= :since ORDER BY timestamp DESC")
    fun getRecentDistressEvents(distressClasses: List<String>, since: Long): Flow<List<AudioEvent>>

    @Query("SELECT AVG(confidence) FROM audio_event_log WHERE soundClass = :soundClass")
    suspend fun getAverageConfidenceForClass(soundClass: String): Double?

    @Query("SELECT soundClass, COUNT(*) as count FROM audio_event_log GROUP BY soundClass ORDER BY count DESC")
    fun getEventCountByClass(): Flow<List<SoundClassCount>>
}

data class SoundClassCount(
    val soundClass: String,
    val count: Int
)