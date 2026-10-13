# Resume prompt — Nour nightly autonomous session

You are resuming an interrupted autonomous session on the **Nour** Android app.
Read `AGENTS.md` first, then `docs/android16-m3e/NIGHTLY_STATUS.md` for the live
progress and the exact step to continue. You are the responsible engineer: keep
working, verify honestly, and never claim a result you did not observe.

## Mission (owner-approved)

1. Close the known gaps: the **notification-permission re-request path** and the
   **remaining localization**.
2. Then keep improving **UI/UX all night**, applying the prompts in
   `/home/eliot/Documents/Obsidian Vault/prompts/Android 16 + M3E Prompt Library`.
   Read that folder's `00 — Start Here.md` for the workflow, then work through
   the relevant prompts in this order:
   `04`/`13` (edge-to-edge + insets), `14` (predictive back), `19`
   (accessibility), `20` (localization/RTL), `10` (adaptive layouts), `12`
   (Compose performance — only with evidence), `07` (screen UI/UX review),
   `08` (typography/color), `09` (motion), then `17`/`18` (debug + independent
   review of your own changes), `25`/`26` (release readiness + handover).
   The audit prompts are read-only: use a sub-agent for them, then verify each
   finding yourself against the source before changing anything.
3. Native Material 3 Expressive adoption is **already in the tree** from an
   earlier pass (`MaterialExpressiveTheme` + `MotionScheme` in
   `ui/theme/Theme.kt`, Compose BOM `2026.06.01` + material3
   `1.5.0-alpha15`). Do **not** extend it (no `MaterialShapes`, `ButtonGroup`,
   `FloatingToolbar`, `LoadingIndicator` adoption) — the owner is holding that
   for a separate, device-verified change. Fix review findings about them, but
   do not add new M3E surface area.

## Non-negotiable rules

- **Offline-first.** No `INTERNET` permission, no network clients, analytics,
  Firebase, or tracking of any kind.
- **User data is the user's.** Room is schema v1 with `exportSchema = false`
  and no migrations; do not touch entities, never clear or rewrite the database
  or `nour_app_prefs` / `nour_app_preferences` preference keys, and never use
  `fallbackToDestructiveMigration()`.
- **Never fabricate religious content.** Missing translation/tafsir/pages must
  say "unavailable". Do not edit anything under
  `app/src/main/resources/quran/` (hash-pinned by `QuranCorpus.kt`).
- **Smallest complete change.** Do not rewrite working engines: prayer
  calculation, Hijri conversion, Qibla sensor math and its honest mode
  reporting, `PrayerNotificationScheduler` (exact alarms, boot/time/timezone
  recovery — boot must never fire a notification), Quran reading continuity.
- **Accessibility is a requirement, not polish:** 48 dp minimum touch targets,
  content descriptions on icon-only controls, layouts that survive 200% font
  scale, never meaning by color alone, and Arabic scripture direction
  independent of interface language. All 14 locales and RTL must keep working.
- **Every animation duration** goes through `NourMotion` so the system
  "Remove animations" setting is honored (decorative motion stops, nothing
  disappears). Use the shared `NourSpace`/`NourSize`/`NourMotion` tokens; no
  magic numbers for durations, sizes, or colors.
- **Git is frozen.** Do not commit, branch, stash-drop, reset, or discard the
  owner's uncommitted work. The owner has their own backup.
- **Resources are tight (~3.7 GiB RAM).** Never run two Gradle builds at once.

## Build and test commands (memory-safe)

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
export ANDROID_HOME="$HOME/Android/Sdk"
cd "/home/eliot/Documents/me and me/Nour"

# iterate:
./gradlew :app:compileDebugKotlin --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx2048m -XX:MaxMetaspaceSize=384m' --max-workers=1

# verify (run one task at a time, never together with a compile in parallel):
./gradlew :app:testDebugUnitTest --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx1536m -XX:MaxMetaspaceSize=384m' --max-workers=1
./gradlew :app:assembleDebug --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx2048m -XX:MaxMetaspaceSize=384m' --max-workers=1
```

Compose BOM `2026.06.01` + material3 `1.5.0-alpha15` are pinned because newer
lines need `compileSdk 37` (not installed) and `1.4.0` keeps M3E APIs
`internal`. Do not change them without a full rebuild + test pass.

After every meaningful step run:

```bash
bash tools/nightly-heartbeat.sh "<short description of what you just did>"
```

This is what keeps the auto-relaunch watcher (`tools/nightly-relaunch.sh`,
systemd timer `nour-nightly-relaunch.timer`) from starting a duplicate session.
If you are about to start a long build, keep the heartbeat fresh while it runs.

## What to do right now

1. `docs/android16-m3e/NIGHTLY_STATUS.md` — read the progress log; it names the
   last finished step and the next one.
2. Re-run the relevant verification before trusting any recorded claim.
3. Continue with the next unfinished item. Add a line to the progress log after
   each completed, verified step.
4. When everything you can safely do is done: set `STATUS: BLOCKED` with the
   reason if a genuine blocker remains, or `STATUS: DONE` when the gaps are
   closed and every change compiles with `:app:testDebugUnitTest` green, then
   write the final report in `docs/android16-m3e/HANDOVER.md` including an
   explicit "not verified (needs a device)" section.

Never stop at "it should work". Show the command and the output, or say it is
not verified.
