package com.example

import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM invariants for the Tanzil 604-page division exposed by
 * [QuranRepository.getMushafPage] and [QuranRepository.getPageForAyah].
 * No Robolectric, no device.
 */
class QuranMushafTest {

    private fun ref(surahNumber: Int, ayahNumber: Int): String = "$surahNumber:$ayahNumber"
    private fun ref(ayah: com.example.features.quran.models.Ayah): String =
        "${ayah.surahNumber}:${ayah.ayahNumber}"

    @Test
    fun everyPageHasVersesAndPartitionIsComplete() {
        val pages = (1..604).map { QuranRepository.getMushafPage(it) }
        assertTrue(pages.all { it.ayahs.isNotEmpty() })
        assertEquals(6236, pages.sumOf { it.ayahs.size })
        assertEquals(6236, pages.flatMap { it.ayahs }.distinct().size)
    }

    @Test
    fun knownPageAnchors() {
        assertEquals(7, QuranRepository.getMushafPage(1).ayahs.size)
        assertEquals("2:1", ref(QuranRepository.getMushafPage(2).ayahs.first()))
        // Page 49 begins at 2:283 and runs through the end of Al-Baqarah (2:286);
        // 2:283 is the first verse on the page, not the last (page 50 starts at 3:1).
        assertEquals("2:283", ref(QuranRepository.getMushafPage(49).ayahs.first()))
        assertEquals("2:286", ref(QuranRepository.getMushafPage(49).ayahs.last()))
        assertEquals("3:1", ref(QuranRepository.getMushafPage(50).ayahs.first()))
        assertEquals(
            listOf("4:176", "5:1", "5:2"),
            QuranRepository.getMushafPage(106).ayahs.map(::ref)
        )
        assertEquals(
            listOf(112, 113, 114),
            QuranRepository.getMushafPage(604).segments.map { it.surahNumber }
        )
        assertEquals(
            listOf(4, 5, 6),
            QuranRepository.getMushafPage(604).segments.map { it.ayahs.size }
        )
    }

    @Test
    fun midPageSurahTransitionCarriesHeader() {
        val page = QuranRepository.getMushafPage(106)
        assertEquals(2, page.segments.size)
        assertFalse(page.segments[0].showSurahHeader) // 4:176
        assertTrue(page.segments[1].showSurahHeader)  // 5:1
        assertEquals(5, page.segments[1].surahNumber)
        assertNull(page.surahHeader) // page does not start at an ayah 1
    }

    @Test
    fun lastPageHasThreeHeaders() {
        val page = QuranRepository.getMushafPage(604)
        assertEquals(3, page.segments.size)
        assertTrue(page.segments.all { it.showSurahHeader })
    }

    @Test
    fun pageLookupRoundTripsForEveryVerse() {
        QuranCorpus.ayahs.forEach { ayah ->
            val page = QuranRepository.getPageForAyah(ayah.surahNumber, ayah.ayahNumber)
            assertNotNull(page)
            assertEquals(ayah.pageNumber, page)
            assertTrue(QuranRepository.getMushafPage(page!!).ayahs.contains(ayah))
        }
    }

    @Test
    fun consecutivePagesAreContiguous() {
        // Page N's last verse immediately precedes page N+1's first verse in
        // QuranCorpus.ayahs: no gaps, no duplicates, no reordering.
        val corpus = QuranCorpus.ayahs
        val indexByRef = corpus.mapIndexed { index, ayah ->
            "${ayah.surahNumber}:${ayah.ayahNumber}" to index
        }.toMap()
        for (page in 1 until 604) {
            val current = QuranRepository.getMushafPage(page)
            val next = QuranRepository.getMushafPage(page + 1)
            val lastRef = "${current.ayahs.last().surahNumber}:${current.ayahs.last().ayahNumber}"
            val firstNextRef = "${next.ayahs.first().surahNumber}:${next.ayahs.first().ayahNumber}"
            val lastIndex = indexByRef[lastRef]
            val firstNextIndex = indexByRef[firstNextRef]
            assertNotNull(lastIndex)
            assertNotNull(firstNextIndex)
            assertEquals(lastIndex!! + 1, firstNextIndex)
        }
    }
}