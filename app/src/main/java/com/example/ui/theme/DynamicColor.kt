package com.example.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.TonalPalette
import com.google.android.material.color.utilities.Variant

/**
 * Seed-driven Material 3 color system.
 *
 * One ARGB seed color is expanded into coordinated tonal
 * palettes (the neutral palettes keep a subtle tint of the seed instead of
 * going fully gray), then mapped onto every semantic M3 color role below.
 * Contrast for each on-* color follows the M3 spec inside the library.
 *
 * ## Why `RestrictedApi` is suppressed here
 *
 * `com.google.android.material.color.utilities.*` is annotated
 * `@RestrictTo(LIBRARY_GROUP)`, so lint flags it for app code. We use it anyway,
 * deliberately, for one reason: it is the only implementation of the M3 tonal
 * palette + contrast algorithm that stays spec-correct. Re-deriving it by hand
 * would risk shipping on-* colors that fail contrast, which is a worse outcome
 * than depending on a library-internal API.
 *
 * The risk is bounded: Material is pinned in `gradle/libs.versions.toml`, so an
 * upgrade is always a deliberate act, and if the API moves the build fails
 * loudly here instead of silently producing wrong colors at runtime. If the
 * library ever ships these as public API, delete this suppression.
 */
@SuppressLint("RestrictedApi")
object SeedColorSchemes {
    /** Brand seed: Apple blue, matching the iOS 27 Design Kit. */
    const val DEFAULT_SEED_ARGB = 0xFF0A7AFF.toInt()
    /** Curated preset seeds shown in the theme picker. */
    val PRESET_SEEDS_ARGB: List<Int> = listOf(
        0xFF0A7AFF.toInt(), // Nour default (Apple blue)
        0xFF0B57D0.toInt(), // Blue
        0xFF006D5B.toInt(), // Emerald
        0xFF146C2E.toInt(), // Green
        0xFF7E5700.toInt(), // Amber brown
        0xFF8B4C00.toInt(), // Orange
        0xFF9A2066.toInt(), // Magenta
        0xFF006874.toInt(), // Teal
    )

    // NUR-004: [Color.kt] no longer carries hand-authored literals — the default
    // palette *is* the seed-generated one for the default `DEFAULT_SEED_ARGB`,
    // so the two files cannot drift apart again (the "grouped page tone" and
    // dark `surfaceVariant` rows were the last contradictory literals).
    // NUR-008: `contrastLevel` is the M3 Expressive user-contrast knob
    // (0.0 standard, 1.0 high contrast) — the high-contrast theme is the same
    // engine run at contrast level 1.0, not a separate hand-maintained palette.
    // The pre-2025 light-branch remappings (background = surfaceDim, container
    // ladder shifted a notch) came from the superseded M3E pass; every role is
    // now the raw generated value in both modes, so a seed user and a
    // default-palette user see the same ladder for the same seed.
    fun schemeFor(seedArgb: Int, darkTheme: Boolean, contrastLevel: Double = 0.0): ColorScheme {
        // iOS 27 palette spec: Variant.CONTENT accents (vivid) with Apple-neutral
        // surfaces (neutral chroma 4, neutral-variant chroma 8).
        val hct = Hct.fromInt(seedArgb)
        val scheme: DynamicScheme = DynamicScheme(
            hct,
            Variant.CONTENT,
            darkTheme,
            contrastLevel,
            TonalPalette.fromHueAndChroma(hct.hue, kotlin.math.min(80.0, hct.chroma.toDouble())),
            TonalPalette.fromHueAndChroma(hct.hue, 24.0),
            TonalPalette.fromHueAndChroma(hct.hue + 60.0, 48.0),
            TonalPalette.fromHueAndChroma(hct.hue, 4.0),
            TonalPalette.fromHueAndChroma(hct.hue, 8.0)
        )
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

        // Identical raw mapping in both modes; only the constructor differs.
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
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
                surfaceDim = surfaceDim, surfaceBright = surfaceBright, surfaceTint = surfaceTint,
                outline = outline, outlineVariant = outlineVariant,
                inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
                inversePrimary = inversePrimary, scrim = scrim
            )
        }
    }

    /** The shipped default palette (Color.kt tokens delegate to these roles). */
    fun defaultScheme(darkTheme: Boolean, highContrast: Boolean = false): ColorScheme =
        schemeFor(DEFAULT_SEED_ARGB, darkTheme, if (highContrast) 1.0 else 0.0)
}
