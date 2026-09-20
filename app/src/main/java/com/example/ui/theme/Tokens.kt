package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.unit.dp

/** Shared layout and motion values. Text sizes belong to Typography. */
object NourSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 18.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object NourSize {
    val touchTarget = 48.dp
    val icon = 24.dp
    val smallIcon = 20.dp
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
}

object NourMotion {
    const val short = 150
    const val medium = 250
    const val long = 300
    val easing = FastOutSlowInEasing
}

/**
 * Single opacity scale for accent-derived fills. The accent itself is always
 * MaterialTheme.colorScheme.primary — never a hardcoded color.
 */
object NourAlpha {
    const val BadgeTint = 0.15f
    const val Hairline = 0.35f
}
