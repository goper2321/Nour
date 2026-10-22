# AGENTS.md

Working rules for AI sessions in this repo. The product owner is not a professional developer: explain decisions in plain English, make small reversible changes, and never claim something works unless it was verified.

## 1. What this is

**Nour** is a privacy-first Islamic companion app for Android, aimed at Muslims worldwide who want reliable prayer and Quran tools without accounts, ads, or tracking.

- Calculates prayer times on-device, schedules Adhan alarms, provides a sensor-fused Qibla compass, and bundles the full verified Quran (Tanzil Uthmani, 114 surahs / 6,236 verses) for offline reading.
- The bundled English translation (Saheeh International) is present; tafsir and other translations are intentionally **not** bundled. The app must say so honestly rather than invent content.

## 2. Tech stack, architecture, and layout

Single module (`:app`). Kotlin + Jetpack Compose + Material 3, Room via KSP, Gradle 9.3.1 wrapper. No backend. `minSdk 24`, `compileSdk 36`, `targetSdk 36`.

```
app/src/main/java/com/example/
├── MainActivity.kt   # single activity; manual nav (NourNavDestination): bar < NourSize.compactBreakpoint, else rail
├── NourApp.kt        # Application: CrashReporter install, DB init, notification channels, offline alarm window
├── CrashReporter.kt  # writes uncaught crashes to Downloads/NourCrash/ (or filesDir below API 29)
├── database/         # Room v1: PrayerLog, Bookmark, KhatmahPlan, AdhanSettings
├── features/         # adhan, location, prayer, qibla, quran, settings, widgets, onboarding
├── localization/     # AppLanguage (14 locales), AppStrings, LocalizationManager
├── ui/components/    # NourNavigation.kt (floating bar/rail chrome), NourComponents, FloatingChrome
└── ui/theme/         # design tokens: NourSpace, NourSize, NourMotion, NourReduceMotion, SeedColorSchemes
```

- Verified Quran resources: `app/src/main/resources/quran/`.
- Visual reference: `docs/ios27-design-system.md` (current iOS 26/27 opaque pass). `docs/android16-m3e/DESIGN_SYSTEM.md` keeps M3 Expressive migration history; its palette/shape values are superseded. Root `DESIGN_SYSTEM.md` is the original hand-written design brief — read the docs instead.

## 3. Build, run, test, inspect

Prereqs: JDK 17/21 (`JAVA_HOME`) and Android SDK platform 36 + build-tools (`ANDROID_HOME`).

```bash
./gradlew :app:assembleDebug                     # debug APK -> app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest                 # all JVM tests
./gradlew :app:testDebugUnitTest --tests "com.example.QuranCorpusTest"   # one class
```

