package com.example

import com.example.features.prayer.calculation.CelestialMath
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import org.junit.Test
import org.junit.Assert.assertTrue

/**
 * Sanity checks for the live-sky astronomy. These are broad physics checks
 * (equinox zenith, synodic-month return, 24h sun sweep), not pixel tests, so
 * they validate that CelestialMath was ported correctly.
 */
class CelestialMathTest {

    private fun utcMillis(y: Int, mo: Int, d: Int, hh: Int, mm: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(y, mo - 1, d, hh, mm, 0)
        }.timeInMillis

    @Test
    fun `sun is near zenith at local solar noon on the equinox at the equator`() {
        // Vernal equinox, UTC noon, exactly on the prime meridian:
        // the sun should sit almost directly overhead.
        val noon = utcMillis(2026, 3, 20, 12, 0)
        val sun = CelestialMath.sunPosition(noon, 0.0, 0.0)
        assertTrue(
            "Expected near-zenith sun at equinox noon, got ${sun.altitudeDeg}",
            sun.altitudeDeg > 85.0
        )
    }

    @Test
    fun `sun is below the horizon at midnight on the same equinox day`() {
        val midnight = utcMillis(2026, 3, 20, 0, 0)
        val sun = CelestialMath.sunPosition(midnight, 0.0, 0.0)
        assertTrue(
            "Expected sun below horizon at midnight, got ${sun.altitudeDeg}",
            sun.altitudeDeg < -10.0
        )
    }

    @Test
    fun `moon phase repeats after one synodic month`() {
        val t0 = utcMillis(2026, 5, 1, 0, 0)
        val synodicMillis = (29.530588 * 86400000.0).toLong()
        val a = CelestialMath.moonInfo(t0, 10.0, 45.0)
        val b = CelestialMath.moonInfo(t0 + synodicMillis, 10.0, 45.0)
        assertTrue(
            "Phase drifted after one synodic month: ${a.phase01} -> ${b.phase01}",
            abs(a.phase01 - b.phase01) < 0.03
        )
    }

    @Test
    fun `sun altitude changes sign through a full day`() {
        var minAlt = Double.POSITIVE_INFINITY
        var maxAlt = Double.NEGATIVE_INFINITY
        val start = utcMillis(2026, 6, 21, 0, 0)
        for (i in 0 until 48) {
            val alt = CelestialMath.sunPosition(start + i * 1800000L, 24.0, 46.0).altitudeDeg
            minAlt = minOf(minAlt, alt)
            maxAlt = maxOf(maxAlt, alt)
        }
        assertTrue(
            "Sweep too flat: [$minAlt, $maxAlt]",
            minAlt < -5.0 && maxAlt > 60.0
        )
    }
}