package com.example.features.prayer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.widgets.ImmersiveSkyBackground
import com.example.localization.LocalAppStrings
import com.example.localization.formatNumerals
import com.example.localization.LocalUse24HourFormat
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeamlessPrayerHeader(
    location: UserLocation,
    countdownState: CountdownState,
    todayPrayerTimes: PrayerTimes,
    now: Date,
    onRequestLocationUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val use24Hour = LocalUse24HourFormat.current

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_header_card"),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        // Hero countdown card integrating the dynamic celestial sky as an intrinsic part of the card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardExpressiveShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    shape = CardExpressiveShape
                )
        ) {
            // 1. Dynamic celestial sky background embedded directly within the countdown card
            ImmersiveSkyBackground(
                prayerTimes = todayPrayerTimes,
                location = location,
                now = now,
                countdownState = countdownState,
                modifier = Modifier
                    .matchParentSize()
                    .testTag("prayer_sky_background")
            )

            // 2. High-legibility atmospheric scrim overlay for daylight, twilight, and night contrast
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.50f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                            )
                        )
                    )
            )

            // 3. Countdown Card Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                // Header row: Next badge & location button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = strings.nextBadge,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(
                        onClick = { onRequestLocationUpdate?.invoke() },
                        enabled = onRequestLocationUpdate != null,
                        shape = PillShape,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("prayer_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = location.cityName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Prayer Name
                Text(
                    text = prayerLabel(countdownState.nextPrayer),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Scheduled Time
                Text(
                    text = formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour)),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )

                // Animated Countdown Digits
                AnimatedContent(
                    targetState = countdownState.formattedRemaining,
                    transitionSpec = {
                        (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)) { it / 3 } +
                            fadeIn(animationSpec = tween(120)))
                            .togetherWith(
                                slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 3 } +
                                    fadeOut(animationSpec = tween(120))
                            )
                    },
                    label = "countdown_animated_digits"
                ) { remaining ->
                    Text(
                        text = formatNumerals(remaining),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("countdown_timer_text")
                    )
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { dayProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(PillShape)
                        .testTag("countdown_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                )

                // Footer Row: current prayer, percentage, next prayer
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
                ) {
                    Text(
                        text = "${prayerLabel(countdownState.currentPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.currentPrayer, use24Hour))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatNumerals("${(dayProgress * 100).toInt()}%"),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "${prayerLabel(countdownState.nextPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
