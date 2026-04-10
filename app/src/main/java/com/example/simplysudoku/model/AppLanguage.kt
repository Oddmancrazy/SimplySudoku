package com.example.simplysudoku.model

enum class AppLanguage(
    val storageValue: String,
    val displayName: String
) {
    SYSTEM("system", "System"),
    NORWEGIAN_BOKMAL("nb", "Norsk"),
    ENGLISH("en", "English");

    companion object {
        fun fromStorageValue(value: String?): AppLanguage {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}