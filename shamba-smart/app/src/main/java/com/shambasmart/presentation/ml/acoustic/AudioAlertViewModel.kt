package com.shambasmart.presentation.ml.acoustic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.MainActivity
import com.shambasmart.R
import com.shambasmart.data.local.dao.AudioEventDao
import com.shambasmart.data.local.dao.HealthRecordDao
import com.shambasmart.data.local.entity.AudioEvent
import com.shambasmart.data.local.entity.SoundClasses
import com.shambasmart.ml.AudioClassificationResult
import com.shambasmart.ml.SoundClassifier
import com.shambasmart.ml.SpectrogramData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioAlertViewModel @Inject constructor(
    application: Application,
    private val soundClassifier: SoundClassifier,
    private val audioEventDao: AudioEventDao,
    private val healthRecordDao: HealthRecordDao
) : AndroidViewModel(application) {

    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastClassification = MutableStateFlow<AudioClassificationResult?>(null)
    val lastClassification: StateFlow<AudioClassificationResult?> = _lastClassification.asStateFlow()

    private val _spectrogram = MutableStateFlow<SpectrogramData?>(null)
    val spectrogram: StateFlow<SpectrogramData?> = _spectrogram.asStateFlow()

    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

    private val _batteryOptimized = MutableStateFlow(false)
    val batteryOptimized: StateFlow<Boolean> = _batteryOptimized.asStateFlow()

    // Recent events from database
    val recentEvents: StateFlow<List<AudioEvent>> = audioEventDao.getRecentAudioEvents(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val totalEvents: StateFlow<Int> = audioEventDao.getAllAudioEvents()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val distressCount: StateFlow<Int> = audioEventDao.getEventsByClasses(SoundClasses.distressClasses)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageConfidence: StateFlow<Double> = audioEventDao.getAllAudioEvents()
        .map { events ->
            if (events.isEmpty()) 0.0
            else events.map { it.confidence }.average()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private var currentPlotId: Long? = null
    private var deviceId: String = Build.MODEL ?: "unknown"
    private var lastDistressTime = 0L

    init {
        initializeClassifier()
        observeClassifierState()
    }

    private fun initializeClassifier() {
        viewModelScope.launch {
            val initialized = soundClassifier.initialize()
            if (!initialized) {
                // Handle initialization failure
            }
        }
    }

    private fun observeClassifierState() {
        viewModelScope.launch {
            soundClassifier.isListeningFlow.collect { listening ->
                _isListening.value = listening
            }
        }

        viewModelScope.launch {
            soundClassifier.lastClassification.collect { result ->
                _lastClassification.value = result
            }
        }

        viewModelScope.launch {
            soundClassifier.spectrogram.collect { data ->
                _spectrogram.value = data
            }
        }

        viewModelScope.launch {
            soundClassifier.batteryOptimized.collect { optimized ->
                _batteryOptimized.value = optimized
            }
        }
    }

    fun toggleListening() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        soundClassifier.startListening(
            onClassification = { result ->
                handleClassification(result)
            },
            onDistressDetected = { result ->
                handleDistressDetection(result)
            }
        )
    }

    private fun stopListening() {
        soundClassifier.stopListening()
    }

    private fun handleClassification(result: AudioClassificationResult) {
        viewModelScope.launch {
            val audioEvent = AudioEvent(
                timestamp = result.timestamp,
                soundClass = result.soundClass,
                confidence = result.confidence,
                plotId = currentPlotId,
                deviceId = deviceId,
                isAnomaly = SoundClasses.isDistressEvent(result.soundClass)
            )

            audioEventDao.insert(audioEvent)
        }
    }

    private fun handleDistressDetection(result: AudioClassificationResult) {
        val now = System.currentTimeMillis()
        
        // Throttle distress alerts (max 1 per 10 seconds)
        if (now - lastDistressTime < 10000) return
        lastDistressTime = now

        viewModelScope.launch {
            // Vibrate device for distress alerts
            vibrateDevice()

            // Save distress event
            val audioEvent = AudioEvent(
                timestamp = result.timestamp,
                soundClass = result.soundClass,
                confidence = result.confidence,
                plotId = currentPlotId,
                deviceId = deviceId,
                isAnomaly = true
            )

            audioEventDao.insert(audioEvent)

            // Show notification
            showDistressNotification(result)
        }
    }

    private fun showDistressNotification(result: AudioClassificationResult) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Acoustic Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for livestock distress sounds"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Format sound class for display
        val soundClassDisplay = result.soundClass.split("_").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Assuming notification icon exists
            .setContentTitle("⚠️ Livestock Distress Detected")
            .setContentText("$soundClassDisplay (${(result.confidence * 100).toInt()}% confidence)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Sound: $soundClassDisplay\nConfidence: ${(result.confidence * 100).toInt()}%\nTime: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(result.timestamp))}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun vibrateDevice() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
        } catch (e: Exception) {
            // Handle vibration error
        }
    }

    fun toggleBatteryOptimization() {
        val newValue = !_batteryOptimized.value
        _batteryOptimized.value = newValue
        soundClassifier.setBatteryOptimized(newValue)
    }

    fun startCalibration() {
        viewModelScope.launch {
            _isCalibrating.value = true
            
            // Record a 3-second sample
            val audioData = soundClassifier.recordSample(3000)
            
            if (audioData != null) {
                // Calibrate threshold based on recorded sample
                val threshold = soundClassifier.calibrateThreshold(audioData)
                
                // TODO: Save calibration settings to preferences
                // preferences.setAudioThreshold(threshold)
            }
            
            delay(500) // Brief delay for UX
            _isCalibrating.value = false
        }
    }

    fun setCurrentPlot(plotId: Long) {
        currentPlotId = plotId
    }

    fun clearEvents() {
        viewModelScope.launch {
            audioEventDao.deleteAll()
        }
    }

    fun linkEventToHealthRecord(eventId: Long, healthRecordId: Long) {
        viewModelScope.launch {
            audioEventDao.linkToHealthRecord(eventId, healthRecordId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundClassifier.release()
    }

    companion object {
        private const val CHANNEL_ID = "acoustic_alerts"
        private const val NOTIFICATION_ID = 1001
    }
}
