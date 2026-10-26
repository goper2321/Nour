package com.example

import android.util.Log
import com.example.database.NourDatabase
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.example.features.quran.data.QuranRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** One named startup step. Plain Kotlin only — no DI framework (NUR-019). */
class InitializationStep(val name: String, val action: () -> Unit)

/**
 * Nour's startup graph, extracted from NourApp.kt (NUR-019).
 *
 * [attach] runs the steps in exactly the pre-extraction onCreate() order — the
 * order matters and is asserted by AppInitializerTest:
 *
 *  1. **CrashReporter.install** — first, so even very early startup crashes
 *     land in the app's crash file (Downloads/NourCrash or filesDir).
 *  2. **NourApp.instance** — the singleton is bound next; nothing downstream
 *     (database init included) may consult it before this step.
 *  3. **NourDatabase.getInstance** — blocking work runs before any screen asks
 *     for a DAO.
 *  4. **PrayerNotificationScheduler.initChannels** — the adhan channels must
 *     exist before any scheduler notification is posted.
 *  5. **scheduleOfflineRollingWindow** — depends on 3 (alarm settings) and 4
 *     (its notification channel); the work is async and failures are logged,
 *     not thrown, so a late alarm-scheduling problem never prevents app start.
 *  6. **warmSearchIndex** — background Quran work; depends on 3; likewise
 *     async with logged failures.
 *
 * Failure semantics are unchanged from the pre-extraction NourApp.onCreate:
 * the synchronous steps do NOT catch exceptions (a crash inside step 2 aborts
 * the remaining steps — AppInitializerTest pins that fail-fast behavior), while
 * steps 5–6 keep their own try/catch that only logs, exactly as before.
 */
object AppInitializer {

    /** Startup steps in exact execution order. Tests assert this list. */
    val stepNames: List<String> = listOf(
        "CrashReporter.install",
        "NourApp.instance",
        "NourDatabase.getInstance",
        "PrayerNotificationScheduler.initChannels",
        "PrayerNotificationScheduler.scheduleOfflineRollingWindow",
        "QuranRepository.warmSearchIndex",
    )

    /** Single entrypoint; NourApp.onCreate calls nothing else. */
    fun attach(application: NourApp) {
        runInOrder(
            listOf(
                InitializationStep(stepNames[0]) { CrashReporter.install(application) },
                InitializationStep(stepNames[1]) { NourApp.instance = application },
                InitializationStep(stepNames[2]) {
                    application.database = NourDatabase.getInstance(application)
                },
                InitializationStep(stepNames[3]) {
                    PrayerNotificationScheduler.initChannels(application)
                },
                InitializationStep(stepNames[4]) {
                    // Same coroutine shape as pre-extraction: async + logged failure.
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            PrayerNotificationScheduler.scheduleOfflineRollingWindow(application)
                        } catch (t: Throwable) {
                            Log.e("AppInitializer", "Failed to schedule offline rolling window", t)
                        }
                    }
                },
                InitializationStep(stepNames[5]) {
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            QuranRepository.warmSearchIndex()
                        } catch (t: Throwable) {
                            Log.e("AppInitializer", "Failed to warm Quran search index", t)
                        }
                    }
                },
            ),
        )
    }

    /**
     * Runs steps sequentially. There is deliberately NO try/catch: a throwing
     * step aborts the sequence — the pre-extraction onCreate() behavior
     * (AppInitializerTest asserts the fail-fast propagation).
     */
    fun runInOrder(steps: List<InitializationStep>) {
        for (step in steps) {
            step.action()
        }
    }
}
