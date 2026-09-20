package com.example

import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integrity tests for the bundled Tanzil Uthmani corpus. These run the real
 * resource-loading path (hash verification, ordering, counts) in the JVM.
 */
class QuranCorpusTest {
    @Test
    fun corpusIsCompleteAndOrdered() {
        val ayahs = QuranCorpus.ayahs
        assertEquals(6236, ayahs.size)
        // Canonical order: surah then ayah, contiguous within each surah.
        var expectedSurah = 1
        var expectedAyah = 1
        ayahs.forEach { ayah ->
            assertEquals(expectedSurah, ayah.surahNumber)
            assertEquals(expectedAyah, ayah.ayahNumber)
            expectedAyah++
            if (expectedAyah > QuranRepository.getSurah(expectedSurah).ayahCount) {
                expectedSurah++
                expectedAyah = 1
            }
        }
        // After consuming all 114 surahs the walk points at surah 115, ayah 1.
        assertEquals(115, expectedSurah)
        assertEquals(1, expectedAyah)
    }

    @Test
    fun everyVerseHasNonBlankSourceText() {
        QuranCorpus.ayahs.forEach { ayah ->
            assertTrue("Blank verse ${ayah.surahNumber}:${ayah.ayahNumber}", ayah.arabicText.isNotBlank())
        }
    }

    @Test
    fun pageAndJuzMetadataCoverAllVerses() {
        QuranCorpus.ayahs.forEach { ayah ->
            assertTrue(ayah.pageNumber in 1..604)
            assertTrue(ayah.juzNumber in 1..30)
        }
        // Known anchors from the Tanzil metadata: Al-Baqarah starts page 2, Juz 30 starts An-Naba 78:1.
        assertEquals(2, QuranRepository.getAyahsForSurah(2).first().pageNumber)
        assertEquals(30, QuranRepository.getAyahsForSurah(78).first().juzNumber)
    }

    @Test
    fun noFabricatedTranslationOrTafsirIsPresent() {
        // Translations carry only the authentic bundled English edition; tafsir must stay empty
        // until a source with verifiable provenance is bundled.
        QuranCorpus.ayahs.forEach { ayah ->
            ayah.translations.keys.forEach { key ->
                assertEquals(QuranCorpus.ENGLISH_EDITION_ID, key)
            }
            assertTrue(ayah.tafsirEntries.isEmpty())
        }
    }

    @Test
    fun resolveAyahReturnsLiveTextOrNothing() {
        // A known reference resolves to the exact bundled source line.
        val ayatAlKursi = QuranRepository.resolveAyah(2, 255)
        requireNotNull(ayatAlKursi)
        assertTrue(ayatAlKursi.arabicText.startsWith("ٱللَّهُ لَآ إِلَـٰهَ إِلَّا هُوَ"))

        // Out-of-range and invalid references return null — never fabricated text.
        assertEquals(null, QuranRepository.resolveAyah(2, 287))
        assertEquals(null, QuranRepository.resolveAyah(1, 0))
        assertEquals(null, QuranRepository.resolveAyah(115, 1))
    }
}
