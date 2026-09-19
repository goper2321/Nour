package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun textStyle(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp
)

val Typography = Typography(
    displayLarge = textStyle(48, 56, FontWeight.SemiBold),
    displayMedium = textStyle(40, 48, FontWeight.SemiBold),
    displaySmall = textStyle(36, 44, FontWeight.SemiBold),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold),
    titleLarge = textStyle(22, 28, FontWeight.Medium),
    titleMedium = textStyle(16, 24, FontWeight.Medium),
    titleSmall = textStyle(14, 20, FontWeight.Medium),
    bodyLarge = textStyle(16, 24),
    bodyMedium = textStyle(14, 22),
    bodySmall = textStyle(12, 18),
    labelLarge = textStyle(14, 20, FontWeight.Medium),
    labelMedium = textStyle(12, 18, FontWeight.Medium),
    labelSmall = textStyle(12, 18, FontWeight.Medium)
)
