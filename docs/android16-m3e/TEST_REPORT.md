# Nour — Test Report (Phase F)

All results below were produced on this machine. Nothing is inferred.

## Environment

- Host: Linux x86_64 (Fedora kernel 7.2.5), 3.7 GiB RAM.
- JDK: Temurin 21 (`/usr/lib/jvm/java-21-temurin-jdk`). The default `java` on
  this host is JDK 25, which is not used.
- Android SDK: `~/Android/Sdk`, platforms `android-36` + `android-36.1`,
  build-tools `36.0.0`.
- Gradle: 9.3.1 wrapper (the `gradlew` script was not executable; fixed).
- Because the host is x86_64, the Robolectric classes are **not** excluded by the
  `app/build.gradle.kts` host gate, so the full suite ran.

## Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
export ANDROID_HOME="$HOME/Android/Sdk"

./gradlew :app:assembleDebug :app:testDebugUnitTest
./gradlew :app:assembleRelease
```

## Results

| Check | Command | Result |
|---|---|---|
| Baseline before fix | `:app:assembleDebug :app:testDebugUnitTest` | **FAILED** at `:app:validateSigningDebug` (missing `${rootDir}/debug.keystore`) |
| Debug build | `:app:assembleDebug` | **BUILD SUCCESSFUL** |
| Unit tests | `:app:testDebugUnitTest` | **BUILD SUCCESSFUL**, 34/34 pass |
| Release build | `:app:assembleRelease` | **BUILD SUCCESSFUL** (includes `lintVitalRelease`) |
| 16 KB page size | `zipalign -c -P 16 -v 4` + `readelf -lW` | **Verification successful**; arm64 LOAD `Align = 0x4000` |

### Unit tests (34 tests, 0 failures, 0 errors, 0 skipped)

| Class | tests | fail | err | skip |
|---|---|---|---|---|
| CelestialMathTest | 4 | 0 | 0 | 0 |
| ExampleRobolectricTest | 14 | 0 | 0 | 0 |
| NourDesignSystemTest | 4 | 0 | 0 | 0 |
| PrayerCalculationTest | 4 | 0 | 0 | 0 |
| QuranCorpusTest | 5 | 0 | 0 | 0 |
| SeedSchemeTest | 1 | 0 | 0 | 0 |
| SetupWizardTest | 2 | 0 | 0 | 0 |
| **Total** | **34** | **0** | **0** | **0** |

`NourDesignSystemTest` renders `SeamlessPrayerHeader` and `DaySwitcherBar`;
`SetupWizardTest` drives the onboarding steps. Both cover code touched by this
change and pass, so the reduced-motion and back-handling edits did not regress
them.

### Follow-up re-run (prayer-screen redesign)

After the prayer-screen redesign (date switcher above the immersive sky header;
six `PrayerCard`s collapsed into one read-only five-row card) the same commands
were run again:

| Check | Command | Result |
|---|---|---|
| Debug build + tests | `:app:assembleDebug :app:testDebugUnitTest` | **BUILD SUCCESSFUL**, 34/34 pass |
| Release build | `:app:assembleRelease` | **BUILD SUCCESSFUL** (incl. `lintVitalRelease`) |

The `PrayerCard` test was replaced 1:1 by
`simplifiedCardShowsFivePrayerRowsReadOnly`, so the total is still 34 with 0
failures / 0 errors / 0 skipped.

### Artifacts

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk` (24.6 MB)
- Release APK: `app/build/outputs/apk/release/app-release.apk` (17.6 MB,
  signed with the debug keystore fallback — see the warning below)

The release build logs:
`Nour: no upload keystore configured; release builds will use the debug keystore (not for production).`
That is expected without `KEYSTORE_PATH`/`STORE_PASSWORD`. **Do not ship this
release APK**; it is only proof that release compilation and lint pass.

## Android 16 compatibility checks performed

- **16 KB page size — CONFIRMED PASS.** The only native libraries are
  `libandroidx.graphics.path.so` (from `androidx.graphics:graphics-path`,
  transitively included) for arm64-v8a/armeabi-v7a/x86/x86_64. `zipalign -c -P 16`
  passed and the arm64 LOAD segments are 16 KB aligned.
- **`lintVitalRelease` — CONFIRMED PASS.** AGP's release-blocking lint ran as part
  of `assembleRelease` and reported no fatal issues.
- PendingIntent mutability, foreground-service type, notification/exact-alarm
  permissions, and large-screen manifest behavior were reviewed statically (see
  `BASELINE.md` §5) — all already correct or not applicable.

## Not verified (requires a device/emulator — not claimed as passing)

- Predictive-back *animation* and back behavior end-to-end (the `BackHandler`s
  compile and the tests pass, but the gesture was not exercised).
- Edge-to-edge insets visually, including the new expanded-layout bottom inset.
- RTL layouts, 200% font scale, TalkBack, Qibla sensor calibration states, and
  Adhan playback under Doze/battery saver.
- Arabic scripture rendering with the Android 16 `elegantTextHeight` default.
- Notification-banner behavior after an upgrade from a pre-API-33 install. The
  re-request affordance was **added** in the 2026-10-12 nightly session (Settings
  → Notifications row with an app-details fallback); the on-device banner flow
  itself is still unverified.

## Notes / limitations

- Gradle warns that the daemon ran out of the configured 512 MiB metaspace and
  will restart; this is a host-memory limitation (3.7 GiB RAM), not a build error.
- No CI, and no standalone lint/detekt/ktlint/spotless gates exist in the repo
  (per `AGENTS.md`); only AGP's `lintVital` ran.
- Full instrumented tests (`androidTest`) need a device/emulator and were not run.

## 2026-10-12 nightly session (gaps: notification re-request + localization)

Confirmed on this host (x86_64, JDK 21, SDK android-36), commands run and output seen:

- `:app:compileDebugKotlin` — BUILD SUCCESSFUL (3m13s, then 2m47s after the
  translation sweep).
- `:app:testDebugUnitTest` — **34 tests, 0 failures, 0 errors, 0 skipped**
  (CelestialMath 4, ExampleRobolectric 14, NourDesignSystem 4,
  PrayerCalculation 4, QuranCorpus 5, SeedScheme 1, SetupWizard 2). Robolectric
  classes ran (x86_64 host).
- `:app:assembleDebug` — BUILD SUCCESSFUL, `app-debug.apk` 26 MB.

What changed:
- **Localization:** removed the last hardcoded user-facing strings in
  `QuranScreen.kt` (the non-Arabic surah subtitle, the Arabic font-size sample
  glyph, and translation-edition labels which now use BCP-47 endonyms).
  Completed the string table: 32 fields that existed only in Arabic were added
  to the other 12 locales (384 strings) and 10 fields that were English in every
  locale were added to all 13 non-English locales (130 strings). Every
  `AppStrings` field now has an override in all 13 non-English locales except
  `fontSizeSampleLetter` (an intentional glyph, not a translatable phrase).
- **Notification re-request:** Settings → Notifications permission row now falls
  back to the app's details page (with a Toast if even that fails) instead of a
  silent `catch`.

Not verified / caveats:
- **The 514 new translations are AI-authored and have NOT been reviewed by
  native speakers.** Short UI labels are low-risk, but a native review pass is
  recommended before release. This is the main quality caveat of this session.
- Everything device-only remains unverified (RTL rendering, 200% font scale,
  TalkBack, the actual notification re-grant flow on an OEM build).
- Audits that needed no change (recorded, not just asserted): every raw
  `tween`/`spring` is gated by `NourMotion`/`NourReduceMotion`, and every
  icon-only `IconButton` has a `contentDescription`.
