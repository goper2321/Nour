# Nour — Baseline Audit (Phase A)

Recorded before the refinement described in `MIGRATION_PLAN.md`. Evidence is
marked **CONFIRMED** (observed in code, build output, or command output) or
**SUSPECTED** (reasoned, not yet reproduced on a device).

## 1. Architecture and stack (CONFIRMED)

- Single Gradle module `:app`; Kotlin + Jetpack Compose + Material 3; Room via KSP;
  manual in-activity navigation; no backend.
- `compileSdk` = Android 36 (minorApiLevel 1); `targetSdk` = 36; `minSdk` = 24
  (`app/build.gradle.kts`).
- `namespace = "com.example"`; `applicationId = "com.aistudio.nour.islamic"`.
- No `INTERNET` permission and no network client. Room is schema v1 with
  `exportSchema = false` and no migrations.
- Bundled Quran (Tanzil Uthmani) with SHA-256 integrity constants in
  `QuranCorpus.kt`; Saheeh International English translation.
- 14 locales via `localization/AppStrings.kt`; RTL supported.

## 2. Build and test baseline (CONFIRMED)

Host: Linux x86_64, JDK 21 (Temurin), Android SDK at `~/Android/Sdk`
(platforms android-36 / android-36.1, build-tools 36.0.0), Gradle 9.3.1 wrapper.
x86_64 means the Robolectric classes are **not** host-excluded here, so the full
suite runs.

**Original command** (`./gradlew :app:assembleDebug :app:testDebugUnitTest`):
**FAILED** after 12m at `:app:validateSigningDebug`:

```
> Keystore file '/home/eliot/Documents/me and me/Nour/debug.keystore' not found
  for signing config 'debugConfig'.
```

Both the `debug` and the release-fallback signing configs pointed at a
**gitignored** `${rootDir}/debug.keystore`. On a clean checkout that file does not
exist, so the app could not be built at all. Tests never executed.

**After the signing fix** (§4 item 1): `BUILD SUCCESSFUL`; `:app:assembleDebug`
and `:app:testDebugUnitTest` both ran. See `TEST_REPORT.md` for the final counts.

## 3. UI / UX and design-system state (CONFIRMED by inspection)

- A coherent hand-built M3E token layer already exists: `NourSpace`, `NourSize`,
  `NourAlpha`, `NourMotion`, `ExpressiveShapes` + semantic shapes, a custom
  `Typography` with tabular figures, and shared primitives in
  `ui/components/NourComponents.kt`. Details in `DESIGN_SYSTEM.md`.
- Color: built-in lavender light/dark schemes, optional dynamic color (API 31+),
  and a seed-driven scheme via the Material `SchemeTonalSpot` utilities.
- Adaptive navigation: bottom bar below 600 dp, `NavigationRail` at/above 600 dp
  (`MainActivity.kt`). `Tokens.kt` also defines `expandedBreakpoint = 840.dp`
  which is **never used** (dead token) - no 840 dp size class exists.

## 4. Confirmed defects

1. **Debug/release-fallback build broken on a clean checkout** (section 2).
   *Fix:* sign with AGP's built-in `debug` config (`~/.android/debug.keystore`,
   auto-created by AGP); keep a real upload keystore for production.
2. **Reduced-motion rule violated by finite spring animations.**
   `README`/`AGENTS.md` require every animation duration to pass through
   `NourMotion.duration(...)` so the system "Remove animations" setting is
   honored. Two files called Compose `spring(...)` directly and ignored it:
   - `features/prayer/ui/SeamlessPrayerHeader.kt` - the hero countdown digit
     slide in/out (`spring(...)`) ran even when motion was reduced.
   - `features/onboarding/ui/SetupWizard.kt` - the stepper dot width
     (`animateDpAsState` with `spring`) and the 8 step slide/fade springs.
   *Fix:* a reduced-motion-aware `NourMotion.spring(...)` helper, used at those
   call sites (collapses to `snap()` when motion is reduced).
3. **Predictive back not handled (targetSdk 36 enables it by default).**
   No `BackHandler`/`onBackPressed` existed anywhere. Consequences:
   - System back from Settings/Qibla/Quran exited the whole app.
   - In the Quran surah reader, back left the app instead of returning to the
     library. *Fix:* a host `BackHandler` (secondary tab → Salah) plus a reader
     `BackHandler` (reader → library).
4. **Expanded-layout bottom inset gap (SUSPECTED → mitigated).** On the rail
   layout there is no bottom bar, so `Scaffold` bottom padding is 0 and screens
   applied only the top inset; the last list row could sit under the
   gesture/navigation bar. *Fix:* apply the navigation-bar inset to the content
   frame on expanded screens only (no IME double-padding). Still needs a device
   visual check.

## 5. Android 16 compatibility risks (from the platform audit)

Verified as already-correct / not applicable:

- `PendingIntent` mutability: every call uses `FLAG_IMMUTABLE`; no
  `FLAG_MUTABLE`; no notification trampoline.
- Foreground service: `mediaPlayback` type declared with matching permission and
  typed `startForeground` on API 29+; not subject to the Android 16 media
  processing cap.
- No orientation/resizability/aspect-ratio restrictions declared, so the
  large-screen behavior change is a no-op; content widths are bounded.
- `POST_NOTIFICATIONS` requested at runtime (onboarding); exact-alarm permission
  declared, checked, and gracefully downgraded on `SecurityException`.
- No `JobScheduler`/`WorkManager`, no `ScheduledExecutorService`, no
  `TYPE_HEART_RATE`, no `MediaStore.getVersion`, no `elegantTextHeight`
  overrides, no nested intents.

Remaining risks (**SUSPECTED**, need a device/build artifact):

- **SUSPECTED:** notifications are only requested during onboarding; an upgrade
  from a pre-API-33 install (or a declined prompt) leaves no re-request path.
- **SUSPECTED:** 16 KB page-size - no app-native libs; a dependency `.so`
  (e.g. `play-services-location`) alignment was not verified. Inspect the built
  APK's `lib/**/*.so`.
- **SUSPECTED:** Arabic scripture rendering with the API 36 `elegantTextHeight`
  default - verify no clipping at large font scales.
- Deprecated-but-functional APIs: `Resources.updateConfiguration`
  (`LocalizationManager`), `getPackageInfo(String,int)` (`CrashReporter`),
  `Geocoder.getFromLocation` (`LocationPermissionManager`). Not API-36 blockers.

## 6. High-risk components (do not disturb)

- Prayer calculation engine, Hijri conversion, Qibla sensor math and its honest
  mode reporting, Adhan scheduling (`PrayerNotificationScheduler`), Quran
  reading continuity, and all Room data (schema v1, no migrations).

## 7. Prioritized plan

Ordered in `MIGRATION_PLAN.md`. In short: fix the build (§4.1), then
reduced-motion and predictive-back correctness (§4.2, §4.3), then the
expanded-layout inset (§4.4); defer the Compose BOM / `material3` upgrade to
native M3E APIs as a separate, device-verified change.
