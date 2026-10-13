# Nour — Applied Prompt Review (Android 16 + M3E Prompt Library)

Session: overnight autonomous run, 2026-10-12.
Scope: prompts **07, 08, 10, 12, 17, 18, 21, 22, 24, 25** (plus the two deferred
localization/token items carried over from the previous session).

Verification legend used below:

- **Verified** — a command was run and its actual output seen.
- **Source-reviewed** — read from code; no runtime/device evidence.
- **Not verified (needs device)** — cannot be settled without an Android device/emulator.

Everything is uncommitted working-tree change (git is intentionally frozen this session).

---

## 07 — Screen Redesign and UI Review

Runs read-only; implementation only where a concrete, low-risk fix exists.
No device or screenshots were available, so this is a **source-based** critique
(the prompt asks to say so explicitly). It is not a claim of visual validation.

Journey reviewed: bottom navigation / rail shell and the primary screens it hosts
(Prayer, Qibla, Quran, Settings), plus the onboarding wizard.

Confirmed by source:

- The shell switches `NavigationBar` (compact) → `NavigationRail` (≥ 600 dp) in
  `MainActivity.kt` (~line 330). Single `NourNavDestination` model, manual nav,
  `BackHandler` present (~line 326) and predictive-back compatible.
- Content is width-bounded (`NourSize.readingWidth` 720 dp for Quran,
  `contentWidth` 960 dp otherwise, ~line 442), so long lines on tablets are avoided.
- Empty/loading states exist where they matter: Quran search shows
  `strings.noResultsForPattern`; progress indicators are used in
  `SetupWizard`, `QuranScreen`, `SeamlessPrayerHeader`, `LocationPermissionManager`.
- Scaffold uses `contentWindowInsets = WindowInsets(0,0,0,0)` with per-screen
  `statusBarsPadding` + `imePadding`, so insets are handled once, not twice.

Findings:

| # | Severity | Finding | Recommendation |
|---|----------|---------|----------------|
| 7.1 | Low | Onboarding prayer-time chips used a fixed `Modifier.width(92.dp)` (`SetupWizard.kt`), which can clip the time string at large font scales. | **Fixed:** changed to `Modifier.widthIn(min = 92.dp)` so the chip keeps its 92 dp floor but may grow. Verified compiles + tests pass. |
| 7.2 | Low | Several screens' visual rhythm (weights, exact paddings) can only be judged on-device. | Not verified (needs device). |
| 7.3 | Info | No duplicate/competing navigation controls found; a single nav model drives both bar and rail. | None. |

No screen was redesigned: no source evidence supported a structural change, and
the prompt requires not inventing redesigns without evidence.

---

## 08 — Typography, Color and Theming

Findings (all **source-reviewed**):

- **Hardcoded colors are intentional and were preserved.** They live only in
  celestial/sky artwork (`SeamlessPrayerHeader.kt`, `SkyVisualizer.kt`,
  `GoogleWeatherSkyElements.kt`) and in theme-definition files
  (`ui/theme/Color.kt`), plus one deliberate seed-swatch preview
  (`SettingsScreen.kt:299 Color.Black`). UI chrome otherwise uses Material 3
  color roles.
- **Typography uses the M3 type scale** via `MaterialTheme.typography` in the
  screens. The remaining literal `.sp` values are deliberate: the Quran reader
  font size is a *user-controlled feature* (12/14/18/22/24/26 sp pool), and one
  compact prayer label (`SettingsScreen.kt`, 14 sp = `labelLarge` equivalent).
  No change made — converting them would alter user-controlled sizing.
- **State is never conveyed by color alone** in the surfaces reviewed (selected
  nav items also move/scale, prayer status rows carry text labels).
- **Not verified (needs device):** light/dark parity, contrast ratios (WCAG),
  and 200 % font-scale rendering. These need a screen or Roborazzi golden run on
  x86_64.

No color/typography code change was warranted; this section's deliverable is the
audit above.

---

## 10 — Adaptive Layouts and Large Screens

Findings (**source-reviewed**):

