# Dynamic color wiring — audit trace (NUR-005)

Written 2026-10-23 for the Nour Polish Checklist item NUR-005 ("audit the dynamic
color wiring end to end"). This is a **trace + verdict record**, not a change log:
the audit found the wiring correct as designed and flipped nothing, so behavior
is unchanged by this audit. The wording below reports what was read, where, and
the verdict for each hop.

## Contract being audited

- Default = **reference palette** (seed generated, brand-controlled). Wallpaper
  (Monet / Material You) colors are **opt-in**, behind a Settings switch, and
  only on Android 12+ (API 31).
- A user-chosen seed color always wins over wallpaper colors.
- The switch must be disabled (not just hidden) below API 31, with honest copy.

## Verified chain (file:line at audit time)

1. **Entry point** — `MainActivity.kt:143`
   `var dynamicColors by remember { mutableStateOf(appearance.dynamicColors) }`
   — persisted state is the single source at boot. [Worked]
2. **Theme call** — `MainActivity.kt:149-155`
   `MyApplicationTheme(..., dynamicColor = dynamicColors, ...)`. Theme default in
   `Theme.kt:92` is `dynamicColor: Boolean = false` ("Reference palette by
   default; wallpaper colors are opt-in"). [Worked]
3. **Theme branch order** — `Theme.kt:96-100`
   First `seedColorArgb != null -> SeedColorSchemes.schemeFor(...)` (custom seed
   wins, matches the comment on `Theme.kt:93`), only then
   `dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
   dynamicDarkColorScheme(context) / dynamicLightColorScheme(context)`.
   On older SDKs the condition degrades to the reference palette instead of
   crashing. [Worked]
4. **Toggle UI** — `SettingsScreen.kt:514-519`
   Switch `checked = dynamicColors, onCheckedChange = onDynamicColorsChange`,
   `enabled = android.os.Build.VERSION.SDK_INT >= 31` — disabled (not hidden)
   below 31. Above it, `SettingsScreen.kt:511` shows `dynamicColorsDesc` on
   31+ and `dynamicColorsRequires` below. [Worked]
5. **Persistence** — `features/settings/NourPreferences.kt:25-26`
   `dynamic_colors` boolean preference, default `false`. [Worked]
6. **Write-back path** — `MainActivity.kt:158-162`
   `NourMainApp(themeMode, dynamicColors, ..., onDynamicColorsChange = { enabled
   -> dynamicColors = enabled; appearance.dynamicColors = enabled })` — Compose
   state and store updated together in one place. [Worked]
7. **Seed override guard** — `MainActivity.kt:172-174`
   Selecting a custom seed sets `dynamicColors = false` and
   `appearance.dynamicColors = false` when a valid seed is chosen, so the two
   write paths are mutually exclusive and the "seed wins" branch in Theme.kt is
   never silently in conflict. [Worked]

## Verdict

**[Worked]** — the reference-default → opt-in wallpaper → seed-wins pipeline is
wired as the design contract says, all writes are guarded, and nothing needed
fixing. Confirmed by code trace only (no code was changed by this item).

## Known limits / where this audit cannot see

- **Visual end state on a device is unverified.** All Robolectric render tests
  (`ScreenRenderTest`, `ScreenCaptureMatrixTest`) run with wallpaper colors off
  (`dynamicColors` default `false` and screens rendered without the MainActivity
  wiring), so the dynamic branch of `Theme.kt:98-101` never renders in tests.
  On a physical Android 12+ device with the switch on, the actual palettes and
  their accessibility contrast were **not verified** — treat as pending device
  acceptance like the rest of the visual checks.
- **Legacy-prefs edge:** if a state file somehow contained both a custom seed
  and `dynamic_colors = true`, `Theme.kt:97` picks the seed and the switch keeps
  showing on — silently ignored, no crash. Every write site clears the other
  value, but a hand-edited or very old store could land in that state.
- **SDK gating honesty relies on `dynamicColorsRequires`** (`AppStrings.kt`) —
  copy translations were not re-verified against every locale in this audit.
