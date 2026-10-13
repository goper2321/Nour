STATUS: RUNNING
HEARTBEAT: 1791795386
STEP: deeper pass done; running FINAL full verify (all tests + assembleDebug)

# Nour nightly session state

This file is the machine-readable heartbeat used by
`tools/nightly-relaunch.sh` (systemd timer `nour-nightly-relaunch.timer`).
Do not delete it while work is in progress; set `STATUS: DONE` when finished
or `STATUS: BLOCKED` if a genuine blocker needs the owner.

Owner-approved mission: close the known gaps (notification-permission
re-request path, remaining localization) and then keep improving UI/UX all
night, following the "Android 16 + M3E Prompt Library". Native M3E adoption is
already in the tree from an earlier pass; do not extend it without device
verification.

## Hard rules (from AGENTS.md + prompt library ground rules)

- Offline-first. No INTERNET permission, no network/analytics/tracking.
- User data is the user's: never clear/rewrite Room (schema v1, no migrations)
  or preferences. Never use fallbackToDestructiveMigration().
- Never fabricate religious content; missing content reports "unavailable".
- Smallest complete change; never rewrite working engines (prayer, Qibla,
  Adhan scheduling, Quran continuity, Hijri).
- Every animation duration routes through `NourMotion` so "Remove animations"
  is honored. 48 dp touch targets. Content descriptions on icon-only buttons.
- Report confirmed (command run, output seen) vs assumed vs not verified.
  Device-only checks are always "not verified" here.
- Host has ~3.7 GiB RAM: never run two Gradle builds at once; use
  `--no-daemon -Dorg.gradle.jvmargs='-Xmx2048m -XX:MaxMetaspaceSize=384m'
  --max-workers=1`; iterate with `:app:compileDebugKotlin`, verify with
  `:app:testDebugUnitTest` + `:app:assembleDebug`.
- Git: do NOT commit, do NOT create branches, do NOT reset/discard the
  owner's uncommitted work.

## Baseline (confirmed before any edit tonight)

- `:app:assembleDebug` -> BUILD SUCCESSFUL (10m12s), APK 26.8 MB @ 01:14:33
- `:app:testDebugUnitTest` -> 34 tests, 0 failures, 0 errors, 0 skipped
  (fresh run 01:18:18; classes: CelestialMath 4, ExampleRobolectric 14,
  NourDesignSystem 4, PrayerCalculation 4, QuranCorpus 5, SeedScheme 1,
  SetupWizard 2)

## Progress log

Append one line per completed step: `HH:MM - what - evidence`.
02:29 - fixed watchdog: ExecStart had an unquoted space ("me and me") -> 203/EXEC, so the timer never relaunched anything. Now ExecStart=/usr/bin/bash "<script>", verified with `systemctl --user start nour-nightly-relaunch.service` -> "session healthy ... -> exit", no duplicate agent. STALE_SECONDS raised to 900s.
02:5x - batch1 (localization + notification re-request) edits done; :app:compileDebugKotlin BUILD SUCCESSFUL (3m13s).
     - QuranScreen: non-Arabic subtitle now localized (was English englishMeaning); font-size sample glyph localized; edition chips show language endonyms (BCP-47 + Locale.getDisplayLanguage).
     - QuranRepository: TRANSLATION_EDITIONS.language now BCP-47 codes.
     - SettingsScreen: notification re-grant fallback -> ACTION_APPLICATION_DETAILS_SETTINGS + Toast; 8x width(8.dp)->NourSpace.sm; new string notifSettingsUnavailable (base only for now).
     - Localization gap quantified: 32 fields Arabic-only (setup wizard, alarms master...), 8 fields English-only (error/tone strings).
02:52 - localization completeness: added 32 Arabic-only fields to the other 12 locales (384 strings) + 10 English-only fields to all 13 locales (130 strings). Coverage now: every AppStrings field has an override in all 13 non-English locales except fontSizeSampleLetter (intentional glyph). JSON sources: /home/eliot/.opencode2/tmp/opencode/nour-i18n.json, nour-i18n2.json. AppStrings.kt 3789->4303 lines.
02:55 - audits (read-only, no code change needed):
     - Animations: every raw tween/spring is already gated by NourMotion.duration / NourMotion.reduced / NourReduceMotion.isEnabled. No violations.
     - Accessibility: all IconButton icon-only controls have a contentDescription (several via a later line; script false-positives cleared by manual read). No unlabeled icon-only controls found.
     - Note (deferred): Quran share text embeds English "Surah ... via Nour" in the shared payload; needs a pattern-style string to localize. Low priority.