- Adaptive navigation is **already implemented**: `maxWidth >= NourSize.compactBreakpoint`
  (600 dp) selects the rail; below it, the bottom bar (`MainActivity.kt` ~330–420).
  This covers compact/medium/expanded for phones, foldables, tablets and desktop-ish
  widths.
- **No orientation/resizability locks.** `AndroidManifest.xml` sets no
  `android:screenOrientation` and no `resizeableActivity="false"`, satisfying the
  Android 16 large-screen posture.
- Content widths are bounded (720/960 dp), preventing over-long measure.
- **Fixed-width container fixed** (also listed under 07): onboarding chip now uses
  `widthIn(min = 92.dp)`.
- Larger breakpoints (dedicated two-pane layouts at ≥ 840 dp) are **not**
  implemented; with a single-module, list/detail-light app the current rail+bounded
  content is a reasonable stopping point. Flagged as a future enhancement, not a
  defect.
- **Not verified (needs device):** foldable hinge handling, multi-window resizing,
  and rail ergonomics at 200 % font scale.

---

## 12 — Compose Performance Optimization

The prompt requires measurement first; **no device means no profiling**, so this
is explicitly a **code-reasoned** pass, not a measured one.

Concrete change made (**Verified** compile + tests):

- Added stable `key =` to the long/looping lazy lists so item identity survives
  recomposition and scroll state is preserved:
  - `QuranScreen`: surah list, popular surahs, search surah hits, search ayah hits,
    reading ayah lists (×2), mushaf ayah lists (×2), translation editions,
    bookmarks. Keys are the true identities (`surah.number`,
    `surahNumber * 1000 + ayahNumber`, `edition.id`, `bookmark.id`).
  - `SetupWizard`: onboarding prayer list keyed by `PrayerName.name`.
- Short, static lists (colour seeds, ≥ 5-item city lists) were left as-is; keys
  there are low value and the element types lack guaranteed-unique fields.

Findings:

| # | Severity | Finding | Recommendation |
|---|----------|---------|----------------|
| 12.1 | Low | Long ayah/surah lists previously had no `key`, so scroll/expanded state could be lost on list mutation. | **Fixed** (keys added). |
| 12.2 | Info | No `@Stable`/`@Immutable` annotations on the Quran model data classes. | Optional. They are effectively immutable; annotating `Surah`/`Ayah` could skip some recomposition. Left for a measured pass. |
| 12.3 | Info | No baseline profile shipped (see 24). | See 24.1. |

**Not verified (needs device):** jank/frame timings, recomposition counts, startup
traces. No performance number in this document is measured.

---

## 17 — Debug Without Guessing

No new user-reported defect was supplied during this run. The prompt was applied to
the one real defect encountered this session, with the full root-cause discipline:

**Case: nightly watchdog relaunched but the agent never came back.**

- Symptoms: watchdog considered the agent dead and relaunched it, but
  the relaunch produced no running agent.
- Hypotheses tested against evidence: (a) quota exhausted — ruled out for the
  watchdog itself; (b) the systemd unit's `ExecStart` was being re-parsed wrongly.
- **Root cause:** an unquoted/again-split `ExecStart` argument caused the service
  command to be fragmented, so the relaunch executed the wrong command line. This
  is a "plausible-sounding wrong cause" trap (it looked like a quota problem).
- **Fix:** corrected the `ExecStart` invocation and, additionally, added a
  **duplicate-agent guard** to `tools/nightly-relaunch.sh` so a relaunch refuses
  while any live `opencode2 … --auto` process exists. Guard isolated-tested:
  `GUARD: SKIP (live agent detected)`, matching only the real session pid.
- **Honest residual:** the *funded-credential* part of auto-relaunch is an owner
  action and remains **unverified** (a real relaunch needs a key with credit).
  This is documented in `HANDOVER.md`, not hidden.

No other defect was reproduced; nothing was silenced with try/catch, retries, or
disabled tests.

---

## 18 — Independent Code Review (of this session's diff)

Reviewed the working-tree changes made this run and the immediately preceding
session (read-only critique, source-based).

