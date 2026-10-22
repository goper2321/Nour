# Nour — Design System

> **Superseded for current visual work (2026-10-21).** The iOS 26/27 opacity
> pass replaced the palette, shape ladder, and navigation chrome described
> here. Use `docs/ios27-design-system.md` as the current reference. This file is
> retained as the Android 16 / M3 Expressive migration record, now merged with
> the iOS 26-inspired design standard below.

Status: historical specification for the earlier M3 Expressive baseline, merged
with the iOS 26-inspired app design standard. Update
`docs/ios27-design-system.md` for current tokens and components.

## 1. Objective

Design and implement a complete, consistent, production-quality UI inspired by
Apple's iOS design language, applied to Nour's Android/Compose stack.

The design must feel:
- Minimalist, elegant, clean, and carefully composed.
- Content-first, with clear visual hierarchy.
- Refined through spacing, typography, geometry, materials, and motion.
- Consistent across every screen and component.
- Native to the platform on which the application runs.

Use Apple's Human Interface Guidelines as the primary visual reference. Do not
interpret this as a request to add blur, pill shapes, or rounded corners
everywhere.

## 2. Mandatory design principles

### Navigation
- Use a floating bottom navigation bar when appropriate; keep it visually
  separate from content; use simple monochrome icons and concise labels.
- Show the selected destination with a clear, restrained state; support safe
  areas and device corner geometry; adapt while scrolling where appropriate.
- Never cover important content or essential controls; use top floating
  toolbars for contextual actions.
- Nour: bar < `NourSize.compactBreakpoint` (600 dp), else rail; four
  destinations (Salah, Qibla, Quran, Settings); selection bar, no selection pill.

### Buttons and controls
- Circular icon buttons for compact standalone actions; capsule buttons for
  primary actions and suitable grouped controls; rounded rectangles for cards,
  inputs, and containers.
- Maintain consistent component dimensions and internal spacing; provide
  visible default/pressed/focused/selected/disabled/loading states; give icon
  buttons accessible labels.
- Do not make every button circular or every action a pill.

### Materials and surfaces
- Prefer solid or standard material surfaces for content, cards, and primary UI
  regions; subtle elevation (light shadows) only where it clarifies hierarchy.
- Keep foreground text and icons highly readable in both light and dark mode.
- Avoid excessive blur, reflections, gradients, glow, and decorative borders;
  provide an accessible appearance when motion is reduced.
- Nour: floating chrome uses opaque `nourFloatingSurfaceColor()`; cards use
  `nourCardSurfaceColor()`; hairlines use solid `outlineVariant`.

### Typography and layout
- Use the platform's appropriate system font; establish clear hierarchy for
  page titles, section headings, body text, supporting labels, and metadata.
- Generous but purposeful whitespace; consistent alignment and content margins;
  prefer simple surfaces over nested cards; keep text readable and responsive.
- Support dark/light mode, larger text, localization, and RTL when required.
- Nour: `FontFamily.Default`, tabular figures on live numbers, sentence case,
  no all-caps metadata.

### Motion and interaction
- Fluid, natural transitions and restrained spring animations; small press-scale
  changes on suitable controls; transitions that communicate state changes and
  preserve spatial continuity.
- Avoid excessive bounce, elastic motion, parallax, and decorative animation;
  respect reduced-motion settings; keep interactions responsive.
- Nour: every duration via `NourMotion.duration(ms)` (returns 0 when reduced);
  `NourMotion.spring(...)` collapses to `snap()`.

## 3. Theme strategy

`MyApplicationTheme` (`ui/theme/Theme.kt`) picks a `ColorScheme` in this order:

1. Custom seed color (user-picked) → `SeedColorSchemes.schemeFor(seed, dark)`.
2. Dynamic/wallpaper color (opt-in) → framework `dynamicLight/DarkColorScheme` on API 31+.
3. Built-in Nour palette → `LightColorScheme` / `DarkColorScheme`.

