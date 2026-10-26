package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Apple HIG / iOS 27 Design Kit palette. NUR-004 unification: the hand-authored
// literals are gone; every M3 token below now *delegates* to the seed-generated
// scheme for the brand seed #0A7AFF (Variant.CONTENT, Apple-neutral surfaces,
// chroma 4/8) in DynamicColor.kt, so Color.kt and DynamicColor.kt can no longer
// disagree on any shared role ("grouped page tone" and the dark surfaceVariant
// row were the last contradictions). Variable names and the public API are
// unchanged for feature code.
private val GeneratedLight = SeedColorSchemes.defaultScheme(darkTheme = false)
private val GeneratedDark = SeedColorSchemes.defaultScheme(darkTheme = true)

// Light · standard scheme role values (generated, single source of truth).
val M3LightPrimary = GeneratedLight.primary
val M3LightOnPrimary = GeneratedLight.onPrimary
val M3LightPrimaryContainer = GeneratedLight.primaryContainer
val M3LightOnPrimaryContainer = GeneratedLight.onPrimaryContainer
val M3DarkPrimary = GeneratedDark.primary
val M3DarkOnPrimary = GeneratedDark.onPrimary
val M3DarkPrimaryContainer = GeneratedDark.primaryContainer
val M3DarkOnPrimaryContainer = GeneratedDark.onPrimaryContainer

val M3LightSecondary = GeneratedLight.secondary
val M3LightOnSecondary = GeneratedLight.onSecondary
val M3LightSecondaryContainer = GeneratedLight.secondaryContainer
val M3LightOnSecondaryContainer = GeneratedLight.onSecondaryContainer
val M3DarkSecondary = GeneratedDark.secondary
val M3DarkOnSecondary = GeneratedDark.onSecondary
val M3DarkSecondaryContainer = GeneratedDark.secondaryContainer
val M3DarkOnSecondaryContainer = GeneratedDark.onSecondaryContainer

val M3LightTertiary = GeneratedLight.tertiary
val M3LightOnTertiary = GeneratedLight.onTertiary
val M3LightTertiaryContainer = GeneratedLight.tertiaryContainer
val M3LightOnTertiaryContainer = GeneratedLight.onTertiaryContainer
val M3DarkTertiary = GeneratedDark.tertiary
val M3DarkOnTertiary = GeneratedDark.onTertiary
val M3DarkTertiaryContainer = GeneratedDark.tertiaryContainer
val M3DarkOnTertiaryContainer = GeneratedDark.onTertiaryContainer

val M3LightBackground = GeneratedLight.background
val M3LightOnBackground = GeneratedLight.onBackground
val M3LightSurface = GeneratedLight.surface
val M3LightOnSurface = GeneratedLight.onSurface
val M3LightSurfaceVariant = GeneratedLight.surfaceVariant
val M3LightOnSurfaceVariant = GeneratedLight.onSurfaceVariant
val M3LightSurfaceContainerLowest = GeneratedLight.surfaceContainerLowest
val M3LightSurfaceContainerLow = GeneratedLight.surfaceContainerLow
val M3LightSurfaceContainer = GeneratedLight.surfaceContainer
val M3LightSurfaceContainerHigh = GeneratedLight.surfaceContainerHigh
val M3LightSurfaceContainerHighest = GeneratedLight.surfaceContainerHighest
val M3LightOutline = GeneratedLight.outline
val M3LightOutlineVariant = GeneratedLight.outlineVariant

val M3DarkBackground = GeneratedDark.background
val M3DarkOnBackground = GeneratedDark.onBackground
val M3DarkSurface = GeneratedDark.surface
val M3DarkOnSurface = GeneratedDark.onSurface
val M3DarkSurfaceVariant = GeneratedDark.surfaceVariant
val M3DarkOnSurfaceVariant = GeneratedDark.onSurfaceVariant
val M3DarkSurfaceContainerLowest = GeneratedDark.surfaceContainerLowest
val M3DarkSurfaceContainerLow = GeneratedDark.surfaceContainerLow
val M3DarkSurfaceContainer = GeneratedDark.surfaceContainer
val M3DarkSurfaceContainerHigh = GeneratedDark.surfaceContainerHigh
val M3DarkSurfaceContainerHighest = GeneratedDark.surfaceContainerHighest
val M3DarkOutline = GeneratedDark.outline
val M3DarkOutlineVariant = GeneratedDark.outlineVariant

val M3LightError = GeneratedLight.error
val M3LightOnError = GeneratedLight.onError
val M3LightErrorContainer = GeneratedLight.errorContainer
val M3LightOnErrorContainer = GeneratedLight.onErrorContainer
val M3DarkError = GeneratedDark.error
val M3DarkOnError = GeneratedDark.onError
val M3DarkErrorContainer = GeneratedDark.errorContainer
val M3DarkOnErrorContainer = GeneratedDark.onErrorContainer

// Legacy aliases retained for existing feature code.
val MonoPitchBlack = Color.Black
val MonoObsidian = Color(0xFF09090B)
val MonoCharcoal = Color(0xFF141416)
val MonoGraphite = Color(0xFF1F1F23)
val MonoSlateDark = Color(0xFF2E2E34)
val MonoSlateMedium = Color(0xFF52525B)
val MonoSlateLight = Color(0xFF71717A)
val MonoSilver = Color(0xFFA1A1AA)
val MonoPlatinum = Color(0xFFD4D4D8)
val MonoPearl = Color(0xFFE4E4E7)
val MonoAlabaster = Color(0xFFF4F4F6)
val MonoPureWhite = Color.White
val EmeraldPrimary = M3LightPrimary
val EmeraldDark = M3LightPrimaryContainer
val EmeraldLight = M3DarkPrimary
val EmeraldContainer = M3LightPrimaryContainer
val OnEmeraldContainer = M3LightOnPrimaryContainer
val GoldAccent = M3LightTertiary
val GoldLight = M3DarkTertiary
val GoldContainer = M3LightTertiaryContainer
val OnGoldContainer = M3LightOnTertiaryContainer
val SandBackground = M3LightBackground
val SandSurface = M3LightSurface
val SandSurfaceVariant = M3LightSurfaceVariant
val SandText = M3LightOnSurface
val SandTextMuted = M3LightOnSurfaceVariant
val MidnightBackground = M3DarkBackground
val MidnightSurface = M3DarkSurface
val MidnightSurfaceVariant = M3DarkSurfaceVariant
val MidnightText = M3DarkOnSurface
val MidnightTextMuted = M3DarkOnSurfaceVariant
val EmeraldNightPrimary = M3DarkPrimary
val GoldNightAccent = M3DarkTertiary