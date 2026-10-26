package com.example.features.quran.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.example.features.settings.QuranReadingTheme

/**
 * C3 reading palettes (Paper / Night).
 *
 * Both are fully opaque, low-glare card palettes for long reading sessions.
 * They recolor only the reader's cards and containers: the page background,
 * the gaps between cards, app chrome, dialogs and the settings sheet keep the
 * app's own scheme. That is why [applyTo] deliberately leaves `background`
 * and `surface` untouched — the host's grouped page shows through the gaps.
 *
 * Container roles are set from the two table tokens because the reader paints
 * cards with `surfaceContainerLow` (verse cards), headers with
 * `surfaceContainer`, and the control/footer surfaces with
 * `surfaceContainerHigh`. Leaving them at the app's values would mix a light
 * card into Night (or a dark card into Paper) and break text contrast.
 *
 * Action roles are overridden too. The reading theme is independent of the
 * app's light/dark mode, so an untouched light-mode `primary` would be dark
 * blue on Night's near-black card, and an untouched dark-mode
 * `secondaryContainer` would be a navy pill on Paper. Every pair below is
 * checked at WCAG AA (≥ 4.5:1) by `QuranReadingColorsTest`.
 */
internal object QuranReadingColors {
    // Paper (warm, daylight)
    val PaperBackground = Color(0xFFFBF7EE)
    val PaperSurface = Color(0xFFFFFDF7)
    val PaperOnSurface = Color(0xFF1F1B16)
    val PaperOnSurfaceVariant = Color(0xFF5C5347)
    val PaperOutlineVariant = Color(0xFFE4DCCB)
    val PaperContainer = Color(0xFFF5EFE2)
    val PaperContainerHigh = Color(0xFFEFE7D6)
    val PaperPrimary = Color(0xFF6A4A1C)
    val PaperOnPrimary = Color(0xFFFFFFFF)
    val PaperSecondaryContainer = Color(0xFFEADCC0)
    val PaperOnSecondaryContainer = Color(0xFF3F2E14)

    // Night (near-black, warm ink)
    val NightBackground = Color(0xFF12100D)
    val NightSurface = Color(0xFF1C1916)
    val NightOnSurface = Color(0xFFF2E9DA)
    val NightOnSurfaceVariant = Color(0xFFC9BFAE)
    val NightOutlineVariant = Color(0xFF3A342C)
    val NightContainer = Color(0xFF241F1A)
    val NightContainerHigh = Color(0xFF2C2620)
    val NightPrimary = Color(0xFFE7C88E)
    val NightOnPrimary = Color(0xFF241A00)
    val NightSecondaryContainer = Color(0xFF3A3122)
    val NightOnSecondaryContainer = Color(0xFFF0E3C8)
}

/**
 * Returns [base] unchanged for [QuranReadingTheme.SYSTEM], so the default is
 * pixel-identical to the pre-C3 build. Paper/Night replace only the card,
 * container and action roles the reader paints; `background`, `surface` and
 * `surfaceVariant` are inherited so the grouped page and its inter-card gaps
 * stay app-themed.
 */
internal fun QuranReadingTheme.applyTo(base: ColorScheme): ColorScheme = when (this) {
    QuranReadingTheme.SYSTEM -> base
    QuranReadingTheme.PAPER -> base.copy(
        onSurface = QuranReadingColors.PaperOnSurface,
        onSurfaceVariant = QuranReadingColors.PaperOnSurfaceVariant,
        outlineVariant = QuranReadingColors.PaperOutlineVariant,
        surfaceContainerLowest = QuranReadingColors.PaperSurface,
        surfaceContainerLow = QuranReadingColors.PaperSurface,
        surfaceContainer = QuranReadingColors.PaperContainer,
        surfaceContainerHigh = QuranReadingColors.PaperContainerHigh,
        surfaceContainerHighest = QuranReadingColors.PaperContainerHigh,
        primary = QuranReadingColors.PaperPrimary,
        onPrimary = QuranReadingColors.PaperOnPrimary,
        secondaryContainer = QuranReadingColors.PaperSecondaryContainer,
        onSecondaryContainer = QuranReadingColors.PaperOnSecondaryContainer
    )
    QuranReadingTheme.NIGHT -> base.copy(
        onSurface = QuranReadingColors.NightOnSurface,
        onSurfaceVariant = QuranReadingColors.NightOnSurfaceVariant,
        outlineVariant = QuranReadingColors.NightOutlineVariant,
        surfaceContainerLowest = QuranReadingColors.NightSurface,
        surfaceContainerLow = QuranReadingColors.NightSurface,
        surfaceContainer = QuranReadingColors.NightContainer,
        surfaceContainerHigh = QuranReadingColors.NightContainerHigh,
        surfaceContainerHighest = QuranReadingColors.NightContainerHigh,
        primary = QuranReadingColors.NightPrimary,
        onPrimary = QuranReadingColors.NightOnPrimary,
        secondaryContainer = QuranReadingColors.NightSecondaryContainer,
        onSecondaryContainer = QuranReadingColors.NightOnSecondaryContainer
    )
}
