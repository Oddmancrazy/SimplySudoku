package no.oddman.simplysudoku.data.repository

import android.content.Context
import no.oddman.simplysudoku.data.backup.BackupFileManager
import no.oddman.simplysudoku.model.AppLanguage
import no.oddman.simplysudoku.model.AppSettings
import no.oddman.simplysudoku.model.Difficulty
import no.oddman.simplysudoku.model.GameMode

class SettingsRepository(
    private val context: Context
) {
    private val prefs = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun hasSelectedLanguage(): Boolean {
        return prefs.contains(KEY_LANGUAGE)
    }

    fun getSettings(): AppSettings {
        return AppSettings(
            difficulty = Difficulty.entries.firstOrNull {
                it.name == prefs.getString(KEY_DIFFICULTY, Difficulty.EASY.name)
            } ?: Difficulty.EASY,
            gameMode = GameMode.entries.firstOrNull {
                it.name == prefs.getString(KEY_GAME_MODE, GameMode.MODERN.name)
            } ?: GameMode.MODERN,
            language = AppLanguage.fromStorageValue(
                prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.storageValue)
            ),
            autoBackupEnabled = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false),
            backupUri = prefs.getString(KEY_BACKUP_URI, null)
        )
    }

    fun saveDifficulty(difficulty: Difficulty) {
        prefs.edit()
            .putString(KEY_DIFFICULTY, difficulty.name)
            .apply()

        triggerAutoBackupIfEnabled()
    }

    fun saveGameMode(gameMode: GameMode) {
        prefs.edit()
            .putString(KEY_GAME_MODE, gameMode.name)
            .apply()

        triggerAutoBackupIfEnabled()
    }

    fun saveLanguage(language: AppLanguage) {
        prefs.edit()
            .putString(KEY_LANGUAGE, language.storageValue)
            .apply()

        triggerAutoBackupIfEnabled()
    }

    fun saveAutoBackupEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled)
            .apply()

        triggerAutoBackupIfEnabled()
    }

    fun saveBackupUri(uri: String?) {
        prefs.edit()
            .putString(KEY_BACKUP_URI, uri)
            .apply()

        triggerAutoBackupIfEnabled()
    }

    fun clearBackupUri() {
        prefs.edit()
            .remove(KEY_BACKUP_URI)
            .apply()
    }

    fun applySettings(
        settings: AppSettings,
        triggerAutoBackup: Boolean = true
    ) {
        prefs.edit()
            .putString(KEY_DIFFICULTY, settings.difficulty.name)
            .putString(KEY_GAME_MODE, settings.gameMode.name)
            .putString(KEY_LANGUAGE, settings.language.storageValue)
            .putBoolean(KEY_AUTO_BACKUP_ENABLED, settings.autoBackupEnabled)
            .putString(KEY_BACKUP_URI, settings.backupUri)
            .apply()

        if (triggerAutoBackup) {
            triggerAutoBackupIfEnabled()
        }
    }

    private fun triggerAutoBackupIfEnabled() {
        val settings = getSettings()

        if (!settings.autoBackupEnabled || settings.backupUri.isNullOrBlank()) return

        val records = RecordRepository(context).getAllRecordsSync()

        // Sjekk om det er en mappe (tree) eller en spesifikk fil
        if (settings.backupUri.contains("document")) {
            BackupFileManager.writeBackupToFileUri(
                context = context,
                fileUriString = settings.backupUri,
                records = records,
                settings = settings
            )
        } else {
            BackupFileManager.writeBackupToTreeUri(
                context = context,
                treeUriString = settings.backupUri,
                records = records,
                settings = settings
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "app_settings"

        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_GAME_MODE = "gameMode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_AUTO_BACKUP_ENABLED = "autoBackupEnabled"
        private const val KEY_BACKUP_URI = "backupUri"
    }
}

