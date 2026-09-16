package com.example.features.widgets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import java.util.Calendar
import java.util.Date
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-quality, minimalist SVG/Vector-based celestial atmospheric state.
 * Encapsulates the 11 continuous celestial phases from Deep Tahajjud Night to Deep Night.
 */
data class AtmosphericState(
    val name: String,
    val skyColors: List<Color>,
    val horizonGlowColor: Color,
    val celestialGlowColor: Color,
    val sunRayColor: Color,
    val trajectoryColor: Color,
    val starOpacity: Float,
    val skyLuminosity: Float,
    val duneColor1: Color,
    val duneColor2: Color,
    val scrimColor: Color
)

/**
 * Linearly interpolates between two colors.
 */
private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}

/**
 * Linearly interpolates between two color lists (for multi-stop gradients).
 */
private fun lerpColorList(list1: List<Color>, list2: List<Color>, fraction: Float): List<Color> {
    val count = maxOf(list1.size, list2.size)
    return (0 until count).map { i ->
        val c1 = list1.getOrElse(i) { list1.last() }
        val c2 = list2.getOrElse(i) { list2.last() }
        lerpColor(c1, c2, fraction)
    }
}

/**
 * Linearly interpolates between two floats.
 */
private fun lerpFloat(v1: Float, v2: Float, fraction: Float): Float {
    return v1 + (v2 - v1) * fraction.coerceIn(0f, 1f)
}

/**
 * Evaluates the 11-Phase Atmospheric System at the specified time.
 * Continuously interpolates between the 11 states:
 * 1. Deep Tahajjud Night
 * 2. Pre-Dawn
 * 3. Fajr Twilight
 * 4. Rose-Coral Dawn
 * 5. Sunrise
 * 6. Morning
 * 7. High-Noon Azure
 * 8. Afternoon
 * 9. Maghrib Sunset
 * 10. Star-Lit Dusk
 * 11. Deep Night
 */
