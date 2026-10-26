package com.example.features.prayer.ui

import com.example.features.prayer.models.PrayerName

/**
 * Pure decision logic for NUR-014 (on-screen countdown haptics).
 *
 * Contract, per the checklist item:
 * - A light tick fires when the minute the header displays changes
 *   (each minute boundary), guarded by a remembered previous snapshot so
 *   each boundary fires exactly once.
 * - A distinguishable, stronger cue fires when the prayer time lands while
 *   the header is on screen. `getCountdownState` swaps `nextPrayer` as soon
 *   as the clock reaches that prayer, so an advance of the next-prayer
 *   pointer IS the arrival signal; it cannot repeat while the same
 *   next prayer is being counted down (that is the dedup fired via the
 *   same remembered snapshot the minute counter uses).
 * - Nothing fires on the first composition: opening the app is not a
 *   boundary crossing.
 *
 * This logic is deliberately plain Kotlin so it stays JVM-testable; the
 * composable in [SeamlessPrayerHeader] only translates the returned kind
 * into `LocalHapticFeedback.performHapticFeedback` calls and never calls
 * `Vibrator` directly (the permission-free path).
 */
enum class CountdownHapticKind { NONE, MINUTE_TICK, PRAYER_ARRIVAL }

/**
 * What the header currently shows: the prayer being counted toward and the
 * whole-minute bucket of the displayed digits. `formattedRemaining` clamps
 * at 0:00:00, so [shownMinute] never goes negative either.
 */
data class CountdownHapticSnapshot(
    val nextPrayer: PrayerName,
    val shownMinute: Int
)

object CountdownArrivalHaptics {

    /**
     * The minute figure the header displays for [remainingMillis]:
     * 119_000 ms -> 1 (one full minute is still on screen),
     *  60_000 ms -> 1,  59_999 ms -> 0,  clamped at 0 like the digits.
     */
    fun shownMinuteOf(remainingMillis: Long): Int =
        ((remainingMillis / 1000).coerceAtLeast(0) / 60).toInt()

    /**
     * Decide which haptic (if any) the transition to [current] produces.
     * `previous == null` means this is the first composition.
     */
    fun kindFor(previous: CountdownHapticSnapshot?, current: CountdownHapticSnapshot): CountdownHapticKind {
        if (previous == null) return CountdownHapticKind.NONE
        return when {
            // Arrival: the countdown reached the moment it was ticking toward.
            // Sweeping straight to the next minute bucket (0 -> hours) is the
            // same event; the single arrival cue covers it.
            current.nextPrayer != previous.nextPrayer -> CountdownHapticKind.PRAYER_ARRIVAL
            // Same target, minute number changed: a plain minute tick.
            current.shownMinute != previous.shownMinute -> CountdownHapticKind.MINUTE_TICK
            else -> CountdownHapticKind.NONE
        }
    }
}
