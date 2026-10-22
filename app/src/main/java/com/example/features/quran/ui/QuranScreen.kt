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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
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
    onOpenSurahConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }
    val prefs = remember { NourPreferences(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surah list, 1 = Bookmarks, 2 = Search
    var currentPageNumber by remember { mutableIntStateOf(1) } // 1 to 604
    var selectedSurahForReading by remember { mutableStateOf<Surah?>(null) }
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
    var selectedTafsirId by remember { mutableStateOf("muyassar") }
    var arabicFontSize by remember { mutableFloatStateOf(prefs.arabicFontSize) }
    var lastReadSurahNumber by remember { mutableIntStateOf(prefs.lastReadSurah) }
    var lastReadAyahNumber by remember { mutableIntStateOf(prefs.lastReadAyah) }

    // Persist reading preferences/continuity whenever they change
    LaunchedEffect(readingMode) { prefs.readingMode = readingMode.name }
    LaunchedEffect(arabicFontSize) { prefs.arabicFontSize = arabicFontSize }
    LaunchedEffect(lastReadSurahNumber) { prefs.lastReadSurah = lastReadSurahNumber }
    LaunchedEffect(lastReadAyahNumber) { prefs.lastReadAyah = lastReadAyahNumber }

    // Tafsir Modal state
    var selectedTafsirAyah by remember { mutableStateOf<Ayah?>(null) }

    // Direct Page Jump dialog
    var showPageJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }

    // Reading Settings Bottom Sheet state
    var showReadingSettingsSheet by remember { mutableStateOf(false) }

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
    LaunchedEffect(openSurahRequest) {
        if (openSurahRequest != null) {
            selectedSurahForReading = openSurahRequest
            lastReadSurahNumber = openSurahRequest.number
            lastReadAyahNumber = 1
            onOpenSurahConsumed()
        }
    }

    // Predictive back: while the surah reader is open, system back returns to
    // the library instead of leaving the app. This is composed after the host's
    // BackHandler, so it takes precedence only while the reader is showing.
    BackHandler(enabled = selectedSurahForReading != null) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
            // Uncluttered, peaceful Surah Reader View
            SurahReaderView(
                surah = selectedSurahForReading!!,
                readingMode = readingMode,
                onModeChange = { readingMode = it },
                editionId = selectedEditionId,
                tafsirId = selectedTafsirId,
                arabicFontSize = arabicFontSize,
                bookmarks = bookmarks,
                onBack = { selectedSurahForReading = null },
                onSelectSurah = { newSurah ->
                    selectedSurahForReading = newSurah
                    lastReadSurahNumber = newSurah.number
                    lastReadAyahNumber = 1
                },
                onVisibleVerseChanged = { ayahNumber ->
                    lastReadAyahNumber = ayahNumber
                },
                initialAyah = if (selectedSurahForReading!!.number == lastReadSurahNumber) lastReadAyahNumber else 1,
                onFontSizeChange = { arabicFontSize = it },
                onSelectEdition = { selectedEditionId = it },
                onSelectTafsir = { selectedTafsirAyah = it },
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
                            selectedSurahForReading = surah
                            lastReadSurahNumber = surah.number
                            lastReadAyahNumber = 1
                        },
                        onSelectAyah = { ayah ->
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

    // Direct Page Jump Dialog
    if (showPageJumpDialog) {
        AlertDialog(
            onDismissRequest = { showPageJumpDialog = false },
            shape = DialogExpressiveShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = strings.jumpTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                OutlinedTextField(
                    value = jumpPageInput,
                    onValueChange = { jumpPageInput = it },
                    label = { Text(strings.pageNumberLabel) },
                    placeholder = { Text(strings.pagePlaceholder) },
                    singleLine = true,
                    shape = CardSubtleShape,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = jumpPageInput.toIntOrNull()
                        if (p != null && p in 1..604) {
                            currentPageNumber = p
                            showPageJumpDialog = false
                        } else {
                            Toast.makeText(context, strings.invalidPage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = PillShape
                ) {
                    Text(strings.goToPageBtn)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPageJumpDialog = false },
                    shape = PillShape
                ) { Text(strings.cancel) }
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
            selectedEditionId = selectedEditionId,
            onSelectEdition = { selectedEditionId = it },
            selectedTafsirId = selectedTafsirId,
            onSelectTafsir = { selectedTafsirId = it },
            onDismiss = { showReadingSettingsSheet = false }
        )
    }

    // Tafsir Reflection Modal
    selectedTafsirAyah?.let { ayah ->
        val surah = QuranRepository.getSurah(ayah.surahNumber)
        AlertDialog(
            onDismissRequest = { selectedTafsirAyah = null },
            shape = DialogExpressiveShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Column {
                    Text(
                        text = formatNumerals("${strings.tafsirLabel} • ${if (LocalAppLanguage.current == AppLanguage.ARABIC) surah.nameArabic else surah.nameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})"),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = strings.authenticExegesis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CardSubtleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Right,
                            lineHeight = 30.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NourSpace.lg)
                        )
                    }

                    Column {
                        Text(
                            text = strings.muyassarTitle,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(NourSpace.xs))
                        val muyassarTafsir = ayah.getTafsir("muyassar")
                        Text(
                            text = muyassarTafsir ?: strings.muyassarMissing,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column {
                        Text(
                            text = strings.kathirTitle,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(NourSpace.xs))
                        val kathirTafsir = ayah.getTafsir("kathir")
                        Text(
                            text = kathirTafsir ?: strings.kathirMissing,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTafsirAyah = null },
                    shape = PillShape
                ) {
                    Text(strings.close)
                }
            }
        )
    }
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
    val strings = LocalAppStrings.current
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
                                Text(
                                    text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderView(
    surah: Surah,
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    editionId: String,
    tafsirId: String,
    arabicFontSize: Float,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit,
    onSelectSurah: (Surah) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onSelectEdition: (String) -> Unit,
    onSelectTafsir: (Ayah) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    onVisibleVerseChanged: ((Int) -> Unit)? = null,
    initialAyah: Int = 1
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val ayahs = remember(surah.number) { QuranRepository.getAyahsForSurah(surah.number) }

    val listState = rememberLazyListState()

    // Per-verse continuity only in CARDS mode, where each verse is its own list
    // item (item 0 is the header row). Continuous mode renders the Surah as one
    // item, so index-based verse mapping would be fabricated — resume opens the
    // Surah there instead.
    if (readingMode == QuranReadingMode.CARDS) {
        LaunchedEffect(surah.number, ayahs) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { index ->
                    val visible = ayahs.getOrNull(index - 1)
                    if (visible != null) onVisibleVerseChanged?.invoke(visible.ayahNumber)
                }
        }
        // Restore, clamped against the live item count once layout exists.
        LaunchedEffect(surah.number) {
            if (initialAyah > 1) {
                snapshotFlow { listState.layoutInfo.totalItemsCount }
                    .firstOrNull { it > 1 }
                    ?.let { count ->
                        listState.scrollToItem(initialAyah.coerceIn(1, count - 1))
                    }
            }
        }
    }

    val arabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    var showSurahDropdown by remember { mutableStateOf(false) }
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    // Floating clearance rule ([NourSize.floatingClearance]): the content list
    // reserves the toolbar's real measured band plus the clearance slot, so
    // nothing sits behind the pill when reading starts at the top. Initialized
    // from the height token; the first layout pass corrects it (the pill grows
    // at larger font scales, where a fixed reserve would sit wrong).
    val toolbarDensity = LocalDensity.current
    var toolbarReserve by remember {
        mutableStateOf(NourSpace.sm + NourSize.toolbarHeight + NourSize.floatingClearance)
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
                            // Reserve = the pill's band (sm above it) plus the
                            // clearance slot for the first resting card.
                            toolbarReserve =
                                NourSpace.sm + toolbarDensity.run { coords.size.height.toDp() } +
                                    NourSize.floatingClearance
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
                                    text = formatNumerals("${surah.number}. ${if (arabicUi) surah.nameArabic else surah.nameEnglish} ▾"),
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
                                        "${surah.nameArabic.isolateRtlDirection()} • ${surah.ayahCount} ${strings.versesWord}"
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
                                        selectedAyahForInspector = null
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

        // Reading Content
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // Horizontal margin comes from the global NourContentFrame gutter.
            // Vertical: measured toolbar band plus clearance above; the host's
            // floating-bar slot below, both per [NourSize.floatingClearance].
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

                        if (surah.number != 9) { // At-Tawbah does not have Bismillah
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = NourSpace.xs)
                            )
                        }
                    }
                }
            }

            if (readingMode == QuranReadingMode.CARDS) {
                // Verse Cards Mode
                items(ayahs, key = { it.surahNumber * 1000 + it.ayahNumber }) { ayah ->
                    val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    shape = PillShape
                                ) {
                                    Text(
                                        text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = NourSpace.md, vertical = 3.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { onSelectTafsir(ayah) },
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = NourSpace.sm)
                                    ) {
                                        Text(strings.tafsirLabel, style = MaterialTheme.typography.labelMedium)
                                    }

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
                            Text(
                                text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                                fontSize = arabicFontSize.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = (arabicFontSize * 1.8f).sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

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
                            isBookmarked = bookmarks.any { it.surahNumber == inspectedAyah.surahNumber && it.ayahNumber == inspectedAyah.ayahNumber },
                            onDismiss = { selectedAyahForInspector = null },
                            onSelectTafsir = { onSelectTafsir(inspectedAyah) },
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
                                    TextButton(
                                        onClick = { onSelectTafsir(ayah) },
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text(strings.tafsirLabel, style = MaterialTheme.typography.labelSmall)
                                    }
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
}
}

