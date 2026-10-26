package com.example.features.prayer

import com.example.features.prayer.ui.DaySwipeDirection
import com.example.features.prayer.ui.DaySwipeGestureLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * NUR-010 — pure swipe-direction mapping for the day switcher (JVM, no
 * Robolectric; the gesture wiring is covered by DaySwitcherSwipeTest).
 */
class DaySwipeGestureLogicTest {

    @Test
    fun `travel below the 24 dp threshold is not a swipe`() {
        // 24 dp at density 1 == 24 px travel; 23 is below, exactly 24 qualifies.
        assertNull(DaySwipeGestureLogic.directionForTravel(23f, 24f, isRtl = false))
        assertNull(DaySwipeGestureLogic.directionForTravel(-23f, 24f, isRtl = false))
        assertNull(DaySwipeGestureLogic.directionForTravel(0f, 24f, isRtl = false))
        assertEquals(
            DaySwipeDirection.PREVIOUS,
            DaySwipeGestureLogic.directionForTravel(24f, 24f, isRtl = false)
        )
    }

    @Test
    fun `ltr swipes - left is next, right is previous`() {
        assertEquals(
            DaySwipeDirection.NEXT,
            DaySwipeGestureLogic.directionForTravel(-60f, 24f, isRtl = false)
        )
        assertEquals(
            DaySwipeDirection.PREVIOUS,
            DaySwipeGestureLogic.directionForTravel(60f, 24f, isRtl = false)
        )
    }

    @Test
    fun `rtl swipes flip - right is next, left is previous`() {
        assertEquals(
            DaySwipeDirection.NEXT,
            DaySwipeGestureLogic.directionForTravel(60f, 24f, isRtl = true)
        )
        assertEquals(
            DaySwipeDirection.PREVIOUS,
            DaySwipeGestureLogic.directionForTravel(-60f, 24f, isRtl = true)
        )
    }

    @Test
    fun `degenerate threshold never resolves a direction`() {
        assertNull(DaySwipeGestureLogic.directionForTravel(60f, 0f, isRtl = false))
        assertNull(DaySwipeGestureLogic.directionForTravel(Float.NaN, 24f, isRtl = false))
    }
}
