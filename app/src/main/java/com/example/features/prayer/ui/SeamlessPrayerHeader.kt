package com.example.features.prayer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.features.prayer.calculation.CelestialMath
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.widgets.AtmosphericState
import com.example.features.widgets.ImmersiveSkyBackground
import com.example.features.widgets.getInterpolatedAtmosphericState
import com.example.localization.LocalAppStrings
import com.example.localization.formatNumerals
import com.example.localization.LocalUse24HourFormat
import com.example.ui.components.nourFloatingSurfaceColor
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape
import java.util.Calendar
import java.util.Date
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private enum class CountdownTextRole {
    Primary,
    Muted,
    Accent
}

private data class CountdownTextPalette(
    val primaryDark: Color,
    val primaryLight: Color,
    val mutedDark: Color,
    val mutedLight: Color,
    val accentDark: Color,
    val accentLight: Color
)

private data class CountdownLuminance(
    val mean: Float = 0.5f,
    val low: Float = 0.25f,
    val high: Float = 0.75f
)

private data class CountdownSkyProbe(
    val atmosphere: AtmosphericState,
    val width: Float,
    val height: Float,
    val density: Float,
    val horizonY: Float,
    val sunX: Float,
    val sunY: Float,
    val sunAlpha: Float,
    val moonX: Float,
    val moonY: Float,
    val moonAlpha: Float,
    val timeSeconds: Double,
    val initialLuminance: CountdownLuminance
) {
    fun sample(bounds: Rect): CountdownLuminance {
        if (width <= 0f || height <= 0f || bounds.width <= 0f || bounds.height <= 0f) {
            // Nothing is placed yet. Falling back to a fixed mid-grey guess made
            // the first frame pick the dark text variant — nearly invisible on a
            // night sky — before the real sample arrived. The sky gradient is
            // known at this point, so estimate from it instead.
            return initialLuminance
        }

        val padding = 6f * density
        val left = (bounds.left - padding).coerceIn(0f, width)
        val top = (bounds.top - padding).coerceIn(0f, height)
        val right = (bounds.right + padding).coerceIn(0f, width)
        val bottom = (bounds.bottom + padding).coerceIn(0f, height)
        val sampleWidth = right - left
        val sampleHeight = bottom - top
        if (sampleWidth <= 0f || sampleHeight <= 0f) return initialLuminance

        val columns = 7
        val rows = 5
        val luminanceValues = ArrayList<Float>(columns * rows)
        var weightedLuminance = 0f
        var totalWeight = 0f

        for (row in 0 until rows) {
            val normalizedY = (row + 0.5f) / rows * 2f - 1f
            for (column in 0 until columns) {
                val normalizedX = (column + 0.5f) / columns * 2f - 1f
                val x = left + sampleWidth * (column + 0.5f) / columns
                val y = top + sampleHeight * (row + 0.5f) / rows
                val weight = exp(-(normalizedX * normalizedX * 0.9f + normalizedY * normalizedY * 0.7f))
                    .coerceAtLeast(0.05f)
                val luminance = sampleColor(x, y).luminance().coerceIn(0f, 1f)
                luminanceValues += luminance
                weightedLuminance += luminance * weight
                totalWeight += weight
            }
        }

        val sorted = luminanceValues.sorted()
        val lowIndex = ((sorted.size - 1) * 0.10f).toInt()
        val highIndex = ((sorted.size - 1) * 0.90f).toInt()
        return CountdownLuminance(
            mean = if (totalWeight > 0f) weightedLuminance / totalWeight else 0.5f,
            low = sorted[lowIndex],
            high = sorted[highIndex]
        )
    }

    private fun sampleColor(x: Float, y: Float): Color {
        val colors = atmosphere.skyColors
        val gradientPosition = (y / height).coerceIn(0f, 1f) * (colors.lastIndex.coerceAtLeast(1))
        val lowerIndex = floor(gradientPosition).toInt().coerceIn(0, colors.lastIndex)
        val upperIndex = (lowerIndex + 1).coerceAtMost(colors.lastIndex)
        var color = mixColors(colors[lowerIndex], colors[upperIndex], gradientPosition - lowerIndex)

        val xFraction = (x / width).coerceIn(0f, 1f)
        val yFraction = (y / height).coerceIn(0f, 1f)
        if (yFraction in 0.04f..0.42f) {
            val motionSeconds = timeSeconds.toFloat()
            val cloudWave = (
                sin(xFraction * PI.toFloat() * 7f + motionSeconds * 0.025f) *
                    sin(yFraction * PI.toFloat() * 5f - motionSeconds * 0.018f)
                ).coerceAtLeast(0f)
            val cloudAlpha = (0.012f + atmosphere.skyLuminosity * 0.018f) * cloudWave
            val cloudColor = if (atmosphere.skyLuminosity < 0.25f) {
                Color(0xFF7088A8)
            } else {
                Color.White
            }
            color = mixColors(color, cloudColor, cloudAlpha)
        }

        // Model both bodies so text keeps its guaranteed contrast whenever
        // either the Sun or the Moon passes behind it.
        color = withCelestialGlow(color, x, y, sunX, sunY, sunAlpha, isSun = true)
        color = withCelestialGlow(color, x, y, moonX, moonY, moonAlpha, isSun = false)

        val horizonDistance = abs(y - horizonY)
        val bloomAlpha = (
            (1f - horizonDistance / (65f * density)).coerceIn(0f, 1f).let { it * it } * 0.20f
            )
        color = mixColors(color, atmosphere.horizonGlowColor.copy(alpha = 1f), bloomAlpha)

        if (y > horizonY) {
            val depth = ((y - horizonY) / (height - horizonY).coerceAtLeast(1f)).coerceIn(0f, 1f)
            color = mixColors(color, atmosphere.duneColor2.copy(alpha = 1f), 0.16f + depth * 0.24f)
        }

        return color
    }

    /**
     * The approximate Sun/Moon contribution at a point: a soft ambient glow
     * plus the brighter disk core, so adaptive text keeps its contrast even
     * when a body sits directly behind it.
     */
    private fun withCelestialGlow(
        base: Color,
        x: Float,
        y: Float,
        centerX: Float,
        centerY: Float,
        alpha: Float,
        isSun: Boolean
    ): Color {
        if (alpha <= 0f) return base
        val deltaX = x - centerX
        val deltaY = y - centerY
        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        val outerRadius = (if (isSun) 75f else 52f) * density
        val glowAlpha = (
            (1f - distance / (outerRadius * 3.2f)).coerceIn(0f, 1f).let { it * it } *
                alpha * (if (isSun) 0.50f else 0.26f)
            )
        val glowColor = if (isSun) Color(0xFFFFC857) else Color(0xFFFFE082)
        val withGlow = mixColors(base, glowColor, glowAlpha)
        val bodyRadius = (if (isSun) 18f else 20f) * density
        return if (distance < bodyRadius) {
            mixColors(withGlow, glowColor, alpha * (0.92f - 0.12f * distance / bodyRadius))
        } else {
            withGlow
        }
    }
}

