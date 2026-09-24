package com.example.features.qibla.ui

import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.SheetExpressiveShape
import com.example.ui.components.NourEmptyState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.hardware.SensorManager
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Info
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.prayer.models.UserLocation
import com.example.features.qibla.QiblaData
import com.example.features.qibla.QiblaSensorManager
import kotlin.math.cos
import kotlin.math.sin

import com.example.localization.LocalAppStrings
import com.example.localization.LocalUseEasternNumerals
import com.example.localization.Numerals
import com.example.localization.formatNumerals
import com.example.features.location.LocationPermissionCard
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.ScreenRotation

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

    val animatedRotation by animateFloatAsState(
        targetValue = -effectiveHeading,
        animationSpec = spring(stiffness = 300f),
        label = "compass_rotation"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(NourSpace.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
        Text(
            text = when {
                manualMode -> strings.qiblaModeManual
                !qiblaState.hasMagnetometer -> strings.qiblaNoSensor
                !qiblaState.hasHeading -> strings.qiblaWaiting
                qiblaState.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ->
                    strings.qiblaNeedCalibration
                !qiblaState.isDeviceFlat -> strings.qiblaHoldFlatShort
                else -> strings.qiblaEstimated
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().testTag("qibla_mode_status")
        )

        // Top Location and Accuracy Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.qiblaTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${location.cityName} ${strings.kaabaSuffix}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Sensor Accuracy Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (accuracyText, accuracyColor) = when {
                        manualMode -> strings.accuracyManual to MaterialTheme.colorScheme.onSurfaceVariant
                        !qiblaState.hasHeading -> strings.accuracyWaiting to MaterialTheme.colorScheme.onSurfaceVariant
                        qiblaState.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> strings.accuracyHigh to MaterialTheme.colorScheme.primary
                        qiblaState.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> strings.accuracyMedium to MaterialTheme.colorScheme.secondary
                        else -> strings.accuracyCalibrate to MaterialTheme.colorScheme.onSurfaceVariant
                    }

            Surface(
                color = accuracyColor.copy(alpha = NourAlpha.BadgeTint),
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
                            .background(accuracyColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = accuracyText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = accuracyColor,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = strings.calibrationGuide,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Location Permission & GPS acquisition card if location not yet auto-detected
        if (!location.isAutoDetected && onLocationUpdated != null) {
            LocationPermissionCard(
                currentLocation = location,
                onLocationDetected = onLocationUpdated
            )
        }

        // Combined sensor + declination status line (one row instead of two)
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = PillShape,
            border = null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(NourSize.smallIcon)
                )
                Spacer(modifier = Modifier.width(NourSpace.xs))
                Text(
                    text = formatNumerals(
                        (if (qiblaState.hasMagnetometer && qiblaState.hasAccelerometer)
                            strings.sensorFusion else strings.singleSensor) +
                        " • ${strings.trueNorthPrefix}: " +
                        String.format(Locale.US, "%+.1f°", qiblaState.declination)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        // Tilt Alert if phone is held at steep angle (>35°)
        AnimatedVisibility(visible = !qiblaState.isDeviceFlat && !manualMode) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                shape = CardExpressiveShape,
                border = null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = NourSpace.lg, vertical = NourSpace.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(NourSize.icon)
                    )
                    Spacer(modifier = Modifier.width(NourSpace.md))
                    Text(
                        text = strings.tiltMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Facing Qibla Banner
        AnimatedVisibility(visible = isFacingQibla) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        // Compass Rose Dial with Kaaba Pointer
        Box(
            modifier = Modifier
                .widthIn(max = NourSize.compass).fillMaxWidth().aspectRatio(1f)
                .semantics { contentDescription = Numerals.convert("${strings.qiblaHeading} ${qiblaState.qiblaBearing.toInt()}°", useEasternNumerals) }
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            val dialBorderColor = MaterialTheme.colorScheme.outlineVariant
            val tickColor = MaterialTheme.colorScheme.outline
            val northColor = MaterialTheme.colorScheme.primary
            val southColor = MaterialTheme.colorScheme.outline
            val needleColor = MaterialTheme.colorScheme.primary
            val sunIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            val hubColor = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val hubTextColor = if (isFacingQibla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            val kaabaGoldColor = MaterialTheme.colorScheme.primary
            val kaabaCenterColor = MaterialTheme.colorScheme.onPrimary
            val textMeasurer = rememberTextMeasurer()

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
                val cardinalLabels = listOf(
                    Triple("N", 0.0, northColor),
                    Triple("E", 90.0, southColor),
                    Triple("S", 180.0, southColor),
                    Triple("W", 270.0, southColor)
                )
                val labelDistance = radius - 34.dp.toPx()
                cardinalLabels.forEach { (label, angleDeg, color) ->
                    val rad = Math.toRadians(angleDeg)
                    val textLayout = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            fontSize = if (label == "N") 14.sp else 12.sp,
                            fontWeight = if (label == "N") FontWeight.SemiBold else FontWeight.Medium,
                            color = color
                        )
                    )
                    val posX = center.x + labelDistance * sin(rad).toFloat() - (textLayout.size.width / 2f)
                    val posY = center.y - labelDistance * cos(rad).toFloat() - (textLayout.size.height / 2f)
                    drawText(textLayout, topLeft = Offset(posX, posY))
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

        // Qibla Angle & Kaaba Distance Details (full-width stacked cards:
        // side-by-side halves squeezed long translations into vertical text)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("qibla_metrics_card"),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                ) {
                    Text(
                        text = strings.qiblaHeading.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatNumerals(String.format(Locale.getDefault(), "%.1f°", qiblaState.qiblaBearing)),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.2f,
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = strings.fromTrueNorth.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
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
                        text = strings.distanceToKaaba.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatNumerals(String.format(Locale.getDefault(), "%,.0f km", qiblaState.distanceKm)),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.2f,
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = strings.makkahLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Sun-Based Cross Check Card (full width)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = PillShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    }
                }
                Spacer(modifier = Modifier.width(NourSpace.md))
                Column {
                    Text(
                        text = strings.sunPosition,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(NourSpace.xs))
                    Text(
                        text = formatNumerals(String.format(strings.sunBearingDescPattern, String.format(Locale.getDefault(), "%.0f°", qiblaState.sunAzimuth))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }

        // Manual Bearing Fallback Toggle & Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
        ) {
            Column(modifier = Modifier.padding(NourSpace.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.manualFallbackTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = strings.manualFallbackSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { manualMode = !manualMode },
                        enabled = !manualMode || qiblaState.hasMagnetometer,
                        shape = PillShape,
                        modifier = Modifier.testTag("toggle_manual_bearing")
                    ) {
                        Text(if (manualMode) strings.useSensors else strings.manualSliderBtn, maxLines = 1, softWrap = false)
                    }
                }

                if (manualMode) {
                    Spacer(modifier = Modifier.height(NourSpace.md))
                    Text(
                        text = formatNumerals("${strings.referenceHeadingPrefix}: ${manualHeading.toInt()}°"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = manualHeading,
                        onValueChange = {
                            manualHeading = it
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier.testTag("manual_bearing_slider")
                    )
                }
            }
        }
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = CardSubtleShape
                    ) {
                        Column(modifier = Modifier.padding(NourSpace.lg)) {
                            Text(
                                text = strings.figure8Title,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.figure8Desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                Button(
                    onClick = { showCalibrationDialog = false },
                    shape = PillShape
                ) {
                    Text(strings.gotIt)
                }
            }
        )
    }
}
