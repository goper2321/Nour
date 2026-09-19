package com.example.features.widgets

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.features.prayer.calculation.CelestialMath.MoonInfo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Google Weather-style SVG vector drawing primitives and atmospheric layers.
 * Provides subtle, soft, and atmospheric illustrations for:
 * - Multi-lobed Google Weather clouds with dimensional gradient fills and rim highlights
 * - Minimalist Google Weather Sun with 8 rounded pill rays and multi-stage corona
 * - True-phase Google Weather Moon with companion 4-point sparkle stars and earthshine
 * - Twinkling 4-point diamond sparkle stars and celestial stardust
 * - Angled rounded rain streaks and atmospheric precipitation vapor
 */
object GoogleWeatherSkyElements {

    /**
     * Adds a Google Weather 4-point diamond sparkle star to a Path.
     */
    fun addSparkleStar(path: Path, cx: Float, cy: Float, radius: Float, pinch: Float = 0.22f) {
        path.apply {
            moveTo(cx, cy - radius)
            cubicTo(cx, cy - radius * pinch, cx + radius * pinch, cy, cx + radius, cy)
            cubicTo(cx + radius * pinch, cy, cx, cy + radius * pinch, cx, cy + radius)
            cubicTo(cx, cy + radius * pinch, cx - radius * pinch, cy, cx - radius, cy)
            cubicTo(cx - radius * pinch, cy, cx, cy - radius * pinch, cx, cy - radius)
            close()
        }
    }

    /**
     * Adds an iconic Google Weather cloud silhouette composed of a rounded base capsule
     * and 3 overlapping puffy lobes.
     */
    fun addCloudSilhouette(path: Path, x: Float, y: Float, w: Float, h: Float) {
        path.apply {
            // Base rounded capsule
            addRoundRect(
                RoundRect(
                    left = x + w * 0.08f,
                    top = y + h * 0.44f,
                    right = x + w * 0.92f,
                    bottom = y + h * 0.86f,
                    cornerRadius = CornerRadius(h * 0.21f, h * 0.21f)
                )
            )
            // Left puff
            val rLeft = h * 0.28f
            val cxLeft = x + w * 0.28f
            val cyLeft = y + h * 0.54f
            addOval(Rect(cxLeft - rLeft, cyLeft - rLeft, cxLeft + rLeft, cyLeft + rLeft))

            // Center tall dome
            val rCenter = h * 0.38f
            val cxCenter = x + w * 0.50f
            val cyCenter = y + h * 0.36f
            addOval(Rect(cxCenter - rCenter, cyCenter - rCenter, cxCenter + rCenter, cyCenter + rCenter))

            // Right shoulder puff
            val rRight = h * 0.30f
            val cxRight = x + w * 0.72f
            val cyRight = y + h * 0.48f
            addOval(Rect(cxRight - rRight, cyRight - rRight, cxRight + rRight, cyRight + rRight))
        }
    }

