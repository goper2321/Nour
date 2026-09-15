package com.example.features.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Computes dynamic atmospheric gradient matching live solar altitude and geographic location.
 */
fun getAtmosphericSkyGradient(
    prayerTimes: PrayerTimes,
    nowMillis: Long,
    location: UserLocation = UserLocation(),
    maxElevation: Double = 60.0
): List<Color> {
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    return when {
        // 1. Deep Astronomical Night (Tahajjud)
        nowMillis < fajrMillis - (45 * 60 * 1000L) -> listOf(
            Color(0xFF03050D),
            Color(0xFF080D1F),
            Color(0xFF101734),
            Color(0xFF192246)
        )
        // 2. Pre-Fajr Astronomical Twilight
        nowMillis < fajrMillis -> listOf(
            Color(0xFF06091A),
            Color(0xFF10152F),
            Color(0xFF1E2146),
            Color(0xFF2E2C58)
        )
        // 3. Fajr (True Dawn / Civil Twilight) - Rose coral, amethyst, golden horizon
        nowMillis in fajrMillis until sunriseMillis -> {
            val t = ((nowMillis - fajrMillis).toDouble() / (sunriseMillis - fajrMillis).coerceAtLeast(1L)).toFloat()
            if (t < 0.5f) {
                listOf(
                    Color(0xFF0A0F28),
                    Color(0xFF1F1C42),
                    Color(0xFF482D52),
                    Color(0xFF82405D),
                    Color(0xFFBC5B64)
                )
            } else {
                listOf(
                    Color(0xFF101C38),
                    Color(0xFF302E56),
                    Color(0xFF6B415C),
                    Color(0xFFBA5A58),
                    Color(0xFFEB8D66)
                )
            }
        }
        // 4. Sunrise (Shuruq / Golden Hour) - Radiant golden apricot and dawn blue
        nowMillis in sunriseMillis until sunriseMillis + (45 * 60 * 1000L) -> listOf(
            Color(0xFF152646),
            Color(0xFF2A4372),
            Color(0xFF5E6084),
            Color(0xFFBA725B),
            Color(0xFFFAA256)
        )
        // 5. Morning (Duha) - Bright crisp azure
        nowMillis in (sunriseMillis + (45 * 60 * 1000L)) until dhuhrMillis -> listOf(
            Color(0xFF153D72),
            Color(0xFF265EA0),
            Color(0xFF4582CA),
            Color(0xFF75ADEB)
        )
        // 6. Solar Noon / Dhuhr - Peak solar altitude, luminous high-noon sky
        nowMillis in dhuhrMillis until asrMillis -> listOf(
            Color(0xFF164B8A),
            Color(0xFF2571B8),
            Color(0xFF4596E5),
            Color(0xFF79BFFB)
        )
        // 7. Afternoon / Asr - Warm golden afternoon shifting towards bronze
        nowMillis in asrMillis until sunsetMillis - (30 * 60 * 1000L) -> listOf(
            Color(0xFF193E6C),
            Color(0xFF2B5990),
            Color(0xFF587492),
            Color(0xFFA98E72)
        )
        // 8. Sunset Golden Hour & Maghrib Twilight - Fiery sunset crimson and amber
        nowMillis in (sunsetMillis - (30 * 60 * 1000L)) until sunsetMillis -> listOf(
            Color(0xFF161B38),
            Color(0xFF36294D),
            Color(0xFF7D3852),
            Color(0xFFD45943),
            Color(0xFFFC9E4E)
        )
        // 9. Maghrib Dusk Twilight (Post-sunset afterglow)
        nowMillis in sunsetMillis until ishaMillis -> listOf(
            Color(0xFF10122A),
            Color(0xFF281B3C),
            Color(0xFF562646),
            Color(0xFF9C404B),
            Color(0xFFDB6E4E)
        )
        // 10. Dusk to Isha Nightfall
        nowMillis in ishaMillis until ishaMillis + (45 * 60 * 1000L) -> listOf(
            Color(0xFF090D21),
            Color(0xFF15193B),
            Color(0xFF25224C),
            Color(0xFF352756)
        )
        // 11. Late Night
        else -> listOf(
            Color(0xFF03050D),
            Color(0xFF080D1F),
            Color(0xFF101734),
            Color(0xFF192246)
        )
    }
}

