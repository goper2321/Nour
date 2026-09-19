package com.example.features.prayer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.localization.LocalAppStrings
import com.example.localization.LocalSurfaceStyle
import com.example.ui.theme.SurfaceStyle
import com.example.localization.formatNumerals
import com.example.localization.LocalUse24HourFormat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.PillShape
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeamlessPrayerHeader(
    hazeState: HazeState? = null,
    location: UserLocation,
    countdownState: CountdownState,
    todayPrayerTimes: PrayerTimes,
    now: Date,
    onRequestLocationUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val use24Hour = LocalUse24HourFormat.current
    val surfaceMode = LocalSurfaceStyle.current
    val hazeOn = surfaceMode == SurfaceStyle.BLUR && hazeState != null
    fun prayerLabel(prayer: PrayerName) = when (prayer) {
        PrayerName.FAJR -> strings.fajr
        PrayerName.SUNRISE -> strings.sunrise
        PrayerName.DHUHR -> strings.dhuhr
        PrayerName.ASR -> strings.asr
        PrayerName.MAGHRIB -> strings.maghrib
        PrayerName.ISHA -> strings.isha
    }
    // Day-scale progress (Fajr to Isha): a continuous hairline fill that maps
    // to the daily rhythm without the old prayer-boundary ticks.
    val dayProgressTarget = remember(todayPrayerTimes, now) {
        val span = (todayPrayerTimes.isha.time - todayPrayerTimes.fajr.time)
            .toDouble().coerceAtLeast(1.0)
        ((now.time - todayPrayerTimes.fajr.time) / span).toFloat().coerceIn(0f, 1f)
    }
    val dayProgress by animateFloatAsState(
        dayProgressTarget,
        animationSpec = tween(NourMotion.long, easing = NourMotion.easing),
        label = "seamless_day_progress"
    )
    Column(modifier.fillMaxWidth().testTag("prayer_header_card"),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)) {
        // Open sky above the hero card so the background stands out.
        Spacer(modifier = Modifier.height(NourSpace.xl))
        // True 16dp Gaussian backdrop blur + 90% transparent tint (Haze 1.5.x,
        // matched to this project's Compose 1.7 runtime).
        val blurredCardStyle = HazeStyle(
            backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
            tint = HazeTint(MaterialTheme.colorScheme.primaryContainer.copy(alpha = NourAlpha.GlassTint)),
            blurRadius = 16.dp,
            noiseFactor = 0f
        )
        androidx.compose.material3.Card(
            shape = com.example.ui.theme.CardExpressiveShape,
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (hazeOn) androidx.compose.ui.graphics.Color.Transparent
                else if (surfaceMode == SurfaceStyle.OPAQUE) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = NourAlpha.GlassTint),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            border = null,
            modifier = Modifier.fillMaxWidth().then(
                if (hazeOn) Modifier
                    .clip(com.example.ui.theme.CardExpressiveShape)
                    .hazeEffect(
                        state = hazeState!!,
                        style = blurredCardStyle
                    ) else Modifier
            )
        ) {
        Column(Modifier.padding(NourSpace.lg), verticalArrangement = Arrangement.spacedBy(NourSpace.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.nextBadge,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(
                    onClick = { onRequestLocationUpdate?.invoke() },
                    enabled = onRequestLocationUpdate != null,
                    shape = PillShape,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("prayer_location_button")
                ) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        location.cityName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Text(
                prayerLabel(countdownState.nextPrayer),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour)),
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, softWrap = false)
            Text(formatNumerals(countdownState.formattedRemaining),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = NourAlpha.Dimmed),
                maxLines = 1, softWrap = false,
                modifier = Modifier.testTag("countdown_timer_text"))
            LinearProgressIndicator(
                progress = { dayProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .testTag("countdown_progress_bar"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
            )
            FlowRow(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NourSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)) {
                Text("${prayerLabel(countdownState.currentPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.currentPrayer, use24Hour))}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(formatNumerals("${(dayProgress * 100).toInt()}%"),
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
                Text("${prayerLabel(countdownState.nextPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour))}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
        }
    }
}
