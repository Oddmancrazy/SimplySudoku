package no.oddman.simplysudoku.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import no.oddman.simplysudoku.R
import no.oddman.simplysudoku.data.backup.BackupFileManager
import no.oddman.simplysudoku.data.repository.RecordRepository
import no.oddman.simplysudoku.data.repository.SettingsRepository
import no.oddman.simplysudoku.model.AppLanguage
import no.oddman.simplysudoku.model.AppSettings
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

        val messageRes = when {
            enabled && currentSettings.backupUri.isNullOrBlank() ->
                R.string.auto_backup_on_no_uri
            enabled ->
                R.string.auto_backup_on
            else ->
                R.string.auto_backup_off
        }

        refreshWithResMessage(messageRes)
    }

    fun setBackupUri(uri: String?) {
        settingsRepository.saveBackupUri(uri)
        refreshWithResMessage(R.string.folder_selected)
    }

    fun clearBackupUri() {
        settingsRepository.clearBackupUri()
        refreshWithResMessage(R.string.folder_removed)
    }

    fun exportBackupNow() {
        val backupUri = _uiState.value.settings.backupUri
        if (backupUri.isNullOrBlank()) return
        exportToFolder(backupUri)
    }

    fun exportToSingleFile(fileUriString: String) {
        _uiState.update { it.copy(isWorking = true, statusMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val records = recordRepository.getAllRecordsSync()
            val settings = settingsRepository.getSettings()

            val result = BackupFileManager.writeBackupToFileUri(
                context = appContext,
                fileUriString = fileUriString,
                records = records,
                settings = settings
            )

            if (result.isSuccess) {
                // Lagre denne URI-en som fast backup-plassering
                settingsRepository.saveBackupUri(fileUriString)
            }

            _uiState.update {
                it.copy(
                    settings = settingsRepository.getSettings(),
                    isWorking = false,
                    statusMessage = if (result.isSuccess) appContext.getString(R.string.backup_saved_connected) else appContext.getString(R.string.failed_to_save)
                )
            }
        }
    }

    private fun exportToFolder(folderUri: String) {
        _uiState.update { it.copy(isWorking = true, statusMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val records = recordRepository.getAllRecordsSync()
            val settings = settingsRepository.getSettings()

            val result = BackupFileManager.writeBackupToTreeUri(
                context = appContext,
                treeUriString = folderUri,
                records = records,
                settings = settings
            )

            _uiState.update {
                it.copy(
                    isWorking = false,
                    statusMessage = if (result.isSuccess) appContext.getString(R.string.backup_exported) else appContext.getString(R.string.export_failed)
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
                        statusMessage = appContext.getString(R.string.failed_to_import)
                    )
                }
                return@launch
            }

            val snapshot = importResult.getOrNull()
            if (snapshot == null) {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        statusMessage = appContext.getString(R.string.backup_empty_invalid)
                    )
                }
                return@launch
            }

            // Fletter inn rekorder i stedet for å erstatte alt
            recordRepository.mergeRecords(
                newRecords = snapshot.records,
                triggerAutoBackup = false
            )

            // Vi bruker innstillingene fra backup-fila, 
            // men vi TVINGER den til å bruke den nye fil-stien vi nettopp valgte.
            val updatedSettings = snapshot.settings.copy(
                backupUri = fileUriString,
                autoBackupEnabled = true // Vi slår det på siden brukeren aktivt importerer
            )

            settingsRepository.applySettings(
                settings = updatedSettings,
                triggerAutoBackup = false
            )

            _uiState.update {
                it.copy(
                    settings = settingsRepository.getSettings(),
                    isWorking = false,
                    statusMessage = appContext.getString(R.string.backup_merged_connected)
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
                it.copy(statusMessage = appContext.getString(R.string.backup_ready_share))
            }
            result.getOrNull()
        } else {
            _uiState.update {
                it.copy(statusMessage = appContext.getString(R.string.failed_share_backup))
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

    private fun refreshWithResMessage(messageRes: Int) {
        _uiState.update {
            it.copy(
                settings = settingsRepository.getSettings(),
                statusMessage = appContext.getString(messageRes)
            )
        }
    }
}

