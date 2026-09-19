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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.calculation.CelestialMath
import java.util.Calendar
import java.util.Date
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Deterministic 0..1 hash for stable procedural placement (no per-frame Random).
 */
private fun hash01(index: Int, salt: Int): Float {
    val x = sin(index * 127.1f + salt * 311.7f) * 43758.5453f
    return x - floor(x)
}

/** A Google Weather drifting cloud: base slot, vertical band, scale, drift speed, and layer. */
private data class GoogleWeatherCloudSpec(
    val baseX: Float,
    val yFrac: Float,
    val wFrac: Float,
    val hFrac: Float,
    val speed: Float,
    val bobPhase: Float,
    val layer: Int // 0: Distant ambient, 1: Midground Google Weather, 2: Foreground wisp
)

/** Procedural Google Weather cloud layers with varied depths, speeds, and bobbing phases. */
private fun googleWeatherCloudSpecs(): List<GoogleWeatherCloudSpec> = listOf(
    // Distant background clouds (softer, higher, slower drift)
    GoogleWeatherCloudSpec(baseX = 0.05f, yFrac = 0.08f, wFrac = 0.44f, hFrac = 0.12f, speed = 0.0016f, bobPhase = 0f, layer = 0),
    GoogleWeatherCloudSpec(baseX = 0.62f, yFrac = 0.12f, wFrac = 0.40f, hFrac = 0.11f, speed = 0.0019f, bobPhase = 1.8f, layer = 0),
    // Midground signature Google Weather clouds (classic puffy silhouettes)
    GoogleWeatherCloudSpec(baseX = 0.18f, yFrac = 0.17f, wFrac = 0.36f, hFrac = 0.11f, speed = 0.0032f, bobPhase = 0.8f, layer = 1),
    GoogleWeatherCloudSpec(baseX = 0.54f, yFrac = 0.22f, wFrac = 0.34f, hFrac = 0.10f, speed = 0.0036f, bobPhase = 2.4f, layer = 1),
    GoogleWeatherCloudSpec(baseX = 0.88f, yFrac = 0.16f, wFrac = 0.35f, hFrac = 0.10f, speed = 0.0028f, bobPhase = 3.6f, layer = 1),
    // Foreground delicate wisps (lower, drifting slightly faster)
    GoogleWeatherCloudSpec(baseX = 0.30f, yFrac = 0.27f, wFrac = 0.27f, hFrac = 0.08f, speed = 0.0046f, bobPhase = 4.2f, layer = 2),
    GoogleWeatherCloudSpec(baseX = 0.76f, yFrac = 0.29f, wFrac = 0.25f, hFrac = 0.075f, speed = 0.0042f, bobPhase = 5.0f, layer = 2)
)

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
    isReadingMode: Boolean = false,
    showPrecipitation: Boolean = false,
    precipitationIntensity: Float = 0f,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val dynamicPrimary = MaterialTheme.colorScheme.primary
    val dynamicTertiary = MaterialTheme.colorScheme.tertiary
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

    // Google Weather smooth rain loop animation
    val rainProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "google_weather_rain"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(atmosphericState.skyColors))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // Keep the landscape expansive while the celestial accent stays above the cards.
        val horizonY = heightPx * 0.62f
        val skyBand = with(density) { (maxHeight * 0.18f).coerceIn(48.dp, 144.dp).toPx() }
        val celestialScale = (widthPx / with(density) { 390.dp.toPx() }).coerceIn(1f, 1.35f)

        // True astronomical placement for this instant, latitude and longitude.
        // East (morning) sits left, west (evening) right; altitude sets height.
        val cloudDrift = remember { googleWeatherCloudSpecs() }
        val sunPos = remember(nowMillis, location.latitude, location.longitude) {
            CelestialMath.sunPosition(nowMillis, location.latitude, location.longitude)
        }
        val moon = remember(nowMillis, location.latitude, location.longitude) {
            CelestialMath.moonInfo(nowMillis, location.latitude, location.longitude)
        }
        val activeAlt = if (isDaytime) sunPos.altitudeDeg else moon.position.altitudeDeg
        val activeHour = if (isDaytime) sunPos.hourAngleDeg else moon.position.hourAngleDeg
        // Bodies fade smoothly across the horizon line instead of popping.
        val bodyAlpha = ((activeAlt + 1.5) / 3.0).toFloat().coerceIn(0f, 1f)
        val hourFrac = ((activeHour + 120.0) / 240.0).coerceIn(0.0, 1.0)
        val celestialX = widthPx * (0.12f + 0.76f * hourFrac).toFloat()
        val yTop = skyBand * 0.02f
        val celestialY = (horizonY - (activeAlt / maxElevation) * (horizonY - yTop))
            .toFloat()
            .coerceIn(0f, horizonY + with(density) { 36.dp.toPx() })

        // Primary Immersive Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val celestialCenter = Offset(celestialX, celestialY)
            val timeSeconds = nowMillis / 1000.0
            val cloudLuminosity = atmosphericState.skyLuminosity
            val duskWarmth = ((0.65f - abs(cloudLuminosity - 0.45f)) / 0.2f).coerceIn(0f, 1f)
            val isDarkNight = cloudLuminosity < 0.25f

            // -------------------------------------------------------------
            // 1. Immersive Atmospheric Light Wash & Celestial Glare
            // -------------------------------------------------------------
            val atmosphericGlareRadius = (if (isDaytime) 220.dp.toPx() else 140.dp.toPx()) * coronaPulse * celestialScale
            val glareGlow = lerpColor(atmosphericState.celestialGlowColor, dynamicPrimary, 0.12f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glareGlow.copy(alpha = (if (isDaytime) 0.35f else 0.20f) * bodyAlpha),
                        glareGlow.copy(alpha = 0.12f * bodyAlpha),
                        Color.Transparent
                    ),
                    center = celestialCenter,
                    radius = atmosphericGlareRadius
                ),
                radius = atmosphericGlareRadius,
                center = celestialCenter
            )

            // -------------------------------------------------------------
            // 2. Google Weather Starfield & Cosmic Serenity
            // -------------------------------------------------------------
            GoogleWeatherSkyElements.drawStarfield(
                drawScope = this,
                width = w,
                height = h,
                starOpacity = atmosphericState.starOpacity,
                starTwinkle = starTwinkle,
                shootingStarProgress = shootingStarProgress,
                dynamicAccent = dynamicPrimary
            )

            // -------------------------------------------------------------
            // 3. Multi-Layered Google Weather Clouds (Procedural drift & gentle floating bob)
            // -------------------------------------------------------------
            cloudDrift.forEach { cloud ->
                val drift = (((cloud.baseX + timeSeconds * cloud.speed) % 1.35).let {
                    if (it < 0) it + 1.35 else it
                } - 0.18).toFloat()
                val bobOffset = sin(timeSeconds * 0.45 + cloud.bobPhase).toFloat() * 2.8.dp.toPx()
                val cloudW = w * cloud.wFrac
                val cloudH = h * cloud.hFrac
                val cloudX = w * drift
                val cloudY = h * cloud.yFrac + bobOffset

                // Time-of-day and dynamic accent responsive colors
                val baseFill = if (isDarkNight) {
                    Color(0xFF1E283C)
                } else {
                    lerpColor(Color(0xFFEBF3FC), Color.White, cloudLuminosity)
                }
                val highlightColor = when {
                    isDarkNight -> Color(0xFFADC6E5)
                    duskWarmth > 0.05f -> lerpColor(Color.White, Color(0xFFFFE082), duskWarmth * 0.85f)
                    else -> Color.White
                }
                val shadowColor = when {
                    isDarkNight -> Color(0xFF101724)
                    duskWarmth > 0.05f -> lerpColor(Color(0xFFD3E3F5), atmosphericState.horizonGlowColor.copy(alpha = 1f), duskWarmth * 0.65f)
                    else -> lerpColor(Color(0xFFD3E3F5), dynamicPrimary.copy(alpha = 0.12f), 0.20f)
                }
                val fillColor = if (duskWarmth > 0.05f && !isDarkNight) {
                    lerpColor(baseFill, Color(0xFFFFCC80), duskWarmth * 0.70f)
                } else {
                    baseFill
                }

                // Layer-based depth opacity
                val layerMultiplier = when (cloud.layer) {
                    0 -> 0.75f // distant
                    1 -> 1.0f  // midground
                    else -> 0.65f // foreground wisp
                }
                val baseAlpha = (0.12f + 0.15f * cloudLuminosity) * layerMultiplier
                val finalAlpha = if (isReadingMode) baseAlpha * 0.70f else baseAlpha

                GoogleWeatherSkyElements.drawCloud(
                    drawScope = this,
                    topLeft = Offset(cloudX, cloudY),
                    size = Size(cloudW, cloudH),
                    fillColor = fillColor,
                    highlightColor = highlightColor,
                    shadowColor = shadowColor,
                    alpha = finalAlpha,
                    isDarkNight = isDarkNight
                )
            }

            // -------------------------------------------------------------
            // 4. Celestial Body (Google Weather Sun or True-Phase Moon)
            // -------------------------------------------------------------
            if (isDaytime) {
                GoogleWeatherSkyElements.drawSun(
                    drawScope = this,
                    center = celestialCenter,
                    bodyAlpha = bodyAlpha,
                    coronaPulse = coronaPulse,
                    sunRayRotation = sunRayRotation,
                    dynamicAccent = dynamicPrimary,
                    duskWarmth = duskWarmth,
                    atmosphericGlow = atmosphericState.celestialGlowColor
                )
            } else {
                GoogleWeatherSkyElements.drawMoon(
                    drawScope = this,
                    center = celestialCenter,
                    moon = moon,
                    bodyAlpha = bodyAlpha,
                    coronaPulse = coronaPulse,
                    starTwinkle = starTwinkle,
                    dynamicAccent = dynamicTertiary
                )
            }

            // -------------------------------------------------------------
            // 5. Google Weather Rain & Precipitation Layer
            // -------------------------------------------------------------
            val effectiveRainIntensity = when {
                precipitationIntensity > 0f -> precipitationIntensity
                showPrecipitation -> 0.85f
                else -> 0f
            }
            if (effectiveRainIntensity > 0.01f) {
                GoogleWeatherSkyElements.drawRain(
                    drawScope = this,
                    width = w,
                    height = h,
                    rainProgress = rainProgress,
                    intensity = effectiveRainIntensity,
                    dynamicAccent = dynamicPrimary
                )
            }

            // -------------------------------------------------------------
            // 6. Immersive Horizon Bloom (Atmospheric Light Scattering)
            // -------------------------------------------------------------
            val horizonBloomColor = lerpColor(atmosphericState.horizonGlowColor, dynamicPrimary, 0.10f)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        horizonBloomColor.copy(alpha = 0.25f),
                        horizonBloomColor.copy(alpha = 0.55f)
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
                        horizonBloomColor.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.40f),
                        horizonBloomColor.copy(alpha = 0.50f),
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
            // 7. Minimalist Layered Desert Dunes
            // -------------------------------------------------------------
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
            // 8. Seamless Atmospheric Scrim
            // -------------------------------------------------------------
            val scrimStartY = if (isReadingMode) 0f else skyBand * 0.65f
            val alpha1 = if (isReadingMode) 0.35f else 0.18f
            val alpha2 = if (isReadingMode) 0.60f else 0.40f
            val alpha3 = if (isReadingMode) 0.84f else 0.72f

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isReadingMode) surfaceColor.copy(alpha = 0.10f) else Color.Transparent,
                        surfaceColor.copy(alpha = alpha1),
                        surfaceColor.copy(alpha = alpha2),
                        surfaceColor.copy(alpha = alpha3)
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
