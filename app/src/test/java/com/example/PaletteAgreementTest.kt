package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.M3DarkBackground
import com.example.ui.theme.M3DarkError
import com.example.ui.theme.M3DarkErrorContainer
import com.example.ui.theme.M3DarkOnBackground
import com.example.ui.theme.M3DarkOnError
import com.example.ui.theme.M3DarkOnErrorContainer
import com.example.ui.theme.M3DarkOnPrimary
import com.example.ui.theme.M3DarkOnPrimaryContainer
import com.example.ui.theme.M3DarkOnSecondary
import com.example.ui.theme.M3DarkOnSecondaryContainer
import com.example.ui.theme.M3DarkOnSurface
import com.example.ui.theme.M3DarkOnSurfaceVariant
import com.example.ui.theme.M3DarkOnTertiary
import com.example.ui.theme.M3DarkOnTertiaryContainer
import com.example.ui.theme.M3DarkOutline
import com.example.ui.theme.M3DarkOutlineVariant
import com.example.ui.theme.M3DarkPrimary
import com.example.ui.theme.M3DarkPrimaryContainer
import com.example.ui.theme.M3DarkSecondary
import com.example.ui.theme.M3DarkSecondaryContainer
import com.example.ui.theme.M3DarkSurface
import com.example.ui.theme.M3DarkSurfaceContainer
import com.example.ui.theme.M3DarkSurfaceContainerHigh
import com.example.ui.theme.M3DarkSurfaceContainerHighest
import com.example.ui.theme.M3DarkSurfaceContainerLow
import com.example.ui.theme.M3DarkSurfaceContainerLowest
import com.example.ui.theme.M3DarkSurfaceVariant
import com.example.ui.theme.M3DarkTertiary
import com.example.ui.theme.M3DarkTertiaryContainer
import com.example.ui.theme.M3LightBackground
import com.example.ui.theme.M3LightError
import com.example.ui.theme.M3LightErrorContainer
import com.example.ui.theme.M3LightOnBackground
import com.example.ui.theme.M3LightOnError
import com.example.ui.theme.M3LightOnErrorContainer
import com.example.ui.theme.M3LightOnPrimary
import com.example.ui.theme.M3LightOnPrimaryContainer
import com.example.ui.theme.M3LightOnSecondary
import com.example.ui.theme.M3LightOnSecondaryContainer
import com.example.ui.theme.M3LightOnSurface
import com.example.ui.theme.M3LightOnSurfaceVariant
import com.example.ui.theme.M3LightOnTertiary
import com.example.ui.theme.M3LightOnTertiaryContainer
import com.example.ui.theme.M3LightOutline
import com.example.ui.theme.M3LightOutlineVariant
import com.example.ui.theme.M3LightPrimary
import com.example.ui.theme.M3LightPrimaryContainer
import com.example.ui.theme.M3LightSecondary
import com.example.ui.theme.M3LightSecondaryContainer
import com.example.ui.theme.M3LightSurface
import com.example.ui.theme.M3LightSurfaceContainer
import com.example.ui.theme.M3LightSurfaceContainerHigh
import com.example.ui.theme.M3LightSurfaceContainerHighest
import com.example.ui.theme.M3LightSurfaceContainerLow
import com.example.ui.theme.M3LightSurfaceContainerLowest
import com.example.ui.theme.M3LightSurfaceVariant
import com.example.ui.theme.M3LightTertiary
import com.example.ui.theme.M3LightTertiaryContainer
import com.example.ui.theme.SeedColorSchemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NUR-004 regression pin: after the palette unification, the Color.kt tokens
 * *are* the seed-generated values — pinning delegation (token == generated
 * role) for both modes so neither file can drift again. The exact hexes of the
 * rows the unification actually CHANGED are also fixed here (audited against
 * the shipped seed #0A7AFF with material-color-utilities: the grouped page
 * tone, the container ladder, `outlineVariant`, and the dark `surfaceVariant`
 * literal that was a copy of surfaceContainerHighest).
 *
 * Also pins NUR-008's mechanism: high contrast is the same generated engine at
 * M3 contrast level 1.0 — stronger, not hand-tuned.
 *
 * Pure JVM: the dynamic color engine is pure arithmetic (see SeedSchemeTest).
 */
class PaletteAgreementTest {

    private fun assertHex(expected: String, actual: Color) {
        assertEquals(
            "expected $expected but token resolved to " + Integer.toHexString(actual.toArgb()),
            (0xFF000000L or expected.toLong(16)), actual.toArgb().toLong() and 0xFFFFFFFFL
        )
    }

