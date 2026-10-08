package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecurityManager(private val context: Context) {

    private val KEY_ALIAS = "ExpenseManagerMasterKey"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"

    // In-memory runtime session state
    private var lastActivityTimeMs: Long = System.currentTimeMillis()
    private var isCurrentlyUnlocked: Boolean = false

    init {
        ensureKeystoreKey()
    }

    private fun ensureKeystoreKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Handled gracefully for test/restricted environments
        }
    }

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt$pin$salt".toByteArray(Charsets.UTF_8)
        var hash = md.digest(combined)
        // Perform 1000 rounds of hashing
        for (i in 0 until 1000) {
            hash = md.digest(hash)
        }
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, salt: String, expectedHash: String): Boolean {
        val computed = hashPin(pin, salt)
        return computed == expectedHash
    }

    fun recordActivity() {
        lastActivityTimeMs = System.currentTimeMillis()
    }

    fun isSessionExpired(timeoutSeconds: Int): Boolean {
        if (timeoutSeconds <= 0) return true
        val elapsedSeconds = (System.currentTimeMillis() - lastActivityTimeMs) / 1000
        return elapsedSeconds >= timeoutSeconds
    }

    fun setUnlocked(unlocked: Boolean) {
        isCurrentlyUnlocked = unlocked
        if (unlocked) {
            recordActivity()
        }
    }

    fun isUnlocked(): Boolean = isCurrentlyUnlocked
}
