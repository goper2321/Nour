# Navigation contract

Reference for how screens are selected and composed. Documents what the code
does today — verified against `MainActivity.kt` and
`ui/components/NourNavigation.kt`. Update this file with the code, never the
reverse.

There is no navigation library: one `MainActivity`, a plain `NourNavDestination`
enum, and `AnimatedContent` for the swap. No routes, no deep links, no fragments.

## 1. Destinations

| Destination | Screen (call) | Entry points | Content max width | Content padding |
|-------------|---------------|--------------|-------------------|-----------------|
| `SALAH` | `PrayerScreen(location, calculationMethod, madhab, highLatRule, adjustments, onRequestLocationUpdate)` | Tab 1 in bar/rail | unbounded | none — the sky hero bleeds edge-to-edge |
| `QIBLA` | `QiblaScreen(location, onLocationUpdated)` | Tab 2 | `NourSize.contentWidth` (960 dp) | horizontal `NourSpace.lg` (16 dp) |
| `QURAN` | `QuranScreen()` | Tab 3 | `NourSize.readingWidth` (720 dp) | horizontal 16 dp |
| `SETTINGS` | `SettingsScreen(…)` | Tab 4 | `NourSize.contentWidth` (960 dp) | horizontal 16 dp |

There are no screen-to-screen "navigate-ons": every entry point to a
destination is a bar or rail item. Other doors that exist:

- `SettingsScreen(onReopenSetup)` → sets `setupDone = false`, which leaves
  destinations entirely and re-shows `SetupWizard` (see §3).
- Settings' exact-alarm row opens the **system** settings via
  `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` (API 31+), falling back to the app
  details page. That is an external intent, not a destination.
- The Quran reader shows a surah inside the `QURAN` destination; its own
  `BackHandler` closes the reader layer (QuranScreen.kt, enabled while a surah
  is selected for reading).

## 2. Enum metadata

`NourNavDestination` (MainActivity.kt) — four entries, each carrying icon pair
and test tag:

| Entry | Selected / unselected icon | Test tag | Label source |
|-------|---------------------------|----------|--------------|
| `SALAH` | `Icons.Rounded/Outlined.Schedule` | `nav_salah` | `strings.navSalah` |
| `QIBLA` | `Icons.Rounded/Outlined.Explore` | `nav_qibla` | `strings.navQibla` |
| `QURAN` | `Icons.Rounded/Outlined.AutoStories` | `nav_quran` | `strings.navQuran` |
| `SETTINGS` | `Icons.Rounded/Outlined.Settings` | `nav_settings` | `strings.navSettings` |

## 3. Routing conditions

- **Nav state.** A single `currentDestination` (`rememberSaveable` with a
  name-based Saver; survives process death). Default is `SALAH` on a fresh
  install.
- **Setup gate.** While `setupDone` is false, `SetupWizard` replaces the whole
  navigation — the four tabs do not exist yet. `setupDone` starts true when
  either `prefs.setupCompleted` or the legacy flag
  `nour_app_prefs`/`has_asked_location_first_time` is set; completing the
  wizard writes both.
- **Bar vs rail.** `BoxWithConstraints` in `NourMainApp`:
  `maxWidth >= NourSize.compactBreakpoint` (600 dp) → floating rail on the
  leading edge (width reserve `NourSize.navRailReserve` = 88 dp); otherwise the
  floating bottom bar at `BottomCenter`. Both are built from the same
  `NourNavItem` list generated from `NourNavDestination.values()`; selection
  writes `currentDestination = values()[index]` (index = ordinal).
- **Back.** A host `BackHandler` is enabled on every destination *except*
  `SALAH` and returns to `SALAH` (predictive back exits the app only from the
  home tab). The reader's `BackHandler` composes later, so while a surah layer
  is open it takes precedence; it is composed *inside* the `QURAN` branch.
- **Transitions.** `AnimatedContent` keyed on the destination: fade-in
  `NourMotion.medium` (300 ms) / fade-out `NourMotion.short` (200 ms), both
  through `NourMotion.duration(...)`, so the system *Remove animations*
  setting collapses them to instant swaps.
- **Bottom reserve.** Each destination receives `LocalFloatingBottomExtra`:
  rail layout = system inset + 16 dp; bar layout = system inset + 8 dp gap +
  the bar's real measured band + an 8 dp breath. The bar reports its laid-out
  height via `onHeightMeasured`, so the reserve tracks font scales (the
  formula lives in `MainActivity`; `FloatingChromeTest` mirrors it — change
  one, change both).

## 4. State that forces a screen

- `setupDone == false` → `SetupWizard` (no destinations, no chrome).
- Nothing else forces a destination. There are no deep links, no
  notification-tap launches into a specific destination, and no home-screen
  widget today.

## 5. Adding a destination — 4 steps

1. Add an enum entry `NAME(Icons.Rounded.X, Icons.Outlined.X, "nav_name")` to
   `NourNavDestination` and extend `getTitle(strings)` with the label. The
   label string goes through `localization/AppStrings.kt` — the default English
   field plus overrides in all 14 language blocks (AGENTS.md §7; `res/values/`
   stays `app_name`-only).
2. Add the `when (destination)` branch in `MainActivity`'s `AnimatedContent`
   body: call the screen with its parameters.
3. Decide its width in the two small `when`s above it (content `maxWidth` —
   `Infinity`/`readingWidth`/`contentWidth` — and `contentPadding`; `SALAH`'s
   zero-padding edge-to-edge treatment is the exception, not the default).
   The bottom reserve arrives automatically via `LocalFloatingBottomExtra`.
4. Tests + docs: add the screen to `ScreenRenderTest` (and to
   `ScreenCaptureMatrixTest` if it has dark/font/RTL variants, following the
   `mNN_` naming there), keep `FloatingChromeTest`'s reserve formula in mind,
   and update the tables in this file by hand.
