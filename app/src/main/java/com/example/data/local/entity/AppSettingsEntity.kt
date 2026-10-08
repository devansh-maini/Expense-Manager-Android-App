package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1, // Single row config
    val isPinLockEnabled: Boolean = false,
    val pinSalt: String = "",
    val pinHash: String = "", // Salted SHA-256 hash
    val isBiometricEnabled: Boolean = false,
    val autoLockTimeoutSeconds: Int = 60, // 0 = immediate, 30, 60, 300, etc.
    val isScreenshotProtected: Boolean = false, // WindowManager.LayoutParams.FLAG_SECURE
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val currencySymbol: String = "₹",
    val budgetWarningThreshold: Int = 90 // Default warning percentage
)
