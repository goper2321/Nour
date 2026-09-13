package com.example.features.widgets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerTimes
import java.util.Date
import java.util.Locale

/**
 * Unified aesthetic Celestial Prayer Hero Card.
 * Elegantly merges the celestial Sky/Night-Sky visualization (with dynamic Sun/Moon arc)
 * and an architecturally redesigned, high-craft segmented countdown into a single serene, atmospheric card.
 */
@Composable
fun CelestialPrayerHeroCard(
    prayerTimes: PrayerTimes,
    countdownState: CountdownState,
    now: Date = Date(),
    modifier: Modifier = Modifier
) {
    val nowMillis = now.time
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time

    val isDaytime = nowMillis in sunriseMillis..sunsetMillis
    val daytimeProgress = if (isDaytime) {
        val totalDay = (sunsetMillis - sunriseMillis).coerceAtLeast(1L)
        ((nowMillis - sunriseMillis).toDouble() / totalDay).toFloat().coerceIn(0f, 1f)
    } else {
        0.5f // Centered moon for night
    }

    val animatedProgress by animateFloatAsState(
        targetValue = daytimeProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "celestial_arc_progress"
    )

    // Dynamic Atmospheric Sky Gradient matching real celestial stages
    val skyGradient = when {
        nowMillis < prayerTimes.fajr.time -> listOf(Color(0xFF060913), Color(0xFF0E1626), Color(0xFF1E1B4B)) // Deep Night
        nowMillis in prayerTimes.fajr.time until prayerTimes.sunrise.time -> listOf(Color(0xFF1E1B4B), Color(0xFF581C87), Color(0xFFEA580C)) // Dawn Fajr
        nowMillis in prayerTimes.sunrise.time until prayerTimes.dhuhr.time -> listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF7DD3FC)) // Morning
        nowMillis in prayerTimes.dhuhr.time until prayerTimes.asr.time -> listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFFBAE6FD)) // High Noon
        nowMillis in prayerTimes.asr.time until prayerTimes.maghrib.time -> listOf(Color(0xFF9A3412), Color(0xFFD97706), Color(0xFFFDE68A)) // Golden Asr
        nowMillis in prayerTimes.maghrib.time until prayerTimes.isha.time -> listOf(Color(0xFF4C0519), Color(0xFF831843), Color(0xFFFB7185)) // Sunset Maghrib
        else -> listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B)) // Isha / Late Night
    }

    // Countdown hours, minutes, seconds breakdown
    val totalSeconds = (countdownState.remainingMillis / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val hoursFormatted = String.format(Locale.US, "%02d", hours)
    val minutesFormatted = String.format(Locale.US, "%02d", minutes)
    val secondsFormatted = String.format(Locale.US, "%02d", seconds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("celestial_prayer_card"),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(skyGradient))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Status badge & Scheduled Time Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = if (isDaytime) "☀️ Daylight • ${countdownState.currentPrayer.displayName}" else "🌙 Night Sky • ${countdownState.currentPrayer.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Scheduled Time Pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "At ${prayerTimes.formattedTime(countdownState.nextPrayer)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Next Prayer Title: Arabic & English
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = countdownState.nextPrayer.arabicName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE68A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = countdownState.nextPrayer.displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Celestial Visualizer Canvas (Sun / Moon trajectory arc with milestones)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val arcHeight = h * 0.72f
                        val baselineY = h - 16f

                        // Twinkle stars when night
                        if (!isDaytime) {
                            val starOffsets = listOf(
                                Offset(w * 0.10f, h * 0.22f),
                                Offset(w * 0.22f, h * 0.12f),
                                Offset(w * 0.36f, h * 0.32f),
                                Offset(w * 0.64f, h * 0.16f),
                                Offset(w * 0.76f, h * 0.26f),
                                Offset(w * 0.88f, h * 0.14f),
                                Offset(w * 0.93f, h * 0.36f)
                            )
                            starOffsets.forEachIndexed { idx, pt ->
                                val starRadius = if (idx % 2 == 0) 1.5.dp.toPx() else 2.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = if (idx % 2 == 0) 0.85f else 0.45f),
                                    radius = starRadius,
                                    center = pt
                                )
                            }
                        }

                        // Parabolic path of the celestial body
                        val path = Path().apply {
                            moveTo(20f, baselineY)
                            quadraticBezierTo(w / 2f, baselineY - arcHeight, w - 20f, baselineY)
                        }

                        // Dotted trajectory arc
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.3f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        )

                        // Celestial Orb Rendering
                        if (isDaytime) {
                            val t = animatedProgress
                            val currentX = 20f + (w - 40f) * t
                            val currentY = baselineY - (4 * (arcHeight - 12f) * t * (1 - t))

                            // Radiant Sun aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color.Transparent),
                                    center = Offset(currentX, currentY),
                                    radius = 28.dp.toPx()
                                ),
                                radius = 28.dp.toPx(),
                                center = Offset(currentX, currentY)
                            )

                            // Sun Core
                            drawCircle(
                                color = Color(0xFFFFFDE7),
                                radius = 9.dp.toPx(),
                                center = Offset(currentX, currentY)
                            )
                        } else {
                            // Glowing Crescent Moon
                            val moonX = w / 2f
                            val moonY = baselineY - arcHeight * 0.52f

                            // Moon ambient glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x55FEF08A), Color.Transparent),
                                    center = Offset(moonX, moonY),
                                    radius = 26.dp.toPx()
                                ),
                                radius = 26.dp.toPx(),
                                center = Offset(moonX, moonY)
                            )

                            // Outer Moon
                            drawCircle(
                                color = Color(0xFFFEF08A),
                                radius = 11.dp.toPx(),
                                center = Offset(moonX, moonY)
                            )
                            // Inner cut-out for delicate crescent silhouette
                            drawCircle(
                                color = skyGradient[1],
                                radius = 9.5.dp.toPx(),
                                center = Offset(moonX + 4.5.dp.toPx(), moonY - 2.5.dp.toPx())
                            )
                        }

                        // Horizon line
                        drawLine(
                            color = Color.White.copy(alpha = 0.22f),
                            start = Offset(0f, baselineY),
                            end = Offset(w, baselineY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Milestone markers on horizon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "Fajr" to (nowMillis >= prayerTimes.fajr.time && nowMillis < prayerTimes.sunrise.time),
                            "Sunrise" to (nowMillis >= prayerTimes.sunrise.time && nowMillis < prayerTimes.dhuhr.time),
                            "Dhuhr" to (nowMillis >= prayerTimes.dhuhr.time && nowMillis < prayerTimes.asr.time),
                            "Asr" to (nowMillis >= prayerTimes.asr.time && nowMillis < prayerTimes.maghrib.time),
                            "Maghrib" to (nowMillis >= prayerTimes.maghrib.time && nowMillis < prayerTimes.isha.time),
                            "Isha" to (nowMillis >= prayerTimes.isha.time || nowMillis < prayerTimes.fajr.time)
                        ).forEach { (name, isActive) ->
                            Text(
                                text = name,
                                color = if (isActive) Color(0xFFFDE68A) else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // REDESIGNED COUNTDOWN: Segmented Architectural Glassmorphic Capsules
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TIME REMAINING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours Capsule
                        CountdownUnitCapsule(
                            value = hoursFormatted,
                            unit = "HOURS"
                        )

                        // Colon Divider
                        Text(
                            text = ":",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A).copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Minutes Capsule
                        CountdownUnitCapsule(
                            value = minutesFormatted,
                            unit = "MINS"
                        )

                        // Colon Divider
                        Text(
                            text = ":",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A).copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Seconds Capsule
                        CountdownUnitCapsule(
                            value = secondsFormatted,
                            unit = "SECS"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sleek Progress Bar towards next prayer
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { countdownState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFDE68A),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Window elapsed: ${(countdownState.progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Text(
                            text = "Countdown active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFDE68A).copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-craft countdown capsule with frosted background, subtle border,
 * bold monospace numerals, and micro unit label.
 */
@Composable
private fun CountdownUnitCapsule(
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(68.dp),
        color = Color.Black.copy(alpha = 0.38f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFFBEB)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = unit,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFFDE68A).copy(alpha = 0.9f)
            )
        }
    }
}
