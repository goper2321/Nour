@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.features.quran.ui

import androidx.activity.compose.BackHandler
import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.SheetExpressiveShape
import com.example.ui.components.NourEmptyState
import com.example.ui.components.NourCircularIconButton
import com.example.ui.components.NourFloatingToolbar
import com.example.ui.components.NourSearchField
import com.example.ui.components.NourSegmentOption
import com.example.ui.components.NourSegmentedControl
import com.example.ui.components.nourCardSurfaceColor
import com.example.ui.components.nourSegmentedControlHeight
import com.example.ui.components.LocalFloatingBottomExtra
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.LocalUseEasternNumerals
import com.example.localization.Numerals
import com.example.localization.formatNumerals
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.BookmarkEntity
import com.example.database.NourDatabase
import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranRepository
import com.example.features.settings.NourPreferences
import com.example.features.settings.QuranReadingTheme
import com.example.features.settings.QuranTypeface
import com.example.features.quran.models.Ayah
import com.example.features.quran.models.RevelationType
import com.example.features.quran.models.Surah
import kotlinx.coroutines.launch

/**
 * Reading view modes for Surahs and Mushaf pages:
 * - CARDS: Each Ayat rendered in its own distinct Card with actions and translation.
 * - CONTINUOUS: The Surah/Page rendered as a whole continuous flowing text while respecting Ayats.
 */
enum class QuranReadingMode(val label: String) {
    CARDS("Verse Cards"),
    CONTINUOUS("Continuous Text")
}

/**
 * Scopes card colours to a [QuranReadingTheme] without painting the page.
 *
 * SYSTEM (the default) composes the app's own scheme and is pixel-identical to
 * the pre-C3 screen. PAPER/NIGHT swap only the card, container and action roles
 * for the reader subtree; the host grouped background still shows through the
 * gaps between cards, and app chrome, dialogs and the settings sheet stay
 * outside this scope and keep the app scheme. No background is painted here on
 * purpose: tinting the whole reader is exactly the regression this scope
 * exists to prevent.
 */
@Composable
private fun QuranReadingThemeScope(
    theme: QuranReadingTheme,
    content: @Composable () -> Unit
) {
    val scheme = theme.applyTo(MaterialTheme.colorScheme)
    MaterialTheme(colorScheme = scheme) {
        content()
    }
}

/**
 * Floating segmented tab bar for the Quran library. Solid track, opaque thumb,
 * no glass and no oversized pills. Its height can grow when a label wraps to
 * two lines at large font scales; the in-flow spacers reserve the same
 * font-aware height, so rows never slide underneath it.
 */