    /**
     * Draws a single Google Weather-style cloud with dimensional gradient fill and subtle crest highlight.
     */
    fun drawCloud(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Size,
        fillColor: Color,
        highlightColor: Color,
        shadowColor: Color,
        alpha: Float,
        isDarkNight: Boolean
    ) {
        if (alpha <= 0.005f) return
        val w = size.width
        val h = size.height
        val x = topLeft.x
        val y = topLeft.y

        val cloudPath = Path()
        addCloudSilhouette(cloudPath, x, y, w, h)

        // Dimensional body gradient
        val bodyBrush = Brush.verticalGradient(
            colors = listOf(
                highlightColor.copy(alpha = alpha),
                fillColor.copy(alpha = alpha * 0.95f),
                shadowColor.copy(alpha = alpha * 0.85f)
            ),
            startY = y + h * 0.20f,
            endY = y + h * 0.88f
        )
        drawScope.drawPath(cloudPath, brush = bodyBrush)

        // Soft sun/moon rim light across the upper crest
        val rimPath = Path().apply {
            val rCenter = h * 0.38f
            val cxCenter = x + w * 0.50f
            val cyCenter = y + h * 0.36f
            addOval(Rect(cxCenter - rCenter, cyCenter - rCenter, cxCenter + rCenter, cyCenter + rCenter))

            val rRight = h * 0.30f
            val cxRight = x + w * 0.72f
            val cyRight = y + h * 0.48f
            addOval(Rect(cxRight - rRight, cyRight - rRight, cxRight + rRight, cyRight + rRight))
        }

        val rimAlpha = alpha * (if (isDarkNight) 0.30f else 0.45f)
        drawScope.drawPath(
            path = rimPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    highlightColor.copy(alpha = rimAlpha),
                    Color.Transparent
                ),
                startY = y + h * 0.15f,
                endY = y + h * 0.52f
            )
        )
    }

    /**
     * Draws the Google Weather-style Sun illustration at the calculated astronomical position.
     */
    fun drawSun(
        drawScope: DrawScope,
        center: Offset,
        bodyAlpha: Float,
        coronaPulse: Float,
        sunRayRotation: Float,
        dynamicAccent: Color,
        duskWarmth: Float,
        atmosphericGlow: Color
    ) {
        if (bodyAlpha <= 0.005f) return

        with(drawScope) {
            // 1. Multi-Stage Ambient Corona Halo
            val outerAuraRadius = 75.dp.toPx() * coronaPulse
            val midAuraRadius = 46.dp.toPx() * coronaPulse

            val warmGlow = lerp(Color(0xFFFBBC04), Color(0xFFFF7043), duskWarmth)
            val blendedAura = lerp(warmGlow, dynamicAccent, 0.15f)

            // Outer soft atmospheric bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blendedAura.copy(alpha = 0.32f * bodyAlpha),
                        atmosphericGlow.copy(alpha = 0.12f * bodyAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerAuraRadius
                ),
                radius = outerAuraRadius,
                center = center
            )

            // Mid warm halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blendedAura.copy(alpha = 0.45f * bodyAlpha),
                        blendedAura.copy(alpha = 0.15f * bodyAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = midAuraRadius
                ),
                radius = midAuraRadius,
                center = center
            )

            // 2. Eight Signature Google Weather Rounded Pill Rays
            val rayCount = 8
            val innerRayRadius = 24.dp.toPx() * (1f + 0.04f * (coronaPulse - 1f))
            val outerRayRadius = 33.dp.toPx() * (1f + 0.08f * (coronaPulse - 1f))
            val rayStrokeWidth = 4.0.dp.toPx()

            val rayColor = lerp(
                Color(0xFFFBBC04),
                lerp(Color(0xFFFFA000), dynamicAccent, 0.12f),
                duskWarmth
            )

            for (i in 0 until rayCount) {
                val angleDeg = i * (360.0 / rayCount) + (sunRayRotation * 0.22f)
                val angleRad = Math.toRadians(angleDeg)

                val rayStart = Offset(
                    center.x + (innerRayRadius * cos(angleRad)).toFloat(),
                    center.y + (innerRayRadius * sin(angleRad)).toFloat()
                )
                val rayEnd = Offset(
                    center.x + (outerRayRadius * cos(angleRad)).toFloat(),
                    center.y + (outerRayRadius * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = rayColor.copy(alpha = bodyAlpha * 0.95f),
                    start = rayStart,
                    end = rayEnd,
                    strokeWidth = rayStrokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Central Clean Sun Disk with warm gradient & soft specular highlight
            val coreRadius = 18.dp.toPx()
            val sunTopColor = lerp(Color(0xFFFFE082), Color(0xFFFFB74D), duskWarmth)
            val sunBottomColor = lerp(Color(0xFFFBBC04), Color(0xFFE65100), duskWarmth)

            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        sunTopColor.copy(alpha = bodyAlpha),
                        sunBottomColor.copy(alpha = bodyAlpha)
                    ),
                    startY = center.y - coreRadius,
                    endY = center.y + coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Inner specular gleam
            val specularOffset = Offset(center.x - 4.dp.toPx(), center.y - 4.dp.toPx())
            drawCircle(
                color = Color.White.copy(alpha = 0.40f * bodyAlpha),
                radius = 3.2.dp.toPx(),
                center = specularOffset
            )
        }
    }

    /**
     * Draws the Google Weather-style Moon illustration with true-phase illumination
     * and companion 4-point sparkle stars.
     */
    fun drawMoon(
        drawScope: DrawScope,
        center: Offset,
        moon: MoonInfo,
        bodyAlpha: Float,
        coronaPulse: Float,
        starTwinkle: Float,
        dynamicAccent: Color
    ) {
        if (bodyAlpha <= 0.005f) return

        with(drawScope) {
            val moonRadius = 20.dp.toPx()
            val baseMoonColor = Color(0xFFFFE082) // Google Weather lunar amber-yellow
            val moonColor = lerp(baseMoonColor, dynamicAccent, 0.10f)

            // 1. Ambient Lunar Aura
            val moonAuraRadius = 52.dp.toPx() * coronaPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moonColor.copy(alpha = 0.28f * bodyAlpha),
                        moonColor.copy(alpha = 0.08f * bodyAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = moonAuraRadius
                ),
                radius = moonAuraRadius,
                center = center
            )

            // 2. Google Weather Companion 4-Point Sparkle Stars
            // Star 1: Primary diamond sparkle beside the moon
            val sparkle1Center = Offset(center.x + 21.dp.toPx(), center.y - 14.dp.toPx())
            val sparkle1Radius = 5.2.dp.toPx() * starTwinkle
            val sparkle1Path = Path()
            addSparkleStar(sparkle1Path, sparkle1Center.x, sparkle1Center.y, sparkle1Radius)

            // Halo for sparkle 1
            drawCircle(
                color = moonColor.copy(alpha = 0.30f * starTwinkle * bodyAlpha),
                radius = sparkle1Radius * 1.8f,
                center = sparkle1Center
            )
            drawPath(
                path = sparkle1Path,
                color = Color.White.copy(alpha = 0.95f * bodyAlpha)
            )

            // Star 2: Subtle secondary micro-sparkle
            val sparkle2Center = Offset(center.x + 28.dp.toPx(), center.y + 10.dp.toPx())
            val sparkle2Radius = 2.8.dp.toPx() * (1.2f - starTwinkle).coerceIn(0.4f, 1.0f)
            val sparkle2Path = Path()
            addSparkleStar(sparkle2Path, sparkle2Center.x, sparkle2Center.y, sparkle2Radius)
            drawPath(
                path = sparkle2Path,
                color = moonColor.copy(alpha = 0.85f * bodyAlpha)
            )

            // 3. True-Phase Moon Body
            // Earthshine limb: delicate faint orb so the dark disk is softly perceivable
            drawCircle(
                color = moonColor.copy(alpha = 0.16f * bodyAlpha),
                radius = moonRadius,
                center = center
            )

            val illumination = moon.illumination.toFloat()
            val cosPhase = cos(2.0 * PI * moon.phase01).toFloat()

            if (illumination > 0.02f && bodyAlpha > 0f) {
                if (illumination > 0.98f) {
                    // Full Moon
                    drawCircle(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF9C4).copy(alpha = bodyAlpha),
                                moonColor.copy(alpha = bodyAlpha)
                            ),
                            startY = center.y - moonRadius,
                            endY = center.y + moonRadius
                        ),
                        radius = moonRadius,
                        center = center
                    )
                } else {
                    // Crescent or Gibbous phase with smooth mathematical terminator
                    val terminatorRx = (moonRadius * abs(cosPhase)).coerceAtLeast(0.5f)
                    val terminatorSweep =
                        if ((moon.waxing && cosPhase > 0f) || (!moon.waxing && cosPhase <= 0f)) 180f
                        else -180f

                    val litPath = Path().apply {
                        if (moon.waxing) {
                            moveTo(center.x, center.y - moonRadius)
                            arcTo(
                                Rect(center.x - moonRadius, center.y - moonRadius, center.x + moonRadius, center.y + moonRadius),
                                -90f, 180f, false
                            )
                        } else {
                            moveTo(center.x, center.y + moonRadius)
                            arcTo(
                                Rect(center.x - moonRadius, center.y - moonRadius, center.x + moonRadius, center.y + moonRadius),
                                90f, 180f, false
                            )
                        }
                        arcTo(
                            Rect(center.x - terminatorRx, center.y - moonRadius, center.x + terminatorRx, center.y + moonRadius),
                            if (moon.waxing) 90f else -90f,
                            terminatorSweep, false
                        )
                        close()
                    }

                    drawPath(
                        path = litPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFDE7).copy(alpha = bodyAlpha),
                                moonColor.copy(alpha = bodyAlpha)
                            ),
                            startY = center.y - moonRadius,
                            endY = center.y + moonRadius
                        )
                    )
                }
            }
        }
    }

    /**
     * Draws Google Weather-style 4-point diamond sparkle stars and celestial stardust.
     */
    fun drawStarfield(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        starOpacity: Float,
        starTwinkle: Float,
        shootingStarProgress: Float,
        dynamicAccent: Color
    ) {
        if (starOpacity <= 0.04f) return

        with(drawScope) {
            // Cosmic stardust bloom in the upper zenith
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lerp(Color(0x2080D8FF), dynamicAccent, 0.15f).copy(alpha = 0.16f * starOpacity),
                        Color(0x0E7986CB).copy(alpha = 0.08f * starOpacity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.48f, height * 0.12f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.48f, height * 0.12f)
            )

            // Calibrated celestial stars (xFrac, yFrac, radiusDp, isSparkle)
            val stars = listOf(
                StarDef(0.06f, 0.04f, 2.2f, true),
                StarDef(0.18f, 0.06f, 1.2f, false),
                StarDef(0.32f, 0.03f, 2.6f, true),
                StarDef(0.48f, 0.07f, 1.4f, false),
                StarDef(0.64f, 0.04f, 2.4f, true),
                StarDef(0.79f, 0.08f, 1.3f, false),
                StarDef(0.92f, 0.05f, 2.0f, true),
                StarDef(0.12f, 0.12f, 1.1f, false),
                StarDef(0.27f, 0.15f, 2.2f, true),
                StarDef(0.58f, 0.12f, 2.8f, true),
                StarDef(0.73f, 0.16f, 1.2f, false),
                StarDef(0.88f, 0.11f, 2.0f, true),
                StarDef(0.96f, 0.18f, 1.0f, false),
                StarDef(0.08f, 0.22f, 1.8f, false),
                StarDef(0.22f, 0.25f, 1.1f, false),
                StarDef(0.42f, 0.20f, 2.4f, true),
                StarDef(0.68f, 0.22f, 1.4f, false),
                StarDef(0.84f, 0.24f, 2.0f, true),
                StarDef(0.15f, 0.30f, 1.2f, false),
                StarDef(0.35f, 0.29f, 1.0f, false),
                StarDef(0.60f, 0.31f, 1.6f, false),
                StarDef(0.90f, 0.30f, 1.3f, false),
                StarDef(0.03f, 0.15f, 1.4f, false),
                StarDef(0.52f, 0.02f, 2.1f, true),
                StarDef(0.70f, 0.06f, 1.1f, false),
                StarDef(0.38f, 0.11f, 1.3f, false),
                StarDef(0.81f, 0.03f, 1.8f, true),
                StarDef(0.25f, 0.08f, 1.2f, false)
            )

            val starTints = listOf(Color.White, Color(0xFFCFE4FF), Color(0xFFFFE9C9))

            stars.forEachIndexed { idx, star ->
                val isAlternate = idx % 3 == 0
                val factor = if (isAlternate) starTwinkle else (1.2f - starTwinkle).coerceIn(0.25f, 1.0f)
                val starCenter = Offset(width * star.xFrac, height * star.yFrac)
                val starTint = starTints[idx % starTints.size]

                if (star.isSparkle && star.radiusDp >= 2.0f) {
                    // Google Weather 4-Point Diamond Sparkle Star
                    val sparkleRadius = (star.radiusDp * 1.8f * factor).dp.toPx()
                    val sparklePath = Path()
                    addSparkleStar(sparklePath, starCenter.x, starCenter.y, sparkleRadius)

                    // Soft aura
                    drawCircle(
                        color = starTint.copy(alpha = factor * starOpacity * 0.25f),
                        radius = sparkleRadius * 1.6f,
                        center = starCenter
                    )
                    drawPath(
                        path = sparklePath,
                        color = starTint.copy(alpha = factor * starOpacity * 0.92f)
                    )
                } else {
                    // Delicate magnitude star dot
                    drawCircle(
                        color = starTint.copy(alpha = factor * starOpacity * 0.85f),
                        radius = star.radiusDp.dp.toPx(),
                        center = starCenter
                    )
                }
            }

            // Rare shooting star streak with tapered gradient tail
            if (shootingStarProgress in 0.20f..0.36f) {
                val sp = (shootingStarProgress - 0.20f) / 0.16f
                val startX = width * (0.18f + 0.48f * sp)
                val startY = height * (0.03f + 0.12f * sp)
                val streakLen = 48.dp.toPx()
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
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }

    /**
     * Draws Google Weather signature angled rounded rain streaks and atmospheric vapor mist.
     */
    fun drawRain(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        rainProgress: Float,
        intensity: Float,
        dynamicAccent: Color
    ) {
        if (intensity <= 0.01f) return

        with(drawScope) {
            val sin15 = 0.2588f
            val cos15 = 0.9659f
            val streakLen = 16.dp.toPx()
            val fallDistance = height * 0.55f
            val rainColor = lerp(Color(0xFF80D8FF), dynamicAccent, 0.20f)

            // 24 angled rain streaks distributed naturally
            val streakSeeds = listOf(
                Pair(0.08f, 0.18f), Pair(0.15f, 0.35f), Pair(0.22f, 0.12f), Pair(0.28f, 0.42f),
                Pair(0.35f, 0.22f), Pair(0.42f, 0.08f), Pair(0.48f, 0.30f), Pair(0.55f, 0.15f),
                Pair(0.62f, 0.38f), Pair(0.68f, 0.25f), Pair(0.75f, 0.10f), Pair(0.82f, 0.32f),
                Pair(0.88f, 0.20f), Pair(0.94f, 0.40f), Pair(0.12f, 0.50f), Pair(0.25f, 0.58f),
                Pair(0.38f, 0.48f), Pair(0.50f, 0.55f), Pair(0.65f, 0.52f), Pair(0.78f, 0.46f),
                Pair(0.85f, 0.58f), Pair(0.18f, 0.28f), Pair(0.45f, 0.19f), Pair(0.72f, 0.18f)
            )

            streakSeeds.forEachIndexed { idx, (seedX, seedY) ->
                val streakStagger = (idx * 0.0416f)
                val currProgress = (rainProgress + streakStagger) % 1.0f
                val currY = height * seedY + currProgress * fallDistance
                val currX = width * seedX - (currProgress * fallDistance * sin15 / cos15)

                // Fade in near top, fade out near horizon
                val yFraction = (currY / (height * 0.70f)).coerceIn(0f, 1f)
                val alphaEnvelope = sin(yFraction * PI.toFloat()).coerceIn(0f, 1f)
                val streakAlpha = alphaEnvelope * intensity * 0.45f

                if (streakAlpha > 0.02f) {
                    val start = Offset(currX, currY)
                    val end = Offset(currX - streakLen * sin15, currY + streakLen * cos15)

                    drawLine(
                        color = rainColor.copy(alpha = streakAlpha),
                        start = start,
                        end = end,
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    private fun lerp(c1: Color, c2: Color, f: Float): Color {
        val fraction = f.coerceIn(0f, 1f)
        return Color(
            red = c1.red + (c2.red - c1.red) * fraction,
            green = c1.green + (c2.green - c1.green) * fraction,
            blue = c1.blue + (c2.blue - c1.blue) * fraction,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * fraction
        )
    }

    private data class StarDef(
        val xFrac: Float,
        val yFrac: Float,
        val radiusDp: Float,
        val isSparkle: Boolean
    )
}
