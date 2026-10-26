package com.example

import com.example.features.settings.DiagnosticsMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * NUR-033: the pure decision helpers behind the Settings "Device diagnostics"
 * card, covered on the JVM with no Android or Compose dependencies. These are
 * the honest-state rules the card displays — replace/keep for stored next
 * Adhan records, earliest future schedule, relative fix age with
 * machine-style units, the UTC-informed schedule stamp, corpus integrity, and
 * hash shortening.
 */
class DiagnosticsMathTest {

    // --- shouldReplaceStoredNext ------------------------------------------------------------

    @Test
    fun replaceStoredNext_whenStoredRecordIsAlreadyInThePast() {
        val now = 1_000_000L
        assertTrue(DiagnosticsMath.shouldReplaceStoredNext(storedMillis = 900_000L, candidateMillis = 2_000_000L, nowMillis = now))
    }

    @Test
    fun replaceStoredNext_whenCandidateFiresEarlier() {
        val now = 1_000_000L
        assertTrue(DiagnosticsMath.shouldReplaceStoredNext(storedMillis = 3_000_000L, candidateMillis = 2_500_000L, nowMillis = now))
    }

    @Test
    fun keepStoredNext_whenStoredIsFutureAndCandidateLater() {
        val now = 1_000_000L
        assertFalse(DiagnosticsMath.shouldReplaceStoredNext(storedMillis = 3_000_000L, candidateMillis = 3_500_000L, nowMillis = now))
    }

    @Test
    fun keepStoredNext_whenCandidateEqualsStored() {
        val now = 1_000_000L
        assertFalse(DiagnosticsMath.shouldReplaceStoredNext(storedMillis = 3_000_000L, candidateMillis = 3_000_000L, nowMillis = now))
    }

    // --- nextSchedule ------------------------------------------------------------------------

    @Test
    fun nextSchedule_picksEarliestFutureRecord() {
        val now = 1_000_000L
        val records = mapOf(
            "ISHA" to 4_000_000L,   // future, later
            "FAJR" to 2_000_000L,   // future, earliest → wins
            "MAGHRIB" to 500_000L   // already past → ignored
        )
        assertEquals("FAJR" to 2_000_000L, DiagnosticsMath.nextSchedule(records, now))
    }

    @Test
    fun nextSchedule_nullWhenNothingRecorded() {
        assertNull(DiagnosticsMath.nextSchedule(emptyMap(), nowMillis = 1_000L))
    }

    @Test
    fun nextSchedule_nullWhenEveryRecordIsPast() {
        assertNull(DiagnosticsMath.nextSchedule(mapOf("FAJR" to 500L), nowMillis = 1_000L))
    }

    // --- formatFixAge --------------------------------------------------------------------------

    @Test
    fun fixAge_nullWhenNeverRecorded() {
        assertNull(DiagnosticsMath.formatFixAge(fixMillis = null, nowMillis = 1_000L))
    }

    @Test
    fun fixAge_nullWhenRecordLiesInTheFuture() {
        // An untrusted future record must not be reported as an age.
        assertNull(DiagnosticsMath.formatFixAge(fixMillis = 2_000L, nowMillis = 1_000L))
    }

    @Test
    fun fixAge_nowUnderOneMinute() {
        assertEquals("now", DiagnosticsMath.formatFixAge(fixMillis = 1_000L, nowMillis = 40_000L))
    }

    @Test
    fun fixAge_minutes() {
        assertEquals("5 m", DiagnosticsMath.formatFixAge(fixMillis = 1_000L, nowMillis = 1_000L + 5 * 60_000L))
    }

    @Test
    fun fixAge_hours() {
        assertEquals("2 h", DiagnosticsMath.formatFixAge(fixMillis = 1_000L, nowMillis = 1_000L + 130 * 60_000L))
    }

    @Test
    fun fixAge_days() {
        assertEquals("3 d", DiagnosticsMath.formatFixAge(fixMillis = 1_000L, nowMillis = 1_000L + (3L * 1440 + 20) * 60_000L))
    }

    // --- formatScheduleStamp --------------------------------------------------------------------

    @Test
    fun scheduleStampFormatsDayAndMinute() {
        val cal = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 23, 12, 34, 0)
        }
        assertEquals("2026-10-23 12:34", DiagnosticsMath.formatScheduleStamp(cal.timeInMillis))
    }

    // --- isCorpusIntact / shortHash ---------------------------------------------------------------

    @Test
    fun corpusIntact_onlyWhenActualHashMatchesPinned() {
        // Synthetic fixtures: the real pinned hashes live in QuranCorpus and are
        // compared at runtime; here only the equality rule is under test.
        assertTrue(DiagnosticsMath.isCorpusIntact(actualHash = "abc", expectedHash = "abc"))
        assertFalse(DiagnosticsMath.isCorpusIntact(actualHash = "abd", expectedHash = "abc"))
        assertFalse(DiagnosticsMath.isCorpusIntact(actualHash = null, expectedHash = "abc"))
    }

    @Test
    fun shortHash_truncatesWithEllipsisMarker() {
        val value = DiagnosticsMath.shortHash("6933e133dd56db778c801bf738848454e43648105a151e8d84d86a7cae39ec5f", 12)
        assertEquals("6933e133dd56…", value)
    }

    @Test
    fun shortHash_dashWhenAbsent() {
        assertEquals("—", DiagnosticsMath.shortHash(null))
    }
}
