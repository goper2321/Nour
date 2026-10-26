package com.example.features.prayer.ui

import kotlin.math.abs

/**
 * NUR-010 — pure decision logic for the day-switcher swipe.
 *
 * A horizontal fling with at least [MIN_TRAVEL_DP] of travel advances or
 * retreats the selected day by one, complementing (never replacing) the tap
 * targets on the switcher pills. Direction semantics flip for RTL
 * interfaces: a swipe to the left means "next" in LTR and "previous" in RTL,
 * matching the automirrored arrows of the bar.
 */
enum class DaySwipeDirection { PREVIOUS, NEXT }

object DaySwipeGestureLogic {

    /** Checklist threshold: "detectable fling > 24 dp travel". */
    const val MIN_TRAVEL_DP = 24f

    /**
     * Maps a finished horizontal drag to a day change.
     *
     * @param travelPx  signed horizontal travel accumulated from drag start,
     *                  in pixels (positive = pointer moved rightwards)
     * @param minTravelPx the converted [MIN_TRAVEL_DP] threshold in pixels
     * @param isRtl whether the surrounding layout direction is RTL
     */
    fun directionForTravel(
        travelPx: Float,
        minTravelPx: Float,
        isRtl: Boolean
    ): DaySwipeDirection? {
        if (travelPx.isNaN() || minTravelPx <= 0f || abs(travelPx) < minTravelPx) {
            return null
        }
        val rightwards = travelPx > 0f
        return when {
            rightwards && !isRtl -> DaySwipeDirection.PREVIOUS
            rightwards -> DaySwipeDirection.NEXT
            else -> if (!isRtl) DaySwipeDirection.NEXT else DaySwipeDirection.PREVIOUS
        }
    }
}
