# Nour — Android 16 / M3E Refinement Handover

This is the final summary (deliverable F). Supporting documents:
`BASELINE.md`, `MIGRATION_PLAN.md`, `DESIGN_SYSTEM.md`, `TEST_REPORT.md`,
`NIGHTLY_REVIEW.md` (per-prompt audit findings, 2026-10-12), `NIGHTLY_STATUS.md`
(chronological log).

## What this pass did

The app was already Compose + Material 3 and already targeted API 36, so this was
a **refinement and hardening** pass, not a green-field migration. Work was scoped
to confirmed defects rather than a cosmetic reskin.

### 1. Fixed: the project could not be built on a clean checkout
The `debug` and release-fallback signing configs pointed at a **gitignored**
`${rootDir}/debug.keystore`, so `./gradlew :app:assembleDebug` failed at
`:app:validateSigningDebug` before compiling an APK. Both build types now use
AGP's built-in `debug` signing config (the standard `~/.android/debug.keystore`,
created by AGP). Release still prefers a real upload keystore whenever
`KEYSTORE_PATH` + `STORE_PASSWORD` are set; the debug fallback is clearly logged
and is not for production.

### 2. Fixed: reduced-motion rule was violated
`README`/`AGENTS.md` require every animation to honor the system "Remove
animations" setting. `SeamlessPrayerHeader` (hero countdown digits) and
`SetupWizard` (stepper pill, step slides) called Compose `spring(...)` directly
and kept animating when motion was reduced. Added
`NourMotion.spring(stiffness, dampingRatio)`, which collapses to `snap()` when
motion is reduced (mirroring the existing `NourMotion.duration(...)` for tweens),
and routed those call sites through it. Behavior when motion is **on** is
unchanged.

### 3. Fixed: predictive back was not handled
`targetSdk 36` enables predictive back by default. There was no back handling, so
system back exited the whole app from Settings/Qibla/Quran, and exited the app
from inside the Quran reader. Added:
- a host `BackHandler` (`MainActivity`) that returns a secondary tab to the
  prayer screen;
- a `BackHandler` in `QuranScreen` that returns the open surah reader to the
  library (composed after the host handler, so it takes precedence only while the
  reader is open).

### 4. Mitigated: expanded-layout bottom inset
On the rail layout there is no bottom bar, so `Scaffold` bottom padding is 0 and a
screen's last row could sit under the gesture/navigation bar. The content frame
now applies the navigation-bar bottom inset on expanded screens only (no IME
double-padding).

### 5. Housekeeping
- `gradlew` is now executable (it was `100644`, so `./gradlew` failed).
- Removed three pre-existing unused imports in `MainActivity`.

### 6. Prayer screen redesign (requested follow-up)

The owner asked that the animated sky card become immersive, that the card under
it collapse into five simple lines, and that the date switcher sit above them.

- **Order:** sky hero card → date switcher → simplified prayer-times card.
- **Immersive sky:** `SeamlessPrayerHeader` is now full-bleed with square top
  corners (`ImmersiveHeaderShape`) and extends up behind the system status bar;
  the header's inner content applies `statusBarsPadding()` so text stays clear of
  the status bar. `MainActivity` applies no top inset for this screen, so the
  inset is applied exactly once.
- **Date switcher:** the visible "Today" button was removed; tapping the middle
  of the bar (`day_switcher_reset`) returns to today. The bar is opaque again
  now that it sits below the sky (the temporary `translucent` variant was
  removed).
- **Simplified list:** the six separate `PrayerCard`s were replaced by one
  read-only `SimplifiedPrayerTimesCard` (`PrayerScreen.kt`) with five rows —
  Fajr, Dhuhr, Asr, Maghrib, Isha. Sunrise is intentionally omitted. The next
  prayer row is tinted with `primary`; there is no interaction.
- **Behaviour change (owner-approved):** the in-app "mark as prayed" toggle is
  gone. The prayer log is still written from the Adhan notification's **Mark as
  prayed** action (`PrayerNotificationScheduler`, `ACTION_MARK_PRAYED`), and the
  Room table/DAO are untouched. The now-unused `markedAsPrayed`/`unmarkedPrayer`
  strings were left in the 14 locale blocks.

## Design-system decisions