- Built-in palette is a muted lavender identity (light primary `#4A4459`,
  background `#FCF8FD`-family). Dark color is defined explicitly, not derived.
- `SeedColorSchemes` uses `com.google.android.material.color.utilities`
  (`SchemeTonalSpot` + `MaterialDynamicColors`) to expand one seed into every
  semantic role with spec-correct contrast.
- Dynamic color is **off by default**; the stock palette is the reference look.
- Theme mode and dynamic-color opt-in persist via `NourPreferences`.
- System-bar icon appearance follows the resolved theme
  (`WindowCompat.getInsetsController`).

Rule: never hardcode a semantic color in a screen. Use `MaterialTheme.colorScheme`.
Artistic palettes (sky visualizer, header sky probe) are the only sanctioned
exceptions.

Colors (semantic): use semantic background, surface, text, separator, accent, and
status colors; support light/dark; maintain accessible contrast; keep the default
palette neutral and introduce accent color with restraint. Error/tertiary/warning
surfaces keep their semantic roles — never use the blue accent for warnings.

## 4. Design tokens

All in `ui/theme/`. Use semantic tokens, not raw `.dp`, for padding/gaps.

Spacing (Nour / standard):
| Token | Value | Use |
|---|---|---|
| `NourSpace.xs` | 4 dp | micro spacing |
| `NourSpace.sm` | 8 dp | compact spacing |
| `NourSpace.md` | 12 dp | component spacing |
| `NourSpace.lg` | 16 dp | standard spacing |
| `NourSpace.xl` | 24 dp | section spacing |
| `NourSpace.xxl` | 32 dp | major separation |

Geometry (shape ladder):
| Token | Value | Use |
|---|---|---|
| small marks | 8 dp | tiny marks, dots |
| `CardSubtleShape` / `ListRowShape` / `InputExpressiveShape` | 16 dp | rows, inputs, small cards |
| `CardExpressiveShape` / `DialogExpressiveShape` | 24 dp | cards, sheets, dialogs, nav bar/rail |
| `PillShape` / `ButtonExpressiveShape` / `SegmentedControlShape` | capsule | actions and selection controls |
| `ImmersiveHeaderShape` | 24 dp bottom corners | sky/reader headers meeting the top edge |

Pills are only for controls. All other surfaces use rounded rectangles. Nothing
sits between 24 dp and a true capsule.

Controls / sizes:
| Token | Value | Use |
|---|---|---|
| `touchTarget` | 48 dp | every interactive control's minimum |
| `circularControl` | 44 dp | visible circle inside the 48 dp target |
| `icon` / `smallIcon` | 24 / 20 dp | action icon / inline icon |
| `rowIcon` | 22 dp | leading settings-row glyph |
| `input` | 56 dp | search/input height |
| `navBarHeight` | 68 dp min (grows for two label lines) | floating bottom bar |
| `navRailWidth` | 72 dp | floating rail surface |
| `navRailReserve` | 88 dp | host column reserved for the rail |
| `toolbarHeight` | 52 dp | reader/context toolbar |
| `compass` | 320 dp | max Qibla dial |

Circular controls: equal width and height. Floating navigation: ~64–72 dp tall,
adjusted for safe areas. Minimum touch target: 44×44 pt on iOS / 48 dp on
Android. Primary button height: ~48–52 pt/dp. Circular icon button: ~44–48
pt/dp. Icon size: ~20–24 pt/dp.

Other tokens:
- `NourAlpha`: opacity scale for accent-derived fills/borders (`CardBorder`=0.40
  canonical resting card border).
- `ExpressiveShapes`: M3 `Shapes` (8/12/16/24/28 dp), default for components.
- Semantic shapes: `PillShape`, `CardExpressiveShape` (28), `CardSubtleShape`
  (14), `ListRowShape` (22), `DialogExpressiveShape`, `SheetExpressiveShape`,
  `InputExpressiveShape`, `ButtonExpressiveShape`, `ChipExpressiveShape`.

