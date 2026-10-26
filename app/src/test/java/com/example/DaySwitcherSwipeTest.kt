package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
import com.example.features.prayer.ui.DaySwitcherBar
import com.example.ui.theme.MyApplicationTheme
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * NUR-010: the day switcher advances (or retreats) one day when the switcher
 * row is flung horizontally, in addition to the existing tap pills. The
 * direction semantics must flip for RTL interfaces, and the tap pills must
 * keep working untouched.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class DaySwitcherSwipeTest {
    @get:Rule
    val compose = createComposeRule()

    // Plain field counters: gestures and their callbacks are exercised and
    // settled on the Robolectric main thread, so plain access is deterministic.
    private var prevCount = 0
    private var nextCount = 0

    private fun setContent(rtl: Boolean = false) {
        prevCount = 0
        nextCount = 0
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                MyApplicationTheme(dynamicColor = false) {
                    DaySwitcherBar(
                        selectedDate = Date(),
                        dayOffset = 0,
                        onPreviousDay = { prevCount++ },
                        onNextDay = { nextCount++ },
                        onResetToToday = {},
                        modifier = Modifier.testTag("day_switcher_bar")
                    )
                }
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun ltrSwipeLeftAdvancesToNextDay() {
        setContent(rtl = false)
        compose.onNodeWithTag("day_switcher_bar").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        assertEquals("a leftwards fling on an LTR interface must be NEXT", 1, nextCount)
        assertEquals(0, prevCount)
    }

    @Test
    fun ltrSwipeRightRetreatsToPreviousDay() {
        setContent(rtl = false)
        compose.onNodeWithTag("day_switcher_bar").performTouchInput { swipeRight() }
        compose.waitForIdle()
        assertEquals("a rightwards fling on an LTR interface must be PREVIOUS", 1, prevCount)
        assertEquals(0, nextCount)
    }

    @Test
    fun rtlSwipeRightAdvancesToNextDay() {
        setContent(rtl = true)
        compose.onNodeWithTag("day_switcher_bar").performTouchInput { swipeRight() }
        compose.waitForIdle()
        assertEquals("the direction mapping must flip for RTL: rightwards = NEXT", 1, nextCount)
        assertEquals(0, prevCount)
    }

    @Test
    fun previousPillTapStillWorks() {
        setContent(rtl = false)
        compose.onNodeWithTag("day_switcher_prev").performClick()
        compose.waitForIdle()
        assertEquals("taps stay primary: the previous pill must keep working", 1, prevCount)
        assertEquals(0, nextCount)
    }
}