@Composable
private fun QuranTabBar(
    selectedTab: Int,
    bookmarkCount: Int,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val options = listOf(
        NourSegmentOption(value = 0, label = strings.tabSurah),
        NourSegmentOption(
            value = 1,
            label = if (bookmarkCount > 0) {
                formatNumerals("${strings.tabBookmark} ($bookmarkCount)")
            } else {
                strings.tabBookmark
            }
        ),
        NourSegmentOption(value = 2, label = strings.tabSearch)
    )
    NourSegmentedControl(
        options = options,
        selected = selectedTab,
        onSelected = onTabSelect,
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = NourSpace.lg, vertical = NourSpace.sm)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    modifier: Modifier = Modifier,
    openSurahRequest: Surah? = null,
    onOpenSurahConsumed: () -> Unit = {},
    openAyahRequest: Int? = null,
    onOpenAyahConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }
    val prefs = remember { NourPreferences(context) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Surah, 1 = Bookmark, 2 = Search
    var selectedSurahForReading by remember { mutableStateOf<Surah?>(null) }
    var deepLinkAyah by remember { mutableStateOf<Int?>(null) }
    // The surah a deep link belongs to. It scopes the ayah to that reader page
    // so a later swipe never re-applies the old landing to another surah.
    var deepLinkSurahNumber by remember { mutableIntStateOf(0) }
    var readingMode by remember {
        mutableStateOf(
            prefs.readingMode?.let { stored ->
                QuranReadingMode.values().firstOrNull { it.name == stored }
            } ?: QuranReadingMode.CONTINUOUS
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var selectedEditionId by remember { mutableStateOf("sahih_en") }
    var arabicFontSize by remember { mutableFloatStateOf(prefs.arabicFontSize) }
    var quranReadingTheme by remember { mutableStateOf(prefs.quranReadingTheme) }
    var quranTypeface by remember { mutableStateOf(prefs.quranTypeface) }
    var lastReadSurahNumber by remember { mutableIntStateOf(prefs.lastReadSurah) }
    var lastReadAyahNumber by remember { mutableIntStateOf(prefs.lastReadAyah) }

    // Persist reading preferences/continuity whenever they change
    LaunchedEffect(readingMode) { prefs.readingMode = readingMode.name }
    LaunchedEffect(arabicFontSize) { prefs.arabicFontSize = arabicFontSize }
    LaunchedEffect(quranReadingTheme) { prefs.quranReadingTheme = quranReadingTheme }
    LaunchedEffect(quranTypeface) { prefs.quranTypeface = quranTypeface }
    LaunchedEffect(lastReadSurahNumber) { prefs.lastReadSurah = lastReadSurahNumber }
    LaunchedEffect(lastReadAyahNumber) { prefs.lastReadAyah = lastReadAyahNumber }

    // Reading Settings Bottom Sheet state
    var showReadingSettingsSheet by remember { mutableStateOf(false) }

    // Go-to-verse dialog. The regular reader is the only target now; the
    // dialog prefills from the surah currently on screen.
    var showGoToVerseDialog by remember { mutableStateOf(false) }

    val strings = LocalAppStrings.current
    val bookmarks by db.bookmarkDao().getAllBookmarks().collectAsState(initial = emptyList())
    // Fail gracefully (not crash) if the bundled Quran text is corrupt or
    // missing: integrity is verified on first access, and the lazy corpus
    // caches that result, so checking once here guards every read below.
    val corpusFailure = remember { runCatching { QuranCorpus.ayahs }.exceptionOrNull() }
    if (corpusFailure != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("quran_corpus_error"),
            contentAlignment = Alignment.Center
        ) {
            NourEmptyState(title = strings.corpusErrorTitle, message = strings.corpusErrorMessage)
        }
        return
    }
    LaunchedEffect(openSurahRequest, openAyahRequest) {
        if (openSurahRequest != null) {
            selectedSurahForReading = openSurahRequest
            lastReadSurahNumber = openSurahRequest.number
            if (openAyahRequest != null) {
                deepLinkAyah = openAyahRequest
                deepLinkSurahNumber = openSurahRequest.number
                lastReadAyahNumber = openAyahRequest
            } else {
                deepLinkAyah = null
                deepLinkSurahNumber = 0
                lastReadAyahNumber = 1
            }
            onOpenSurahConsumed()
            if (openAyahRequest != null) onOpenAyahConsumed()
        } else if (openAyahRequest != null) {
            deepLinkAyah = openAyahRequest
            deepLinkSurahNumber = selectedSurahForReading?.number ?: lastReadSurahNumber
            onOpenAyahConsumed()
        }
    }

    // Predictive back: while the surah reader is open, system back returns to
    // the library instead of leaving the app. This is composed after the host's
    // BackHandler, so it takes precedence only while the reader is showing.
    BackHandler(enabled = selectedSurahForReading != null) {
        deepLinkAyah = null
        deepLinkSurahNumber = 0
        selectedSurahForReading = null
    }

    // No topbar: library content runs edge-to-edge beneath the status bar,
    // with the pills floating over the scrolling content.
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("quran_screen")
    ) {
        if (selectedSurahForReading != null) {
            val readingSurah = selectedSurahForReading!!
            // A deep link is scoped to the surah it was requested for. Once
            // the reader swipes to another surah, the landing ayah no longer
            // applies and the next page follows the persisted reading mode.
            val scopedDeepLinkAyah = deepLinkAyah
                ?.takeIf { deepLinkSurahNumber == readingSurah.number }
            // C3: Paper/Night recolour reader cards only; the gaps between
            // them keep the host's grouped background.
            QuranReadingThemeScope(theme = quranReadingTheme) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    SurahReaderView(
                        surah = readingSurah,
                        readingMode = readingMode,
                        onModeChange = {
                            deepLinkAyah = null
                            deepLinkSurahNumber = 0
                            readingMode = it
                        },
                        forceCards = scopedDeepLinkAyah != null,
                        editionId = selectedEditionId,
                        arabicFontSize = arabicFontSize,
                        typeface = quranTypeface,
                        bookmarks = bookmarks,
                        onBack = {
                            deepLinkAyah = null
                            deepLinkSurahNumber = 0
                            selectedSurahForReading = null
                        },
                        onSelectSurah = { newSurah ->
                            deepLinkAyah = null
                            deepLinkSurahNumber = 0
                            selectedSurahForReading = newSurah
                            lastReadSurahNumber = newSurah.number
                            lastReadAyahNumber = 1
                        },
                        onSurahSettled = { settledSurah ->
                            // Swipe navigation moves the surah pointer. A new
                            // surah starts honestly at verse 1 until the
                            // settled page reports its real first visible
                            // verse; the surah already open keeps its saved
                            // verse (the opening restore depends on it).
                            if (settledSurah.number != lastReadSurahNumber) {
                                lastReadSurahNumber = settledSurah.number
                                lastReadAyahNumber = 1
                            }
                            // A swipe away from a deep-linked surah consumes
                            // the link: returning later restores the saved
                            // position instead of re-landing on that verse.
                            if (deepLinkSurahNumber != 0 &&
                                settledSurah.number != deepLinkSurahNumber
                            ) {
                                deepLinkAyah = null
                                deepLinkSurahNumber = 0
                            }
                            selectedSurahForReading = settledSurah
                        },
                        onVisibleVerseChanged = { ayahNumber ->
                            lastReadAyahNumber = ayahNumber
                        },
                        initialAyah = scopedDeepLinkAyah
                            ?: if (readingSurah.number == lastReadSurahNumber) lastReadAyahNumber else 1,
                        deepLinkAyah = scopedDeepLinkAyah,
                        onGoToVerse = { showGoToVerseDialog = true },
                        onOpenSettings = { showReadingSettingsSheet = true },
                        onToggleBookmark = { ayah ->
                            coroutineScope.launch {
                                val surah = QuranRepository.getSurah(ayah.surahNumber)
                                val existing = db.bookmarkDao().getBookmark(ayah.surahNumber, ayah.ayahNumber)
                                if (existing != null) {
                                    db.bookmarkDao().deleteBookmark(ayah.surahNumber, ayah.ayahNumber)
                                    Toast.makeText(context, strings.bookmarkRemoved, Toast.LENGTH_SHORT).show()
                                } else {
                                    db.bookmarkDao().insertBookmark(
                                        BookmarkEntity(
                                            surahNumber = ayah.surahNumber,
                                            ayahNumber = ayah.ayahNumber,
                                            surahName = surah.nameEnglish,
                                            ayahText = ayah.arabicText,
                                            translationText = ayah.getTranslation(selectedEditionId, strings.translationNotAvailable)
                                        )
                                    )
                                    Toast.makeText(context, strings.ayahBookmarked, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        } else {
            // Full-bleed lists scroll under the status bar and the floating pills.
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Surahs list with "Continue Reading" card under [(Surah) (Bookmark) (Search)]
                    SurahListView(
                        lastReadSurah = QuranRepository.getSurah(lastReadSurahNumber),
                        lastReadAyah = lastReadAyahNumber,
                        onContinueReading = {
                            selectedSurahForReading = QuranRepository.getSurah(lastReadSurahNumber)
                        },
                        onSurahClick = { surah ->
                            deepLinkAyah = null
                            deepLinkSurahNumber = 0
                            selectedSurahForReading = surah
                            lastReadSurahNumber = surah.number
                            lastReadAyahNumber = 1
                        },
                        )
                }
                1 -> {
                    // Bookmarks View
                    BookmarksView(
                        bookmarks = bookmarks,
                        onGoToAyah = { bm ->
                            deepLinkAyah = bm.ayahNumber
                            deepLinkSurahNumber = bm.surahNumber
                            selectedSurahForReading = QuranRepository.getSurah(bm.surahNumber)
                            lastReadSurahNumber = bm.surahNumber
                            lastReadAyahNumber = bm.ayahNumber
                        },
                        onDeleteBookmark = { id ->
                            coroutineScope.launch { db.bookmarkDao().deleteById(id) }
                        },
                        )
                }
                2 -> {
                    // Search View
                    QuranSearchView(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        editionId = selectedEditionId,
                        onSelectSurah = { surah ->
                            deepLinkAyah = null
                            deepLinkSurahNumber = 0
                            selectedSurahForReading = surah
                            lastReadSurahNumber = surah.number
                            lastReadAyahNumber = 1
                        },
                        onSelectAyah = { ayah ->
                            deepLinkAyah = ayah.ayahNumber
                            deepLinkSurahNumber = ayah.surahNumber
                            selectedSurahForReading = QuranRepository.getSurah(ayah.surahNumber)
                            lastReadSurahNumber = ayah.surahNumber
                            lastReadAyahNumber = ayah.ayahNumber
                        },
                        )
                }
            }
            }
            QuranTabBar(
                selectedTab = selectedTab,
                bookmarkCount = bookmarks.size,
                onTabSelect = { selectedTab = it },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Go to Verse dialog (regular reader toolbar).
    if (showGoToVerseDialog) {
        val openSurah = selectedSurahForReading
        val surahNumber = openSurah?.number ?: lastReadSurahNumber
        val ayahNumber = if (openSurah != null && openSurah.number == lastReadSurahNumber) {
            lastReadAyahNumber.coerceIn(1, openSurah.ayahCount)
        } else {
            1
        }
        QuranGoToVerseDialog(
            initialSurah = surahNumber,
            initialAyah = ayahNumber,
            onDismiss = { showGoToVerseDialog = false },
            onConfirm = { targetSurah, targetAyah ->
                deepLinkAyah = targetAyah
                deepLinkSurahNumber = targetSurah
                selectedSurahForReading = QuranRepository.getSurah(targetSurah)
                lastReadSurahNumber = targetSurah
                lastReadAyahNumber = targetAyah
                showGoToVerseDialog = false
            }
        )
    }

    // Reading Settings Bottom Sheet
    if (showReadingSettingsSheet) {
        QuranSettingsBottomSheet(
            readingMode = readingMode,
            onModeChange = { readingMode = it },
            arabicFontSize = arabicFontSize,
            onFontSizeChange = { arabicFontSize = it },
            readingTheme = quranReadingTheme,
            onReadingThemeChange = { quranReadingTheme = it },
            typeface = quranTypeface,
            onTypefaceChange = { quranTypeface = it },
            selectedEditionId = selectedEditionId,
            onSelectEdition = { selectedEditionId = it },
            onDismiss = { showReadingSettingsSheet = false }
        )
    }
}

/**
 * Go-to-verse dialog: the only deliberate "open 5:255" path in the reader and
 * the page reader. Accepts Western, Arabic-Indic and Extended Arabic-Indic
 * digits through [Numerals.parseLocalizedNumber]; an out-of-range or
 * non-numeric pair never navigates and shows [AppStrings.invalidVerse].
 */
@Composable
fun QuranGoToVerseDialog(
    initialSurah: Int,
    initialAyah: Int,
    onDismiss: () -> Unit,
    onConfirm: (surahNumber: Int, ayahNumber: Int) -> Unit
) {
    val strings = LocalAppStrings.current
    var surahInput by remember { mutableStateOf(initialSurah.toString()) }
    var ayahInput by remember { mutableStateOf(initialAyah.toString()) }
    var invalid by remember { mutableStateOf(false) }

    val confirm = {
        val surahNumber = Numerals.parseLocalizedNumber(surahInput)
        val ayahNumber = Numerals.parseLocalizedNumber(ayahInput)
        val ayahCount = if (surahNumber != null && surahNumber in 1..114) {
            QuranRepository.getSurah(surahNumber).ayahCount
        } else {
            0
        }
        if (surahNumber == null || ayahNumber == null || surahNumber !in 1..114 || ayahNumber !in 1..ayahCount) {
            invalid = true
        } else {
            onConfirm(surahNumber, ayahNumber)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogExpressiveShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = strings.goToVerseTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(NourSpace.md)) {
                    OutlinedTextField(
                        value = surahInput,
                        onValueChange = { surahInput = it; invalid = false },
                        label = { Text(strings.tabSurah) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = CardSubtleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("go_to_verse_surah_input")
                    )
                    OutlinedTextField(
                        value = ayahInput,
                        onValueChange = { ayahInput = it; invalid = false },
                        label = { Text(strings.ayahLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        keyboardActions = KeyboardActions(onDone = { confirm() }),
                        shape = CardSubtleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("go_to_verse_ayah_input")
                    )
                }
                if (invalid) {
                    Text(
                        text = strings.invalidVerse,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = NourSpace.xs)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = confirm,
                shape = PillShape,
                modifier = Modifier.testTag("go_to_verse_confirm")
            ) {
                Text(strings.goToVerseTitle)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = PillShape,
                modifier = Modifier.testTag("go_to_verse_cancel")
            ) {
                Text(strings.cancel)
            }
        }
    )
}

/**
 * Continue Reading Card:
 * Elegantly styled similar to the Surah card with matching geometry, typography, and badge.
 */
@Composable
fun ContinueReadingSurahCard(
    lastReadSurah: Surah,
    onContinueReading: () -> Unit,
    modifier: Modifier = Modifier,
    lastReadAyah: Int = 1
) {
    val strings = LocalAppStrings.current
    val arabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    val totalVerses = lastReadSurah.ayahCount
    val lastAyah = lastReadAyah.coerceIn(1, totalVerses)
    val fraction = lastAyah.toFloat() / totalVerses
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardExpressiveShape)
            .clickable { onContinueReading() }
            .testTag("continue_reading_card"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
    ) {
        Column(Modifier.padding(NourSpace.lg)) {
        Text(strings.continueReadingBtn, style = MaterialTheme.typography.labelMedium)
        Text(
            if (arabicUi) lastReadSurah.nameArabic else lastReadSurah.nameEnglish,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (arabicUi) {
            val revelationAr = if (lastReadSurah.revelationType == RevelationType.MECCAN) strings.meccan else strings.medinan
            Text(
                formatNumerals("${lastReadSurah.number} · $revelationAr • ${lastReadSurah.ayahCount} ${strings.versesWord}"),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            // Fully localized line. `englishMeaning` is always English (Tanzil
            // metadata has no per-locale surah meanings bundled), so showing it
            // in e.g. a French or Indonesian UI leaked English. Mirror the
            // Arabic branch instead: number · revelation • verse count.
            val revelation = if (lastReadSurah.revelationType == RevelationType.MECCAN) strings.meccan else strings.medinan
            Text(
                formatNumerals("${lastReadSurah.number} · $revelation • ${lastReadSurah.ayahCount} ${strings.versesWord}"),
                style = MaterialTheme.typography.bodySmall
            )
        }
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().padding(top = NourSpace.md),
            color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        Text(formatNumerals(String.format(strings.verseOfPattern, lastAyah, totalVerses, (fraction * 100).toInt())),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = NourSpace.xs))
        }
    }
}

/**
 * Clean Surah List with Continue Reading card under [(Surah) (Bookmark) (Search)].
 */
@Composable
fun SurahListView(
    lastReadSurah: Surah,
    lastReadAyah: Int,
    onContinueReading: () -> Unit,
    onSurahClick: (Surah) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = NourSpace.xs, bottom = LocalFloatingBottomExtra.current),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        // Spacer reserves the overlay footprint so cards never slide under the pills.
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(nourSegmentedControlHeight() + NourSpace.sm * 2)
            )
        }
        // "Continue Reading" card under the pills
        item {
            ContinueReadingSurahCard(
                lastReadSurah = lastReadSurah,
                onContinueReading = onContinueReading,
                lastReadAyah = lastReadAyah
            )
        }

        items(QuranRepository.ALL_SURAHS, key = { it.number }) { surah ->
            SurahListItem(surah, onClick = { onSurahClick(surah) },
                modifier = Modifier.testTag("surah_item_${surah.number}"))
        }
    }
}

/**
 * Dedicated Search View for the "Search" tab.
 */
@Composable
fun QuranSearchView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    editionId: String,
    onSelectSurah: (Surah) -> Unit,
    onSelectAyah: (Ayah) -> Unit
) {
    val strings = LocalAppStrings.current
    val matchingSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else QuranRepository.ALL_SURAHS.filter {
            it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
            it.englishMeaning.contains(searchQuery, ignoreCase = true) ||
            it.nameArabic.contains(searchQuery) ||
            it.number.toString() == searchQuery.trim()
        }
    }

    val arabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    var matchingAyahs by remember { mutableStateOf(emptyList<Ayah>()) }
    var isSearching by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            matchingAyahs = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        // Search can scan the complete corpus. Keep even the uncached first scan
        // off the main thread so composition never waits for the index.
        matchingAyahs = withContext(Dispatchers.Default) {
            QuranRepository.search(searchQuery)
        }
        isSearching = false
    }

    Column(
        // Horizontal margin comes from the global NourContentFrame gutter.
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(nourSegmentedControlHeight() + NourSpace.sm * 2)
        )
        // Minimalist search field: slim pill, autofocus on entering the tab,
        // keyboard Search action dismisses the keyboard.
        val focusManager = LocalFocusManager.current
        val searchFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) { searchFocus.requestFocus() }
        NourSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = strings.searchVerseHint,
            clearContentDescription = strings.clearSearch,
            focusRequester = searchFocus,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .padding(top = NourSpace.sm, bottom = NourSpace.xs),
            fieldModifier = Modifier.testTag("quran_search_field")
        )

        if (searchQuery.isBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md),
                contentPadding = PaddingValues(bottom = LocalFloatingBottomExtra.current)
            ) {
                item {
                    Text(
                        text = strings.popularSurahs,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = NourSpace.sm, bottom = NourSpace.xs)
                    )
                }
                val popularSurahs = listOf(1, 36, 18, 67, 55, 56).map { QuranRepository.getSurah(it) }
                items(popularSurahs, key = { it.number }) { surah ->
                    SurahListItem(surah, onClick = { onSelectSurah(surah) })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md),
                contentPadding = PaddingValues(bottom = LocalFloatingBottomExtra.current)
            ) {
                if (matchingSurahs.isNotEmpty()) {
                    item {
                        Text(
                            text = formatNumerals(String.format(strings.surahsCountPattern, matchingSurahs.size)),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = NourSpace.xs, bottom = 2.dp)
                        )
                    }
                    items(matchingSurahs, key = { it.number }) { surah ->
                        SurahListItem(surah, onClick = { onSelectSurah(surah) })
                    }
                }

                if (matchingAyahs.isNotEmpty()) {
                    item {
                        Text(
                            text = formatNumerals(String.format(strings.versesCountPattern, matchingAyahs.size)),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = NourSpace.sm, bottom = 2.dp)
                        )
                    }
                    items(matchingAyahs, key = { it.surahNumber * 1000 + it.ayahNumber }) { ayah ->
                        val surah = QuranRepository.getSurah(ayah.surahNumber)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardExpressiveShape)
                                .clickable { onSelectAyah(ayah) },
                            shape = CardExpressiveShape,
                            colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
                        ) {
                            Column(modifier = Modifier.padding(NourSpace.lg)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatNumerals(String.format(strings.surahVerseLabelPattern, if (LocalAppLanguage.current == AppLanguage.ARABIC) surah.nameArabic else surah.nameEnglish, ayah.ayahNumber)),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = surah.nameArabic,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // NUR-016: Arabic scripture block scoped RTL;
                                // the translation Text below is untouched and
                                // keeps the interface direction (LTR reading).
                                ScriptureBox {
                                    Text(
                                        text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(modifier = Modifier.height(NourSpace.xs))
                                Text(
                                    text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (!isSearching && matchingSurahs.isEmpty() && matchingAyahs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatNumerals(String.format(strings.noResultsForPattern, searchQuery)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated, uncrowded Surah Reader View:
 * Features a minimalist top bar, bottom sheet preferences,
 * delicate Bismillah ornament, and seamless switching between Flow and Cards.
 *
 * The 114 surahs sit in a [HorizontalPager]: swiping left/right moves
 * surah-to-surah like the removed Mushaf pager, while the toolbar stays
 * fixed. Each page keeps its own [androidx.compose.foundation.lazy.LazyListState],
 * so swiping away and back restores the same verse position.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderView(
    surah: Surah,
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    forceCards: Boolean = false,
    editionId: String,
    arabicFontSize: Float,
    typeface: QuranTypeface = QuranTypeface.SYSTEM,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit,
    onSelectSurah: (Surah) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    onGoToVerse: () -> Unit = {},
    onSurahSettled: (Surah) -> Unit = onSelectSurah,
    onVisibleVerseChanged: ((Int) -> Unit)? = null,
    initialAyah: Int = 1,
    deepLinkAyah: Int? = null
) {
    val strings = LocalAppStrings.current
    val arabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    val effectiveReadingMode = if (forceCards) QuranReadingMode.CARDS else readingMode

    // C4: null for SYSTEM, so the default text style is untouched.
    val scriptureTypeface = scriptureFontFamily(typeface)

    // The Mushaf pager's horizontal gesture, now applied to the regular
    // reader: pages are surahs (1..114), not Tanzil pages. The toolbar stays
    // fixed while the page content follows the finger.
    val pagerState = rememberPagerState(
        initialPage = surah.number - 1,
        pageCount = { QuranRepository.ALL_SURAHS.size }
    )
    // One saved position per surah. Without this, swiping away and back would
    // silently reset a long surah to its header (the old single-list reader
    // also never keyed its state by surah).
    val pageStates = remember {
        mutableMapOf<Int, androidx.compose.foundation.lazy.LazyListState>()
    }

    // Frozen restore/marker target: set once for the opening position and
    // replaced only by an explicit deep link. Swiping must never re-apply the
    // previous surah's ayah to a different page.
    var restoreTarget by remember {
        mutableStateOf(initialAyah.takeIf { it > 1 }?.let { surah.number to it })
    }
    var markerTarget by remember { mutableStateOf(restoreTarget) }

    // Follow explicit navigation (library deep link, go-to-verse, dropdown,
    // end-of-surah buttons). A swipe settle updates `surah` to the page that
    // is already on screen, so this is a no-op then.
    LaunchedEffect(surah.number, deepLinkAyah) {
        val targetPage = surah.number - 1
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
        if (deepLinkAyah != null) {
            val target = surah.number to deepLinkAyah
            restoreTarget = target
            markerTarget = target
        }
    }

    // The marker acknowledges one restored/deep-linked landing. Leaving its
    // surah clears it; swiping back does not resurrect it.
    LaunchedEffect(pagerState.settledPage) {
        val marked = markerTarget ?: return@LaunchedEffect
        if (pagerState.settledPage + 1 != marked.first) markerTarget = null
    }
    // The marker lives on a verse card, so leaving cards mode discards it.
    LaunchedEffect(effectiveReadingMode) {
        if (effectiveReadingMode != QuranReadingMode.CARDS) markerTarget = null
    }

    // Persist continuity on settle. The page content reports the exact first
    // visible verse; this effect only moves the surah pointer.
    LaunchedEffect(pagerState.settledPage) {
        onSurahSettled(QuranRepository.getSurah(pagerState.settledPage + 1))
    }

    var showSurahDropdown by remember { mutableStateOf(false) }
    val currentSurah = QuranRepository.getSurah(pagerState.currentPage + 1)

    // Band-plus-breathing reserve ([onGloballyPositioned]): the content list
    // reserves the toolbar's real measured band plus an 8 dp breath, so the
    // first resting card stays readable, never slides under the pill, and
    // doesn't kiss its edge. Initialized from the height token; the first
    // layout pass corrects it (the pill grows at larger font scales).
    val toolbarDensity = LocalDensity.current
    var toolbarReserve by remember {
        mutableStateOf(NourSpace.lg + NourSize.toolbarHeight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_reader_view")
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Floating, opaque reader toolbar: contextual actions stay put but
            // never depend on the page behind them for contrast. It is overlaid
            // so the reading content can peek through its side/bottom gutters.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .padding(horizontal = NourSpace.md, vertical = NourSpace.sm)
            ) {
                NourFloatingToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            // Reserve = the pill's real measured band plus an
                            // 8 dp breath: the first resting card sits one
                            // small breath below the band's edge.
                            toolbarReserve =
                                NourSpace.lg + toolbarDensity.run { coords.size.height.toDp() }
                        }
                        .testTag("surah_reader_toolbar")
                ) {
                    NourCircularIconButton(
                        onClick = onBack,
                        contentDescription = strings.backToSurahs,
                        modifier = Modifier.testTag("surah_reader_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(NourSize.smallIcon)
                        )
                    }

                    Box(Modifier.weight(1f)) {
                        TextButton(
                            onClick = { showSurahDropdown = true },
                            shape = PillShape,
                            contentPadding = PaddingValues(horizontal = NourSpace.sm, vertical = NourSpace.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatNumerals("${currentSurah.number}. ${if (arabicUi) currentSurah.nameArabic else currentSurah.nameEnglish} ▾"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    // The Arabic name is isolated so the Latin
                                    // words stay whole instead of being woven
                                    // into the Arabic run ("7 • الفاتحة Verses").
                                    text = formatNumerals(
                                        "${currentSurah.nameArabic.isolateRtlDirection()} • ${currentSurah.ayahCount} ${strings.versesWord}"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSurahDropdown,
                            onDismissRequest = { showSurahDropdown = false },
                            shape = CardSubtleShape
                        ) {
                            QuranRepository.ALL_SURAHS.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(if (arabicUi) formatNumerals("${s.number}. ${s.nameArabic}") else "${s.number}. ${s.nameEnglish} (${s.nameArabic})", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        onSelectSurah(s)
                                        showSurahDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Quick Flow vs Cards Switcher
                    NourCircularIconButton(
                        onClick = {
                            val nextMode = if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS
                            onModeChange(nextMode)
                        },
                        contentDescription = strings.toggleMode
                    ) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Rounded.ViewAgenda else Icons.Rounded.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(NourSize.smallIcon)
                        )
                    }

                    // Deliberate verse navigation (C1)
                    NourCircularIconButton(
                        onClick = onGoToVerse,
                        contentDescription = strings.goToVerseTitle,
                        modifier = Modifier.testTag("surah_reader_go_to_verse")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Numbers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(NourSize.smallIcon)
                        )
                    }

                    // Reading Settings Modal Trigger
                    NourCircularIconButton(
                        onClick = onOpenSettings,
                        contentDescription = strings.readingSettings
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(NourSize.smallIcon)
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                key = { it },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("surah_pager")
            ) { pageIndex ->
                val pageSurah = QuranRepository.getSurah(pageIndex + 1)
                val pageAyahs = remember(pageIndex) {
                    QuranRepository.getAyahsForSurah(pageIndex + 1)
                }
                val pageListState = remember(pageIndex) {
                    pageStates.getOrPut(pageIndex) {
                        androidx.compose.foundation.lazy.LazyListState()
                    }
                }
                val pageRestoreAyah = restoreTarget
                    ?.takeIf { it.first == pageIndex + 1 }
                    ?.second
                val isSettled = pagerState.settledPage == pageIndex
                SurahReaderPage(
                    surah = pageSurah,
                    ayahs = pageAyahs,
                    listState = pageListState,
                    readingMode = effectiveReadingMode,
                    editionId = editionId,
                    arabicFontSize = arabicFontSize,
                    scriptureTypeface = scriptureTypeface,
                    bookmarks = bookmarks,
                    onToggleBookmark = onToggleBookmark,
                    onSelectSurah = onSelectSurah,
                    toolbarReserve = toolbarReserve,
                    markerAyah = markerTarget
                        ?.takeIf { it.first == pageIndex + 1 }
                        ?.second,
                    onClearMarker = { markerTarget = null },
                    restoreAyah = pageRestoreAyah,
                    onRestoreConsumed = { restoreTarget = null },
                    isSettled = isSettled,
                    onVisibleVerseChanged = onVisibleVerseChanged
                )
            }
        }
    }
}

/**
 * One surah page inside the horizontal pager: header card, verse cards or
 * continuous text, and the end-of-surah navigation card. Its list state is
 * owned by the reader and saved per surah, so swiping away and back restores
 * the same position.
 */
@Composable
private fun SurahReaderPage(
    surah: Surah,
    ayahs: List<Ayah>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    readingMode: QuranReadingMode,
    editionId: String,
    arabicFontSize: Float,
    scriptureTypeface: FontFamily?,
    bookmarks: List<BookmarkEntity>,
    onToggleBookmark: (Ayah) -> Unit,
    onSelectSurah: (Surah) -> Unit,
    toolbarReserve: androidx.compose.ui.unit.Dp,
    markerAyah: Int?,
    onClearMarker: () -> Unit,
    restoreAyah: Int?,
    onRestoreConsumed: () -> Unit,
    isSettled: Boolean,
    onVisibleVerseChanged: ((Int) -> Unit)?
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val arabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    // Restore an explicit landing (cold start at the persisted verse, a deep
    // link or go-to-verse) once per request. Only CARDS mode has a
    // verse-per-item list; continuous mode renders the whole surah as one
    // card, so a verse index would be fabricated there. Ordinary scrolling
    // updates the persisted position but never re-triggers this effect.
    LaunchedEffect(surah.number, restoreAyah, readingMode) {
        val target = restoreAyah ?: return@LaunchedEffect
        if (readingMode == QuranReadingMode.CARDS && target > 1) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .firstOrNull { it > 1 }
                ?.let { count ->
                    listState.scrollToItem(target.coerceIn(1, count - 1))
                }
        }
        onRestoreConsumed()
    }

    // Per-verse continuity only in CARDS mode, where each verse is its own list
    // item (item 0 is the header row). Continuous mode renders the Surah as one
    // item, so index-based verse mapping would be fabricated — resume opens the
    // Surah there instead. Only the settled page reports, so the pre-composed
    // neighbour can never overwrite the visible surah's position.
    LaunchedEffect(surah.number, readingMode, isSettled) {
        if (readingMode != QuranReadingMode.CARDS || !isSettled) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val verse = when {
                    index <= 0 -> 1
                    index <= ayahs.size -> ayahs[index - 1].ayahNumber
                    else -> ayahs.lastOrNull()?.ayahNumber ?: 1
                }
                onVisibleVerseChanged?.invoke(verse)
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_page_${surah.number}"),
        // Horizontal margin comes from the global NourContentFrame gutter.
        // Vertical: the toolbar's band plus an 8 dp breath above, and the
        // bar's band plus a breath below — resting content sits one small
        // breath clear of each band's edge (readable, never behind a pill).
        contentPadding = PaddingValues(top = toolbarReserve, bottom = LocalFloatingBottomExtra.current),
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
        // Delicate, Spiritual Surah Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_header_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NourSpace.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "سُورَةُ ${surah.nameArabic}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(NourSpace.xs))
                    if (!arabicUi) {
                        Text(
                            text = "${strings.tabSurah} ${surah.nameEnglish} — ${surah.englishMeaning}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${if (surah.revelationType == com.example.features.quran.models.RevelationType.MECCAN) strings.meccan else strings.medinan} • ${surah.ayahCount} ${strings.versesWord} • ${strings.juzLabel} ${surah.juzNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (readingMode == QuranReadingMode.CARDS) {
            // Verse Cards Mode
            items(ayahs, key = { it.surahNumber * 1000 + it.ayahNumber }) { ayah ->
                val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }
                val showsLastReadMarker = markerAyah == ayah.ayahNumber

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            // While "Last read" is visible, a tap on any verse
                            // acknowledges it and clears the marker. A raw
                            // pointer tap (not clickable) keeps the card out
                            // of the semantics merge tree, so the marker's
                            // own node stays readable to tests/TalkBack.
                            if (markerAyah != null) {
                                Modifier.pointerInput(markerAyah) {
                                    detectTapGestures { onClearMarker() }
                                }
                            } else {
                                Modifier
                            }
                        )
                        .testTag("ayah_card_${ayah.ayahNumber}"),
                    shape = CardExpressiveShape,
                    colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                    border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
                    elevation = CardDefaults.cardElevation(defaultElevation = NourSize.flat)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NourSpace.lg)
                    ) {
                        // C2: static acknowledgement of a restored or
                        // deep-linked position. It lives inside the verse
                        // reference pill so it costs no extra row height and
                        // leaves no hairline gaps at the card edges; the
                        // next verse tap clears it. No animation.
                        //
                        // Wraps below the reference pill when a large font
                        // scale leaves no room for both on one line.
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(NourSpace.xs),
                            itemVerticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = PillShape,
                                modifier = if (showsLastReadMarker) {
                                    Modifier.testTag("last_read_marker")
                                } else {
                                    Modifier
                                }
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("${ayah.surahNumber}:${ayah.ayahNumber}")
                                        if (showsLastReadMarker) {
                                            append(" · ")
                                            append(strings.lastReadMarker)
                                        }
                                    },
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = NourSpace.md, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onToggleBookmark(ayah) },
                                    modifier = Modifier.size(NourSize.touchTarget)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = strings.bookmarkLabel,
                                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val copyText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}]"
                                        clipboardManager.setText(AnnotatedString(copyText))
                                        Toast.makeText(context, strings.verseCopied, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(NourSize.touchTarget)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = strings.copyAction,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n\n" + String.format(strings.quranShareAttribution, surah.nameEnglish, ayah.surahNumber, ayah.ayahNumber)
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, strings.shareChooser))
                                    },
                                    modifier = Modifier.size(NourSize.touchTarget)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = strings.shareAction,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(NourSpace.md))

                        // Arabic text with end-of-Ayah marker
                        // NUR-016: scoped RTL, interface-independent.
                        ScriptureBox {
                            Text(
                                text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                                fontSize = arabicFontSize.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = scriptureTypeface,
                                // Amiri Quran carries taller marks: give it the
                                // 2.1 line box the continuous/page reader uses.
                                lineHeight = (arabicFontSize * if (scriptureTypeface == null) 1.8f else 2.1f).sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Translation
                        Text(
                            text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            // Continuous Flow Page Mode
            item {
                ContinuousQuranTextCard(
                    ayahs = ayahs,
                    arabicFontSize = arabicFontSize,
                    fontFamily = scriptureTypeface,
                    selectedAyahNumber = selectedAyahForInspector?.ayahNumber,
                    onSelectAyahNumber = { tappedNumber ->
                        selectedAyahForInspector = if (selectedAyahForInspector?.ayahNumber == tappedNumber) null else ayahs.firstOrNull { it.ayahNumber == tappedNumber }
                    }
                )
            }

            // Interactive Inspector for the tapped verse
            selectedAyahForInspector?.let { inspectedAyah ->
                item {
                    AyahInspectorCard(
                        ayah = inspectedAyah,
                        editionId = editionId,
                        fontFamily = scriptureTypeface,
                        isBookmarked = bookmarks.any { it.surahNumber == inspectedAyah.surahNumber && it.ayahNumber == inspectedAyah.ayahNumber },
                        onDismiss = { selectedAyahForInspector = null },
                        onToggleBookmark = { onToggleBookmark(inspectedAyah) }
                    )
                }
            }

            // Clean Translation Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NourSpace.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.fullTranslation,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(
                        onClick = { showTranslationsInContinuous = !showTranslationsInContinuous },
                        shape = PillShape
                    ) {
                        Text(if (showTranslationsInContinuous) strings.hideAll else formatNumerals(String.format(strings.showAllPattern, surah.ayahCount)))
                    }
                }
            }

            if (showTranslationsInContinuous) {
                items(ayahs, key = { it.surahNumber * 1000 + it.ayahNumber }) { ayah ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardSubtleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(NourSpace.lg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatNumerals(String.format(strings.verseLabelPattern, ayah.ayahNumber)),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(NourSpace.xs))
                            Text(
                                text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // End of Surah Navigation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NourSpace.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (surah.number > 1) {
                                onSelectSurah(QuranRepository.getSurah(surah.number - 1))
                                selectedAyahForInspector = null
                            }
                        },
                        enabled = surah.number > 1,
                        shape = PillShape,
                        contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.sm)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(NourSize.smallIcon))
                        Spacer(modifier = Modifier.width(NourSpace.xs))
                        Text(strings.previousSurah, style = MaterialTheme.typography.labelLarge)
                    }

                    TextButton(
                        onClick = {
                            if (surah.number < 114) {
                                onSelectSurah(QuranRepository.getSurah(surah.number + 1))
                                selectedAyahForInspector = null
                            }
                        },
                        enabled = surah.number < 114,
                        shape = PillShape,
                        contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.sm)
                    ) {
                        Text(strings.nextSurah, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.width(NourSpace.xs))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(NourSize.smallIcon))
                    }
                }
            }
        }
    }
}


