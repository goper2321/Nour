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
import androidx.compose.ui.text.font.FontWeight
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
                        text = "Qibla Direction",
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
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Accurate" to MaterialTheme.colorScheme.primary
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium" to MaterialTheme.colorScheme.secondary
                        else -> "Calibrate" to MaterialTheme.colorScheme.outline
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
                            contentDescription = "Calibration Guide",
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
                            "Accel + Mag Fusion"
                        else
                            "Single Sensor",
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
                        text = "True North: ${String.format(Locale.US, "%+.1f°", qiblaState.declination)}",
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
                        text = "Hold phone flat (parallel to the ground) for optimal accelerometer & magnetometer accuracy.",
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
                        text = "Allahu Akbar — You are facing the Holy Kaaba!",
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

                // Degree tick marks (every 10 degrees)
                for (deg in 0 until 360 step 10) {
                    val isMajor = deg % 30 == 0
                    val tickLen = if (isMajor) 18.dp.toPx() else 10.dp.toPx()
                    val rad = Math.toRadians(deg.toDouble())
                    val outerX = center.x + (radius - 8.dp.toPx()) * sin(rad).toFloat()
                    val outerY = center.y - (radius - 8.dp.toPx()) * cos(rad).toFloat()
                    val innerX = center.x + (radius - 8.dp.toPx() - tickLen) * sin(rad).toFloat()
                    val innerY = center.y - (radius - 8.dp.toPx() - tickLen) * cos(rad).toFloat()

                    drawLine(
                        color = if (deg == 0) northColor else tickColor,
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                    )
                }

                // True North Monochrome Arrow
                val northPath = Path().apply {
                    moveTo(center.x, center.y - radius + 30.dp.toPx())
                    lineTo(center.x - 10.dp.toPx(), center.y - 40.dp.toPx())
                    lineTo(center.x + 10.dp.toPx(), center.y - 40.dp.toPx())
                    close()
                }
                drawPath(path = northPath, color = northColor)

                // South Subtle Arrow
                val southPath = Path().apply {
                    moveTo(center.x, center.y + radius - 30.dp.toPx())
                    lineTo(center.x - 8.dp.toPx(), center.y + 40.dp.toPx())
                    lineTo(center.x + 8.dp.toPx(), center.y + 40.dp.toPx())
                    close()
                }
                drawPath(path = southPath, color = southColor)

                // Kaaba Qibla Pointer Needle
                val qiblaRad = Math.toRadians(qiblaState.qiblaBearing.toDouble())
                val qiblaX = center.x + (radius - 45.dp.toPx()) * sin(qiblaRad).toFloat()
                val qiblaY = center.y - (radius - 45.dp.toPx()) * cos(qiblaRad).toFloat()

                val needlePath = Path().apply {
                    moveTo(qiblaX, qiblaY)
                    val baseLeftX = center.x + 14.dp.toPx() * sin(qiblaRad - Math.PI / 2).toFloat()
                    val baseLeftY = center.y - 14.dp.toPx() * cos(qiblaRad - Math.PI / 2).toFloat()
                    val baseRightX = center.x + 14.dp.toPx() * sin(qiblaRad + Math.PI / 2).toFloat()
                    val baseRightY = center.y - 14.dp.toPx() * cos(qiblaRad + Math.PI / 2).toFloat()
                    lineTo(baseLeftX, baseLeftY)
                    lineTo(baseRightX, baseRightY)
                    close()
                }
                drawPath(needlePath, color = needleColor)

                // Kaaba marker icon at tip of needle
                drawCircle(
                    color = pointerOuter,
                    radius = 9.dp.toPx(),
                    center = Offset(qiblaX, qiblaY)
                )
                drawCircle(
                    color = pointerInner,
                    radius = 4.dp.toPx(),
                    center = Offset(qiblaX, qiblaY)
                )

                // Sun bearing indicator for celestial cross-check
                val sunRad = Math.toRadians(qiblaState.sunAzimuth.toDouble())
                val sunX = center.x + (radius - 20.dp.toPx()) * sin(sunRad).toFloat()
                val sunY = center.y - (radius - 20.dp.toPx()) * cos(sunRad).toFloat()
                drawCircle(color = sunIndicatorColor, radius = 6.dp.toPx(), center = Offset(sunX, sunY))
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
                        text = "Qibla Bearing",
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
                        text = "From True North",
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
                        text = "Distance to Kaaba",
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
                        text = "Makkah Al-Mukarramah",
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
                        text = "Sun-Based Celestial Cross-Check",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Current Sun bearing is approx ${String.format(Locale.getDefault(), "%.0f°", qiblaState.sunAzimuth)}. Look at the Sun's physical position outside to confirm device compass orientation.",
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
                            text = "Manual Bearing Fallback",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "For testing or sensors unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { manualMode = !manualMode },
                        shape = PillShape,
                        modifier = Modifier.testTag("toggle_manual_bearing")
                    ) {
                        Text(if (manualMode) "Use Sensors" else "Manual Slider")
                    }
                }

                if (manualMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Rotate phone orientation manually: ${manualHeading.toInt()}°",
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
                    text = "Sensor Calibration Guidance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "To ensure high compass accuracy for the Qibla, your phone's magnetometer must be calibrated against magnetic interference:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = CardSubtleShape
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🔄 Figure-8 Motion:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hold your phone flat and wave it gently in a continuous Figure-8 (infinity symbol ∞) motion in the air 3 to 5 times.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = "• Keep away from large metal objects, speakers, laptop magnets, or thick magnetic phone cases.\n• Keep the phone parallel to the ground while facing the Qibla.",
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
                    Text("Got it")
                }
            }
        )
    }
}
