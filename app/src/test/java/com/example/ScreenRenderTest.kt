package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.example.features.onboarding.ui.SetupWizard
import com.example.features.prayer.calculation.CelestialMath
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.ui.PrayerScreen
import com.example.features.widgets.ImmersiveSkyBackground
import com.example.features.qibla.ui.QiblaScreen
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.ui.SettingsScreen
import com.example.localization.AppLanguage
import com.example.ui.components.NourContentFrame
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar
import java.util.Date

/**
 * Per-screen rendering verification (prompt 07 / 21).
 *
 * Renders each top-level screen with realistic fake data on the JVM and asserts
 * the screen's root content is actually displayed — i.e. it composes and lays out
 * without a runtime crash. It also writes a PNG per screen to
 * `app/build/screen-verify/` so the layout can be eyeballed without a device.
 *
 * This is *rendering* verification, not a substitute for a real-device pass:
 * sensors, Doze, RTL and TalkBack still need hardware.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenRenderTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun pauseClock() {
        // Screens such as PrayerScreen run an infinite 1-second ticker. A paused
        // test clock keeps Compose idle so assertions and image capture settle
        // instead of timing out (same pattern as NourDesignSystemTest).
        compose.mainClock.autoAdvance = false
    }

    private val location = UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0, false)

    private fun capture(name: String) {
        // Roborazzi renders the Robolectric view hierarchy directly, so it does
        // not wait on Compose frames the way captureToImage() does.
        compose.onRoot().captureRoboImage("build/screen-verify/$name.png")
    }

    @Test
    fun prayerScreenRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    PrayerScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments()
                    )
                }
            }
        }
        compose.onNodeWithTag("prayer_screen_list").assertIsDisplayed()
        // Salah review fixes, locked in: the countdown still renders without
        // its per-second animation wrapper, and a non-GPS location is
        // labelled honestly instead of showing a bare city name.
        compose.onNodeWithTag("countdown_timer_text").assertIsDisplayed()
        compose.onNodeWithTag("prayer_location_button").assertTextContains("Default location", substring = true)
        capture("01_prayer")
    }

    @Test
    fun qiblaScreenRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame { QiblaScreen(location = location) }
            }
        }
        compose.onNodeWithTag("qibla_mode_status").assertIsDisplayed()
        capture("02_qibla")
    }

    @Test
    fun quranScreenRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame { QuranScreen() }
            }
        }
        compose.onNodeWithTag("quran_screen").assertIsDisplayed()
        capture("03_quran")
    }

    @Test
    fun settingsScreenRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    SettingsScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments(),
                        onLocationChange = {},
                        onMethodChange = {},
                        onMadhabChange = {},
                        onHighLatRuleChange = {},
                        onAdjustmentsChange = {}
                    )
                }
            }
        }
        compose.onNodeWithTag("settings_screen_list").assertIsDisplayed()
        capture("04_settings")
    }

    @Test
    fun setupWizardRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SetupWizard(
                    currentLanguage = AppLanguage.ENGLISH,
                    onLanguageChange = {},
                    userLocation = location,
                    onLocationChange = {},
                    alarmsEnabled = true,
                    onAlarmsEnabledChange = {},
                    onSetupComplete = {}
                )
            }
        }
        compose.onNodeWithTag("setup_wizard_root").assertIsDisplayed()
        capture("05_setup_wizard")
    }

    /**
     * Ask 1: the sky must be able to show the Sun and the Moon at the same time,
     * each placed from time + location, the Moon at its true phase. This picks a
     * deterministic daytime instant (scanning fixed calendar days, so the
     * captured image does not drift with the wall clock) when both bodies are
     * comfortably above the horizon and renders the real sky component.
     */
    @Test
    fun skyShowsSunAndMoonTogether() {
        var chosen: Pair<Date, PrayerTimes>? = null
        val day = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.JANUARY, 1, 12, 0, 0)
        }
        var dayIndex = 0
        while (chosen == null && dayIndex < 90) {
            val times = PrayerCalculationEngine.calculate(
                date = day.time,
                location = location,
                method = CalculationMethod.UMM_AL_QURA,
                madhab = Madhab.SHAFI_MALIKI_HANBALI,
                highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                adjustments = PrayerAdjustments()
            )
            var t = times.sunrise.time + 30 * 60_000L
            while (t < times.maghrib.time) {
                val sunAlt = CelestialMath.sunPosition(t, location.latitude, location.longitude).altitudeDeg
                val moon = CelestialMath.moonInfo(t, location.latitude, location.longitude)
                if (sunAlt > 15.0 && moon.position.altitudeDeg > 15.0 && moon.illumination > 0.40) {
                    // Both bodies comfortably up and the Moon shows a real phase.
                    chosen = Date(t) to times
                    break
                }
                t += 10 * 60_000L
            }
            day.add(Calendar.DAY_OF_YEAR, 1)
            dayIndex++
        }
        assertNotNull("Expected a daytime instant with both Sun and Moon up", chosen)
        val (instant, times) = chosen!!
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Box(Modifier.fillMaxSize()) {
                    ImmersiveSkyBackground(
                        prayerTimes = times,
                        location = location,
                        now = instant,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        capture("06_sky_sun_and_moon")
    }
}
