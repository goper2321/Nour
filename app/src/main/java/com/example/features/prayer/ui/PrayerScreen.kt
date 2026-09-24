package com.example.features.prayer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.widget.Toast
import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourAlpha
import com.example.ui.components.NourBadge
import androidx.compose.material3.IconToggleButton
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.ui.components.NourContentFrame
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WbSunny
import com.example.ui.theme.CardExpressiveShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.localization.LocalAppStrings
import com.example.localization.formatNumerals
import com.example.localization.LocalUse24HourFormat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PrayerScreen(
    location: UserLocation,
    calculationMethod: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments,
    onOpenAdjustments: () -> Unit,
    onOpenSettings: () -> Unit,
    onLocationUpdated: ((UserLocation) -> Unit)? = null,
    onRequestLocationUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }
    val use24Hour = LocalUse24HourFormat.current

    var currentTime by remember { mutableStateOf(Date()) }
    // Update live ticker every second for precise countdown & active window
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    var selectedDayOffset by remember { mutableIntStateOf(0) }

    // Day-bucket key so expensive prayer-time math runs at day boundaries,
    // not on every 1s countdown tick.
    val todayDayKey = remember(currentTime) {
        val cal = Calendar.getInstance()
        cal.time = currentTime
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val displayedDate = remember(todayDayKey, selectedDayOffset) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = todayDayKey
        if (selectedDayOffset != 0) {
            cal.add(Calendar.DAY_OF_YEAR, selectedDayOffset)
        }
        cal.time
    }

    val todayPrayerTimes = remember(todayDayKey, location, calculationMethod, madhab, highLatRule, adjustments) {
        PrayerCalculationEngine.calculate(
            date = Date(todayDayKey),
            location = location,
            method = calculationMethod,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )
    }

    val displayedPrayerTimes = remember(displayedDate, location, calculationMethod, madhab, highLatRule, adjustments) {
        if (selectedDayOffset == 0) {
            todayPrayerTimes
        } else {
            PrayerCalculationEngine.calculate(
                date = displayedDate,
                location = location,
                method = calculationMethod,
                madhab = madhab,
                highLatRule = highLatRule,
                adjustments = adjustments
            )
        }
    }

    val countdownState = todayPrayerTimes.getCountdownState(currentTime)

    val displayedDateStr = remember(displayedDate) { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(displayedDate) }
    val displayedPrayerLogs by db.prayerLogDao().getLogsForDate(displayedDateStr).collectAsState(initial = emptyList())

    // Stop adhan if composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            if (AdhanPlayer.isPlaying) {
                AdhanPlayer.stop()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("prayer_sky_page")
    ) {
        val skyReveal = NourSpace.md
        NourContentFrame {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("prayer_screen_list"),
            contentPadding = PaddingValues(start = NourSpace.lg, end = NourSpace.lg,
                top = skyReveal, bottom = NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
        ) {
            // Dates, location, and the live next-prayer summary.
            item {
                SeamlessPrayerHeader(
                    location = location,
                    countdownState = countdownState,
                    todayPrayerTimes = todayPrayerTimes,
                    now = currentTime,
                    onRequestLocationUpdate = onRequestLocationUpdate
                )
            }

            // Daily navigation remains separate from the live countdown.
            item {
                DaySwitcherBar(
                    selectedDate = displayedDate,
                    dayOffset = selectedDayOffset,
                    onPreviousDay = { selectedDayOffset-- },
                    onNextDay = { selectedDayOffset++ },
                    onResetToToday = { selectedDayOffset = 0 }
                )
            }

            // Today's / Selected Day's Individual Prayer Cards
            items(PrayerName.values()) { prayer ->
                val isToday = selectedDayOffset == 0
                val isNext = isToday && countdownState.nextPrayer == prayer
                val isCurrent = isToday && countdownState.currentPrayer == prayer
                val isSunrise = prayer == PrayerName.SUNRISE
                val log = displayedPrayerLogs.firstOrNull { it.prayerName == prayer.name }
                val isPrayed = log?.status == "PRAYED"

                PrayerCard(
                    prayer = prayer,
                    timeFormatted = displayedPrayerTimes.formattedTime(prayer, use24Hour),
                    isNext = isNext,
                    isCurrent = isCurrent,
                    isSunrise = isSunrise,
                    isPrayed = isPrayed,
                    onTogglePrayed = {
                        coroutineScope.launch {
                            val newStatus = if (isPrayed) "MISSED" else "PRAYED"
                            db.prayerLogDao().insertOrUpdateLog(
                                PrayerLogEntity(
                                    date = displayedDateStr,
                                    prayerName = prayer.name,
                                    status = newStatus
                                )
                            )
                            val stringsNow = com.example.localization.AppStrings.forLanguage(
                                com.example.localization.LocalizationManager.getSavedLanguage(context)
                            )
                            val prayerTitle = when (prayer) {
                                PrayerName.FAJR -> stringsNow.fajr
                                PrayerName.SUNRISE -> stringsNow.sunrise
                                PrayerName.DHUHR -> stringsNow.dhuhr
                                PrayerName.ASR -> stringsNow.asr
                                PrayerName.MAGHRIB -> stringsNow.maghrib
                                PrayerName.ISHA -> stringsNow.isha
                            }
                            val msg = if (!isPrayed) "$prayerTitle ${stringsNow.markedAsPrayed}" else "$prayerTitle ${stringsNow.unmarkedPrayer}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerCard(
    prayer: PrayerName,
    timeFormatted: String,
    isNext: Boolean,
    isCurrent: Boolean,
    isSunrise: Boolean,
    isPrayed: Boolean,
    onTogglePrayed: () -> Unit
) {
    val strings = LocalAppStrings.current
    val title = when (prayer) {
        PrayerName.FAJR -> strings.fajr
        PrayerName.SUNRISE -> strings.sunrise
        PrayerName.DHUHR -> strings.dhuhr
        PrayerName.ASR -> strings.asr
        PrayerName.MAGHRIB -> strings.maghrib
        PrayerName.ISHA -> strings.isha
    }
    // Three visual states: completed (filled check), next (accent ring),
    // upcoming (muted). The ring uses the shared selected-border token.
    val rowState = when {
        isPrayed -> 0
        isNext -> 1
        else -> 2
    }
    val solidContainer =
        if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val titleColor = when (rowState) {
        1 -> MaterialTheme.colorScheme.onPrimaryContainer
        2 -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier.fillMaxWidth()
            .testTag("prayer_card_${prayer.name}"),
        shape = com.example.ui.theme.ListRowShape,
        colors = CardDefaults.cardColors(containerColor = solidContainer,
            contentColor = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
        border = when {
            isNext -> BorderStroke(NourSize.selectedBorder, MaterialTheme.colorScheme.primary)
            isCurrent -> BorderStroke(NourSize.border, MaterialTheme.colorScheme.primary)
            else -> BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
        }
    ) {
        Row(Modifier.fillMaxWidth().padding(NourSpace.lg), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.md)) {
            if (!isSunrise) {
                IconToggleButton(
                    checked = isPrayed,
                    onCheckedChange = { onTogglePrayed() },
                    modifier = Modifier.size(NourSize.touchTarget).testTag("log_prayer_${prayer.name}")
                        .semantics { stateDescription = if (isPrayed) strings.prayedStatus else strings.notPrayedStatus }
                ) {
                    Icon(if (isPrayed) Icons.Rounded.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isPrayed) "${strings.unmarkPrayer} $title" else "${strings.markAsPrayed}: $title",
                        tint = when (rowState) {
                            0 -> MaterialTheme.colorScheme.primary
                            1 -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        })
                }
            } else {
                Icon(Icons.Rounded.WbSunny, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(NourSize.touchTarget).padding(NourSpace.md))
            }
            FlowRow(Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                    Text(title, style = MaterialTheme.typography.titleMedium,
                        color = titleColor,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    val badge = when {
                        isPrayed -> strings.completedBadge
                        isNext -> strings.nextBadge
                        isCurrent -> strings.currentBadge
                        isSunrise -> strings.shuruqBadge
                        else -> ""
                    }
                    if (badge.isNotBlank()) {
                        val badgeColor = when (rowState) {
                            1 -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
                            0 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                Text(formatNumerals(timeFormatted),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    color = titleColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    maxLines = 1, softWrap = false)
            }
        }
    }
}
