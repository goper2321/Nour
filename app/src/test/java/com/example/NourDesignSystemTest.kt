package com.example

import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.ui.DaySwitcherBar
import com.example.features.prayer.ui.PrayerCard
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
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
        compose.onNodeWithTag("day_switcher_today_btn").performClick()
        compose.runOnIdle {
            assertEquals(1, previous)
            assertEquals(1, next)
            assertEquals(1, today)
        }
    }

    @Test
    fun prayerCompletionExposesCheckedStateAndCallback() {
        var clicks = 0
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                PrayerCard(prayer = PrayerName.FAJR, timeFormatted = "05:30", isNext = false, isCurrent = false, isSunrise = false, isPrayed = true) { clicks++ }
            }
        }
        compose.onNodeWithTag("log_prayer_FAJR").assertIsOn().performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
