package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun textStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
    fontFeatureSettings: String? = null
) = TextStyle(
    fontFamily = FontFamily.Default,
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
    displayLarge = textStyle(48, 56, FontWeight.Bold, -0.25f, "tnum"),
    displayMedium = textStyle(40, 48, FontWeight.Bold, -0.2f, "tnum"),
    displaySmall = textStyle(34, 42, FontWeight.SemiBold, 0f, "tnum"),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold, 0f),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold, 0f),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold, 0f),
    titleLarge = textStyle(22, 28, FontWeight.SemiBold, 0f),
    titleMedium = textStyle(16, 24, FontWeight.Medium, 0.15f),
    titleSmall = textStyle(14, 20, FontWeight.Medium, 0.1f),
    bodyLarge = textStyle(16, 24, FontWeight.Normal, 0.25f),
    bodyMedium = textStyle(14, 22, FontWeight.Normal, 0.25f),
    bodySmall = textStyle(12, 18, FontWeight.Normal, 0.4f),
    labelLarge = textStyle(14, 20, FontWeight.SemiBold, 0.1f),
    labelMedium = textStyle(12, 16, FontWeight.Medium, 0.4f),
    labelSmall = textStyle(11, 14, FontWeight.Medium, 0.5f)
)