fun getInterpolatedAtmosphericState(
    prayerTimes: PrayerTimes,
    nowMillis: Long
): AtmosphericState {
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    // 11 distinct celestial anchors across the 24-hour cycle
    val anchors = listOf(
        // 0. Deep Tahajjud Night (~1.5h before Fajr)
        (fajrMillis - 90 * 60 * 1000L) to AtmosphericState(
            name = "Deep Tahajjud Night",
            skyColors = listOf(Color(0xFF010308), Color(0xFF040614), Color(0xFF080D22), Color(0xFF101732)),
            horizonGlowColor = Color(0x25141E48),
            celestialGlowColor = Color(0x3582B1FF),
            sunRayColor = Color.Transparent,
            trajectoryColor = Color(0x3580D8FF),
            starOpacity = 0.95f,
            skyLuminosity = 0.10f,
            duneColor1 = Color(0x28040612),
            duneColor2 = Color(0x4003050E),
            scrimColor = Color(0xFF02030A)
        ),
        // 1. Pre-Dawn (Astronomical Twilight, ~40m before Fajr)
        (fajrMillis - 40 * 60 * 1000L) to AtmosphericState(
            name = "Pre-Dawn",
            skyColors = listOf(Color(0xFF030510), Color(0xFF080D20), Color(0xFF121632), Color(0xFF1C1D40)),
            horizonGlowColor = Color(0x352B2456),
            celestialGlowColor = Color(0x40A0C4F8),
            sunRayColor = Color.Transparent,
            trajectoryColor = Color(0x4090CAF9),
            starOpacity = 0.85f,
            skyLuminosity = 0.15f,
            duneColor1 = Color(0x28060918),
            duneColor2 = Color(0x45050714),
            scrimColor = Color(0xFF040612)
        ),
        // 2. Fajr Twilight (True Dawn start)
        fajrMillis to AtmosphericState(
            name = "Fajr Twilight",
            skyColors = listOf(Color(0xFF06091C), Color(0xFF14132E), Color(0xFF2E1C3E), Color(0xFF522850), Color(0xFF76365C)),
            horizonGlowColor = Color(0x60964E74),
            celestialGlowColor = Color(0x50EAA8BC),
            sunRayColor = Color(0x40FFA884),
            trajectoryColor = Color(0x48E0B0D0),
            starOpacity = 0.60f,
            skyLuminosity = 0.30f,
            duneColor1 = Color(0x321C1226),
            duneColor2 = Color(0x48140B1E),
            scrimColor = Color(0xFF070A1A)
        ),
        // 3. Rose-Coral Dawn (Midway between Fajr and Sunrise)
        (fajrMillis + ((sunriseMillis - fajrMillis) * 0.65).toLong()) to AtmosphericState(
            name = "Rose-Coral Dawn",
            skyColors = listOf(Color(0xFF091024), Color(0xFF1E1C3E), Color(0xFF472648), Color(0xFF82364E), Color(0xFFCF6258)),
            horizonGlowColor = Color(0x80F2846C),
            celestialGlowColor = Color(0x65FFB284),
            sunRayColor = Color(0x65FFBA80),
            trajectoryColor = Color(0x55FFA088),
            starOpacity = 0.20f,
            skyLuminosity = 0.50f,
            duneColor1 = Color(0x3826162C),
            duneColor2 = Color(0x551E1022),
            scrimColor = Color(0xFF0A0F20)
        ),
        // 4. Sunrise (Shuruq)
        sunriseMillis to AtmosphericState(
            name = "Sunrise",
            skyColors = listOf(Color(0xFF0E1C34), Color(0xFF1C3258), Color(0xFF484664), Color(0xFFA05648), Color(0xFFF29046)),
            horizonGlowColor = Color(0xAAFFAA48),
            celestialGlowColor = Color(0x88FFC72C),
            sunRayColor = Color(0x95FFD860),
            trajectoryColor = Color(0x65FFE082),
            starOpacity = 0.0f,
            skyLuminosity = 0.70f,
            duneColor1 = Color(0x383A2620),
            duneColor2 = Color(0x582C1A14),
            scrimColor = Color(0xFF0D1728)
        ),
        // 5. Morning (Duha)
        (sunriseMillis + ((dhuhrMillis - sunriseMillis) * 0.45).toLong()) to AtmosphericState(
            name = "Morning",
            skyColors = listOf(Color(0xFF0F2E58), Color(0xFF1B4880), Color(0xFF3068A8), Color(0xFF5E92D2)),
            horizonGlowColor = Color(0x60BCE0FC),
            celestialGlowColor = Color(0x70FFE48C),
            sunRayColor = Color(0x85FFFCC0),
            trajectoryColor = Color(0x55E3F2FD),
            starOpacity = 0.0f,
            skyLuminosity = 0.85f,
            duneColor1 = Color(0x2C1A324C),
            duneColor2 = Color(0x48122436),
            scrimColor = Color(0xFF0C2038)
        ),
        // 6. High-Noon Azure (Dhuhr)
        dhuhrMillis to AtmosphericState(
            name = "High-Noon Azure",
            skyColors = listOf(Color(0xFF0E3668), Color(0xFF1A5490), Color(0xFF3074BD), Color(0xFF62A2E2)),
            horizonGlowColor = Color(0x70D0EEFF),
            celestialGlowColor = Color(0x80FFF8B0),
            sunRayColor = Color(0x95FFFFE0),
            trajectoryColor = Color(0x65FFFFFF),
            starOpacity = 0.0f,
            skyLuminosity = 1.0f,
            duneColor1 = Color(0x301C3A56),
            duneColor2 = Color(0x4E142A40),
            scrimColor = Color(0xFF0B223E)
        ),
        // 7. Afternoon (Asr)
        asrMillis to AtmosphericState(
            name = "Afternoon",
            skyColors = listOf(Color(0xFF123054), Color(0xFF20446C), Color(0xFF3F5872), Color(0xFF7A6C62), Color(0xFFA88C6C)),
            horizonGlowColor = Color(0x75E6C690),
            celestialGlowColor = Color(0x70FFCE36),
            sunRayColor = Color(0x85FFE28C),
            trajectoryColor = Color(0x55FFE8B8),
            starOpacity = 0.0f,
            skyLuminosity = 0.80f,
            duneColor1 = Color(0x35302A24),
            duneColor2 = Color(0x52241E18),
            scrimColor = Color(0xFF101E32)
        ),
        // 8. Maghrib Sunset
        sunsetMillis to AtmosphericState(
            name = "Maghrib Sunset",
            skyColors = listOf(Color(0xFF0E1228), Color(0xFF221836), Color(0xFF52203C), Color(0x9E3A30), Color(0xFFEA8034)),
            horizonGlowColor = Color(0xAAFF7440),
            celestialGlowColor = Color(0x85FFBC50),
            sunRayColor = Color(0x90FFAC30),
            trajectoryColor = Color(0x65FFAB91),
            starOpacity = 0.0f,
            skyLuminosity = 0.60f,
            duneColor1 = Color(0x3A301A20),
            duneColor2 = Color(0x58241216),
            scrimColor = Color(0xFF0C1020)
        ),
        // 9. Star-Lit Dusk (Post-Sunset Afterglow)
        (sunsetMillis + ((ishaMillis - sunsetMillis) * 0.45).toLong()) to AtmosphericState(
            name = "Star-Lit Dusk",
            skyColors = listOf(Color(0xFF070A1E), Color(0xFF13102A), Color(0xFF261530), Color(0xFF461D32), Color(0xFF722E3A)),
            horizonGlowColor = Color(0x4578324E),
            celestialGlowColor = Color(0x458BB0E0),
            sunRayColor = Color(0x30FF9470),
            trajectoryColor = Color(0x40CE93D8),
            starOpacity = 0.55f,
            skyLuminosity = 0.30f,
            duneColor1 = Color(0x2A161020),
            duneColor2 = Color(0x40100816),
            scrimColor = Color(0xFF060918)
        ),
        // 10. Deep Night (Isha settling into midnight)
        (ishaMillis + 45 * 60 * 1000L) to AtmosphericState(
            name = "Deep Night",
            skyColors = listOf(Color(0xFF010308), Color(0xFF050716), Color(0xFF0A0F24), Color(0xFF101530)),
            horizonGlowColor = Color(0x20141C40),
            celestialGlowColor = Color(0x3598C8F8),
            sunRayColor = Color.Transparent,
            trajectoryColor = Color(0x3590CAF9),
            starOpacity = 0.95f,
            skyLuminosity = 0.12f,
            duneColor1 = Color(0x24040612),
            duneColor2 = Color(0x3A02040E),
            scrimColor = Color(0xFF02040A)
        )
    )

    val firstAnchorTime = anchors.first().first
    val lastAnchorTime = anchors.last().first

    // Handle seamless cyclic interpolation across day boundaries
    val (stateA, stateB, fraction) = when {
        nowMillis <= firstAnchorTime -> {
            // Wrapping from yesterday's Deep Night into today's Deep Tahajjud
            val prevDeepNightTime = lastAnchorTime - (24 * 3600 * 1000L)
            val totalSpan = (firstAnchorTime - prevDeepNightTime).coerceAtLeast(1L)
            val f = ((nowMillis - prevDeepNightTime).toDouble() / totalSpan).toFloat()
            Triple(anchors.last().second, anchors.first().second, f)
        }
        nowMillis >= lastAnchorTime -> {
            // Wrapping from today's Deep Night into tomorrow's Deep Tahajjud
            val nextTahajjudTime = firstAnchorTime + (24 * 3600 * 1000L)
            val totalSpan = (nextTahajjudTime - lastAnchorTime).coerceAtLeast(1L)
            val f = ((nowMillis - lastAnchorTime).toDouble() / totalSpan).toFloat()
            Triple(anchors.last().second, anchors.first().second, f)
        }
        else -> {
            // Between two anchors in the current cycle
            var foundIndex = 0
            for (i in 0 until anchors.size - 1) {
                if (nowMillis >= anchors[i].first && nowMillis < anchors[i + 1].first) {
                    foundIndex = i
                    break
                }
            }
            val tStart = anchors[foundIndex].first
            val tEnd = anchors[foundIndex + 1].first
            val totalSpan = (tEnd - tStart).coerceAtLeast(1L)
            val f = ((nowMillis - tStart).toDouble() / totalSpan).toFloat()
            Triple(anchors[foundIndex].second, anchors[foundIndex + 1].second, f)
        }
    }

    // Blend all atmospheric properties smoothly
    return AtmosphericState(
        name = if (fraction < 0.5f) stateA.name else stateB.name,
        skyColors = lerpColorList(stateA.skyColors, stateB.skyColors, fraction),
        horizonGlowColor = lerpColor(stateA.horizonGlowColor, stateB.horizonGlowColor, fraction),
        celestialGlowColor = lerpColor(stateA.celestialGlowColor, stateB.celestialGlowColor, fraction),
        sunRayColor = lerpColor(stateA.sunRayColor, stateB.sunRayColor, fraction),
        trajectoryColor = lerpColor(stateA.trajectoryColor, stateB.trajectoryColor, fraction),
        starOpacity = lerpFloat(stateA.starOpacity, stateB.starOpacity, fraction),
        skyLuminosity = lerpFloat(stateA.skyLuminosity, stateB.skyLuminosity, fraction),
        duneColor1 = lerpColor(stateA.duneColor1, stateB.duneColor1, fraction),
        duneColor2 = lerpColor(stateA.duneColor2, stateB.duneColor2, fraction),
        scrimColor = lerpColor(stateA.scrimColor, stateB.scrimColor, fraction)
    )
}

