# Nour preference-keys audit (NUR-022)

**Post-implementation refresh (2026-10-24, B8):** the table below now lists the
full catalogue after NUR-004…NUR-037 landed. Added since the audit:
`theme_high_contrast` (NUR-008), plus the diagnostics/scheduler keys
`location_last_fix_millis`, `adhan_next_*`, `adhan_exact_ok` (NUR-033).
**Wave B/C Quran additions (2026-10-26):** `quran_last_read_page`,
`quran_reading_theme`, `quran_typeface` (all additive; no key renamed). The page
key is now **legacy**: the Mushaf UI was removed on 2026-10-26, so nothing in
production reads or writes it; it stays so upgrades don't lose the stored value.
All reads validate stored values and fall back to defaults (NourPreferences).

Two stores exist; AGENTS.md names both and calls the duplication suspicious.
They are NOT near-duplicates overall — the domains are disjoint:

- `nour_app_prefs` — theme, reading, prayer, location, setup state
  (base of `localization/AppStrings.kt`-independent UI state).
- `nour_app_preferences` — app language only (LocalizationManager).

## Table: file → store → key → type → reader → writer

| File (accessor) | Store | Key | Type | Reader | Writer |
|---|---|---|---|---|---|
| features/settings/NourPreferences.kt | nour_app_prefs | theme_mode | String | NourPreferences → MainActivity, Settings UI | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | dynamic_colors | Boolean | NourPreferences → theme | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | theme_high_contrast | Boolean (NUR-008) | NourPreferences → MainActivity/Theme | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | theme_seed_color | Int? | NourPreferences → theme | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | numeral_use_eastern | Boolean | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | time_use_24h | Boolean | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | prayer_alarms_enabled | Boolean | NourPreferences → PrayerNotificationScheduler | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | setup_completed | Boolean | NourPreferences → MainActivity | NourPreferences (typed) |
| MainActivity.kt (direct, lines 219/334) | nour_app_prefs | has_asked_location_first_time | Boolean | MainActivity (bootstrap `setupDone`) | MainActivity (`onSetupComplete`) |
| features/settings/NourPreferences.kt | nour_app_prefs | location_is_auto_detected | Boolean | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | saved_location_lat | Float | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | saved_location_lng | Float | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | saved_location_city | String | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | saved_location_country | String | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | saved_location_tz | Float | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | calculation_method | Enum name | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | madhab | Enum name | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | high_latitude_rule | Enum name | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | adjustment_fajr … adjustment_isha | Int ×6 | NourPreferences → MainActivity | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_reading_mode | String? | NourPreferences → QuranScreen | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_arabic_font_size | Float | NourPreferences → QuranScreen | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_last_read_surah | Int | NourPreferences → QuranScreen | NourPreferences (typed) |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_last_read_ayah | Int | NourPreferences → QuranScreen | NourPreferences (typed, `commit()` — landing last-read must not race process death) |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_last_read_page | Int (1–604; default 1) | **None** (legacy Mushaf page; accessor retained) | **None** — no production writer since the Mushaf UI removal; key is never renamed or cleared |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_reading_theme | String (SYSTEM / PAPER / NIGHT; default SYSTEM) | NourPreferences → QuranScreen reader | NourPreferences (typed; unknown values fall back to SYSTEM) |
| features/settings/NourPreferences.kt | nour_app_prefs | quran_typeface | String (SYSTEM / AMIRI; default SYSTEM) | NourPreferences → QuranScreen reader | NourPreferences (typed; unknown values fall back to SYSTEM) |
| diagnostics readers (NUR-033) | nour_app_prefs | location_last_fix_millis | Long (epoch millis; 0 = never / auto-cleared) | NourPreferences.locationLastFixMillis → Settings diagnostics card | Location fix path (typed) |
| PrayerNotificationScheduler.kt | nour_app_prefs | adhan_next_<PRAYER> | Long ×5 (FAJR…ISHA; epoch millis) | NourPreferences.adhanNextPrayer → diagnostics card; scheduler dedupe | PrayerNotificationScheduler |
| PrayerNotificationScheduler.kt | nour_app_prefs | adhan_exact_ok | Boolean | NourPreferences.adhanExactOk → Settings diagnostics card | PrayerNotificationScheduler |
| localization/LocalizationManager.kt | nour_app_preferences | selected_app_language | String | LocalizationManager → MainActivity | LocalizationManager (typed) |

All reads validate stored values and fall back to defaults (NourPreferences).
Widgets use no preferences at all.

## Near-duplicate found (documentation, NO code change)

`setup_completed` and `has_asked_location_first_time` hold the same logical
fact ("first-run setup done"); the legacy flag predates the typed accessor.
Both must keep working (AGENTS.md protected keys). Proposal, NOT applied:

- Canonical: `setup_completed` (typed, validated).
- Phase 1: MainActivity keeps reading `setup_completed || has_asked_location_first_time`
  (current behavior — exactly what line 218 does) and STOPS writing the legacy
  flag once a release has shipped x86_64 + ARM with the typed key.
- Phase 2 (later checklist): delete the legacy flag after an install-base
  release cycle — never delete while its bootstrap reader exists (G-guards).

## Orphan keys

None. Grep proves every key has ≥1 reader and ≥1 writer
(`grep -rn "getSharedPreferences\|SharedPreferences" app/src/main/java` →
NourPreferences.kt, LocalizationManager.kt, MainActivity.kt ×2 only).
Nothing was removed; all known keys are preserved untouched.
