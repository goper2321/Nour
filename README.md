# Nour (نور) — Islamic Companion for Android

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203%20Expressive-000000.svg?style=flat)](https://m3.material.io/)
[![Room](https://img.shields.io/badge/Database-Room%20(KSP)-F58220.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Offline First](https://img.shields.io/badge/Privacy-Offline--First-09090B.svg?style=flat)](#privacy-first-architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Nour** (*Light* / *نور*) is a privacy-first Islamic companion application built natively with modern **Android, Jetpack Compose**, and **Material 3 Expressive design**.

Nour calculates prayer times locally with an on-device astronomical engine, schedules exact-alarm Adhans, provides a sensor-fused Qibla compass with solar cross-check, and includes a Quran reader with the complete, verified Tanzil Uthmani text (114 Surahs, 6,236 verses) bundled offline.

---

## Key Features

### 1. Offline Astronomical Prayer Times
* **On-device calculation**: Solar declination and equation-of-time equations run entirely locally — no cloud APIs.
* **Global Calculation Authorities**: Muslim World League (MWL), ISNA, Umm al-Qura, Egyptian General Authority, Karachi, Gulf/Qatar, Singapore, France (UOIF), Sweden, Tehran, Morocco.
* **Madhab Asr Variations**: Shafi'i / Maliki / Hanbali (shadow factor 1) and Hanafi (shadow factor 2).
* **High-Latitude Handling**: Angle-based, Middle-of-the-Night, and One-Seventh rules.
* **Hijri Calendar**: Daily conversion displayed with the prayer schedule and per-prayer minute adjustments.

### 2. Celestial Sky Visualizer
* **Atmosphere, not illustration**: the Salah hero is a bounded sky canvas driven by the calculated prayer times. Eleven continuous phases (Tahajjud night → pre-dawn → Fajr twilight → dawn → sunrise → morning → noon → afternoon → Maghrib → dusk → night) blend across the sky gradient, horizon glow, drifting clouds and layered dunes. There are no sun rays, sparkles, diamond stars or shooting stars — the hero reads as the app's own atmosphere.
* **True sun and moon**: the sun disc and a true-phase moon are placed from their real hour angle and altitude for your location and time, so the moon shows its actual lit fraction and a daytime moon can share the sky with the sun.
* **Live countdown**: time and progress toward the upcoming Salah. The digits sample the sky directly behind them and fall back to a neutral text colour when the sky cannot carry 3:1 contrast.
* **Reduced motion**: with *Remove animations*, cloud drift, star twinkle and the halo pulse settle on a still frame — the sky keeps its colours, it just stops moving.

### 3. Adhan Alarms
* **Exact Scheduling**: `AlarmManager.setExactAndAllowWhileIdle()` for on-time prayer calls while the device sleeps (subject to the device's exact-alarm permission).
* **Boot Recovery**: `RECEIVE_BOOT_COMPLETED` reschedules remaining prayers from persisted preferences; boot never fires a prayer notification.
* **Adhan Notifications**: Prayer notifications with one-tap *Mark as Prayed* and *Snooze* actions, per-prayer Adhan toggles, tone, and volume in Settings.

### 4. Qibla Compass
* **Great-Circle Bearing**: Spherical-trigonometry bearing from your coordinates to the Kaaba in Makkah (`21.4225° N, 39.8262° E`), with distance.
* **Sensor Fusion**: Live dial from accelerometer + magnetometer (rotation vector fallback), low-pass filtering, and magnetic-declination compensation.
* **Honest Modes**: The screen always states whether you are in Sensor or Manual mode. The manual slider is a reference heading only — the app never claims your phone physically faces the Qibla from a simulated heading, and the alignment banner only appears from real, calibrated sensor readings.
* **Solar Azimuth Cross-Check**: Secondary solar indicator to verify compass orientation against the physical sun, plus figure-8 calibration guidance.

### 5. Quran Reader (verified Tanzil Uthmani text)
* **Complete Verified Corpus**: The full Uthmani text (Tanzil 1.1, 114 Surahs, 6,236 verses) is bundled verbatim and SHA-256-verified at load, with real page/juz metadata. See `app/src/main/resources/quran/SOURCES.md` for license and attribution.
* **Surah Pager**: Swipe left/right between all 114 surahs with one fixed toolbar; each surah remembers its own scroll position, so returning restores the verse you were reading.
* **Dual Reading Modes**: Verse cards or continuous text, with adjustable Arabic typography.
* **Go to Verse**: Jump to any surah/ayah from the reader; typed numbers accept Western and Arabic-Indic digits, and the landed verse is highlighted until the next tap.
* **Reading Continuity**: Last read surah/verse, reading mode, font size, reading theme, and typeface persist across restarts; a transient "last read" marker shows where you resumed. Bookmarks resolve against the bundled text.
* **Reading Themes**: System, warm Paper, or low-glare Night — Paper/Night tint only the cards while the page and the gaps between cards keep the app theme; fully opaque and WCAG AA-checked.
* **Optional Amiri Quran**: An opt-in Quranic Naskh face for Arabic scripture only, bundled with its full SIL OFL 1.1 license; labels, numbers and translations keep the app typeface.
* **Honest Availability**: Translations and tafsir are not bundled; the reader says so instead of substituting unverified prose. Search covers the Arabic corpus and Surah names.

### 6. Daily Salah Tracker
* One-tap completion logging for the five prayers with persistent local Room history.

---

## Material 3 Expressive Design

Nour follows a restrained Material 3 Expressive language: shared spacing/dimension/motion tokens, semantic color roles (with Android 12+ dynamic color), readable typography, 48 dp touch targets, wrapping rows that adapt to large font scales, bounded content widths on wide screens, and quiet transitions. Arabic scripture direction is independent of the interface language, and RTL layouts are supported.

The current visual layer is an iOS 26/27-inspired, **opaque-only** refinement on top of that foundation: grouped surfaces, floating opaque navigation (bottom bar on compact windows, rail on wide ones), circular compact actions, capsule buttons, and a generated Apple-neutral blue palette. It deliberately avoids liquid glass, blur, and translucent chrome. See [`docs/ios27-design-system.md`](docs/ios27-design-system.md) for the tokens, component rules, screen guidance, and verification status.

The Salah sky hero is the one full-bleed surface. It ends on `ImmersiveHeaderShape` (24 dp bottom corners) so it meets the grouped page like every other surface, and it absorbs the page's leftover height so the day switcher and times card land on the content reserve instead of leaving a gap — at large font scales or in short windows it keeps its natural height and the page scrolls.

## Accessibility

* **Reduced motion**: when the system animation scale is 0 (*Remove animations* /
  developer setting), decorative motion is disabled app-wide. The sky
  visualizer, the wizard soundwave indicator and the Qibla spring settle on
  their final state instead of looping — nothing disappears, it just stops
  moving. Implemented in `ui/theme/Motion.kt`; every animation goes through
  `NourMotion.duration(...)`.
* **Screen readers**: icon-only controls carry content descriptions, clear
  buttons in the setup wizard are labelled, and purely decorative icons use
  `contentDescription = null` so they are not announced twice.
* **Touch targets** are 48 dp minimum.
* **Layout** uses wrapping rows and bounded content widths, so it survives large
  font scales and small screens.

---

## Architecture & Tech Stack

```
app/src/main/java/com/example/
├── MainActivity.kt                 # Single Activity entry point & navigation host
├── NourApp.kt                      # Application class, notification channel setup
├── database/                       # Local Room Persistence
│   ├── Entities.kt                 # PrayerLog, Bookmark, KhatmahPlan, AdhanSettings
│   ├── Daos.kt                     # Type-safe Room DAOs
│   └── NourDatabase.kt             # SQLite database instance with KSP
├── features/
│   ├── adhan/                      # Adhan scheduling & audio playback
│   ├── location/                   # Device location & permission handling
│   ├── prayer/                     # Prayer calculation engine, models & UI
│   ├── qibla/                      # Qibla compass engine & sensor manager
│   ├── quran/                      # Verified Quran corpus, models & reader UI
│   ├── settings/                   # Durable preferences store & settings UI
│   └── widgets/                    # Sky visualizer
├── localization/                   # App strings & language management
└── ui/theme/                       # Design-system tokens, shapes, type & theme
```

### Core Libraries & Tools
* **Jetpack Compose (BOM 2024.09.00)** with Material 3.
* **Room Database** with KSP: prayer logs, bookmarks, khatmah plans, Adhan settings.
* **Coroutines & StateFlow** for reactive state.
* **Quran text**: [Tanzil](https://tanzil.net/) Uthmani 1.1, used verbatim under CC BY 3.0 with the required notice preserved.
* **Amiri Quran**: optional scripture typeface by The Amiri Project Authors, bundled under the SIL Open Font License 1.1 (full text in `app/src/main/res/raw/amiri_quran_ofl.txt`).

---

## Privacy-First Architecture

Nour keeps your data on your device:
* **No Analytics or Trackers**: No third-party telemetry or user profiling.
* **Prayer calculations and Qibla bearings run 100% on-device.** (Reverse geocoding of a detected GPS fix may use the platform Geocoder; no Nour-operated servers exist.)
* **Quran text is bundled offline** — no network calls to read.
* **Zero Advertisements.**
* **No cloud backup / no sync**: `android:allowBackup="false"`. Bookmarks,
  prayer log, khatmah plans and preferences stay on this device only —
  uninstalling loses them. (A future in-app export feature could cover
  backups; not built yet.)
* **Transparent Permissions**:
  * `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: On-device computation of prayer times and Qibla bearing. A preset city can be chosen manually without granting location.
  * `SCHEDULE_EXACT_ALARM`: Punctual Adhan notifications at the computed minute.
  * `POST_NOTIFICATIONS`: Delivers Adhan notifications.
  * `RECEIVE_BOOT_COMPLETED`: Re-registers scheduled prayer alarms after reboot.

### Location source strategy (de-Googled ROMs)

Location affects only prayer times and the Qibla bearing, so Nour never needs
a server and never requests the INTERNET permission. The source chain is:

1. **Fused provider (Google Play Services)** — used only when play services
   report a healthy connection.
2. **Android platform `LocationManager.GPS_PROVIDER`** — the fallback for
   phones running GrapheneOS/CalyxOS or any ROM without GMS, and whenever the
   fused call errors, times out, or returns no fix.
3. **Android platform `LocationManager.NETWORK_PROVIDER`** — when GPS is
   disabled or has no fix.
4. **Manual entry** — supported today: Settings → "Select City" (and the same
   city picker in the onboarding wizard) choose from holy cities plus the full
   world/preset list with IANA-timezone UTC offsets, fully offline.

While a platform provider supplies the fix, the location card shows an honest
"Using pure device GPS" state instead of implying fused accuracy. Decision
logic is unit-tested in `LocationSourceResolverTest`; storage docs live in
`docs/reading-continuity.md` for Quran position persistence and this file's
Privacy section above for the permission rationale.

---

## Getting Started

### Prerequisites
* **JDK 17** or **JDK 21** (`JAVA_HOME` must point at it).
* **Android SDK** with platform 36 and build-tools (`ANDROID_HOME` must point at it): `minSdk 24` (Android 7.0+), `compileSdk 36`.
* Gradle comes from the checked-in wrapper (`./gradlew`, Gradle 9.3.1), so no separate Gradle install is needed.

### Build & Run
```bash
# Debug APK
./gradlew :app:assembleDebug

# Termux / ARM64 Linux (uses the system aapt2; Maven's aapt2 is x86_64-only)
JAVA_HOME=$PREFIX/lib/jvm/java-21-openjdk ANDROID_HOME=~/android-sdk \
  ./gradlew :app:assembleDebug --no-daemon \
  -Pandroid.aapt2FromMavenOverride=$PREFIX/bin/aapt2 \
  -Dorg.gradle.vfs.watch=false -Dorg.gradle.native=false \
  -Dorg.gradle.jvmargs='-Xmx2g' --max-workers=2
```
Install the resulting APK from `app/build/outputs/apk/debug/` on a device.

### Release build & signing
Release signing reads (in order of preference):
* `KEYSTORE_PATH` — path to an upload keystore file,
* `STORE_PASSWORD`, `KEY_PASSWORD`, and optional `KEY_ALIAS` (default `upload`).

```bash
KEYSTORE_PATH=/path/to/my-upload-key.jks STORE_PASSWORD='…' KEY_PASSWORD='…' \
  ./gradlew :app:assembleRelease
```
If no upload keystore is configured, `assembleRelease` fails immediately with
an error naming the signing variables — the old silent `debug.keystore`
fallback was removed on purpose. Debug builds are unaffected.

### Running Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```
Pure-JVM tests run on any host: 108 `@Test`s across 21 classes, one of which
(`MigrationTest`) is `@Ignore`d pending the real Room migration (so the suite
shows 1 skipped).

The Robolectric-driven tests need an **x86_64 Linux or macOS** host: 100 `@Test`s
across 12 classes (`ExampleRobolectricTest`, `NourDesignSystemTest`,
`SetupWizardTest`, `ScreenRenderTest`, `ScreenCaptureMatrixTest`,
`FloatingChromeTest`, `NourPreferencesRoundTripTest`,
`SettingsDiagnosticsCardsTest`, `ReducedMotionBarEndpointsTest`,
`QuranScriptureBoxTest`, `DaySwitcherSwipeTest`, `QuranReaderTest`).
Suite total: **108 + 100 = 208 tests, of which 1 is skipped** (`MigrationTest` —
207 execute, 0 failures; measured 2026-10-26). Judge results by
`BUILD SUCCESSFUL`.

### Testing on different hosts

**ARM64 Linux (Termux):** the Robolectric classes are excluded automatically.
Robolectric 4.15+ needs its `nativeruntime` shared library, which is published
only for linux-x86_64 (plus macOS); on Linux aarch64 every Robolectric test
would abort with
`The Robolectric native runtime is not supported on Linux (aarch64)`.
`app/build.gradle.kts` detects the host (Linux + aarch64/arm64), excludes
exactly the `src/test` classes discovered by source to use
`RobolectricTestRunner`, and prints a loud line of the form:
`Nour: Robolectric is not supported on Linux/aarch64 - skipping N Robolectric test class(es): …`
**A green local run on ARM proves only the 108 pure-JVM tests (107 execute,
1 skipped) — it does not prove the full suite.** Exercise the 100 Robolectric
tests on CI or an x86_64/macOS machine.

**x86_64 Linux / macOS:** nothing is excluded; all 208 tests run (207 execute +
1 skipped).

Screenshot captures are part of those Robolectric tests (`ScreenRenderTest`
and `ScreenCaptureMatrixTest` render 36 screens on the JVM). After any visual
change, re-record per the recipe in AGENTS.md §3/§7:

```bash
./gradlew :app:testDebugUnitTest -Proborazzi.test.record=true --no-build-cache
```

PNGs land in `app/build/screen-verify/`; treat the absence of a fresh PNG as a
failed capture, and compare changed screens with pixel metrics — never claim a
visual pass by eye.

> Do not "fix" build slowness on any host by touching `gradle.properties`
> memory/speed flags: the 2.5 GB heap, 512 MB metaspace, `parallel=false` and
> the in-process Kotlin daemon are deliberate (AGENTS.md §3). On Termux, use
> the documented aapt2-override flags from *Build & Run* above instead.

### Permissions (why each exists)
Nour is offline-first and declares **no `INTERNET` permission**. Prayer times,
Qibla bearings, and the Quran reader run 100% on-device.
* `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: on-device prayer-time and Qibla computation. A preset city can be chosen manually without granting location.
* `SCHEDULE_EXACT_ALARM`: punctual Adhan notifications at the computed minute.
* `POST_NOTIFICATIONS`: delivers Adhan notifications.
* `RECEIVE_BOOT_COMPLETED`: re-registers scheduled prayer alarms after reboot.
* `FOREGROUND_SERVICE` (+ `mediaPlayback` type), `VIBRATE`, `WAKE_LOCK`: reliable Adhan audio playback while the device sleeps.

### Configuration
There are no API keys, feature flags, or required environment variables for
normal use. The offline corpus (`app/src/main/resources/quran/`) is
SHA-256-verified at load; see `SOURCES.md` there for provenance and licenses.

### Known limitations
* Robolectric/Compose UI tests need an x86_64 host or CI; they are skipped automatically on ARM64 (see above).
* Translations beyond the bundled Saheeh International English text and tafsir are intentionally not bundled; the reader says so instead of substituting unverified prose.
* Full visual verification (daylight/twilight/night sky, 360/800/1200 dp widths, dark/light/dynamic themes, RTL, 200% font scale, TalkBack pass) requires a physical device.
* Newly added strings (exact-alarm status, Adhan voice descriptions, corpus-error message) currently fall back to English in non-English locales until translated.
* `release` builds are unsigned-for-production unless the signing environment variables are set (see Release build & signing above).

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details. The bundled Quran text and metadata are from the Tanzil Project under Creative Commons Attribution 3.0, verbatim with the required notices — see `app/src/main/resources/quran/SOURCES.md`. The optional Amiri Quran typeface is copyright The Amiri Quran Project Authors and licensed under the SIL Open Font License 1.1; the full license is bundled at `app/src/main/res/raw/amiri_quran_ofl.txt`.