Files touched and review outcome:

- `features/quran/ui/QuranScreen.kt` — localized subtitle/sample glyph/edition chip;
  share text now built from `strings.quranShareAttribution` + `String.format`;
  LazyColumn keys added.
- `localization/AppStrings.kt` — `quranShareAttribution` base + 13 locale overrides;
  ~514 earlier translation overrides.
- `features/onboarding/ui/SetupWizard.kt` — prayer list `key`, `widthIn(min=)`.
- `features/settings/ui/SettingsScreen.kt` — notification-permission fallback,
  `imePadding()`, `NourSize.rowIcon`, tokenised spacers.
- `ui/theme/Tokens.kt` — `NourSize.rowIcon`, existing `NourSpace` tokens.
- `tools/nightly-relaunch.sh` — duplicate-agent guard.

Checks performed:

- **Key uniqueness:** all new LazyList keys are unique (`surah.number`;
  `surahNumber*1000+ayahNumber` since ayah counts < 1000; `edition.id`;
  `String` ids; `PrayerName.name`). No collision risk found.
- **Kotlin string escaping:** the new format field uses `\$` so `%1$s` is not read
  as a template; the literal `"${…}"` interpolations around it are correct.
- **Inserts are additive:** translation overrides are appended inside existing
  `AppStrings().apply { }` blocks, so no override is dropped.
- **No protected behaviour touched:** prayer math, Hijri, Qibla, the scheduler,
  Room entities, and quran resource files are all unchanged.
- **Defects found:** none. Concerns are limited to the unverifiable-on-host set
  (see 21).

---

## 21 — QA Like a Real User

No emulator/device is attached, so this is the honest matrix. "Not tested" is used
literally — a green build is **not** evidence a device flow works.

| Flow | Status |
|------|--------|
| Cold launch → prayer times render | Not tested (needs device) |
| Location permission grant/deny paths | Not tested (needs device) |
| Qibla: sensor calibration → "facing the Kaaba" banner | Not tested (needs device, sensor) |
| Adhan alarm fires with screen off / Doze | Not tested (needs device) |
| Notification permission re-request fallback (new) | Not tested (needs device); logic source-reviewed |
| Quran: read, last-position persistence across cold restart | Not tested (needs device) |
| Quran: bookmark add/remove, reading mode, font size persist | Not tested (needs device) |
| Quran: share sheet text per locale | Not tested (needs device); string path compiles |
| IME: keyboard does not cover Settings fields | Not tested (needs device) |
| RTL layout (Arabic/Hebrew-direction locales) | Not tested (needs device) |
| 200 % font scale across screens | Not tested (needs device) |
| TalkBack traversal of icon-only controls | Not tested (needs device) |
| Compile + unit tests | **Verified** — 34 tests, 0 failures/errors/skipped |
| Debug APK assembles | **Verified** — BUILD SUCCESSFUL, ~26 MB |

---

## 22 — Privacy and Security Audit

Read-only; findings documented, no code changed.

Confirmed by source/commands (**Verified**):

- **No `INTERNET` permission** in `AndroidManifest.xml`; therefore no SDK (including
  Play services) can exfiltrate data. The offline-first guarantee holds at the
  manifest level.
- **No analytics/tracking/ads/Firebase** dependencies in
  `app/build.gradle.kts` / `gradle/libs.versions.toml`.
- **No committed secrets** — a targeted scan for `api_key|secret|password|token`
  assignments over `app/` and `gradle/` returned nothing.
- **Components scoped correctly:** `MainActivity` `exported="true"` (launcher only);
  `PrayerAlarmService`, `PrayerAlarmReceiver`, `PrayerActionReceiver` all
  `exported="false"`.
- **Backup scoped:** `allowBackup=true` but the rules files include only
  shared-prefs + database (no arbitrary files / secrets / keystore).
- **Permissions all justified:** location (Qibla/prayer), notifications, exact alarm,
  foreground service (media playback), vibrate, wake lock, boot.
- **16 KB page alignment PASSES** — `zipalign -c -P 16` reports
  "Verification successful" for the debug APK (Android 15+/16 requirement).

