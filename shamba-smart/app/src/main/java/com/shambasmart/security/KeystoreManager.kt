package com.shambasmart.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Android Keystore operations for database encryption key.
 * Generates and retrieves AES keys securely stored in Android Keystore.
 */
@Singleton
class KeystoreManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "shamba_smart_db_key"
        private const val KEY_SIZE = 256
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Gets existing key or generates a new one if it doesn't exist.
     * @return SecretKey for database encryption
     */
    fun getOrCreateKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            getKey() ?: generateKey()
        } else {
            generateKey()
        }
    }

    /**
     * Retrieves the existing key from Keystore.
     * @return SecretKey or null if not found
     */
    fun getKey(): SecretKey? {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a new AES key in Android Keystore.
     * @return The generated SecretKey
     */
    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setKeySize(KEY_SIZE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
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
     * Checks if the key exists in Keystore.
     * @return true if key exists, false otherwise
     */
    fun keyExists(): Boolean {
        return keyStore.containsAlias(KEY_ALIAS)
    }

    /**
     * Deletes the key from Keystore.
     * Use with caution - will require database re-encryption.
     */
    fun deleteKey() {
        keyStore.deleteEntry(KEY_ALIAS)
    }

    /**
     * Gets the transformation string for encryption.
     */
    fun getTransformation(): String = TRANSFORMATION
}