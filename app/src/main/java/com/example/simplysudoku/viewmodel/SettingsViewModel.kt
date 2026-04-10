package com.example.simplysudoku.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.simplysudoku.data.backup.BackupFileManager
import com.example.simplysudoku.data.repository.RecordRepository
import com.example.simplysudoku.data.repository.SettingsRepository
import com.example.simplysudoku.model.AppLanguage
import com.example.simplysudoku.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isWorking: Boolean = false,
    val statusMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val recordRepository = RecordRepository(appContext)

    private val _uiState = MutableStateFlow(
        SettingsUiState(settings = settingsRepository.getSettings())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun refresh() {
        _uiState.update {
            it.copy(settings = settingsRepository.getSettings())
        }
    }

    fun clearStatusMessage() {
        _uiState.update {
            it.copy(statusMessage = null)
        }
    }

    fun setLanguage(language: AppLanguage) {
        settingsRepository.saveLanguage(language)
        
        // Bruk AppCompatDelegate for å aktivere språket umiddelbart
        val appLocale: LocaleListCompat = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.storageValue)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)

        refresh()
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        settingsRepository.saveAutoBackupEnabled(enabled)

        val currentSettings = settingsRepository.getSettings()

        val message = when {
            enabled && currentSettings.backupUri.isNullOrBlank() ->
                "Automatisk backup er slått på. Velg lagringsmappe for å ta det i bruk."
            enabled ->
                "Automatisk backup er slått på."
            else ->
                "Automatisk backup er slått av."
        }

        refreshWithMessage(message)
    }

    fun setBackupUri(uri: String?) {
        settingsRepository.saveBackupUri(uri)
        refreshWithMessage("Lagringsmappe valgt.")
    }

    fun clearBackupUri() {
        settingsRepository.clearBackupUri()
        refreshWithMessage("Lagringsmappe fjernet.")
    }

    fun exportBackupNow() {
        val backupUri = _uiState.value.settings.backupUri

        if (backupUri.isNullOrBlank()) {
            refreshWithMessage("Velg lagringsmappe først.")
            return
        }

        _uiState.update { it.copy(isWorking = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val records = recordRepository.getAllRecordsSync()
            val settings = settingsRepository.getSettings()

            val result = BackupFileManager.writeBackupToTreeUri(
                context = appContext,
                treeUriString = backupUri,
                records = records,
                settings = settings
            )

            _uiState.update {
                it.copy(
                    settings = settingsRepository.getSettings(),
                    isWorking = false,
                    statusMessage = if (result.isSuccess) {
                        "Backup eksportert."
                    } else {
                        "Klarte ikke å eksportere backup."
                    }
                )
            }
        }
    }

    fun importBackupFromFile(fileUriString: String) {
        _uiState.update { it.copy(isWorking = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val importResult = BackupFileManager.importBackupFromFileUri(
                context = appContext,
                fileUriString = fileUriString
            )

            if (importResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        statusMessage = "Klarte ikke å importere backup."
                    )
                }
                return@launch
            }

            val snapshot = importResult.getOrNull()
            if (snapshot == null) {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        statusMessage = "Backupfilen var tom eller ugyldig."
                    )
                }
                return@launch
            }

            recordRepository.replaceAllRecords(
                records = snapshot.records,
                triggerAutoBackup = false
            )

            settingsRepository.applySettings(
                settings = snapshot.settings,
                triggerAutoBackup = false
            )

            _uiState.update {
                it.copy(
                    settings = settingsRepository.getSettings(),
                    isWorking = false,
                    statusMessage = "Backup importert."
                )
            }
        }
    }

    fun createShareBackupUri(): Uri? {
        val records = recordRepository.getAllRecordsSync()
        val settings = settingsRepository.getSettings()

        val result = BackupFileManager.createShareBackupUri(
            context = appContext,
            records = records,
            settings = settings
        )

        return if (result.isSuccess) {
            _uiState.update {
                it.copy(statusMessage = "Backup klar til deling.")
            }
            result.getOrNull()
        } else {
            _uiState.update {
                it.copy(statusMessage = "Klarte ikke å lage delbar backup.")
            }
            null
        }
    }

    private fun refreshWithMessage(message: String) {
        _uiState.update {
            it.copy(
                settings = settingsRepository.getSettings(),
                statusMessage = message
            )
        }
    }
}