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

// Material 3 Expressive Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = M3DarkPrimary,
    onPrimary = M3DarkOnPrimary,
    primaryContainer = M3DarkPrimaryContainer,
    onPrimaryContainer = M3DarkOnPrimaryContainer,
    secondary = M3DarkSecondary,
    onSecondary = M3DarkOnSecondary,
    secondaryContainer = M3DarkSecondaryContainer,
    onSecondaryContainer = M3DarkOnSecondaryContainer,
    tertiary = M3DarkTertiary,
    onTertiary = M3DarkOnTertiary,
    tertiaryContainer = M3DarkTertiaryContainer,
    onTertiaryContainer = M3DarkOnTertiaryContainer,
    background = M3DarkBackground,
    onBackground = M3DarkOnBackground,
    surface = M3DarkSurface,
    onSurface = M3DarkOnSurface,
    surfaceVariant = M3DarkSurfaceVariant,
    onSurfaceVariant = M3DarkOnSurfaceVariant,
    surfaceContainerLowest = M3DarkSurfaceContainerLowest,
    surfaceContainerLow = M3DarkSurfaceContainerLow,
    surfaceContainer = M3DarkSurfaceContainer,
    surfaceContainerHigh = M3DarkSurfaceContainerHigh,
    surfaceContainerHighest = M3DarkSurfaceContainerHighest,
    outline = M3DarkOutline,
    outlineVariant = M3DarkOutlineVariant,
    error = M3DarkError,
    onError = M3DarkOnError,
    errorContainer = M3DarkErrorContainer,
    onErrorContainer = M3DarkOnErrorContainer,
    scrim = MonoPitchBlack
)

// Material 3 Expressive Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = M3LightPrimary,
    onPrimary = M3LightOnPrimary,
    primaryContainer = M3LightPrimaryContainer,
    onPrimaryContainer = M3LightOnPrimaryContainer,
    secondary = M3LightSecondary,
    onSecondary = M3LightOnSecondary,
    secondaryContainer = M3LightSecondaryContainer,
    onSecondaryContainer = M3LightOnSecondaryContainer,
    tertiary = M3LightTertiary,
    onTertiary = M3LightOnTertiary,
    tertiaryContainer = M3LightTertiaryContainer,
    onTertiaryContainer = M3LightOnTertiaryContainer,
    background = M3LightBackground,
    onBackground = M3LightOnBackground,
    surface = M3LightSurface,
    onSurface = M3LightOnSurface,
    surfaceVariant = M3LightSurfaceVariant,
    onSurfaceVariant = M3LightOnSurfaceVariant,
    surfaceContainerLowest = M3LightSurfaceContainerLowest,
    surfaceContainerLow = M3LightSurfaceContainerLow,
    surfaceContainer = M3LightSurfaceContainer,
    surfaceContainerHigh = M3LightSurfaceContainerHigh,
    surfaceContainerHighest = M3LightSurfaceContainerHighest,
    outline = M3LightOutline,
    outlineVariant = M3LightOutlineVariant,
    error = M3LightError,
    onError = M3LightOnError,
    errorContainer = M3LightErrorContainer,
    onErrorContainer = M3LightOnErrorContainer,
    scrim = MonoPitchBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Reference palette by default; wallpaper colors are opt-in.
    seedColorArgb: Int? = null, // Custom seed wins over wallpaper when set.
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        seedColorArgb != null -> SeedColorSchemes.schemeFor(seedColorArgb, darkTheme)
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

