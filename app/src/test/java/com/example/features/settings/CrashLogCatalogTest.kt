package com.example.features.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NUR-001: pure JVM tests for the crash-file listing/parsing helpers that
 * back the Settings "Diagnostics" section. Fixtures mirror exactly what
 * CrashReporter writes so the helpers stay in sync with the real format.
 */
class CrashLogCatalogTest {

    private fun logText(time: String, version: String, exception: String) = buildString {
        appendLine("Nour crash report")
        appendLine("time=$time")
        appendLine("version=$version")
        appendLine("thread=main")
        appendLine("exception=$exception")
        appendLine("--- stack ---")
        appendLine("java.lang.RuntimeException: boom")
        appendLine("  at com.example.Somewhere(Somewhere.kt:42)")
    }

    @Test
    fun recognizesTheCrashReporterNamePattern() {
        assertTrue(CrashLogCatalog.isCrashLogFileName("nour-crash-1739981.log"))
        assertFalse(CrashLogCatalog.isCrashLogFileName("nour-crash-notdigits.log"))
        assertFalse(CrashLogCatalog.isCrashLogFileName("other-1739981.log"))
        assertFalse(CrashLogCatalog.isCrashLogFileName("nour-crash-1739981.txt"))
        assertFalse(CrashLogCatalog.isCrashLogFileName("nour-crash-.log"))
    }

    @Test
    fun extractsEpochFromTheName() {
        assertEquals(1739981L, CrashLogCatalog.epochMillisFromName("nour-crash-1739981.log"))
        assertNull(CrashLogCatalog.epochMillisFromName("nour-crash-notdigits.log"))
        assertNull(CrashLogCatalog.epochMillisFromName("random.log"))
    }

    @Test
    fun parsesTheHeaderLinesCrashReporterWrites() {
        val header = CrashLogCatalog.parseHeader(
            logText(time = "2026-10-22 23:04:56", version = "1.2.0", exception = "java.lang.IllegalStateException: boom")
        )
        assertEquals("2026-10-22 23:04:56", header.time)
        assertEquals("1.2.0", header.version)
        assertEquals("java.lang.IllegalStateException: boom", header.exceptionSummary)
    }

    @Test
    fun toleratesTruncatedLogs() {
        val header = CrashLogCatalog.parseHeader("Nour crash report\nversion=\n")
        assertNull(header.time)
        assertNull(header.version)
        assertNull(header.exceptionSummary)
    }

    @Test
    fun listingIsNewestFirstWithFullText() {
        val entries = CrashLogCatalog.buildEntries(
            listOf(
                "nour-crash-1739980612000.log" to logText("2026-02-19 06:23:32", "1.2.0", "old"),
                "nour-crash-1739980666000.log" to logText("2026-02-19 06:24:26", "1.2.0", "new"),
                "nour-crash-1739980555000.log" to logText("2026-02-19 06:22:35", "1.2.0", "older")
            )
        )

        assertEquals(3, entries.size)
        assertEquals("new", entries[0].exceptionSummary)
        assertEquals("old", entries[1].exceptionSummary)
        assertEquals("older", entries[2].exceptionSummary)
        assertEquals(listOf(1739980666000L, 1739980612000L, 1739980555000L), entries.map { it.epochMillis })
        // Copy/Share receive the whole log, not just the header summary.
        assertTrue(entries[0].fullText.contains("--- stack ---"))
    }

    @Test
    fun nonCrashFilesAreExcluded() {
        val entries = CrashLogCatalog.buildEntries(
            listOf(
                "nour-crash-1739980666000.log" to "Nour crash report",
                "downloads-cache-file.log" to "Nour crash report"
            )
        )
        assertEquals(1, entries.size)
        assertEquals("nour-crash-1739980666000.log", entries[0].fileName)
    }
}
