package com.example

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.core.app.ApplicationProvider
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.ui.PrayerScreen
import com.example.features.prayer.ui.SeamlessPrayerHeader
import com.example.features.prayer.ui.SimplifiedPrayerTimesCard
import com.example.features.qibla.ui.QiblaScreen
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.NourPreferences
import com.example.features.settings.QuranReadingTheme
import com.example.features.settings.QuranTypeface
import com.example.features.settings.ui.SettingsScreen
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.LocalUse24HourFormat
import com.example.ui.components.NourContentFrame
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Date

/**
 * Configuration-matrix captures: dark theme, 200% font scale, small window, RTL,
 * and the Quran reader states.
 *
 * These exist so layout can be *seen* on this host (which has no device). They
 * are rendering checks, not device verification: sensors, Doze, TalkBack and
 * real IME behaviour still need hardware.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenCaptureMatrixTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun pauseClock() {
        compose.mainClock.autoAdvance = false
    }

    private val location = UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0, false)

    private fun capture(name: String) {
        // Root visibility is a cheap smoke assertion, so a capture test can
        // fail if composition produced nothing rather than only throwing.
        compose.onRoot().assertIsDisplayed()
        compose.onRoot().captureRoboImage("build/screen-verify/$name.png")
    }

    /** Let pending effects/layout settle without letting timers fire. */
    private fun settle(frames: Int = 4) {
        repeat(frames) { compose.mainClock.advanceTimeByFrame() }
    }

    /**
     * B7: measure, don't eyeball. `NourContentFrame` owns the 16 dp side
     * gutter, so the tagged page root must sit inside it on every variant.
     */
    private fun assertPageHasFrameGutter(pageTag: String) {
        val window = compose.onRoot().getBoundsInRoot()
        val page = compose.onNodeWithTag(pageTag).getBoundsInRoot()
        assertTrue("page left=${page.left} (gutter < 16.dp)", page.left >= 16.dp - 0.5.dp)
        assertTrue(
            "page right=${page.right}, window right=${window.right} (gutter < 16.dp)",
            page.right <= window.right - 16.dp + 0.5.dp
        )
    }

    @Composable
    private fun Direction(language: AppLanguage, content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalAppLanguage provides language,
            LocalAppStrings provides AppStrings.forLanguage(language),
            content = content
        )
    }

    @Composable
    private fun ScaledDensity(fontScale: Float, content: @Composable () -> Unit) {
        val base = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale), content = content)
    }

    private fun prayerScreen() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                    PrayerScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments(),
                        // Non-null, as in MainActivity: the "Default location"
                        // control is only disabled when no callback is wired.
                        onRequestLocationUpdate = {}
                    )
                }
            }
        }
        settle()
    }

    @Test
    fun prayerDark() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                    PrayerScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments(),
                        onRequestLocationUpdate = {}
                    )
                }
            }
        }
        settle()
        capture("m01_prayer_dark")
    }

    @Test
    fun prayerLargeFont() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(2f) {
                    NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                        PrayerScreen(
                            location = location,
                            calculationMethod = CalculationMethod.UMM_AL_QURA,
                            madhab = Madhab.SHAFI_MALIKI_HANBALI,
                            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                            adjustments = PrayerAdjustments(),
                            onRequestLocationUpdate = {}
                        )
                    }
                }
            }
        }
        settle()
        capture("m02_prayer_font200")
    }

    @Test
    fun prayerArabicRtl() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Direction(AppLanguage.ARABIC) {
                    CompositionLocalProvider(LocalUse24HourFormat provides true) {
                        NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                            PrayerScreen(
                                location = UserLocation(24.7136, 46.6753, "الرياض", "السعودية", 3.0, false),
                                calculationMethod = CalculationMethod.UMM_AL_QURA,
                                madhab = Madhab.SHAFI_MALIKI_HANBALI,
                                highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                                adjustments = PrayerAdjustments(),
                                onRequestLocationUpdate = {}
                            )
                        }
                    }
                }
            }
        }
        settle()
        capture("m03_prayer_ar_rtl")
    }

    @Test
    fun prayerArabicRtlLargeFont() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Direction(AppLanguage.ARABIC) {
                    CompositionLocalProvider(LocalUse24HourFormat provides true) {
                        ScaledDensity(2f) {
                            NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                                PrayerScreen(
                                    location = UserLocation(24.7136, 46.6753, "الرياض", "السعودية", 3.0, false),
                                    calculationMethod = CalculationMethod.UMM_AL_QURA,
                                    madhab = Madhab.SHAFI_MALIKI_HANBALI,
                                    highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                                    adjustments = PrayerAdjustments(),
                                    onRequestLocationUpdate = {}
                                )
                            }
                        }
                    }
                }
            }
        }
        settle()
        compose.onNodeWithTag("prayer_screen_list").assertIsDisplayed()
        compose.onNodeWithTag("countdown_timer_text").assertIsDisplayed()
        capture("m15_prayer_ar_rtl_font200")
    }

    @Test
    fun qiblaArabicRtlLargeFont() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Direction(AppLanguage.ARABIC) {
                    ScaledDensity(2f) {
                        NourContentFrame {
                            QiblaScreen(
                                location = UserLocation(24.7136, 46.6753, "الرياض", "السعودية", 3.0, false)
                            )
                        }
                    }
                }
            }
        }
        settle()
        compose.onNodeWithTag("qibla_header_title").assertIsDisplayed()
        compose.onNodeWithTag("qibla_accuracy_indicator").assertIsDisplayed()
        capture("m16_qibla_ar_rtl_font200")
    }

    @Test
    fun settingsThemeControlLargeFont() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(2f) {
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
        }
        settle()
        compose.onNodeWithTag("settings_theme_control").assertIsDisplayed()
        compose.onNodeWithTag("settings_theme_control").assertHeightIsAtLeast(52.dp)
        compose.onNodeWithText("System default", substring = true).assertIsDisplayed()
        capture("m17_settings_font200")
    }

    /** The hero at local noon, when the sky behind it is at its brightest. */
    @Test
    fun heroAtNoon() {
        val date = Date()
        val times = PrayerCalculationEngine.calculate(
            date = date,
            location = location,
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = PrayerAdjustments()
        )
        // 13:05 local on the same day: the sun is high, so the sky is pale and
        // the hero text has to flip to a dark colour.
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 13)
        cal.set(java.util.Calendar.MINUTE, 5)
        cal.set(java.util.Calendar.SECOND, 0)
        val midday = cal.time
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SeamlessPrayerHeader(
                    location = location,
                    countdownState = times.getCountdownState(midday),
                    todayPrayerTimes = times,
                    now = midday,
                    onRequestLocationUpdate = {}
                )
            }
        }
        settle()
        capture("m11_hero_noon")
    }

    private fun readerScreen(surahNumber: Int, cards: Boolean) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).readingMode = if (cards) "CARDS" else "CONTINUOUS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    QuranScreen(
                        openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(surahNumber),
                        onOpenSurahConsumed = {}
                    )
                }
            }
        }
        // The reader-open effect lands on the first frame; a few more let the
        // list lay out before the capture.
        settle(6)
    }

    @Test
    fun quranReaderCards() {
        readerScreen(1, cards = true)
        capture("m04_reader_cards")
    }

    @Test
    fun quranReaderCardsLargeFont() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).readingMode = "CARDS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(1.6f) {
                    NourContentFrame {
                        QuranScreen(
                            openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(1),
                            onOpenSurahConsumed = {}
                        )
                    }
                }
            }
        }
        settle(6)
        capture("m05_reader_cards_font160")
    }

    /** Worst case for the verse-card header: 200% font on a narrow phone. */
    @Test
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun quranReaderCardsLargeFontNarrow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).readingMode = "CARDS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(2f) {
                    NourContentFrame {
                        QuranScreen(
                            openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(1),
                            onOpenSurahConsumed = {}
                        )
                    }
                }
            }
        }
        settle(6)
        capture("m14_reader_cards_font200_narrow")
    }

    @Test
    fun quranReaderContinuous() {
        readerScreen(1, cards = false)
        capture("m06_reader_continuous")
    }

    @Test
    fun quranReaderArabic() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).readingMode = "CARDS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Direction(AppLanguage.ARABIC) {
                    NourContentFrame {
                        QuranScreen(
                            openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(112),
                            onOpenSurahConsumed = {}
                        )
                    }
                }
            }
        }
        settle(6)
        capture("m07_reader_ar")
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun prayerSmallScreen() {
        prayerScreen()
        capture("m08_prayer_small")
    }

    @Test
    fun prayerTimesCard() {
        val times = PrayerCalculationEngine.calculate(
            date = Date(),
            location = location,
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = PrayerAdjustments()
        )
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SimplifiedPrayerTimesCard(prayerTimes = times, use24Hour = true, nextPrayer = PrayerName.ASR)
                    }
                }
            }
        }
        settle()
        capture("m09_times_card")
    }

    @Test
    fun prayerTimesCardLargeFont() {
        val times = PrayerCalculationEngine.calculate(
            date = Date(),
            location = location,
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = PrayerAdjustments()
        )
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(2f) {
                    NourContentFrame {
                        SimplifiedPrayerTimesCard(prayerTimes = times, use24Hour = true, nextPrayer = PrayerName.ASR)
                    }
                }
            }
        }
        settle()
        capture("m10_times_card_font200")
    }

    /**
     * The same Salah hero after ~2 s of frames, so every colour tween has
     * finished. Comparing this with the 4-frame captures separates "the design
     * really chose a low-contrast colour" from "the capture froze mid-tween".
     */
    @Test
    fun prayerSettled() {
        prayerScreen()
        settle(120)
        capture("m12_prayer_settled")
    }

    @Test
    fun heroAtNoonSettled() {
        val date = Date()
        val times = PrayerCalculationEngine.calculate(
            date = date,
            location = location,
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
            adjustments = PrayerAdjustments()
        )
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 13)
        cal.set(java.util.Calendar.MINUTE, 5)
        cal.set(java.util.Calendar.SECOND, 0)
        val midday = cal.time
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SeamlessPrayerHeader(
                    location = location,
                    countdownState = times.getCountdownState(midday),
                    todayPrayerTimes = times,
                    now = midday,
                    onRequestLocationUpdate = {}
                )
            }
        }
        settle(120)
        capture("m13_hero_noon_settled")
    }

    // NUR-008: the high-contrast toggle must visibly compose with BOTH themes.
    // Same Salah layout as m01/prayerDark, rendered through the M3 contrast
    // level 1.0 scheme, so the owner can see light+HC and dark+HC side by side
    // in the final capture pass. Named m18/m19 to mirror the matrix's scheme
    // (the checklist's placeholder "m04_*" collides with m04_reader_cards).
    @Test
    fun prayerHighContrast() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false, highContrast = true) {
                NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                    PrayerScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments(),
                        onRequestLocationUpdate = {}
                    )
                }
            }
        }
        settle()
        capture("m18_prayer_high_contrast")
    }

    @Test
    fun prayerDarkHighContrast() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true, highContrast = true) {
                NourContentFrame(contentPadding = PaddingValues(0.dp)) {
                    PrayerScreen(
                        location = location,
                        calculationMethod = CalculationMethod.UMM_AL_QURA,
                        madhab = Madhab.SHAFI_MALIKI_HANBALI,
                        highLatRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
                        adjustments = PrayerAdjustments(),
                        onRequestLocationUpdate = {}
                    )
                }
            }
        }
        settle()
        capture("m19_prayer_dark_high_contrast")
    }

    @Test
    fun quranReaderSwipeMovesToNextSurah() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).readingMode = "CARDS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    QuranScreen(
                        openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(1),
                        onOpenSurahConsumed = {}
                    )
                }
            }
        }
        settle(8)
        compose.onNodeWithTag("quran_screen").assertExists()
        compose.onNodeWithTag("surah_page_1").assertIsDisplayed()
        compose.onNodeWithTag("surah_pager").performTouchInput { swipeLeft() }
        settle(40)
        compose.onNodeWithTag("surah_page_2").assertIsDisplayed()
        assertPageHasFrameGutter("surah_page_2")
        capture("m20_reader_swipe_surah_2")
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun quranTabsFont200Narrow() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(2f) {
                    NourContentFrame { QuranScreen() }
                }
            }
        }
        val english = AppStrings()
        listOf(english.tabSurah, english.tabBookmark, english.tabSearch).forEach { label ->
            val bounds = compose.onNodeWithText(label).getBoundsInRoot()
            assertTrue("$label width=${bounds.width}", bounds.width > 0.dp)
            assertTrue("$label height=${bounds.height}", bounds.height > 0.dp)
        }
        capture("m23_quran_tabs_font200_narrow")
    }

    // ---- C3 reading themes / C4 typeface --------------------------------

    @Test
    fun quranReaderPaperTheme() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).also {
            it.readingMode = "CARDS"
            it.quranReadingTheme = QuranReadingTheme.PAPER
        }
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    QuranScreen(
                        openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(1),
                        onOpenSurahConsumed = {}
                    )
                }
            }
        }
        settle(8)
        capture("m26_reader_paper")
    }

    @Test
    fun quranReaderNightTheme() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).also {
            it.readingMode = "CARDS"
            it.quranReadingTheme = QuranReadingTheme.NIGHT
        }
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    QuranScreen(
                        openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(1),
                        onOpenSurahConsumed = {}
                    )
                }
            }
        }
        settle(8)
        compose.onNodeWithTag("ayah_card_1").assertExists()
        capture("m27_reader_night")
    }

    @Test
    fun quranReaderAmiriTypeface() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).also {
            it.readingMode = "CARDS"
            it.quranTypeface = QuranTypeface.AMIRI
        }
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourContentFrame {
                    QuranScreen(
                        openSurahRequest = com.example.features.quran.data.QuranRepository.getSurah(1),
                        onOpenSurahConsumed = {}
                    )
                }
            }
        }
        settle(8)
        compose.onNodeWithTag("ayah_card_1").assertExists()
        capture("m24_reader_amiri")
    }

}
