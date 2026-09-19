package com.example.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class NumeralSystem(val code: String) {
    WESTERN("western"),
    EASTERN("eastern");

    companion object {
        fun fromCode(code: String?): NumeralSystem =
            entries.firstOrNull { it.code == code } ?: WESTERN
    }
}

object Numerals {
    private val EASTERN_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /**
     * Normalizes ANY Unicode decimal digits (Eastern Arabic ٠-٩, extended ۰-۹,
     * Devanagari, Bengali, Thai, ...) to Western 0-9. Needed because
     * String.format with e.g. an Arabic locale already emits Eastern digits,
     * which a Western-only conversion could never switch back.
     */
    fun toWestern(input: String): String {
        var needsConversion = false
        for (c in input) {
            val d = Character.digit(c, 10)
            if (d in 0..9 && (c !in '0'..'9')) {
                needsConversion = true
                break
            }
        }
        if (!needsConversion) return input
        val sb = StringBuilder(input.length)
        for (c in input) {
            val d = Character.digit(c, 10)
            sb.append(if (d in 0..9) ('0' + d) else c)
        }
        return sb.toString()
    }

    /** Converts Western digits 0-9 in [input] to Eastern Arabic-Indic ٠-٩. */
    fun toEastern(input: String): String {
        val western = toWestern(input)
        var needsConversion = false
        for (c in western) {
            if (c in '0'..'9') {
                needsConversion = true
                break
            }
        }
        if (!needsConversion) return western
        val sb = StringBuilder(western.length)
        for (c in western) {
            sb.append(if (c in '0'..'9') EASTERN_DIGITS[c - '0'] else c)
        }
        return sb.toString()
    }

    fun convert(input: String, useEastern: Boolean): String =
        if (useEastern) toEastern(input) else toWestern(input)
}

/**
 * false = 0123456789 (default, preserves previous behavior),
 * true = ٠١٢٣٤٥٦٧٨٩.
 * Quranic ayah end-markers (۝ + digits) intentionally stay Eastern
 * for script authenticity regardless of this setting.
 */
val LocalUseEasternNumerals = compositionLocalOf { false }

@Composable
fun formatNumerals(text: String): String {
    return Numerals.convert(text, LocalUseEasternNumerals.current)
}
