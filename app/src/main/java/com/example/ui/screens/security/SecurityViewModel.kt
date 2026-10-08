package com.example.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.repository.SettingsRepository
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val settingsRepository: SettingsRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val settings: StateFlow<AppSettingsEntity?> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setPin(pin: String) {
        viewModelScope.launch {
            val salt = securityManager.generateSalt()
            val hash = securityManager.hashPin(pin, salt)
            settingsRepository.updatePin(enabled = true, salt = salt, hash = hash)
            securityManager.setUnlocked(true)
        }
    }

    fun removePin() {
        viewModelScope.launch {
            settingsRepository.updatePin(enabled = false, salt = "", hash = "")
        }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBiometric(enabled)
        }
    }

    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.updateAutoLockTimeout(seconds)
        }
    }

    fun setScreenshotProtection(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateScreenshotProtection(enabled)
        }
    }
}
