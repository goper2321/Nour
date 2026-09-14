package com.example.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        isRtl = false
    ),
    ARABIC(
        code = "ar",
        displayName = "Arabic",
        nativeName = "العربية",
        isRtl = true
    ),
    FRENCH(
        code = "fr",
        displayName = "French",
        nativeName = "Français",
        isRtl = false
    ),
    TURKISH(
        code = "tr",
        displayName = "Turkish",
        nativeName = "Türkçe",
        isRtl = false
    ),
    INDONESIAN(
        code = "id",
        displayName = "Indonesian",
        nativeName = "Bahasa Indonesia",
        isRtl = false
    ),
    URDU(
        code = "ur",
        displayName = "Urdu",
        nativeName = "اردو",
        isRtl = true
    );

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
