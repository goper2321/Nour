package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.ui.DaySwitcherBar
import com.example.features.prayer.ui.SeamlessPrayerHeader
import com.example.features.prayer.ui.SimplifiedPrayerTimesCard
import com.example.features.settings.NourPreferences
import com.example.ui.components.NourFloatingNavigationBar
import com.example.ui.components.NourNavItem
import com.example.ui.components.NourSearchField
import com.example.ui.components.NourSegmentOption
import com.example.ui.components.NourSegmentedControl
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NourDesignSystemTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun defaultPreferencesUse24HourFormat() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = NourPreferences(context)
        assertTrue("Expected 24-hour format to be default true", prefs.time24h)
    }

    @Test
    fun countdownCardIntegratesDynamicSkyBackground() {
        compose.mainClock.autoAdvance = false
        val location = UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)
        val now = Date()
        val times = PrayerCalculationEngine.calculate(date = now, location = location, method = CalculationMethod.UMM_AL_QURA)
        val countdown = CountdownState(
            currentPrayer = PrayerName.DHUHR,
            nextPrayer = PrayerName.ASR,
            remainingMillis = 3600000L,
            progress = 0.5f
        )

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SeamlessPrayerHeader(
                    location = location,
                    countdownState = countdown,
                    todayPrayerTimes = times,
                    now = now
                )
            }
        }

        compose.onNodeWithTag("prayer_header_card").assertIsDisplayed()
        compose.onNodeWithTag("prayer_sky_background").assertIsDisplayed()
        compose.onNodeWithTag("countdown_timer_text").assertIsDisplayed()
        compose.onNodeWithTag("countdown_progress_bar").assertIsDisplayed()
    }

    @Test
    fun dayControlsKeepTheirCallbacks() {
        var previous = 0
        var next = 0
        var today = 0
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                DaySwitcherBar(selectedDate = Date(0), dayOffset = 1, onPreviousDay = { previous++ }, onNextDay = { next++ }, onResetToToday = { today++ })
            }
        }
        compose.onNodeWithTag("day_switcher_prev").performClick()
        compose.onNodeWithTag("day_switcher_next").performClick()
        compose.onNodeWithTag("day_switcher_reset").performClick()
        compose.runOnIdle {
            assertEquals(1, previous)
            assertEquals(1, next)
            assertEquals(1, today)
        }
    }

    @Test
    fun simplifiedCardShowsFivePrayerRowsReadOnly() {
        val location = UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)
        val times = PrayerCalculationEngine.calculate(
            date = Date(),
            location = location,
            method = CalculationMethod.UMM_AL_QURA
        )
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SimplifiedPrayerTimesCard(prayerTimes = times, use24Hour = true)
            }
        }
        compose.onNodeWithTag("simplified_prayer_card").assertIsDisplayed()
        compose.onNodeWithTag("simplified_prayer_row_FAJR").assertIsDisplayed()
        compose.onNodeWithTag("simplified_prayer_row_DHUHR").assertIsDisplayed()
        compose.onNodeWithTag("simplified_prayer_row_ASR").assertIsDisplayed()
        compose.onNodeWithTag("simplified_prayer_row_MAGHRIB").assertIsDisplayed()
        compose.onNodeWithTag("simplified_prayer_row_ISHA").assertIsDisplayed()
        // Sunrise is intentionally outside the five-prayer summary.
        compose.onNodeWithTag("simplified_prayer_row_SUNRISE").assertDoesNotExist()
    }

    @Test
    fun searchFieldTagTargetsEditableTextNode() {
        val query = mutableStateOf("")
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourSearchField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    placeholder = "Search",
                    fieldModifier = Modifier.testTag("test_search_field")
                )
            }
        }
        compose.onNodeWithTag("test_search_field").performTextInput("Allah")
        compose.runOnIdle {
            assertEquals("Allah", query.value)
        }
    }

    @Test
    fun floatingNavigationBarGrowsForTwoLabelLinesAtLargeFont() {
        val selected = mutableStateOf(0)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                MyApplicationTheme(dynamicColor = false) {
                    NourFloatingNavigationBar(
                        items = listOf(
                            NourNavItem(
                                label = "Gebetszeiten",
                                selectedIcon = Icons.Rounded.Schedule,
                                unselectedIcon = Icons.Outlined.Schedule,
                                testTag = "nav_test"
                            )
                        ),
                        selectedIndex = selected.value,
                        onSelected = { selected.value = it }
                    )
                }
            }
        }
        compose.onNodeWithTag("nav_test").assertIsDisplayed()
        compose.onNodeWithTag("nour_navigation_bar").assertHeightIsAtLeast(90.dp)
    }

    @Test
    fun segmentedControlReportsSelection() {
        val selected = mutableStateOf("system")
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                NourSegmentedControl(
                    options = listOf(
                        NourSegmentOption("system", "System", testTag = "segment_system"),
                        NourSegmentOption("light", "Light", testTag = "segment_light"),
                        NourSegmentOption("dark", "Dark", testTag = "segment_dark")
                    ),
                    selected = selected.value,
                    onSelected = { selected.value = it }
                )
            }
        }
        compose.onNodeWithTag("segment_system").assertIsSelected()
        compose.onNodeWithTag("segment_system").assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("segment_dark").performClick()
        compose.onNodeWithTag("segment_dark").assertIsSelected()
        compose.runOnIdle { assertEquals("dark", selected.value) }
    }
}
