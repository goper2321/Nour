@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.features.quran.ui

import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.SheetExpressiveShape
import com.example.ui.components.NourEmptyState
import com.example.ui.components.StatusBlurStrip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.LocalSurfaceStyle
import com.example.ui.theme.SurfaceStyle
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.UserLocation
import com.example.features.widgets.ImmersiveSkyBackground
import androidx.compose.ui.res.painterResource
import java.util.Date
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
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.BookmarkEntity
import com.example.database.NourDatabase
import com.example.features.quran.data.QuranRepository
import com.example.features.settings.NourPreferences
import com.example.features.quran.models.Ayah
import com.example.features.quran.models.RevelationType
import com.example.features.quran.models.Surah
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
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

/** Fixed pill-row content height shared by the overlay and the in-flow spacers. */
private val PillsBarHeight = 48.dp

/**
 * Floating tab pills shared by the library lists. Rendered in sticky headers
 * (in-flow layout makes overlap structurally impossible) as frosted glass;
 * the blurred status strip above provides the live glass-blur.
 */
@Composable
private fun GlassPillsRow(
    selectedTab: Int,
    bookmarkCount: Int,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NourSpace.lg, vertical = NourSpace.sm),
            horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            GlassPillTab(
                selected = selectedTab == 0,
                onClick = { onTabSelect(0) },
                label = strings.tabSurah,
                modifier = Modifier.weight(1f),
                hazeState = hazeState
            )
            GlassPillTab(
                selected = selectedTab == 1,
                onClick = { onTabSelect(1) },
                label = if (bookmarkCount > 0) formatNumerals("${strings.tabBookmark} ($bookmarkCount)")
                else strings.tabBookmark,
                modifier = Modifier.weight(1f),
                hazeState = hazeState
            )
            GlassPillTab(
                selected = selectedTab == 2,
                onClick = { onTabSelect(2) },
                label = strings.tabSearch,
                modifier = Modifier.weight(1f),
                hazeState = hazeState
            )
        }
    }
}

/**
 * One frosted pill. In Blur mode the pill gets a real 16dp backdrop blur via
 * Haze (transparent container + effect layer, exactly like the prayer cards);
 * otherwise it uses the solid/frosted fills. Selection shows via tint.
 */
