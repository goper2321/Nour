package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Reference palette by default; wallpaper colors are opt-in.
    seedColorArgb: Int? = null, // Custom seed wins over wallpaper when set.
    // NUR-008: M3 Expressive user-contrast level (0.0 standard / 1.0 high).
    // Runs through the same seed engine as the default palette, so it composes
    // with light, dark and custom seeds. Wallpaper-based dynamic color has no
    // contrast knob on the platform side; when high contrast is on, the
    // design-system engine (with its stronger outlines/solid containers) is
    // chosen over the wallpaper scheme instead — the toggle never silently
    // does nothing, and it is never translucent.
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        seedColorArgb != null ->
            SeedColorSchemes.schemeFor(
                seedColorArgb, darkTheme,
                if (highContrast) 1.0 else 0.0
            )
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !highContrast -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // The default (static) palette *is* the seed-generated one; see
        // NUR-004 in DynamicColor.kt/Color.kt. High contrast is that same
        // engine at M3 contrast level 1.0.
        else -> SeedColorSchemes.defaultScheme(darkTheme, highContrast)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Material 3 Expressive theme: the native `MaterialExpressiveTheme` with an
    // explicit `MotionScheme`. Colors, typography and shapes still come from the
    // app's existing token layer, so the visual identity is unchanged; only the
    // theming API and the component motion scheme are now the native M3E ones.
    // The system "Remove animations" setting selects the calmer `standard`
    // scheme instead of the bouncier `expressive` one.
    val reduceMotion = NourReduceMotion.remember()
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = if (reduceMotion) MotionScheme.standard() else MotionScheme.expressive(),
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}