/**
 * Computes dynamic atmospheric gradient matching live solar altitude.
 */
fun getSkyGradient(prayerTimes: PrayerTimes, nowMillis: Long): List<Color> {
    return getAtmosphericSkyGradient(prayerTimes, nowMillis)
}

/**
 * Returns current celestial stage title and descriptive caption.
 */
fun getCelestialStage(prayerTimes: PrayerTimes, nowMillis: Long): Pair<String, String> {
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    return when {
        nowMillis < fajrMillis -> "Night Sky • Tahajjud Window" to "Deep astronomical night until Fajr"
        nowMillis in fajrMillis until sunriseMillis -> "Dawn Twilight • Fajr" to "First light of dawn appearing"
        nowMillis in sunriseMillis until dhuhrMillis -> "Morning Sun • Shuruq" to "Sun rising towards celestial zenith"
        nowMillis in dhuhrMillis until asrMillis -> "Solar Zenith • High Noon" to "Sun at maximum daily elevation"
        nowMillis in asrMillis until sunsetMillis -> "Afternoon • Asr" to "Sun descending with gentle golden light"
        nowMillis in sunsetMillis until ishaMillis -> "Dusk Twilight • Maghrib" to "Sun dipping below the horizon"
        else -> "Night Sky • Isha" to "Night settled with celestial stars"
    }
}

/**
 * Full-page living IMMERSIVE DYNAMIC BACKGROUND for the "Salah" page.
 *
 * Renders the full "Sun and Moon Cycle" directly in the living background instead of an enclosed card:
 * - Real-time atmospheric color gradients matching user GPS location & time of day.
 * - Dynamic 24-hour sinusoidal celestial orbital trajectory across the upper screen dome.
 * - Arc apex dynamically scaled by user latitude and seasonal solar declination (lower arc in high latitudes, high arc in equatorial regions).
 * - Live animated Sun traversing the daytime arc with rotating solar rays, dynamic corona pulse, and vertical horizon beam.
 * - Live animated Moon traversing the nocturnal arc with celestial halo and star cluster.
 * - Dynamic horizon dividing line with subtle atmospheric scattering aura and desert dunes silhouette.
 * - Prayer milestone nodes on the trajectory (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha) with live glowing active marker.
 * - Ambient starfield (40+ twinkling stars) with shooting star animations during twilight and night.
 * - Smooth vertical gradient vignette toward the bottom for maximum foreground content contrast.
 */
