# Nour implementation status

> Android 16 (API 36) / Material 3 Expressive refinement pass: see
> `docs/android16-m3e/` for the baseline audit, migration plan, design-system
> specification, test report, and handover.

## Implemented
- Visual foundation follows the user's supplied Prayer App M3E reference (`Download/Prayer App — M3E.html`): muted lavender palette (light: #4A4459 primary, #FCF8FD background, #E6E0F0 primary container; coherent dark counterpart), 28dp feature cards, 22dp list rows, 40dp/14dp-radius icon tiles, bold hero typography.
- Functional appearance settings: theme mode (System/Light/Dark) and wallpaper-based dynamic colors are persisted opt-in controls in Settings (dynamic color now off by default).
- Salah: compact date/location, prominent lavender next-prayer card with large prayer name and live countdown/progress, compact prayer rows with status tile and bold times, real bundled verse card (Ash-Sharh 94:6 via QuranRepository.resolveAyah, no demo text), stationary full-page sky with restrained celestial detail and theme-aware fade.
- Quran: pill search entry, prominent primary-container Continue Reading card with real persisted verse progress (clamped to actual Surah count), compact Surah rows with number tile and right-aligned Arabic.
- Qibla: paired direction/distance cards, compact location pill, calibration guidance; sensor math, honest mode status and manual-slider disclaimer unchanged.
- Settings: appearance group with functional theme controls; existing language/location/method/madhab/adjustments/Adhan settings preserved with grouped styling.
- Shared spacing, dimensions, shape, motion, typography, and content-width tokens; heading/panel/badge/frame/empty-state primitives.
- Main page-title headers removed from Salah, Qibla, Quran and Settings; navigation and functional section labels retained.
- Salah uses a stationary full-page sky behind bounded scrolling content, a responsive upper sky reveal, lowered dunes, and a theme-aware scrim. Dates/location and countdown remain on opaque panels; prayer actions are preserved.
- Existing bottom bar/rail preserved with bounded content and quieter navigation transitions.
- Responsive Qibla dial, themed marker colors, consistent icons, readable hub label, and honest sensor/manual status line (manual slider never claims physical alignment; alignment banner gated on real sensor readings).
- Quran directory/search row reuse, wrapping tabs, larger action targets, accessible verse-selection actions, and scrollable reading preferences.
- Settings radio semantics for Madhab and consistent typography/surfaces; GPS detection failure feedback.
- Verified Quran corpus: complete Tanzil Uthmani 1.1 text (114 surahs, 6,236 verses) bundled with SHA-256 verification at load; all fabricated verse/translation/tafsir/page fallbacks removed; search covers the real corpus. See `app/src/main/resources/quran/SOURCES.md`.
- Durable preferences: prayer location/method/madhab/high-latitude/adjustments and Quran reading mode, Arabic font size, last Surah, and last verse (cards mode) persist across restarts via typed `NourPreferences` over the existing prefs file; appearance (theme mode, dynamic colors) likewise persists; Room schema unchanged.
- Corpus integrity tests (`QuranCorpusTest`) pass: counts, canonical order, page/juz metadata, no fabricated translations/tafsir.

## Preserved
Room schema, prayer calculation, sensor math, and existing user records (logs, bookmarks, khatmah, Adhan settings) are unchanged. Existing destinations and Quran tabs remain unchanged; dormant Mushaf navigation was not connected.

## Not verified / remaining work
- Robolectric/Compose UI tests cannot run in this Termux ARM64 environment (`Robolectric native runtime is not supported on Linux aarch64`); they need x86_64 CI or a device. All other JVM unit tests run.
- Translations and tafsir are intentionally not bundled; reader shows an honest unavailability message. Bundle only sources with verifiable provenance and redistribution permission.
- Celestial art retains its time-of-day palette and existing animation, with a theme-aware foreground scrim. Full-page visual checks at daylight/twilight/night and across screen sizes are still pending; compilation is not visual verification, and no rendered side-by-side comparison against the reference HTML has been performed (no browser/adb available).
- Review all screens at 360/800/1200 dp, dark/light/dynamic themes, RTL, and 200% font scale on a device; verify dropdown anchoring, TalkBack, contrast, keyboard/IME behavior.
- Several existing English labels remain untranslated. Complete localization before release.
- Device acceptance still pending: cold-restart persistence, Arabic/large-font navigation, sensor/manual Qibla calibration states, Adhan with screen off / battery saver / reboot / permission changes.

## Build (Termux, verified working)
`JAVA_HOME=$PREFIX/lib/jvm/java-21-openjdk ANDROID_HOME=~/android-sdk ~/build-tools/gradle-9.3.1/bin/gradle -p '<project>' :app:assembleDebug --no-daemon -Pandroid.aapt2FromMavenOverride=$PREFIX/bin/aapt2 -Dorg.gradle.vfs.watch=false -Dorg.gradle.native=false -Dorg.gradle.jvmargs='-Xmx2g' --max-workers=2`

Backups: pre-M3E `~/nour-before-m3e-20260917-143617`; pre-reliability `~/nour-before-reliability-20260917-174825`.
