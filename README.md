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
* Bounded atmospheric canvas reflecting the time of day between prayer phases.
* Live countdown with progress toward the upcoming Salah.

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
* **Dual Reading Modes**: Verse cards or continuous text, with adjustable Arabic typography.
* **Reading Continuity**: Reading mode, font size, and last read Surah/verse persist across restarts; bookmarks resolve against the bundled text.
* **Honest Availability**: Translations and tafsir are not bundled; the reader says so instead of substituting unverified prose. Search covers the Arabic corpus and Surah names.

### 6. Daily Salah Tracker
* One-tap completion logging for the five prayers with persistent local Room history.

---

## Material 3 Expressive Design

Nour follows a restrained Material 3 Expressive language: shared spacing/dimension/motion tokens, semantic color roles (with Android 12+ dynamic color), readable typography, 48 dp touch targets, wrapping rows that adapt to large font scales, bounded content widths on wide screens, and quiet transitions. Arabic scripture direction is independent of the interface language, and RTL layouts are supported.

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

---

## Privacy-First Architecture

Nour keeps your data on your device:
* **No Analytics or Trackers**: No third-party telemetry or user profiling.
* **Prayer calculations and Qibla bearings run 100% on-device.** (Reverse geocoding of a detected GPS fix may use the platform Geocoder; no Nour-operated servers exist.)
* **Quran text is bundled offline** — no network calls to read.
* **Zero Advertisements.**
* **Transparent Permissions**:
  * `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: On-device computation of prayer times and Qibla bearing. A preset city can be chosen manually without granting location.
  * `SCHEDULE_EXACT_ALARM`: Punctual Adhan notifications at the computed minute.
  * `POST_NOTIFICATIONS`: Delivers Adhan notifications.
  * `RECEIVE_BOOT_COMPLETED`: Re-registers scheduled prayer alarms after reboot.

---

## Getting Started

### Prerequisites
* **JDK 17** or **JDK 21**.
* **Android SDK**: `minSdk 24` (Android 7.0+), `compileSdk 36`.

### Build & Run
1. Open the project in Android Studio and let Gradle sync, or build from the command line with `gradle :app:assembleDebug` (this repository ships without the Gradle wrapper).
2. Install the generated debug APK on a device.

### Running Unit Tests
```bash
gradle :app:testDebugUnitTest
```
Corpus-integrity and pure-logic tests run on any JVM. Robolectric/Compose UI tests require an x86_64 host (they do not run on ARM64 Linux, including Termux).

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details. The bundled Quran text and metadata are from the Tanzil Project under Creative Commons Attribution 3.0, verbatim with the required notices — see `app/src/main/resources/quran/SOURCES.md`.
