# Store listing drafts (Google Play)

Drafts for NUR-040. Only claims verified against the repository are made here
(see `AGENTS.md`, the manifest, and `README.md`): no moon-sighting, fatwa, or
scholarship authority claims; no network features — not even in future tense.
Where Play Console applies hard limits, they are noted.

## 1. Title (≤ 30 characters)

**Nour – Prayer Times & Quran** *(27 characters)*

Alternates:

- `Nour: Prayer, Qibla, Quran` *(26)*
- `Nour (نور) – Prayer & Quran` *(27 characters as measured here — verify in the
  console, since Arabic chars are fine in titles but truncation may look
  wrong)*

Keep the CJK-safe advice: count the final pick in Play Console itself before
submitting.

## 2. Short description (≤ 80 characters)

**Prayer times, Qibla compass, Quran – fully offline, no accounts or ads.**
*(71 characters — verify in console.)*

Alternates:

- `Athan alarms, Qibla compass and the Quran on your device. No internet used.`

## 3. Full description (≤ 4000 characters)

```
Nour is a quiet, privacy-first companion for your daily worship. Everything
runs on your device — there are no accounts, no ads, no analytics, and the
app does not even have permission to use the internet.

PRAYER TIMES THAT WORK OFFLINE
• On-device astronomical calculations — solar declination, equation of time,
  no cloud APIs.
• Twelve calculation authorities: Muslim World League, ISNA, Umm al-Qura,
  Egyptian General Authority, Karachi, Gulf, Qatar, Singapore, France (UOIF),
  Sweden, Tehran, Morocco.
• Shafi'i/Maliki/Hanbali and Hanafi Asr madhab settings, three
  high-latitude rules, per-prayer minute adjustments.
• A calculated Hijri date is shown with the daily schedule.

ADHAN ALARMS
• Exact-alarm scheduling so the call arrives on time even when the device
  sleeps.
• Adhan audio with tone and volume settings, per-prayer toggles.
• After a reboot the remaining prayers are re-scheduled automatically.
• Notification actions: mark as prayed, snooze, dismiss.

QIBLA COMPASS
• Great-circle bearing to the Kaaba in Makkah with distance readout.
• Accelerometer + magnetometer sensor fusion with low-pass filtering and
  magnetic declination compensation.
• An honest compass: the manual slider is a reference heading only, and the
  "facing the Kaaba" confirmation appears only from real calibrated sensor
  readings.
• A solar azimuth cross-check to verify the compass against the sun, plus
  figure-8 calibration guidance.

QURAN READER
• The complete Tanzil Uthmani text (1.1): 114 surahs, 6,236 verses, bundled
  offline and SHA-256 verified when the app loads.
• Verse cards or continuous reading, adjustable Arabic typography.
• Bookmarks, reading plan (khatmah), and last-read position that survive
  restarts.
• Search across the Arabic corpus and surah names.
• What is included is stated plainly: an English translation (Saheeh
  International) is bundled; tafsir and other translations are not, and the
  app says so rather than substituting content. Text source and licenses are
  documented in the app's bundled source notices (`quran/SOURCES.md`).

DAILY TRACKER + QUIET DESIGN
• One-tap mark-as-prayed with a persistent local history.
• A calm sky visualizer follows the time of day.
• Material 3 Expressive interface, 14 languages in one app, right-to-left
  layouts, large font support, and a reduced-motion mode.

PRIVACY, PLAINLY STATED
• No INTERNET permission. Your location never leaves the device.
• Your prayer log, bookmarks and settings live in local storage.
• No accounts to create, nothing to link, nothing to sync.
• Crash logs, if a failure occurs, stay on your device unless you choose to
  share one.

Source attribute: Quran text and metadata from Tanzil (tanzil.net), used
under CC BY 3.0 as shipped; see the app package's `quran/SOURCES.md`.

Nour is an independent tool. It does not claim scholarly authority, and the
Hijri date it displays is calculated, not confirmed by moon sighting.
```

Verify the rendered length in Play Console (the block above is ≈2,900
characters with line breaks, within budget).

## 4. Screenshot guidance

Play Console style rules apply (min 2, max 8 phone screenshots; PNG/JPEG;
portrait 9:16 — use **1080×1920**). Guidance, not files: the capture matrix
(`app/build/screen-verify/`, 31 screens) is the palette; captures are device-
realistic renders, re-shoot on a real device at 1080×1920 for the listing.

| # | Screen | Notes |
|---|--------|-------|
| 1 | Salah day view — sky header + countdown + times card | flagship; show a realistic method/location |
| 2 | Times card close-up with the coming-prayer row highlighted | crop from 1 at full width |
| 3 | Qibla compass — sensor mode with the honest-mode label visible | do not fake the Kaaba-facing banner in manuals |
| 4 | Quran reader, verse cards | show real Uthmani text; no invented verses |
| 5 | Quran reader, continuous mode | |
| 6 | Quran search results | |
| 7 | Settings (appearance / language) | shows the 14-language switcher |
| 8 | Dark theme of 1 | mirrors capture `m01_prayer_dark` |

Rules: light background text from the lists above only — never overlay
marketing claims on scripture; keep captures at 100 % font scale except if a
deliberate accessibility shot is wanted; RTL capture (Arabic interface, mirrors
`m03_prayer_ar_rtl`) may replace #7 to showcase localization.

## 5. Feature graphic brief

- **Size:** 1024×500 px (Play requirement), PNG or JPEG.
- **Style:** opaque surfaces only — solid `#0A7AFF`-seeded blue panels on the
  design system's neutral background; no glass, blur, gradient overlays, or
  translucent chrome (repo rule: opaque-only visual layer).
- **Content:** the Arabic wordmark «نور» plus the app name in Latin script,
  and at most one device mock of the Salah screen; hairline borders, not
  shadows, for separation; keep plenty of safe margin (Play capsules edges).
- **Avoid:** any faith-authority language ("official", "fatwa", moon
  sighting), network-commanded promises ("updates with new recitations", …),
  and blur/glass styling.

## 6. Listing-text policy cautions

- The title must claim nothing beyond what the app does — no "official",
  no certification wording.
- Do not promise moon-sighting correctness, fatwa correctness, or hadith
  collections (the app contains none).
- Do not describe any network feature, even future tense ("cloud backup
  coming soon" is forbidden — the app has no network stack and none planned).
