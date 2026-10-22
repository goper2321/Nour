package com.example

import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranRepository
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that the search index (precomputed normalized verse text and map-based
 * surah lookups) returns exactly the same results as the original per-keystroke
 * scan it replaced. The reference implementation below is a verbatim copy of that
 * original algorithm, deliberately independent of the indexed code paths.
 */
class QuranSearchTest {

    private fun referenceSearchPairs(query: String): List<Pair<Int, Int>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val normalizedQuery = QuranRepository.normalizeArabic(trimmed).lowercase(Locale.ROOT)

        return QuranCorpus.ayahs.filter { ayah ->
            val matchesArabic = QuranRepository.normalizeArabic(ayah.arabicText).lowercase(Locale.ROOT).contains(normalizedQuery)
            val surah = QuranRepository.ALL_SURAHS.firstOrNull { it.number == ayah.surahNumber }
                ?: QuranRepository.ALL_SURAHS.first()
            val matchesSurah = surah.nameEnglish.contains(trimmed, ignoreCase = true) ||
                               surah.nameArabic.contains(trimmed)
            matchesArabic || matchesSurah
        }.map { it.surahNumber to it.ayahNumber }
    }

    private fun searchPairs(query: String): List<Pair<Int, Int>> =
        QuranRepository.search(query).map { it.surahNumber to it.ayahNumber }

    @Test
    fun searchMatchesOriginalPerKeystrokeScan() {
        val queries = listOf(
            "الله",      // very common Arabic term
            "الرحمن",    // diacritic-free query against decorated Uthmani text
            "الرحم",     // partial word
            "Yusuf",     // English surah name
            "yusuf",     // case-insensitive English surah name
            "Muhammad",  // English surah name, multi-match potential
            "الناس",     // Arabic surah name that also appears in verse text
            "zzzzz",     // no matches
            " ",         // blank
            ""           // empty
        )
        queries.forEach { query ->
            assertEquals("Query '$query'", referenceSearchPairs(query), searchPairs(query))
        }
    }

    @Test
    fun surahNameSearchReturnsWholeSurah() {
        // "Yusuf" is surah 12's English name; its Arabic verses never contain the
        // Latin string, so every hit must be surah 12 and the surah name match
        // must produce all 111 of its verses.
        val hits = QuranRepository.search("Yusuf")
        assertEquals(111, hits.size)
        assertTrue(hits.all { it.surahNumber == 12 })
        assertEquals((1..111).toList(), hits.map { it.ayahNumber })
    }

    @Test
    fun surahLookupStillFallsBackToFirstSurah() {
        for (number in 1..114) {
            assertEquals(number, QuranRepository.getSurah(number).number)
        }
        assertEquals(1, QuranRepository.getSurah(0).number)
        assertEquals(1, QuranRepository.getSurah(999).number)
    }

    @Test
    fun ayahsForSurahMatchFullListFilter() {
        for (number in 1..114) {
            val expected = QuranCorpus.ayahs
                .filter { it.surahNumber == number }
                .map { it.surahNumber to it.ayahNumber }
            val actual = QuranRepository.getAyahsForSurah(number)
                .map { it.surahNumber to it.ayahNumber }
            assertEquals("Surah $number", expected, actual)
        }
        assertTrue(QuranRepository.getAyahsForSurah(999).isEmpty())
    }

    @Test
    fun indexedLookupsMatchLinearScans() {
        // Every verse resolves to the same entry the old full scan found.
        QuranCorpus.ayahs.forEach { ayah ->
            assertEquals(ayah, QuranRepository.resolveAyah(ayah.surahNumber, ayah.ayahNumber))
        }
        // Out-of-range and invalid references never fabricate text.
        assertNull(QuranRepository.resolveAyah(1, 8))
        assertNull(QuranRepository.resolveAyah(2, 287))
        assertNull(QuranRepository.resolveAyah(0, 1))
        assertNull(QuranRepository.resolveAyah(115, 1))
    }

    @Test
    fun repeatedSearchReturnsIdenticalResults() {
        // Guards against accidental mutation of the shared indexed lists.
        val first = searchPairs("الله")
        val second = searchPairs("الله")
        assertEquals(first, second)
        val firstSurah = QuranRepository.getAyahsForSurah(36).map { it.ayahNumber }
        val secondSurah = QuranRepository.getAyahsForSurah(36).map { it.ayahNumber }
        assertEquals(firstSurah, secondSurah)
    }
}
