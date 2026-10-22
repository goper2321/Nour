package com.example

import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the localization tables themselves.
 *
 * Every user-facing string is formatted with `String.format`, so a translation
 * that drops, duplicates or renames a placeholder (`%1$s`) does not fail the
 * build — it throws `IllegalFormatException` the first time a user in that
 * language opens the screen. These checks are pure JVM and run on any host.
 */
class AppStringsIntegrityTest {

    private val specifier = Regex("""%(?:\d+\$)?[sdf]""")

    private fun specifiers(value: String): List<String> =
        specifier.findAll(value).map { it.value }.sorted().toList()

    /** All `var x: String` properties, read through their generated getters. */
    private fun stringFields(strings: AppStrings): Map<String, String> =
        strings::class.java.methods
            .filter {
                it.parameterCount == 0 &&
                    it.returnType == String::class.java &&
                    it.name.startsWith("get")
            }
            .associate { it.name.removePrefix("get") to it.invoke(strings) as String }

    @Test
    fun everyLocaleKeepsTheEnglishFormatSpecifiers() {
        val english = stringFields(AppStrings.forLanguage(AppLanguage.ENGLISH))
        val problems = mutableListOf<String>()

        AppLanguage.entries
            .filter { it != AppLanguage.ENGLISH }
            .forEach { language ->
                val localized = stringFields(AppStrings.forLanguage(language))
                english.forEach { (field, englishValue) ->
                    val localizedValue = localized[field] ?: englishValue
                    val expected = specifiers(englishValue)
                    val actual = specifiers(localizedValue)
                    if (expected != actual) {
                        problems += "$language.$field expects $expected but has $actual"
                    }
                }
            }

        assertEquals("placeholder mismatches:\n" + problems.joinToString("\n"), 0, problems.size)
    }

    @Test
    fun everyLocaleIsActuallyTranslated() {
        val english = stringFields(AppStrings.forLanguage(AppLanguage.ENGLISH))
        val untranslated = mutableListOf<String>()

        AppLanguage.entries
            .filter { it != AppLanguage.ENGLISH }
            .forEach { language ->
                val localized = stringFields(AppStrings.forLanguage(language))
                val differing = english.count { (field, value) -> localized[field] != value }
                // 296 fields are overridden per locale; the floor leaves room for
                // words that are genuinely the same in a given language.
                if (differing < 250) {
                    untranslated += "$language differs on only $differing of ${english.size} fields"
                }
            }

        // A near-empty locale table means a block was accidentally reset to
        // English, which would silently ship an untranslated screen.
        assertTrue("suspiciously untranslated locales:\n" + untranslated.joinToString("\n"), untranslated.isEmpty())
    }

    @Test
    fun localizedTextIsNotBlank() {
        val blank = mutableListOf<String>()
        AppLanguage.entries.forEach { language ->
            stringFields(AppStrings.forLanguage(language)).forEach { (field, value) ->
                if (value.isBlank()) blank += "$language.$field"
            }
        }
        assertTrue("blank strings:\n" + blank.joinToString("\n"), blank.isEmpty())
    }
}