Findings:

| # | Severity | Finding | Recommendation |
|---|----------|---------|----------------|
| 22.1 | Medium | `CrashReporter` writes stack traces to shared `Downloads/NourCrash/` (MediaStore) in **all** build types, including release. On a user's device this is world-readable to other apps/media scanners. | Gate on `BuildConfig.DEBUG`, or document retention + tell users to delete. Left unchanged because the owner explicitly relies on this path for field diagnostics. |
| 22.2 | Low | `play-services-location` pulls `play-services-base/basement/tasks`. Harmless offline (no INTERNET) but is a Google SDK a de-Googled build wouldn't want. | Optional: platform `LocationManager` could replace it. Not changed (feature-bearing dependency). |
| 22.3 | Info | `com.google.android.material` (View library) is present; used for the XML theme parent. | Fine. |

---

## 24 — Performance and Startup Audit

Read-only (**source-reviewed**; no device trace).

Confirmed:

- `NourApp.onCreate` is **light**: installs the crash handler, sets the singleton,
  obtains the Room instance (build is lazy — the DB is not opened until first
  query), initialises notification channels, and schedules the rolling alarm
  window **on `Dispatchers.IO`**, never on the main thread. Good startup hygiene.
- No blocking disk/network work on the main thread at startup.

Findings:

| # | Severity | Finding | Recommendation |
|---|----------|---------|----------------|
| 24.1 | Medium | **No baseline profile** (`baseline-prof.txt` absent), and no Macrobenchmark module. Cold-start and first-scroll are therefore unoptimised for the PGO/JIT path. | Generate a Baseline Profile (Macrobenchmark) once a device/CI exists. Cannot be produced or verified on this host. |
| 24.2 | Low | `isMinifyEnabled = false` (see 25) leaves the release APK larger than necessary. | See 25.|

**Not verified (needs device):** cold/warm start timings, frame drops, memory.

---

## 25 — Release Readiness and Handover

Read-only (**source-reviewed** + commands).

Confirmed:

- `targetSdk = 36`, `compileSdk = 36`, `minSdk = 24` — meets the Google Play 2026
  requirement (16 KB / targetSdk 36).
- `versionCode = 2`, `versionName = 1.1`.
- Release build is **not debuggable** (no `isDebuggable = true`).
- **16 KB page alignment passes** (see 22).
- Debug APK builds and all 34 unit tests pass.
- Signing: correctly falls back to `debug.keystore` only when `KEYSTORE_PATH` /
  `STORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` are absent — i.e. **it will ship a
  debug-signed APK unless release creds are supplied**. This is documented and is an
  owner/CI action.

Finding:

| # | Severity | Finding | Recommendation |
|---|----------|---------|----------------|
| 25.1 | **High** | **R8/`isMinifyEnabled = false`** for the release build (and no `isShrinkResources`). Shipping unminified is a size/perf/IP-protection gap for a release. | Enable once a device regression pass is possible: set `isMinifyEnabled = true`, `isShrinkResources = true`, then verify Room/Compose/entry points on-device with the existing 21-line `proguard-rules.pro`. **Not changed this session** — enabling R8 without a device regression run risks a release-only crash, which the prompt explicitly forbids shipping blindly. |
| 25.2 | Medium | Release signing depends on env vars; absent → debug keystore. | Document in the release checklist (already warned in `AGENTS.md`). |
| 25.3 | Medium | Crash logs to shared storage in release (same as 22.1). | Gate on debug or document retention. |

Handover of state: this review, `NIGHTLY_STATUS.md`, `HANDOVER.md` and
`TEST_REPORT.md` together describe everything changed and everything still requiring
a device.

---

## 05 — Dependency and Build Upgrade

Audit (**source-reviewed**, versions read from `gradle/libs.versions.toml` and the
wrapper). The toolchain is already current for this codebase, so **no upgrade was
made** — the prompt says not to upgrade blindly and not to add libraries.

Current set (already on modern, mutually compatible versions):

