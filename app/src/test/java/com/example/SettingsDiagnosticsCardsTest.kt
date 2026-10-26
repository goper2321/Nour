package com.example

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranSources
import com.example.features.settings.NourPreferences
import com.example.features.settings.ui.DeviceDiagnosticsCard
import com.example.features.settings.ui.QuranSourcesCard
import com.example.localization.AppStrings
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * NUR-033 / NUR-035: renders the two new Settings cards directly (they are
 * `internal` composables so the test can compose them in isolation) and
 * asserts every factual row actually shows up with the value the card
 * promises. Rows are rendered inside the real theme the same way the
 * Settings list includes them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingsDiagnosticsCardsTest {
    @get:Rule
    val compose = createComposeRule()

    private val strings = AppStrings() // English defaults

    private fun clipboardOf(context: Context): ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // --- NUR-033: device diagnostics card ----------------------------------------------------

    @Test
    fun diagnosticsCard_showsFactualRows_fromRecords() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        // Seed the app's own additive records the scheduler would have written.
        context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("adhan_next_FAJR", System.currentTimeMillis() + 120_000L)
            .putBoolean("adhan_exact_ok", true)
            .apply()
        val prefs = NourPreferences(context)
        prefs.lastLocationFixMillis = System.currentTimeMillis() - 5 * 60_000L // 5 minutes ago

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                DeviceDiagnosticsCard(
                    calculationMethod = CalculationMethod.UMM_AL_QURA,
                    madhab = Madhab.SHAFI_MALIKI_HANBALI,
                    highLatRule = HighLatitudeRule.ANGLE_BASED,
                    strings = strings
                )
            }
        }

        // The gather runs on Dispatchers.IO; wait until the snapshot lands.
        // This test is the class's first: it pays Robolectric's compose +
        // Room/SQLite cold start, whose render comfortably outran the previous
        // 10_000 ms cap, so give the first render a bounded-but-generous
        // window (the later tests in this class stay on the fast warm path).
        compose.waitUntil(30_000) {
            compose.onAllNodesWithTag("diag_row_version").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithTag("settings_device_diagnostics_card").assertExists()
        compose.onNodeWithTag("diag_row_version").assertExists()
        // Factual values, never invented:
        compose.onNodeWithText("Umm Al-Qura University, Makkah").assertExists()
        compose.onNodeWithText("Standard (Shafi'i, Maliki, Hanbali)").assertExists()
        compose.onNodeWithText("Angle-Based Rule").assertExists()
        compose.onNodeWithText("Tabular (calculated)").assertExists()
        compose.onNodeWithText("5 m").assertExists() // machine-style fix age
        // The Adhan row is one composite value ("Fajr · yyyy-MM-dd HH:mm ·
        // scheduled exact") so the flag and the prayer name are matched as
        // substrings; onNodeWithText's contains is case-sensitive, so the
        // phrase must match the rendered casing exactly.
        compose.onNodeWithText("scheduled exact", substring = true).assertExists()
        compose.onNodeWithText("Fajr", substring = true).assertExists()
        compose.onNodeWithText("intact", substring = true).assertExists()
        // The corpus row must show the *actual* digest's leading digits of the
        // pinned Uthmani text, computed from the bundled file — not a copy.
        compose.onNodeWithText(QuranCorpus.TEXT_HASH.take(12), substring = true).assertExists()
    }

    @Test
    fun diagnosticsCard_longPressCopiesRowValue() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        val prefs = NourPreferences(context)
        prefs.lastLocationFixMillis = System.currentTimeMillis() - 5 * 60_000L // "5 m"

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                DeviceDiagnosticsCard(
                    calculationMethod = CalculationMethod.UMM_AL_QURA,
                    madhab = Madhab.SHAFI_MALIKI_HANBALI,
                    highLatRule = HighLatitudeRule.ANGLE_BASED,
                    strings = strings
                )
            }
        }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("diag_row_lastfix").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithTag("diag_row_lastfix").performTouchInput { longClick() }
        compose.waitForIdle()

        val clipboardText = clipboardOf(context).primaryClip?.getItemAt(0)?.text?.toString()
        assertNotNull("long-press copy must put the row value on the clipboard", clipboardText)
        assertEquals("5 m", clipboardText)
    }

    private fun sanitizeDiagnosticsStore(context: Context) {
        runCatching { context.deleteSharedPreferences("nour_app_prefs") }
        val fresh = context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
        fresh.edit().clear().commit()
        val wipe = fresh.edit()
        com.example.features.prayer.models.PrayerName.values().forEach { prayer ->
            wipe.remove("adhan_next_${prayer.name}")
        }
        wipe.remove("adhan_exact_ok")
        wipe.remove("location_last_fix_millis")
        wipe.commit()
    }

    @Test
    fun diagnosticsCard_reportsHonestEmptyState() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        // Deterministic honest-empty fixture. The residue this test kept
        // catching is NOT cross-test leakage: it is a live writer in THIS
        // test. NourApp.onCreate (via AppInitializer) launches
        // scheduleOfflineRollingWindow on Dispatchers.IO at Application
        // creation; that wave computes 7 days of prayers (Makkah fallback)
        // and, through the scheduler's own NUR-033 records, writes future
        // adhan_next_* triggers into nour_app_prefs a few milliseconds into
        // the test — i.e. after a one-shot wipe but before the card's gather
        // reads. So: (1) flip the alarm switch off first (the wave's very
        // first statement checks it and may short-circuit), then (2) drain —
        // repeatedly wipe until the store stays free of future Adhan records
        // across a bounded quiet window (the wave is one-shot; nothing else
        // writes these keys in this test), and (3) do a final wipe before the
        // single composition. The switch key is cleared with everything else
        // by the final wipe, so no preference is left altered. A second
        // wipe-and-recompose phase was tried first and is illegal anyway:
        // createComposeRule allows exactly one setContent per test.
        val prefsStore = context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
        prefsStore.edit().putBoolean("prayer_alarms_enabled", false).commit()
        val deadline = System.currentTimeMillis() + 5_000L
        var quietPolls = 0
        while (quietPolls < 3 && System.currentTimeMillis() < deadline) {
            sanitizeDiagnosticsStore(context)
            val residue = NourPreferences(context).nextAdhanRecords()
                .values.any { it > System.currentTimeMillis() }
            quietPolls = if (residue) 0 else quietPolls + 1
            Thread.sleep(100)
        }
        sanitizeDiagnosticsStore(context)
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                DeviceDiagnosticsCard(
                    calculationMethod = CalculationMethod.UMM_AL_QURA,
                    madhab = Madhab.SHAFI_MALIKI_HANBALI,
                    highLatRule = HighLatitudeRule.ANGLE_BASED,
                    strings = strings
                )
            }
        }
        // Bounded wait for the gather (Dispatchers.IO + Room) to land. The
        // generous cap is the class-cold-start allowance: if JUnit orders
        // this method FIRST, it pays the same Robolectric/Room start-up the
        // first test here documents at 30 s.
        compose.waitUntil(30_000) {
            compose.onAllNodesWithTag("diag_row_nextadhan").fetchSemanticsNodes().isNotEmpty()
        }

        // The honest empty values come straight from the strings the card is
        // required to show, so the assertions stay pinned to the real wording
        // (English defaults here) instead of re-typed literals.
        compose.onNodeWithText(strings.diagLastFixNever, substring = true).assertExists()
        compose.onNodeWithText(strings.diagAdhanNone, substring = true).assertExists()
    }

    // --- NUR-035: About this Quran card ---------------------------------------------------------

    @Test
    fun quranSourcesCard_showsProvenanceHashesAndTafsirHonesty() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                QuranSourcesCard(strings = strings)
            }
        }
        compose.waitForIdle()

        compose.onNodeWithTag("settings_quran_sources_card").assertExists()
        // The essence of quran/SOURCES.md. The CC licence is intentionally
        // stated on TWO rows (the Uthmani text source line, and the bundled
        // metadata-partition line), so assert each provenance row by its FULL
        // line — a generic "Creative Commons Attribution" substring would
        // match both rows and fail the unique-node lookup.
        compose.onNodeWithText(QuranSources.UTHMANI_SOURCE).assertExists()
        compose.onNodeWithText(QuranSources.METADATA_LINE).assertExists()
        compose.onNodeWithText("Tanzil Quran Text", substring = true).assertExists()
        compose.onNodeWithText("tanzil.net", substring = true).assertExists()
        // Both BUNDLED_LINE and SAHIH_LINE mention "Saheeh International", so
        // match the full translation line: a substring lookup is ambiguous by
        // design and would find two legitimate rows.
        compose.onNodeWithText(QuranSources.SAHIH_LINE).assertExists()
        compose.onNodeWithText("verifies the SHA-256", substring = true).assertExists()
        // Pinned hashes interpolated from the single pinned source (no retyped literals):
        compose.onNodeWithText("Pinned SHA-256 (uthmani.txt): ${QuranCorpus.TEXT_HASH}").assertExists()
        compose.onNodeWithText("Pinned SHA-256 (en_sahihintl.txt): ${QuranCorpus.EN_SAHIH_HASH}").assertExists()
        // The honest line about what is NOT in the app:
        compose.onNodeWithText(strings.quranSourcesNotBundled, substring = true).assertExists()
    }
}
