package com.example.features.prayer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.CardExpressiveShape
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
    val celestialX: Float,
    val celestialY: Float,
    val bodyAlpha: Float,
    val isDaytime: Boolean,
    val timeSeconds: Double
) {
    fun sample(bounds: Rect): CountdownLuminance {
        if (width <= 0f || height <= 0f || bounds.width <= 0f || bounds.height <= 0f) {
            return CountdownLuminance()
        }

        val padding = 6f * density
        val left = (bounds.left - padding).coerceIn(0f, width)
        val top = (bounds.top - padding).coerceIn(0f, height)
        val right = (bounds.right + padding).coerceIn(0f, width)
        val bottom = (bounds.bottom + padding).coerceIn(0f, height)
        val sampleWidth = right - left
        val sampleHeight = bottom - top
        if (sampleWidth <= 0f || sampleHeight <= 0f) return CountdownLuminance()

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

        val deltaX = x - celestialX
        val deltaY = y - celestialY
        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        val outerRadius = (if (isDaytime) 75f else 52f) * density
        val glowAlpha = (
            (1f - distance / (outerRadius * 3.2f)).coerceIn(0f, 1f).let { it * it } *
                bodyAlpha * (if (isDaytime) 0.50f else 0.26f)
            )
        val glowColor = if (isDaytime) Color(0xFFFFC857) else Color(0xFFFFE082)
        color = mixColors(color, glowColor, glowAlpha)

        val bodyRadius = (if (isDaytime) 18f else 20f) * density
        if (distance < bodyRadius && bodyAlpha > 0f) {
            val bodyAlphaAtCenter = bodyAlpha * (0.92f - 0.12f * distance / bodyRadius)
            color = mixColors(color, glowColor, bodyAlphaAtCenter)
        }

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
        mutedDark = darker(onSurfaceVariant, surfaceLowest),
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
    val isDaytime = nowMillis in prayerTimes.sunrise.time..prayerTimes.maghrib.time
    val atmosphere = getInterpolatedAtmosphericState(prayerTimes, nowMillis)
    val calendar = Calendar.getInstance().apply { time = now }
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))
    val maxElevation = (90.0 - abs(location.latitude - declination)).coerceIn(15.0, 90.0)
    val sun = CelestialMath.sunPosition(nowMillis, location.latitude, location.longitude)
    val moon = CelestialMath.moonInfo(nowMillis, location.latitude, location.longitude)
    val activeAltitude = if (isDaytime) sun.altitudeDeg else moon.position.altitudeDeg
    val activeHour = if (isDaytime) sun.hourAngleDeg else moon.position.hourAngleDeg
    val bodyAlpha = ((activeAltitude + 1.5) / 3.0).toFloat().coerceIn(0f, 1f)
    val hourFraction = ((activeHour + 120.0) / 240.0).coerceIn(0.0, 1.0)
    val horizonY = safeHeight * 0.62f
    val skyBand = (safeHeight * 0.18f).coerceIn(48f * safeDensity, 144f * safeDensity)
    val celestialX = safeWidth * (0.12f + 0.76f * hourFraction).toFloat()
    val top = skyBand * 0.02f
    val celestialY = (horizonY - (activeAltitude / maxElevation) * (horizonY - top))
        .toFloat()
        .coerceIn(0f, horizonY + 36f * safeDensity)
    return CountdownSkyProbe(
        atmosphere = atmosphere,
        width = safeWidth,
        height = safeHeight,
        density = safeDensity,
        horizonY = horizonY,
        celestialX = celestialX,
        celestialY = celestialY,
        bodyAlpha = bodyAlpha,
        isDaytime = isDaytime,
        timeSeconds = nowMillis / 1000.0
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

private fun contrastRatio(first: Float, second: Float): Float {
    val lighter = max(first, second)
    val darker = min(first, second)
    return (lighter + 0.05f) / (darker + 0.05f)
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
        val worstCase = min(
            contrastRatio(foreground, sample.low),
            contrastRatio(foreground, sample.high)
        )
        val averageCase = contrastRatio(foreground, sample.mean)
        return worstCase * 0.68f + averageCase * 0.32f
    }

    val darkScore = score(dark)
    val lightScore = score(light)
    return when {
        lightScore > darkScore + 0.18f -> light
        darkScore > lightScore + 0.18f -> dark
        sample.mean >= 0.45f -> dark
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
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = NourMotion.duration(900), easing = NourMotion.easing),
        label = "adaptive_countdown_text"
    )
    val haloStrength = ((sample.high - sample.low - 0.18f) / 0.32f).coerceIn(0f, 1f)
    val haloColor = if (animatedColor.luminance() < 0.5f) Color.White else Color.Black
    val renderedStyle = if (haloStrength > 0f) {
        style.copy(
            shadow = Shadow(
                color = haloColor.copy(alpha = 0.40f * haloStrength),
                offset = Offset.Zero,
                blurRadius = 1.35f * probe.density
            )
        )
    } else {
        style
    }

    Text(
        text = text,
        style = renderedStyle,
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
        // Hero countdown card integrating the dynamic celestial sky as an intrinsic part of the card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardExpressiveShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(
                    BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
                    shape = CardExpressiveShape
                )
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
                countdownState = countdownState,
                showSurfaceScrim = false,
                modifier = Modifier
                    .matchParentSize()
                    .testTag("prayer_sky_background")
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
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = strings.nextBadge,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = NourSpace.sm, vertical = 5.dp),
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
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = 7.dp),
                        modifier = Modifier.testTag("prayer_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(NourSize.smallIcon)
                        )
                        Text(
                            text = location.cityName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = NourSpace.xs)
                        )
                    }
                }

                // Prayer Name
                AdaptiveCountdownText(
                    text = prayerLabel(countdownState.nextPrayer),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    role = CountdownTextRole.Primary,
                    probe = skyProbe,
                    cardBounds = cardBounds,
                    palette = adaptiveTextPalette,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Scheduled Time
                AdaptiveCountdownText(
                    text = formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour)),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    role = CountdownTextRole.Muted,
                    probe = skyProbe,
                    cardBounds = cardBounds,
                    palette = adaptiveTextPalette,
                    maxLines = 1,
                    softWrap = false
                )

                // Animated Countdown Digits
                AnimatedContent(
                    targetState = countdownState.formattedRemaining,
                    transitionSpec = {
                        (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)) { it / 3 } +
                            fadeIn(animationSpec = tween(NourMotion.duration(120))))
                            .togetherWith(
                                slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 3 } +
                                    fadeOut(animationSpec = tween(NourMotion.duration(120)))
                            )
                    },
                    label = "countdown_animated_digits"
                ) { remaining ->
                    AdaptiveCountdownText(
                        text = formatNumerals(remaining),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Bold
                        ),
                        role = CountdownTextRole.Accent,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
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
                    AdaptiveCountdownText(
                        text = "${prayerLabel(countdownState.currentPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.currentPrayer, use24Hour))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        role = CountdownTextRole.Muted,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    AdaptiveCountdownText(
                        text = "${prayerLabel(countdownState.nextPrayer)} ${formatNumerals(todayPrayerTimes.formattedTime(countdownState.nextPrayer, use24Hour))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        role = CountdownTextRole.Muted,
                        probe = skyProbe,
                        cardBounds = cardBounds,
                        palette = adaptiveTextPalette,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