/**
 * Renders a single page of the 604-page Quran Mushaf.
 */
@Composable
fun MushafSinglePageContent(
    pageNumber: Int,
    readingMode: QuranReadingMode,
    editionId: String,
    tafsirId: String,
    arabicFontSize: Float,
    bookmarks: List<BookmarkEntity>,
    onOpenFullSurah: (Surah) -> Unit,
    onSelectTafsir: (Ayah) -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val mushafPage = remember(pageNumber) { QuranRepository.getMushafPage(pageNumber) }
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("mushaf_single_page_$pageNumber"),
        contentPadding = PaddingValues(top = NourSpace.lg, bottom = LocalFloatingBottomExtra.current),
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
            // Surah Header if beginning of a Surah on this page
            mushafPage.surahHeader?.let { surah ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NourSpace.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سُورَةُ ${surah.nameArabic}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${strings.tabSurah} ${surah.nameEnglish} • ${surah.englishMeaning}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { onOpenFullSurah(surah) },
                                shape = PillShape,
                                contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.xs)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(NourSpace.xs))
                                Text(formatNumerals(String.format(strings.readWholeSurahPattern, surah.ayahCount)), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            if (readingMode == QuranReadingMode.CARDS) {
                // Verse Cards on Page
                items(mushafPage.ayahs, key = { it.surahNumber * 1000 + it.ayahNumber }) { ayah ->
                    val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ayah_card_${ayah.ayahNumber}"),
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
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
                                        text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = NourSpace.md, vertical = 3.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { onSelectTafsir(ayah) },
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = NourSpace.sm)
                                    ) {
                                        Text(strings.tafsirLabel, style = MaterialTheme.typography.labelMedium)
                                    }

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
                                            val copyText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n[${ayah.surahNumber}:${ayah.ayahNumber}]"
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
                                }
                            }

                            Spacer(modifier = Modifier.height(NourSpace.md))

                            Text(
                                text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                                fontSize = arabicFontSize.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = (arabicFontSize * 1.8f).sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Continuous Flow on Page
                item {
                    ContinuousQuranTextCard(
                        ayahs = mushafPage.ayahs,
                        arabicFontSize = arabicFontSize,
                        selectedAyahNumber = selectedAyahForInspector?.ayahNumber,
                        onSelectAyahNumber = { tappedNumber ->
                            selectedAyahForInspector = if (selectedAyahForInspector?.ayahNumber == tappedNumber) null else mushafPage.ayahs.firstOrNull { it.ayahNumber == tappedNumber }
                        }
                    )
                }

                // Interactive Inspector
                selectedAyahForInspector?.let { inspectedAyah ->
                    item {
                        AyahInspectorCard(
                            ayah = inspectedAyah,
                            editionId = editionId,
                            isBookmarked = bookmarks.any { it.surahNumber == inspectedAyah.surahNumber && it.ayahNumber == inspectedAyah.ayahNumber },
                            onDismiss = { selectedAyahForInspector = null },
                            onSelectTafsir = { onSelectTafsir(inspectedAyah) },
                            onToggleBookmark = { onToggleBookmark(inspectedAyah) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = NourSpace.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.pageTranslations,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = { showTranslationsInContinuous = !showTranslationsInContinuous },
                            shape = PillShape
                        ) {
                            Text(if (showTranslationsInContinuous) strings.hideBtn else strings.showAllBtn)
                        }
                    }
                }

                if (showTranslationsInContinuous) {
                    items(mushafPage.ayahs, key = { it.surahNumber * 1000 + it.ayahNumber }) { ayah ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardSubtleShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(NourSpace.lg)) {
                                Text(
                                    text = formatNumerals(String.format(strings.verseLabelPattern, ayah.ayahNumber)),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
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

            // Authentic Mushaf Page Number Footer (inside LazyColumn)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NourSpace.md),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = PillShape,
                        border = null
                    ) {
                        Text(
                            text = formatNumerals(String.format(strings.pageFooterPattern, pageNumber)),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = NourSpace.lg, vertical = 6.dp)
                        )
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
    selectedEditionId: String,
    onSelectEdition: (String) -> Unit,
    selectedTafsirId: String,
    onSelectTafsir: (String) -> Unit,
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
                    items(QuranRepository.TRANSLATION_EDITIONS, key = { it.id }) { edition ->
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
            Text(
                text = continuousAnnotatedString,
                fontSize = arabicFontSize.sp,
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

/**
 * An interactive card that surfaces the translation, tafsir, and actions
 * for an Ayah tapped in continuous text view.
 */
@Composable
fun AyahInspectorCard(
    ayah: Ayah,
    editionId: String,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onSelectTafsir: () -> Unit,
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

            Text(
                text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                fontSize = 18.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

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
                Button(
                    onClick = onSelectTafsir,
                    shape = PillShape,
                    contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.tafsirLabel, style = MaterialTheme.typography.labelMedium)
                }

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
                        Text(
                            text = liveAyah?.arabicText ?: bm.ayahText,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
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