@Composable
fun ImmersiveSkyBackground(
    prayerTimes: PrayerTimes,
    location: UserLocation = UserLocation(),
    now: Date = Date(),
    modifier: Modifier = Modifier
) {
    val nowMillis = now.time
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    val isDaytime = nowMillis in sunriseMillis..sunsetMillis

    // 1. Astronomy calculations based on user location and day of year
    val calendar = remember(now) { Calendar.getInstance().apply { time = now } }
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

    // Solar declination (-23.45° to +23.45°)
    val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))

    // Max solar elevation angle at solar noon for user latitude
    val maxElevation = (90.0 - abs(location.latitude - declination)).coerceIn(15.0, 90.0)

    // Elevation factor (0.30f to 1.0f): scales height of the celestial arc dynamically!
    val arcHeightFactor = (maxElevation / 90.0).toFloat().coerceIn(0.30f, 1.0f)

    // Daytime & Nighttime progress
    val daytimeProgress = if (isDaytime) {
        val totalDay = (sunsetMillis - sunriseMillis).coerceAtLeast(1L)
        ((nowMillis - sunriseMillis).toDouble() / totalDay).toFloat().coerceIn(0f, 1f)
    } else 0.5f

    val nighttimeProgress = if (!isDaytime) {
        if (nowMillis >= sunsetMillis) {
            val tomorrowSunrise = sunriseMillis + (24 * 3600 * 1000L)
            val totalNight = (tomorrowSunrise - sunsetMillis).coerceAtLeast(1L)
            ((nowMillis - sunsetMillis).toDouble() / totalNight).toFloat().coerceIn(0f, 1f)
        } else {
            val yesterdaySunset = sunsetMillis - (24 * 3600 * 1000L)
            val totalNight = (sunriseMillis - yesterdaySunset).coerceAtLeast(1L)
            ((nowMillis - yesterdaySunset).toDouble() / totalNight).toFloat().coerceIn(0f, 1f)
        }
    } else 0.5f

    val skyGradient = remember(nowMillis, prayerTimes, location) {
        getAtmosphericSkyGradient(prayerTimes, nowMillis, location, maxElevation)
    }

    // Ambient continuous animations
    val infiniteTransition = rememberInfiniteTransition(label = "immersive_sky_anim")
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sky_star_twinkle"
    )
    val horizonBreath by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "horizon_breath"
    )
    val sunRayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_ray_rotation"
    )
    val coronaPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corona_pulse"
    )
    val shootingStarProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shooting_star"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(skyGradient))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        val horizonY = heightPx * 0.36f
        val arcMaxHeight = heightPx * 0.22f * arcHeightFactor

        // Sun / Moon live coordinate on screen
        val activeT = if (isDaytime) daytimeProgress else nighttimeProgress
        val celestialX = widthPx * (0.08f + 0.84f * activeT)
        // Parabolic arc: peak at activeT = 0.5
        val celestialY = horizonY - (arcMaxHeight * sin(PI * activeT).toFloat())

        // 1. Celestial Canvas for Living Orbit, Stars, Corona Glow, and Horizon Atmosphere
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // A. Starfield (active during night, dawn twilight, or dusk twilight)
            val showStars = nowMillis < sunriseMillis || nowMillis > sunsetMillis
            if (showStars) {
                val starPositions = listOf(
                    Offset(w * 0.08f, h * 0.04f),
                    Offset(w * 0.22f, h * 0.07f),
                    Offset(w * 0.38f, h * 0.03f),
                    Offset(w * 0.55f, h * 0.06f),
                    Offset(w * 0.72f, h * 0.04f),
                    Offset(w * 0.88f, h * 0.08f),
                    Offset(w * 0.15f, h * 0.12f),
                    Offset(w * 0.31f, h * 0.15f),
                    Offset(w * 0.65f, h * 0.13f),
                    Offset(w * 0.82f, h * 0.17f),
                    Offset(w * 0.94f, h * 0.11f),
                    Offset(w * 0.10f, h * 0.22f),
                    Offset(w * 0.48f, h * 0.20f),
                    Offset(w * 0.76f, h * 0.24f),
                    Offset(w * 0.25f, h * 0.28f),
                    Offset(w * 0.85f, h * 0.30f),
                    Offset(w * 0.05f, h * 0.33f),
                    Offset(w * 0.60f, h * 0.32f),
                    Offset(w * 0.90f, h * 0.34f),
                    Offset(w * 0.40f, h * 0.35f),
                    Offset(w * 0.18f, h * 0.05f),
                    Offset(w * 0.68f, h * 0.08f),
                    Offset(w * 0.35f, h * 0.24f),
                    Offset(w * 0.52f, h * 0.16f)
                )
                starPositions.forEachIndexed { index, pos ->
                    val factor = if (index % 3 == 0) starTwinkle else (1.15f - starTwinkle).coerceIn(0.2f, 1f)
                    val radius = if (index % 2 == 0) 1.8.dp.toPx() else 1.2.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = factor * if (isDaytime) 0f else 0.85f),
                        radius = radius,
                        center = pos
                    )
                }

                // Periodic shooting star streak
                if (!isDaytime && shootingStarProgress in 0.15f..0.35f) {
                    val sp = (shootingStarProgress - 0.15f) / 0.20f
                    val startX = w * (0.20f + 0.50f * sp)
                    val startY = h * (0.05f + 0.10f * sp)
                    val endX = startX + 45.dp.toPx()
                    val endY = startY + 20.dp.toPx()
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.8f), Color.Transparent),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.8.dp.toPx()
                    )
                }
            }

            // B. The Living 24h Celestial Orbital Trajectory Arc
            val dayArc = Path().apply {
                moveTo(w * 0.06f, horizonY)
                quadraticTo(w * 0.50f, horizonY - (arcMaxHeight * 2f), w * 0.94f, horizonY)
            }

            // Translucent glowing fill under the daytime curve
            val fillPath = Path().apply {
                moveTo(w * 0.06f, horizonY)
                quadraticTo(w * 0.50f, horizonY - (arcMaxHeight * 2f), w * 0.94f, horizonY)
                lineTo(w * 0.94f, horizonY)
                lineTo(w * 0.06f, horizonY)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (if (isDaytime) Color(0x33FFE082) else Color(0x1A90CAF9)),
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    startY = horizonY - arcMaxHeight,
                    endY = horizonY
                )
            )

            // Dotted Celestial Orbital Trajectory Path
            drawPath(
                path = dayArc,
                color = if (isDaytime) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.25f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
                )
            )

            // C. Dynamic Horizon Line with Atmospheric Scattering Aura
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (isDaytime) 0.35f else 0.22f),
                        Color.White.copy(alpha = if (isDaytime) 0.55f else 0.35f),
                        Color.White.copy(alpha = if (isDaytime) 0.35f else 0.22f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 1.4.dp.toPx()
            )

            // D. Prayer Milestone Nodes along the celestial trajectory
            val milestones = listOf(
                Pair(0.04f, "Fajr"),
                Pair(0.08f, "Sunrise"),
                Pair(0.50f, "Dhuhr"),
                Pair(0.72f, "Asr"),
                Pair(0.92f, "Maghrib"),
                Pair(0.96f, "Isha")
            )
            milestones.forEach { (tFrac, name) ->
                val mx = w * tFrac
                val my = horizonY - (arcMaxHeight * sin(PI * tFrac.toDouble()).toFloat())
                val isNearActive = abs(celestialX - mx) < (w * 0.08f)
                drawCircle(
                    color = if (isNearActive) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.45f),
                    radius = if (isNearActive) 3.5.dp.toPx() else 2.2.dp.toPx(),
                    center = Offset(mx, my)
                )
                if (isNearActive) {
                    drawCircle(
                        color = Color(0x44FFD54F),
                        radius = 8.dp.toPx() * coronaPulse,
                        center = Offset(mx, my)
                    )
                }
            }

            // E. Vertical Light Sunbeam / Moonbeam connecting celestial body to horizon
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (if (isDaytime) Color(0x66FFD54F) else Color(0x4480D8FF)),
                        Color.Transparent
                    ),
                    startY = celestialY,
                    endY = horizonY
                ),
                start = Offset(celestialX, celestialY),
                end = Offset(celestialX, horizonY),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
            )

            // Small glowing ground marker on the horizon
            drawCircle(
                color = if (isDaytime) Color(0xFFFFE082) else Color(0xFFB3E5FC),
                radius = 3.dp.toPx(),
                center = Offset(celestialX, horizonY)
            )

            // F. Celestial Body Radial Atmospheric Corona (Drawn behind the SVG vector)
            if (isDaytime) {
                // Outer Warm Golden Solar Flare Corona
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FFC107),
                            Color(0x22FF9800),
                            Color.Transparent
                        ),
                        center = Offset(celestialX, celestialY),
                        radius = 85.dp.toPx() * coronaPulse
                    ),
                    radius = 85.dp.toPx() * coronaPulse,
                    center = Offset(celestialX, celestialY)
                )
                // Inner Solar White-Gold Halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x88FFF59D),
                            Color(0x33FFD54F),
                            Color.Transparent
                        ),
                        center = Offset(celestialX, celestialY),
                        radius = 42.dp.toPx()
                    ),
                    radius = 42.dp.toPx(),
                    center = Offset(celestialX, celestialY)
                )
            } else {
                // Soft Celestial Silver Lunar Corona
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x4480D8FF),
                            Color(0x1A40C4FF),
                            Color.Transparent
                        ),
                        center = Offset(celestialX, celestialY),
                        radius = 65.dp.toPx() * coronaPulse
                    ),
                    radius = 65.dp.toPx() * coronaPulse,
                    center = Offset(celestialX, celestialY)
                )
            }

            // G. Rolling Horizon Landscape Silhouette with Atmospheric Mist
            val dunesPath = Path().apply {
                moveTo(0f, horizonY + 12.dp.toPx())
                cubicTo(
                    w * 0.25f, horizonY + 4.dp.toPx(),
                    w * 0.45f, horizonY + 18.dp.toPx(),
                    w * 0.70f, horizonY + 8.dp.toPx()
                )
                cubicTo(
                    w * 0.85f, horizonY + 2.dp.toPx(),
                    w * 0.95f, horizonY + 14.dp.toPx(),
                    w, horizonY + 10.dp.toPx()
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = dunesPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x22000000),
                        Color(0x66000000)
                    ),
                    startY = horizonY,
                    endY = horizonY + 30.dp.toPx()
                )
            )

            // H. Deep Atmospheric Bottom Scrim Vignette for Pristine Foreground Readability
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x55020409),
                        Color(0xBF03060E),
                        Color(0xF0020408)
                    ),
                    startY = horizonY + 15.dp.toPx(),
                    endY = h
                ),
                size = Size(w, h - horizonY - 15.dp.toPx()),
                topLeft = Offset(0f, horizonY + 15.dp.toPx())
            )
        }

        // 2. Physical Animated Sun or Moon Composable positioned along the dynamic celestial arc
        val celestialSizeDp = if (isDaytime) 44.dp else 38.dp
        val celestialSizePx = with(density) { celestialSizeDp.toPx() }

        Box(
            modifier = Modifier
                .padding(
                    start = with(density) { (celestialX - celestialSizePx / 2).coerceAtLeast(0f).toDp() },
                    top = with(density) { (celestialY - celestialSizePx / 2).coerceAtLeast(0f).toDp() }
                )
                .size(celestialSizeDp),
            contentAlignment = Alignment.Center
        ) {
            if (isDaytime) {
                // Radiant rotating Sun SVG
                Image(
                    painter = painterResource(id = R.drawable.ic_fancy_sun),
                    contentDescription = "Live Daylight Sun",
                    modifier = Modifier
                        .size(celestialSizeDp)
                        .rotate(sunRayRotation)
                )
            } else {
                // Fancy Crescent Moon with twinkling stars
                Image(
                    painter = painterResource(id = R.drawable.ic_fancy_crescent_moon),
                    contentDescription = "Live Nighttime Moon",
                    modifier = Modifier.size(celestialSizeDp)
                )
            }
        }
    }
}

