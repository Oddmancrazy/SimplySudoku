package com.example.simplysudoku.model

data class AppSettings(
    val difficulty: Difficulty = Difficulty.EASY,
    val gameMode: GameMode = GameMode.MODERN,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val autoBackupEnabled: Boolean = false,
    val backupUri: String? = null
)