/**
 * Reading Settings Modal Bottom Sheet:
 * Keeps the reading view uncrowded and clean by isolating font size,
 * translation edition, tafsir, and view modes into an elegant bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSettingsBottomSheet(
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    arabicFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    readingTheme: QuranReadingTheme,
    onReadingThemeChange: (QuranReadingTheme) -> Unit,
    typeface: QuranTypeface,
    onTypefaceChange: (QuranTypeface) -> Unit,
    selectedEditionId: String,
    onSelectEdition: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetExpressiveShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = NourSpace.xl, vertical = NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.readingPrefs,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(NourSize.touchTarget)) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.close)
                }
            }

            // View Mode Segmented Switch
            Column {
                Text(
                    text = strings.layoutStyle,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(NourSpace.sm))
                QuranViewModeToggle(
                    readingMode = readingMode,
                    onModeChange = onModeChange
                )
            }

            // Arabic Font Size Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.arabicFontSize,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${arabicFontSize.toInt()} sp",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(NourSpace.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.fontSizeSampleLetter, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = arabicFontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 18f..40f,
                        steps = 10,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = NourSpace.md)
                    )
                    Text(strings.fontSizeSampleLetter, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }

            // C3 reading theme: System keeps the app scheme; Paper and Night
            // repaint only the reader/page surface, not this sheet.
            Column {
                Text(
                    text = strings.readingThemeTitle,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(NourSpace.sm))
                NourSegmentedControl(
                    options = listOf(
                        NourSegmentOption(
                            value = QuranReadingTheme.SYSTEM,
                            label = strings.readingThemeSystem,
                            testTag = "reading_theme_system"
                        ),
                        NourSegmentOption(
                            value = QuranReadingTheme.PAPER,
                            label = strings.readingThemePaper,
                            testTag = "reading_theme_paper"
                        ),
                        NourSegmentOption(
                            value = QuranReadingTheme.NIGHT,
                            label = strings.readingThemeNight,
                            testTag = "reading_theme_night"
                        )
                    ),
                    selected = readingTheme,
                    onSelected = onReadingThemeChange,
                    modifier = Modifier.testTag("quran_reading_theme_toggle"),
                    role = Role.RadioButton
                )
            }

            // C4 scripture typeface: two options only; the OFL credit travels
            // with the font and stays visible wherever the choice is made.
            Column {
                Text(
                    text = strings.readingTypefaceTitle,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(NourSpace.sm))
                NourSegmentedControl(
                    options = listOf(
                        NourSegmentOption(
                            value = QuranTypeface.SYSTEM,
                            label = strings.readingTypefaceSystem,
                            testTag = "reading_typeface_system"
                        ),
                        NourSegmentOption(
                            value = QuranTypeface.AMIRI,
                            label = strings.readingTypefaceAmiri,
                            testTag = "reading_typeface_amiri"
                        )
                    ),
                    selected = typeface,
                    onSelected = onTypefaceChange,
                    modifier = Modifier.testTag("quran_typeface_toggle"),
                    role = Role.RadioButton
                )
                Spacer(modifier = Modifier.height(NourSpace.xs))
                Text(
                    text = "Amiri Quran © Khaled Hosny, SIL OFL 1.1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Translation Edition Selector
            Column {
                Text(
                    text = strings.translationEdition,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(NourSpace.sm))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QuranRepository.bundledTranslationEditions, key = { it.id }) { edition ->
                        val isSelected = edition.id == selectedEditionId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectEdition(edition.id) },
                            shape = PillShape,
                            label = { Text("${edition.name} (${languageEndonym(edition.language)})", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                containerColor = nourCardSurfaceColor()
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(NourSpace.xs))
                Text(
                    text = strings.translationsBundledNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(NourSpace.sm))
                Text(
                    text = strings.tafsirNotBundledNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(NourSpace.sm))
        }
    }
}

/**
 * Toggle component allowing instant switching between:
 * - strings.cardsMode (each Aya with its own card)
 * - strings.continuousMode (whole Surah in a single scrollable page respecting Ayats)
 */
