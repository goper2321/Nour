package com.example.features.quran.ui

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.BookmarkEntity
import com.example.database.NourDatabase
import com.example.features.quran.data.QuranRepository
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surahs, 1 = Mushaf, 2 = Bookmarks
    var currentPageNumber by remember { mutableIntStateOf(1) } // 1 to 604
    var selectedSurahForReading by remember { mutableStateOf<Surah?>(null) }
    var readingMode by remember { mutableStateOf(QuranReadingMode.CONTINUOUS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var selectedEditionId by remember { mutableStateOf("sahih_en") }
    var selectedTafsirId by remember { mutableStateOf("muyassar") }
    var arabicFontSize by remember { mutableFloatStateOf(24f) }

    // Tafsir Modal state
    var selectedTafsirAyah by remember { mutableStateOf<Ayah?>(null) }

    // Direct Page Jump dialog
    var showPageJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }

    // Reading Settings Bottom Sheet state
    var showReadingSettingsSheet by remember { mutableStateOf(false) }

    // Last Read Surah tracking (defaults to Al-Fatihah, updates when reading)
    var lastReadSurahNumber by remember { mutableIntStateOf(1) }

    val bookmarks by db.bookmarkDao().getAllBookmarks().collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("quran_screen")
    ) {
        if (selectedSurahForReading != null) {
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
                            Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                        } else {
                            db.bookmarkDao().insertBookmark(
                                BookmarkEntity(
                                    surahNumber = ayah.surahNumber,
                                    ayahNumber = ayah.ayahNumber,
                                    surahName = surah.nameEnglish,
                                    ayahText = ayah.arabicText,
                                    translationText = ayah.getTranslation(selectedEditionId)
                                )
                            )
                            Toast.makeText(context, "Ayah bookmarked!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        } else {
            // Main Quran Browsing View (Spacious, Uncrowded, Serene)
            QuranMainHeader(
                isSearchExpanded = isSearchExpanded || searchQuery.isNotBlank(),
                searchQuery = searchQuery,
                onToggleSearch = {
                    isSearchExpanded = !isSearchExpanded
                    if (!isSearchExpanded) searchQuery = ""
                },
                onSearchQueryChange = { searchQuery = it },
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                bookmarkCount = bookmarks.size
            )

            // Content Area
            if (searchQuery.isNotBlank()) {
                val searchResults = remember(searchQuery) { QuranRepository.search(searchQuery) }
                SearchResultsView(
                    results = searchResults,
                    query = searchQuery,
                    editionId = selectedEditionId,
                    onSelectAyah = { ayah ->
                        selectedSurahForReading = QuranRepository.getSurah(ayah.surahNumber)
                        lastReadSurahNumber = ayah.surahNumber
                        searchQuery = ""
                        isSearchExpanded = false
                    }
                )
            } else {
                when (selectedTab) {
                    0 -> {
                        // Surahs list with quick filter & last read banner
                        SurahListView(
                            lastReadSurah = QuranRepository.getSurah(lastReadSurahNumber),
                            onContinueReading = {
                                selectedSurahForReading = QuranRepository.getSurah(lastReadSurahNumber)
                            },
                            onSurahClick = { surah ->
                                selectedSurahForReading = surah
                                lastReadSurahNumber = surah.number
                            }
                        )
                    }
                    1 -> {
                        // 604-Page Mushaf Viewer
                        MushafPageView(
                            pageNumber = currentPageNumber,
                            readingMode = readingMode,
                            onModeChange = { readingMode = it },
                            editionId = selectedEditionId,
                            tafsirId = selectedTafsirId,
                            arabicFontSize = arabicFontSize,
                            onPrevPage = { if (currentPageNumber > 1) currentPageNumber-- },
                            onNextPage = { if (currentPageNumber < 604) currentPageNumber++ },
                            onJumpPageClick = {
                                jumpPageInput = currentPageNumber.toString()
                                showPageJumpDialog = true
                            },
                            onOpenSettings = { showReadingSettingsSheet = true },
                            onSelectTafsir = { selectedTafsirAyah = it },
                            onOpenFullSurah = { surah ->
                                selectedSurahForReading = surah
                                lastReadSurahNumber = surah.number
                                selectedTab = 0
                            },
                            onToggleBookmark = { ayah ->
                                coroutineScope.launch {
                                    val surah = QuranRepository.getSurah(ayah.surahNumber)
                                    val existing = db.bookmarkDao().getBookmark(ayah.surahNumber, ayah.ayahNumber)
                                    if (existing != null) {
                                        db.bookmarkDao().deleteBookmark(ayah.surahNumber, ayah.ayahNumber)
                                        Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        db.bookmarkDao().insertBookmark(
                                            BookmarkEntity(
                                                surahNumber = ayah.surahNumber,
                                                ayahNumber = ayah.ayahNumber,
                                                surahName = surah.nameEnglish,
                                                ayahText = ayah.arabicText,
                                                translationText = ayah.getTranslation(selectedEditionId)
                                            )
                                        )
                                        Toast.makeText(context, "Ayah bookmarked!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            bookmarks = bookmarks
                        )
                    }
                    2 -> {
                        // Bookmarks View
                        BookmarksView(
                            bookmarks = bookmarks,
                            onGoToAyah = { bm ->
                                selectedSurahForReading = QuranRepository.getSurah(bm.surahNumber)
                                lastReadSurahNumber = bm.surahNumber
                                selectedTab = 0
                            },
                            onDeleteBookmark = { id ->
                                coroutineScope.launch { db.bookmarkDao().deleteById(id) }
                            }
                        )
                    }
                }
            }
        }
    }

    // Direct Page Jump Dialog
    if (showPageJumpDialog) {
        AlertDialog(
            onDismissRequest = { showPageJumpDialog = false },
            shape = CardExpressiveShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = "Jump to Page (1 - 604)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = jumpPageInput,
                    onValueChange = { jumpPageInput = it },
                    label = { Text("Page Number") },
                    placeholder = { Text("1 to 604") },
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
                            Toast.makeText(context, "Enter a page between 1 and 604", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = PillShape
                ) {
                    Text("Go to Page")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPageJumpDialog = false },
                    shape = PillShape
                ) { Text("Cancel") }
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
            shape = CardExpressiveShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Column {
                    Text(
                        text = "Tafsir • ${surah.nameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Authentic Scholarly Exegesis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
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
                                .padding(14.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Tafsir al-Muyassar (التفسير الميسر):",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ayah.getTafsir("muyassar"),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column {
                        Text(
                            text = "Ibn Kathir Reflection:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ayah.getTafsir("kathir"),
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
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Top App Header for Quran browsing with clean typography, expandable search,
 * and modern segmented capsule tabs.
 */
@Composable
fun QuranMainHeader(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    bookmarkCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            // Title & Search Icon Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "The Noble Qur'an",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "القرآن الكريم",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onToggleSearch,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Rounded.Close else Icons.Rounded.Search,
                        contentDescription = "Search Quran",
                        tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable Search Bar
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search Surah, verse, English or Arabic...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = PillShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("quran_search_field")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Modern Segmented Capsule Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QuranTabPill(
                    title = "Surahs",
                    subtitle = "114",
                    isSelected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    modifier = Modifier.weight(1f)
                )
                QuranTabPill(
                    title = "Mushaf",
                    subtitle = "604p",
                    isSelected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    modifier = Modifier.weight(1f)
                )
                QuranTabPill(
                    title = "Saved",
                    subtitle = "$bookmarkCount",
                    isSelected = selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuranTabPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = PillShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Clean, breathable Surah List with spiritual Continue Reading hero card
 * and Revelation Type filter chips.
 */
@Composable
fun SurahListView(
    lastReadSurah: Surah,
    onContinueReading: () -> Unit,
    onSurahClick: (Surah) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Meccan", "Medinan"

    val filteredSurahs = remember(selectedFilter) {
        when (selectedFilter) {
            "Meccan" -> QuranRepository.ALL_SURAHS.filter { it.revelationType == RevelationType.MECCAN }
            "Medinan" -> QuranRepository.ALL_SURAHS.filter { it.revelationType == RevelationType.MEDINAN }
            else -> QuranRepository.ALL_SURAHS
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Last Read Spiritual Card (Inviting, Serene Hero Banner)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onContinueReading() },
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONTINUE READING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${lastReadSurah.number}. ${lastReadSurah.nameEnglish}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${lastReadSurah.englishMeaning} • Page ${lastReadSurah.startPage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = lastReadSurah.nameArabic,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Resume",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Filter Chips (Meccan / Medinan)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to "114",
                    "Meccan" to "86",
                    "Medinan" to "28"
                ).forEach { (label, count) ->
                    val isSelected = selectedFilter == label
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = label },
                        shape = PillShape,
                        label = { Text("$label ($count)", style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // Surah Items (Generously Spaced & Uncluttered)
        items(filteredSurahs) { surah ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardExpressiveShape)
                    .clickable { onSurahClick(surah) }
                    .testTag("surah_item_${surah.number}"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Islamic Number Badge
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CardSubtleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${surah.number}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // English Name & Translation
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = surah.nameEnglish,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = surah.englishMeaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                text = "${surah.ayahCount} verses",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Arabic Name & Revelation details
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = surah.nameArabic,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${surah.revelationType.label} • Juz ${surah.juzNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
    onToggleBookmark: (Ayah) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val ayahs = remember(surah.number) { QuranRepository.getAyahsForSurah(surah.number) }

    var showSurahDropdown by remember { mutableStateOf(false) }
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_reader_view")
    ) {
        // Minimalist, Peaceful Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back & Surah Switcher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("surah_reader_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Surahs")
                    }

                    Box {
                        TextButton(
                            onClick = { showSurahDropdown = true },
                            shape = PillShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${surah.number}. ${surah.nameEnglish} ▾",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${surah.nameArabic} • ${surah.ayahCount} verses",
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
                                    text = { Text("${s.number}. ${s.nameEnglish} (${s.nameArabic})", style = MaterialTheme.typography.bodyMedium) },
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

                // Quick Action Icons (View Mode Switcher + Reading Settings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Flow vs Cards Switcher
                    IconButton(
                        onClick = {
                            val nextMode = if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS
                            onModeChange(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Rounded.ViewAgenda else Icons.Rounded.AutoStories,
                            contentDescription = "Switch View Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Reading Settings Modal Trigger
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Reading Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Reading Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Delicate, Spiritual Surah Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardExpressiveShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "سُورَةُ ${surah.nameArabic}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Surah ${surah.nameEnglish} — ${surah.englishMeaning}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${surah.revelationType.label} • ${surah.ayahCount} Verses • Juz ${surah.juzNumber}",
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
                                modifier = Modifier.padding(vertical = 4.dp)
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
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
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { onSelectTafsir(ayah) },
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Tafsir", style = MaterialTheme.typography.labelMedium)
                                    }

                                    IconButton(
                                        onClick = { onToggleBookmark(ayah) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val copyText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}]"
                                            clipboardManager.setText(AnnotatedString(copyText))
                                            Toast.makeText(context, "Verse copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val shareText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n\n— [Surah ${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}] via Nour"
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = "Share",
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Translation
                            Text(
                                text = ayah.getTranslation(editionId),
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
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full Translation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showTranslationsInContinuous = !showTranslationsInContinuous },
                            shape = PillShape
                        ) {
                            Text(if (showTranslationsInContinuous) "Hide All" else "Show All (${surah.ayahCount})")
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
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Verse ${ayah.ayahNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(
                                        onClick = { onSelectTafsir(ayah) },
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text("Tafsir", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ayah.getTranslation(editionId),
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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                            Text("Previous Surah")
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
                            Text("Next Surah")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean 604-Page Mushaf Viewer with uncluttered header and bottom page turner.
 */
@Composable
fun MushafPageView(
    pageNumber: Int,
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val mushafPage = remember(pageNumber) { QuranRepository.getMushafPage(pageNumber) }
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Minimalist Page Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onJumpPageClick,
                    shape = PillShape
                ) {
                    Text(
                        text = "Page $pageNumber / 604 • Juz ${mushafPage.juzNumber} ▾",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val nextMode = if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS
                            onModeChange(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Rounded.ViewAgenda else Icons.Rounded.AutoStories,
                            contentDescription = "Toggle Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Reader Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سُورَةُ ${surah.nameArabic}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Surah ${surah.nameEnglish} • ${surah.englishMeaning}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { onOpenFullSurah(surah) },
                                shape = PillShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Read Whole Surah (${surah.ayahCount} verses)", style = MaterialTheme.typography.labelMedium)
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
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
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { onSelectTafsir(ayah) },
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Tafsir", style = MaterialTheme.typography.labelMedium)
                                    }

                                    IconButton(
                                        onClick = { onToggleBookmark(ayah) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val copyText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n[${ayah.surahNumber}:${ayah.ayahNumber}]"
                                            clipboardManager.setText(AnnotatedString(copyText))
                                            Toast.makeText(context, "Verse copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy",
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = ayah.getTranslation(editionId),
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
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page Translations",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showTranslationsInContinuous = !showTranslationsInContinuous },
                            shape = PillShape
                        ) {
                            Text(if (showTranslationsInContinuous) "Hide" else "Show All")
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
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Verse ${ayah.ayahNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ayah.getTranslation(editionId),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Clean Bottom Page Turner
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    Text("Previous")
                }

                Text(
                    text = "Page $pageNumber",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onNextPage,
                    enabled = pageNumber < 604,
                    shape = PillShape
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reading Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            // View Mode Segmented Switch
            Column {
                Text(
                    text = "Layout Style",
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
                        text = "Arabic Font Size",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${arabicFontSize.toInt()} sp",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = arabicFontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 18f..40f,
                        steps = 10,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Translation Edition Selector
            Column {
                Text(
                    text = "Translation Edition",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
 * - "Verse Cards" (each Aya with its own card)
 * - "Continuous Text" (whole Surah in a single scrollable page respecting Ayats)
 */
@Composable
fun QuranViewModeToggle(
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(4.dp)
        ) {
            // Verse Cards button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .clickable { onModeChange(QuranReadingMode.CARDS) }
                    .testTag("toggle_cards_mode"),
                color = if (readingMode == QuranReadingMode.CARDS) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = PillShape
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
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
                        text = "Verse Cards",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (readingMode == QuranReadingMode.CARDS) FontWeight.Bold else FontWeight.Medium,
                        color = if (readingMode == QuranReadingMode.CARDS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Continuous Text button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .clickable { onModeChange(QuranReadingMode.CONTINUOUS) }
                    .testTag("toggle_continuous_mode"),
                color = if (readingMode == QuranReadingMode.CONTINUOUS) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = PillShape
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
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
                        text = "Continuous Page",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (readingMode == QuranReadingMode.CONTINUOUS) FontWeight.Bold else FontWeight.Medium,
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
                        fontWeight = FontWeight.Bold,
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), CardExpressiveShape)
            .testTag("continuous_text_surface"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        text = "Surah ${surah.nameEnglish} • Verse ${ayah.ayahNumber}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Close Inspector")
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
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ayah.getTranslation(editionId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSelectTafsir,
                    shape = PillShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tafsir", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onToggleBookmark,
                    shape = PillShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBookmarked) "Saved" else "Bookmark", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = {
                        val copyText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}]"
                        clipboardManager.setText(AnnotatedString(copyText))
                        Toast.makeText(context, "Verse copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val shareText = "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n\n— [Surah ${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}] via Nour"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
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
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${results.size} verses found for \"$query\"",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(14.dp)
            )
        }

        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No verses matched your search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { ayah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAyah(ayah) },
                        shape = CardExpressiveShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Surah ${QuranRepository.getSurah(ayah.surahNumber).nameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Page ${ayah.pageNumber}",
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
                                text = ayah.getTranslation(editionId),
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
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No saved verses yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the heart icon while reading any Ayah to save your reflections and favorite verses here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarks) { bm ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoToAyah(bm) },
                    shape = CardExpressiveShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bm.surahName} (${bm.surahNumber}:${bm.ayahNumber})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { onDeleteBookmark(bm.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Remove Bookmark",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bm.ayahText,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bm.translationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