@Composable
private fun GlassPillTab(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val surfaceMode = LocalSurfaceStyle.current
    val hazeOn = surfaceMode == SurfaceStyle.BLUR && hazeState != null
    val solidFill =
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow
    val glassStyle = HazeStyle(
        backgroundColor = Color.Transparent,
        tint = HazeTint(solidFill.copy(alpha = NourAlpha.GlassTint)),
        blurRadius = 16.dp,
        noiseFactor = 0f
    )
    Box(
        modifier = modifier
            .clip(PillShape)
            .then(
                if (hazeOn) Modifier.hazeEffect(state = hazeState!!, style = glassStyle)
                else Modifier
            )
            .background(
                when {
                    hazeOn -> Color.Transparent
                    surfaceMode == SurfaceStyle.OPAQUE -> solidFill
                    selected -> solidFill.copy(alpha = NourAlpha.SelectedTint)
                    else -> solidFill.copy(alpha = NourAlpha.FrostedFill)
                },
                shape = PillShape
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = PillShape
            )
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = NourSpace.md)
            .height(PillsBarHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surahs, 1 = Mushaf, 2 = Bookmarks
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
    LaunchedEffect(openSurahRequest) {
        if (openSurahRequest != null) {
            selectedSurahForReading = openSurahRequest
            lastReadSurahNumber = openSurahRequest.number
            onOpenSurahConsumed()
        }
    }

    val hazeState = remember { HazeState() }
    // No topbar: library content runs edge-to-edge beneath the status bar,
    // with the pills floating as glass over the scrolling content.
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
                    .hazeSource(state = hazeState)
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
                        },
                        onSelectAyah = { ayah ->
                            selectedSurahForReading = QuranRepository.getSurah(ayah.surahNumber)
                            lastReadSurahNumber = ayah.surahNumber
                        },
                        )
                }
            }
            }
            GlassPillsRow(
                selectedTab = selectedTab,
                bookmarkCount = bookmarks.size,
                onTabSelect = { selectedTab = it },
                modifier = Modifier.align(Alignment.TopCenter),
                hazeState = hazeState
            )
        }
        StatusBlurStrip(
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CardSubtleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
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
                        Spacer(modifier = Modifier.height(4.dp))
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
                        Spacer(modifier = Modifier.height(4.dp))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
    ) {
        Column(Modifier.padding(NourSpace.lg)) {
        Text(strings.continueReadingBtn, style = MaterialTheme.typography.labelMedium)
        Text(
            if (arabicUi) lastReadSurah.nameArabic else lastReadSurah.nameEnglish,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        if (arabicUi) {
            val revelationAr = if (lastReadSurah.revelationType == RevelationType.MECCAN) strings.meccan else strings.medinan
            Text(
                formatNumerals("${lastReadSurah.number} · $revelationAr • ${lastReadSurah.ayahCount} ${strings.versesWord}"),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text("${lastReadSurah.number} · ${lastReadSurah.englishMeaning}", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().padding(top = NourSpace.md),
            color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
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
        contentPadding = PaddingValues(start = NourSpace.lg, end = 16.dp, top = NourSpace.xs, bottom = NourSpace.xl),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        // Spacer reserves the overlay footprint so cards never slide under the pills.
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(PillsBarHeight + NourSpace.sm * 2)
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

        items(QuranRepository.ALL_SURAHS) { surah ->
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
    val matchingAyahs = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else QuranRepository.search(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = NourSpace.lg)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(PillsBarHeight + NourSpace.sm * 2)
        )
        // Minimalist search bar: slim pill, autofocus on entering the tab,
        // keyboard Search action dismisses the keyboard.
        val focusManager = LocalFocusManager.current
        val searchFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) { searchFocus.requestFocus() }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NourSpace.sm, bottom = NourSpace.xs)
                .focusRequester(searchFocus)
                .testTag("quran_search_field"),
            placeholder = {
                Text(
                    strings.searchVerseHint,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.clearSearch
                        )
                    }
                }
            },
            shape = PillShape,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        if (searchQuery.isBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md),
                contentPadding = PaddingValues(bottom = NourSpace.xl)
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
                items(popularSurahs) { surah ->
                    SurahListItem(surah, onClick = { onSelectSurah(surah) })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md),
                contentPadding = PaddingValues(bottom = NourSpace.xl)
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
                    items(matchingSurahs) { surah ->
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
                    items(matchingAyahs) { ayah ->
                        val surah = QuranRepository.getSurah(ayah.surahNumber)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardExpressiveShape)
                                .clickable { onSelectAyah(ayah) },
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
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (matchingSurahs.isEmpty() && matchingAyahs.isEmpty()) {
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
    var showAtmosphericRain by remember { mutableStateOf(false) }

    val prefs = remember { NourPreferences(context) }
    val userLocation = remember { prefs.savedLocation ?: UserLocation() }
    val todayPrayerTimes = remember(userLocation) {
        PrayerCalculationEngine.calculate(
            date = Date(),
            location = userLocation,
            method = prefs.calculationMethod,
            madhab = prefs.madhab,
            highLatRule = prefs.highLatRule,
            adjustments = prefs.adjustments
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_reader_view")
    ) {
        // Real-time astronomical sky background with Google Weather-style SVG illustrations
        ImmersiveSkyBackground(
            prayerTimes = todayPrayerTimes,
            location = userLocation,
            isReadingMode = true,
            showPrecipitation = showAtmosphericRain,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Minimalist, Peaceful Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                shadowElevation = 0.dp
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NourSpace.sm, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back & Surah Switcher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("surah_reader_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backToSurahs)
                    }

                    Box {
                        TextButton(
                            onClick = { showSurahDropdown = true },
                            shape = PillShape,
                            contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.xs)
                        ) {
                            Column {
                                Text(
                                    text = formatNumerals("${surah.number}. ${if (arabicUi) surah.nameArabic else surah.nameEnglish} ▾"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formatNumerals("${surah.nameArabic} • ${surah.ayahCount} ${strings.versesWord}"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }

                // Quick Action Icons (Weather Ambiance + View Mode Switcher + Reading Settings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Atmospheric Weather Ambiance Toggle (Clouds, Rain, Sun & Moon)
                    IconButton(
                        onClick = { showAtmosphericRain = !showAtmosphericRain },
                        modifier = Modifier.testTag("sky_weather_toggle_button")
                    ) {
                        Icon(
                            painter = painterResource(com.example.R.drawable.ic_weather_rain),
                            contentDescription = "Atmospheric Rain & Details",
                            tint = if (showAtmosphericRain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick Flow vs Cards Switcher
                    IconButton(
                        onClick = {
                            val nextMode = if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS
                            onModeChange(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Rounded.ViewAgenda else Icons.Rounded.AutoStories,
                            contentDescription = strings.toggleMode,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Reading Settings Modal Trigger
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = strings.readingSettings,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Reading Content
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
        ) {
            // Delicate, Spiritual Surah Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardExpressiveShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f)),
                    border = null
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
                        Spacer(modifier = Modifier.height(4.dp))
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
                items(ayahs) { ayah ->
                    val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ayah_card_${ayah.ayahNumber}"),
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f)),
                        border = null,
                        elevation = CardDefaults.cardElevation(defaultElevation = NourSize.flat)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NourSpace.lg)
                        ) {
                            // Header Row with Ayah badge & clean action icons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = PillShape
                                ) {
                                    Text(
                                        text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
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
                                            val shareText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n\n— [Surah ${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}] via Nour"
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

                            Spacer(modifier = Modifier.height(12.dp))

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
                    items(ayahs) { ayah ->
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
                                Spacer(modifier = Modifier.height(4.dp))
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
                    border = null
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
                            shape = PillShape
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.previousSurah)
                        }

                        TextButton(
                            onClick = {
                                if (surah.number < 114) {
                                    onSelectSurah(QuranRepository.getSurah(surah.number + 1))
                                    selectedAyahForInspector = null
                                }
                            },
                            enabled = surah.number < 114,
                            shape = PillShape
                        ) {
                            Text(strings.nextSurah)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * 604-Page Quran Mushaf Viewer with memory-efficient HorizontalPager and LazyColumn modes.
 * Only visible and adjacent pages are rendered and retained in memory.
 */
@Composable
fun MushafPageView(
    pageNumber: Int,
    onPageChanged: (Int) -> Unit,
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    editionId: String,
    tafsirId: String,
    arabicFontSize: Float,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onJumpPageClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectTafsir: (Ayah) -> Unit,
    onOpenFullSurah: (Surah) -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    bookmarks: List<BookmarkEntity>
) {
    val strings = LocalAppStrings.current
    var isPagerMode by remember { mutableStateOf(true) }
    val currentPageData = remember(pageNumber) { QuranRepository.getMushafPage(pageNumber) }

    val pagerState = rememberPagerState(
        initialPage = (pageNumber - 1).coerceIn(0, 603)
    ) { 604 }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (pageNumber - 1).coerceIn(0, 603)
    )

    LaunchedEffect(pageNumber) {
        if (isPagerMode) {
            if (pagerState.currentPage != pageNumber - 1) {
                pagerState.scrollToPage(pageNumber - 1)
            }
        } else {
            if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != pageNumber - 1) {
                listState.scrollToItem(pageNumber - 1)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (isPagerMode) {
            val newPage = pagerState.currentPage + 1
            if (newPage != pageNumber) {
                onPageChanged(newPage)
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isPagerMode && listState.isScrollInProgress) {
            val newPage = listState.firstVisibleItemIndex + 1
            if (newPage != pageNumber) {
                onPageChanged(newPage)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NourSpace.lg, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onJumpPageClick,
                    shape = PillShape
                ) {
                    Text(
                        text = formatNumerals(String.format(strings.pageIndicatorPattern, pageNumber, currentPageData.juzNumber)),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isPagerMode = !isPagerMode }) {
                        Icon(
                            imageVector = if (isPagerMode) Icons.Rounded.SwapHoriz else Icons.Rounded.SwapVert,
                            contentDescription = if (isPagerMode) strings.switchVertical else strings.switchPageFlip,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            val nextMode = if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS
                            onModeChange(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Rounded.ViewAgenda else Icons.Rounded.AutoStories,
                            contentDescription = strings.toggleMode,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = strings.readingSettings,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (isPagerMode) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("mushaf_horizontal_pager"),
                beyondViewportPageCount = 1,
                key = { it }
            ) { pageIdx ->
                MushafSinglePageContent(
                    pageNumber = pageIdx + 1,
                    readingMode = readingMode,
                    editionId = editionId,
                    tafsirId = tafsirId,
                    arabicFontSize = arabicFontSize,
                    bookmarks = bookmarks,
                    onOpenFullSurah = onOpenFullSurah,
                    onSelectTafsir = onSelectTafsir,
                    onToggleBookmark = onToggleBookmark
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("mushaf_vertical_lazy_column"),
                contentPadding = PaddingValues(NourSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
            ) {
                items(604, key = { it }) { pageIdx ->
                    MushafSinglePageContent(
                        pageNumber = pageIdx + 1,
                        readingMode = readingMode,
                        editionId = editionId,
                        tafsirId = tafsirId,
                        arabicFontSize = arabicFontSize,
                        bookmarks = bookmarks,
                        onOpenFullSurah = onOpenFullSurah,
                        onSelectTafsir = onSelectTafsir,
                        onToggleBookmark = onToggleBookmark
                    )
                    if (pageIdx < 603) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = NourSpace.md),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NourSpace.lg, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPrevPage,
                        enabled = pageNumber > 1,
                        shape = PillShape
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.previousBtn)
                    }

                    Slider(
                        value = pageNumber.toFloat(),
                        onValueChange = { onPageChanged(it.toInt().coerceIn(1, 604)) },
                        valueRange = 1f..604f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = NourSpace.md)
                            .testTag("mushaf_page_slider")
                    )

                    Button(
                        onClick = onNextPage,
                        enabled = pageNumber < 604,
                        shape = PillShape
                    ) {
                        Text(strings.nextBtn)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
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
        contentPadding = PaddingValues(NourSpace.lg),
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
            // Surah Header if beginning of a Surah on this page
            mushafPage.surahHeader?.let { surah ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(formatNumerals(String.format(strings.readWholeSurahPattern, surah.ayahCount)), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            if (readingMode == QuranReadingMode.CARDS) {
                // Verse Cards on Page
                items(mushafPage.ayahs) { ayah ->
                    val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ayah_card_${ayah.ayahNumber}"),
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = null
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
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = PillShape
                                ) {
                                    Text(
                                        text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
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

                            Spacer(modifier = Modifier.height(12.dp))

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
                    items(mushafPage.ayahs) { ayah ->
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
                                Spacer(modifier = Modifier.height(4.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("A", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = arabicFontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 18f..40f,
                        steps = 10,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = NourSpace.md)
                    )
                    Text("A", fontSize = 24.sp, fontWeight = FontWeight.Medium)
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
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QuranRepository.TRANSLATION_EDITIONS) { edition ->
                        val isSelected = edition.id == selectedEditionId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectEdition(edition.id) },
                            shape = PillShape,
                            label = { Text("${edition.name} (${edition.language})", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = PillShape,
        modifier = modifier
            .fillMaxWidth()
            .testTag("quran_view_mode_toggle")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NourSpace.xs)
        ) {
            // Verse Cards button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .heightIn(min = NourSize.touchTarget)
                    .selectable(selected = readingMode == QuranReadingMode.CARDS, role = Role.RadioButton, onClick = { onModeChange(QuranReadingMode.CARDS) })
                    .testTag("toggle_cards_mode"),
                color = if (readingMode == QuranReadingMode.CARDS) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = PillShape
            ) {
                Row(
                    modifier = Modifier.padding(vertical = NourSpace.sm, horizontal = NourSpace.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ViewAgenda,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (readingMode == QuranReadingMode.CARDS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.cardsMode,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (readingMode == QuranReadingMode.CARDS) FontWeight.Medium else FontWeight.Medium,
                        color = if (readingMode == QuranReadingMode.CARDS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Continuous Text button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .heightIn(min = NourSize.touchTarget)
                    .selectable(selected = readingMode == QuranReadingMode.CONTINUOUS, role = Role.RadioButton, onClick = { onModeChange(QuranReadingMode.CONTINUOUS) })
                    .testTag("toggle_continuous_mode"),
                color = if (readingMode == QuranReadingMode.CONTINUOUS) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = PillShape
            ) {
                Row(
                    modifier = Modifier.padding(vertical = NourSpace.sm, horizontal = NourSpace.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (readingMode == QuranReadingMode.CONTINUOUS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.continuousMode,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (readingMode == QuranReadingMode.CONTINUOUS) FontWeight.Medium else FontWeight.Medium,
                        color = if (readingMode == QuranReadingMode.CONTINUOUS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
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
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val ayahMarkerColor = MaterialTheme.colorScheme.primary

    val continuousAnnotatedString = remember(ayahs, selectedAyahNumber, arabicFontSize, primaryContainerColor) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val isSelected = (ayah.ayahNumber == selectedAyahNumber)
                pushStringAnnotation(tag = "AYAH", annotation = "${ayah.ayahNumber}")

                if (isSelected) {
                    pushStyle(
                        SpanStyle(
                            background = primaryContainerColor.copy(alpha = 0.5f),
                            color = onPrimaryContainerColor,
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardExpressiveShape)
            .testTag("continuous_text_surface"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f)
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
            .border(1.5.dp, MaterialTheme.colorScheme.primary, CardExpressiveShape)
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = PillShape
                ) {
                    Text(
                        text = formatNumerals(String.format(strings.surahVerseLabelPattern, if (LocalAppLanguage.current == AppLanguage.ARABIC) surah.nameArabic else surah.nameEnglish, ayah.ayahNumber)),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
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

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ayah.getTranslation(editionId, strings.translationNotAvailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    Spacer(modifier = Modifier.width(4.dp))
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
                        val shareText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId, strings.translationNotAvailable)}\"\n\n— [Surah ${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}] via Nour"
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
fun SearchResultsView(
    results: List<Ayah>,
    query: String,
    editionId: String,
    onSelectAyah: (Ayah) -> Unit
) {
    val strings = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatNumerals(String.format(strings.versesFoundPattern, results.size, query)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(NourSpace.lg)
            )
        }

        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    strings.noVersesMatched,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(NourSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md)
            ) {
                items(results) { ayah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAyah(ayah) },
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = null
                    ) {
                        Column(modifier = Modifier.padding(NourSpace.lg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatNumerals("${strings.tabSurah} ${if (LocalAppLanguage.current == AppLanguage.ARABIC) QuranRepository.getSurah(ayah.surahNumber).nameArabic else QuranRepository.getSurah(ayah.surahNumber).nameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})"),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = formatNumerals("${strings.pageLabel} ${ayah.pageNumber}"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ayah.getTranslation(editionId, strings.translationNotAvailable),
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
                    color = MaterialTheme.colorScheme.primaryContainer,
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
            contentPadding = PaddingValues(NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.md)
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(PillsBarHeight + NourSpace.sm * 2)
                )
            }
            items(bookmarks) { bm ->
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
                        Spacer(modifier = Modifier.height(8.dp))
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
