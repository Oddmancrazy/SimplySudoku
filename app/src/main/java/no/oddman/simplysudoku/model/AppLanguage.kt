package no.oddman.simplysudoku.model

enum class AppLanguage(
    val storageValue: String,
    val displayName: String,
    val flag: String
) {
    SYSTEM("system", "System", "⚙️"),
    NORWEGIAN_BOKMAL("nb", "Norsk", "🇳🇴"),
    ENGLISH("en", "English", "🇺🇸"),
    SWEDISH("sv", "Svenska", "🇸🇪"),
    DANISH("da", "Dansk", "🇩🇰"),
    GERMAN("de", "Deutsch", "🇩🇪"),
    FRENCH("fr", "Français", "🇫🇷"),
    SPANISH("es", "Español", "🇪🇸"),
    ITALIAN("it", "Italiano", "🇮🇹"),
    DUTCH("nl", "Nederlands", "🇳🇱"),
    HEBREW("he", "עברית", "🇮🇱"),
    ARABIC("ar", "العربية", "🇸🇦"),
    THAI("th", "ไทย", "🇹🇭"),
    CHINESE_SIMPLIFIED("zh", "简体中文", "🇨🇳"),
    KOREAN("ko", "한국어", "🇰🇷"),
    JAPANESE("ja", "日本語", "🇯🇵");

    /**
     * Returns the word for "Language" in this specific language.
     * Used for the settings button label.
     */
    fun getLabel(): String {
        return when (this) {
            NORWEGIAN_BOKMAL -> "Språk"
            SWEDISH -> "Språk"
            DANISH -> "Sprog"
            GERMAN -> "Sprache"
            FRENCH -> "Langue"
            SPANISH -> "Idioma"
            ITALIAN -> "Lingua"
            DUTCH -> "Taal"
            HEBREW -> "שפה"
            ARABIC -> "اللغة"
            THAI -> "ภาษา"
            CHINESE_SIMPLIFIED -> "语言"
            KOREAN -> "언어"
            JAPANESE -> "言語"
            else -> "Language"
        }
    }

    companion object {
        fun fromStorageValue(value: String?): AppLanguage {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }

        fun fromLocaleCode(code: String?): AppLanguage {
            if (code == null) return ENGLISH
            // Match "nb-NO" or "nb" to NORWEGIAN_BOKMAL
            val baseCode = code.split("-")[0].lowercase()
            return entries.firstOrNull { it.storageValue == baseCode } ?: ENGLISH
        }
    }
}

