package com.example.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BackupStatus {
    object Idle : BackupStatus()
    data class Success(val message: String, val data: String? = null) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}

class BackupViewModel(private val backupRepository: BackupRepository) : ViewModel() {

    private val _status = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val status: StateFlow<BackupStatus> = _status

    fun exportJson() {
        viewModelScope.launch {
            try {
                val json = backupRepository.exportToJson()
                _status.value = BackupStatus.Success("JSON Export generated successfully!", json)
            } catch (e: Exception) {
                _status.value = BackupStatus.Error("Failed to export: ${e.localizedMessage}")
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            try {
                val csv = backupRepository.exportExpensesToCsv()
                _status.value = BackupStatus.Success("CSV Export generated successfully!", csv)
            } catch (e: Exception) {
                _status.value = BackupStatus.Error("Failed to export: ${e.localizedMessage}")
            }
        }
    }

    fun restoreJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val success = backupRepository.restoreFromJson(jsonString)
                if (success) {
                    _status.value = BackupStatus.Success("All records successfully restored into database!")
                } else {
                    _status.value = BackupStatus.Error("Failed to parse JSON backup format.")
                }
            } catch (e: Exception) {
                _status.value = BackupStatus.Error("Restore error: ${e.localizedMessage}")
            }
        }
    }

    fun clearStatus() {
        _status.value = BackupStatus.Idle
    }
}
