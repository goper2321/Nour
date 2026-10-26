package com.example

import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranRepository
import com.example.features.quran.models.Ayah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranEditionHonestyTest {
    @Test
    fun exactlyOneBundledTranslation() {
        assertEquals(listOf("sahih_en"),
            QuranRepository.TRANSLATION_EDITIONS.filter { it.bundled }.map { it.id })
    }

    @Test
    fun bundledTranslationEditionsHasOne() {
        assertEquals(1, QuranRepository.bundledTranslationEditions.size)
        assertEquals("sahih_en", QuranRepository.bundledTranslationEditions.first().id)
    }

    @Test
    fun bundledTranslationCoversEveryVerse() {
        val missing = QuranCorpus.ayahs.filter {
            it.translations["sahih_en"].isNullOrBlank()
        }
        assertEquals(emptyList<Ayah>(), missing)
        assertEquals(6236, QuranCorpus.ayahs.size)
    }
}
