package com.example.features.prayer.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.localization.LocalAppStrings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.widgets.ImmersiveSkyBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    var currentTime by remember { mutableStateOf(Date()) }
    // Update live ticker every second for precise countdown & active window
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    var selectedDayOffset by remember { mutableIntStateOf(0) }

    val displayedDate = remember(currentTime, selectedDayOffset) {
        if (selectedDayOffset == 0) {
            currentTime
        } else {
            val cal = Calendar.getInstance()
            cal.time = currentTime
            cal.add(Calendar.DAY_OF_YEAR, selectedDayOffset)
            cal.time
        }
    }

    val todayPrayerTimes = remember(currentTime, location, calculationMethod, madhab, highLatRule, adjustments) {
        PrayerCalculationEngine.calculate(
            date = currentTime,
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
    val displayedHijriDate = remember(displayedDate) { HijriDateHelper.fromGregorian(displayedDate) }

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

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Immersive Atmospheric Sky Background spanning the full screen
        // Dynamic Sun & Moon cycle living directly in the background
        ImmersiveSkyBackground(
            prayerTimes = todayPrayerTimes,
            location = location,
            now = currentTime,
            countdownState = countdownState,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Foreground Weather App Style Layered Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("prayer_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Atmospheric Seamless Prayer Header (Merges Hijri & Gregorian Dates, Interactive Location, Next Prayer Countdown & Progress Bar directly onto the dynamic sky background without cards)
            item {
                SeamlessPrayerHeader(
                    hijriDate = displayedHijriDate,
                    location = location,
                    countdownState = countdownState,
                    todayPrayerTimes = todayPrayerTimes,
                    now = displayedDate,
                    onRequestLocationUpdate = onRequestLocationUpdate
                )
            }

            // Atmospheric Day Switcher placed directly over the prayers
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
                    timeFormatted = displayedPrayerTimes.formattedTime(prayer),
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
                            val msg = if (!isPrayed) "${prayer.displayName} marked as prayed" else "${prayer.displayName} unmarked"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Polished, high-craft individual prayer card with celestial iconography,
 * distinct Next/Current badges, audio preview triggers, and smooth check logging.
 */
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
    val borderColor by animateColorAsState(
        targetValue = when {
            isNext -> MaterialTheme.colorScheme.primary
            isCurrent -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        label = "card_border_color"
    )

    val containerColor = when {
        isNext -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
        isCurrent -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
        isSunrise -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f)
    }

    val prayerIcon: ImageVector = when (prayer) {
        PrayerName.FAJR -> Icons.Rounded.WbTwilight
        PrayerName.SUNRISE -> Icons.Rounded.WbSunny
        PrayerName.DHUHR -> Icons.Rounded.WbSunny
        PrayerName.ASR -> Icons.Rounded.WbSunny
        PrayerName.MAGHRIB -> Icons.Rounded.WbTwilight
        PrayerName.ISHA -> Icons.Rounded.NightsStay
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.name}"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isNext || isCurrent) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Checkmark for logging prayer (or subtle icon for sunrise)
                if (!isSunrise) {
                    IconButton(
                        onClick = onTogglePrayed,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("log_prayer_${prayer.name}")
                    ) {
                        Icon(
                            imageVector = if (isPrayed) Icons.Rounded.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (isPrayed) "Prayed" else "Mark as prayed",
                            tint = if (isPrayed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = prayerIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Prayer Title & Badges
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isPrayed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = PillShape
                            ) {
                                Text(
                                    text = strings.nextBadge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = PillShape
                            ) {
                                Text(
                                    text = strings.currentBadge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isSunrise) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = PillShape
                            ) {
                                Text(
                                    text = strings.shuruqBadge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.arabicName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        if (isSunrise) {
                            Text(
                                text = " • Tahajjud concludes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                            )
                        } else if (isPrayed) {
                            Text(
                                text = " • Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right side: Formatted Time
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