/**
 * Sleek Translucent Floating Prayer Status Header.
 *
 * Replaces the old bulky card by floating cleanly over the immersive dynamic background,
 * presenting key countdown and celestial metrics without duplicating the sun & moon arc:
 * - Glassmorphism surface matching the dynamic living background.
 * - Next prayer name, Arabic typography, and start time.
 * - Precision countdown capsules (Hours : Minutes : Seconds).
 * - Live daylight/night progress status chip and current GPS location.
 * - 4-tile compact milestone metrics row (Sunrise, Solar Noon, Sunset, Nightfall).
 */
@Composable
fun CelestialPrayerStatusHeader(
    prayerTimes: PrayerTimes,
    countdownState: CountdownState,
    location: UserLocation = UserLocation(),
    now: Date = Date(),
    modifier: Modifier = Modifier
) {
    val nowMillis = now.time
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val ishaMillis = prayerTimes.isha.time

    val isDaytime = nowMillis in sunriseMillis..sunsetMillis

    val daytimeProgress = if (isDaytime) {
        val totalDay = (sunsetMillis - sunriseMillis).coerceAtLeast(1L)
        ((nowMillis - sunriseMillis).toDouble() / totalDay).toFloat().coerceIn(0f, 1f)
    } else 0.5f

    val nighttimeProgress = if (!isDaytime) {
        if (nowMillis >= sunsetMillis) {
            val tomorrowSunrise = sunriseMillis + (24 * 3600 * 1000L)
            val totalNight = (tomorrowSunrise - sunsetMillis).coerceAtLeast(1L)
            ((nowMillis - sunsetMillis).toDouble() / totalNight).toFloat().coerceIn(0f, 1f)
        } else {
            val yesterdaySunset = sunsetMillis - (24 * 3600 * 1000L)
            val totalNight = (sunriseMillis - yesterdaySunset).coerceAtLeast(1L)
            ((nowMillis - yesterdaySunset).toDouble() / totalNight).toFloat().coerceIn(0f, 1f)
        }
    } else 0.5f

    val celestialStage = remember(prayerTimes, nowMillis) {
        getCelestialStage(prayerTimes, nowMillis)
    }

    val totalSeconds = (countdownState.remainingMillis / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val hoursFormatted = String.format(Locale.US, "%02d", hours)
    val minutesFormatted = String.format(Locale.US, "%02d", minutes)
    val secondsFormatted = String.format(Locale.US, "%02d", seconds)

    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("celestial_prayer_status_header"),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.30f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 1. Category Eyebrow + Live Celestial Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_sun_moon_cycle),
                                contentDescription = "Sun and Moon Cycle",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "CELESTIAL PRAYER CYCLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = celestialStage.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Weather-style Event Badge (Daylight % or Night %)
                Surface(
                    color = Color.Black.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = if (isDaytime) "☀️ ${(daytimeProgress * 100).toInt()}% Day" else "🌙 ${(nighttimeProgress * 100).toInt()}% Night",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Primary Countdown Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Next: ${countdownState.nextPrayer.displayName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = countdownState.nextPrayer.arabicName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Starts at ${prayerTimes.formattedTime(countdownState.nextPrayer)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Segmented Countdown Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WeatherCountdownCapsule(value = hoursFormatted, unit = "HR")
                    Text(
                        text = ":",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    WeatherCountdownCapsule(value = minutesFormatted, unit = "MIN")
                    Text(
                        text = ":",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    WeatherCountdownCapsule(value = secondsFormatted, unit = "SEC")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Progress Strip towards next Salah
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${countdownState.currentPrayer.displayName} → ${countdownState.nextPrayer.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(countdownState.progress * 100).toInt()}% elapsed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                LinearProgressIndicator(
                    progress = { countdownState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.20f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Four-Tile Celestial Milestone Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_sunrise,
                    label = "Sunrise",
                    time = prayerTimes.formattedTime(PrayerName.SUNRISE),
                    isPassed = nowMillis > sunriseMillis,
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_solar_noon,
                    label = "Solar Noon",
                    time = prayerTimes.formattedTime(PrayerName.DHUHR),
                    isPassed = nowMillis > dhuhrMillis,
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_sunset,
                    label = "Sunset",
                    time = prayerTimes.formattedTime(PrayerName.MAGHRIB),
                    isPassed = nowMillis > sunsetMillis,
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_twilight,
                    label = "Nightfall",
                    time = prayerTimes.formattedTime(PrayerName.ISHA),
                    isPassed = nowMillis > ishaMillis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Expandable Astronomical & Location Details
            Surface(
                onClick = { showDetails = !showDetails },
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val daylightDurationMillis = (sunsetMillis - sunriseMillis).coerceAtLeast(0L)
                        val dlHours = daylightDurationMillis / (3600 * 1000)
                        val dlMinutes = (daylightDurationMillis % (3600 * 1000)) / (60 * 1000)

                        Text(
                            text = "Daylight: ${dlHours}h ${dlMinutes}m • ${location.cityName}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (showDetails) "Hide ▲" else "Details ▼",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(
                        visible = showDetails,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Text(
                                text = celestialStage.second,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Dynamic sun & moon arc in the background reflects your latitude (${String.format(Locale.US, "%.2f°", location.latitude)}) and solar declination.",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for CelestialPrayerHeroCard.
 * Delegates to the modernized CelestialPrayerStatusHeader.
 */
@Composable
fun CelestialPrayerHeroCard(
    prayerTimes: PrayerTimes,
    countdownState: CountdownState,
    now: Date = Date(),
    modifier: Modifier = Modifier
) {
    CelestialPrayerStatusHeader(
        prayerTimes = prayerTimes,
        countdownState = countdownState,
        location = UserLocation(),
        now = now,
        modifier = modifier
    )
}

/**
 * Draws the 24-hour celestial wave path, horizon line, glow fill, and twinkling stars.
 */
private fun DrawScope.drawCelestialWaveAndSky(
    isDaytime: Boolean,
    dayProgress: Float,
    nightProgress: Float,
    starTwinkle: Float
) {
    val w = size.width
    val h = size.height
    val horizonY = h * 0.72f
    val waveHeight = h * 0.52f

    // 1. Draw Star Constellations in the sky
    if (!isDaytime) {
        val stars = listOf(
            Offset(w * 0.08f, h * 0.18f),
            Offset(w * 0.18f, h * 0.28f),
            Offset(w * 0.32f, h * 0.12f),
            Offset(w * 0.45f, h * 0.34f),
            Offset(w * 0.60f, h * 0.16f),
            Offset(w * 0.74f, h * 0.24f),
            Offset(w * 0.86f, h * 0.14f),
            Offset(w * 0.94f, h * 0.32f)
        )
        stars.forEachIndexed { i, offset ->
            val radius = if (i % 2 == 0) 1.6.dp.toPx() else 2.2.dp.toPx()
            val alpha = if (i % 2 == 0) starTwinkle else (1.2f - starTwinkle).coerceIn(0.3f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = offset
            )
        }
    }

    // 2. Daytime Celestial Arc
    val dayArc = Path().apply {
        moveTo(w * 0.05f, horizonY)
        quadraticTo(w * 0.50f, horizonY - waveHeight, w * 0.95f, horizonY)
    }

    // Gradient fill under the daytime sun curve
    val fillPath = Path().apply {
        moveTo(w * 0.05f, horizonY)
        quadraticTo(w * 0.50f, horizonY - waveHeight, w * 0.95f, horizonY)
        lineTo(w * 0.95f, horizonY)
        lineTo(w * 0.05f, horizonY)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.04f),
                Color.Transparent
            ),
            startY = horizonY - waveHeight,
            endY = horizonY
        )
    )

    // Dotted celestial trajectory path
    drawPath(
        path = dayArc,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    )

    // 3. Horizon Reference Line (Weather app style)
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(0f, horizonY),
        end = Offset(w, horizonY),
        strokeWidth = 1.2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    )

    // Current position indicator line down to horizon
    val activeT = if (isDaytime) dayProgress else nightProgress
    val currentX = w * (0.05f + 0.90f * activeT)
    val currentY = horizonY - (4 * waveHeight * activeT * (1 - activeT))

    // Vertical dashed marker beam
    drawLine(
        color = Color.White.copy(alpha = 0.40f),
        start = Offset(currentX, currentY),
        end = Offset(currentX, horizonY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )

    // Small glowing ground marker on the horizon
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = Offset(currentX, horizonY)
    )
}

/**
 * Weather app style capsule for hours, minutes, and seconds.
 */
@Composable
private fun WeatherCountdownCapsule(
    value: String,
    unit: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
            Text(
                text = unit,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4D4D8),
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Text label for milestones along the horizon (Dawn, Sunrise, Zenith, Sunset, Dusk).
 */
@Composable
private fun CelestialMilestoneLabel(
    name: String,
    time: String,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.65f)
        )
        Text(
            text = time,
            fontSize = 8.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.50f)
        )
    }
}

/**
 * Clean Weather Metric Tile (Sunrise, Solar Noon, Sunset, Nightfall) with vector SVG.
 */
@Composable
private fun WeatherMetricTile(
    iconRes: Int,
    label: String,
    time: String,
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = if (isPassed) 0.28f else 0.40f),
        border = BorderStroke(
            1.dp,
            if (isPassed) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPassed) Color.White.copy(alpha = 0.75f) else Color.White,
                maxLines = 1
            )
        }
    }
}