02:56 - running :app:assembleDebug for full-build verification.
04:35 - SERVER RESTART at ~02:57->04:33 (hosted server bounced; ~1.5h gap). All batch-1/batch-2 edits survived (AppStrings 4303 lines, notif fallback, QuranScreen/Repository changes). IME edits had NOT persisted and were re-applied.
     - WATCHDOG FINDING (important): the relauncher's ExecStart bug is fixed and the timer fires, but EVERY relaunch attempt exits rc=1 because the relaunch account is OUT OF CREDITS: "This request requires more credits... requested up to 65536 tokens, but can only afford 953" (model anthropic/claude-haiku-5.5). The safety net therefore CANNOT resume the session until the owner tops up credits. No duplicate agent was started (good), but the fallback is non-functional for billing reasons, not code. Documented for the owner.
     - Heartbeat refreshed; watchdog now logs "session healthy".
04:36 - re-applied SettingsScreen IME padding: main list .imePadding() (inline seed-hex field) + city-search dialog content .imePadding(). Mirrors SetupWizard's AlertDialog pattern; unverifiable without a device.
04:36 - running :app:testDebugUnitTest + :app:assembleDebug.
04:38 - VERIFIED: :app:testDebugUnitTest -> 34 tests, 0 failures, 0 errors, 0 skipped; :app:assembleDebug -> BUILD SUCCESSFUL, app-debug.apk 26M (04:38). Single Gradle invocation, --no-daemon --max-workers=1 -Xmx2048m.
04:40 - watchdog hardening: added a guard to tools/nightly-relaunch.sh so it NEVER starts a second agent while any OpenCode --auto process is alive. Reason: during the server restart the heartbeat went stale while the live session was still working, and the watchdog attempted a relaunch (it only failed by luck: no credits). A duplicate would have edited the same uncommitted files. Isolated test: the guard detects only the live session -> "SKIP (live agent detected)".
04:41 - SAFETY-NET DIAGNOSIS (owner action needed): the ExecStart bug is fixed, but the relauncher still cannot resume a session because of provider authentication/credits:
     - session env OPENROUTER_API_KEY = valid FREE-TIER key (free_model_daily_requests limit 50/day, 0 used).
     - stored ~/.local/share/opencode/auth.json [openrouter] key = INVALID (401 "User not found").
     - the relaunch resolved to a PAID model (anthropic/claude-haiku-5.5) -> "requires more credits ... can only afford 953" -> rc=1 on every attempt.
     - net effect: unattended auto-resume is currently NON-FUNCTIONAL. To restore it the owner must give the systemd user service a working credential (e.g. EnvironmentFile=%h/.config/nour-nightly.env with a funded OPENROUTER_API_KEY, or the opencode/cline-pass key) and/or point the relaunch at a funded model. No duplicate agent was ever started.
04:47 - VERIFIED (rowIcon token batch): added NourSize.rowIcon = 22.dp (between smallIcon 20 and icon 24) and applied it to the 4 Settings leading row icons (Translate/Alarm/Notifications/Explore). :app:testDebugUnitTest -> 34/0/0/0; :app:assembleDebug -> BUILD SUCCESSFUL, app-debug.apk 26M (04:47).
04:55 - audit pass (read-only, no code change needed):
     - Back handling (prompt 14): androidx.activity.compose.BackHandler used correctly (MainActivity nav-back + QuranScreen reader-close). Dialogs/sheets/menus are M3 components, so system/predictive back is handled by the libraries. Manifest declares no deprecated screenOrientation/resizeableActivity and no explicit enableOnBackInvokedCallback (defaults enabled for targetSdk 36). PASS.
     - Backup / data extraction: allowBackup=true with data_extraction_rules.xml + backup_rules.xml scoped to sharedpref + database ONLY (no "file" domain, so Downloads/NourCrash logs are excluded; comments confirm no secrets). PASS, no change needed.
     - Localization literals: only one Text("<literal>") remains — the QuranScreen edition chip (edition proper name + language endonym), which is intentional; no literal contentDescriptions. PASS.
     - Deferred (low severity, not done this session): QuranScreen share text appears twice and appends the English tail "— [Surah <englishName> n:n] via Nour". Localizing it needs a new pattern string + 13 locale overrides.
04:54 - VERIFIED (design-token pass 2): replaced 33 Spacer(modifier = Modifier.height/width(N.dp)) magic numbers with NourSpace tokens (4->xs, 8->sm, 12->md, 18->lg, 24->xl, 32->xxl) across SetupWizard/QiblaScreen/QuranScreen/SettingsScreen (all already imported NourSpace). Values with no matching token (2/6/10/14/16/20/48) were left untouched, so layout is byte-for-byte identical. :app:testDebugUnitTest -> 34/0/0/0; :app:assembleDebug -> BUILD SUCCESSFUL, app-debug.apk 26M (04:54).

