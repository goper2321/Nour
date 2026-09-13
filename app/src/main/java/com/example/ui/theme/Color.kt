package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Material 3 Expressive Monochrome Palette
// Distinct tonal stops from pure pitch black to crystalline white
// =========================================================================

val MonoPitchBlack = Color(0xFF000000)
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
val MonoPureWhite = Color(0xFFFFFFFF)

// Light Theme Specific Monochrome Tokens
val MonoLightBackground = Color(0xFFFAFAFA)
val MonoLightSurface = Color(0xFFFFFFFF)
val MonoLightSurfaceVariant = Color(0xFFF0F0F3)
val MonoLightSurfaceContainer = Color(0xFFF4F4F6)
val MonoLightSurfaceContainerHigh = Color(0xFFEBEBEF)
val MonoLightTextPrimary = Color(0xFF09090B)
val MonoLightTextSecondary = Color(0xFF52525B)
val MonoLightTextTertiary = Color(0xFF71717A)
val MonoLightOutline = Color(0xFFD4D4D8)
val MonoLightOutlineVariant = Color(0xFFE4E4E7)

// Dark Theme Specific Monochrome Tokens
val MonoDarkBackground = Color(0xFF09090B)
val MonoDarkSurface = Color(0xFF111114)
val MonoDarkSurfaceVariant = Color(0xFF1E1E22)
val MonoDarkSurfaceContainer = Color(0xFF18181C)
val MonoDarkSurfaceContainerHigh = Color(0xFF24242A)
val MonoDarkTextPrimary = Color(0xFFFAFAFA)
val MonoDarkTextSecondary = Color(0xFFA1A1AA)
val MonoDarkTextTertiary = Color(0xFF71717A)
val MonoDarkOutline = Color(0xFF3F3F46)
val MonoDarkOutlineVariant = Color(0xFF27272A)

// Compatibility Aliases (remapped to pristine monochrome tokens)
val EmeraldPrimary = MonoPitchBlack
val EmeraldDark = MonoCharcoal
val EmeraldLight = MonoSilver
val EmeraldContainer = MonoPearl
val OnEmeraldContainer = MonoObsidian

val GoldAccent = MonoSlateMedium
val GoldLight = MonoSilver
val GoldContainer = MonoAlabaster
val OnGoldContainer = MonoObsidian

val SandBackground = MonoLightBackground
val SandSurface = MonoLightSurface
val SandSurfaceVariant = MonoLightSurfaceVariant
val SandText = MonoLightTextPrimary
val SandTextMuted = MonoLightTextSecondary

val MidnightBackground = MonoDarkBackground
val MidnightSurface = MonoDarkSurface
val MidnightSurfaceVariant = MonoDarkSurfaceVariant
val MidnightText = MonoDarkTextPrimary
val MidnightTextMuted = MonoDarkTextSecondary
val EmeraldNightPrimary = MonoPureWhite
val GoldNightAccent = MonoSilver

