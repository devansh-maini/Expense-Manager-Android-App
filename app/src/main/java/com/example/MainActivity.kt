package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.security.SecurityManager
import com.example.ui.ExpenseApp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var securityManager: SecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(applicationContext)
        securityManager = SecurityManager(applicationContext)

        // Clear FLAG_SECURE immediately to prevent black screen in streaming emulator
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Reset any existing database record where isScreenshotProtected was set to true by default
        lifecycleScope.launch {
            val settings = database.appSettingsDao().getSettingsSync()
            if (settings != null && settings.isScreenshotProtected) {
                database.appSettingsDao().insertOrUpdate(settings.copy(isScreenshotProtected = false))
            }
        }

        // Observe screenshot protection preference
        // NOTE: Streaming web emulators capture display frames via virtual display.
        // FLAG_SECURE blocks frame capture and renders a solid black screen in emulators.
        // Therefore, we only apply FLAG_SECURE on real physical devices.
        lifecycleScope.launch {
            database.appSettingsDao().getSettingsFlow().collect { settings ->
                if (!isEmulator() && settings?.isScreenshotProtected == true) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        setContent {
            MyApplicationTheme {
                ExpenseApp(
                    database = database,
                    securityManager = securityManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check auto-lock timeout
        lifecycleScope.launch {
            val settings = database.appSettingsDao().getSettingsSync()
            if (settings != null && settings.isPinLockEnabled) {
                if (securityManager.isSessionExpired(settings.autoLockTimeoutSeconds)) {
                    securityManager.setUnlocked(false)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        securityManager.recordActivity()
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}