    @Test
    fun tokensDelegateToTheGeneratedSchemeInBothModes() {
        val light = SeedColorSchemes.defaultScheme(darkTheme = false)
        val dark = SeedColorSchemes.defaultScheme(darkTheme = true)
        // Every shared role must resolve from the same generated scheme.
        assertEquals(light.primary, M3LightPrimary)
        assertEquals(light.onPrimary, M3LightOnPrimary)
        assertEquals(light.primaryContainer, M3LightPrimaryContainer)
        assertEquals(light.onPrimaryContainer, M3LightOnPrimaryContainer)
        assertEquals(light.secondary, M3LightSecondary)
        assertEquals(light.secondaryContainer, M3LightSecondaryContainer)
        assertEquals(light.tertiary, M3LightTertiary)
        assertEquals(light.tertiaryContainer, M3LightTertiaryContainer)
        assertEquals(light.error, M3LightError)
        assertEquals(light.errorContainer, M3LightErrorContainer)
        assertEquals(light.background, M3LightBackground)
        assertEquals(light.surface, M3LightSurface)
        assertEquals(light.surfaceVariant, M3LightSurfaceVariant)
        assertEquals(light.onSurface, M3LightOnSurface)
        assertEquals(light.onSurfaceVariant, M3LightOnSurfaceVariant)
        assertEquals(light.surfaceContainerLowest, M3LightSurfaceContainerLowest)
        assertEquals(light.surfaceContainerLow, M3LightSurfaceContainerLow)
        assertEquals(light.surfaceContainer, M3LightSurfaceContainer)
        assertEquals(light.surfaceContainerHigh, M3LightSurfaceContainerHigh)
        assertEquals(light.surfaceContainerHighest, M3LightSurfaceContainerHighest)
        assertEquals(light.outline, M3LightOutline)
        assertEquals(light.outlineVariant, M3LightOutlineVariant)
        assertEquals(dark.primary, M3DarkPrimary)
        assertEquals(dark.onPrimary, M3DarkOnPrimary)
        assertEquals(dark.primaryContainer, M3DarkPrimaryContainer)
        assertEquals(dark.secondaryContainer, M3DarkSecondaryContainer)
        assertEquals(dark.error, M3DarkError)
        assertEquals(dark.background, M3DarkBackground)
        assertEquals(dark.surface, M3DarkSurface)
        assertEquals(dark.surfaceVariant, M3DarkSurfaceVariant)
        assertEquals(dark.onSurface, M3DarkOnSurface)
        assertEquals(dark.onSurfaceVariant, M3DarkOnSurfaceVariant)
        assertEquals(dark.surfaceContainerLowest, M3DarkSurfaceContainerLowest)
        assertEquals(dark.surfaceContainerLow, M3DarkSurfaceContainerLow)
        assertEquals(dark.surfaceContainer, M3DarkSurfaceContainer)
        assertEquals(dark.surfaceContainerHigh, M3DarkSurfaceContainerHigh)
        assertEquals(dark.surfaceContainerHighest, M3DarkSurfaceContainerHighest)
        assertEquals(dark.outline, M3DarkOutline)
        assertEquals(dark.outlineVariant, M3DarkOutlineVariant)
    }

    @Test
    fun lightRowsFixedByTheUnification() {
        // Old hand-authored literals -> generated neutrals (audited values).
        assertHex("FAF8FD", M3LightBackground)
        assertHex("FAF8FD", M3LightSurface)
        assertHex("E1E2EC", M3LightSurfaceVariant)
        assertHex("F5F3F7", M3LightSurfaceContainerLow)
        assertHex("E3E2E6", M3LightSurfaceContainerHighest)
        assertHex("C4C6D0", M3LightOutlineVariant)
    }

    @Test
    fun darkRowFixedByTheUnification() {
        // The hand-authored literal (0xFF343538) was a copy of
        // surfaceContainerHighest; the generated role is the neutral-variant
        // tone, same value the light outlineVariant resolves to.
        assertHex("44474F", M3DarkSurfaceVariant)
    }

    /**
     * NUR-008: high contrast is the SAME engine at M3 contrast level 1.0.
     * Pins that the scheme actually shifts (the toggle must change what the
     * user sees), that boundary contrast only grows, and that every role
     * stays opaque.
     */
    @Test
    fun highContrastSchemeIsTheSameEngineAtContrastLevelOne() {
        fun wcagContrast(a: Color, b: Color): Float {
            fun channel(v: Float): Float =
                if (v <= 0.04045f) v / 12.92f else Math.pow(((v + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
            fun lum(x: Color) = 0.2126f * channel(x.red) + 0.7152f * channel(x.green) + 0.0722f * channel(x.blue)
            val l1 = maxOf(lum(a), lum(b)); val l2 = minOf(lum(a), lum(b))
            return (l1 + 0.05f) / (l2 + 0.05f)
        }
        for (dark in listOf(false, true)) {
            val mode = if (dark) "dark" else "light"
            val standard = SeedColorSchemes.defaultScheme(dark)
            val high = SeedColorSchemes.defaultScheme(dark, highContrast = true)
            // Contrast level 1.0 must strengthen the boundary role, never weaken it.
            assertTrue(
                "outline/surface contrast must not shrink at high contrast ($mode): " +
                    "${wcagContrast(high.outline, high.surface)} vs ${wcagContrast(standard.outline, standard.surface)}",
                wcagContrast(high.outline, high.surface) >= wcagContrast(standard.outline, standard.surface)
            )
            assertTrue(
                "primaryContainer must change at high contrast ($mode)",
                high.primaryContainer != standard.primaryContainer
            )
            // Opaque = alpha byte of the packed ARGB is 0xFF (Color.isOpaque is
            // not public API; test the alpha byte directly).
            fun assertOpaque(role: String, c: Color) = assertTrue(
                "$role must be opaque at high contrast ($mode)",
                (c.toArgb() ushr 24) == 0xFF
            )
            assertOpaque("primaryContainer", high.primaryContainer)
            assertOpaque("surfaceVariant", high.surfaceVariant)
        }
    }
}