private fun createCountdownTextPalette(
    onSurface: Color,
    surface: Color,
    onSurfaceVariant: Color,
    surfaceLowest: Color,
    primary: Color,
    primaryContainer: Color
): CountdownTextPalette {
    fun darker(first: Color, second: Color) = if (first.luminance() <= second.luminance()) first else second
    fun lighter(first: Color, second: Color) = if (first.luminance() >= second.luminance()) first else second
    return CountdownTextPalette(
        primaryDark = darker(onSurface, surface),
        primaryLight = lighter(onSurface, surface),
        // The muted pair still has to clear 4.5:1 on a pale noon sky, and with
        // no halo behind the glyphs that is a real constraint. onSurface joins
        // the dark candidates: in light themes it is darker than
        // onSurfaceVariant (so it wins where contrast is tight), while in dark
        // themes surfaceContainerLowest stays the darkest option and the pick
        // is unchanged.
        mutedDark = darker(darker(onSurfaceVariant, surfaceLowest), onSurface),
        mutedLight = lighter(onSurfaceVariant, surfaceLowest),
        accentDark = darker(primary, primaryContainer),
        accentLight = lighter(primary, primaryContainer)
    )
}

private fun createCountdownSkyProbe(
    prayerTimes: PrayerTimes,
    location: UserLocation,
    now: Date,
    width: Float,
    height: Float,
    density: Float
): CountdownSkyProbe {
    val safeWidth = max(width, 1f)
    val safeHeight = max(height, 1f)
    val safeDensity = max(density, 0.1f)
    val nowMillis = now.time
    val atmosphere = getInterpolatedAtmosphericState(prayerTimes, nowMillis)
    val calendar = Calendar.getInstance().apply { time = now }
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))
    val maxElevation = (90.0 - abs(location.latitude - declination)).coerceIn(15.0, 90.0)
    val sun = CelestialMath.sunPosition(nowMillis, location.latitude, location.longitude)
    val moon = CelestialMath.moonInfo(nowMillis, location.latitude, location.longitude)
    val horizonY = safeHeight * 0.62f
    val skyBand = (safeHeight * 0.18f).coerceIn(48f * safeDensity, 144f * safeDensity)
    val top = skyBand * 0.02f
    fun xFor(hourAngleDeg: Double): Float =
        safeWidth * (0.12f + 0.76f * ((hourAngleDeg + 120.0) / 240.0).coerceIn(0.0, 1.0)).toFloat()
    fun yFor(altitudeDeg: Double): Float =
        (horizonY - (altitudeDeg / maxElevation) * (horizonY - top)).toFloat()
            .coerceIn(0f, horizonY + 36f * safeDensity)
    // Mirror the renderer: each body is visible on its own altitude.
    val sunAlpha = ((sun.altitudeDeg + 1.5) / 3.0).toFloat().coerceIn(0f, 1f)
    val moonAlpha = ((moon.position.altitudeDeg + 1.5) / 3.0).toFloat().coerceIn(0f, 1f)
    return CountdownSkyProbe(
        atmosphere = atmosphere,
        width = safeWidth,
        height = safeHeight,
        density = safeDensity,
        horizonY = horizonY,
        sunX = xFor(sun.hourAngleDeg),
        sunY = yFor(sun.altitudeDeg),
        sunAlpha = sunAlpha,
        moonX = xFor(moon.position.hourAngleDeg),
        moonY = yFor(moon.position.altitudeDeg),
        moonAlpha = moonAlpha,
        timeSeconds = nowMillis / 1000.0,
        initialLuminance = skyGradientLuminance(atmosphere)
    )
}

