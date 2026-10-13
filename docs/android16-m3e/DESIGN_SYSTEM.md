# Nour — Material 3 Expressive Design System

Status: current-state specification (verified against source). This is the single
reference for tokens, components, typography, shape, and motion. Update it when
the system changes.

## 1. Theme strategy

`MyApplicationTheme` (`ui/theme/Theme.kt`) picks a `ColorScheme` in this order:

1. Custom seed color (user-picked) → `SeedColorSchemes.schemeFor(seed, dark)`.
2. Dynamic/wallpaper color (opt-in) → framework `dynamicLight/DarkColorScheme` on API 31+.
3. Built-in Nour palette → `LightColorScheme` / `DarkColorScheme`.

- The built-in palette is a muted lavender identity (light primary `#4A4459`,
  background `#FCF8FD`-family). Dark color is defined explicitly, not derived.
- `SeedColorSchemes` uses `com.google.android.material.color.utilities`
  (`SchemeTonalSpot` + `MaterialDynamicColors`) to expand one seed into every
  semantic role with spec-correct contrast. `RestrictedApi` is suppressed there
  deliberately (see the file header). Material is version-pinned in
  `gradle/libs.versions.toml`, so an upgrade is a deliberate act.
- Dynamic color is **off by default**; the stock palette is the reference look.
- Theme mode (`system`/`light`/`dark`) and dynamic-color opt-in persist via
  `NourPreferences`.
- System-bar icon appearance follows the resolved theme
  (`WindowCompat.getInsetsController`).

Rule: never hardcode a semantic color in a screen. Use `MaterialTheme.colorScheme`.
Artistic palettes (the sky visualizer and the header's sky probe) are the only
sanctioned exceptions and live in `SkyVisualizer.kt` / `SeamlessPrayerHeader.kt`.

## 2. Design tokens

All in `ui/theme/`.

| Token object | Purpose | Notes |
|---|---|---|
| `NourSpace` | spacing steps (`xs`=4 … `xxl`=32) | use instead of raw `.dp` for padding/gaps |
| `NourSize` | component dimensions | `touchTarget`=48, `compactBreakpoint`=600, `expandedBreakpoint`=840, `readingWidth`=720, `contentWidth`=960 |
| `NourAlpha` | opacity scale for accent-derived fills/borders | `CardBorder`=0.40 is the canonical resting card border |
| `NourMotion` | animation durations + easing + reduced-motion gate | see §5 |
| `ExpressiveShapes` | M3 `Shapes` (8/12/16/24/28 dp) | default for components |
| semantic shapes | `PillShape`, `CardExpressiveShape` (28), `CardSubtleShape` (14), `ListRowShape` (22), `DialogExpressiveShape`, `SheetExpressiveShape`, `InputExpressiveShape`, `ButtonExpressiveShape`, `ChipExpressiveShape` | use the semantic shape, not a raw `RoundedCornerShape`, in screens |

Note: `Color.kt` retains legacy `Mono*`/`Emerald*`/`Gold*`/`Sand*`/`Midnight*`
aliases (mostly mapped onto the M3 roles). These are compatibility shims; new
code should use M3 roles, not the aliases.

## 3. Typography (`ui/theme/Type.kt`)

- Custom `Typography` built from a `textStyle(size, lineHeight, weight, letterSpacing, feature)` helper, `FontFamily.Default`.
- Display/headline/title/body/label all defined; `displayLarge/Medium/Small` use tabular figures (`tnum`).
- `TextStyle.withTabularNumbers()` and explicit `fontFeatureSettings = "tnum"`
  are used on timers/counts to stop digit jitter. Keep this on any live number.
- Hierarchy in practice: hero prayer name = `displaySmall` (ExtraBold);
  section titles = `headlineLarge`/`titleLarge`; group labels = `labelLarge`;
  metadata = `bodySmall`/`labelMedium`.

## 4. Shareable components (`ui/components/NourComponents.kt`)

- `NourContentFrame(maxWidth)` — centers content and bounds width; `contentWidth` default, `readingWidth` for Quran.
- `NourScreenHeading(title, subtitle)` — `headlineLarge` + optional `bodyMedium`, marked `heading()`.
- `NourSectionHeader(title)` — `labelLarge`, marked `heading()` for TalkBack.
- `NourPanel(content)` — `surfaceContainerLow` card with the canonical hairline border and flat elevation.
- `NourBadge(text, emphasized)` — pill surface, primary/secondary container.
- `NourEmptyState(title, message)` — centered heading + body for empty/error states.

Rule: consolidate repeated card/panel styling into these primitives instead of
re-deriving `Card(...)` borders per screen. Do not add generic abstractions that
make a simple screen harder to read.

## 5. Motion (`ui/theme/Tokens.kt`, `ui/theme/Motion.kt`)

- Durations: `short`=150, `medium`=250, `long`=300 ms; easing `FastOutSlowInEasing`.
- **Reduced motion:** `NourReduceMotion.isEnabled(context)` reads
  `Settings.Global.ANIMATOR_DURATION_SCALE == 0` ("Remove animations"). It is
  published via `NourReduceMotion.Local` and mirrored into `NourMotion.reduced`.
- **Rule:** every finite animation duration goes through `NourMotion.duration(ms)`,
  which returns `0` when motion is reduced. Continuous/decorative loops are gated
  by `NourMotion.allowContinuous` / an explicit `reducedMotion` check and settle
  on a static final frame (never disappear).
- Spring-based transitions must also collapse under reduced motion. Prefer a
  reduced-motion-aware spring helper over calling `spring(...)` directly (see
  `NourMotion.duration` for the tween equivalent).
- Motion purpose: explain relationships (navigation crossfade), acknowledge
  actions (selection), preserve continuity (step slides). Avoid decorative loops
  and delayed interactions.

## 6. Accessibility requirements

- Minimum 48 dp touch targets (`NourSize.touchTarget`).
- Icon-only controls need a meaningful `contentDescription`; purely decorative
  icons use `contentDescription = null` so they are not announced twice.
- Headings carry `semantics { heading() }`; toggles expose `stateDescription`.
- No meaning conveyed by color/animation alone (the Qibla screen states its mode
  in text, not just color).
- Layout must survive large font scales (wrapping rows via `FlowRow`, bounded
  content widths) and RTL (`supportsRtl=true`, per-language layout direction).
- Arabic scripture direction is independent of interface language.

## 7. Open design-system work

- Not every animation honors reduced motion yet (see the audit's confirmed
  defects) — spring transitions in `SeamlessPrayerHeader` and `SetupWizard`.
- The design system is hand-built; it does not use the newer `material3`
  `MaterialExpressiveTheme`/`MotionScheme` APIs (the pinned Compose BOM predates
  them). Adopting them is a deliberate, separately-verified dependency change.
