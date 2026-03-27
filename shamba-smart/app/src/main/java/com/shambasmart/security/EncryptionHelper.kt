package com.shambasmart.security

import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles encryption and decryption of sensitive data using Android Keystore.
 * Used to securely store the database passphrase.
 */
@Singleton
class EncryptionHelper @Inject constructor(
    private val keystoreManager: KeystoreManager
) {
    companion object {
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val IV_SIZE = 16 // AES block size
        private const val KEY_SIZE = 32 // 256-bit key
    }

    /**
     * Encrypts plaintext using the Keystore key.
     * @param plainText The text to encrypt
     * @return Base64-encoded string containing IV + encrypted data
     */
    fun encrypt(plainText: String): String {
        val key = keystoreManager.getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine IV + encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts ciphertext using the Keystore key.
     * @param encryptedText Base64-encoded string containing IV + encrypted data
     * @return Decrypted plaintext string
     * @throws SecurityException if decryption fails
     */
    fun decrypt(encryptedText: String): String {
        return try {
            val key = keystoreManager.getOrCreateKey()
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // Extract IV and encrypted data
            val iv = combined.copyOfRange(0, IV_SIZE)
            val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt data", e)
        }
    }

    /**
     * Encrypts a byte array (e.g., database passphrase).
     * @param data The bytes to encrypt
     * @return Base64-encoded encrypted string
     */
    fun encryptBytes(data: ByteArray): String {
        val key = keystoreManager.getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)

        // Combine IV + encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts to a byte array.
     * @param encryptedText Base64-encoded encrypted string
     * @return Decrypted byte array
     */
    fun decryptToBytes(encryptedText: String): ByteArray {
        return try {
            val key = keystoreManager.getOrCreateKey()
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // Extract IV and encrypted data
            val iv = combined.copyOfRange(0, IV_SIZE)
            val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

            cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt data", e)
        }
    }

    /**
     * Generates a secure random passphrase for database encryption.
     * @return ByteArray containing the random passphrase
     */
    fun generateSecurePassphrase(): ByteArray {
        val passphrase = ByteArray(KEY_SIZE)
        SecureRandom().nextBytes(passphrase)
        return passphrase
    }

    /**
     * Migrates database from old passphrase to new Keystore-protected passphrase.
     * @param oldPassphrase The existing hardcoded passphrase
     * @return The new encrypted passphrase string
     */
    fun migratePassphrase(oldPassphrase: ByteArray): String {
        // Generate new secure passphrase
        val newPassphrase = generateSecurePassphrase()

        // Encrypt new passphrase with Keystore
        return encryptBytes(newPassphrase)
    }
}