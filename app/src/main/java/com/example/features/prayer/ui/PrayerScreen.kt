package com.example.features.prayer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppStrings
import com.example.localization.LocalAppStrings
import com.example.localization.LocalUse24HourFormat
import com.example.localization.formatNumerals
import com.example.ui.components.NourContentFrame
import com.example.ui.components.LocalFloatingBottomExtra
import com.example.ui.components.nourCardSurfaceColor
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import kotlinx.coroutines.delay
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
    onRequestLocationUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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

    // Stop adhan if composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            if (AdhanPlayer.isPlaying) {
                AdhanPlayer.stop()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("prayer_sky_page")
    ) {
        // The outer (MainActivity) frame already applies the global side
        // gutter; this nested frame only caps the reading width.
        NourContentFrame(contentPadding = PaddingValues(0.dp)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("prayer_screen_list"),
                contentPadding = PaddingValues(bottom = LocalFloatingBottomExtra.current),
                verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
            ) {
                // Immersive sky header: the sky bleeds up behind the system
                // status bar; content stays clear of it.
                item {
                    SeamlessPrayerHeader(
                        location = location,
                        countdownState = countdownState,
                        todayPrayerTimes = todayPrayerTimes,
                        now = currentTime,
                        onRequestLocationUpdate = onRequestLocationUpdate
                    )
                }

                // Day navigation sits between the sky and the prayer-times card.
                // Tapping the bar returns to today (no separate "Today" button).
                // Shares the NourSpace.lg side measure with the cards below;
                // only the sky hero above is full-bleed.
                item {
                    DaySwitcherBar(
                        selectedDate = displayedDate,
                        dayOffset = selectedDayOffset,
                        onPreviousDay = { selectedDayOffset-- },
                        onNextDay = { selectedDayOffset++ },
                        onResetToToday = { selectedDayOffset = 0 },
                        modifier = Modifier.padding(horizontal = NourSpace.lg)
                    )
                }

                // Simplified, read-only daily prayer times (Fajr -> Isha).
                // Same shared side measure as the day switcher.
                item {
                    SimplifiedPrayerTimesCard(
                        prayerTimes = displayedPrayerTimes,
                        use24Hour = use24Hour,
                        nextPrayer = if (selectedDayOffset == 0) countdownState.nextPrayer else null,
                        modifier = Modifier.padding(horizontal = NourSpace.lg)
                    )
                }
            }
        }
    }
}

/**
 * Read-only summary of the five obligatory prayers for the selected day: one
 * compact card with five plain rows (name, time). There is deliberately no
 * per-prayer completion toggle here — the prayer log is written from the
 * "Mark as prayed" action on the Adhan notification.
 */
@Composable
fun SimplifiedPrayerTimesCard(
    prayerTimes: PrayerTimes,
    use24Hour: Boolean,
    nextPrayer: PrayerName? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val prayers = remember {
        listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )
    }
    Card(
        modifier = modifier.fillMaxWidth().testTag("simplified_prayer_card"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(
            containerColor = nourCardSurfaceColor(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
    ) {
        Column(Modifier.fillMaxWidth()) {
            prayers.forEachIndexed { index, prayer ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline),
                        modifier = Modifier.padding(horizontal = NourSpace.lg)
                    )
                }
                val isNext = prayer == nextPrayer
                if (!isNext) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NourSpace.lg, vertical = NourSpace.md)
                            .testTag("simplified_prayer_row_${prayer.name}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayerNameLabel(prayer, strings),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = formatNumerals(prayerTimes.formattedTime(prayer, use24Hour)),
                            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    // The coming prayer gets a real container — tint, leading
                    // accent bar and a day/night icon — so it reads as more
                    // than colour alone, in any theme and for screen readers.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NourSpace.md, vertical = NourSpace.xs)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                // Merged so the highlighted row is announced as one
                                // item ("Asr, 15:26, Next") instead of three.
                                .semantics(mergeDescendants = true) { stateDescription = strings.nextBadge }
                                .testTag("simplified_prayer_row_${prayer.name}"),
                            shape = CardSubtleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    // Outer md + inner (lg - md) = lg total, so the
                                    // highlighted row's time ends on exactly the
                                    // same x as the plain rows' times (a plain
                                    // row's inset is lg on both sides).
                                    .padding(
                                        horizontal = NourSpace.lg - NourSpace.md,
                                        vertical = NourSpace.lg
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(NourSpace.xs)
                                        .fillMaxHeight()
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            shape = PillShape
                                        )
                                )
                                Spacer(Modifier.width(NourSpace.sm))
                                Icon(
                                    imageVector = nextPrayerIcon(prayer),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(NourSize.smallIcon)
                                )
                                Spacer(Modifier.width(NourSpace.sm))
                                Text(
                                    text = prayerNameLabel(prayer, strings),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    // Fills the remaining width so the time is pinned
                                    // to the trailing edge instead of butting against
                                    // the prayer name.
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatNumerals(prayerTimes.formattedTime(prayer, use24Hour)),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun prayerNameLabel(prayer: PrayerName, strings: AppStrings): String = when (prayer) {
    PrayerName.FAJR -> strings.fajr
    PrayerName.SUNRISE -> strings.sunrise
    PrayerName.DHUHR -> strings.dhuhr
    PrayerName.ASR -> strings.asr
    PrayerName.MAGHRIB -> strings.maghrib
    PrayerName.ISHA -> strings.isha
}

/** Day/night cue for the highlighted coming-prayer row: sun while the sun is up, moon at night. */
private fun nextPrayerIcon(prayer: PrayerName) = when (prayer) {
    PrayerName.FAJR -> Icons.Rounded.DarkMode
    PrayerName.SUNRISE -> Icons.Rounded.LightMode
    PrayerName.DHUHR -> Icons.Rounded.LightMode
    PrayerName.ASR -> Icons.Rounded.LightMode
    PrayerName.MAGHRIB -> Icons.Rounded.DarkMode
    PrayerName.ISHA -> Icons.Rounded.DarkMode
}