| Component | Version |
|-----------|---------|
| Gradle wrapper | 9.3.1 |
| AGP | 9.1.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.06.01 |
| Material3 | 1.5.0-alpha15 (the M3E-carrying alpha — intentional) |
| Room | 2.7.0 |
| KSP | 2.3.5 |
| Coroutines | 1.10.2 |
| play-services-location | 21.3.0 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 |

There is no dependency that is both outdated *and* required by a pending task.
Upgrading anything here would be change for its own sake, against the prompt's
guidance, and cannot be device-verified this session. **No upgrade performed.**

## 15 — Data Persistence and Migration Safety

No data change was requested this session, so this is the safety audit the prompt
asks to run (and the standing rule from `AGENTS.md`).

Confirmed by source (**Verified** read of `database/NourDatabase.kt`, `Entities.kt`):

- Room schema is at **version 1**, `exportSchema = false`.
- No `fallbackToDestructiveMigration()` anywhere; no `addMigrations(...)` present
  (none needed at v1).
- Entities are unchanged this run: `PrayerLogEntity`, `BookmarkEntity`,
  `KhatmahPlanEntity`, `AdhanSettingsEntity`.
- Stable preference keys preserved: `nour_app_prefs`
  (`MainActivity`, `NourPreferences`) and `nour_app_preferences`
  (`LocalizationManager`). No key was renamed or orphaned.
- On-create seeding (default Adhan tones + a Ramadan khatmah plan) runs once via the
  Room `Callback`, not on every open.
- Backup remains scoped to shared-prefs + database only (see 22).

Conclusion: **no migration is needed and no user-data operation changed.** Risk to
existing user data: **none** (nothing was written or deleted). If a future change
adds/alters an entity, it requires a version bump **plus a real migration** — the
standing rule, reaffirmed here.

## 04 — Android 16 (API 36) Migration Audit

Read-only platform-compatibility pass. Configuration (actual, read from source):
`compileSdk 36` (minorApiLevel 1), `targetSdk 36`, `minSdk 24`, AGP 9.1.1,
Gradle 9.3.1, Kotlin 2.2.10, Compose BOM 2026.06.01, Material3 1.5.0-alpha15.

| # | Item | Verdict | Evidence |
|---|------|---------|----------|
| 1 | Edge-to-edge enforcement | **OK** | `enableEdgeToEdge()` (`MainActivity.kt:127`); no `windowOptOutEdgeToEdgeEnforcement`; scaffold uses `contentWindowInsets = WindowInsets(0,0,0,0)` with per-screen `statusBarsPadding`/`imePadding` so insets are applied once. |
| 2 | Predictive back on by default | **OK** | No `onBackPressed()` override, no `KEYCODE_BACK`, no `enableOnBackInvokedCallback="false"`. Uses `androidx.activity` `BackHandler` (host + Quran reader). |
| 3 | Large-screen behavior | **OK** | No `screenOrientation`, `resizeableActivity="false"`, `minAspectRatio` or `maxAspectRatio`. Adaptive bar/rail at 600 dp (see 10). |
| 4 | `elegantTextHeight` deprecated | **OK** | No occurrences in `app/src/main`. |
| 5 | Intent redirection hardening | **OK** | All created intents are explicit (`Intent(context, SomeClass)`); `PrayerAlarmReceiver`/`PrayerActionReceiver` are `exported="false"`, so no untrusted intent/extras are forwarded. |
| 6 | 16 KB page size | **OK** | Only `.so` is `libandroidx.graphics.path.so`; `zipalign -c -P 16` → "Verification successful". |
| 7 | JobScheduler quotas | **OK / N-A** | No `JobScheduler`/`WorkService`/`WorkManager`; background work uses `AlarmManager` exact alarms (`setExactAndAllowWhileIdle` with `canScheduleExactAlarms` handling). |
| 8 | Bluetooth bond-loss etc. | **OK / N-A** | No Bluetooth/BLE usage. |
| 9 | Notifications / permissions / exact alarm / FGS | **OK** | `POST_NOTIFICATIONS` with a re-request path (see 16); `SCHEDULE_EXACT_ALARM`; `foregroundServiceType="mediaPlayback"` + matching `FOREGROUND_SERVICE_MEDIA_PLAYBACK`. |

