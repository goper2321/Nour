package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.features.quran.ui.QuranReadingColors
import com.example.features.quran.ui.applyTo
import com.example.features.settings.QuranReadingTheme
import com.example.ui.theme.SeedColorSchemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * C3: Paper/Night are opt-in reader card palettes, so they must (a) leave
 * SYSTEM untouched, (b) be independent of the app's light/dark mode, (c) leave
 * the page background and the gaps between cards to the host app theme, and
 * (d) keep every text/card pair the reader actually paints at WCAG AA,
 * including the action colours reused for the reference pill and highlight.
 */
class QuranReadingColorsTest {

    private fun contrast(a: Color, b: Color): Double {
        fun channel(v: Float): Double =
            if (v <= 0.04045f) v / 12.92 else Math.pow(((v + 0.055) / 1.055).toDouble(), 2.4)
        fun luminance(c: Color): Double =
            0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
        val l1 = maxOf(luminance(a), luminance(b))
        val l2 = minOf(luminance(a), luminance(b))
        return (l1 + 0.05) / (l2 + 0.05)
    }

    private fun bothAppModes() = listOf(
        SeedColorSchemes.defaultScheme(false),
        SeedColorSchemes.defaultScheme(true)
    )

    @Test
    fun systemIsTheAppSchemeUntouched() {
        bothAppModes().forEach { base ->
            assertSame(base, QuranReadingTheme.SYSTEM.applyTo(base))
        }
    }

    /**
     * Owner-visible contract: Paper/Night tint the cards only. The page and
     * the gaps between cards must keep the host app background, so a reader in
     * Paper on a dark app still shows dark gaps and paper cards (and vice
     * versa) rather than a painted full-page surface.
     */
    @Test
    fun paperAndNightLeaveThePageBackgroundToTheApp() {
        bothAppModes().forEach { base ->
            listOf(QuranReadingTheme.PAPER, QuranReadingTheme.NIGHT).forEach { theme ->
                val scheme = theme.applyTo(base)
                assertEquals("$theme background", base.background, scheme.background)
                assertEquals("$theme surface", base.surface, scheme.surface)
                assertEquals("$theme surfaceVariant", base.surfaceVariant, scheme.surfaceVariant)
                assertEquals("$theme onBackground", base.onBackground, scheme.onBackground)
            }
        }
    }

    @Test
    fun paperAndNightCardsAreOpaqueAndIdenticalInLightAndDarkAppMode() {
        listOf(QuranReadingTheme.PAPER, QuranReadingTheme.NIGHT).forEach { theme ->
            val light = theme.applyTo(SeedColorSchemes.defaultScheme(false))
            val dark = theme.applyTo(SeedColorSchemes.defaultScheme(true))
            assertEquals(light.surfaceContainerLow, dark.surfaceContainerLow)
            assertEquals(light.surfaceContainer, dark.surfaceContainer)
            assertEquals(light.onSurface, dark.onSurface)
            assertEquals(light.primary, dark.primary)
            assertEquals(1f, light.surfaceContainerLow.alpha)
            assertEquals(1f, light.surfaceContainer.alpha)
            assertEquals(1f, light.secondaryContainer.alpha)
        }
    }

    @Test
    fun everyTextSurfacePairMeetsWcagAa() {
        listOf(QuranReadingTheme.PAPER, QuranReadingTheme.NIGHT).forEach { theme ->
            bothAppModes().forEach { base ->
                val scheme = theme.applyTo(base)
                // Only card/container roles are themed; on-page text keeps the
                // host ink because the page background stays the host's.
                val cards = listOf(
                    scheme.surfaceContainerLow,
                    scheme.surfaceContainer,
                    scheme.surfaceContainerHigh
                )
                cards.forEach { card ->
                    assertTrue(
                        "$theme onSurface on $card = ${contrast(scheme.onSurface, card)}",
                        contrast(scheme.onSurface, card) >= 4.5
                    )
                    assertTrue(
                        "$theme onSurfaceVariant on $card = ${contrast(scheme.onSurfaceVariant, card)}",
                        contrast(scheme.onSurfaceVariant, card) >= 4.5
                    )
                }
                // Action roles: primary paints icons, the last-read label, the
                // surah title and the ayah marker directly on cards;
                // secondaryContainer paints the verse-reference pill and the
                // go-to-verse highlight behind scripture.
                assertTrue(
                    "$theme primary on card = ${contrast(scheme.primary, scheme.surfaceContainerLow)}",
                    contrast(scheme.primary, scheme.surfaceContainerLow) >= 4.5
                )
                assertTrue(
                    "$theme onPrimary on primary = ${contrast(scheme.onPrimary, scheme.primary)}",
                    contrast(scheme.onPrimary, scheme.primary) >= 4.5
                )
                assertTrue(
                    "$theme onSecondaryContainer = ${contrast(scheme.onSecondaryContainer, scheme.secondaryContainer)}",
                    contrast(scheme.onSecondaryContainer, scheme.secondaryContainer) >= 4.5
                )
            }
        }
    }

    @Test
    fun paperCardsAreLightAndNightCardsAreDark() {
        // Guards against a token swap: Paper must read as daylight, Night as
        // night, under both app modes.
        val paper = QuranReadingTheme.PAPER.applyTo(SeedColorSchemes.defaultScheme(true))
        val night = QuranReadingTheme.NIGHT.applyTo(SeedColorSchemes.defaultScheme(false))
        assertTrue(paper.surfaceContainerLow.luminance() > 0.7f)
        assertTrue(night.surfaceContainerLow.luminance() < 0.05f)
        assertEquals(QuranReadingColors.PaperSurface, paper.surfaceContainerLow)
        assertEquals(QuranReadingColors.NightSurface, night.surfaceContainerLow)
        // And neither repaints the page behind the cards.
        assertEquals(
            SeedColorSchemes.defaultScheme(true).background,
            paper.background
        )
        assertEquals(
            SeedColorSchemes.defaultScheme(false).background,
            night.background
        )
    }
}
