# `:core` module split — plan only (not implemented)

**Status: proposal.** No build file, source file, or directory in this repo has
been changed to advance this split. `settings.gradle.kts` still contains only
`include(":app")`. This document exists so the decision, the mapping, and the
migration recipe do not have to be re-derived when the trigger below fires.

## Why an eventual split

`:app` currently compiles everything in one unit: pure astronomy/Quran math,
Room entities, Android framework code, sensors, and all Compose screens. The
costs surface when CI grows (NUR-003/025), when iteration on UI should not
recompile the prayer engine, and when a second surface (e.g. a real home-screen
widget or a wear flavor) would want the engines without Compose.

## Target shape

Five Gradle modules. `:app` remains the only Android-UI module and depends on
all four core modules.

| Module | Moves (current path under `app/src/main/java/com/example/`) | Verified deps today | Notes |
|--------|----------------------------------------------------------------|---------------------|-------|
| `:core:calculation` | `features/prayer/calculation/` (PrayerCalculationEngine, CelestialMath, HijriDateHelper), `features/prayer/models/` | kotlin.math, java.text, java.util only — no Android, no kotlinx | Pure JVM; unit tests `CelestialMathTest`, `PrayerCalculationTest` move with it |
| `:core:quran` | `features/quran/data/` (QuranCorpus, QuranRepository), `features/quran/models/` | javax.xml.parsers, org.w3c.dom, java.security, java.util — pure JVM | Bundled corpus moves too: `app/src/main/resources/quran/` → the module's `src/main/resources/quran/` (loaded via `javaClass.getResourceAsStream("/quran/…")`, so a classloader-based read keeps working unchanged). `QuranCorpusTest`, `QuranSearchTest` move with it |
| `:core:database` | `database/` (Entities, Daos, NourDatabase) | androidx.room (2.7.0 via `libs.versions.toml`), kotlinx.coroutines, android.content.Context | KSP + Room compiler plugin apply to this module only after the split |
| `:core:localization` | pure parts of `localization/` (`AppStrings`, `AppLanguage`) | none (pure Kotlin) | **Not i18n strings from `res/`** — `res/values/strings.xml` stays `app_name`-only. See the wrinkle below |
| `:app` | `MainActivity`, `NourApp`, `CrashReporter`, `ui/theme`, `ui/components`, `features/prayer/ui`, `features/qibla/`, `features/location/` (Play Services + Geocoder), `features/adhan/` (receivers, exact alarms, audio service), `features/settings/` (SharedPreferences), `features/widgets/` (Compose sky art, *not* an OS widget), `features/onboarding/`, and the Compose halves of `localization/` | Compose, framework, coroutines | Keeps its current Robolectric host gate |

## Dependency direction

```
:core:calculation   ─┐
:core:quran          ├─→  :app  (Compose UI, chrome, widgets, notifications)
:core:database      ─┤
:core:localization  ─┘
```

Today the coupling already runs this way — e.g. `NourPreferences` (settings)
imports `features.prayer.models.*` — so no dependency is invented. No core
module may import another core module unless a concrete need appears
(`:core:database` → none planned; assume none).

## Known wrinkles to resolve before moving anything

- **`:core:localization` is mixed today.** `LocalizationManager` and
  `NourApp`-side code need `android.content.Context`, and the Compose
  `CompositionLocal`s are defined in `LocalizationManager.kt`/`Numerals.kt`
  (`LocalAppStrings`, `LocalUseEasternNumerals`, …). The carve is: pure string tables + `AppLanguage`
  move to the core module; the CompositionLocals and the `Context`-using
  manager stay in `:app` for now. Don't pull compose-runtime into core just to
  avoid that one move.
- **Qibla math does not move yet.** `QiblaCalculator.kt` imports
  `android.hardware.*` (sensors, GeomagneticField) and `Context`, so it is
  framework code until a pure-bearing helper is extracted. Keep it in `:app`
  and treat "extract the pure bearing trig" as the first PR *after* the split,
  if ever.
- **The Robolectric exclusion gate is per-module.** `app/build.gradle.kts`
  discovers Robolectric classes by walking the module's own `src/test`. Robolectric
  test files that stay in `:app` need nothing; if a class ever moves, the moved
  module needs the same exclusion pattern (x86_64-only runtime).
- **Room schema export (NUR-021)** should land first so `schemas/` is in the
  repo *before* the database module moves — wiring the schema location is
  simpler in a single module.

## Migration recipe (when triggered)

1. Edit `settings.gradle.kts`: `include(":app", ":core:calculation", ":core:quran", ":core:database", ":core:localization")`.
2. Give each core module a minimal build script: plain
   `kotlin("jvm")` + `java` for the three pure modules; `:core:database` gets
   the aliases `androidx-room-runtime/ktx/compiler` from `libs.versions.toml`
   plus KSP (version catalog stays the single dependency source — AGENTS.md §6).
3. Move sources in **two independent PRs**: (a) move the packages while keeping
   their existing package names (`com.example.features.prayer.calculation`,
   …) — zero import churn; (b) optionally remap to `com.example.core.*` in a
   separate, mechanical commit. Recommended: do (a) only unless a second
   application surface appears.
4. Move the corpus directory with `:core:quran` and run `QuranCorpusTest`
   (`*_HASH` constants must remain untouched — the corpus bytes move, they do
   not change; AGENTS.md §7).
5. Point `:app` at the modules with `implementation(project(":core:…"))`;
   delete the moved source dirs. Runtime behavior (Room schema, hashes, prefs
   keys) must be byte-for-byte identical — the 23 pure-JVM tests plus the
   Room-heavy Robolectric tests are the regression net.
6. Update `AGENTS.md` §2 tree and this document's status line together.

Throughout: minSdk 24 stays per-module consistent; no new releases, no
publishing config, no Gradle memory-flag changes.

## This split starts when: …

> **(Owner fills this in — it is the gate, and nothing happens before it.)**
>
> Candidates from the review: the Quran feature grows a second engine (e.g.
> reader pagination or tajweed metadata) that would roughly double `:core:quran`
> size; CI build times become the complaint, not a nice-to-have metric; or a
> real second surface (home-screen widget, wear) needs the engines without
> Compose.
>
> Not a trigger: "feels cleaner." Do nothing before the Room migration
> machinery (NUR-021) is in master — moving `database/` with no schema export
> on disk invites a silent migration gap.

`Trigger agreed: ______ (date + signature)`
