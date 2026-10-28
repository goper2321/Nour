package com.example.features.prayer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Adaptive ink selection for the countdown that sits directly on the sky.
 *
 * The hero has no scrim and no halo, so legibility is carried entirely by
 * choosing, for each text role, the palette candidate that contrasts best with
 * the sky pixels actually behind the glyphs. This file owns the scoring; the
 * header owns the layout and the sampling box.
 */

internal enum class CountdownTextRole {
    Primary,
    Muted,
    Accent
}

internal data class CountdownTextPalette(
    val primaryDark: Color,
    val primaryLight: Color,
    val mutedDark: Color,
    val mutedLight: Color,
    val accentDark: Color,
    val accentLight: Color
)

internal data class CountdownLuminance(
    val mean: Float = 0.5f,
    val low: Float = 0.25f,
    val high: Float = 0.75f
)

private fun contrastRatio(first: Float, second: Float): Float {
    val lighter = max(first, second)
    val darker = min(first, second)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** Contrast against the darkest *and* the brightest part of the sampled area. */
private fun worstCaseContrast(color: Color, sample: CountdownLuminance): Float {
    val foreground = color.luminance()
    return min(
        contrastRatio(foreground, sample.low),
        contrastRatio(foreground, sample.high)
    )
}

private fun countdownDarkVariant(
    palette: CountdownTextPalette,
    role: CountdownTextRole
): Color = when (role) {
    CountdownTextRole.Primary -> palette.primaryDark
    CountdownTextRole.Muted -> palette.mutedDark
    CountdownTextRole.Accent -> palette.accentDark
}

private fun countdownLightVariant(
    palette: CountdownTextPalette,
    role: CountdownTextRole
): Color = when (role) {
    CountdownTextRole.Primary -> palette.primaryLight
    CountdownTextRole.Muted -> palette.mutedLight
    CountdownTextRole.Accent -> palette.accentLight
}

/** Weighted contrast score: worst sampled pixel first, mean as a tie-breaker. */
private fun contrastScore(sample: CountdownLuminance, color: Color): Float {
    val foreground = color.luminance()
    val averageCase = contrastRatio(foreground, sample.mean)
    return worstCaseContrast(color, sample) * 0.68f + averageCase * 0.32f
}

private fun pickContrastColor(
    sample: CountdownLuminance,
    darkVariant: Color,
    lightVariant: Color
): Color {
    val darkScore = contrastScore(sample, darkVariant)
    val lightScore = contrastScore(sample, lightVariant)
    return when {
        lightScore > darkScore + 0.18f -> lightVariant
        darkScore > lightScore + 0.18f -> darkVariant
        // The scores are within a coin flip; break the tie by the mean. The
        // light/dark contrast curves of these palettes cross near luminance
        // 0.18-0.21, so anything brighter than 0.20 favours the dark variant.
        // (0.45 used to keep white far past the crossover: on mid-luminance
        // skies that cost the small time line 1-1.5:1 — measured 3.44:1 in
        // `m13_hero_noon_settled.png` where dark measures 4.9:1.)
        sample.mean >= 0.20f -> darkVariant
        else -> lightVariant
    }
}

internal fun adaptiveTextColor(
    sample: CountdownLuminance,
    palette: CountdownTextPalette,
    role: CountdownTextRole
): Color {
    val tinted = pickContrastColor(
        sample = sample,
        darkVariant = countdownDarkVariant(palette, role),
        lightVariant = countdownLightVariant(palette, role)
    )
    // The countdown numerals are a brand-blue pair, and blue-on-blue is the
    // one sky the palette cannot win: on the midday gradient neither blue
    // variant cleared 3:1 (measured 1.5-2.7:1 across the digits). They are the
    // hero's headline, so when the tint cannot carry large-text contrast the
    // numerals fall back to the neutral pair instead of fading into the sky.
    if (role != CountdownTextRole.Accent || worstCaseContrast(tinted, sample) >= 3.0f) {
        return tinted
    }
    return pickContrastColor(sample, palette.primaryDark, palette.primaryLight)
}