- **Kept** the existing hand-built M3E token layer (spacing, size, shape,
  typography, motion, shared components) — it is coherent. Documented it in
  `DESIGN_SYSTEM.md` as the single reference.
- **Did not** upgrade the Compose BOM / `material3` to the newer native M3E APIs
  (`MaterialExpressiveTheme`, `MotionScheme`, `MaterialShapes`, `ButtonGroup`,
  `FloatingToolbar`). That is a high-blast-radius dependency change that cannot be
  visually validated here (no device), so it is recorded as an explicit
  follow-up with its own verification gate rather than done blind.
- **Did not** touch Room (schema v1, no migrations), the prayer engine, Hijri
  conversion, Qibla math/mode honesty, Adhan scheduling, Quran continuity, or
  localization.

## Changed files

| File | Change |
|---|---|
| `app/build.gradle.kts` | Signing config uses AGP built-in debug keystore; release falls back to it |
| `app/src/main/java/com/example/ui/theme/Tokens.kt` | Added reduced-motion-aware `NourMotion.spring(...)` |
| `.../features/prayer/ui/SeamlessPrayerHeader.kt` | Countdown transition uses `NourMotion.spring`; immersive full-bleed sky, `statusBarsPadding()` |
| `.../features/prayer/ui/PrayerScreen.kt` | Order: sky card → date switcher → read-only 5-row `SimplifiedPrayerTimesCard` (removed `PrayerCard`) |
| `.../features/prayer/ui/DaySwitcherBar.kt` | "Today" button removed in favour of tapping the bar to reset (`day_switcher_reset`) |
| `.../ui/theme/Shapes.kt` | New `ImmersiveHeaderShape` token |
| `app/src/test/java/com/example/NourDesignSystemTest.kt` | Replaced the `PrayerCard` test with a read-only `SimplifiedPrayerTimesCard` test |
| `.../features/onboarding/ui/SetupWizard.kt` | Stepper + step transitions use `NourMotion.spring`; removed unused import |
| `.../MainActivity.kt` | Predictive-back `BackHandler`; expanded bottom inset; removed unused imports |
| `.../features/quran/ui/QuranScreen.kt` | Reader `BackHandler` |
| `gradlew` | File mode `100644` → `100755` |
| `docs/android16-m3e/*` | New deliverables (this document and the four above) |
| `DESIGN_MIGRATION.md` | Added a pointer to `docs/android16-m3e/` |

