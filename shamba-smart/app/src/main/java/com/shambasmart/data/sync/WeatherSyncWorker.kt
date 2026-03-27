package com.shambasmart.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shambasmart.data.repository.WeatherCacheRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val weatherCacheRepository: WeatherCacheRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "weather_sync_worker"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Clear expired cache first
                weatherCacheRepository.clearExpiredCache()

                // Refresh weather data
                val result = weatherCacheRepository.refreshWeatherCache()

                result.fold(
                    onSuccess = { cachedDays ->
                        // Log success
                        Result.success()
                    },
                    onFailure = { error ->
                        // If we have cached data, return success with retry
                        val cachedCount = weatherCacheRepository.getValidCacheCount()
                        if (cachedCount > 0) {
                            Result.retry()
                        } else {
                            Result.failure()
                        }
                    }
                )
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}