/**
 * Luminance estimate for a sky whose gradient is known but whose text bounds are
 * not yet measured. The hero text sits in the upper half of the gradient, so
 * that half carries the mean; the extremes keep the worst-case scoring honest.
 */
private fun skyGradientLuminance(atmosphere: AtmosphericState): CountdownLuminance {
    val colors = atmosphere.skyColors
    if (colors.isEmpty()) return CountdownLuminance()
    val luminance = colors.map { it.luminance().coerceIn(0f, 1f) }
    val upperHalf = luminance.take((luminance.size + 1) / 2)
    return CountdownLuminance(
        mean = upperHalf.average().toFloat(),
        low = luminance.min(),
        high = luminance.max()
    )
}

private fun mixColors(first: Color, second: Color, fraction: Float): Color {
    val amount = fraction.coerceIn(0f, 1f)
    return Color(
        red = first.red + (second.red - first.red) * amount,
        green = first.green + (second.green - first.green) * amount,
        blue = first.blue + (second.blue - first.blue) * amount,
        alpha = first.alpha + (second.alpha - first.alpha) * amount
    )
}

/**
 * Numerals already carry their meaning in the digits, so they should not
 * swallow the hero when the system font scale grows — and these single-line
 * strings cannot wrap (softWrap is disabled with Clip), so at very large
 * scales they would silently cut digits off the edge. Compose multiplies
 * each sp by the font scale; cancelling most of that growth caps the
 * rendered size below a chosen multiple of the base size.
 */
