package com.shambasmart.security

/**
 * Metadata for a database backup file.
 * Used for tracking backup history and validation.
 */
data class BackupMetadata(
    val fileName: String,
    val fileSizeBytes: Long,
    val timestamp: Long,
    val databaseVersion: Int,
    val appVersion: String,
    val checksum: String
)