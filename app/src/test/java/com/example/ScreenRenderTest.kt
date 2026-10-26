package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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
import com.example.ui.components.NourFloatingNavigationBar
import com.example.ui.components.NourFloatingNavigationRail
import com.example.ui.components.NourNavItem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NourSize
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    private val navigationItems = listOf(
        NourNavItem("Salah", Icons.Rounded.Schedule, Icons.Outlined.Schedule, "nav_salah"),
        NourNavItem("Qibla", Icons.Rounded.Explore, Icons.Outlined.Explore, "nav_qibla"),
        NourNavItem("Quran", Icons.Rounded.AutoStories, Icons.Outlined.AutoStories, "nav_quran"),
        NourNavItem("Settings", Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    private fun capture(name: String) {
        // Roborazzi renders the Robolectric view hierarchy directly, so it does
        // not wait on Compose frames the way captureToImage() does.
        compose.onRoot().captureRoboImage("build/screen-verify/$name.png")
    }

    @Test
    fun prayerScreenRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                // Mirrors MainActivity: Salah opts out of the frame gutter
                // so the sky hero bleeds edge-to-edge.
                NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                    PrayerScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments(),
                        // Wired, as in MainActivity: the "Default location"
                        // control only renders its disabled (transparent)
                        // style when this callback is absent.
                        onRequestLocationUpdate = {}
                    )
                }
            }
        }
        // The prayer page learns its sky stretch on the first layout pass;
        // advance frames so the capture shows the settled (stretched) state,
        // not the first-pass natural layout. Same idea as the capture
        // matrix's settle() helper, which also renders this screen.
        repeat(4) { compose.mainClock.advanceTimeByFrame() }
        compose.onNodeWithTag("prayer_screen_list").assertIsDisplayed()
        // Salah review fixes, locked in: the countdown still renders without
        // its per-second animation wrapper. The location control now shows the
        // place name only, keeping the sky hero uncluttered.
        compose.onNodeWithTag("countdown_timer_text").assertIsDisplayed()
        compose.onNodeWithTag("prayer_location_button").assertTextContains("Makkah", substring = true)

        // Countdown-dock contract (2026-10-22): a stretched hero parks the
        // digits in the lower half of the sky card, right above the switcher.
        // This also fails if the layout settle above is removed, since a
        // first-pass (unstretched) layout keeps the digits high.
        val header = compose.onNodeWithTag("prayer_header_card").fetchSemanticsNode()
        val digits = compose.onNodeWithTag("countdown_timer_text").fetchSemanticsNode()
        val digitsTopInHeader = digits.positionInRoot.y - header.positionInRoot.y
        assertTrue(
            "countdown digits must sit in the lower half of the stretched sky card",
            digitsTopInHeader > header.size.height / 2
        )

        // Row-evening contract (2026-10-22): every row measures the same band
        // height — the 48 dp touch-target floor, text + 24 dp as the font
        // grows — highlighted coming-prayer row included, whatever the wall
        // clock makes 'next'. Heights print on failure so an uneven row can
        // be located without a debugger.
        val rowHeights = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA").associateWith { name ->
            compose.onNodeWithTag("simplified_prayer_row_$name").fetchSemanticsNode().size.height
        }
        assertEquals(
            "prayer rows must sit even (highlighted row included): $rowHeights",
            1,
            rowHeights.values.distinct().size
        )
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
        // NUR-009 (layered 2026-10-24): the heading readout is now the LAST
        // card of the lower stack; with the dial absorbing the slack it must
        // dock into the lower reaches of the viewport — its top past 60% of
        // the root height — instead of sitting right under the dial.
        val qiblaRoot = compose.onRoot().fetchSemanticsNode()
        val qiblaMetrics = compose.onNodeWithTag("qibla_metrics_card").fetchSemanticsNode()
        assertTrue(
            "qibla metrics card must dock low (top=${qiblaMetrics.positionInRoot.y} vs limit=${qiblaRoot.size.height * 0.6f})",
            qiblaMetrics.positionInRoot.y > qiblaRoot.size.height * 0.6f
        )
        capture("02_qibla")
    }

    @Test
    fun qiblaScreenDarkRenders() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                NourContentFrame { QiblaScreen(location = location) }
            }
        }
        compose.onNodeWithTag("qibla_mode_status").assertIsDisplayed()
        capture("11_qibla_dark")
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

    /**
     * Chrome capture: the floating bottom bar is rendered by a real Scaffold
     * over the Quran screen, at the same compact breakpoint MainActivity uses.
     * This is the only capture that shows the navigation surface itself; the
     * per-screen captures intentionally render their screen in isolation.
     */
    @Test
    fun navigationChromeRenders() {
        var lastSelected = -1
        compose.setContent {
            val selection = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableIntStateOf(2)
            }
            MyApplicationTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        NourFloatingNavigationBar(
                            items = navigationItems,
                            selectedIndex = selection.intValue,
                            onSelected = {
                                selection.intValue = it
                                lastSelected = it
                            },
                            modifier = Modifier.testTag("nour_bottom_navigation")
                        )
                    }
                ) { innerPadding ->
                    NourContentFrame(
                        modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())
                    ) { QuranScreen() }
                }
            }
        }
        compose.onNodeWithTag("nour_bottom_navigation").assertIsDisplayed()
        compose.onNodeWithTag("nav_quran").assertIsSelected()
        capture("08_navigation_chrome")
        compose.onNodeWithTag("nav_salah").performClick()
        compose.mainClock.advanceTimeBy(200)
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(0, lastSelected) }
        compose.onNodeWithTag("nav_salah").assertIsSelected()
    }

    /** Same chrome, dark theme: opaque charcoal surfaces must not turn glassy. */
    @Test
    fun navigationChromeDarkRenders() {
        compose.setContent {
            val selection = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableIntStateOf(2)
            }
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        NourFloatingNavigationBar(
                            items = navigationItems,
                            selectedIndex = selection.intValue,
                            onSelected = { selection.intValue = it },
                            modifier = Modifier.testTag("nour_bottom_navigation_dark")
                        )
                    }
                ) { innerPadding ->
                    NourContentFrame(
                        modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())
                    ) { QuranScreen() }
                }
            }
        }
        compose.onNodeWithTag("nour_bottom_navigation_dark").assertIsDisplayed()
        compose.onNodeWithTag("nav_quran").assertIsSelected()
        capture("09_navigation_chrome_dark")
    }

    /** Expanded layout: the floating rail reserves its own gutter, labels stay visible. */
    @Test
    @Config(sdk = [36], qualifiers = "w900dp-h1000dp-xhdpi")
    fun navigationRailRenders() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Row(Modifier.fillMaxSize()) {
                        NourFloatingNavigationRail(
                            items = navigationItems,
                            selectedIndex = 1,
                            onSelected = {},
                            modifier = Modifier
                                .width(NourSize.navRailReserve)
                                .fillMaxHeight()
                                .testTag("nour_navigation_rail")
                        )
                        NourContentFrame(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) { QuranScreen() }
                    }
                }
            }
        }
        compose.onNodeWithTag("nour_navigation_rail").assertIsDisplayed()
        compose.onNodeWithTag("nav_qibla").assertIsSelected()
        capture("10_navigation_rail")
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
    fun settingsScreenDarkRenders() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
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
        capture("12_settings_dark")
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

    /**
     * The starfield only draws once the sky is dark, so the day-time two-body
     * capture above never exercises its sparkle/twinkle path. This renders a
     * deterministic night instant (fixed date, Makkah) so the night sky drawing
     * is covered on the JVM too.
     */
    @Test
    fun skyRendersNightStarfield() {
        val night = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.JANUARY, 1, 21, 0, 0)
        }.time
        val times = PrayerCalculationEngine.calculate(
            date = night,
            location = location,
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = PrayerAdjustments()
        )
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Box(Modifier.fillMaxSize()) {
                    ImmersiveSkyBackground(
                        prayerTimes = times,
                        location = location,
                        now = night,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        capture("07_sky_night")
    }
}