private fun TextStyle.withFontScaleCap(fontScale: Float, cap: Float = 1.3f): TextStyle =
    if (fontScale <= cap) this else copy(fontSize = fontSize * (cap / fontScale))

private fun contrastRatio(first: Float, second: Float): Float {
    val lighter = max(first, second)
    val darker = min(first, second)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** Contrast against the darkest *and* the brightest part of the sampled area. */
private fun worstCaseContrast(color: Color, sample: CountdownLuminance): Float {
    val foreground = color.luminance()
    return min(
        contrastRatio(foreground, sample.low),
        contrastRatio(foreground, sample.high)
    )
}

private fun adaptiveTextColor(
    sample: CountdownLuminance,
    palette: CountdownTextPalette,
    role: CountdownTextRole
): Color {
    val dark = when (role) {
        CountdownTextRole.Primary -> palette.primaryDark
        CountdownTextRole.Muted -> palette.mutedDark
        CountdownTextRole.Accent -> palette.accentDark
    }
    val light = when (role) {
        CountdownTextRole.Primary -> palette.primaryLight
        CountdownTextRole.Muted -> palette.mutedLight
        CountdownTextRole.Accent -> palette.accentLight
    }

    fun score(color: Color): Float {
        val foreground = color.luminance()
        val averageCase = contrastRatio(foreground, sample.mean)
        return worstCaseContrast(color, sample) * 0.68f + averageCase * 0.32f
    }

    val darkScore = score(dark)
    val lightScore = score(light)
    return when {
        lightScore > darkScore + 0.18f -> light
        darkScore > lightScore + 0.18f -> dark
        // The scores are within a coin flip; break the tie by the mean. The
        // light/dark contrast curves of these palettes cross near luminance
        // 0.18-0.21, so anything brighter than 0.20 favours the dark variant.
        // (0.45 used to keep white far past the crossover: on mid-luminance
        // skies that cost the small time line 1-1.5:1 — measured 3.44:1 in
        // `m13_hero_noon_settled.png` where dark measures 4.9:1.)
        sample.mean >= 0.20f -> dark
        else -> light
    }
}

@Composable
private fun AdaptiveCountdownText(
    text: String,
    style: TextStyle,
    role: CountdownTextRole,
    probe: CountdownSkyProbe,
    cardBounds: Rect,
    palette: CountdownTextPalette,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true
) {
    var measuredRootBounds by remember { mutableStateOf(Rect.Zero) }
    val localBounds = remember(measuredRootBounds, cardBounds) {
        val left = (measuredRootBounds.left - cardBounds.left).coerceIn(0f, cardBounds.width)
        val top = (measuredRootBounds.top - cardBounds.top).coerceIn(0f, cardBounds.height)
        val right = (measuredRootBounds.right - cardBounds.left).coerceIn(0f, cardBounds.width)
        val bottom = (measuredRootBounds.bottom - cardBounds.top).coerceIn(0f, cardBounds.height)
        Rect(left, top, right, bottom)
    }
    val sample = remember(probe, localBounds) {
        probe.sample(localBounds)
    }
    val targetColor = remember(sample, palette, role) {
        adaptiveTextColor(sample, palette, role)
    }
    // Before the first measurement `sample` is the sky-gradient estimate, so
    // `targetColor` is already close to the settled colour. Applying the first
    // real colour instantly avoids a visible fade at open; later sky-driven
    // colour changes still cross-fade.
    var hasMeasured by remember { mutableStateOf(false) }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = if (hasMeasured) {
            tween(durationMillis = NourMotion.duration(900), easing = NourMotion.easing)
        } else {
            snap()
        },
        label = "adaptive_countdown_text"
    )
    SideEffect { if (localBounds.width > 0f) hasMeasured = true }
    // The hero text sits directly on the sky: no scrim, no halo, no shadow
    // layer between the artwork and the glyphs (they muddy the sky). Contrast
    // is carried by the sampled colour choice above alone, which is why the
    // palette candidates are as dark/light as the theme allows.

    Text(
        text = text,
        style = style,
        color = animatedColor,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            val size = coordinates.size
            val rootBounds = Rect(
                position.x,
                position.y,
                position.x + size.width,
                position.y + size.height
            )
            if (rootBounds != measuredRootBounds) measuredRootBounds = rootBounds
        }
    )
}

