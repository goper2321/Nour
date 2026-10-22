package com.example.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace

/**
 * Absolute bottom padding a scroll surface keeps so its last row ends one
 * small breath (8 dp) above the floating bottom bar's band. Content stays
 * readable at the scroll end and is never hidden behind the pill, while the
 * list remains free to draw under the system gesture area while scrolling —
 * the zone below the band is part of the page, not a dead band.
 *
 * NourMainActivity provides the value on every layout: the system
 * navigation inset + the bar's 8 dp gap above it + the bar's real measured
 * band + the breathing gap, so screens don't need to know insets or bar
 * geometry. Without a provider it falls back to [NourSpace.lg].
 *
 * The reader's top toolbar applies the same band-plus-breathing reserve via
 * its own measured value; it is screen-local, not per-activity.
 */
val LocalFloatingBottomExtra: androidx.compose.runtime.ProvidableCompositionLocal<Dp> =
    compositionLocalOf { NourSpace.lg }
