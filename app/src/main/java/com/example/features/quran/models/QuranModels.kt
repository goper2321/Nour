package com.example.features.quran.models

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishMeaning: String,
    val revelationType: RevelationType,
    val ayahCount: Int,
    val startPage: Int,
    val juzNumber: Int
)

enum class RevelationType(val label: String) {
    MECCAN("Meccan"),
    MEDINAN("Medinan");

    fun localizedLabel(strings: com.example.localization.AppStrings): String = when (this) {
        MECCAN -> strings.meccan
        MEDINAN -> strings.medinan
    }
}

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val translations: Map<String, String> = emptyMap(),
    val tafsirEntries: Map<String, String> = emptyMap()
) {
    fun getTranslation(editionId: String, fallback: String = "Translation not available"): String {
        return translations[editionId] ?: fallback
    }

    fun getTafsir(editionId: String): String? = tafsirEntries[editionId]
}

data class MushafSegment(
    val surahNumber: Int,
    val showSurahHeader: Boolean, // true when this segment contains ayah 1 of its surah
    val ayahs: List<Ayah>
)

data class MushafPage(
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbQuarter: Int,
    val surahHeader: Surah?,      // kept for existing callers/tests: header of the first segment when showSurahHeader, else null
    val ayahs: List<Ayah>,
    val segments: List<MushafSegment> = emptyList()
)

data class TranslationEdition(
    val id: String,
    val name: String,
    val language: String,
    val author: String,
    val bundled: Boolean = false
)

// TafsirEdition removed in Wave A: no tafsir is bundled, so advertising
// editions the app cannot render was dishonest.
