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
└── ui/theme/         # design tokens: NourSpace, NourSize, NourMotion, NourReduceMotion, SeedColorSchemes
```

- Verified Quran resources: `app/src/main/resources/quran/`.
- `DESIGN_MIGRATION.md` tracks M3 Expressive implementation status and remaining device-verification work.

## 3. Build, run, test, inspect

Prereqs: JDK 17/21 (`JAVA_HOME`) and Android SDK platform 36 + build-tools (`ANDROID_HOME`).

```bash
./gradlew :app:assembleDebug                     # debug APK -> app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest                 # all JVM tests
./gradlew :app:testDebugUnitTest --tests "com.example.QuranCorpusTest"   # one class
```

- **Termux / ARM64 Linux** needs the system aapt2 (Maven's is x86_64-only). Full flag set is in README "Build & Run": `-Pandroid.aapt2FromMavenOverride=$PREFIX/bin/aapt2 --no-daemon -Xmx2g --max-workers=2`.
- **Release:** env vars `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS` (default `upload`), `KEY_PASSWORD`. Without them `assembleRelease` silently falls back to `debug.keystore` — never ship that.
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

- **Offline-first, no `INTERNET` permission.** Prayer, Qibla, and Quran run 100% on-device. Do not add network clients, analytics, Firebase, or the `INTERNET` permission. `app/build.gradle.kts` lists deps that were deliberately removed; re-add only with a concrete on-device feature that needs them.
- **Do not fabricate religious content.** Missing translations/tafsir/pages must report "unavailable"; the `corpusErrorMessage` path exists for integrity failures.
- Restrained **Material 3 Expressive**: use the shared tokens in `ui/theme/` instead of magic numbers. Route **every animation duration** through `NourMotion.duration(...)` so the system "Remove animations" setting is honored (decorative motion stops, nothing disappears).
- Keep **48 dp minimum touch targets**, content descriptions on icon-only controls, wrapping rows/bounded widths that survive large font scales, and Arabic scripture direction independent of interface language.

## 6. Safe editing rules

- **User data is the user's.** Room is at schema version 1 with `exportSchema = false` and no migrations: touching entities requires a version bump plus a real migration, or existing logs/bookmarks are lost or the app crashes. Never clear or rewrite the DB or preferences to "fix" a bug.
- **Dependencies:** change only `gradle/libs.versions.toml` and only when a feature requires it. Keep the app offline.
- **Don't commit secrets or local files:** `.env`, `debug.keystore`, `.claude/`, `.obsidian/` are gitignored. Back up big changes before large refactors (see the backup paths noted at the end of `DESIGN_MIGRATION.md`).
- Prefer the smallest complete change. Don't rewrite working engines or screens to satisfy a small request.

## 7. Verification and honest reporting

- Run `./gradlew :app:testDebugUnitTest` after code changes and report the actual result. Pure-JVM tests (`QuranCorpusTest`, `CelestialMathTest`, `SeedSchemeTest`, `PrayerCalculationTest`) run on any host.
- **Robolectric tests** (`ExampleRobolectricTest`, `NourDesignSystemTest`, `SetupWizardTest`) need x86_64 Linux or macOS. `app/build.gradle.kts` excludes those classes on Linux aarch64 with a loud log line — so a green run on ARM does **not** mean they passed.
- **Screenshot verification:** `ScreenRenderTest` renders each screen on the JVM and, with `-Proborazzi.test.record=true`, writes PNGs to `app/build/screen-verify/` (copied to `~/Downloads/screen-verify/` for the owner). The agent cannot view images: check gutters/edges with pixel metrics, never claim visual approval. Note `assertTextContains` defaults to exact match — pass `substring = true` for partial text.
- **Any change to `app/src/main/resources/quran/` files requires updating the matching `*_HASH` constant in `QuranCorpus.kt`**, or the app asserts at startup. The Uthmani text must not be modified; provenance is in `quran/SOURCES.md`.
- **Localization:** user-facing Compose text lives in `localization/AppStrings.kt` (one `AppStrings().apply { … }` block per language), separate from `res/values/strings.xml` (which only holds `app_name`). New fields default to English; translate by adding an override per language block.
- State clearly what is **confirmed** (command run, output seen) versus **assumed** or **unverified**. Visual/device checks (themes, RTL, 200% font scale, TalkBack, Qibla sensors, Adhan with screen off) require a physical device — say "not verified" rather than implying success from a compile.

## Open questions / needs verification

- Whether the `:app` release build has been signed with a real upload keystore on any CI.
- Full device acceptance (cold-restart persistence, sensor calibration states, Adhan under Doze/battery saver) is still pending per `DESIGN_MIGRATION.md`.
