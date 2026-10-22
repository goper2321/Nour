package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.ui.unit.dp

/** Shared layout and motion values. Text sizes belong to Typography. */
object NourSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    /** Standard content rhythm (Apple 16 pt). */
    val lg = 16.dp
    /** Section rhythm (Apple 24 pt). */
    val xl = 24.dp
    val xxl = 32.dp
}

object NourSize {
    val touchTarget = 48.dp
    /** Visual circle for compact icon actions; the touch target stays 48 dp. */
    val circularControl = 44.dp
    val icon = 24.dp
    val smallIcon = 20.dp
    /** Leading icon inside a settings/list row (22 dp): between [smallIcon] and [icon]. */
    val rowIcon = 22.dp
    val badge = 40.dp
    val input = 56.dp
    val compactBreakpoint = 600.dp
    val expandedBreakpoint = 840.dp
    val readingWidth = 720.dp
    val contentWidth = 960.dp
    val compass = 320.dp
    val skyHeight = 180.dp
    val border = 1.dp
    val selectedBorder = 2.dp
    val flat = 0.dp
    val raised = 2.dp

    // Navigation geometry. The floating bar sits above the system inset and
    // never touches the screen edge; the rail keeps the same 24 dp shape.
    val navBarHeight = 68.dp
    val navRailWidth = 72.dp
    /** Horizontal space reserved for the floating rail in the expanded layout. */
    val navRailReserve = 88.dp
    val toolbarHeight = 52.dp
}

object NourElevation {
    /** Cards stay flat; separation comes from the surface ladder and hairlines. */
    val card = 0.dp
    /** Contextual floating toolbar. */
    val toolbar = 6.dp
    /** Bottom navigation / leading rail. */
    val floating = 10.dp
    /** Resting segmented-control thumb. */
    val control = 1.dp
}

object NourMotion {
    // iOS 27 motion: springs for gestures, M3 easings for cross-fades.
    const val short = 200
    const val medium = 300
    const val long = 500
    val easing = FastOutSlowInEasing

    /**
     * Mirrors [NourReduceMotion.Local] so the helpers below, which run without a
     * composable context, can still honour the user's reduced-motion setting.
     * Set once per composition by [NourReduceMotionProvider].
     */
    internal var reduced: Boolean = false

    /**
     * `tween` that collapses to an instant change when motion is reduced.
     * Prefer this over calling `tween(...)` directly.
     */
    fun duration(millis: Int): Int = if (reduced) 0 else millis

    /**
     * `spring` that collapses to an instant change when motion is reduced.
     * Prefer this over calling `spring(...)` directly for finite transitions
     * (slides, fades, size and colour changes). Uses the Compose
     * [Spring.StiffnessMediumLow] / [Spring.DampingRatioNoBouncy] defaults unless
     * overridden, so call sites only state what they need.
     */
    fun <T> spring(
        stiffness: Float = Spring.StiffnessMedium,
        dampingRatio: Float = 0.75f
    ): FiniteAnimationSpec<T> =
        if (reduced) snap()
        else androidx.compose.animation.core.spring(stiffness = stiffness, dampingRatio = dampingRatio)

    /** True when decorative, endlessly-repeating motion must not be started. */
    val allowContinuous: Boolean get() = !reduced
}


/**
 * Single opacity scale for accent-derived fills. The accent itself is always
 * MaterialTheme.colorScheme.primary — never a hardcoded color.
 */
object NourAlpha {
    const val BadgeTint = 0.15f
    // Opaque doctrine (iOS 27): hairlines are solid outline-variant, never alpha.
    const val Hairline = 1.0f

    /** Canonical outline opacity for resting card/panel borders (opaque hairline). */
    const val CardBorder = 1.0f

    /** Remaining-track veil for sky-adaptive progress bars. */
    const val TrackVeil = 0.30f
}
