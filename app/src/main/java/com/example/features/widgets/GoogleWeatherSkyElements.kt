package com.example.features.widgets

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.features.prayer.calculation.CelestialMath.MoonInfo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Atmospheric vector primitives for the immersive sky.
 *
 * The shapes are deliberately restrained: light, haze and celestial bodies,
 * not weather-app iconography. Nour's chrome is opaque, typographic and
 * iOS-styled, so the sky reads as atmosphere rather than a cartoon scene:
 * - soft, low-contrast cloud masses without rim highlights
 * - a luminous Sun disc and bloom (no icon-style rays or specular dot)
 * - the Moon at its true phase with earthshine
 * - plain star dots (no 4-point sparkle glyphs)
 * - angled rounded rain streaks for the (currently disabled) precipitation layer
 */
object GoogleWeatherSkyElements {

    /**
     * Adds a soft cloud silhouette: a rounded base capsule with three
     * overlapping lobes. The shape is only ever filled with a low-alpha
     * gradient, so it reads as a distant mass rather than clip art.
     */
    fun addCloudSilhouette(path: Path, x: Float, y: Float, w: Float, h: Float) {
        path.apply {
            // Base rounded capsule
            addRoundRect(
                RoundRect(
                    left = x + w * 0.06f,
                    top = y + h * 0.46f,
                    right = x + w * 0.94f,
                    bottom = y + h * 0.84f,
                    cornerRadius = CornerRadius(h * 0.19f, h * 0.19f)
                )
            )
            // Left puff
            val rLeft = h * 0.25f
            val cxLeft = x + w * 0.26f
            val cyLeft = y + h * 0.56f
            addOval(Rect(cxLeft - rLeft, cyLeft - rLeft, cxLeft + rLeft, cyLeft + rLeft))

            // Center dome (slightly off-centre; a perfectly symmetric crown
            // reads as an icon)
            val rCenter = h * 0.33f
            val cxCenter = x + w * 0.48f
            val cyCenter = y + h * 0.40f
            addOval(Rect(cxCenter - rCenter, cyCenter - rCenter, cxCenter + rCenter, cyCenter + rCenter))

            // Right shoulder puff
            val rRight = h * 0.27f
            val cxRight = x + w * 0.72f
            val cyRight = y + h * 0.50f
            addOval(Rect(cxRight - rRight, cyRight - rRight, cxRight + rRight, cyRight + rRight))

            // Soft belly: a wide, shallow bulge under the middle of the base.
            // Without it the capsule left a dead-straight bottom edge, which is
            // the one cue that still made the clouds read as vector stickers.
            val bellyRx = w * 0.38f
            val bellyRy = h * 0.11f
            val bellyCy = y + h * 0.84f
            addOval(
                Rect(
                    x + w * 0.5f - bellyRx,
                    bellyCy - bellyRy,
                    x + w * 0.5f + bellyRx,
                    bellyCy + bellyRy
                )
            )
        }
    }

    /**
     * Size- and colour-dependent geometry for one cloud, precomputed in local
     * `(0,0)-(width,height)` space so a frame only translates and draws it.
     */
    class CloudArt internal constructor(
        internal val silhouette: Path,
        internal val bodyBrush: Brush,
        internal val midFeatherBrush: Brush,
        internal val outerFeatherBrush: Brush,
        internal val pivot: Offset
    )

    /**
     * The three-stop cloud palette (lit crown, body, underside). Bundled
     * because the stops always travel together and a four-parameter builder
     * keeps detekt's `LongParameterList` rule quiet without suppression.
     */
    class CloudColors internal constructor(
        internal val fill: Color,
        internal val highlight: Color,
        internal val shadow: Color
    )

    /**
     * Builds a cloud once. Drawing the result translated by `(x, y)` is
     * pixel-equivalent to the earlier draw-at-absolute-coordinates form: a
     * translation shifts path points and gradient coordinates by exactly the
     * same amount. The caller rebuilds this only when colours/alpha/size
     * change, never per animation frame.
     *
     * The body alpha is split across three concentric, vertically stretched
     * passes (68 / 22 / 10 %). Their sum keeps the old interior density, while
     * the two outer passes leave a 6 px soft skirt instead of a traceable
     * outline — the hard edge was what made the clouds read as vector icons.
     */
    fun buildCloudArt(
        width: Float,
        height: Float,
        colors: CloudColors,
        alpha: Float
    ): CloudArt {
        val cloudPath = Path()
        addCloudSilhouette(cloudPath, 0f, 0f, width, height)

        fun body(alphaScale: Float) = Brush.verticalGradient(
            colors = listOf(
                colors.highlight.copy(alpha = alpha * alphaScale),
                colors.fill.copy(alpha = alpha * 0.95f * alphaScale),
                colors.shadow.copy(alpha = alpha * 0.85f * alphaScale)
            ),
            startY = height * 0.20f,
            endY = height * 0.88f
        )

        return CloudArt(
            silhouette = cloudPath,
            bodyBrush = body(0.68f),
            midFeatherBrush = body(0.22f),
            outerFeatherBrush = body(0.10f),
            pivot = Offset(width * 0.5f, height * 0.5f)
        )
    }

