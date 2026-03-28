package com.shambasmart.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Android Keystore operations for hardware-backed AES-256 encryption.
 * Provides secure key generation, storage, and encryption/decryption operations.
 */
@Singleton
class HardwareKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "shamba_smart_hardware_key"
        private const val KEY_SIZE = 256
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Gets existing hardware-backed key or generates a new one.
     * @return SecretKey for encryption operations
     */
    fun getOrCreateKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            getKey() ?: generateKey()
        } else {
            generateKey()
        }
    }

    /**
     * Retrieves the existing key from Android Keystore.
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
     * Generates a new AES-256 key in Android Keystore with hardware-backed security.
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
            .setUserAuthenticationRequired(false) // Can be enabled for additional security
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts data using the hardware-backed key.
     * @param data The data to encrypt
     * @return Encrypted data with IV prepended
     */
    fun encrypt(data: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        
        // Prepend IV to encrypted data
        return iv + encryptedData
    }

    /**
     * Decrypts data using the hardware-backed key.
     * @param encryptedData The encrypted data with IV prepended
     * @return Decrypted data
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
        val key = getOrCreateKey()
        
        // Extract IV from the beginning
        val iv = encryptedData.copyOfRange(0, 12) // GCM IV is 12 bytes
        val data = encryptedData.copyOfRange(12, encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return cipher.doFinal(data)
    }

    /**
     * Checks if the hardware-backed key exists.
     * @return true if key exists, false otherwise
     */
    fun keyExists(): Boolean {
        return keyStore.containsAlias(KEY_ALIAS)
    }

    /**
     * Deletes the key from Keystore.
     * Use with caution - will require re-encryption of all data.
     */
    fun deleteKey() {
        keyStore.deleteEntry(KEY_ALIAS)
    }

    /**
     * Gets the transformation string for encryption.
     */
    fun getTransformation(): String = TRANSFORMATION
}