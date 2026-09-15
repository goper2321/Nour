package com.example.features.prayer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.localization.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Simplified, seamless header integrated directly into the dynamic sky background.
 * Merges:
 * 1. Hijri & Gregorian Dates with Location
 * 2. Next Prayer Countdown
 * 3. Interval Progress Bar
 * Rendered without enclosing card containers for an airy, atmospheric experience.
 */
@Composable
fun SeamlessPrayerHeader(
    hijriDate: HijriDateHelper.HijriDate,
    location: UserLocation,
    countdownState: CountdownState,
    todayPrayerTimes: PrayerTimes,
    now: Date,
    onRequestLocationUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val gregorianDate = remember(now) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(now)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = countdownState.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "seamless_prayer_progress"
    )

    val nextPrayerNameLocalized = when (countdownState.nextPrayer) {
        PrayerName.FAJR -> strings.fajr
        PrayerName.SUNRISE -> strings.sunrise
        PrayerName.DHUHR -> strings.dhuhr
        PrayerName.ASR -> strings.asr
        PrayerName.MAGHRIB -> strings.maghrib
        PrayerName.ISHA -> strings.isha
    }

    val currentPrayerNameLocalized = when (countdownState.currentPrayer) {
        PrayerName.FAJR -> strings.fajr
        PrayerName.SUNRISE -> strings.sunrise
        PrayerName.DHUHR -> strings.dhuhr
        PrayerName.ASR -> strings.asr
        PrayerName.MAGHRIB -> strings.maghrib
        PrayerName.ISHA -> strings.isha
    }

    val nextPrayerTime = remember(countdownState.nextPrayer, todayPrayerTimes) {
        todayPrayerTimes.formattedTime(countdownState.nextPrayer)
    }

    val currentPrayerTime = remember(countdownState.currentPrayer, todayPrayerTimes) {
        todayPrayerTimes.formattedTime(countdownState.currentPrayer)
    }

    // High legibility text shadow to guarantee crisp reading over any time-of-day sky background
    val textShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.65f),
            offset = Offset(0f, 2f),
            blurRadius = 6f
        )
    }

    val softShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.5f),
            offset = Offset(0f, 1.5f),
            blurRadius = 4f
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_header_card")
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- SECTION 1: SEAMLESS DATE & LOCATION ROW ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hijri Date (English + Arabic)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = hijriDate.formattedEn,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = textShadow
                        )
                    )
                    Text(
                        text = "•",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            shadow = softShadow
                        )
                    )
                    Text(
                        text = hijriDate.formattedAr,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F),
                            shadow = textShadow
                        )
                    )
                }

                // Location Pill (Minimal, translucent, floating seamlessly, tappable to update location)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable(enabled = onRequestLocationUpdate != null) {
                            onRequestLocationUpdate?.invoke()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (location.isAutoDetected) Icons.Rounded.LocationOn else Icons.Rounded.Refresh,
                        contentDescription = "Location",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (location.isAutoDetected) location.cityName else "${location.cityName} ↻",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.95f),
                            shadow = softShadow
                        )
                    )
                }
            }

            // Gregorian Date
            Text(
                text = gregorianDate,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    shadow = softShadow
                )
            )
        }

        // --- SECTION 2: NEXT PRAYER & COUNTDOWN ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Next Prayer badge label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.nextBadge.uppercase(),
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFFFFD54F),
                            shadow = textShadow
                        )
                    )
                    Text(
                        text = "• $nextPrayerTime",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f),
                            shadow = softShadow
                        )
                    )
                }

                // Prayer Name (English + Arabic)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = nextPrayerNameLocalized,
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = textShadow
                        )
                    )
                    Text(
                        text = countdownState.nextPrayer.arabicName,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.80f),
                            shadow = softShadow
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Digital Countdown Timer
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = countdownState.formattedRemaining,
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = Color.White,
                        shadow = textShadow
                    ),
                    modifier = Modifier.testTag("countdown_timer_text")
                )
                Text(
                    text = "remaining",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.70f),
                        shadow = softShadow
                    )
                )
            }
        }

        // --- SECTION 3: SEAMLESS PROGRESS BAR & INTERVAL METRICS ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sleek Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .testTag("countdown_progress_bar")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedProgress)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFD54F), // Warm gold
                                    Color(0xFF81D4FA), // Sky cyan
                                    Color(0xFF80CBC4)  // Oasis emerald
                                )
                            )
                        )
                )
            }

            // Flanking Labels: Current Prayer -> Elapsed % -> Next Prayer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentPrayerNameLocalized $currentPrayerTime",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        shadow = softShadow
                    )
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD54F),
                        shadow = softShadow
                    )
                )
                Text(
                    text = "$nextPrayerNameLocalized $nextPrayerTime",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        shadow = softShadow
                    )
                )
            }
        }
    }
}