    /**
     * Draws a precomputed [CloudArt] at [topLeft], feathering the silhouette
     * with two stretched passes. Allocating nothing per frame beyond the draw
     * calls themselves.
     */
    fun drawCloud(
        drawScope: DrawScope,
        art: CloudArt,
        topLeft: Offset
    ) {
        with(drawScope) {
            translate(topLeft.x, topLeft.y) {
                // Skirts are a fraction of the cloud, not a couple of pixels,
                // but stay modest: stretching further ballooned each lobe into
                // its own soft bubble, a bokeh look the starfield just shed.
                scale(scaleX = 1.05f, scaleY = 1.18f, pivot = art.pivot) {
                    drawPath(art.silhouette, brush = art.outerFeatherBrush)
                }
                scale(scaleX = 1.025f, scaleY = 1.09f, pivot = art.pivot) {
                    drawPath(art.silhouette, brush = art.midFeatherBrush)
                }
                drawPath(art.silhouette, brush = art.bodyBrush)
            }
        }
    }

    /** Precomputed geometry/brushes for the Sun (rebuilt per minute-ish). */
    class SunArt internal constructor(
        internal val outerAuraBrush: Brush,
        internal val midAuraBrush: Brush,
        internal val diskBrush: Brush,
        internal val outerAuraRadius: Float,
        internal val midAuraRadius: Float,
        internal val diskRadius: Float
    )