/**
 * Public backward-compatible helper for atmospheric sky gradient.
 */
fun getAtmosphericSkyGradient(
    prayerTimes: PrayerTimes,
    nowMillis: Long,
    location: UserLocation = UserLocation(),
    maxElevation: Double = 60.0
): List<Color> {
    return getInterpolatedAtmosphericState(prayerTimes, nowMillis).skyColors
}

fun getSkyGradient(prayerTimes: PrayerTimes, nowMillis: Long): List<Color> {
    return getInterpolatedAtmosphericState(prayerTimes, nowMillis).skyColors
}

fun getCelestialStage(prayerTimes: PrayerTimes, nowMillis: Long): Pair<String, String> {
    val state = getInterpolatedAtmosphericState(prayerTimes, nowMillis)
    val desc = when (state.name) {
        "Deep Tahajjud Night" -> "Deep cosmic serenity and nocturnal prayer window"
        "Pre-Dawn" -> "Astronomical twilight lifting darkness before dawn"
        "Fajr Twilight" -> "First true light of dawn appearing on the eastern horizon"
        "Rose-Coral Dawn" -> "Gentle morning hues radiating prior to sunrise"
        "Sunrise" -> "Sun rising across the celestial horizon"
        "Morning" -> "Crisp azure sky during the morning Duha hours"
        "High-Noon Azure" -> "Sun reaching maximum daily elevation at solar zenith"
        "Afternoon" -> "Golden afternoon sunlight descending towards Asr"
        "Maghrib Sunset" -> "Vibrant sunset twilight settling over the horizon"
        "Star-Lit Dusk" -> "Celestial evening afterglow with stars emerging"
        else -> "Settled night under the celestial dome"
    }
    return state.name to desc
}

