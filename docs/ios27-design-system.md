# Nour — iOS 26/27 Design System

Status: current visual-language reference (2026-10-21). It describes the
iOS-inspired, **opaque-only** pass applied to Nour. The earlier
`docs/android16-m3e/DESIGN_SYSTEM.md` remains useful for the M3 Expressive
migration history, but its palette and shape values are superseded by this
document.

This pass changes appearance, not religious or scheduling behaviour. Prayer
calculation, Qibla sensor maths and honesty rules, Adhan scheduling, Quran
reading persistence, Room schema, preference keys, localization and the
offline/no-`INTERNET` constraint are unchanged.

## 1. Design principles

1. **Content first.** Navigation and controls float around content; they never
   become a decorative centrepiece.
2. **Opaque, always.** No liquid glass, blur, refraction, highlight overlays or
   translucent floating chrome. Floating surfaces use a solid elevated colour
   plus a hairline and a restrained shadow.
3. **Grouped, not boxed-in.** The page uses a grouped background; cards and rows
   are white (light) or raised charcoal (dark) with a 1 dp hairline. Cards are
   flat by default; separation comes from the surface ladder.
4. **Accent discipline.** The vivid blue `primary` is reserved for actions,
   selected navigation and the single most important number/indicator. Soft
   accent surfaces use `secondaryContainer`; neutral rows stay neutral.
5. **Native typography.** The platform font (SF Pro when available, otherwise
   the Android default) carries the hierarchy. No display font, no all-caps
   metadata, no letter-spacing tricks.
6. **Motion is feedback, not decoration.** Short springs and press-scale are
   used for taps. Every duration goes through `NourMotion.duration(...)` so the
   system "Remove animations" setting collapses it.

## 2. Colour

Source of truth: `ui/theme/Color.kt` and `ui/theme/Theme.kt`.

The built-in palette is generated from seed `#0A7AFF` with the iOS 27 Design Kit
recipe (Material Color Utilities, Variant CONTENT, spec 2025, phone platform);
neutral surfaces are intentionally low-chroma so they read Apple-neutral.

### Light

| Role | Value | Use |
| --- | --- | --- |
| background / surface | `#F4F4F7` | grouped page |
| surfaceContainerLowest / Low | `#FFFFFF` | cards, inputs, floating chrome |
| surfaceContainer | `#EFEFF4` | subtle panels, reader header |
| surfaceContainerHigh | `#E7E7EC` | pressed tracks, chips |
| surfaceContainerHighest | `#DEDEE4` | segmented track / strongest neutral |
| onSurface | `#1B1B1F` | primary text |
| onSurfaceVariant | `#44474F` | secondary text, labels |
| outlineVariant | `#C7C7CC` | hairlines and borders |
| primary | `#0058BD` | actions, selection, key number |
| secondaryContainer | `#CCDAFE` | pale accent surfaces |
| onSecondaryContainer | `#515F7E` | text on pale accent |

### Dark

| Role | Value | Use |
| --- | --- | --- |
| background / surface | `#121316` | grouped page |
| surfaceContainerLowest | `#0D0E11` | lowest recessed layer |
| surfaceContainerLow | `#1B1B1F` | cards |
| surfaceContainer | `#1F1F23` | subtle panels |
| surfaceContainerHigh | `#292A2D` | floating chrome, segmented track |
| surfaceContainerHighest | `#343538` | segmented selected thumb / strongest neutral |
| onSurface | `#E3E2E6` | primary text |
| onSurfaceVariant | `#C4C6D0` | secondary text, labels |
| outlineVariant | `#44474F` | hairlines and borders |
| primary | `#ADC6FF` | actions, selection, key number |
| secondaryContainer | `#3B4967` | pale (muted) accent surfaces |
| onSecondaryContainer | `#AAB8DB` | text on accent surfaces |

Rules:

- Floating chrome uses `nourFloatingSurfaceColor()`: white in light,
  `surfaceContainerHigh` in dark. It is never alpha-composited.
- Cards use `nourCardSurfaceColor()` (`surfaceContainerLow`, white in light).
- Hairlines use solid `outlineVariant`; do not replace them with translucent
  white/black borders.
- User-selected seeds and wallpaper dynamic colour still flow through
  `MyApplicationTheme`; components must read scheme roles, never hardcoded
  palette constants. The default is the reference palette; dynamic colour is
  opt-in.
- Error, tertiary and warning surfaces keep their semantic scheme roles. Do not
  use the blue accent for warnings.

## 3. Spacing, size and shape

Source of truth: `ui/theme/Tokens.kt` and `ui/theme/Shapes.kt`.

### Spacing