@Composable
fun QuranViewModeToggle(
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val options = listOf(
        NourSegmentOption(
            value = QuranReadingMode.CARDS,
            label = strings.cardsMode,
            icon = Icons.Rounded.ViewAgenda,
            testTag = "toggle_cards_mode"
        ),
        NourSegmentOption(
            value = QuranReadingMode.CONTINUOUS,
            label = strings.continuousMode,
            icon = Icons.Rounded.AutoStories,
            testTag = "toggle_continuous_mode"
        )
    )
    NourSegmentedControl(
        options = options,
        selected = readingMode,
        onSelected = onModeChange,
        modifier = modifier.testTag("quran_view_mode_toggle"),
        role = Role.RadioButton
    )
}

/**
 * Renders the verses of the Quran as flowing continuous text while respecting Ayats.
 * Each Ayah is followed by the traditional Uthmani ayah-end marker (۝) with Eastern Arabic numerals.
 */
@Composable
fun ContinuousQuranTextCard(
    ayahs: List<Ayah>,
    arabicFontSize: Float,
    selectedAyahNumber: Int?,
    onSelectAyahNumber: (Int) -> Unit,
    fontFamily: FontFamily? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val useEasternNumerals = LocalUseEasternNumerals.current
    val highlightContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val onHighlightContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
    val ayahMarkerColor = MaterialTheme.colorScheme.primary

    val continuousAnnotatedString = remember(ayahs, selectedAyahNumber, arabicFontSize, highlightContainerColor) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val isSelected = (ayah.ayahNumber == selectedAyahNumber)
                pushStringAnnotation(tag = "AYAH", annotation = "${ayah.ayahNumber}")

                if (isSelected) {
                    pushStyle(
                        SpanStyle(
                            background = highlightContainerColor,
                            color = onHighlightContainerColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Append the Quranic Arabic text of the verse
                append(ayah.arabicText)
                append(" ")

                // Append the traditional Ayah end marker (۝) and Eastern Arabic digits
                pushStyle(
                    SpanStyle(
                        color = ayahMarkerColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = (arabicFontSize * 0.9f).sp
                    )
                )
                append("\u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)} ")
                pop()

                if (isSelected) {
                    pop()
                }

                pop()
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder), CardExpressiveShape)
            .testTag("continuous_text_surface"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = NourSize.flat)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NourSpace.xl)
        ) {
            // NUR-016: the whole continuous scripture run is one Arabic block
            // (ayah text + U+06DD markers, no interface text inside it), so it
            // carries its own RTL layout scope; the textDirection style stays
            // as the paragraph-direction belt-and-braces for the spans.
            ScriptureBox {
                Text(
                    text = continuousAnnotatedString,
                    fontSize = arabicFontSize.sp,
                    fontFamily = fontFamily,
                    lineHeight = (arabicFontSize * 2.1f).sp,
                    textAlign = TextAlign.Right,
                    style = TextStyle(
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            customActions = ayahs.map { ayah ->
                                CustomAccessibilityAction(Numerals.convert(String.format(strings.selectVersePattern, ayah.ayahNumber), useEasternNumerals)) {
                                    onSelectAyahNumber(ayah.ayahNumber)
                                    true
                                }
                            }
                        }
                        .pointerInput(continuousAnnotatedString) {
                            detectTapGestures { offset ->
                                textLayoutResult?.let { layout ->
                                    val charOffset = layout.getOffsetForPosition(offset)
                                    val annotations = continuousAnnotatedString.getStringAnnotations(
                                        tag = "AYAH",
                                        start = charOffset,
                                        end = charOffset
                                    )
                                    val tappedAyah = annotations.firstOrNull()?.item?.toIntOrNull()
                                    if (tappedAyah != null) {
                                        onSelectAyahNumber(tappedAyah)
                                    }
                                }
                            }
                        },
                    onTextLayout = { textLayoutResult = it }
                )
            }
        }
    }
}