Note: `Color.kt` retains legacy `Mono*`/`Emerald*`/`Gold*`/`Sand*`/`Midnight*`
aliases (compatibility shims; new code uses M3 roles).

## 5. Typography (`ui/theme/Type.kt`)

- Custom `Typography` built from a `textStyle(size, lineHeight, weight,
  letterSpacing, feature)` helper, `FontFamily.Default`.
- Display/headline/title/body/label all defined; `displayLarge/Medium/Small` use
  tabular figures (`tnum`).
- `TextStyle.withTabularNumbers()` and explicit `fontFeatureSettings = "tnum"`
  on timers/counts to stop digit jitter. Keep on any live number.
- Hierarchy: hero prayer name = `displaySmall` (ExtraBold); section titles =
  `headlineLarge`/`titleLarge`; group labels = `labelLarge`; metadata =
  `bodySmall`/`labelMedium`.

Starting scale (iOS 26-inspired, semantic styles not arbitrary sizes):
- Display 48/56, 40/48, 34/42; Semibold, negative tracking.
- Headline 32/40, 28/36, 24/32; Semibold.
- Title 22/28, 16/24, 14/20; Semibold/Medium.
- Body 16/24, 14/22, 12/18; Normal.
- Label 14/20, 12/16, 11/14; SemiBold/Medium.

Prefer medium/semibold weights for headings, regular for body, heavy only for
intentional emphasis. Use sentence case; do not uppercase metadata labels.

## 6. Reusable components

Source: `ui/components/NourComponents.kt` and `ui/components/NourNavigation.kt`.

Nour primitives:
- `NourContentFrame(maxWidth)` — centers content, bounds width (`contentWidth`
  960 dp default, `readingWidth` 720 dp for Quran).
- `NourScreenHeading(title, subtitle)` — `headlineLarge` + optional
  `bodyMedium`, marked `heading()`.
- `NourSectionHeader(title)` — `labelLarge`, marked `heading()` for TalkBack.
- `NourPanel(content)` — `surfaceContainerLow` card with canonical hairline
  border and flat elevation.
- `NourBadge(text, emphasized)` — pill surface, primary/secondary container.
- `NourEmptyState(title, message)` / `NourErrorState` / `NourLoadingState` —
  centered heading + body for empty/error/loading states.
- `NourFloatingToolbar` — solid 24 dp surface, 52 dp min height, 48 dp circular
  actions (Quran reader).
- `NourSegmentedControl` — solid capsule track with opaque raised thumb.
- `NourSearchField` — wraps platform text field with leading search icon and
  48 dp clear action.
- `NourFloatingNavigationBar` / `NourFloatingRail` — compact bar / 72 dp rail.

Candidate components to build only if needed (iOS 26 standard list):
FloatingNavigationBar, FloatingToolbar, CircularIconButton, CapsuleButton,
StandardSurface, AdaptiveSheet, SearchField, SegmentedControl, LoadingState,
EmptyState, ErrorState.

Rule: consolidate repeated card/panel styling into these primitives instead of
re-deriving `Card(...)` borders per screen. Do not add generic abstractions that
make a simple screen harder to read. Each component uses shared tokens and
consistent interaction rules.

## 7. Motion

- Durations: `short`=150, `medium`=250, `long`=300 ms; easing
  `FastOutSlowInEasing`.
- **Reduced motion:** `NourReduceMotion.isEnabled(context)` reads
  `Settings.Global.ANIMATOR_DURATION_SCALE == 0` ("Remove animations"). Published
  via `NourReduceMotion.Local` and mirrored into `NourMotion.reduced`.
- **Rule:** every finite animation duration goes through `NourMotion.duration(ms)`,
  which returns `0` when motion is reduced. Continuous/decorative loops gated by
  `NourMotion.allowContinuous` / explicit `reducedMotion` check, settling on a
  static final frame (never disappear).
