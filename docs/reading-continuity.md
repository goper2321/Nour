# Quran reading continuity — storage trace (NUR-017)

Audit trail for where the Quran reading position and khatmah progress live and
who writes them. Companion to `docs/preferences.md` (the preference store as a
whole); this file focuses on reading continuity under **process death**.

## 1. Preferences: `nour_app_prefs` (via `features/settings/NourPreferences.kt`)

| Key (writing accessor)            | Meaning                       | Type   | Production writer                                       | Write mode |
|-----------------------------------|-------------------------------|--------|---------------------------------------------------------|------------|
| `quran_last_read_surah` (`lastReadSurah`) | Last opened surah    | Int    | `QuranScreen` `LaunchedEffect(lastReadSurahNumber)` (QuranScreen.kt); also the pager settle callback | **`commit()`** — synchronous (NUR-017) |
| `quran_last_read_ayah` (`lastReadAyah`)   | Last opened ayah     | Int    | `QuranScreen` `LaunchedEffect(lastReadAyahNumber)`      | **`commit()`** — synchronous (NUR-017) |
| `quran_last_read_page` (`lastReadPage`)   | **Legacy** Mushaf page (retained) | Int | **None** — the Mushaf UI and its settle writer were removed 2026-10-26 | Accessor still uses `commit()` |
| `quran_reading_mode` (`readingMode`)      | CARDS / CONTINUOUS   | String | `QuranScreen` `LaunchedEffect(readingMode)`             | `apply()` — async flush |
| `quran_arabic_font_size` (`arabicFontSize`) | Arabic font size   | Float  | `QuranScreen` `LaunchedEffect(arabicFontSize)`          | `apply()` — async flush |
| `quran_reading_theme` (`quranReadingTheme`) | SYSTEM / PAPER / NIGHT | String | `QuranScreen` `LaunchedEffect(quranReadingTheme)` (Wave C) | `apply()` — async flush |
| `quran_typeface` (`quranTypeface`)        | SYSTEM / AMIRI       | String | `QuranScreen` `LaunchedEffect(quranTypeface)` (Wave C)  | `apply()` — async flush |

`quran_last_read_page` is deliberately orphaned, not removed: no production
code reads or writes it any more, but the raw key and its accessor stay so an
existing install's stored value is not lost or renamed. `hasReadPage` has no
production caller. `QuranMushafTest` still guards the 604-page division data
(`getMushafPage` / `getPageForAyah`) in the repository, even though no UI
consumes it.

State transitions that drive the position writers: opening the library's
"continue reading" card, tapping a surah, selecting an ayah, jumping from the
bookmark list, consuming an `openSurahRequest` from another screen, and
settling on a surah after a horizontal swipe. Each of those recomposes and the
`LaunchedEffect` writers fire — there is **no** `onSavedInstanceState` /
ViewModel-only path anywhere in the continuity chain.

Wave C adds reader affordances on top of the same state, without new position
stores:

- **Go to verse (C1).** The reader toolbar action opens
  `QuranGoToVerseDialog` (surah + ayah, localized digits accepted). A confirmed
  jump sets `deepLinkAyah` plus `deepLinkSurahNumber`, which forces the reader
  into verse cards for that landing; the restore effect is keyed on
  `(surah.number, restoreAyah, readingMode)` and runs in CARDS mode only. The
  jumped-to verse is highlighted with `secondaryContainer` until the next tap.
  The target writes the normal `lastReadSurah` / `lastReadAyah` pair. There is
  no Mushaf page-jump path any more.
- **Deep links force cards and are scoped.** `openAyahRequest`, go-to-verse and
  bookmark jumps set `deepLinkAyah`; the reader passes
  `forceCards = deepLinkAyah != null`. The landing is therefore always in verse
  cards, even when the persisted `quran_reading_mode` is continuous; the stored
  mode itself is not changed, so the next ordinary open resumes in the user's
  chosen mode. The link is scoped to `deepLinkSurahNumber`: swiping to a
  different surah consumes it (and back clears it), so returning later restores
  the saved position instead of re-landing on the target verse.
