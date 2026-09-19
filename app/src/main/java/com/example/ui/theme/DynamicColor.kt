package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeTonalSpot

/**
 * Seed-driven Material 3 color system.
 *
 * One ARGB seed color is expanded by [SchemeTonalSpot] into coordinated tonal
 * palettes (the neutral palettes keep a subtle tint of the seed instead of
 * going fully gray), then mapped onto every semantic M3 color role below.
 * Contrast for each on-* color follows the M3 spec inside the library.
 */
object SeedColorSchemes {
    /** Brand seed: reproduces the closest stock look when no custom seed is set. */
    const val DEFAULT_SEED_ARGB = 0xFF4A4459.toInt()

    /** Curated preset seeds shown in the theme picker. */
    val PRESET_SEEDS_ARGB: List<Int> = listOf(
        0xFF4A4459.toInt(), // Nour default (lavender)
        0xFF0B57D0.toInt(), // Blue
        0xFF006D5B.toInt(), // Emerald
        0xFF146C2E.toInt(), // Green
        0xFF7E5700.toInt(), // Amber brown
        0xFF8B4C00.toInt(), // Orange
        0xFF9A2066.toInt(), // Magenta
        0xFF006874.toInt(), // Teal
    )

    fun schemeFor(seedArgb: Int, darkTheme: Boolean): ColorScheme {
        val scheme: DynamicScheme = SchemeTonalSpot(Hct.fromInt(seedArgb), darkTheme, 0.0)
        val c = MaterialDynamicColors()
        fun role(color: DynamicColor): Color = Color(color.getArgb(scheme))

        val primary = role(c.primary())
        val onPrimary = role(c.onPrimary())
        val primaryContainer = role(c.primaryContainer())
        val onPrimaryContainer = role(c.onPrimaryContainer())
        val secondary = role(c.secondary())
        val onSecondary = role(c.onSecondary())
        val secondaryContainer = role(c.secondaryContainer())
        val onSecondaryContainer = role(c.onSecondaryContainer())
        val tertiary = role(c.tertiary())
        val onTertiary = role(c.onTertiary())
        val tertiaryContainer = role(c.tertiaryContainer())
        val onTertiaryContainer = role(c.onTertiaryContainer())
        val error = role(c.error())
        val onError = role(c.onError())
        val errorContainer = role(c.errorContainer())
        val onErrorContainer = role(c.onErrorContainer())
        val background = role(c.background())
        val onBackground = role(c.onBackground())
        val surface = role(c.surface())
        val onSurface = role(c.onSurface())
        val surfaceVariant = role(c.surfaceVariant())
        val onSurfaceVariant = role(c.onSurfaceVariant())
        val surfaceContainerLowest = role(c.surfaceContainerLowest())
        val surfaceContainerLow = role(c.surfaceContainerLow())
        val surfaceContainer = role(c.surfaceContainer())
        val surfaceContainerHigh = role(c.surfaceContainerHigh())
        val surfaceContainerHighest = role(c.surfaceContainerHighest())
        val surfaceDim = role(c.surfaceDim())
        val surfaceBright = role(c.surfaceBright())
        val surfaceTint = role(c.surfaceTint())
        val outline = role(c.outline())
        val outlineVariant = role(c.outlineVariant())
        val inverseSurface = role(c.inverseSurface())
        val inverseOnSurface = role(c.inverseOnSurface())
        val inversePrimary = role(c.inversePrimary())
        val scrim = role(c.scrim())

        return if (darkTheme) {
            darkColorScheme(
                primary = primary, onPrimary = onPrimary,
                primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
                secondary = secondary, onSecondary = onSecondary,
                secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary, onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
                error = error, onError = onError,
                errorContainer = errorContainer, onErrorContainer = onErrorContainer,
                background = background, onBackground = onBackground,
                surface = surface, onSurface = onSurface,
                surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
                surfaceDim = surfaceDim, surfaceBright = surfaceBright, surfaceTint = surfaceTint,
                outline = outline, outlineVariant = outlineVariant,
                inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
                inversePrimary = inversePrimary, scrim = scrim
            )
        } else {
            lightColorScheme(
                primary = primary, onPrimary = onPrimary,
                primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
                secondary = secondary, onSecondary = onSecondary,
                secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary, onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
                error = error, onError = onError,
                errorContainer = errorContainer, onErrorContainer = onErrorContainer,
                background = background, onBackground = onBackground,
                surface = surface, onSurface = onSurface,
                surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainer = surfaceContainer,
                surfaceContainerHighest = surfaceContainerHighest,
                surfaceDim = surfaceDim, surfaceBright = surfaceBright, surfaceTint = surfaceTint,
                outline = outline, outlineVariant = outlineVariant,
                inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
                inversePrimary = inversePrimary, scrim = scrim
            )
        }
    }
}