- Spring transitions must also collapse under reduced motion; prefer a
  reduced-motion-aware spring helper over calling `spring(...)` directly.
- Motion purpose: explain relationships (navigation crossfade), acknowledge
  actions (selection), preserve continuity (step slides). Avoid decorative loops
  and delayed interactions.
- Use short, responsive feedback animations; prefer spring-based transitions for
  suitable navigation/component interactions; avoid imposing one animation curve
  on every interaction.
- Press feedback: `nourPressScale` (0.95–0.97) plus standard indication.
- Provide an accessible appearance when motion is reduced.

## 8. Accessibility requirements

- Minimum 48 dp touch targets (`NourSize.touchTarget`); visual circles may be
  44 dp.
- Icon-only controls need a meaningful `contentDescription`; purely decorative
  icons use `contentDescription = null`.
- Headings carry `semantics { heading() }`; toggles expose `stateDescription`.
- No meaning conveyed by color/animation alone (Qibla screen states its mode in
  text).
- Layout must survive large font scales (wrapping rows via `FlowRow`, bounded
  content widths) and RTL (`supportsRtl=true`, per-language layout direction).
- Arabic scripture direction is independent of interface language.
- Dark mode, contrast, keyboard navigation, touch targets, small screens, large
  text, landscape where supported.
- Text uses the platform font and can grow to at least 200%; rows wrap, labels
  ellipsize or stack rather than clipping. Avoid fixed heights around text.

## 9. Engineering requirements

Before changing the application:
1. Inspect the repository, framework, architecture, existing screens, and
   dependencies.
2. Identify target platforms and supported screen sizes.
3. Understand current features and preserve working behavior.
4. Create a concise design-system specification before implementing the UI.

During implementation:
1. Implement shared tokens and components first.
2. Use native platform components when they provide correct behavior/appearance.
3. Use custom components only when necessary.
4. Keep business logic separate from presentation.
5. Avoid unnecessary dependencies and duplicated styles.
6. Do not replace functioning features with static mockups.

Keep the app offline-first: no `INTERNET` permission, no network clients,
analytics, or Firebase. Do not fabricate religious content; missing
translations/tafsir/pages report "unavailable".

## 10. Visual quality assurance

Review every important screen after implementation:
- Spacing, alignment, typography, visual hierarchy.
- Floating navigation placement and safe-area handling.
- Surface and text readability in light and dark backgrounds.
- Circular and capsule control consistency.
- Loading, empty, error, and disabled states.
- Small screens, large text, landscape layouts where supported.
- Dark mode, contrast, keyboard navigation, touch targets.
- Animation quality and reduced-motion behavior.

Build and run using available tools; inspect screenshots or running screens;
correct visual defects and repeat. Do not declare the design complete merely
because the code compiles. Note: JVM captures run with the test clock paused, so
composable colour tweens are frozen at their initial value — do not judge text
colour/contrast from a capture.

## 11. Acceptance criteria

The implementation is complete when:
- Every screen follows the same design language.
- Components and tokens are reused consistently.
- Navigation and interactive controls behave correctly.
- The interface remains readable and accessible.
- Existing required functionality continues to work.
- The application builds successfully and major visual issues have been checked.

The final result must look intentionally designed, not like a collection of
unrelated AI-generated screens.

## 12. Required deliverables

Produce:
1. A documented design system.
2. Centralized design tokens.
3. Reusable UI components.
4. Implemented application screens.
5. A concise summary of changes, validation results, and known limitations.

Make reasonable design decisions independently. Do not ask for approval for every
small visual detail. Prioritize usability, consistency, simplicity,
accessibility, and maintainability.

## 13. Open design-system work

- Not every animation honors reduced motion yet (spring transitions in
  `SeamlessPrayerHeader` and `SetupWizard`).
- The design system is hand-built; it does not use the newer `material3`
  `MaterialExpressiveTheme`/`MotionScheme` APIs (the pinned Compose BOM
  predates them). Adopting them is a deliberate, separately-verified
  dependency change.