| Token | Value | Typical use |
| --- | --- | --- |
| `NourSpace.xs` | 4 dp | icon-to-label, tight stacks |
| `NourSpace.sm` | 8 dp | inner row gap, floating gutter |
| `NourSpace.md` | 12 dp | list rhythm |
| `NourSpace.lg` | 16 dp | standard content gutter and card padding |
| `NourSpace.xl` | 24 dp | section rhythm |
| `NourSpace.xxl` | 32 dp | empty-state breathing room |

### Sizes

| Token | Value | Use |
| --- | --- | --- |
| `touchTarget` | 48 dp | every interactive control's minimum |
| `circularControl` | 44 dp | visible circle inside the 48 dp target |
| `icon` / `smallIcon` | 24 / 20 dp | action icon / inline icon |
| `rowIcon` | 22 dp | leading settings-row glyph |
| `input` | 56 dp | search/input height |
| `navBarHeight` | 68 dp minimum; grows for two label lines at narrow widths or fontScale > 1.05 | floating bottom bar |
| `navRailWidth` | 72 dp | floating rail surface |
| `navRailReserve` | 88 dp | host column reserved for the rail |
| `toolbarHeight` | 52 dp | reader/context toolbar |
| `compass` | 320 dp | max Qibla dial |

### Shape ladder

Pills are only for controls (buttons, chips, segmented controls, nav surfaces
where appropriate). All other surfaces use rounded rectangles.

| Token | Value | Use |
| --- | --- | --- |
| `extraSmall` / small marks | 8 dp | tiny marks, dots |
| `CardSubtleShape` / `ListRowShape` / `InputExpressiveShape` | 16 dp | rows, inputs, small cards |
| `CardExpressiveShape` / `DialogExpressiveShape` | 24 dp | cards, sheets, dialogs, nav bar/rail |
| `PillShape` / `ButtonExpressiveShape` / `SegmentedControlShape` | capsule | actions and selection controls |
| `ImmersiveHeaderShape` | 24 dp bottom corners | sky/reader headers that meet the top edge |

Nothing sits between 24 dp and a true capsule.

## 4. Typography

Source of truth: `ui/theme/Type.kt`. It extends Material Typography but follows
Apple sizing and weight:

- Display 48/56, 40/48, 34/42; Semibold, negative tracking.
- Headline 32/40, 28/36, 24/32; Semibold.
- Title 22/28, 16/24, 14/20; Semibold/Medium.
- Body 16/24, 14/22, 12/18; Normal.
- Label 14/20, 12/16, 11/14; SemiBold/Medium.

Tabular numbers (`withTabularNumbers()`) are required for countdowns and times
so digits do not shift. Use sentence case. Do not uppercase metadata labels.

## 5. Components

Source of truth: `ui/components/NourComponents.kt` and
`ui/components/NourNavigation.kt`.

### Content frame

`NourContentFrame` paints the grouped `surface`, applies the shared side gutter,
and caps content width (`contentWidth` 960 dp, `readingWidth` 720 dp). The Salah
screen passes zero content padding so its sky can bleed edge-to-edge.

### Floating bottom navigation (compact, `<600 dp`)

- Solid `nourFloatingSurfaceColor()` surface, 24 dp corners, 1 dp border,
  10 dp shadow.
- 16 dp side gutter, 8 dp top/bottom gutter, plus the system navigation inset.
- Four destinations: Salah, Qibla, Quran, Settings. Each item is a 48 dp
  selectable target with a 24 dp icon, an always-visible label (11 sp) and a
  short 16 x 3 dp selection bar under the label.
- Selection = accent tint + filled icon + short bar. There is **no selection
  pill**. The bar and rail expose `selectableGroup()` semantics.
- The bar reserves one label line at the default font scale and grows to two
  lines at narrow widths or larger font scales; labels still ellipsize if a
  translation cannot fit.
- The host `Scaffold` pads content by the bar height, so the bar never covers
  the last row.
- The bar does not auto-hide on scroll. Hiding/showing causes content jumps and
  makes navigation less predictable; this is an intentional deviation from
  some iOS patterns.

### Floating rail (`>=600 dp`)

- The same opaque surface and selection language, 72 dp wide, 24 dp corners.
- Top-aligned inside an 88 dp reserved column; applies `safeDrawing` insets and
  scrolls vertically when a short window cannot fit every destination.
- Labels remain beside icons; no icon-only fallback. At very large font scales
  labels may ellipsize visually, while TalkBack still reads the full label.

### Contextual toolbar

`NourFloatingToolbar` is a solid 24 dp surface, 52 dp minimum height, with 48 dp
circular actions. It is used by the Quran reader and stays separate from the
global navigation.

### Segmented control

