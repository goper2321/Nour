package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * NUR-019: the startup graph's order and failure semantics, pinned as pure-JVM
 * tests so every host (including ARM Termux hosts that skip Robolectric) can
 * verify them.
 */
class AppInitializerTest {

    @Test
    fun `startup steps stay in the documented order`() {
        assertEquals(
            listOf(
                "CrashReporter.install", // 1. trap crashes before anything else
                "NourApp.instance", // 2. singleton bound before DB init can consult it
                "NourDatabase.getInstance", // 3. blocking init before any screen asks
                "PrayerNotificationScheduler.initChannels", // 4. channels before scheduler notifications
                "PrayerNotificationScheduler.scheduleOfflineRollingWindow", // 5. needs 3 + 4
                "QuranRepository.warmSearchIndex", // 6. background; needs 3
            ),
            AppInitializer.stepNames,
        )
    }

    @Test
    fun `a failing step stops the sequence fail-fast`() {
        val executed = mutableListOf<String>()
        val steps = listOf(
            InitializationStep("first") { executed.add("first") },
            InitializationStep("second-crashes") {
                executed.add("second-crashes")
                throw IllegalStateException("step 2 crashed")
            },
            InitializationStep("third") { executed.add("third") },
        )
        try {
            AppInitializer.runInOrder(steps)
            fail("runInOrder was expected to propagate the failure (pre-extraction onCreate behavior)")
        } catch (expected: IllegalStateException) {
            // Unchanged semantics: synchronous step failures are not swallowed.
        }
        assertEquals(listOf("first", "second-crashes"), executed)
    }
}
