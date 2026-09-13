package com.example.features.prayer.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.features.widgets.CelestialPrayerHeroCard
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    var currentTime by remember { mutableStateOf(Date()) }
    // Update live ticker every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
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

    val countdownState = todayPrayerTimes.getCountdownState(currentTime)
    val hijriDate = remember(currentTime) { HijriDateHelper.fromGregorian(currentTime) }

    val todayStr = remember(currentTime) { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentTime) }
    val prayerLogs by db.prayerLogDao().getLogsForDate(todayStr).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Weekly, 2 = Monthly, 3 = Year Ahead

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayer_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar & Islamic Hijri Date Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${location.cityName}, ${location.countryName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // 100% Offline / Local Engine Indicator Badge
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "100% Offline • Local Engine",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = hijriDate.formattedEn,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(currentTime),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = hijriDate.formattedAr,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Unified Aesthetic Celestial Prayer Hero Card with Redesigned Segmented Countdown
        item {
            CelestialPrayerHeroCard(
                prayerTimes = todayPrayerTimes,
                countdownState = countdownState,
                now = currentTime
            )
        }

        // View Tabs: Today, Weekly, Monthly, Year Ahead
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Today's Salah", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Weekly") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Monthly") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Year Ahead") }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Today's Individual Prayer Cards
                items(PrayerName.values()) { prayer ->
                    val isNext = countdownState.nextPrayer == prayer
                    val isCurrent = countdownState.currentPrayer == prayer
                    val isSunrise = prayer == PrayerName.SUNRISE
                    val log = prayerLogs.firstOrNull { it.prayerName == prayer.name }
                    val isPrayed = log?.status == "PRAYED"

                    PrayerCard(
                        prayer = prayer,
                        timeFormatted = todayPrayerTimes.formattedTime(prayer),
                        isNext = isNext,
                        isCurrent = isCurrent,
                        isSunrise = isSunrise,
                        isPrayed = isPrayed,
                        onTogglePrayed = {
                            coroutineScope.launch {
                                val newStatus = if (isPrayed) "MISSED" else "PRAYED"
                                db.prayerLogDao().insertOrUpdateLog(
                                    PrayerLogEntity(
                                        date = todayStr,
                                        prayerName = prayer.name,
                                        status = newStatus
                                    )
                                )
                            }
                        }
                    )
                }
            }
            1 -> {
                // Weekly Schedule (7 Days)
                item {
                    WeeklyScheduleCard(
                        location = location,
                        method = calculationMethod,
                        madhab = madhab,
                        highLatRule = highLatRule,
                        adjustments = adjustments
                    )
                }
            }
            2 -> {
                // Monthly Calendar
                item {
                    MonthlyCalendarCard(
                        location = location,
                        method = calculationMethod,
                        madhab = madhab,
                        highLatRule = highLatRule,
                        adjustments = adjustments
                    )
                }
            }
            3 -> {
                // Year Ahead Preview
                item {
                    YearAheadPreviewCard(
                        location = location,
                        method = calculationMethod,
                        madhab = madhab,
                        highLatRule = highLatRule,
                        adjustments = adjustments
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

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
    val borderColor = when {
        isNext -> MaterialTheme.colorScheme.primary
        isCurrent -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }

    val containerColor = when {
        isNext -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isNext || isCurrent) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("prayer_card_${prayer.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Checkmark for logging prayer
                if (!isSunrise) {
                    IconButton(
                        onClick = onTogglePrayed,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("log_prayer_${prayer.name}")
                    ) {
                        Icon(
                            imageVector = if (isPrayed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (isPrayed) "Prayed" else "Mark as prayed",
                            tint = if (isPrayed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold
                        )
                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "NEXT",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = prayer.arabicName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WeeklyScheduleCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "7-Day Prayer Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val days = remember {
                val list = mutableListOf<PrayerTimes>()
                val cal = Calendar.getInstance()
                for (i in 0 until 7) {
                    list.add(
                        PrayerCalculationEngine.calculate(
                            date = cal.time,
                            location = location,
                            method = method,
                            madhab = madhab,
                            highLatRule = highLatRule,
                            adjustments = adjustments
                        )
                    )
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                list
            }

            days.forEach { pt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(pt.date),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.width(90.dp)
                    )
                    Text(text = pt.formattedTime(PrayerName.FAJR), fontSize = 12.sp)
                    Text(text = pt.formattedTime(PrayerName.DHUHR), fontSize = 12.sp)
                    Text(text = pt.formattedTime(PrayerName.ASR), fontSize = 12.sp)
                    Text(text = pt.formattedTime(PrayerName.MAGHRIB), fontSize = 12.sp)
                    Text(text = pt.formattedTime(PrayerName.ISHA), fontSize = 12.sp)
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun MonthlyCalendarCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val cal = remember { Calendar.getInstance() }
            val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time) }
            Text(
                text = "Full Month Schedule: $monthName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Header labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Date", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(50.dp))
                Text(text = "Fajr", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "Dhuhr", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "Asr", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "Maghrib", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "Isha", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Divider(modifier = Modifier.padding(vertical = 6.dp))

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (day in 1..daysInMonth) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                val pt = PrayerCalculationEngine.calculate(
                    date = cal.time,
                    location = location,
                    method = method,
                    madhab = madhab,
                    highLatRule = highLatRule,
                    adjustments = adjustments
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${day}th", fontSize = 11.sp, modifier = Modifier.width(50.dp))
                    Text(text = pt.formattedTime(PrayerName.FAJR), fontSize = 11.sp)
                    Text(text = pt.formattedTime(PrayerName.DHUHR), fontSize = 11.sp)
                    Text(text = pt.formattedTime(PrayerName.ASR), fontSize = 11.sp)
                    Text(text = pt.formattedTime(PrayerName.MAGHRIB), fontSize = 11.sp)
                    Text(text = pt.formattedTime(PrayerName.ISHA), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun YearAheadPreviewCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Year Ahead Solstice & Equinox Solars",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Key astronomical milestones throughout the upcoming calendar year calculated offline:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            val milestones = listOf(
                "Vernal Equinox (March 21)" to Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.MARCH); set(Calendar.DAY_OF_MONTH, 21) },
                "Summer Solstice (June 21)" to Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.JUNE); set(Calendar.DAY_OF_MONTH, 21) },
                "Autumnal Equinox (Sept 22)" to Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.SEPTEMBER); set(Calendar.DAY_OF_MONTH, 22) },
                "Winter Solstice (Dec 21)" to Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.DECEMBER); set(Calendar.DAY_OF_MONTH, 21) }
            )

            milestones.forEach { (label, cal) ->
                val pt = PrayerCalculationEngine.calculate(
                    date = cal.time,
                    location = location,
                    method = method,
                    madhab = madhab,
                    highLatRule = highLatRule,
                    adjustments = adjustments
                )
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Fajr: ${pt.formattedTime(PrayerName.FAJR)}  •  Dhuhr: ${pt.formattedTime(PrayerName.DHUHR)}  •  Maghrib: ${pt.formattedTime(PrayerName.MAGHRIB)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            }
        }
    }
}