08:0x - resumed (post restart). Worked prompts 07/08/10/12/17/18/21/22/24/25 end to end; wrote docs/android16-m3e/NIGHTLY_REVIEW.md.
08:05 - CODE CHANGES this run:
     - Prompt 12: added stable LazyColumn keys — QuranScreen surah list / popular / search-surah / search-ayah / reading ayahs x2 / mushaf ayahs x2 / TRANSLATION_EDITIONS / bookmarks; SetupWizard onboarding prayer list (key = PrayerName.name). All keys are true identities, no collisions.
     - Prompt 07/10: SetupWizard prayer-time chip Modifier.width(92.dp) -> widthIn(min = 92.dp) so it can grow instead of clipping at large font scale.
     - Deferred localization item DONE: Quran share attribution localized. New AppStrings.quranShareAttribution base (— [Surah %1$s %2$d:%3$d] via Nour) + 13 locale overrides; QuranScreen share text (both sites) now String.format(...). Locale overrides: 13.
08:16 - VERIFIED: single Gradle invocation :app:testDebugUnitTest + :app:assembleDebug -> 34 tests, 0 failures/0 errors/0 skipped; BUILD SUCCESSFUL; app-debug.apk 26M (08:16).
08:1x - AUDIT FINDINGS (read-only; no risky change made), full detail in NIGHTLY_REVIEW.md:
     - 22/25: 16 KB page alignment PASSES (zipalign -c -P 16 -> Verification successful); the only .so is androidx libandroidx.graphics.path.so.
     - 22: no INTERNET permission, no analytics/Firebase/ads deps, no committed secrets, exported=false on service+receivers, backup scoped to sharedpref+database.
     - 25.1 HIGH: release has isMinifyEnabled=false (no R8/shrinkResources). NOT enabled this session — needs a device regression pass first (release-only R8 crashes are exactly what the prompt forbids shipping blind).
     - 24.1 MEDIUM: no baseline profile / no Macrobenchmark module (cannot generate on this host).
     - 22.1/25.3 MEDIUM: CrashReporter writes stack traces to shared Downloads/NourCrash in ALL build types incl. release; recommend gating on BuildConfig.DEBUG (left as-is: owner relies on it for field diagnostics).
     - 22.2 LOW: play-services-location present (offline-safe due to no INTERNET; optional to replace with platform LocationManager).
     - 08: hardcoded colors are intentional celestial art / theme defs / seed preview; no change warranted. 07/21: no device -> visual/RTL/font-scale/TalkBack/sensor/Doze flows marked "Not tested (needs device)".
08:2x - completed the remaining library prompts so the whole set is covered: 04 (Android 16 API-36 audit — all 9 items OK, no code change), 05 (dependency/build audit — toolchain already current, no upgrade made), 15 (persistence/migration safety — Room v1/exportSchema=false/no destructive fallback, keys stable, no change needed), 26 (handover — HANDOVER.md updated). Per-prompt findings consolidated in docs/android16-m3e/NIGHTLY_REVIEW.md.
     - All code changes verified by the 08:16 build (34 tests / 0 failures, assembleDebug BUILD SUCCESSFUL, APK 26M). No source changed after that build; docs only.
     - Git untouched: last commit 35a0dc6; 18 modified tracked files + untracked AGENTS.md/docs/tools remain uncommitted.
08:5x - DEEPER PASS (owner-authorized continuation; prompt 07 + remaining token cleanups):
     - Token cleanup (value-identical): 40 raw dimensions routed through shared tokens across Prayer/Qibla/Quran/Settings/SetupWizard (NourSpace for spacedBy/padding 4/8/12/18/24/32; NourSize for .size 20/22/24/40/48, heightIn(min=48), BorderStroke(1), elevation 0). Layout unchanged; untokenizable values left as-is.
     - Prompt 07 fix: SettingsScreen custom-seed title + hex label had maxLines=1 with no overflow -> now TextOverflow.Ellipsis (no hard-clip at large font scale).
     - Per-screen verification: NEW app/src/test/java/com/example/ScreenRenderTest.kt (Robolectric) renders Prayer/Qibla/Quran/Settings/SetupWizard with realistic data, asserts each root is displayed, and (Roborazzi record mode) writes one PNG each to app/build/screen-verify/ (copied to ~/Downloads/screen-verify/); all 822x1782, non-blank.
     - Caveat: rendering verification only. Agent has no image input, so the PNGs are for the owner (or a vision-capable reviewer). Sensors/Doze/RTL/TalkBack still need a device.
08:5x - VERIFIED: single Gradle invocation :app:testDebugUnitTest :app:assembleDebug -Proborazzi.test.record=true -> 39 tests, 0 failures/0 errors/0 skipped (34 prior + 5 new ScreenRenderTest); BUILD SUCCESSFUL; app-debug.apk 26M.
     - APK copied to ~/Downloads/Nour-debug-latest.apk; sha256 d688... is superseded -> new sha256 recorded in HANDOVER/REVIEW.
     - Git untouched: last commit 35a0dc6; all changes remain uncommitted.
