package com.example

import com.example.ui.theme.SeedColorSchemes
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the seed -> M3 tonal palette path (pure math, must never throw). */
class SeedSchemeTest {
    private val seeds = listOf(
        SeedColorSchemes.DEFAULT_SEED_ARGB,
        0xFFFF0000.toInt(),
        0xFF00FF00.toInt(),
        0xFF0000FF.toInt(),
        0xFFFFFFFF.toInt(),
        0xFF000000.toInt(),
        0xFF0B57D0.toInt(),
        0xFF006D5B.toInt()
    ) + SeedColorSchemes.PRESET_SEEDS_ARGB

    @Test
    fun seedSchemesGenerateForLightAndDark() {
        seeds.distinct().forEach { seed ->
            listOf(false, true).forEach { dark ->
                val scheme = SeedColorSchemes.schemeFor(seed, dark)
                // Roles resolve to real opaque colors (contrast handled by the engine).
                assertNotEquals(0UL, scheme.primary.value)
                assertNotEquals(0UL, scheme.surface.value)
                assertTrue(scheme.primaryContainer.value != 0UL)
            }
        }
    }
}
