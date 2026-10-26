# Quran text sources

## Quran text (Uthmani)
- File: `uthmani.txt` — Tanzil Quran Text (Uthmani), Version 1.1, downloaded verbatim from
  https://tanzil.net/pub/download/index.php?quranType=uthmani&outType=txt-2&marks=true&sajdah=true&alef=true&tatweel=true&agree=true
- SHA-256: `6933e133dd56db778c801bf738848454e43648105a151e8d84d86a7cae39ec5f`
- Copyright (C) 2007-2026 Tanzil Project. License: Creative Commons Attribution 3.0.
- The complete Tanzil notice is preserved at the top of `uthmani.txt`. The text is
  distributed unchanged; changing it is not allowed. A link to https://tanzil.net/ is
  provided in the app's About/content-source screen so readers can track updates.
- Check updates at: http://tanzil.net/updates/

## Partition metadata
- File: `metadata.xml` — Tanzil Quran Metadata, Version 1.0, downloaded verbatim from
  https://tanzil.net/res/text/metadata/quran-data.xml
- SHA-256: `8867c1d88191472adec9db694b3cd9f135b1a2ef580574d32cf888dcb22c5c7a`
- Copyright (C) 2008-2009 Tanzil.info, license attribute `cc-by` (Creative Commons
  Attribution). Supplied unchanged and credited here with the source link above.

## Translations and tafsir
- File: `en_sahihintl.txt` — Saheeh International English translation (6236 verses,
  `surah|ayah|text` per line), copied verbatim from the Salah project's bundled copy
  of the text extracted from the Tanzil translation page https://tanzil.net/trans/en.sahih
- SHA-256: `f090ed258e647a393ddaa0b48f1e81ab7a5ccd5ad7d72d0da498e151bc0dbc18`
- Saheeh International (Jeddah, Saudi Arabia). Translation by three American women
  converts; published by Dar Abul-Qasim. Displayed in-app with the credit line
  "English — Saheeh International". No tafsir text is bundled yet.

## What is bundled / not bundled
- Bundled: `uthmani.txt` (Tanzil Uthmani 1.1), `metadata.xml` (Tanzil metadata 1.0,
  includes the 604-page division), `en.sahih.txt`/`en_sahihintl.txt` (Saheeh International).
- Not bundled: tafsir and the other 12 translation editions advertised by Tanzil
  (Pickthall, Hamidullah, Bernström, Diyanet, Jalandhry, Bubenheim, Cortés,
  Indonesian Ministry, Mujibur Rahman, Suhel Farooq Khan, Elmir Kuliev, Ma Jian).
  The app does not invent them; missing content is reported as "not bundled".

## Page layout limitation
- The 604-page division is taken from Tanzil metadata and is the real page
  partition, but Nour does not reproduce the printed Madinah Mushaf's exact line
  breaks, line art, or script variants. The page reader is "verse flow grouped
  by page", not a facsimile.

## Integrity
`QuranCorpus` verifies each file's SHA-256 at load and asserts 114 suras, 6,236
verses in canonical order, and 604 page / 30 juz / 240 hizb-quarter partitions before
the corpus is served.

## Bundled typeface (optional, not Quran text)

- File: `app/src/main/res/font/amiri_quran.ttf` — Amiri Quran Regular (version 1.003),
  136,920 bytes, downloaded verbatim from
  `https://raw.githubusercontent.com/google/fonts/main/ofl/amiriquran/AmiriQuran-Regular.ttf`.
- SHA-256: `e2a47644762d16bdfb6d33e0d8db8c6ff30beae84150ef5a705316bbd829455c`
- Google Fonts path last changed in commit
  `2e4388d444fb67f7e20cc75a72d1f3addfd18d31` (2025-07-17). The earlier plan pinned
  `480bb746e99ea700bb0d6b4dbf96302d58192103`, which does not exist in that repository.
- Copyright 2010-2022 The Amiri Quran Project Authors
  (https://github.com/aliftype/amiri), licensed under the SIL Open Font License 1.1.
  The full license text is bundled at `app/src/main/res/raw/amiri_quran_ofl.txt`
  (SHA-256 `b32e3b24e2a943b8acbf6ab8d5499bfed737d5176091b7341a8c1e7fbcdeb0fa`).
- The font ships as an **optional** scripture typeface (`quran_typeface`), applied only to
  Arabic verse text. It is not a Gradle dependency and never added to the verified
  corpus directory above.
