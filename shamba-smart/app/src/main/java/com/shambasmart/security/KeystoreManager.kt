package com.shambasmart.security

import android.content.Context
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages database encryption key operations.
 * Delegates to HardwareKeyManager for hardware-backed security.
 * Provides backward compatibility for existing database encryption.
 */
@Singleton
class KeystoreManager @Inject constructor(
    private val context: Context,
    private val hardwareKeyManager: HardwareKeyManager
) {
    /**
     * Gets existing key or generates a new one using hardware-backed security.
     * @return ByteArray passphrase for database encryption
     */
    fun getOrCreatePassphrase(): ByteArray {
        return if (hardwareKeyManager.keyExists()) {
            // Use existing hardware-backed key
            hardwareKeyManager.getOrCreateKey().encoded
        } else {
            // Generate new passphrase and store securely
            generateAndStorePassphrase()
        }
    }

    /**
     * Generates a random passphrase and stores it securely.
     * @return ByteArray containing the random passphrase
     */
    private fun generateAndStorePassphrase(): ByteArray {
        val passphrase = ByteArray(32) // 256-bit key
        SecureRandom().nextBytes(passphrase)
        
        // Store passphrase encrypted with hardware key
        val encryptedPassphrase = hardwareKeyManager.encrypt(passphrase)
        context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
            .edit()
            .putString("encrypted_db_passphrase", encryptedPassphrase.toBase64())
            .apply()
        
        return passphrase
    }

    /**
     * Retrieves the stored passphrase.
     * @return ByteArray passphrase or null if not found
     */
    fun getStoredPassphrase(): ByteArray? {
        val encryptedPassphrase = context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
            .getString("encrypted_db_passphrase", null)
            ?: return null
        
        return try {
            hardwareKeyManager.decrypt(encryptedPassphrase.fromBase64())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a random passphrase for database encryption.
     * @return ByteArray containing the random passphrase
     */
    fun generatePassphrase(): ByteArray {
        val passphrase = ByteArray(32) // 256-bit key
        SecureRandom().nextBytes(passphrase)
        return passphrase
    }

    /**
     * Checks if the key exists.
     * @return true if key exists, false otherwise
     */
    fun keyExists(): Boolean {
        return hardwareKeyManager.keyExists() || getStoredPassphrase() != null
    }

    /**
     * Deletes the key.
     * Use with caution - will require database re-encryption.
     */
    fun deleteKey() {
        hardwareKeyManager.deleteKey()
        context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
            .edit()
            .remove("encrypted_db_passphrase")
            .apply()
    }
}

private fun ByteArray.toBase64(): String {
    return android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
}

private fun String.fromBase64(): ByteArray {
    return android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
}
