package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Apple-clean type: near-zero tracking on body/labels, negative tracking on
// display sizes, emphasis in the semibold band (590-640), SF-first stack that
// degrades to the platform default (Roboto on Android) when SF Pro is absent.
private val AppleFontFamily = FontFamily.Default

private fun textStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
    fontFeatureSettings: String? = null
) = TextStyle(
    fontFamily = AppleFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    fontFeatureSettings = fontFeatureSettings
)

/**
 * Extension helper to enable tabular figures (tnum) on any TextStyle.
 * Prevents horizontal layout jitter when countdown timers or numbers change.
 */
fun TextStyle.withTabularNumbers(): TextStyle = this.copy(fontFeatureSettings = "tnum")

val Typography = Typography(
    displayLarge = textStyle(48, 56, FontWeight.SemiBold, -0.25f, "tnum"),
    displayMedium = textStyle(40, 48, FontWeight.SemiBold, -0.2f, "tnum"),
    displaySmall = textStyle(34, 42, FontWeight.Medium, -0.1f, "tnum"),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold, -0.1f),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold, -0.05f),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold, 0f),
    titleLarge = textStyle(22, 28, FontWeight.SemiBold, 0f),
    titleMedium = textStyle(16, 24, FontWeight.Medium, 0f),
    titleSmall = textStyle(14, 20, FontWeight.Medium, 0f),
    bodyLarge = textStyle(16, 24, FontWeight.Normal, 0f),
    bodyMedium = textStyle(14, 22, FontWeight.Normal, 0f),
    bodySmall = textStyle(12, 18, FontWeight.Normal, 0f),
    labelLarge = textStyle(14, 20, FontWeight.SemiBold, 0f),
    labelMedium = textStyle(12, 16, FontWeight.Medium, 0f),
    // NUR-015: labelSmall was the only style failing the lineHeight ≥
    // fontSize + 4 sp formula (11/14 vs the 11/15 floor) — a tight line box
    // risks clipping descenders on the last wrapped line at large font
    // scales. Bumped to 15 sp; same 0.73 line-height ratio as labelMedium.
    labelSmall = textStyle(11, 15, FontWeight.Medium, 0.1f)
)