## Verification (see `TEST_REPORT.md` for raw output)

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
export ANDROID_HOME="$HOME/Android/Sdk"
./gradlew :app:assembleDebug :app:testDebugUnitTest   # BUILD SUCCESSFUL, 34/34 tests
./gradlew :app:assembleRelease                        # BUILD SUCCESSFUL (incl. lintVitalRelease)
```

- 34/34 JVM + Robolectric tests pass on this x86_64 host.
- Debug and release APKs are produced. `zipalign -c -P 16` and ELF inspection
  confirm the app is 16 KB-page compatible (`libandroidx.graphics.path.so` arm64
  LOAD segments are `0x4000` aligned).
- Re-run after the prayer-screen redesign (above): `assembleDebug` +
  `testDebugUnitTest` → **BUILD SUCCESSFUL, 34/34 tests**; `assembleRelease` →
  **BUILD SUCCESSFUL** (incl. `lintVitalRelease`). The counts are unchanged
  because the `PrayerCard` test was replaced 1:1 by the new card test.

## Known limitations and unverified behavior

- **Not device-verified:** predictive-back gesture/animation, edge-to-edge
  insets (including the new expanded-layout inset), RTL, 200% font scale,
  TalkBack, light/dark/dynamic themes, Qibla sensors, Adhan playback under Doze,
  and Arabic rendering with the Android 16 `elegantTextHeight` default.
- **Prayer screen (follow-up), not device-verified:** the sky now sits behind the
  status bar. The status-bar icon appearance is still driven by the light/dark
  theme, so icon contrast over a bright daytime sky in dark theme was **not**
  visually checked. The immersive header's rounded-bottom edge and the tap-to-
  reset date switcher (which has no visible "Today" affordance) are also
  unverified on-device. Sunrise time is no longer shown anywhere on the prayer
  screen (by owner request).
- **Fixed (2026-10-12 nightly):** `POST_NOTIFICATIONS` now has a re-request path.
  Settings → Notifications shows a permission row (Android 13+) that re-triggers
  the system prompt, and when notifications are already granted it opens the
  system notification settings; if that screen is missing on an OEM build it
  falls back to the app's own details page and, if even that fails, tells the
  user instead of failing silently. Not device-verified (needs a real API 33+
  device/OEM build to confirm the fallback path).
- The release APK built here is signed with the **debug** key as a fallback. It is
  proof of compilation only — never ship it. Provide `KEYSTORE_PATH` and
  `STORE_PASSWORD` for a real release.
- Full `:app:lint` was not run (only the release-blocking `lintVitalRelease`); the
  repo has no CI or standalone lint/format gates.
- **Release hardening gaps (2026-10-12 nightly audit, `NIGHTLY_REVIEW.md` 25.1 /
  24.1):** the release build has `isMinifyEnabled = false` (no R8, no resource
  shrinking) and no baseline profile is shipped. Neither was changed this session
  because both need a **device regression pass** to validate safely — a release-only
  R8 crash is exactly the kind of thing that must not be shipped blind. The 16 KB
  page-size requirement **passes** (`zipalign -c -P 16` → Verification successful).
- **Privacy note (`NIGHTLY_REVIEW.md` 22.1):** `CrashReporter` writes stack traces to
  shared `Downloads/NourCrash/` in *all* build types, including release. This is an
  intentional field-diagnostics aid; a release build may want it gated on
  `BuildConfig.DEBUG`.
- **Dependency note (`NIGHTLY_REVIEW.md` 22.2):** `play-services-location` is present.
  It is offline-safe (the app has **no `INTERNET` permission**, so it cannot phone
  home), but a de-Googled build could replace it with the platform `LocationManager`.

## Recommended next steps

1. Install the debug APK on an API 36 device and an older device; walk the major
   journeys and confirm back behavior, insets, themes, RTL, and large fonts.
2. Decide whether to adopt the newer `material3` M3E APIs; if yes, do it as a
   dedicated change with a full rebuild, lint, and device pass.
3. ~~Consider a notification-permission re-request affordance in Settings.~~
   **Done** in the 2026-10-12 nightly session.
4. If continuing the nightly work: the "Android 16 + M3E Prompt Library" pass has
   now been applied across prompts 01–26 (see `NIGHTLY_REVIEW.md` for the full
   per-prompt findings, and `NIGHTLY_STATUS.md` for the exact chronology). The
   remaining items are **device/CI gated**, not code-blocked:
   - device acceptance pass (back, insets, RTL, 200 % font, TalkBack, sensors,
     Adhan under Doze);
   - enable **R8** for release (`isMinifyEnabled`/`isShrinkResources`) only after
     that device regression pass (see below);
   - generate a **baseline profile** via Macrobenchmark once a device/CI exists;
   - decide whether to gate `CrashReporter`'s shared-storage writes on
     `BuildConfig.DEBUG`.
5. Fix the nightly auto-resume credentials (see the safety-net section below).

## Nightly safety net — owner action needed (2026-10-12)

The systemd `--user` relauncher (`nour-nightly-relaunch.service` / `.timer`) no longer has the ExecStart bug (unquoted space) and now refuses to start a second agent while any OpenCode agent process is alive. **However, automatic resume still cannot work** because of provider authentication/credits:

- the stored `~/.local/share/opencode/auth.json` `[openrouter]` key is **invalid** (`401 "User not found"`);
- the only valid key is the session's free-tier `OPENROUTER_API_KEY` (50 free requests/day), which the systemd user service does not inherit;
- the relaunch resolves to a **paid** model (`anthropic/claude-haiku-5.5`) -> "requires more credits ... can only afford 953" -> `rc=1` on every attempt.

To restore unattended auto-resume: give the systemd user service a working credential and a funded model, e.g. create `~/.config/nour-nightly.env` (chmod 600) containing a funded `OPENROUTER_API_KEY=` line, point the relaunch at a funded model, then add `EnvironmentFile=%h/.config/nour-nightly.env` to `~/.config/systemd/user/nour-nightly-relaunch.service` and run `systemctl --user daemon-reload`. Until then the unattended auto-resume is NON-FUNCTIONAL (no duplicate agent is ever started, though).