/**
 * Immersive Celestial Sky Background
 *
 * A boundless, atmospheric celestial canvas:
 * 1. Clean, boundless sky with no orbit/trajectory lines for complete natural immersion.
 * 2. Prominent, majestic celestial body (Sun or Crescent Moon) with rich multi-stage corona and atmospheric glare.
 * 3. 11-Phase continuous atmospheric engine blending color gradients, celestial glows, and horizon luminescence.
 * 4. Atmospheric horizon bloom simulating Rayleigh and Mie light scattering.
 * 5. Minimalist layered desert dunes anchoring the distant horizon.
 * 6. Expansive starfield with subtle twinkling, stardust haze, and occasional shooting star streak.
 * 7. Soft atmospheric scrim ensuring foreground cards and navigation remain clear and readable.
 */
@Composable
fun ImmersiveSkyBackground(
    prayerTimes: PrayerTimes,
    location: UserLocation = UserLocation(),
    now: Date = Date(),
    countdownState: CountdownState? = null,
    modifier: Modifier = Modifier
) {
    val nowMillis = now.time
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val isDaytime = nowMillis in sunriseMillis..sunsetMillis

    // Astronomical parameters based on user GPS latitude and day of year
    val calendar = remember(now) { Calendar.getInstance().apply { time = now } }
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

    // Solar declination (-23.45° to +23.45°)
    val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))
    // Maximum solar elevation at solar noon for user latitude
    val maxElevation = (90.0 - abs(location.latitude - declination)).coerceIn(15.0, 90.0)
    val arcHeightFactor = (maxElevation / 90.0).toFloat().coerceIn(0.30f, 1.0f)

    // Daytime & Nighttime trajectory progress t in [0, 1]
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

    // 11-Phase continuous atmospheric state evaluation
    val atmosphericState = remember(nowMillis, prayerTimes) {
        getInterpolatedAtmosphericState(prayerTimes, nowMillis)
    }

    // Restrained, subtle ambient animations
    val infiniteTransition = rememberInfiniteTransition(label = "celestial_background_anim")

    // Star twinkle oscillation
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    // Corona & Celestial halo gentle pulse
    val coronaPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corona_pulse"
    )

    // Stately Sun ray rotation (50 seconds per full 360° rotation)
    val sunRayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_ray_rotation"
    )

    // Crescent Moon gentle vertical parallax float (+/- 3dp over 6s)
    val moonFloat by infiniteTransition.animateFloat(
        initialValue = -3.0f,
        targetValue = 3.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moon_float"
    )

    // Shooting star streak
    val shootingStarProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shooting_star"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(atmosphericState.skyColors))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // Horizon anchoring line located in the upper third
        val horizonY = heightPx * 0.36f
        val arcMaxHeight = heightPx * 0.20f * arcHeightFactor

        // Celestial body position based on time of day (no visible orbit lines)
        val activeT = if (isDaytime) daytimeProgress else nighttimeProgress
        val celestialX = widthPx * (0.12f + 0.76f * activeT)
        // Parabolic arc for natural celestial positioning
        val baseCelestialY = horizonY - (arcMaxHeight * sin(PI * activeT).toFloat())
        val celestialY = if (isDaytime) baseCelestialY else baseCelestialY + with(density) { moonFloat.dp.toPx() }

        // Primary Immersive Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val celestialCenter = Offset(celestialX, celestialY)

            // -------------------------------------------------------------
            // 1. Immersive Atmospheric Light Wash & Celestial Glare
            // -------------------------------------------------------------
            // Broad ambient light bloom surrounding the Sun/Moon bathing the upper atmosphere
            val atmosphericGlareRadius = if (isDaytime) 220.dp.toPx() * coronaPulse else 140.dp.toPx() * coronaPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        atmosphericState.celestialGlowColor.copy(alpha = if (isDaytime) 0.35f else 0.20f),
                        atmosphericState.celestialGlowColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = celestialCenter,
                    radius = atmosphericGlareRadius
                ),
                radius = atmosphericGlareRadius,
                center = celestialCenter
            )

            // -------------------------------------------------------------
            // 2. Starfield & Cosmic Serenity (Twinkling stars, nebula dust, shooting star)
            // -------------------------------------------------------------
            val starOpacity = atmosphericState.starOpacity
            if (starOpacity > 0.05f) {
                // Subtle stardust / cosmic haze across the upper celestial dome
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1880D8FF).copy(alpha = 0.14f * starOpacity),
                            Color(0x0C7986CB).copy(alpha = 0.08f * starOpacity),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.45f, h * 0.14f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.45f, h * 0.14f)
                )

                // 28 precisely calibrated stars with varied magnitudes
                val stars = listOf(
                    Triple(0.06f, 0.04f, 1.8f),
                    Triple(0.18f, 0.06f, 1.2f),
                    Triple(0.32f, 0.03f, 2.0f),
                    Triple(0.48f, 0.07f, 1.4f),
                    Triple(0.64f, 0.04f, 1.9f),
                    Triple(0.79f, 0.08f, 1.3f),
                    Triple(0.92f, 0.05f, 1.6f),
                    Triple(0.12f, 0.12f, 1.1f),
                    Triple(0.27f, 0.15f, 1.7f),
                    Triple(0.58f, 0.12f, 2.1f),
                    Triple(0.73f, 0.16f, 1.2f),
                    Triple(0.88f, 0.11f, 1.8f),
                    Triple(0.96f, 0.18f, 1.0f),
                    Triple(0.08f, 0.22f, 1.5f),
                    Triple(0.22f, 0.25f, 1.1f),
                    Triple(0.42f, 0.20f, 1.9f),
                    Triple(0.68f, 0.22f, 1.4f),
                    Triple(0.84f, 0.24f, 1.7f),
                    Triple(0.15f, 0.30f, 1.2f),
                    Triple(0.35f, 0.29f, 1.0f),
                    Triple(0.60f, 0.31f, 1.5f),
                    Triple(0.90f, 0.30f, 1.3f),
                    Triple(0.03f, 0.15f, 1.4f),
                    Triple(0.52f, 0.02f, 1.6f),
                    Triple(0.70f, 0.06f, 1.1f),
                    Triple(0.38f, 0.11f, 1.3f),
                    Triple(0.81f, 0.03f, 1.5f),
                    Triple(0.25f, 0.08f, 1.2f)
                )

                stars.forEachIndexed { idx, (sx, sy, baseRadiusDp) ->
                    val isAlternate = idx % 3 == 0
                    val factor = if (isAlternate) starTwinkle else (1.2f - starTwinkle).coerceIn(0.25f, 1.0f)
                    val starCenter = Offset(w * sx, h * sy)

                    // Soft star halo for brighter stars
                    if (baseRadiusDp > 1.6f) {
                        drawCircle(
                            color = Color.White.copy(alpha = factor * starOpacity * 0.30f),
                            radius = (baseRadiusDp * 2.2f).dp.toPx(),
                            center = starCenter
                        )
                    }

                    drawCircle(
                        color = Color.White.copy(alpha = factor * starOpacity * 0.90f),
                        radius = baseRadiusDp.dp.toPx(),
                        center = starCenter
                    )
                }

                // Rare shooting star streak during twilight/night
                if (shootingStarProgress in 0.20f..0.36f) {
                    val sp = (shootingStarProgress - 0.20f) / 0.16f
                    val startX = w * (0.18f + 0.48f * sp)
                    val startY = h * (0.03f + 0.12f * sp)
                    val streakLen = 46.dp.toPx()
                    val endX = startX + streakLen * 0.86f
                    val endY = startY + streakLen * 0.50f

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.85f * starOpacity),
                                Color.Transparent
                            ),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // -------------------------------------------------------------
            // 3. Celestial Body (Google's Weather Simple Sun & Moon Icons)
            // -------------------------------------------------------------
            if (isDaytime) {
                // GOOGLE WEATHER SIMPLE SUN ICON:
                // Minimalist, iconic Google Weather Sunny design

                // A. Ambient soft golden halo
                val sunAuraRadius = 55.dp.toPx() * coronaPulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x4DFBBC04),
                            Color(0x15FBBC04),
                            Color.Transparent
                        ),
                        center = celestialCenter,
                        radius = sunAuraRadius
                    ),
                    radius = sunAuraRadius,
                    center = celestialCenter
                )

                // B. 8 Clean, rounded pill rays (Google Weather iconic signature)
                val rayCount = 8
                val innerRayRadius = 23.dp.toPx()
                val outerRayRadius = 31.dp.toPx()
                val googleSunYellow = Color(0xFFFBBC04) // Google Sunny Yellow

                for (i in 0 until rayCount) {
                    val angleDeg = i * (360.0 / rayCount) + (sunRayRotation * 0.25f)
                    val angleRad = Math.toRadians(angleDeg)

                    val rayStart = Offset(
                        celestialCenter.x + (innerRayRadius * cos(angleRad)).toFloat(),
                        celestialCenter.y + (innerRayRadius * sin(angleRad)).toFloat()
                    )
                    val rayEnd = Offset(
                        celestialCenter.x + (outerRayRadius * cos(angleRad)).toFloat(),
                        celestialCenter.y + (outerRayRadius * sin(angleRad)).toFloat()
                    )
                    drawLine(
                        color = googleSunYellow,
                        start = rayStart,
                        end = rayEnd,
                        strokeWidth = 3.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // C. Central clean Sun disk (Google Weather solid warm yellow)
                val coreRadius = 18.dp.toPx()
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD54F),
                            Color(0xFFFBBC04)
                        ),
                        startY = celestialCenter.y - coreRadius,
                        endY = celestialCenter.y + coreRadius
                    ),
                    radius = coreRadius,
                    center = celestialCenter
                )
            } else {
                // GOOGLE WEATHER SIMPLE MOON ICON:
                // Minimalist, iconic Google Weather Clear Night Crescent & Sparkle Star

                // A. Ambient soft lunar glow
                val moonAuraRadius = 48.dp.toPx() * coronaPulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x35FFE082),
                            Color(0x10FFE082),
                            Color.Transparent
                        ),
                        center = celestialCenter,
                        radius = moonAuraRadius
                    ),
                    radius = moonAuraRadius,
                    center = celestialCenter
                )



                // B. Google Weather Clear Night companion sparkle star
                val starCenterX = celestialCenter.x + 18.dp.toPx()
                val starCenterY = celestialCenter.y - 12.dp.toPx()
                val sparkleLen = 4.5.dp.toPx() * starTwinkle
                // Vertical sparkle line
                val moonColor = Color(0xFFFFE082) // Google Weather lunar amber-yellow
                drawLine(
                    color = moonColor.copy(alpha = 0.90f),
                    start = Offset(starCenterX, starCenterY - sparkleLen),
                    end = Offset(starCenterX, starCenterY + sparkleLen),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Horizontal sparkle line
                drawLine(
                    color = moonColor.copy(alpha = 0.90f),
                    start = Offset(starCenterX - sparkleLen, starCenterY),
                    end = Offset(starCenterX + sparkleLen, starCenterY),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Central star point
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 1.2.dp.toPx(),
                    center = Offset(starCenterX, starCenterY)
                )

                // C. Iconic Google Weather Crescent Moon
                val moonRadius = 20.dp.toPx()
                val outerPath = Path().apply {
                    addOval(
                        Rect(
                            left = celestialCenter.x - moonRadius,
                            top = celestialCenter.y - moonRadius,
                            right = celestialCenter.x + moonRadius,
                            bottom = celestialCenter.y + moonRadius
                        )
                    )
                }
                val innerRadius = 16.5.dp.toPx()
                val innerOffsetX = celestialCenter.x + 7.5.dp.toPx()
                val innerOffsetY = celestialCenter.y - 5.5.dp.toPx()
                val innerPath = Path().apply {
                    addOval(
                        Rect(
                            left = innerOffsetX - innerRadius,
                            top = innerOffsetY - innerRadius,
                            right = innerOffsetX + innerRadius,
                            bottom = innerOffsetY + innerRadius
                        )
                    )
                }

                val crescentPath = Path.combine(
                    operation = PathOperation.Difference,
                    path1 = outerPath,
                    path2 = innerPath
                )
                drawPath(path = crescentPath, color = moonColor)
            }

            // -------------------------------------------------------------
            // 4. Immersive Horizon Bloom (Atmospheric Light Scattering)
            // -------------------------------------------------------------
            // Broad radiant glow rising from the horizon line into the sky
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        atmosphericState.horizonGlowColor.copy(alpha = 0.25f),
                        atmosphericState.horizonGlowColor.copy(alpha = 0.55f)
                    ),
                    startY = horizonY - 60.dp.toPx(),
                    endY = horizonY + 5.dp.toPx()
                ),
                topLeft = Offset(0f, horizonY - 60.dp.toPx()),
                size = Size(w, 65.dp.toPx())
            )

            // Delicate atmospheric horizon line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        atmosphericState.horizonGlowColor.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.40f),
                        atmosphericState.horizonGlowColor.copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = w
                ),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 1.0.dp.toPx()
            )

            // -------------------------------------------------------------
            // 5. Minimalist Layered Desert Dunes
            // -------------------------------------------------------------
            // Dune 1: Distant smooth silhouette
            val dune1Path = Path().apply {
                moveTo(0f, horizonY + 7.dp.toPx())
                cubicTo(
                    w * 0.28f, horizonY + 2.dp.toPx(),
                    w * 0.48f, horizonY + 13.dp.toPx(),
                    w * 0.70f, horizonY + 6.dp.toPx()
                )
                cubicTo(
                    w * 0.85f, horizonY + 2.dp.toPx(),
                    w * 0.95f, horizonY + 9.dp.toPx(),
                    w, horizonY + 5.dp.toPx()
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = dune1Path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        atmosphericState.duneColor1.copy(alpha = 0.35f),
                        atmosphericState.duneColor1.copy(alpha = 0.60f)
                    ),
                    startY = horizonY,
                    endY = horizonY + 45.dp.toPx()
                )
            )

            // Dune 2: Mid-ground smooth silhouette
            val dune2Path = Path().apply {
                moveTo(0f, horizonY + 17.dp.toPx())
                cubicTo(
                    w * 0.22f, horizonY + 21.dp.toPx(),
                    w * 0.40f, horizonY + 12.dp.toPx(),
                    w * 0.62f, horizonY + 10.dp.toPx()
                )
                cubicTo(
                    w * 0.80f, horizonY + 19.dp.toPx(),
                    w * 0.92f, horizonY + 23.dp.toPx(),
                    w, horizonY + 18.dp.toPx()
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = dune2Path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        atmosphericState.duneColor2.copy(alpha = 0.45f),
                        atmosphericState.duneColor2.copy(alpha = 0.75f)
                    ),
                    startY = horizonY + 10.dp.toPx(),
                    endY = horizonY + 70.dp.toPx()
                )
            )

            // Dune 3: Foreground smooth silhouette
            val dune3Path = Path().apply {
                moveTo(0f, horizonY + 28.dp.toPx())
                cubicTo(
                    w * 0.32f, horizonY + 34.dp.toPx(),
                    w * 0.65f, horizonY + 24.dp.toPx(),
                    w, horizonY + 30.dp.toPx()
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = dune3Path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        atmosphericState.duneColor2.copy(alpha = 0.35f),
                        atmosphericState.duneColor2.copy(alpha = 0.85f)
                    ),
                    startY = horizonY + 20.dp.toPx(),
                    endY = horizonY + 90.dp.toPx()
                )
            )

            // -------------------------------------------------------------
            // 6. Seamless Atmospheric Scrim
            // -------------------------------------------------------------
            // Gently blends the lower canvas into the base atmospheric tone, preserving sky radiance while ensuring clear foreground readability
            val scrimStartY = horizonY + 8.dp.toPx()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        atmosphericState.scrimColor.copy(alpha = 0.25f),
                        atmosphericState.scrimColor.copy(alpha = 0.65f),
                        atmosphericState.scrimColor.copy(alpha = 0.88f)
                    ),
                    startY = scrimStartY,
                    endY = h
                ),
                topLeft = Offset(0f, scrimStartY),
                size = Size(w, h - scrimStartY)
            )
        }
    }
}