    /**
     * Builds the Sun's brushes and constants. Radii here are the un-pulsed base
     * values; [drawSun] applies the live corona pulse as a uniform scale, which
     * is mathematically identical to rebuilding each gradient brush at the
     * pulsed radius.
     *
     * The Sun is drawn as light, not as an icon: a soft-edged luminous disc
     * inside a two-stage bloom. The old eight pill-shaped rays and white
     * specular dot belonged to a weather illustration, not to Nour's flat,
     * typographic surfaces.
     */
    fun buildSunArt(
        density: Density,
        center: Offset,
        bodyAlpha: Float,
        duskWarmth: Float,
        dynamicAccent: Color,
        atmosphericGlow: Color
    ): SunArt {
        with(density) {
            // 1. Multi-stage ambient corona
            val outerAuraRadius = 84.dp.toPx()
            val midAuraRadius = 50.dp.toPx()

            // A high sun is white light, not an amber lamp: the corona only
            // turns warm as the sun approaches the horizon. The old fixed
            // amber glow, laid over a blue noon sky, mixed into an olive disc
            // with a visible circular edge.
            val warmGlow = lerp(Color(0xFFFFF6E4), Color(0xFFFF9E5A), duskWarmth)
            val blendedAura = lerp(warmGlow, dynamicAccent, 0.12f)

            val outerAuraBrush = Brush.radialGradient(
                colors = listOf(
                    blendedAura.copy(alpha = 0.20f * bodyAlpha),
                    atmosphericGlow.copy(alpha = 0.07f * bodyAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = outerAuraRadius
            )
            val midAuraBrush = Brush.radialGradient(
                colors = listOf(
                    blendedAura.copy(alpha = 0.30f * bodyAlpha),
                    blendedAura.copy(alpha = 0.10f * bodyAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = midAuraRadius
            )

            // 2. Luminous disc: a warm-white core whose rim feathers into the
            // bloom, so the limb reads as atmosphere rather than a sticker.
            val diskRadius = 23.dp.toPx()
            val coreColor = lerp(Color(0xFFFFFDF2), Color(0xFFFFF3E0), duskWarmth)
            // Only the low sun is amber. Keeping a saturated gold rim at noon
            // turned the disc into a glowing marble over the blue sky; in full
            // daylight the limb is simply a warm white feather.
            val rimColor = lerp(Color(0xFFFFF4DA), Color(0xFFFF9E5A), duskWarmth)
            val diskBrush = Brush.radialGradient(
                colors = listOf(
                    coreColor.copy(alpha = bodyAlpha),
                    rimColor.copy(alpha = bodyAlpha * 0.96f),
                    rimColor.copy(alpha = bodyAlpha * 0.55f),
                    Color.Transparent
                ),
                center = center,
                radius = diskRadius
            )

            return SunArt(
                outerAuraBrush = outerAuraBrush,
                midAuraBrush = midAuraBrush,
                diskBrush = diskBrush,
                outerAuraRadius = outerAuraRadius,
                midAuraRadius = midAuraRadius,
                diskRadius = diskRadius
            )
        }
    }

    /**
     * Draws the Sun as a luminous disc inside its bloom. The corona pulse is a
     * barely-perceptible uniform scale, not a bounce.
     */
    fun drawSun(
        drawScope: DrawScope,
        art: SunArt,
        center: Offset,
        coronaPulse: Float
    ) {
        with(drawScope) {
            scale(scale = coronaPulse, pivot = center) {
                drawCircle(
                    brush = art.outerAuraBrush,
                    radius = art.outerAuraRadius,
                    center = center
                )
                drawCircle(
                    brush = art.midAuraBrush,
                    radius = art.midAuraRadius,
                    center = center
                )
                drawCircle(
                    brush = art.diskBrush,
                    radius = art.diskRadius,
                    center = center
                )
            }
        }
    }

    /** Precomputed geometry/brushes for the moon at its true phase. */
    class MoonArt internal constructor(
        internal val aura: MoonAura,
        internal val surface: MoonSurface,
        internal val lit: MoonLit
    ) {
        /** The pulse-scaled halo drawn behind the disc. */
        class MoonAura internal constructor(
            internal val brush: Brush,
            internal val radius: Float
        )

        /** The whole disc as seen unlit: body radius plus the earthshine limb. */
        class MoonSurface internal constructor(
            internal val radius: Float,
            internal val earthshineColor: Color
        )

        /** The illuminated part: a full-disk gradient, or a terminator path and fill. */
        class MoonLit internal constructor(
            internal val fullDiskBrush: Brush?,
            internal val path: Path?,
            internal val brush: Brush?
        )
    }

    /** Builds the moon's phase geometry once; [drawMoon] only applies the live pulse. */
    fun buildMoonArt(
        density: Density,
        center: Offset,
        moon: MoonInfo,
        bodyAlpha: Float,
        dynamicAccent: Color
    ): MoonArt {
        with(density) {
            val moonRadius = 20.dp.toPx()
            // Moonlight is neutral. The old amber (#FFE082) read as a lit bulb,
            // especially against the navy night, so the body keeps only a hint
            // of warmth and the gradient lands on a cream shadow.
            val baseMoonColor = Color(0xFFFFF6DE)
            val moonColor = lerp(baseMoonColor, dynamicAccent, 0.06f)

            // 1. Ambient lunar aura
            val moonAuraRadius = 52.dp.toPx()
            val auraBrush = Brush.radialGradient(
                colors = listOf(
                    moonColor.copy(alpha = 0.28f * bodyAlpha),
                    moonColor.copy(alpha = 0.08f * bodyAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = moonAuraRadius
            )

            // 2. True-phase moon body
            val illumination = moon.illumination.toFloat()
            val cosPhase = cos(2.0 * PI * moon.phase01).toFloat()

            var fullDiskBrush: Brush? = null
            var litPath: Path? = null
            var litBrush: Brush? = null
            if (illumination > 0.02f && bodyAlpha > 0f) {
                if (illumination > 0.98f) {
                    // Full Moon
                    fullDiskBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF9C4).copy(alpha = bodyAlpha),
                            moonColor.copy(alpha = bodyAlpha)
                        ),
                        startY = center.y - moonRadius,
                        endY = center.y + moonRadius
                    )
                } else {
                    // Crescent or Gibbous phase with smooth mathematical terminator
                    val terminatorRx = (moonRadius * abs(cosPhase)).coerceAtLeast(0.5f)
                    val terminatorSweep =
                        if ((moon.waxing && cosPhase > 0f) || (!moon.waxing && cosPhase <= 0f)) 180f
                        else -180f

                    litPath = Path().apply {
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

                    litBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFDE7).copy(alpha = bodyAlpha),
                            moonColor.copy(alpha = bodyAlpha)
                        ),
                        startY = center.y - moonRadius,
                        endY = center.y + moonRadius
                    )
                }
            }

            return MoonArt(
                aura = MoonArt.MoonAura(auraBrush, moonAuraRadius),
                surface = MoonArt.MoonSurface(
                    radius = moonRadius,
                    earthshineColor = moonColor.copy(alpha = 0.16f * bodyAlpha)
                ),
                lit = MoonArt.MoonLit(fullDiskBrush, litPath, litBrush)
            )
        }
    }

    /**
     * Draws the moon at its true phase. The companion four-point sparkle stars
     * were removed: they read as decorative glyphs next to the app's restrained
     * typography. The sky's own starfield supplies the surrounding light.
     */
    fun drawMoon(
        drawScope: DrawScope,
        art: MoonArt,
        center: Offset,
        coronaPulse: Float
    ) {
        with(drawScope) {
            // 1. Ambient lunar aura (uniform pulse about the moon centre)
            scale(scale = coronaPulse, pivot = center) {
                drawCircle(
                    brush = art.aura.brush,
                    radius = art.aura.radius,
                    center = center
                )
            }

            // 2. True-phase moon body
            // Earthshine limb: delicate faint orb so the dark disk is softly perceivable
            drawCircle(
                color = art.surface.earthshineColor,
                radius = art.surface.radius,
                center = center
            )

            val fullDiskBrush = art.lit.fullDiskBrush
            val litPath = art.lit.path
            val litBrush = art.lit.brush
            if (fullDiskBrush != null) {
                drawCircle(
                    brush = fullDiskBrush,
                    radius = art.surface.radius,
                    center = center
                )
            } else if (litPath != null && litBrush != null) {
                drawPath(
                    path = litPath,
                    brush = litBrush
                )
            }
        }
    }

    /**
     * Static starfield layout `(xFrac, yFrac, radiusDp)`, hoisted so no per-frame
     * star table is allocated. All stars are plain dots; the sky deliberately
     * has no four-point "sparkle" glyphs. The twinkle factor still modulates
     * each star live at draw time, in four phase groups.
     */
    private val STARS = listOf(
        StarDef(0.06f, 0.04f, 2.2f),
        StarDef(0.18f, 0.06f, 1.2f),
        StarDef(0.32f, 0.03f, 2.6f),
        StarDef(0.48f, 0.07f, 1.4f),
        StarDef(0.64f, 0.04f, 2.4f),
        StarDef(0.79f, 0.08f, 1.3f),
        StarDef(0.92f, 0.05f, 2.0f),
        StarDef(0.12f, 0.12f, 1.1f),
        StarDef(0.27f, 0.15f, 2.2f),
        StarDef(0.58f, 0.12f, 2.8f),
        StarDef(0.73f, 0.16f, 1.2f),
        StarDef(0.88f, 0.11f, 2.0f),
        StarDef(0.96f, 0.18f, 1.0f),
        StarDef(0.08f, 0.22f, 1.8f),
        StarDef(0.22f, 0.25f, 1.1f),
        StarDef(0.42f, 0.20f, 2.4f),
        StarDef(0.68f, 0.22f, 1.4f),
        StarDef(0.84f, 0.24f, 2.0f),
        StarDef(0.15f, 0.30f, 1.2f),
        StarDef(0.35f, 0.29f, 1.0f),
        StarDef(0.60f, 0.31f, 1.6f),
        StarDef(0.90f, 0.30f, 1.3f),
        StarDef(0.03f, 0.15f, 1.4f),
        StarDef(0.52f, 0.02f, 2.1f),
        StarDef(0.70f, 0.06f, 1.1f),
        StarDef(0.38f, 0.11f, 1.3f),
        StarDef(0.81f, 0.03f, 1.8f),
        StarDef(0.25f, 0.08f, 1.2f),
        // Faint lower-sky stars: the tall stretched hero would otherwise leave
        // the middle band as an empty wash between the cloud shelf and the
        // countdown.
        StarDef(0.11f, 0.36f, 0.9f),
        StarDef(0.34f, 0.41f, 1.0f),
        StarDef(0.55f, 0.37f, 0.8f),
        StarDef(0.72f, 0.44f, 0.9f),
        StarDef(0.86f, 0.39f, 0.8f),
        StarDef(0.47f, 0.46f, 0.7f)
    )

    private val STAR_TINTS = listOf(Color.White, Color(0xFFCFE4FF), Color(0xFFFFE9C9))

    /** Precomputed starfield geometry/brushes (rebuilt only when size/opacity/theme change). */
    class StarfieldArt internal constructor(
        internal val stars: List<Star>,
        internal val bloomBrush: Brush,
        internal val bloomCenter: Offset,
        internal val bloomRadius: Float
    ) {
        class Star internal constructor(
            internal val center: Offset,
            internal val radiusPx: Float,
            internal val tint: Color
        )
    }

    /** Builds the starfield once per layout/opacity change; [drawStarfield] only twinkles it. */
    fun buildStarfieldArt(
        density: Density,
        width: Float,
        height: Float,
        starOpacity: Float,
        dynamicAccent: Color
    ): StarfieldArt {
        with(density) {
            // Cosmic stardust bloom in the upper zenith
            val bloomCenter = Offset(width * 0.48f, height * 0.12f)
            val bloomRadius = width * 0.65f
            val bloomBrush = Brush.radialGradient(
                colors = listOf(
                    lerp(Color(0x2080D8FF), dynamicAccent, 0.15f).copy(alpha = 0.16f * starOpacity),
                    Color(0x0E7986CB).copy(alpha = 0.08f * starOpacity),
                    Color.Transparent
                ),
                center = bloomCenter,
                radius = bloomRadius
            )

            val stars = STARS.mapIndexed { idx, star ->
                StarfieldArt.Star(
                    center = Offset(width * star.xFrac, height * star.yFrac),
                    radiusPx = star.radiusDp.dp.toPx(),
                    tint = STAR_TINTS[idx % STAR_TINTS.size]
                )
            }

            return StarfieldArt(stars, bloomBrush, bloomCenter, bloomRadius)
        }
    }

    /**
     * Draws the starfield. [twinklePhase] advances continuously (0..2π);
     * four phase buckets keep the field from pulsing in unison, which was the
     * synthetic-looking part of the old two-group twinkle.
     */
    fun drawStarfield(
        drawScope: DrawScope,
        art: StarfieldArt,
        starOpacity: Float,
        twinklePhase: Float
    ) {
        if (starOpacity <= 0.04f) return

        with(drawScope) {
            // Cosmic stardust bloom in the upper zenith
            drawCircle(
                brush = art.bloomBrush,
                radius = art.bloomRadius,
                center = art.bloomCenter
            )

            art.stars.forEachIndexed { idx, star ->
                val bucket = idx % 4
                val factor = (0.65f + 0.25f * sin(twinklePhase + bucket * (PI / 2).toFloat()))
                    .coerceIn(0.30f, 0.95f)

                // Plain dots only. The old 3.6x halo around the brightest stars
                // rendered as a hard-edged grey disc — bokeh, not sky — at hero
                // size, so the star reads as light on a gradient instead.
                drawCircle(
                    color = star.tint.copy(alpha = factor * starOpacity * 0.85f),
                    radius = star.radiusPx,
                    center = star.center
                )
            }
        }
    }

    /** Static rain streak seeds, hoisted so enabling rain costs nothing per frame to allocate. */
    private val RAIN_STREAK_SEEDS = listOf(
        Pair(0.08f, 0.18f), Pair(0.15f, 0.35f), Pair(0.22f, 0.12f), Pair(0.28f, 0.42f),
        Pair(0.35f, 0.22f), Pair(0.42f, 0.08f), Pair(0.48f, 0.30f), Pair(0.55f, 0.15f),
        Pair(0.62f, 0.38f), Pair(0.68f, 0.25f), Pair(0.75f, 0.10f), Pair(0.82f, 0.32f),
        Pair(0.88f, 0.20f), Pair(0.94f, 0.40f), Pair(0.12f, 0.50f), Pair(0.25f, 0.58f),
        Pair(0.38f, 0.48f), Pair(0.50f, 0.55f), Pair(0.65f, 0.52f), Pair(0.78f, 0.46f),
        Pair(0.85f, 0.58f), Pair(0.18f, 0.28f), Pair(0.45f, 0.19f), Pair(0.72f, 0.18f)
    )

    /**
     * Draws angled rounded rain streaks and atmospheric vapor mist. No caller
     * enables precipitation today; the layer is kept for a future weather hook.
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
            RAIN_STREAK_SEEDS.forEachIndexed { idx, (seedX, seedY) ->
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
        val radiusDp: Float
    )
}
