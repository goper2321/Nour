package com.example.features.qibla.ui

import android.hardware.SensorManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import com.example.localization.LocalAppStrings
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
    val strings = LocalAppStrings.current
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

    val effectiveHeading = if (manualMode) manualHeading else qiblaState.deviceHeading
    val relativeAngle = (qiblaState.qiblaBearing - effectiveHeading + 360f) % 360f
    val isFacingQibla = relativeAngle in 357f..360f || relativeAngle in 0f..3f

    val animatedRotation by animateFloatAsState(
        targetValue = -effectiveHeading,
        animationSpec = spring(stiffness = 300f),
        label = "compass_rotation"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Location and Accuracy Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.qiblaTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${location.cityName} → Kaaba, Makkah",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sensor Accuracy Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (accuracyText, accuracyColor) = when (qiblaState.accuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> strings.accuracyAccurate to MaterialTheme.colorScheme.primary
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> strings.accuracyMedium to MaterialTheme.colorScheme.secondary
                        else -> strings.accuracyCalibrate to MaterialTheme.colorScheme.outline
                    }

                    Surface(
                        color = accuracyColor.copy(alpha = 0.15f),
                        shape = PillShape,
                        border = BorderStroke(1.dp, accuracyColor.copy(alpha = 0.3f)),
                        modifier = Modifier.testTag("qibla_accuracy_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(accuracyColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = accuracyText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accuracyColor
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

        // Sensor Fusion & Magnetic Declination Status Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = PillShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (qiblaState.hasMagnetometer && qiblaState.hasAccelerometer)
                            strings.sensorFusionAccelMag
                        else
                            strings.singleSensor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = PillShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${strings.trueNorth}: ${String.format(Locale.US, "%+.1f°", qiblaState.declination)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tilt Alert if phone is held at steep angle (>35°)
        AnimatedVisibility(visible = !qiblaState.isDeviceFlat && !manualMode) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                shape = CardExpressiveShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.tiltAlert,
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
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("facing_qibla_badge")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Aligned with Kaaba",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.allahuAkbarFacingKaaba,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        // Compass Rose Dial with Kaaba Pointer
        Box(
            modifier = Modifier
                .size(310.dp)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            val dialBorderColor = MaterialTheme.colorScheme.outlineVariant
            val tickColor = MaterialTheme.colorScheme.outline
            val northColor = MaterialTheme.colorScheme.primary
            val southColor = MaterialTheme.colorScheme.outline
            val needleColor = MaterialTheme.colorScheme.primary
            val pointerOuter = MaterialTheme.colorScheme.primary
            val pointerInner = MaterialTheme.colorScheme.surface
            val sunIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            val hubColor = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val hubTextColor = if (isFacingQibla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            val textMeasurer = rememberTextMeasurer()

            // Rotating Compass Rose Canvas
            Canvas(
                modifier = Modifier
                    .size(290.dp)
                    .rotate(animatedRotation)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Outer Dial Ring
                drawCircle(
                    color = dialBorderColor,
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Secondary inner guide track
                drawCircle(
                    color = dialBorderColor.copy(alpha = 0.35f),
                    radius = radius - 22.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Degree tick marks (every 5 degrees for a precision instrument feel)
                for (deg in 0 until 360 step 5) {
                    val isCardinal = deg % 90 == 0
                    val isMajor = deg % 30 == 0
                    val tickLen = when {
                        isCardinal -> 16.dp.toPx()
                        isMajor -> 12.dp.toPx()
                        else -> 6.dp.toPx()
                    }
                    val rad = Math.toRadians(deg.toDouble())
                    val outerX = center.x + (radius - 5.dp.toPx()) * sin(rad).toFloat()
                    val outerY = center.y - (radius - 5.dp.toPx()) * cos(rad).toFloat()
                    val innerX = center.x + (radius - 5.dp.toPx() - tickLen) * sin(rad).toFloat()
                    val innerY = center.y - (radius - 5.dp.toPx() - tickLen) * cos(rad).toFloat()

                    drawLine(
                        color = when {
                            deg == 0 -> northColor
                            isMajor -> tickColor
                            else -> tickColor.copy(alpha = 0.5f)
                        },
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // 4 Cardinal Direction Arrow Heads (N, S, E, W)
                // North Arrow (Bold Primary)
                val northPath = Path().apply {
                    moveTo(center.x, center.y - radius + 24.dp.toPx())
                    lineTo(center.x - 7.dp.toPx(), center.y - 48.dp.toPx())
                    lineTo(center.x + 7.dp.toPx(), center.y - 48.dp.toPx())
                    close()
                }
                drawPath(path = northPath, color = northColor)

                // South Arrow (Muted Outline)
                val southPath = Path().apply {
                    moveTo(center.x, center.y + radius - 24.dp.toPx())
                    lineTo(center.x - 6.dp.toPx(), center.y + 48.dp.toPx())
                    lineTo(center.x + 6.dp.toPx(), center.y + 48.dp.toPx())
                    close()
                }
                drawPath(path = southPath, color = southColor)

                // East Arrow (Subtle)
                val eastPath = Path().apply {
                    moveTo(center.x + radius - 24.dp.toPx(), center.y)
                    lineTo(center.x + 48.dp.toPx(), center.y - 6.dp.toPx())
                    lineTo(center.x + 48.dp.toPx(), center.y + 6.dp.toPx())
                    close()
                }
                drawPath(path = eastPath, color = southColor.copy(alpha = 0.7f))

                // West Arrow (Subtle)
                val westPath = Path().apply {
                    moveTo(center.x - radius + 24.dp.toPx(), center.y)
                    lineTo(center.x - 48.dp.toPx(), center.y - 6.dp.toPx())
                    lineTo(center.x - 48.dp.toPx(), center.y + 6.dp.toPx())
                    close()
                }
                drawPath(path = westPath, color = southColor.copy(alpha = 0.7f))

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
                            fontWeight = if (label == "N") FontWeight.ExtraBold else FontWeight.Bold,
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

                // Sleek pointer needle path
                val needlePath = Path().apply {
                    moveTo(qiblaX, qiblaY)
                    val baseLeftX = center.x + 12.dp.toPx() * sin(qiblaRad - Math.PI / 2).toFloat()
                    val baseLeftY = center.y - 12.dp.toPx() * cos(qiblaRad - Math.PI / 2).toFloat()
                    val baseRightX = center.x + 12.dp.toPx() * sin(qiblaRad + Math.PI / 2).toFloat()
                    val baseRightY = center.y - 12.dp.toPx() * cos(qiblaRad + Math.PI / 2).toFloat()
                    lineTo(baseLeftX, baseLeftY)
                    lineTo(baseRightX, baseRightY)
                    close()
                }
                drawPath(needlePath, color = needleColor.copy(alpha = 0.85f))

                // Qibla / Kaaba Marker on the circle perimeter of the compass
                val kaabaMarkerRad = qiblaRad
                val kaabaMarkerDistance = radius - 2.dp.toPx()
                val kaabaCircleX = center.x + kaabaMarkerDistance * sin(kaabaMarkerRad).toFloat()
                val kaabaCircleY = center.y - kaabaMarkerDistance * cos(kaabaMarkerRad).toFloat()

                // Golden beacon / aura on the circle ring
                val goldGlowColor = Color(0xFFFFD54F)
                val kaabaBlackColor = Color(0xFF1E1E1E)

                drawCircle(
                    color = goldGlowColor.copy(alpha = 0.35f),
                    radius = 18.dp.toPx(),
                    center = Offset(kaabaCircleX, kaabaCircleY)
                )
                // Outer circle badge on perimeter
                drawCircle(
                    color = goldGlowColor,
                    radius = 13.dp.toPx(),
                    center = Offset(kaabaCircleX, kaabaCircleY)
                )
                drawCircle(
                    color = kaabaBlackColor,
                    radius = 11.dp.toPx(),
                    center = Offset(kaabaCircleX, kaabaCircleY)
                )

                // Miniature Golden Kiswah band on the Kaaba circle icon
                val cubeHalf = 5.dp.toPx()
                drawRect(
                    color = goldGlowColor,
                    topLeft = Offset(kaabaCircleX - cubeHalf, kaabaCircleY - cubeHalf * 0.4f),
                    size = androidx.compose.ui.geometry.Size(cubeHalf * 2, 2.dp.toPx())
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
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🕋",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "QIBLA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = hubTextColor
                    )
                }
            }
        }

        // Qibla Angle & Kaaba Distance Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.qiblaHeading,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f°", qiblaState.qiblaBearing),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.fromTrueNorth,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.distanceToKaaba,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f km", qiblaState.distanceKm),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = strings.makkahAlMukarramah,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sun-Based Cross Check Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = PillShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "☀️", fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = strings.sunCelestialCheck,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.0f°", qiblaState.sunAzimuth)}. ${strings.sunBearingDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Manual Bearing Fallback Toggle & Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.manualBearingFallback,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.manualBearingSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { manualMode = !manualMode },
                        shape = PillShape,
                        modifier = Modifier.testTag("toggle_manual_bearing")
                    ) {
                        Text(if (manualMode) strings.useSensors else strings.manualSlider)
                    }
                }

                if (manualMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "${strings.rotatePhoneManual}: ${manualHeading.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = manualHeading,
                        onValueChange = {
                            manualHeading = it
                            sensorManager.setManualHeading(it)
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier.testTag("manual_bearing_slider")
                    )
                }
            }
        }
    }

    // Figure-8 Motion Calibration Guidance Dialog
    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            shape = CardExpressiveShape,
            title = {
                Text(
                    text = strings.sensorCalibrationTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = strings.sensorCalibrationIntro,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = CardSubtleShape
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🔄 ${strings.figure8Title}:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.figure8Description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = strings.sensorInterferenceTip,
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
