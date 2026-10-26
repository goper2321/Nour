package com.example.features.quran.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.features.settings.QuranTypeface

/**
 * Amiri Quran (SIL OFL 1.1, bundled at `res/font/amiri_quran.ttf`).
 *
 * A scripture-only exception to the platform typeface: it is applied to Arabic
 * verse text and nothing else. Labels, verse numbers, surah names and
 * translations keep the app typeface, so navigation and metadata stay
 * consistent and readable while the scripture gets its proper Naskh face.
 */
internal val AmiriQuranFamily: FontFamily = FontFamily(
    Font(R.font.amiri_quran, weight = FontWeight.Normal)
)

/**
 * Resolves the opt-in typeface for scripture [Text] nodes. Returns null for
 * SYSTEM so callers keep the inherited style untouched — the default build is
 * byte-for-byte the same layout as before C4.
 */
internal fun scriptureFontFamily(typeface: QuranTypeface): FontFamily? =
    when (typeface) {
        QuranTypeface.SYSTEM -> null
        QuranTypeface.AMIRI -> AmiriQuranFamily
    }