**Unknown / device-only:** runtime behavior of the predictive-back animation, icon
contrast over the immersive header, and Doze killing the alarm — all need a device.

No migration issue found; no code change made (the prompt is read-only). Values are
already live in `AGENTS.md` as protected behavior.

## Summary of code changes this run

| Change | File | Verification |
|--------|------|--------------|
| Stable LazyList keys (Quran + onboarding) | `QuranScreen.kt`, `SetupWizard.kt` | compile + 34 tests pass |
| Prayer-time chips `width(92.dp)` → `widthIn(min = 92.dp)` | `SetupWizard.kt` | compile + 34 tests pass |
| Localized Quran share attribution (base + 13 locales) | `AppStrings.kt`, `QuranScreen.kt` | compile + 34 tests pass |
| Duplicate-agent guard (carried from prior session) | `tools/nightly-relaunch.sh` | isolated test |

**Verified result:** `./gradlew :app:testDebugUnitTest :app:assembleDebug` →
BUILD SUCCESSFUL, 34 tests / 0 failures / 0 errors / 0 skipped, APK ≈ 26 MB.

**Not verified:** every on-device/visual/RTL/font-scale/TalkBack/sensor/Doze item
above, and all performance numbers. AI-authored translations (the earlier 514
overrides plus the 13 share-attribution strings) are **not native-reviewed**.

---

## Deeper pass follow-up (post-handover, owner-authorized)

Done after the owner asked to continue: prompt 07 per-screen work, remaining token
cleanups, and an end-of-pass per-screen verification.

### Remaining token cleanup (value-identical, no visual change)
40 raw dimensions were routed through the shared tokens across Prayer/Qibla/Quran/
Settings/SetupWizard:

- spacing (`NourSpace`): `spacedBy`, `padding(...)` (single + named args) for
  4/8/12/18/24/32 dp → `xs/sm/md/lg/xl/xxl`.
- size (`NourSize`): `.size(20/22/24/40/48.dp)` → `smallIcon/rowIcon/icon/badge/
  touchTarget`; `heightIn(min = 48.dp)` → `touchTarget`; `BorderStroke(1.dp, …)` →
  `border`; `shadowElevation/tonalElevation = 0.dp` → `flat`.

Values with no matching token (2/6/10/14/16/44/52/54/…) were deliberately left as-is,
so layout is byte-for-byte identical.

### Prompt 07 follow-up fix
- `SettingsScreen`: the custom-seed title and hex labels had `maxLines = 1` with no
  `overflow`, so a long translation would hard-clip at large font scales; both now
  use `TextOverflow.Ellipsis`.

### Per-screen verification (end of the deeper pass)
Added `app/src/test/java/com/example/ScreenRenderTest.kt` — a Robolectric test that
renders all five top-level screens with realistic data on the JVM and asserts each
screen's root content is actually displayed (i.e. composes + lays out without a
runtime crash). It also writes one PNG per screen (via Roborazzi) for human review.

- Screens verified rendering: Prayer, Qibla, Quran, Settings, Setup Wizard.
- Images written to `app/build/screen-verify/*.png` (and copied to
  `~/Downloads/screen-verify/`); all 822×1782 and non-blank.
- **Caveat:** this is *rendering* verification. The agent could not visually inspect
  the PNGs (no image input in this model); they exist for the owner / a
  vision-capable reviewer. Sensors, Doze, RTL and TalkBack still require a device.

### Final verification (single Gradle invocation)
`./gradlew :app:testDebugUnitTest :app:assembleDebug -Proborazzi.test.record=true
--no-daemon -Dorg.gradle.jvmargs='-Xmx2048m -XX:MaxMetaspaceSize=384m' --max-workers=1`

→ **BUILD SUCCESSFUL, 39 tests / 0 failures / 0 errors / 0 skipped** (34 previous +
5 new ScreenRenderTest), debug APK ≈ 26 MB.
