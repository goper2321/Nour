package com.example

import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for the on-device prayer calculation engine.
 * No Android framework, no network: these run on any host (including ARM64).
 */
class PrayerCalculationTest {

    private val makkah = UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)

    private fun noonUtc(year: Int, month: Int, day: Int): java.util.Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(year, month, day, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    @Test
    fun prayerTimes_areChronological_forEveryMethod() {
        val date = noonUtc(2026, Calendar.JUNE, 15)
        for (method in CalculationMethod.values()) {
            val times = PrayerCalculationEngine.calculate(
                date = date, location = makkah, method = method,
                madhab = Madhab.SHAFI_MALIKI_HANBALI,
                highLatRule = HighLatitudeRule.ANGLE_BASED,
                adjustments = PrayerAdjustments()
            )
            val ordered = listOf(
                times.fajr to "fajr",
                times.sunrise to "sunrise",
                times.dhuhr to "dhuhr",
                times.asr to "asr",
                times.maghrib to "maghrib",
                times.isha to "isha"
            )
            for (i in 0 until ordered.size - 1) {
                assertTrue(
                    "${method.name}: ${ordered[i].second} (${ordered[i].first}) should precede " +
                        "${ordered[i + 1].second} (${ordered[i + 1].first})",
                    ordered[i].first.before(ordered[i + 1].first)
                )
            }
        }
    }

    @Test
    fun hanafiAsr_isLaterThanShafiiAsr() {
        val date = noonUtc(2026, Calendar.JUNE, 15)
        val shafii = PrayerCalculationEngine.calculate(
            date = date, location = makkah, method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.ANGLE_BASED, adjustments = PrayerAdjustments()
        )
        val hanafi = PrayerCalculationEngine.calculate(
            date = date, location = makkah, method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhab = Madhab.HANAFI,
            highLatRule = HighLatitudeRule.ANGLE_BASED, adjustments = PrayerAdjustments()
        )
        assertTrue("Hanafi Asr should be later than Shafi'i Asr", hanafi.asr.after(shafii.asr))
        // Other prayers must be unaffected by the madhab switch.
        assertEquals(shafii.fajr, hanafi.fajr)
        assertEquals(shafii.dhuhr, hanafi.dhuhr)
    }

    @Test
    fun minuteAdjustments_shiftOnlyTheirPrayer() {
        val date = noonUtc(2026, Calendar.JUNE, 15)
        val base = PrayerCalculationEngine.calculate(
            date = date, location = makkah, method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.ANGLE_BASED, adjustments = PrayerAdjustments()
        )
        val shifted = PrayerCalculationEngine.calculate(
            date = date, location = makkah, method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.ANGLE_BASED, adjustments = PrayerAdjustments(fajr = 5)
        )
        assertEquals(5 * 60 * 1000L, shifted.fajr.time - base.fajr.time)
        assertEquals(base.dhuhr, shifted.dhuhr)
        assertEquals(base.isha, shifted.isha)
    }

    @Test
    fun highLatitudeWinter_producesValidOrderedTimes() {
        // Tromsø in December: polar night territory; engine must not crash and
        // must still return an ordered schedule under every high-latitude rule.
        val tromso = UserLocation(69.6492, 18.9553, "Tromsø", "Norway", 1.0)
        val date = noonUtc(2026, Calendar.DECEMBER, 15)
        for (rule in HighLatitudeRule.values()) {
            val times = PrayerCalculationEngine.calculate(
                date = date, location = tromso, method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
                madhab = Madhab.SHAFI_MALIKI_HANBALI, highLatRule = rule,
                adjustments = PrayerAdjustments()
            )
            val all = listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha)
            for (i in 0 until all.size - 1) {
                assertTrue("$rule: prayer ${PrayerName.values()[i]} should precede ${PrayerName.values()[i + 1]}",
                    all[i].before(all[i + 1]) || all[i] == all[i + 1])
            }
        }
    }
}
