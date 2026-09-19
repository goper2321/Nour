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
    ),
    SPANISH(
        code = "es",
        displayName = "Spanish",
        nativeName = "Español",
        isRtl = false
    ),
    GERMAN(
        code = "de",
        displayName = "German",
        nativeName = "Deutsch",
        isRtl = false
    ),
    MALAY(
        code = "ms",
        displayName = "Malay",
        nativeName = "Bahasa Melayu",
        isRtl = false
    ),
    BENGALI(
        code = "bn",
        displayName = "Bengali",
        nativeName = "বাংলা",
        isRtl = false
    ),
    PERSIAN(
        code = "fa",
        displayName = "Persian",
        nativeName = "فارسی",
        isRtl = true
    ),
    HAUSA(
        code = "ha",
        displayName = "Hausa",
        nativeName = "Hausa",
        isRtl = false
    ),
    PASHTO(
        code = "ps",
        displayName = "Pashto",
        nativeName = "پښتو",
        isRtl = true
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिन्दी",
        isRtl = false
    );

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