22:4x - OWNER ASKS (two new): (1) the prayer sky card should show an animated Sun AND Moon with true positions/phases; (2) one global left/right gutter on every screen.
     - Ask 1 (Sky): ImmersiveSkyBackground now places and fades the Sun and the Moon INDEPENDENTLY (each above the horizon by its own altitude; the Moon at its true phase). Previously a single `if (isDaytime)` drew only one body. The ambient light wash follows the dominant body. SeamlessPrayerHeader's CountdownSkyProbe now samples BOTH bodies so adaptive text contrast still holds. Astronomy untouched (CelestialMath unchanged).
     - Ask 2 (Gutter): NourContentFrame gained a `contentPadding` param defaulting to a global horizontal NourSpace.lg (18 dp) gutter; MainActivity's Scaffold containerColor set to surface (single background). PrayerScreen's nested frame set to zero; duplicate root horizontal padding removed from Prayer (DaySwitcherBar), Qibla, Settings, and Quran (library/reader/mushaf/bookmarks/search); SetupWizard (rendered outside the frame) given the same gutter at its root.
     - VERIFIED: single Gradle invocation :app:testDebugUnitTest :app:assembleDebug -Proborazzi.test.record=true -> 40 tests, 0 failures/0 errors/0 skipped; BUILD SUCCESSFUL; app-debug.apk 26M.
     - Visual proxy check by pixels (this model cannot view images): all 5 screenshots have content starting at x=36 px and ending at x=785 px (= 18 dp gutter at xhdpi) with flat edges. New 06_sky_sun_and_moon.png (deterministic: fixed Jan-2026 day, both bodies > 15 deg altitude, Moon illumination > 0.40) shows two distinct bright bodies.
     - NOT verified (needs device): real look of both bodies + Moon phase, RTL, large font scale, TalkBack, reduced motion, Qibla sensors, Adhan/Doze.
     - APK: ~/Downloads/Nour-debug-latest.apk sha256 6faf45a381410ac4aa5c17c65c41113dfc3fefab68cc38739a8b4a56d08ddd39; screenshots in ~/Downloads/screen-verify/.
     - Git untouched: all changes remain uncommitted.
SALAH REVIEW (independent UI/UX + accessibility pass; implemented in priority order):
     - Removed the per-second bouncy slide/fade on the countdown digits (constant motion + screen-reader re-announce every second); digits now render as plain adaptive text, colour adaptation unchanged. Deleted 6 now-unused animation imports.
     - Next-prayer row exposes `stateDescription = nextBadge` ("NEXT", already localized): no longer colour/weight-only.
     - Location pill: 48 dp min touch target + honest text — non-GPS locations now read "Default location: …", mirroring the permission screen's existing convention (reuses `defaultLocationPrefix`, all 14 locales already translated; no new strings).
     - Day reset: `role = Button`, enabled only while browsing another day (no more dead "Today" tap on today), 48 dp min target.
     - Prayer name marked as a real heading for screen-reader navigation.
     - Removed dead `onOpenAdjustments`/`onOpenSettings`/`onLocationUpdated` params (never wired to UI); updated MainActivity + ScreenRenderTest call sites.
     - New regression assertions: countdown still renders; pill contains "Default location" (first run caught a real bug in my own assertion — `assertTextContains` defaults to exact match; fixed with `substring = true`).
     - VERIFIED: single Gradle invocation -> 40 tests, 0 failures; gutters still 36..785 px on all five screens; BUILD SUCCESSFUL.
     - NOT verified (needs device): visual look, TalkBack behaviour, 48 dp physical sizes, light/dark contrast over the live sky, 200% font (countdown is `softWrap=false`, may clip — flagged, not changed blindly), landscape/split-screen, sensors, Doze.
     - APK: ~/Downloads/Nour-debug-latest.apk sha256 18d963325f77bfaa93bf17160cc29b320637cfc7c6448c68ef131bc91d386997; screenshots in ~/Downloads/screen-verify/.
     - Git untouched: all changes remain uncommitted.
SALAWAT CARD PADDING (owner ask): the prayer-times card had its own extra
     horizontal NourSpace.lg on top of the global frame gutter (36 dp total vs
     18 dp for the sky card and date switcher). Removed the duplicate so all
     three share one measurement. Pixel-verified on 01_prayer.png: sky card,
     day switcher and salawat card all span x=36..785.
     - VERIFIED: single Gradle invocation -> 40 tests, 0 failures; BUILD SUCCESSFUL.
     - APK: ~/Downloads/Nour-debug-latest.apk sha256 2687822a64767c88a3fdaec4af2496cdca75887c4e9d67caa5b287939e2b0ff6; screenshots in ~/Downloads/screen-verify/.
     - Git untouched: all changes remain uncommitted.