- **Termux / ARM64 Linux** needs the system aapt2 (Maven's is x86_64-only). Full flag set is in README "Build & Run": `-Pandroid.aapt2FromMavenOverride=$PREFIX/bin/aapt2 --no-daemon -Xmx2g --max-workers=2`.
- **Don't casually "fix" build memory/speed in `gradle.properties`.** The 2.5 GB heap + 512 MB metaspace, `org.gradle.parallel=false`, and in-process Kotlin daemon are deliberate — a previous 4 GB heap was OOM-killed on 8 GB hosts, and skipping in-process compilation causes "Could not connect to Kotlin compile daemon" errors.
- **`tools/nightly-*.sh` are agent-session watchdogs, not app build tools.** A systemd user timer runs `nightly-relaunch.sh` every 2 min: if the heartbeat in `docs/android16-m3e/NIGHTLY_STATUS.md` (runtime-created, with `NIGHTLY_RESUME_PROMPT.md`) is >15 min stale and STATUS isn't DONE/BLOCKED, it relaunches a bounded (90 min) non-interactive OpenCode session. A session launched that way must call `tools/nightly-heartbeat.sh "<step>"` after every step.
- **Release:** env vars `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS` (default `upload`), `KEY_PASSWORD` (documented in `.env.example`; nothing loads `.env` automatically). Without them `assembleRelease` silently falls back to `debug.keystore` — never ship that.
- **Inspect on device:** install the debug APK; crashes land in `Downloads/NourCrash/` (or app `filesDir` pre-API 29). `androidTest` needs an emulator/device. No CI, lint, detekt, ktlint, or spotless config exists — don't assume those gates.

## 4. Existing behavior that must not break

Preserve these; treat changes here as high risk.

- **Prayer calculation** (all methods, madhab Asr variations, high-latitude rules, per-prayer adjustments) and the **Hijri** conversion.
- **Qibla sensor math** and its honest mode reporting: the manual slider is a reference heading only; the "facing the Kaaba" banner must appear only from real calibrated sensor readings.
- **Adhan scheduling** via `PrayerNotificationScheduler` (exact alarms, boot/time/timezone recovery; boot must never fire a prayer notification).
- **Quran reading continuity**: last-read position, bookmarks, reading mode, and font size persist across restarts.
- **Room data** (prayer logs, bookmarks, khatmah, Adhan settings) and the `nour_app_prefs` / `nour_app_preferences` preference keys.
- **Localization** for all 14 locales, **RTL** layouts, and **reduced-motion** behavior.

## 5. Design, accessibility, and product constraints

- **Offline-first, no `INTERNET` permission.** Prayer, Qibla, and Quran run 100% on-device (the GPS fix uses Play Services `FusedLocationProviderClient` on-device, so `play.services.location` being present is expected). Do not add network clients, analytics, Firebase, or the `INTERNET` permission. `app/build.gradle.kts` comments list the deps that were deliberately removed; re-add only with a concrete on-device feature that needs them.
- **Do not fabricate religious content.** Missing translations/tafsir/pages must report "unavailable"; the `corpusErrorMessage` path exists for integrity failures.
- **Opaque-only iOS 26/27 visual layer.** No liquid glass, blur, refraction, or translucent chrome. Floating chrome uses `nourFloatingSurfaceColor()` (solid), cards use `nourCardSurfaceColor()`, hairlines use solid `outlineVariant`. Use semantic shapes (`CardExpressiveShape`, `CardSubtleShape`, `InputExpressiveShape`, `PillShape`) instead of new radii. Route **every animation duration** through `NourMotion.duration(...)` and press feedback through `nourPressScale` so the system "Remove animations" setting is honored (decorative motion stops, nothing disappears).
- Keep **48 dp minimum touch targets**, content descriptions on icon-only controls, wrapping rows/bounded widths that survive large font scales, and Arabic scripture direction independent of interface language.

## 6. Safe editing rules

- **User data is the user's.** Room is at schema version 1 with `exportSchema = false` and no migrations: touching entities requires a version bump plus a real migration, or existing logs/bookmarks are lost or the app crashes. Never clear or rewrite the DB or preferences to "fix" a bug.
- **Dependencies:** change only `gradle/libs.versions.toml` and only when a feature requires it. Keep the app offline.
- **Don't commit secrets or local files:** `.env`, `debug.keystore`, `.claude/`, `.obsidian/` are gitignored. Back up big changes before large refactors.
- Prefer the smallest complete change. Don't rewrite working engines or screens to satisfy a small request.

## 7. Verification and honest reporting

- Run `./gradlew :app:testDebugUnitTest` after code changes and report the actual result. Pure-JVM tests (`QuranCorpusTest`, `CelestialMathTest`, `SeedSchemeTest`, `PrayerCalculationTest`, `QuranSearchTest`, `AppStringsIntegrityTest` — 23 tests) run on any host. Judge runs by `BUILD SUCCESSFUL` — a worker may print a non-fatal KSP/AWT `NullPointerException` in the log.
- **Robolectric tests** (`ExampleRobolectricTest`, `NourDesignSystemTest`, `SetupWizardTest`, `ScreenRenderTest`, `ScreenCaptureMatrixTest` — 52 tests) need x86_64 Linux or macOS. `app/build.gradle.kts` excludes those classes on Linux aarch64 with a loud log line — so a green run on ARM does **not** mean they passed.
- **Screenshot verification:** `ScreenRenderTest` and `ScreenCaptureMatrixTest` render screens on the JVM and, with `-Proborazzi.test.record=true`, write PNGs to `app/build/screen-verify/` (29 screens). The agent cannot view images: check gutters/edges with pixel metrics, never claim visual approval. Note `assertTextContains` defaults to exact match — pass `substring = true` for partial text.
- **Any change to `app/src/main/resources/quran/` files requires updating the matching `*_HASH` constant in `QuranCorpus.kt`** (`TEXT_HASH`, `METADATA_HASH`, `EN_SAHIH_HASH`), or the app asserts at startup. The Uthmani text must not be modified; provenance is in `quran/SOURCES.md`.
- **Localization:** user-facing Compose text lives in `localization/AppStrings.kt` (one `AppStrings().apply { … }` block per language, 14 total), separate from `res/values/strings.xml` (which only holds `app_name`). New fields default to English; translate by adding an override per language block. All 14 locales deliberately ship in one APK (`bundle.language.enableSplit = false` in `app/build.gradle.kts`): if the bundle split per device language, a user picking an app language not on their device could get a split without the strings the in-app switcher needs. Do not re-enable language splits.
- State clearly what is **confirmed** (command run, output seen) versus **assumed** or **unverified**. Visual/device checks (themes, RTL, 200% font scale, TalkBack, Qibla sensors, Adhan with screen off) require a physical device — say "not verified" rather than implying success from a compile.

## Open questions / needs verification

- Whether the `:app` release build has been signed with a real upload keystore on any CI.
- Full device acceptance (cold-restart persistence, sensor calibration states, Adhan under Doze/battery saver, RTL, 200% font scale, TalkBack) is still pending per `docs/ios27-design-system.md`.
- The hand-authored default palette in `Color.kt` and the seed-generated palette in `DynamicColor.kt` still differ (grouped page tone); unifying them is a palette decision left for a separate change.