- **"Last read" marker (C2).** The acknowledgement is appended to the restored
  verse's reference pill (`2:255 · Last read`). It stays inside the same list
  item — a separate row would shift the `firstVisibleItemIndex → ayah` mapping —
  and because it reuses the existing pill it adds no row height and leaves no
  hairline gaps at the card edges. It is transient UI, not a stored flag: it
  clears when tapped, when the mode changes, or when the reader swipes away to
  another surah. Nothing new is persisted.
- **Surah pager continuity.** The reader is a 114-page `HorizontalPager`, one
  surah per page, with one fixed toolbar. Each surah keeps its own
  `LazyListState` in a session map, so swiping away and back restores the same
  verse rather than the surah header; explicit dropdown/end-of-surah navigation
  restores a previously visited surah's position too, and only a first visit
  starts at the top. On settle the pager updates `lastReadSurah`; a different
  surah resets `lastReadAyah` to 1 until the settled page reports its real
  first visible verse through `onVisibleVerseChanged`. In continuous mode the
  surah text is a single scrolling item, so continuity is per-surah (the verse
  cannot be derived from an item index).

A reader position state that lives only in `remember {}` for a *session* (e.g.
`selectedSurahForReading`, the per-surah `LazyListState` map) is intentionally
not persisted; the durable part is the position the user would resume, and its
store keys are the table above.

## 2. Flush-promptness assessment (process death)

- **Last-read position**: `SharedPreferences.edit().putInt(...).commit()` writes
  synchronously before the setter returns, so a force-kill on the very next
  instruction after a verse change cannot drop the position. This was
  `apply()` before NUR-017; `apply()` schedules a background disk write and a
  hard kill inside that window could silently lose the last write. Swipe
  navigation updates the pair only when the pager **settles** (never while the
  gesture is in flight), and the verse is updated from the settled page's first
  visible item.
- **Reading mode / font size / reading theme / typeface**: still `apply()`.
  They are presentation preferences, not positions; a normal stop flushes them.
  Not part of NUR-017's force-kill requirement.
- **Bookmarks**: Room insert/delete rows (`BookmarkDao`) through a coroutine.
  Room's write path is synchronous against its SQLite file (WAL) once the DAO
  call returns — no preference-flush window applies.
- **Khatmah**: see below; progress has no feature writer yet, so there is no
  position to lose under process death beyond the seeded plan.

## 3. Room tables (schema v1, `database/Entities.kt`)

| Table            | Relevant fields                                                                 | Writer                                                      |
|------------------|----------------------------------------------------------------------------------|-------------------------------------------------------------|
| `bookmarks`      | surahNumber, ayahNumber, surahName, ayahText, translationText, note, createdAt   | `QuranScreen` toggle: `bookmarkDao().insertBookmark` / `deleteBookmark` / `deleteById` |
| `khatmah_plans`  | title, targetDays, startPage, **currentPage**, totalPages, startDateMillis, isActive, lastReadDate | **Seeded once in `NourDatabase` `onCreate`** ("Ramadan 30-Day Khatmah"); no feature currently updates `currentPage` — recorded honestly here as a coverage gap, not papered over |

## 4. Test coverage

`app/src/test/java/com/example/features/settings/NourPreferencesRoundTripTest.kt`
(Robolectric) round-trips a position through the same accessors and raw keys
production uses and verifies the store's read-side coercion (surah pinned to
1–114, ayah ≥ 1). It still round-trips the retired page key through
`lastReadPage` to pin the retained coercion (1–604) without implying a
production writer. `MigrationTest` remains the schema-side guard.

Reader-level continuity is exercised by `QuranReaderTest` (17 cases): swipe
next/back and persistence, the first-surah boundary, per-surah scroll restore,
deep-link consumption on swipe-away, the last-read marker in cards and
continuous modes, go-to-verse, and the three-tab library with no Mushaf tab.
`QuranMushafTest` (6 cases) keeps the page-division data invariants.
`QuranMushafPageTest` was deleted together with the Mushaf UI it drove.
