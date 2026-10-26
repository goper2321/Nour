package com.example

import com.example.localization.Numerals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * One parser for every numeric field (go-to-verse, and any future one). The
 * keyboard follows the system language, not the app language, so Western,
 * Arabic-Indic and Extended Arabic-Indic digits must all work; ambiguous or
 * out-of-range input must fail instead of being guessed.
 */
class NumeralsTest {
    @Test
    fun parsesWesternDigits() {
        assertEquals(255, Numerals.parseLocalizedNumber("255"))
        assertEquals(1, Numerals.parseLocalizedNumber("1"))
    }

    @Test
    fun parsesArabicIndicDigits() {
        assertEquals(255, Numerals.parseLocalizedNumber("٢٥٥"))
        assertEquals(604, Numerals.parseLocalizedNumber("٦٠٤"))
    }

    @Test
    fun parsesExtendedArabicIndicDigits() {
        assertEquals(255, Numerals.parseLocalizedNumber("۲۵۵"))
    }

    @Test
    fun parsesOtherUnicodeDecimalDigits() {
        // Bengali ২৫৫ — Character.digit covers every decimal block.
        assertEquals(255, Numerals.parseLocalizedNumber("২৫৫"))
    }

    @Test
    fun trimsSurroundingWhitespace() {
        assertEquals(5, Numerals.parseLocalizedNumber(" 5 "))
        assertEquals(5, Numerals.parseLocalizedNumber(" ٥ "))
    }

    @Test
    fun rejectsBlankInput() {
        assertNull(Numerals.parseLocalizedNumber(""))
        assertNull(Numerals.parseLocalizedNumber("   "))
    }

    @Test
    fun rejectsNonWholeNumbers() {
        assertNull(Numerals.parseLocalizedNumber("2:255"))
        assertNull(Numerals.parseLocalizedNumber("2.5"))
        assertNull(Numerals.parseLocalizedNumber("-5"))
        assertNull(Numerals.parseLocalizedNumber("+5"))
        assertNull(Numerals.parseLocalizedNumber("5a"))
    }

    @Test
    fun rejectsOverflowInsteadOfCrashing() {
        assertNull(Numerals.parseLocalizedNumber("99999999999"))
    }
}
