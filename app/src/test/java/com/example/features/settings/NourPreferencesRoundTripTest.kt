package com.example.features.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * NUR-017: proves the Quran reading-position round-trips through the exact
 * store and keys production uses — `nour_app_prefs` with
 * `quran_last_read_surah` / `quran_last_read_ayah`, written via
 * NourPreferences by QuranScreen's LaunchedEffects and read back the same way
 * after a "process restart" (a fresh NourPreferences instance). Runs on
 * Robolectric so the real SharedPreferences implementation backs the store.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NourPreferencesRoundTripTest {

    @Test
    fun lastReadPositionRoundTripsThroughProductionKeys() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).also {
            it.lastReadSurah = 37
            it.lastReadAyah = 54
        }

        // Fresh instance = what a cold process start would read.
        val reloaded = NourPreferences(context)
        assertEquals(37, reloaded.lastReadSurah)
        assertEquals(54, reloaded.lastReadAyah)

        // The raw keys production actually reads/writes, byte-identical to the
        // KEY_LAST_* constants in NourPreferences.
        val raw = context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
        assertEquals(37, raw.getInt("quran_last_read_surah", -1))
        assertEquals(54, raw.getInt("quran_last_read_ayah", -1))
    }

    @Test
    fun outOfRangeValuesAreCoercedWhenStored() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = NourPreferences(context)
        prefs.lastReadSurah = 999
        prefs.lastReadAyah = -5

        val reloaded = NourPreferences(context)
        assertEquals(114, reloaded.lastReadSurah)
        assertEquals(1, reloaded.lastReadAyah)
    }

    @Test
    fun lastReadPageRoundTripsAndCoerces() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).lastReadPage = 106
        assertEquals(106, NourPreferences(context).lastReadPage)
        val raw = context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
        assertEquals(106, raw.getInt("quran_last_read_page", -1))
        NourPreferences(context).lastReadPage = 999
        assertEquals(604, NourPreferences(context).lastReadPage)
    }

    @Test
    fun readingThemeAndTypefaceRoundTripThroughProductionKeys() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).also {
            it.quranReadingTheme = QuranReadingTheme.NIGHT
            it.quranTypeface = QuranTypeface.AMIRI
        }

        // Fresh instance = what a cold process start would read.
        val reloaded = NourPreferences(context)
        assertEquals(QuranReadingTheme.NIGHT, reloaded.quranReadingTheme)
        assertEquals(QuranTypeface.AMIRI, reloaded.quranTypeface)

        val raw = context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
        assertEquals("NIGHT", raw.getString("quran_reading_theme", null))
        assertEquals("AMIRI", raw.getString("quran_typeface", null))
    }

    @Test
    fun unknownReadingThemeOrTypefaceFallsBackToSystem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // A value written by a future/renamed build must never crash the reader.
        context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("quran_reading_theme", "SEPIA")
            .putString("quran_typeface", "NASKH")
            .commit()

        val prefs = NourPreferences(context)
        assertEquals(QuranReadingTheme.SYSTEM, prefs.quranReadingTheme)
        assertEquals(QuranTypeface.SYSTEM, prefs.quranTypeface)
    }
}
