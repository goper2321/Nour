# Device acceptance matrix

Printable worksheet for **NUR-039**. The agent cannot reach devices — every row
is an **owner check**. Add your devices in the header, then fill `☐P ☐F` per
device and write failures in Notes. Base the pass/fail on what the app does,
not on what it should do; a row that cannot even start is a FAIL with the
reason.

Devices: **A = ______________ (Android ___)** · **B = ______________
(Android ___)** · **C = ______________ (Android ___)**

## A. Setup & persistence (AGENTS.md §4: Room data, prefs)

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| A1 | First run: setup wizard completes (language, location, Adhan, method); no crash | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| A2 | Cold restart: last-read Quran position (surah, verse, reading mode, font size) restored | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| A3 | Cold restart: bookmarks intact and open the right verses | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| A4 | Cold restart: khatmah plan progress intact | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| A5 | Cold restart: Adhan settings (per-prayer toggles, tone, volume), app language, theme, numerals, 24 h restored | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| A6 | Cold restart: prayer log history intact; logging a new prayer writes and survives another restart | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## B. Adhan (exact alarms, Doze, battery saver)

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| B1 | Next prayer with screen **off**: notification arrives at the computed minute (exact alarm granted) | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| B2 | Sound plays while device sleeps (foreground `mediaPlayback` service) | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| B3 | Notification actions work: mark-as-prayed / snooze / dismiss / stop | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| B4 | Battery saver ON and Doze: same minute-accuracy for at least one full day | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| B5 | Only one notification per prayer — no duplicates at boot or across days | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## C. Boot & time changes (boot must never fire a prayer notification)

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| C1 | Reboot device → **no prayer notification fires at boot**; remaining prayers re-booked (`RECEIVE_BOOT_COMPLETED`) | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| C2 | Change timezone (or travel) → alarms re-schedule for the local times; no ghost reminders at old times | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| C3 | Change system time → alarms re-schedule; prayer times/Hijri recompute for the new date | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## D. Languages, RTL, fonts

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| D1 | Switch through the 14 app languages; every screen stays readable (all ship in one APK) | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| D2 | Arabic interface: layouts mirror RTL; numbers stay LTR digits | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| D3 | Non-Arabic interface: Arabic verses still render right-to-left (script direction ≠ UI language) | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| D4 | 200 % font: no clipped text; rows wrap; every tab reachable; chrome reserve keeps content readable above the bar | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## E. TalkBack & controls

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| E1 | TalkBack: traverse Salah, Qibla, Quran, Settings without unlabeled items ("button, unlabeled") | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| E2 | Icon-only controls announce meaningful names; reduced-motion setting makes decorative motion stop (nothing disappears) | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## F. Qibla (honest modes)

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| F1 | Sensor heading updates smoothly; figure-8 calibration possible | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| F2 | "Facing the Kaaba" banner appears **only** from real calibrated sensor readings — never from the manual slider | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| F3 | Manual slider works and is honestly labelled as a reference heading; screen states which mode is active | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| F4 | Airplane mode / no sensors: the screen never claims a live heading it does not have | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## G. Quran reader

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| G1 | Search: Arabic corpus + surah names find expected matches | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| G2 | Open a surah, read, switch reading modes, adjust font; restart → position restored | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| G3 | Reader toolbar reserve: content never hides behind the floating pill at scroll ends | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

## H. Sky visualizer, diagnostics, crash reporter

| # | Check | A | B | C | Notes |
|---|-------|---|---|---|-------|
| H1 | On-screen sky visualizer (prayer header) matches time of day: dawn → midday → dusk | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| H2 | Countdown docks low on the stretched hero; five times-card rows align | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| H3 | Crash reporter: cause one deliberate crash (no dev-mode trigger is wired — e.g. temporary debug code), then check `Downloads/NourCrash/` (API 29+; app-private below) for `nour-crash-…log` with time/version/stack | ☐P ☐F | ☐P ☐F | ☐P ☐F | |
| H4 | The crash file can be shared **by user choice** from a file manager; the app itself sends nothing anywhere | ☐P ☐F | ☐P ☐F | ☐P ☐F | |

---

**Note on "widgets":** this build has **no home-screen widget** — the manifest
declares no app-widget provider, and `features/widgets/` contains the on-screen
sky canvas used by the prayer header (rows H1–H2). Replace these rows when a
real home-screen widget ships.

**All rows are owner-run.** Robolectric captures dark/RTL/200 %-font vectors on
the JVM, but only a device proves screen-off alarms, sensors, TalkBack, and
Doze behavior. Sign off: date + devices used: ______________
