package com.example.features.prayer.ui

import com.example.features.prayer.models.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * NUR-014: pure JVM tests for the on-screen countdown haptic decision logic.
 * The Alarm/Adhan scheduling path (PrayerNotificationScheduler) is untouched
 * by this feature and is not exercised here.
 */
class CountdownArrivalHapticTest {

    private fun snapshot(nextPrayer: PrayerName, wholeRemainingMillis: Long) =
        CountdownHapticSnapshot(
            nextPrayer = nextPrayer,
            shownMinute = CountdownArrivalHaptics.shownMinuteOf(wholeRemainingMillis)
        )

    @Test
    fun firstCompositionIsSilent() {
        val current = CountdownHapticSnapshot(PrayerName.ASR, 120)
        assertEquals(CountdownHapticKind.NONE, CountdownArrivalHaptics.kindFor(null, current))
    }

    @Test
    fun minuteShownMatchesDisplayedDigitsBucket() {
        assertEquals(0, CountdownArrivalHaptics.shownMinuteOf(59_999))
        assertEquals(1, CountdownArrivalHaptics.shownMinuteOf(60_000))
        assertEquals(1, CountdownArrivalHaptics.shownMinuteOf(119_000))
        assertEquals(2, CountdownArrivalHaptics.shownMinuteOf(120_000))
        assertEquals(360, CountdownArrivalHaptics.shownMinuteOf(6 * 3600 * 1000L))
    }

    @Test
    fun shownMinuteClampsAtZeroLikeTheDigits() {
        assertEquals(0, CountdownArrivalHaptics.shownMinuteOf(0))
        assertEquals(0, CountdownArrivalHaptics.shownMinuteOf(-50))
    }

    @Test
    fun unchangedMinuteDoesNotTick() {
        val previous = snapshot(PrayerName.ASR, 134_000)
        val current = snapshot(PrayerName.ASR, 133_500) // same displayed minute
        assertEquals(CountdownHapticKind.NONE, CountdownArrivalHaptics.kindFor(previous, current))
    }

    @Test
    fun minuteBoundaryTicksLightlyExactlyOnce() {
        val previous = snapshot(PrayerName.ASR, 120_333)
        val boundary = snapshot(PrayerName.ASR, 119_999) // 00 seconds just wrapped
        assertEquals(2, previous.shownMinute)
        assertEquals(1, boundary.shownMinute)
        assertEquals(CountdownHapticKind.MINUTE_TICK, CountdownArrivalHaptics.kindFor(previous, boundary))
        // Holding the same shown minute (normal per-second recomposition) is silent.
        assertEquals(CountdownHapticKind.NONE, CountdownArrivalHaptics.kindFor(boundary, boundary))
    }

    @Test
    fun prayerArrivalIsTheStrongCueAndFiresOnce() {
        // Ticking toward Isha; the clock reaches Isha and the header now
        // counts toward Fajr: one arrival buzz, no extra minute tick.
        val before = CountdownHapticSnapshot(PrayerName.ISHA, 0)
        val after = CountdownHapticSnapshot(PrayerName.FAJR, 571)
        assertEquals(CountdownHapticKind.PRAYER_ARRIVAL, CountdownArrivalHaptics.kindFor(before, after))
        // The very next recomposition (same new target) is silent again.
        assertEquals(CountdownHapticKind.NONE, CountdownArrivalHaptics.kindFor(after, after))
    }

    @Test
    fun arrivalSuppressesTheMinuteTickOnThatTransition() {
        // Minute bucket also jumps (0 -> hours) on arrival; the composer must
        // not fire both the light tick and the arrival buzz for one event.
        val before = CountdownHapticSnapshot(PrayerName.MAGHRIB, 0)
        val after = CountdownHapticSnapshot(PrayerName.ISHA, 3_402)
        assertEquals(CountdownHapticKind.PRAYER_ARRIVAL, CountdownArrivalHaptics.kindFor(before, after))
    }

    @Test
    fun movingTowardNextNightCyclesThroughEveryBoundary() {
        // Simulate an evening: remaining minute decrements within one target.
        var shown = 40
        var previous = CountdownHapticSnapshot(PrayerName.ISHA, shown)
        var tickCount = 0
        var arrivalCount = 0
        repeat(40) {
            val current = CountdownHapticSnapshot(PrayerName.ISHA, shown - 1)
            when (CountdownArrivalHaptics.kindFor(previous, current)) {
                CountdownHapticKind.MINUTE_TICK -> tickCount++
                CountdownHapticKind.PRAYER_ARRIVAL -> arrivalCount++
                CountdownHapticKind.NONE -> {}
            }
            previous = current
            shown -= 1
        }
        assertEquals(40, tickCount)
        assertEquals(0, arrivalCount)
    }
}