`NourSegmentedControl` is a solid capsule track with an opaque raised thumb
(white in light, a lighter `surfaceContainerHighest` in dark) and an `outline`
hairline around the selected thumb, so selection does not rely on a sub-3:1 fill
difference. It is at least 52 dp high; its height is derived from the actual
`labelLarge` line height (two lines + 8 dp), so it grows correctly under
Android 14+ non-linear font scaling instead of clipping. Labels are 14 sp,
icons optional. Use for
2–4 mutually exclusive views (Quran tabs) or choices (theme mode, reading mode,
with `Role.RadioButton`). Do not use it as a row of unrelated buttons.

### Buttons

- **Circular icon button:** 48 dp target, 44 dp opaque circle, 20–24 dp glyph.
  Required for compact actions such as day arrows, reader actions and Qibla
  calibration info. Icon-only controls need a content description.
- **Capsule button:** 48 dp minimum height, `PillShape`. Use for primary and
  secondary actions (Enable location, Use sensors, Save, Try again).
- **Text button:** plain accent text for low-emphasis actions (Change, Reset,
  Show all). It still keeps a 48 dp touch target.
- Press feedback: `nourPressScale` (0.95–0.97) plus the standard indication.

### Cards, rows and inputs

- Cards: 24 dp corners, `nourCardSurfaceColor()`, 1 dp `outlineVariant` hairline,
  zero elevation. Content padding 16 or 24 dp.
- Rows: 16 dp shape, transparent on the grouped page or white inside a card.
  Leading 22 dp icon, title 16/24, secondary 12–14 sp.
- Inputs: 56 dp height, 16 dp corners, opaque container, 1 dp border. The
  `NourSearchField` wraps the platform text field with a leading search icon and
  a 48 dp clear action.
- Pale accent cards (`secondaryContainer`) are for one focal surface per screen:
  next prayer, continue reading, privacy pledge, facing-Qibla confirmation.
- Empty/error/loading states use `NourEmptyState`, `NourErrorState` and
  `NourLoadingState`; never invent local placeholder copy.

## 6. Screen rules

### Salah

- The sky hero remains the only full-bleed, atmospheric surface. It is not
  glass and does not overlay content.
- Location pill is opaque `surfaceContainerHigh`; the next-prayer block uses a
  soft `secondaryContainer` row, not a vivid hero card.
- Date switching uses shared circular icon buttons. Prayer rows are white cards
  with hairline borders; the live prayer uses an accent bar and accent time.
- The Quran verse card resolves real bundled text; never substitute demo text.

### Qibla

- Location/accuracy and compass are grouped cards. The info action is a 44 dp
  circle with an 8 dp gap from the accuracy chip.
- The Qibla banner uses `secondaryContainer` + accent border and appears only
  from real calibrated sensor readings. The manual slider stays a reference
  heading and must never produce the "facing the Kaaba" claim.
- Metrics are two stacked/label-value areas with sentence-case labels; the
  bearing number is the accent focal point.
- Manual/sensor toggle is a capsule, not a pill switch with ambiguous state.

### Quran

- Library uses a 3-way segmented control (Surah / Bookmark / Search), a 56 dp
  opaque search field, a single pale continue-reading card, and white surah
  rows. Page/verse numbering is Arabic-Indic when the language requires it.
- The segmented control grows for two-line labels at large font scales; every
  library list reserves `nourSegmentedControlHeight()` plus the bar's own
  padding, so content never starts underneath a taller bar.
- Reader toolbar is a floating opaque surface with circular actions. Cards use
  24 dp; ayah number pills use `secondaryContainer`, not vivid primary.
- Reading position, bookmarks, mode and font size persist; the reader must not
  reset to surah 1 on process death.
- If a translation/tafsir is not bundled, show the honest unavailable message;
  never fabricate content.

### Settings

- Grouped cards: Appearance (theme mode segmented control, dynamic colour
  switch, seed colour controls), Language & formats, Location & calculation,
  Adhan, Privacy. Seed swatches are a `selectableGroup()` of 48 dp radio
  targets that wraps four per row (no clipped horizontal scroller).
- Low-emphasis actions are text buttons; destructive or primary actions are
  capsules.
- The privacy pledge uses a pale `secondaryContainer` card, not a full-bleed
  vivid primary block.
- Every modal uses the 24 dp dialog shape and opaque `surfaceContainerHigh`.

### Onboarding / dialogs

- Continue/back actions remain capsules; permission and location choices are
  explicit. Dialogs and sheets are opaque, 24 dp, and dismissible by back.
- Do not add network or analytics permissions as part of visual work.

## 7. Motion and accessibility

- `NourMotion.duration(ms)` returns 0 when the system "Remove animations"
  setting is on; use it for every tween. `NourMotion.spring(...)` collapses to
  `snap()`. Decorative continuous motion checks `NourMotion.allowContinuous`.
