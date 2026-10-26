package com.example.features.qibla.ui

import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.hardware.SensorManager
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.features.prayer.models.UserLocation
import com.example.features.qibla.QiblaCalibrationHintLogic
import com.example.features.qibla.QiblaSensorManager
import kotlin.math.cos
import kotlin.math.sin

import com.example.localization.LocalAppStrings
import com.example.localization.LocalUseEasternNumerals
import com.example.localization.Numerals
import com.example.localization.formatNumerals
import com.example.features.location.LocationPermissionCard
import com.example.ui.components.LocalFloatingBottomExtra
import com.example.ui.components.NourCapsuleButton
import com.example.ui.components.NourCircularIconButton

/** Pre-measured cardinal label: angle on the dial plus its cached text layout. */
private data class CardinalLabel(val angleDeg: Double, val layout: TextLayoutResult)

@Composable
fun QiblaScreen(
    location: UserLocation,
    onLocationUpdated: ((UserLocation) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember { QiblaSensorManager(context) }

    DisposableEffect(location) {
        sensorManager.setLocation(location.latitude, location.longitude)
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    val qiblaState by sensorManager.qiblaState.collectAsState()
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var manualMode by remember { mutableStateOf(!qiblaState.hasMagnetometer) }
    var manualHeading by remember { mutableFloatStateOf(0f) }
    val strings = LocalAppStrings.current
    val useEasternNumerals = LocalUseEasternNumerals.current

    val effectiveHeading = if (manualMode) manualHeading else qiblaState.deviceHeading
    val relativeAngle = (qiblaState.qiblaBearing - effectiveHeading + 360f) % 360f
    val isFacingQibla = !manualMode && qiblaState.hasHeading && qiblaState.hasMagnetometer &&
        qiblaState.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM &&
        qiblaState.isDeviceFlat && (relativeAngle >= 357f || relativeAngle <= 3f)

    // NUR-037: additive calibration guidance. The status line above already
    // says "calibration needed" when accuracy drops; this hint adds the
    // missing "how to fix it". Gated to real sensor readings (never manual
    // mode, never sensors missing) and only below ACCURACY_MEDIUM. Pure
    // decision logic lives in QiblaCalibrationHintLogic and stays JVM-tested.
    val showCalibrationHint = QiblaCalibrationHintLogic.shouldShowHint(
        manualMode = manualMode,
        hasMagnetometer = qiblaState.hasMagnetometer,
        hasHeading = qiblaState.hasHeading,
        accuracyInt = qiblaState.accuracy
    )

    val animatedRotation by animateFloatAsState(
        targetValue = -effectiveHeading,
        // Reduced motion: settle on the target heading immediately instead of
        // springing. The compass still tracks, it just does not overshoot.
        animationSpec = if (NourMotion.reduced) tween(0) else spring(stiffness = 300f),
        label = "compass_rotation"
    )

    val scrollState = rememberScrollState()

    // --- NUR-009: one-handed reachability ----------------------------------------------------
    // The dial absorbs all of the page's leftover slack (Salah-screen stretch
    // precedent): everything above the dial is measured as one stack, the
    // sun/manual/metrics cards below it as another, and the dial is stretched
    // so the last card (the heading readout — the value a standing user wants)
    // ends at the reserve line instead of being buried under the floating pill.
    // When the content does not fit (large fonts, tiny windows) the dial is
    // coerced back to its natural size and the page scrolls exactly as before.
    val density = LocalDensity.current
    var upperTopPx by remember { mutableFloatStateOf(0f) }
    var upperHeightPx by remember { mutableFloatStateOf(0f) }
    var lowerHeightPx by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
    val bottomReserve = LocalFloatingBottomExtra.current
    // Natural dial size: square, capped at NourSize.compass, centred.
    val compassNatural = minOf(maxWidth, NourSize.compass)
    val upperTop = with(density) { upperTopPx.toDp() }
    val upperHeight = with(density) { upperHeightPx.toDp() }
    val lowerHeight = with(density) { lowerHeightPx.toDp() }
    // Dial height = window minus everything that is not the dial: the upper
    // stack, the lower stack, the two outer lg gaps around the dial, and the
    // bottom reserve. Measured positions are scroll-content coordinates so
    // the computation is stable while the (now rare) scroll is mid-flight.
    val stretchedDialHeight = (
        maxHeight - bottomReserve - upperTop - upperHeight -
            NourSpace.lg * 2 - lowerHeight
        ).coerceAtLeast(compassNatural)
    // Gate on the first measurements landing: before the upper stack reports
    // its position there is nothing to stretch against (avoid a 0→huge flash).
    val dialStretched = upperTopPx > 0f && stretchedDialHeight > compassNatural

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            // Horizontal margin comes from the global NourContentFrame gutter.
            // Bottom is absolute: the system inset + the bar's band plus its
            // gap below and an 8 dp breath above. The last row rests one small
            // breath above the band's edge — readable, never hidden — while
            // scrolling rows sweep under the pill and the gesture area.
            .padding(top = NourSpace.lg, bottom = LocalFloatingBottomExtra.current),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
        // --- NUR-009: upper stack (everything above the dial), measured ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    upperTopPx = coordinates.positionInRoot().y + scrollState.value
                    upperHeightPx = coordinates.size.height.toFloat()
                },
            verticalArrangement = Arrangement.spacedBy(NourSpace.md)
        ) {
            // Minimal header: the dial below is the page's focal point, so the
            // title, city, honesty chip and calibration guide share one
            // unframed row instead of a card.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.qiblaTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("qibla_header_title")
                    )
                    Text(
                        text = "${location.cityName} ${strings.kaabaSuffix}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(NourSpace.sm))

                // Sensor Accuracy Chip. Weighting the chip as well as the
                // title keeps the title from collapsing to zero width at
                // 200% font or in long translations. The chip stacks to a
                // second line before it ellipsizes.
                Row(
                    modifier = Modifier.weight(1.3f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    val isHighAccuracy = qiblaState.hasHeading &&
                        qiblaState.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH
                    val accuracyText = when {
                        manualMode -> strings.accuracyManual
                        !qiblaState.hasHeading -> strings.accuracyWaiting
                        isHighAccuracy -> strings.accuracyHigh
                        qiblaState.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> strings.accuracyMedium
                        else -> strings.accuracyCalibrate
                    }
                    val accuracyContainer = if (isHighAccuracy) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                    val accuracyContent = if (isHighAccuracy) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val dotColor = if (isHighAccuracy) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        color = accuracyContainer,
                        shape = PillShape,
                        border = null,
                        modifier = Modifier.testTag("qibla_accuracy_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = NourSpace.md, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(NourSpace.sm / 2)
                                    .background(dotColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = accuracyText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = accuracyContent,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(NourSpace.sm))

                NourCircularIconButton(
                    onClick = { showCalibrationDialog = true },
                    contentDescription = strings.calibrationGuide
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(NourSize.smallIcon)
                    )
                }
            }

            // One honest status line carries manual mode, missing/refused
            // sensors, calibration and tilt. The detailed sensor/declination
            // pill and the tilt card were removed: they repeated this line and
            // crowded the page.
            Text(
                text = when {
                    manualMode -> strings.qiblaModeManual
                    !qiblaState.hasMagnetometer -> strings.qiblaNoSensor
                    qiblaState.registrationFailed -> strings.compassUnavailable
                    !qiblaState.hasHeading -> strings.qiblaWaiting
                    // The removed tilt card's advice now lives here, ahead of
                    // calibration: a tilted phone is the first thing to fix,
                    // and the figure-8 hint below waits for a flat device.
                    !qiblaState.isDeviceFlat -> strings.qiblaHoldFlatShort
                    qiblaState.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ->
                        strings.qiblaNeedCalibration
                    else -> strings.qiblaEstimated
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().testTag("qibla_mode_status")
            )

            // Location Permission & GPS acquisition card if location not yet auto-detected
            if (!location.isAutoDetected && onLocationUpdated != null) {
                LocationPermissionCard(
                    currentLocation = location,
                    onLocationDetected = onLocationUpdated
                )
            }

            // Facing Qibla Banner
            AnimatedVisibility(visible = isFacingQibla) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CardExpressiveShape,
                    border = BorderStroke(NourSize.selectedBorder, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("facing_qibla_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = NourSpace.lg, horizontal = NourSpace.lg),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = strings.facingQibla,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(NourSpace.sm))
                        Text(
                            text = strings.estimatedAlignment,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            // NUR-037: figure-8 calibration hint. Plain conditional, not
            // AnimatedVisibility — adding motion for guidance text would fight
            // the reduced-motion contract, and the banner decisions above are
            // untouched. Shown only while sensors are live but degraded.
            if (showCalibrationHint && qiblaState.isDeviceFlat) {
                Text(
                    text = strings.qiblaCalibrationHint,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qibla_calibration_hint")
                )
            }

        } // --- end NUR-009 upper stack ---

        // Compass Rose Dial with Kaaba Pointer
        // NUR-009: the dial is the stretch absorber — it grows to hold the
        // page's slack (so the heading readout docks near the bottom reserve)
        // whenever everything fits; when the content overflows it stays at its
        // natural square size and the page scrolls as before. The rose Canvas
        // and the central hub below draw at the box's width either way, so the
        // dial's own content is untouched by the stretching.
        val dialGeometry = if (dialStretched) {
            Modifier
                .fillMaxWidth()
                .widthIn(max = NourSize.compass)
                .height(stretchedDialHeight)
        } else {
            Modifier
                .widthIn(max = NourSize.compass).fillMaxWidth().aspectRatio(1f)
        }
        val dialCore = Modifier
            .semantics { contentDescription = Numerals.convert("${strings.qiblaHeading} ${qiblaState.qiblaBearing.toInt()}°", useEasternNumerals) }
            .testTag("qibla_compass_dial")
        Box(
            modifier = dialGeometry.then(dialCore),
            contentAlignment = Alignment.Center
        ) {
            val dialBorderColor = MaterialTheme.colorScheme.outlineVariant
            val tickColor = MaterialTheme.colorScheme.outline
            val northColor = MaterialTheme.colorScheme.primary
            // Cardinal text is information, not decoration: use onSurfaceVariant,
            // not the lighter outline role (which can fall below 4.5:1 on the
            // grouped light background).
            val southColor = MaterialTheme.colorScheme.onSurfaceVariant
            val needleColor = MaterialTheme.colorScheme.primary
            val sunIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            val hubColor = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val hubTextColor = if (isFacingQibla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            val kaabaGoldColor = MaterialTheme.colorScheme.primary
            val kaabaCenterColor = MaterialTheme.colorScheme.onPrimary
            val textMeasurer = rememberTextMeasurer()

            // The compass redraws on every sensor frame (50 Hz). Text layouts for
            // N/E/S/W are measured once per theme/density change and reused; only
            // their position on the dial is recomputed while drawing.
            // Cardinal labels come from the shared type scale (labelLarge /
            // labelMedium); only the direction colour varies, so the dial reads
            // like the rest of the app instead of carrying its own text styles.
            val cardinalMajorStyle = MaterialTheme.typography.labelLarge
            val cardinalMinorStyle = MaterialTheme.typography.labelMedium
            val cardinalLabels = remember(textMeasurer, northColor, southColor, cardinalMajorStyle, cardinalMinorStyle) {
                // Compass cardinals stay Latin by convention (as on physical
                // compasses); this is a direction instrument, not a
                // translatable body label.
                listOf(
                    CardinalLabel(
                        angleDeg = 0.0,
                        layout = textMeasurer.measure(
                            text = "N",
                            style = cardinalMajorStyle.copy(color = northColor)
                        )
                    ),
                    CardinalLabel(
                        angleDeg = 90.0,
                        layout = textMeasurer.measure(
                            text = "E",
                            style = cardinalMinorStyle.copy(color = southColor)
                        )
                    ),
                    CardinalLabel(
                        angleDeg = 180.0,
                        layout = textMeasurer.measure(
                            text = "S",
                            style = cardinalMinorStyle.copy(color = southColor)
                        )
                    ),
                    CardinalLabel(
                        angleDeg = 270.0,
                        layout = textMeasurer.measure(
                            text = "W",
                            style = cardinalMinorStyle.copy(color = southColor)
                        )
                    )
                )
            }

            // Rotating Compass Rose Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize().padding(NourSpace.lg)
                    .rotate(animatedRotation)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Thin outer ring
                drawCircle(
                    color = dialBorderColor,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Sparse tick marks (10 minor, 30 major)
                for (deg in 0 until 360 step 10) {
                    val isCardinal = deg % 90 == 0
                    val isMajor = deg % 30 == 0
                    val tickLen = if (isCardinal) 14.dp.toPx() else 7.dp.toPx()
                    val rad = Math.toRadians(deg.toDouble())
                    val outerX = center.x + (radius - 5.dp.toPx()) * sin(rad).toFloat()
                    val outerY = center.y - (radius - 5.dp.toPx()) * cos(rad).toFloat()
                    val innerX = center.x + (radius - 5.dp.toPx() - tickLen) * sin(rad).toFloat()
                    val innerY = center.y - (radius - 5.dp.toPx() - tickLen) * cos(rad).toFloat()

                    drawLine(
                        color = when {
                            deg == 0 -> northColor
                            isMajor -> tickColor
                            else -> tickColor.copy(alpha = 0.55f)
                        },
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Draw N, S, E, W Typography on the dial ring
                val labelDistance = radius - 34.dp.toPx()
                cardinalLabels.forEach { (angleDeg, layout) ->
                    val rad = Math.toRadians(angleDeg)
                    val posX = center.x + labelDistance * sin(rad).toFloat() - (layout.size.width / 2f)
                    val posY = center.y - labelDistance * cos(rad).toFloat() - (layout.size.height / 2f)
                    drawText(layout, topLeft = Offset(posX, posY))
                }

                // Kaaba Qibla Pointer Needle from Center outward
                val qiblaRad = Math.toRadians(qiblaState.qiblaBearing.toDouble())
                val needleTipDistance = radius - 16.dp.toPx()
                val qiblaX = center.x + needleTipDistance * sin(qiblaRad).toFloat()
                val qiblaY = center.y - needleTipDistance * cos(qiblaRad).toFloat()

                // Needle: slim tapering triangle from a center pivot dot
                val needleHalfWidth = 7.dp.toPx()
                val needlePath = Path().apply {
                    moveTo(qiblaX, qiblaY)
                    val baseLeftX = center.x + needleHalfWidth * sin(qiblaRad - Math.PI / 2).toFloat()
                    val baseLeftY = center.y - needleHalfWidth * cos(qiblaRad - Math.PI / 2).toFloat()
                    val baseRightX = center.x + needleHalfWidth * sin(qiblaRad + Math.PI / 2).toFloat()
                    val baseRightY = center.y - needleHalfWidth * cos(qiblaRad + Math.PI / 2).toFloat()
                    lineTo(baseLeftX, baseLeftY)
                    lineTo(baseRightX, baseRightY)
                    close()
                }
                drawPath(needlePath, color = needleColor)
                drawCircle(color = needleColor, radius = 5.dp.toPx(), center = center)

                // Qibla / Kaaba Marker on the circle perimeter of the compass
                val kaabaMarkerRad = qiblaRad
                val kaabaMarkerDistance = radius - 2.dp.toPx()
                val kaabaCircleX = center.x + kaabaMarkerDistance * sin(kaabaMarkerRad).toFloat()
                val kaabaCircleY = center.y - kaabaMarkerDistance * cos(kaabaMarkerRad).toFloat()

                // Kaaba badge: gold ring with dark center
                drawCircle(
                    color = kaabaGoldColor,
                    radius = 12.dp.toPx(),
                    center = Offset(kaabaCircleX, kaabaCircleY)
                )
                drawCircle(
                    color = kaabaCenterColor,
                    radius = 9.dp.toPx(),
                    center = Offset(kaabaCircleX, kaabaCircleY)
                )

                // Sun bearing indicator for celestial cross-check
                val sunRad = Math.toRadians(qiblaState.sunAzimuth.toDouble())
                val sunX = center.x + (radius - 22.dp.toPx()) * sin(sunRad).toFloat()
                val sunY = center.y - (radius - 22.dp.toPx()) * cos(sunRad).toFloat()
                drawCircle(color = sunIndicatorColor.copy(alpha = 0.8f), radius = 5.dp.toPx(), center = Offset(sunX, sunY))
            }

            // Central Kaaba Hub
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = hubColor,
                shadowElevation = NourSize.flat
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.Explore, contentDescription = null, tint = hubTextColor)
                    Text(
                        text = strings.qiblaHubLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = hubTextColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // --- NUR-009: lower stack — the manual fallback (a single text action
        // until it is needed) and the heading/distance readout. Measured for
        // the stretch; the readout is last so it docks at the bottom reserve
        // when the dial absorbs the page's slack.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    lowerHeightPx = coordinates.size.height.toFloat()
                },
            verticalArrangement = Arrangement.spacedBy(NourSpace.md)
        ) {
            if (manualMode) {
                // The status line above already explains that the slider is a
                // reference, not a measurement. Show only the control and its
                // live value while manual mode is active.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatNumerals("${strings.referenceHeadingPrefix}: ${manualHeading.toInt()}°"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (qiblaState.hasMagnetometer) {
                            TextButton(
                                onClick = { manualMode = false },
                                modifier = Modifier.testTag("toggle_manual_bearing")
                            ) {
                                Text(strings.useSensors)
                            }
                        }
                    }
                    Slider(
                        value = manualHeading,
                        onValueChange = {
                            manualHeading = it
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier
                            .testTag("manual_bearing_slider")
                            // NUR-011: the slider itself was an empty
                            // speaking element under TalkBack; its state now
                            // carries the reference heading the row shows.
                            .semantics {
                                stateDescription = Numerals.convert(
                                    "${strings.referenceHeadingPrefix}: ${manualHeading.toInt()}°",
                                    useEasternNumerals
                                )
                            }
                    )
                }
            } else {
                // Low-emphasis fallback, collapsed until the user asks for it.
                // When no magnetometer exists the screen starts in manual mode,
                // so this branch cannot strand a sensorless device.
                TextButton(
                    onClick = { manualMode = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("toggle_manual_bearing")
                ) {
                    Text(strings.manualSliderBtn)
                }
            }

            // NUR-009 (moved down): heading and distance close the page at the
            // bottom reserve line. Unframed: the numbers are the content, not
            // another card.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qibla_metrics_card"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                ) {
                    Text(
                        text = strings.qiblaHeading,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatNumerals(String.format(Locale.getDefault(), "%.1f°", qiblaState.qiblaBearing)),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = strings.fromTrueNorth,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .width(NourSize.border)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                ) {
                    Text(
                        text = strings.distanceToKaaba,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatNumerals(String.format(Locale.getDefault(), "%,.0f km", qiblaState.distanceKm)),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = strings.makkahLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        } // --- end NUR-009 lower stack ---
    }
    }

    // Figure-8 Motion Calibration Guidance Dialog
    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            shape = DialogExpressiveShape,
            title = {
                Text(
                    text = strings.calibrationTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(NourSpace.md)) {
                    Text(
                        text = strings.calibrationDesc,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = CardSubtleShape
                    ) {
                        Column(modifier = Modifier.padding(NourSpace.lg)) {
                            Text(
                                text = strings.figure8Title,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(NourSpace.xs))
                            Text(
                                text = strings.figure8Desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Text(
                        text = strings.calibrationTips,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                NourCapsuleButton(
                    onClick = { showCalibrationDialog = false },
                    text = strings.gotIt
                )
            }
        )
    }
}
