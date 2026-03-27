package com.shambasmart.security

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * WorkManager worker that executes encrypted database backups.
 * Runs on a schedule via BackupScheduler.
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: BackupManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "BackupWorker"
        const val KEY_BACKUP_RESULT = "backup_result"
        const val KEY_BACKUP_PATH = "backup_path"
        const val KEY_BACKUP_SIZE = "backup_size"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Clean old backups (keep last 7 days)
            backupManager.cleanOldBackups(maxAgeDays = 7)

            // Create encrypted backup
            val backupFile = backupManager.createEncryptedBackup()

            if (backupFile != null && backupFile.exists()) {
                // Create backup metadata
                val metadata = BackupMetadata(
                    fileName = backupFile.name,
                    fileSizeBytes = backupFile.length(),
                    timestamp = System.currentTimeMillis(),
                    databaseVersion = 8,
                    appVersion = "1.1.0",
                    checksum = backupManager.calculateChecksum(backupFile)
                )
                backupManager.saveBackupMetadata(metadata)

                Result.success(
                    androidx.work.Data.Builder()
                        .putString(KEY_BACKUP_RESULT, "success")
                        .putString(KEY_BACKUP_PATH, backupFile.absolutePath)
                        .putLong(KEY_BACKUP_SIZE, backupFile.length())
                        .build()
                )
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}