- Press-scale is tactile feedback, not an entrance animation.
- Minimum touch target 48×48 dp; visual circles may be 44 dp.
- Icon-only actions need content descriptions and a visible pressed state.
- Arabic scripture direction is independent of interface language; RTL layouts
  mirror the chrome but not the Quran text.
- Text uses the platform font and can grow to at least 200%; rows wrap, labels
  ellipsize or stack rather than clipping. Avoid fixed heights around text.
- The floating nav/rail and every card must remain opaque when the user picks a
  custom seed or wallpaper colour.

## 8. Verification status

Final JVM/build run on 2026-10-21:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk \
ANDROID_HOME="$HOME/Android/Sdk" \
GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=768m" \
./gradlew :app:testDebugUnitTest :app:assembleDebug \
  -Proborazzi.test.record=true --offline --no-daemon --max-workers=2
```

- `BUILD SUCCESSFUL in 3m 13s`; 75 tests, 0 failures, 0 errors, 0 skipped
  across 11 classes. The KSP/AWT worker printed a non-fatal
  `NullPointerException`; the build and tests still completed successfully.
- 29 PNG captures in `app/build/screen-verify/`.
- Debug APK: 27,154,257 bytes (26 MB);
  SHA-256 `11e446b808fa82e9b5a5a985fe2a1c3e4ed80b02ee7f63db2f2a08af96393824`.
- Follow-up review fixes applied: `NourSearchField` field semantics/tag, Quran
  search off the main thread plus startup index warm-up, `rememberSaveable`
  navigation state, growing nav bar for two-line labels, and Qibla manual
  fallback reflow.

Visual checks are provided by `ScreenRenderTest` / `ScreenCaptureMatrixTest`
PNG captures in `app/build/screen-verify/`:

- `01_prayer`, `02_qibla`, `03_quran`, `04_settings`, `05_setup_wizard`
- `06_sky_sun_and_moon`, `07_sky_night`
- `08_navigation_chrome`, `09_navigation_chrome_dark`, `10_navigation_rail`
- `11_qibla_dark`, `12_settings_dark`
- `m*` matrix: dark, 200% font, RTL, 320 dp width, light/dark comparisons,
  reader cards/continuous modes, plus `m15_prayer_ar_rtl_font200`,
  `m16_qibla_ar_rtl_font200` and `m17_settings_font200` for the combined
  RTL/large-font layouts.

What is **confirmed** by JVM tests and captures:

- Kotlin and unit-test sources compile.
- The new nav bar/rail render, expose `selectableGroup()` selection semantics,
  and report selection changes.
- The segmented control reports selection changes and exposes selected state;
  choice-style controls use `Role.RadioButton`.
- No `INTERNET` permission or network client was added.
- Screens render at the capture breakpoints without crashes, including the
  matrix's Settings, Qibla and Salah screens at 200% font and, for Qibla/Salah,
  RTL at 200% simultaneously.
- The `02_qibla`, `03_quran`, `04_settings`, `08_navigation_chrome`,
  `10_navigation_rail`, `m15`–`m17` captures were inspected; no clipping was
  found in those JVM renders.

What is **not verified** and needs a physical device:

- TalkBack traversal and focus order for the custom nav bar/rail and segmented
  controls.
- RTL plus 200% font scale on a physical device (a JVM capture now exists,
  but real shaping, font fallback and TalkBack focus still need hardware).
- Qibla sensor calibration, tilt warning and honesty banner.
- Adhan exact-alarm delivery with the screen off, Doze and battery saver.
- Cold-restart persistence of reading position, bookmarks, theme and Adhan
  settings.
- Visual contrast judgements from captures alone (captures run with a paused
  clock and cannot prove every animated state).

Known follow-ups, not regressions from this pass:

- The hand-authored default palette in `Color.kt` and the palette generated
  from the default `#0A7AFF` seed in `DynamicColor.kt` still differ (most
  visibly the grouped page tone). Captures use the static default. Unifying them
  is a palette decision, not a layout fix, and was left for a separate change.
- Grouped cards intentionally sit close to the page tone; separation rests on
  the hairline border. Revisit only if the product wants stronger card edges.

## 9. Changing the system

1. Change semantic tokens in `ui/theme/` first; never hand-tune component
   colours.
2. Keep floating chrome opaque; `tools`-style alpha overlays are out of bounds.
3. Use the semantic shapes (`CardExpressiveShape`, `CardSubtleShape`,
   `InputExpressiveShape`, `PillShape`) instead of new radii.
4. Route new durations through `NourMotion.duration` and new press feedback
   through `nourPressScale`.
5. Add or update a capture/test when a screen's structure changes, and record
   actual command results in `DESIGN_MIGRATION.md`. Do not claim a device check
   from a JVM capture.