@Composable
fun SeamlessPrayerHeader(
    location: UserLocation,
    countdownState: CountdownState,
    todayPrayerTimes: PrayerTimes,
    now: Date,
    onRequestLocationUpdate: (() -> Unit)? = null,
    /**
     * When set, the sky hero is fixed to exactly this height: the screen
     * stretches it by the page's leftover slack so the day switcher and the
     * prayer card end at the bottom reserve. Null keeps the hero sized by
     * its text stack — this also avoids clipping while the stack height is
     * still unmeasured (first layout pass).
     */
    heroHeight: Dp? = null,
    /** Reports the hero text stack's laid-out height for the stretch math. */
    onHeroStackHeight: ((Dp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val use24Hour = LocalUse24HourFormat.current
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val adaptiveTextPalette = remember(colorScheme) {
        createCountdownTextPalette(
            onSurface = colorScheme.onSurface,
            surface = colorScheme.surface,
            onSurfaceVariant = colorScheme.onSurfaceVariant,
            surfaceLowest = colorScheme.surfaceContainerLowest,
            primary = colorScheme.primary,
            primaryContainer = colorScheme.primaryContainer
        )
    }
    var cardBounds by remember { mutableStateOf(Rect.Zero) }
    val skyProbe = remember(cardBounds, todayPrayerTimes, location, now, density) {
        createCountdownSkyProbe(
            prayerTimes = todayPrayerTimes,
            location = location,
            now = now,
            width = cardBounds.width,
            height = cardBounds.height,
            density = density.density
        )
    }

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
        animationSpec = tween(NourMotion.duration(NourMotion.long), easing = NourMotion.easing),
        label = "seamless_day_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_header_card"),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        // Hero sky: full-bleed by design (the screen opts out of the frame
        // gutter), so there is deliberately no card background, border or
        // rounding here — the rendered sky itself is the backdrop and the
        // content below floats on it with the shared side measure.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(heroHeight?.let { Modifier.height(it) } ?: Modifier)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size
                    val nextBounds = Rect(
                        position.x,
                        position.y,
                        position.x + size.width,
                        position.y + size.height
                    )
                    if (nextBounds != cardBounds) cardBounds = nextBounds
                }
        ) {
            // 1. Dynamic celestial sky background embedded directly within the countdown card
            ImmersiveSkyBackground(
                prayerTimes = todayPrayerTimes,
                location = location,
                now = now,
                showSurfaceScrim = false,
                modifier = Modifier
                    .matchParentSize()
                    .testTag("prayer_sky_background")
            )

            // 3. Countdown Card Content
            //
            // Grouping, not uniform gaps: the kicker's name and scheduled
            // time read as one unit (NourSpace.sm), the live countdown sits
            // one step aside (NourSpace.md), and the progress/status pair
            // closes the card after the widest gap of all (NourSpace.lg).
            // Sky-content column: the location pill stays parked at the top;
            // in a stretched hero the countdown cluster docks at the bottom
            // and the whole middle is open sky for the arcs. The column fills
            // the hero only when it is stretched — a natural (content-sized)
            // hero keeps the plain stacked flow.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = NourSpace.lg, vertical = NourSpace.lg)
                    .then(
                        if (heroHeight != null) {
                            Modifier.fillMaxHeight()
                        } else {
                            Modifier
                        }
                    )
            ) {
                // Floor pieces for the screen's stretch math: the pill row
                // plus the kicker-to-status span is exactly the sky card's
                // content height; the stretch only flexes the gap between
                // them. Root coordinates keep the span scroll-invariant.
                var pillHeight by remember { mutableStateOf(0.dp) }
                var kickerTopY by remember { mutableStateOf(0f) }
                var statusBottomY by remember { mutableStateOf(0f) }
                val reportStackFloor: () -> Unit = {
                    onHeroStackHeight?.invoke(
                        pillHeight + with(density) { (statusBottomY - kickerTopY).toDp() }
                    )
                }

                // Header row: location button only, parked at the start.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            pillHeight = with(density) { coordinates.size.height.toDp() }
                            reportStackFloor()
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onRequestLocationUpdate?.invoke() },
                        enabled = onRequestLocationUpdate != null,
                        shape = PillShape,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = nourFloatingSurfaceColor(),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
                        contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = 7.dp),
                        modifier = Modifier
                            .testTag("prayer_location_button")
                            .heightIn(min = NourSize.touchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(NourSize.smallIcon)
                        )
                        Text(
                            // Keep the sky hero uncluttered: only the place name,
                            // in the smallest legible type, is shown over the sky.
                            text = location.cityName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = NourSpace.xs)
                        )
                    }
                }

                // The gap flexes: pure sky in a stretched hero; the fixed xl
                // step in the natural, content-sized one. A stretched hero
                // pushes the countdown block down so it sits right above the
                // card's lower edge.
                if (heroHeight != null) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.height(NourSpace.xl))
                }

                // Kicker: the coming prayer and its scheduled time as one
                // small, quiet pair. The countdown below is the hero's single
                // dominant headline — two same-size headlines used to compete
                // for the eye (and the period end time now lives right here,
                // so the status row never has to repeat it). Mirrors in RTL.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                        .onGloballyPositioned { coordinates ->
                            kickerTopY = coordinates.positionInRoot().y
                            reportStackFloor()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
                ) {
                    AdaptiveCountdownText(
                        text = prayerLabel(countdownState.nextPrayer),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        role = CountdownTextRole.Primary,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    AdaptiveCountdownText(
                        text = formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour)),
                        style = MaterialTheme.typography.titleMedium
                            .copy(fontFeatureSettings = "tnum")
                            .withFontScaleCap(density.fontScale),
                        role = CountdownTextRole.Muted,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(Modifier.height(NourSpace.md))

                // Countdown digits: deliberately not animated. The text itself
                // changes every second, so a slide/fade on top is constant
                // motion with no information value (and re-announces under
                // screen readers). Colour still adapts to the sky behind it.
                // A bare "02:14:36" is ambiguous when read aloud, so the digits
                // carry the translated "Next Prayer: …" spoken label.
                val remainingText = formatNumerals(countdownState.formattedRemaining)
                // NUR-014: on-screen countdown haptics. A light tick when the
                // displayed minute changes, a stronger cue when the prayer
                // time lands while this header is visible. Both run through
                // LocalHapticFeedback only — no Vibrator, no permission, and
                // nothing fires in the background. The first composition
                // stays silent: opening the app is not a boundary crossing.
                val hapticFeedback = LocalHapticFeedback.current
                var lastHapticSnapshot by remember { mutableStateOf<CountdownHapticSnapshot?>(null) }
                SideEffect {
                    val snapshot = CountdownHapticSnapshot(
                        nextPrayer = countdownState.nextPrayer,
                        shownMinute = CountdownArrivalHaptics.shownMinuteOf(countdownState.remainingMillis)
                    )
                    when (CountdownArrivalHaptics.kindFor(previous = lastHapticSnapshot, current = snapshot)) {
                        CountdownHapticKind.MINUTE_TICK ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        CountdownHapticKind.PRAYER_ARRIVAL ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        CountdownHapticKind.NONE -> Unit
                    }
                    lastHapticSnapshot = snapshot
                }
                AdaptiveCountdownText(
                    text = remainingText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.Bold
                    ).withFontScaleCap(density.fontScale),
                    role = CountdownTextRole.Accent,
                    probe = skyProbe,
                    cardBounds = cardBounds,
                    palette = adaptiveTextPalette,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .testTag("countdown_timer_text")
                        .semantics {
                            contentDescription = String.format(strings.countdownPattern, remainingText)
                        }
                )

                Spacer(Modifier.height(NourSpace.lg))

                // Progress Bar — with the status line below it, the closing
                // group separated from the hero by the widest gap.
                //
                // Both segments sample the sky exactly like the text above.
                // The plain theme primary is too dark to read over a night sky
                // in the light theme (measured 1.5:1), which left the pale
                // track looking filled while the fill vanished — the
                // indicator read backwards. The adaptive pair keeps the fill
                // the strongest element and the track a soft remainder on
                // every sky, in both themes.
                var barBounds by remember { mutableStateOf(Rect.Zero) }
                val barLocalBounds = remember(barBounds, cardBounds) {
                    Rect(
                        (barBounds.left - cardBounds.left).coerceIn(0f, cardBounds.width),
                        (barBounds.top - cardBounds.top).coerceIn(0f, cardBounds.height),
                        (barBounds.right - cardBounds.left).coerceIn(0f, cardBounds.width),
                        (barBounds.bottom - cardBounds.top).coerceIn(0f, cardBounds.height)
                    )
                }
                val barSample = remember(skyProbe, barLocalBounds) {
                    skyProbe.sample(barLocalBounds)
                }
                val barFillTarget = remember(barSample, adaptiveTextPalette) {
                    adaptiveTextColor(barSample, adaptiveTextPalette, CountdownTextRole.Accent)
                }
                val barTrackTarget = remember(barSample, adaptiveTextPalette) {
                    adaptiveTextColor(barSample, adaptiveTextPalette, CountdownTextRole.Muted)
                }
                var barMeasured by remember { mutableStateOf(false) }
                val barFill by animateColorAsState(
                    targetValue = barFillTarget,
                    animationSpec = if (barMeasured) {
                        tween(durationMillis = NourMotion.duration(900), easing = NourMotion.easing)
                    } else {
                        snap()
                    },
                    label = "seamless_progress_fill"
                )
                val barTrack by animateColorAsState(
                    targetValue = barTrackTarget,
                    animationSpec = if (barMeasured) {
                        tween(durationMillis = NourMotion.duration(900), easing = NourMotion.easing)
                    } else {
                        snap()
                    },
                    label = "seamless_progress_track"
                )
                SideEffect { if (barLocalBounds.width > 0f) barMeasured = true }

                LinearProgressIndicator(
                    progress = { dayProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(PillShape)
                        // Expose the live fraction to screen readers; the
                        // "%" beside it is plain text they otherwise skip.
                        .semantics {
                            progressBarRangeInfo = ProgressBarRangeInfo(dayProgress, 0f..1f)
                        }
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size
                            val nextBounds = Rect(
                                position.x,
                                position.y,
                                position.x + size.width,
                                position.y + size.height
                            )
                            if (nextBounds != barBounds) barBounds = nextBounds
                        }
                        .testTag("countdown_progress_bar"),
                    color = barFill,
                    trackColor = barTrack.copy(alpha = NourAlpha.TrackVeil)
                )

                Spacer(Modifier.height(NourSpace.sm))

                // Status line: the period you are in, and how far the day has
                // run. One fixed row instead of a wrapping flow, and no third
                // copy of the coming prayer — that is already the headline.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            statusBottomY = coordinates.positionInRoot().y + coordinates.size.height
                            reportStackFloor()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdaptiveCountdownText(
                        text = "${prayerLabel(countdownState.currentPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.currentPrayer, use24Hour))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        role = CountdownTextRole.Muted,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    AdaptiveCountdownText(
                        text = formatNumerals("${(dayProgress * 100).toInt()}%"),
                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                        role = CountdownTextRole.Accent,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
