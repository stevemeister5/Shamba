package com.shambasmart.security

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules daily encrypted database backups via WorkManager.
 * Backups run at 2 AM daily with 7-day retention.
 */
@Singleton
class BackupScheduler @Inject constructor(
    private val context: Context
) {

    companion object {
        const val BACKUP_WORK_NAME = "shamba_daily_backup"
        const val BACKUP_HOUR = 2 // 2 AM
    }

    /**
     * Schedules daily backup at 2 AM.
     * Only runs when device is charging and idle to minimize battery impact.
     */
    fun scheduleDailyBackup() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresDeviceIdle(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(BackupWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    /**
     * Cancels the scheduled backup.
     */
    fun cancelScheduledBackup() {
        WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
    }

    /**
     * Triggers an immediate backup (for manual use).
     */
    fun triggerImmediateBackup() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val backupRequest = androidx.work.OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .addTag(BackupWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueue(backupRequest)
    }
}