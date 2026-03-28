package com.shambasmart.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
     * Gets existing key or generates a new one using hardware-backed security.
     * @return SecretKey for encryption operations
     */
    fun getOrCreateKey(): javax.crypto.SecretKey {
        return hardwareKeyManager.getOrCreateKey()
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

    /**
     * Rotates the encryption key by generating a new passphrase
     * and re-encrypting it with the hardware key.
     * @return The new passphrase for database re-encryption
     */
    fun rotateKey(): ByteArray {
        // Generate new passphrase
        val newPassphrase = generatePassphrase()
        
        // Store new passphrase encrypted with hardware key
        val encryptedPassphrase = hardwareKeyManager.encrypt(newPassphrase)
        context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
            .edit()
            .putString("encrypted_db_passphrase", encryptedPassphrase.toBase64())
            .putLong("key_rotated_at", System.currentTimeMillis())
            .apply()
        
        return newPassphrase
    }

    /**
     * Verifies the stored key can be decrypted correctly.
     * @return true if key is valid, false otherwise
     */
    fun verifyKey(): Boolean {
        return try {
            val passphrase = getStoredPassphrase()
            passphrase != null && passphrase.size == 32
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the timestamp of the last key rotation.
     * @return Timestamp in milliseconds, or 0 if never rotated
     */
    fun getKeyRotationTimestamp(): Long {
        return context.getSharedPreferences("shamba_security", Context.MODE_PRIVATE)
            .getLong("key_rotated_at", 0L)
    }
}

private fun ByteArray.toBase64(): String {
    return android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
}

private fun String.fromBase64(): ByteArray {
    return android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
}