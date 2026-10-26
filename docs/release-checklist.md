# Release checklist (NUR-031)

Owner-only: the agent prepares and documents; the owner runs the release build
with a real upload keystore (see `docs/release-signing.md`).

## Build command

```bash
KEYSTORE_PATH=/path/to/upload.jks \
STORE_PASSWORD='…' \
KEY_ALIAS=upload \
KEY_PASSWORD='…' \
./gradlew :app:assembleRelease
```

Without those env vars the build now FAILS fast (NUR-002 guard) — never a
silent debug-signed fallback.

## Minified build sanity (first minified build)

1. `isMinifyEnabled = true` + `isShrinkResources = true` are set in the
   `release` build type only (`app/build.gradle.kts`).
2. Keep rules live in `app/proguard-rules.pro` (Compose, Material, Room,
   Adhan, widgets, Quran, line numbers for crash reports).
3. **FIRST minified build must be checked for a startup crash before anything
   ships** — install on a device and launch cold.

## Release test matrix (owner runs on a real device)

- [ ] Release APK installs; cold start reaches Salah with no crash.
- [ ] Adhan fires with screen off (Doze + battery saver enabled).
- [ ] Boot recovery: reboot → no ghost prayer notification, alarms re-booked.
- [ ] Timezone/time change: alarms re-schedule.
- [ ] Qibla: compass listens, bearing updates.
- [ ] Quran: search, open surah, read to end, font adjust, kill+restart keeps position.
- [ ] Hijri date computes.
- [ ] Crash reporter: force a crash (dev trigger), file appears in
      `Downloads/NourCrash/`, shareable.
- [ ] Widgets: kill app; widget updates next prayer; tap opens app on Salah.

## Version bump flow (NUR-041)

Single source of truth: `gradle.properties` → `nour.versionName` (e.g. `1.1.0`).
`versionCode = Major*10000 + Minor*100 + Patch` (v1.0 = 10000; int-max ceiling
caps major at 214,748). Bump `nour.versionName` only; never edit
`versionCode`/`versionName` in `build.gradle.kts`. Match the Play Console
version before upload.