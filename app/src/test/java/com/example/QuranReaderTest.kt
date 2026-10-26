package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.features.quran.data.QuranRepository
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.NourPreferences
import com.example.features.settings.QuranReadingTheme
import com.example.features.settings.QuranTypeface
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.components.NourContentFrame
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class QuranReaderTest {
    @get:Rule
    val compose = androidx.compose.ui.test.junit4.createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val strings = AppStrings()

    private fun settle(frames: Int = 8) {
        repeat(frames) { compose.mainClock.advanceTimeByFrame() }
    }

    private fun setContent() {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppLanguage provides AppLanguage.ENGLISH,
                    LocalAppStrings provides strings
                ) {
                    NourContentFrame { QuranScreen() }
                }
            }
        }
    }

    private fun openSurah(n: Int) {
        compose.onNodeWithTag("surah_item_$n").performClick()
        settle(12)
    }

    private fun swipeToNextSurah() {
        compose.onNodeWithTag("surah_pager").performTouchInput { swipeLeft() }
        settle(40)
    }

    private fun swipeToPreviousSurah() {
        compose.onNodeWithTag("surah_pager").performTouchInput { swipeRight() }
        settle(40)
    }

    private fun awaitPage(surahNumber: Int) {
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("surah_page_$surahNumber")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun readerShowsBasmalaOnce() {
        setContent()
        openSurah(2)
        val nodes = compose.onAllNodesWithText("بِسْمِ", substring = true)
            .fetchSemanticsNodes()
        assertEquals(1, nodes.size)
    }

    @Test
    fun readerHasNoTafsirAction() {
        setContent()
        openSurah(1)
        compose.onNodeWithContentDescription(strings.tafsirLabel).assertDoesNotExist()
    }

    @Test
    fun settingsSheetShowsOnlyBundledEdition() {
        setContent()
        openSurah(1)
        settle(4)
        compose.onNodeWithContentDescription(strings.readingSettings, substring = true).performClick()
        settle(12)
        compose.onNodeWithText("Sahih International", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Pickthall").assertDoesNotExist()
        compose.onNodeWithText(strings.translationsBundledNote).assertIsDisplayed()
    }

    // ---- C3: reading themes / C4: typeface ------------------------------

    @Test
    fun readingThemeAndTypefaceSelectionPersistAndStaySelected() {
        NourPreferences(context).readingMode = "CARDS"
        setContent()
        openSurah(1)
        settle(4)
        compose.onNodeWithContentDescription(strings.readingSettings, substring = true).performClick()
        settle(12)

        // Defaults are the untouched app scheme and the platform typeface.
        compose.onNodeWithTag("reading_theme_system").assertIsSelected()
        compose.onNodeWithTag("reading_typeface_system").assertIsSelected()

        compose.onNodeWithTag("reading_theme_night").performClick()
        compose.onNodeWithTag("reading_typeface_amiri").performClick()
        settle(4)

        compose.onNodeWithTag("reading_theme_night").assertIsSelected()
        compose.onNodeWithTag("reading_typeface_amiri").assertIsSelected()

        // The prefs are the exact store a cold start reads.
        val prefs = NourPreferences(context)
        assertEquals(QuranReadingTheme.NIGHT, prefs.quranReadingTheme)
        assertEquals(QuranTypeface.AMIRI, prefs.quranTypeface)

        // The scripture node is still there — selecting a typeface must never
        // hide or replace verse text.
        compose.onNodeWithTag("ayah_card_1").assertExists()

        // And the choice survives reopening the sheet.
        compose.onNodeWithContentDescription(strings.close).performClick()
        settle(8)
        compose.onNodeWithContentDescription(strings.readingSettings, substring = true).performClick()
        settle(12)
        compose.onNodeWithTag("reading_theme_night").assertIsSelected()
        compose.onNodeWithTag("reading_typeface_amiri").assertIsSelected()
    }

    @Test
    fun continuousModeDeepLinkLandsOnRequestedVerse() {
        NourPreferences(context).readingMode = "CONTINUOUS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppLanguage provides AppLanguage.ENGLISH,
                    LocalAppStrings provides strings
                ) {
                    NourContentFrame {
                        QuranScreen(
                            openSurahRequest = QuranRepository.getSurah(2),
                            openAyahRequest = 255,
                            onOpenSurahConsumed = {},
                            onOpenAyahConsumed = {}
                        )
                    }
                }
            }
        }
        settle(8)
        compose.onNodeWithTag("ayah_card_255").assertIsDisplayed()
        compose.onNodeWithTag("last_read_marker").assertIsDisplayed()
        assertEquals("CONTINUOUS", NourPreferences(context).readingMode)
    }

    @Test
    fun libraryHasThreeTabsAndNoMushafTab() {
        setContent()
        listOf(strings.tabSurah, strings.tabBookmark, strings.tabSearch).forEach { label ->
            compose.onNodeWithText(label, substring = true).assertIsDisplayed()
        }
        compose.onNodeWithText(strings.tabMushaf).assertDoesNotExist()
    }

    @Test
    fun searchTabShowsNewHint() {
        setContent()
        // switch to search tab (index 2)
        compose.onNodeWithText(strings.tabSearch).performClick()
        settle()
        compose.onNodeWithText(strings.searchVerseHint, substring = true).assertIsDisplayed()
    }

    // ---- C1: go to verse -------------------------------------------------

    private fun setContentWithDeepLink(surahNumber: Int, ayahNumber: Int) {
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppLanguage provides AppLanguage.ENGLISH,
                    LocalAppStrings provides strings
                ) {
                    NourContentFrame {
                        QuranScreen(
                            openSurahRequest = QuranRepository.getSurah(surahNumber),
                            openAyahRequest = ayahNumber,
                            onOpenSurahConsumed = {},
                            onOpenAyahConsumed = {}
                        )
                    }
                }
            }
        }
    }

    private fun typeVerse(surah: String, ayah: String) {
        compose.onNodeWithTag("go_to_verse_surah_input").performTextClearance()
        compose.onNodeWithTag("go_to_verse_surah_input").performTextInput(surah)
        compose.onNodeWithTag("go_to_verse_ayah_input").performTextClearance()
        compose.onNodeWithTag("go_to_verse_ayah_input").performTextInput(ayah)
        settle(2)
    }

    private fun openGoToVerseDialog() {
        compose.onNodeWithContentDescription(strings.goToVerseTitle).performClick()
        settle(8)
    }

    // Robolectric#8460: a Dialog with text fields loops above 320 dp width.
    @Test
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun goToVerseOpensRequestedSurahAndAyah() {
        setContent()
        openSurah(1)
        settle(4)
        openGoToVerseDialog()
        typeVerse(surah = "5", ayah = "1")
        compose.onNodeWithTag("go_to_verse_confirm").performClick()
        settle(12)

        val maidah = QuranRepository.getSurah(5)
        compose.onNodeWithText("5. ${maidah.nameEnglish} ▾").assertIsDisplayed()
        compose.onNodeWithTag("ayah_card_1").assertIsDisplayed()
        // C1 hands the reader an explicit position, so C2 acknowledges it.
        compose.onNodeWithTag("last_read_marker").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun goToVerseAcceptsArabicIndicDigits() {
        setContent()
        openSurah(1)
        settle(4)
        openGoToVerseDialog()
        typeVerse(surah = "٢", ayah = "٢٥٥")
        compose.onNodeWithTag("go_to_verse_confirm").performClick()
        settle(12)

        val baqarah = QuranRepository.getSurah(2)
        compose.onNodeWithText("2. ${baqarah.nameEnglish} ▾").assertIsDisplayed()
        compose.onNodeWithTag("ayah_card_255").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun goToVerseRejectsOutOfRangeAyah() {
        setContent()
        openSurah(1)
        settle(4)
        openGoToVerseDialog()
        typeVerse(surah = "5", ayah = "121")
        compose.onNodeWithTag("go_to_verse_confirm").performClick()
        settle(4)

        compose.onNodeWithText(strings.invalidVerse).assertIsDisplayed()
        // Still in Al-Fatihah: an invalid pair never navigates.
        val fatihah = QuranRepository.getSurah(1)
        compose.onNodeWithText("1. ${fatihah.nameEnglish} ▾").assertIsDisplayed()
    }

    // ---- C2: last-read marker -------------------------------------------

    @Test
    fun lastReadMarkerShowsAtRestoredVerseOnly() {
        NourPreferences(context).readingMode = "CARDS"
        setContentWithDeepLink(2, 255)
        settle(8)

        compose.onNodeWithTag("ayah_card_255").assertIsDisplayed()
        compose.onNodeWithTag("last_read_marker").assertIsDisplayed()
    }

    @Test
    fun lastReadMarkerClearsOnNextVerseTap() {
        NourPreferences(context).readingMode = "CARDS"
        setContentWithDeepLink(2, 255)
        settle(8)
        compose.onNodeWithTag("last_read_marker").assertIsDisplayed()
        val markedHeight = compose.onNodeWithTag("ayah_card_255").fetchSemanticsNode().size.height

        compose.onNodeWithTag("ayah_card_256").performClick()
        settle(2)
        compose.onNodeWithTag("last_read_marker").assertDoesNotExist()

        // The marker lives inside the existing reference pill: clearing it
        // must not resize the verse card (owner-visible regression).
        val clearedHeight = compose.onNodeWithTag("ayah_card_255").fetchSemanticsNode().size.height
        assertEquals("last-read marker must not change the verse card height", clearedHeight, markedHeight)
    }

    @Test
    fun freshSurahOpenHasNoLastReadMarker() {
        setContent()
        openSurah(2)
        compose.onNodeWithTag("last_read_marker").assertDoesNotExist()
    }

    // ---- horizontal surah navigation (former Mushaf gesture) ------------

    @Test
    fun swipeMovesBetweenSurahsAndPersistsTheSurah() {
        NourPreferences(context).readingMode = "CARDS"
        setContent()
        openSurah(1)

        swipeToNextSurah()
        awaitPage(2)
        val baqarah = QuranRepository.getSurah(2)
        compose.onNodeWithText("2. ${baqarah.nameEnglish} ▾").assertIsDisplayed()
        compose.onNodeWithTag("surah_page_2").assertIsDisplayed()
        compose.waitUntil(5_000) { NourPreferences(context).lastReadSurah == 2 }

        swipeToPreviousSurah()
        awaitPage(1)
        val fatihah = QuranRepository.getSurah(1)
        compose.onNodeWithText("1. ${fatihah.nameEnglish} ▾").assertIsDisplayed()
        compose.onNodeWithTag("surah_page_1").assertIsDisplayed()
        compose.waitUntil(5_000) { NourPreferences(context).lastReadSurah == 1 }
    }

    @Test
    fun swipeAtTheFirstSurahStaysOnSurahOne() {
        NourPreferences(context).readingMode = "CARDS"
        setContent()
        openSurah(1)

        swipeToPreviousSurah()

        val fatihah = QuranRepository.getSurah(1)
        compose.onNodeWithText("1. ${fatihah.nameEnglish} ▾").assertIsDisplayed()
        compose.onNodeWithTag("surah_page_1").assertIsDisplayed()
        assertEquals(1, NourPreferences(context).lastReadSurah)
    }

    @Test
    fun swipeRestoresEachSurahsScrollPosition() {
        NourPreferences(context).readingMode = "CARDS"
        setContent()
        openSurah(2)

        // Item 0 is the surah header, so item 40 is verse 40 at the top.
        compose.onNodeWithTag("surah_page_2").performScrollToIndex(40)
        settle(4)
        compose.waitUntil(5_000) { NourPreferences(context).lastReadAyah == 40 }

        swipeToNextSurah()
        awaitPage(3)
        swipeToPreviousSurah()
        awaitPage(2)

        // The saved per-surah list state must bring verse 40 back, not the
        // surah header at the top.
        compose.onNodeWithTag("ayah_card_40").assertIsDisplayed()
        compose.waitUntil(5_000) { NourPreferences(context).lastReadAyah == 40 }
        compose.waitUntil(5_000) { NourPreferences(context).lastReadSurah == 2 }
    }

    @Test
    fun swipeAwayConsumesADeepLinkSoReturningDoesNotReland() {
        NourPreferences(context).readingMode = "CARDS"
        setContentWithDeepLink(2, 255)
        settle(8)
        compose.onNodeWithTag("ayah_card_255").assertIsDisplayed()

        swipeToNextSurah()
        awaitPage(3)
        swipeToPreviousSurah()
        awaitPage(2)

        // The deep link marked only the first landing. Returning through the
        // pager restores the saved scroll position instead of re-applying it.
        compose.onNodeWithTag("last_read_marker").assertDoesNotExist()
        compose.onNodeWithTag("ayah_card_255").assertExists()
    }
}
