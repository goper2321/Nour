package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Material 3 Expressive Monochrome Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = MonoPureWhite,
    onPrimary = MonoPitchBlack,
    primaryContainer = MonoGraphite,
    onPrimaryContainer = MonoPureWhite,
    secondary = MonoSilver,
    onSecondary = MonoPitchBlack,
    secondaryContainer = MonoCharcoal,
    onSecondaryContainer = MonoPlatinum,
    tertiary = MonoPlatinum,
    onTertiary = MonoPitchBlack,
    tertiaryContainer = MonoSlateDark,
    onTertiaryContainer = MonoPureWhite,
    background = MonoDarkBackground,
    onBackground = MonoDarkTextPrimary,
    surface = MonoDarkSurface,
    onSurface = MonoDarkTextPrimary,
    surfaceVariant = MonoDarkSurfaceVariant,
    onSurfaceVariant = MonoDarkTextSecondary,
    surfaceContainer = MonoDarkSurfaceContainer,
    surfaceContainerHigh = MonoDarkSurfaceContainerHigh,
    outline = MonoDarkOutline,
    outlineVariant = MonoDarkOutlineVariant,
    scrim = MonoPitchBlack
)

// Material 3 Expressive Monochrome Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = MonoPitchBlack,
    onPrimary = MonoPureWhite,
    primaryContainer = MonoPearl,
    onPrimaryContainer = MonoPitchBlack,
    secondary = MonoSlateMedium,
    onSecondary = MonoPureWhite,
    secondaryContainer = MonoAlabaster,
    onSecondaryContainer = MonoObsidian,
    tertiary = MonoSlateLight,
    onTertiary = MonoPureWhite,
    tertiaryContainer = MonoPearl,
    onTertiaryContainer = MonoCharcoal,
    background = MonoLightBackground,
    onBackground = MonoLightTextPrimary,
    surface = MonoLightSurface,
    onSurface = MonoLightTextPrimary,
    surfaceVariant = MonoLightSurfaceVariant,
    onSurfaceVariant = MonoLightTextSecondary,
    surfaceContainer = MonoLightSurfaceContainer,
    surfaceContainerHigh = MonoLightSurfaceContainerHigh,
    outline = MonoLightOutline,
    outlineVariant = MonoLightOutlineVariant,
    scrim = MonoPitchBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prioritize our handcrafted Material 3 Expressive Monochrome theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}