/**
 * An interactive card that surfaces the translation, tafsir, and actions
 * for an Ayah tapped in continuous text view.
 */
@Composable
fun AyahInspectorCard(
    ayah: Ayah,
    editionId: String,
    fontFamily: FontFamily? = null,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val surah = QuranRepository.getSurah(ayah.surahNumber)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(NourSize.selectedBorder, MaterialTheme.colorScheme.primary, CardExpressiveShape)
            .testTag("ayah_inspector_card"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = NourSize.flat)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NourSpace.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = PillShape
                ) {
                    Text(
                        text = formatNumerals(String.format(strings.surahVerseLabelPattern, if (LocalAppLanguage.current == AppLanguage.ARABIC) surah.nameArabic else surah.nameEnglish, ayah.ayahNumber)),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.xs)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(NourSize.touchTarget)
                ) {
                    Icon(Icons.Rounded.Clear, contentDescription = strings.closeInspector)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // NUR-016: scoped RTL, interface-independent; translation and
            // action row below stay in the interface direction.
            ScriptureBox {
                Text(
                    text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                    lineHeight = if (fontFamily == null) 30.sp else (18f * 2.1f).sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(NourSpace.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(NourSpace.sm))

            Text(
                text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(NourSpace.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NourSpace.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleBookmark,
                    shape = PillShape,
                    contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(NourSpace.xs))
                    Text(if (isBookmarked) strings.savedLabel else strings.bookmarkLabel, style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = {
                        val copyText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}]"
                        clipboardManager.setText(AnnotatedString(copyText))
                        Toast.makeText(context, strings.verseCopied, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(NourSize.touchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = strings.copyAction,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val shareText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n\n" + String.format(strings.quranShareAttribution, surah.nameEnglish, ayah.surahNumber, ayah.ayahNumber)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, strings.shareChooser))
                    },
                    modifier = Modifier.size(NourSize.touchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = strings.shareAction,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarksView(
    bookmarks: List<BookmarkEntity>,
    onGoToAyah: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(NourSpace.xxl),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(NourSize.icon),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = strings.bookmarksEmptyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.bookmarksEmptyDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Horizontal margin comes from the global NourContentFrame gutter.
            contentPadding = PaddingValues(top = NourSpace.lg, bottom = LocalFloatingBottomExtra.current),
            verticalArrangement = Arrangement.spacedBy(NourSpace.md)
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(nourSegmentedControlHeight() + NourSpace.sm * 2)
                )
            }
            items(bookmarks, key = { it.id }) { bm ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoToAyah(bm) },
                    shape = CardExpressiveShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = null
                ) {
                    Column(modifier = Modifier.padding(NourSpace.lg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatNumerals("${if (LocalAppLanguage.current == AppLanguage.ARABIC) QuranRepository.getSurah(bm.surahNumber).nameArabic else bm.surahName} (${bm.surahNumber}:${bm.ayahNumber})"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { onDeleteBookmark(bm.id) },
                                modifier = Modifier.size(NourSize.touchTarget)
                            ) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = strings.removeBookmark,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(NourSpace.sm))
                        // Resolve from the verified corpus; keep stored text only for
                        // references that no longer resolve (never fabricate).
                        val liveAyah = remember(bm.id) {
                            QuranRepository.resolveAyah(bm.surahNumber, bm.ayahNumber)
                        }
                        // NUR-016: bookmarked verse scoped RTL independent of
                        // the interface language; the stored-text fallback
                        // comes from the same verified corpus.
                        ScriptureBox {
                            Text(
                                text = liveAyah?.arabicText ?: bm.ayahText,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (liveAyah == null) {
                            Text(
                                text = strings.savedNotInText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = liveAyah.getTranslation("sahih_en", strings.translationNotAvailable),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wraps an Arabic (right-to-left) run in Unicode bidi isolates.
 *
 * A mixed line such as "الفاتحة • 7 Verses" is a single string handed to one
 * `Text`, so the platform bidi algorithm is free to interleave the Latin words
 * into the Arabic run — which renders as "7 • الفاتحة Verses". Isolating the
 * Arabic keeps both parts whole in an LTR and an RTL interface alike, and keeps
 * the scripture's own direction independent of the interface language.
 */
private fun String.isolateRtlDirection(): String = "\u2067$this\u2069"

/**
 * Language endonym for a BCP-47 tag, e.g. "sv" -> "svenska", "zh" -> "中文",
 * "ar" -> "العربية". Uses the platform display name in its own language so the
 * translation-edition chips never show English language names inside another
 * interface language. Falls back to the raw tag if the platform has no name.
 */
private fun languageEndonym(tag: String): String {
    val locale = java.util.Locale.forLanguageTag(tag)
    return locale.getDisplayLanguage(locale).ifBlank { tag }
}
