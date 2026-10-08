package com.example.data.repository

import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val appSettingsDao: AppSettingsDao) {
    val settingsFlow: Flow<AppSettingsEntity?> = appSettingsDao.getSettingsFlow()

    suspend fun getSettingsSync(): AppSettingsEntity {
        return appSettingsDao.getSettingsSync() ?: AppSettingsEntity().also {
            appSettingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        appSettingsDao.insertOrUpdate(settings)
    }

    suspend fun updatePin(enabled: Boolean, salt: String, hash: String) {
        val current = getSettingsSync()
        appSettingsDao.insertOrUpdate(
            current.copy(
                isPinLockEnabled = enabled,
                pinSalt = salt,
                pinHash = hash
            )
        )
    }

    suspend fun updateBiometric(enabled: Boolean) {
        val current = getSettingsSync()
        appSettingsDao.insertOrUpdate(current.copy(isBiometricEnabled = enabled))
    }

    suspend fun updateAutoLockTimeout(seconds: Int) {
        val current = getSettingsSync()
        appSettingsDao.insertOrUpdate(current.copy(autoLockTimeoutSeconds = seconds))
    }

    suspend fun updateScreenshotProtection(protected: Boolean) {
        val current = getSettingsSync()
        appSettingsDao.insertOrUpdate(current.copy(isScreenshotProtected = protected))
    }

    suspend fun updateThemeMode(theme: String) {
        val current = getSettingsSync()
        appSettingsDao.insertOrUpdate(current.copy(themeMode = theme))
    }

    suspend fun updateBudgetThreshold(threshold: Int) {
        val current = getSettingsSync()
        appSettingsDao.insertOrUpdate(current.copy(budgetWarningThreshold = threshold))
    }
}
