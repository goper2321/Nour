package com.example

import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranBasmalaTest {
    private val normalizedBasmala =
        QuranRepository.normalizeArabic("بسم الله الرحمن الرحيم")

    private fun countBasmala(text: String): Int {
        val normalized = QuranRepository.normalizeArabic(text)
        var count = 0
        var idx = 0
        while (true) {
            val found = normalized.indexOf(normalizedBasmala, idx)
            if (found < 0) break
            count++
            idx = found + normalizedBasmala.length
        }
        return count
    }

    @Test
    fun basmalaAppearsOncePerSurah() {
        // Surah 9 (At-Tawbah) has no Basmala. Surah 27 (An-Naml) contains
        // the phrase a second time inside 27:30 (Sulayman's letter), which is
        // legitimate corpus text, not a duplicate header.
        (1..114).forEach { n ->
            val joined = QuranRepository.getAyahsForSurah(n)
                .joinToString(" ") { it.arabicText }
            val expected = when (n) {
                9 -> 0
                27 -> 2
                else -> 1
            }
            assertEquals("surah $n", expected, countBasmala(joined))
        }
    }

    @Test
    fun surahOneFirstVerseIsBasmala() {
        val first = QuranRepository.getAyahsForSurah(1).first().arabicText
        assertTrue(countBasmala(first) >= 1)
    }

    @Test
    fun surahNineFirstVerseHasNoBasmala() {
        val first = QuranRepository.getAyahsForSurah(9).first().arabicText
        assertEquals(0, countBasmala(first))
    }
}
