package com.example.features.quran.data

/**
 * NUR-035 — "About this Quran" provenance as data. This is the essence of
 * `resources/quran/SOURCES.md` restated as plain code constants so a user
 * can see inside the app where their scripture text came from, without any
 * file-IO trickery.
 *
 * The pinned SHA-256 lines are NOT retyped here (no duplicate hash literals):
 * they are interpolated at render time from [QuranCorpus]'s constants, so
 * the displayed provenance always comes from the single pinned source.
 * Nothing in this file fetches, links, or claims licence precedence over
 * the original licences — URLs are shown as plain text.
 */
internal object QuranSources {
    const val UTHMANI_VERSION =
        "Quran text (Uthmani): Tanzil Quran Text, Version 1.1 — used verbatim, unchanged."

    const val UTHMANI_SOURCE =
        "Source: tanzil.net — copyright Tanzil Project, Creative Commons Attribution 3.0. Updates: tanzil.net/updates"

    const val METADATA_LINE =
        "Partition metadata: Tanzil Quran Metadata v1.0 (Creative Commons Attribution) — bundled verbatim, including the 604-page division (metadata.xml)."

    const val SAHIH_LINE =
        "English translation: Saheeh International, published by Dar Abul-Qasim (Jeddah) — displayed verbatim."

    const val BUNDLED_LINE =
        "Bundled: uthmani.txt (Tanzil Uthmani 1.1), metadata.xml (Tanzil metadata 1.0, includes the 604-page division), en.sahih.txt (Saheeh International)."

    const val NOT_BUNDLED_LINE =
        "Not bundled: tafsir and the other 12 translation editions — the app does not invent them."

    const val PAGE_LAYOUT_LINE =
        "Page divisions are metadata, not a printed-page facsimile: Nour does not reproduce the printed Mushaf's exact line breaks or line art."

    const val INTEGRITY_LINE =
        "Integrity: the app verifies the SHA-256 of every bundled Quran file while loading and refuses to serve anything that does not match."

    fun uthmaniHashLine(hash: String) = "Pinned SHA-256 (uthmani.txt): $hash"
    fun sahihHashLine(hash: String) = "Pinned SHA-256 (en_sahihintl.txt): $hash"

    /** Provenance lines in card order; hash lines interpolated from pinned constants. */
    fun lines(textHash: String, sahihHash: String): List<String> = listOf(
        UTHMANI_VERSION,
        UTHMANI_SOURCE,
        uthmaniHashLine(textHash),
        METADATA_LINE,
        BUNDLED_LINE,
        NOT_BUNDLED_LINE,
        PAGE_LAYOUT_LINE,
        SAHIH_LINE,
        sahihHashLine(sahihHash),
        INTEGRITY_LINE,
    )
}
