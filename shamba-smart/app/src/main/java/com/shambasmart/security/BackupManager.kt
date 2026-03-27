package com.shambasmart.security

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encrypted backup and restore operations for the database.
 * Supports backup to SD card with AES-256 encryption.
 */
@Singleton
class BackupManager @Inject constructor(
    private val context: Context,
    private val hardwareKeyManager: HardwareKeyManager
) {
    companion object {
        private const val BACKUP_DIR = "ShambaBackups"
        private const val BACKUP_EXTENSION = ".shamba"
        private const val DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
    }

    /**
     * Creates an encrypted backup of the database to SD card.
     * @return The backup file if successful, null otherwise
     */
    suspend fun createEncryptedBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("shamba_smart.db")
            if (!dbFile.exists()) {
                return@withContext null
            }

            // Create backup directory
            val backupDir = getBackupDirectory()
            backupDir.mkdirs()

            // Generate backup filename with timestamp
            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "shamba_backup_$timestamp$BACKUP_EXTENSION")

            // Create encrypted backup
            createEncryptedZip(dbFile, backupFile)

            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restores the database from an encrypted backup file.
     * @param backupFile The backup file to restore from
     * @return true if restoration was successful, false otherwise
     */
    suspend fun restoreFromBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("shamba_smart.db")
            
            // Delete existing database if it exists
            if (dbFile.exists()) {
                dbFile.delete()
            }

            // Restore from encrypted backup
            restoreFromEncryptedZip(backupFile, dbFile)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Gets the backup directory on SD card.
     * @return The backup directory File
     */
    fun getBackupDirectory(): File {
        val externalStorage = Environment.getExternalStorageDirectory()
        return File(externalStorage, BACKUP_DIR)
    }

    /**
     * Lists all available backup files.
     * @return List of backup files sorted by date (newest first)
     */
    fun listBackups(): List<File> {
        val backupDir = getBackupDirectory()
        if (!backupDir.exists()) {
            return emptyList()
        }

        return backupDir.listFiles { file ->
            file.isFile && file.name.endsWith(BACKUP_EXTENSION)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Gets the latest backup file.
     * @return The latest backup file or null if no backups exist
     */
    fun getLatestBackup(): File? {
        return listBackups().firstOrNull()
    }

    /**
     * Deletes a backup file.
     * @param backupFile The backup file to delete
     * @return true if deletion was successful, false otherwise
     */
    fun deleteBackup(backupFile: File): Boolean {
        return try {
            backupFile.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the size of a backup file in bytes.
     * @param backupFile The backup file
     * @return Size in bytes or -1 if file doesn't exist
     */
    fun getBackupSize(backupFile: File): Long {
        return if (backupFile.exists()) backupFile.length() else -1
    }

    private fun createEncryptedZip(sourceFile: File, targetFile: File) {
        ZipOutputStream(FileOutputStream(targetFile)).use { zipOut ->
            val entry = ZipEntry(sourceFile.name)
            zipOut.putNextEntry(entry)

            FileInputStream(sourceFile).use { fis ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    // Encrypt the data before writing
                    val encryptedChunk = hardwareKeyManager.encrypt(buffer.copyOf(bytesRead))
                    zipOut.write(encryptedChunk)
                }
            }

            zipOut.closeEntry()
        }
    }

    private fun restoreFromEncryptedZip(sourceFile: File, targetFile: File) {
        ZipInputStream(FileInputStream(sourceFile)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    FileOutputStream(targetFile).use { fos ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (zipIn.read(buffer).also { bytesRead = it } != -1) {
                            // Decrypt the data before writing
                            val decryptedChunk = hardwareKeyManager.decrypt(buffer.copyOf(bytesRead))
                            fos.write(decryptedChunk)
                        }
                    }
                }
                entry = zipIn.nextEntry
            }
        }
    }

    /**
     * Cleans old backups, keeping only backups from the last N days.
     * @param maxAgeDays Maximum age of backups to keep
     */
    suspend fun cleanOldBackups(maxAgeDays: Int = 7) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
        val backupDir = getBackupDirectory()
        if (!backupDir.exists()) return@withContext

        backupDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(BACKUP_EXTENSION) && file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }

    /**
     * Calculates SHA-256 checksum of a file.
     * @param file The file to calculate checksum for
     * @return Hex string of the checksum
     */
    fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Saves backup metadata to a JSON file alongside the backup.
     * @param metadata The backup metadata to save
     */
    suspend fun saveBackupMetadata(metadata: BackupMetadata) = withContext(Dispatchers.IO) {
        try {
            val backupDir = getBackupDirectory()
            val metadataFile = File(backupDir, "${metadata.fileName}.metadata.json")
            val gson = Gson()
            metadataFile.writeText(gson.toJson(metadata))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads backup metadata from a JSON file.
     * @param backupFile The backup file to load metadata for
     * @return BackupMetadata or null if not found
     */
    suspend fun loadBackupMetadata(backupFile: File): BackupMetadata? = withContext(Dispatchers.IO) {
        try {
            val metadataFile = File(backupFile.parent, "${backupFile.name}.metadata.json")
            if (metadataFile.exists()) {
                val gson = Gson()
                gson.fromJson(metadataFile.readText(), BackupMetadata::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
