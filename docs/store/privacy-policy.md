# Nour privacy policy

Draft for the Google Play listing's "Privacy policy" link. Plain language on
purpose — this describes the app as it is built today (verified against the
Android manifest, `CrashReporter.kt`, `docs/`, and `AGENTS.md`). The owner
reviews once before upload.

## What Nour is

Nour (نور, "Light") is an Islamic companion app for Android: prayer times,
Adhan alarms, a Qibla compass, and a Quran reader. It works completely without
a network connection.

## What we collect: nothing

Nour has no accounts, no analytics, no trackers, no ads, and no cloud backend.
The app does not even have the Internet permission, so it has no way to send
your data anywhere. Nour operates no servers, and it never learns who uses it
or where.

## Your data stays on your device

Everything the app knows lives in local storage on your phone:

- **Prayer log** — prayers you mark as prayed (local Room database).
- **Bookmarks and khatmah plans** — your saved places and reading plan.
- **Reading state** — last-read surah/verse, reading mode, font size.
- **Preferences** — your chosen language, theme, calculation method, madhab,
  adjustment minutes, location choice, numerals and time format, Adhan
  settings.

Deleting the app removes this data from the device as normal Android app
uninstallation.

## Location

To compute prayer times and the Qibla for *your* coordinates, the app can
determine where the device is. This fix is obtained on-device (Play Services
Fused Location, or a manually chosen city if you do not grant location
permission), and reverse-geocoding a fix may use the platform Geocoder — i.e.
Android itself, not a Nour-operated service. Your coordinates are used for
arithmetic only. They are never transmitted: with no Internet permission there
is nothing to transmit them with.

## Android backups

Nour's data may be copied by **Android's own** backup and device-transfer
services if you, the device owner, have them enabled. The app's backup rules
allow exactly two areas to be backed up: app preferences and the local
database — nothing else, and Nour itself performs no networking of any kind.
This handling is documented here honestly; it may be revisited in a future
build if this policy changes.

## Crash logs

If the app crashes, the crash reporter writes a plain-text log so the failure
can be understood later: timestamp, app version, thread name, the exception
and its stack trace. The file is stored locally — on Android 10+ in
`Downloads/NourCrash/nour-crash-….log`, below that in the app's private
storage. Nour sends crash logs nowhere. You can read the file yourself, and if
you *choose* you may share it (for example by email or via a bug report you
file) — sharing is always your decision.

## Permissions, and why each exists

The manifest declares exactly these permissions:

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | on-device prayer-time and Qibla computation; optional — a preset city works without it |
| `SCHEDULE_EXACT_ALARM` | punctual Adhan notifications at the computed minute |
| `POST_NOTIFICATIONS` | delivers Adhan notifications |
| `FOREGROUND_SERVICE` (+ media playback type) | Adhan audio playback while the device sleeps |
| `VIBRATE`, `WAKE_LOCK` | keep alarm delivery and sound reliable when the screen is off |
| `RECEIVE_BOOT_COMPLETED` | re-register your scheduled prayer alarms after a reboot (booting never fires a prayer notification) |

There is no `INTERNET` permission.

## Languages

Nour ships all 14 interface languages in one app, and the language switcher is
in-app; your language choice is stored on the device.

## What this policy is not

It is not a guarantee of religious correctness. Prayer times are astronomical
calculations using the calculation methods you choose; the Hijri date is
calculated from the calendar day, not confirmed by local moon sighting.

## Changes and contact

If the app's data handling ever changes, this file changes with it — Nour has
no mechanism to start collecting silently (no network, no remote config).

Questions about the data the app keeps: <contact-email — owner adds a real
address before publishing>.
