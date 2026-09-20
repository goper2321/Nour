package com.example.features.onboarding.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.features.location.DeviceLocationHelper
import com.example.features.location.HOLY_CITIES
import com.example.features.location.WORLD_LOCATIONS
import com.example.features.location.WorldLocation
import com.example.features.location.rememberLocationPermissionState
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.theme.ButtonExpressiveShape
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.ListRowShape
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive First-Launch Setup Wizard:
 * 0. Welcome & Privacy Pledge
 * 1. Language Selection (dynamic instant-apply)
 * 2. Location (Auto GPS with fallback to World & Holy Cities search picker)
 * 3. Prayer Alerts (Adhan notifications, exact alarms & sound test)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupWizard(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    userLocation: UserLocation,
    onLocationChange: (UserLocation) -> Unit,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var step by rememberSaveable { mutableIntStateOf(0) }

    // ---- location state ----
    val locationPermissions = rememberLocationPermissionState()
    var detecting by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }

    fun detectLocation() {
        if (detecting) return
        detecting = true
        locationError = false
        scope.launch {
            val loc = DeviceLocationHelper.getCurrentLocation(context)
            if (loc != null) {
                onLocationChange(DeviceLocationHelper.resolveUserLocation(context, loc))
                locationError = false
            } else {
                locationError = true
            }
            detecting = false
        }
    }

    LaunchedEffect(step, locationPermissions.allPermissionsGranted) {
        if (step == 2 && locationPermissions.allPermissionsGranted &&
            !userLocation.isAutoDetected && !detecting
        ) {
            detectLocation()
        }
    }

    // ---- notification permission (Android 13+ only shows a row) ----
    val notifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val notifGranted = if (Build.VERSION.SDK_INT >= 33) {
        notifPermission.status is PermissionStatus.Granted
    } else {
        true
    }

    // ---- exact alarms (Android 12+ only shows a row) ----
    val alarmManager = remember {
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    }
    var alarmTick by remember { mutableIntStateOf(0) }
    val canExact = remember(alarmTick) {
        if (Build.VERSION.SDK_INT >= 31) alarmManager?.canScheduleExactAlarms() ?: false else true
    }

    fun openExactAlarmSettings() {
        alarmTick++
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                )
            } catch (_: Exception) {
            }
        }
    }

    fun openAppNotificationSettings() {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            )
        } catch (_: Exception) {
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(NourSpace.lg),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Progress Indicator (M3E animated pill indicators)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NourSpace.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isCurrent = index == step
                    val isPast = index < step
                    val width by animateDpAsState(
                        targetValue = if (isCurrent) 28.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "progress_pill_width_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(PillShape)
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                }
                            )
                    )
                }
            }
        }

        // Animated step content with expressive spring slide & fade
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = NourSpace.sm)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 2 } + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } + fadeOut())
                    } else {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 2 } + fadeOut())
                    }
                },
                label = "setup_step_transition",
                modifier = Modifier.fillMaxSize()
            ) { current ->
                when (current) {
                    0 -> SetupWelcomeStep(
                        onStart = { step = 1 },
                        onSkip = onSetupComplete
                    )
                    1 -> SetupLanguageStep(
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange
                    )
                    2 -> SetupLocationStep(
                        userLocation = userLocation,
                        permissionGranted = locationPermissions.allPermissionsGranted,
                        detecting = detecting,
                        hasError = locationError,
                        onUseLocation = {
                            if (locationPermissions.allPermissionsGranted) detectLocation()
                            else locationPermissions.launchMultiplePermissionRequest()
                        },
                        onLocationChange = onLocationChange
                    )
                    else -> SetupAlertsStep(
                        notifRowVisible = Build.VERSION.SDK_INT >= 33,
                        notifGranted = notifGranted,
                        onNotifToggle = {
                            if (notifGranted) openAppNotificationSettings()
                            else notifPermission.launchPermissionRequest()
                        },
                        exactRowVisible = Build.VERSION.SDK_INT >= 31,
                        exactGranted = canExact,
                        onExactToggle = { openExactAlarmSettings() }
                    )
                }
            }
        }

        // Bottom Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = NourSpace.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .heightIn(min = NourSize.touchTarget)
                            .testTag("setup_back")
                    ) {
                        Text(
                            text = strings.setupBack,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    TextButton(
                        onClick = onSetupComplete,
                        modifier = Modifier
                            .heightIn(min = NourSize.touchTarget)
                            .testTag("setup_skip")
                    ) {
                        Text(
                            text = strings.setupSkip,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        if (step < 3) step++ else onSetupComplete()
                    },
                    shape = ButtonExpressiveShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .heightIn(min = NourSize.touchTarget)
                        .testTag("setup_next")
                ) {
                    Text(
                        text = if (step == 3) strings.setupStart else strings.setupNext,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Step 0: Welcome Step with M3E hero badge, tagline, and privacy pledge feature cards.
 */
@Composable
private fun SetupWelcomeStep(
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Spacer(modifier = Modifier.height(NourSpace.sm))

        // Hero Icon with gentle gradient ring
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Mosque,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            )
        }

        Text(
            text = strings.setupWelcomeTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = strings.privacyPledgeBullet5,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = NourSpace.md)
        )

        Spacer(modifier = Modifier.height(NourSpace.xs))

        // Privacy & Offline First Feature Pillars
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                FeaturePillarRow(
                    icon = Icons.Rounded.WifiOff,
                    title = strings.privacyPledgeBullet1,
                    subtitle = "All calculations and data are processed locally"
                )
                FeaturePillarRow(
                    icon = Icons.Rounded.Shield,
                    title = strings.privacyPledgeBullet2,
                    subtitle = "No trackers, ads, or third-party telemetry"
                )
                FeaturePillarRow(
                    icon = Icons.Rounded.Lock,
                    title = strings.privacyPledgeBullet3,
                    subtitle = "Works instantly without logins or registrations"
                )
            }
        }
    }
}

@Composable
private fun FeaturePillarRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NourSpace.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Step 1: Language selection with instant reactive updates and native script representation.
 */
@Composable
private fun SetupLanguageStep(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            Icon(
                imageVector = Icons.Rounded.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = strings.languageSectionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.languageSectionSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(NourSpace.xs))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
        ) {
            items(AppLanguage.entries) { lang ->
                val isSelected = currentLanguage == lang
                Surface(
                    shape = ListRowShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageChange(lang) }
                        .testTag("language_option_${lang.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NourSpace.lg, vertical = NourSpace.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 2: Location selection supporting both GPS auto-detection and manual search picker.
 */
@Composable
private fun SetupLocationStep(
    userLocation: UserLocation,
    permissionGranted: Boolean,
    detecting: Boolean,
    hasError: Boolean,
    onUseLocation: () -> Unit,
    onLocationChange: (UserLocation) -> Unit
) {
    val strings = LocalAppStrings.current
    var showCityPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = strings.setupLocationTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.setupLocationSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Location Status Card
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.activeLocation,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (userLocation.countryName.isNotBlank() && userLocation.cityName != userLocation.countryName) {
                                "${userLocation.cityName}, ${userLocation.countryName}"
                            } else {
                                userLocation.cityName
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = PillShape,
                        color = if (userLocation.isAutoDetected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.padding(start = NourSpace.sm)
                    ) {
                        Text(
                            text = if (userLocation.isAutoDetected) "GPS" else "Manual",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (userLocation.isAutoDetected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Coordinates & Timezone details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                ) {
                    Text(
                        text = "%.2f°, %.2f°".format(userLocation.latitude, userLocation.longitude),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "UTC%+02.0f:00".format(userLocation.timezoneOffsetHours),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action Options: GPS & City Search Picker
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            Button(
                onClick = onUseLocation,
                shape = ButtonExpressiveShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = NourSize.touchTarget)
                    .testTag("setup_use_location"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                enabled = !detecting
            ) {
                if (detecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(NourSpace.sm))
                    Text(
                        text = "Detecting GPS...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(NourSpace.sm))
                    Text(
                        text = strings.setupUseMyLocation,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedButton(
                onClick = { showCityPicker = true },
                shape = ButtonExpressiveShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = NourSize.touchTarget)
                    .testTag("setup_choose_city_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(NourSpace.sm))
                Text(
                    text = strings.selectCity,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (hasError) {
            Text(
                text = "GPS detection unavailable or timed out. Please choose your city manually above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = NourSpace.xs)
            )
        }
    }

    // World & Holy Cities Picker Dialog
    if (showCityPicker) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredList = remember(searchQuery) {
            WORLD_LOCATIONS.filter { it.matches(searchQuery.trim()) }
        }

        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            shape = DialogExpressiveShape,
            title = {
                Text(
                    text = strings.selectCity,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_city_search_field"),
                        placeholder = { Text(strings.searchLocationHint) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = strings.clearSearch
                                    )
                                }
                            }
                        },
                        shape = CardSubtleShape,
                        singleLine = true
                    )

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 360.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                    ) {
                        if (searchQuery.isBlank()) {
                            item {
                                Text(
                                    text = strings.holyCitiesTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = NourSpace.xs, bottom = 2.dp)
                                )
                            }
                            items(HOLY_CITIES) { holy ->
                                SetupCityRow(
                                    location = holy,
                                    isSelected = userLocation.cityName == holy.capital,
                                    onSelect = {
                                        onLocationChange(holy.toUserLocation())
                                        showCityPicker = false
                                    }
                                )
                            }
                            item {
                                Text(
                                    text = "All World Capitals",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = NourSpace.sm, bottom = 2.dp)
                                )
                            }
                        }

                        items(filteredList) { city ->
                            SetupCityRow(
                                location = city,
                                isSelected = userLocation.cityName == city.capital,
                                onSelect = {
                                    onLocationChange(city.toUserLocation())
                                    showCityPicker = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCityPicker = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SetupCityRow(
    location: WorldLocation,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = CardSubtleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NourSpace.md, vertical = NourSpace.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.capital,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = location.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = location.utcLabel(),
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Step 3: Alerts, notification permissions, exact alarms, and audio test preview.
 */
@Composable
private fun SetupAlertsStep(
    notifRowVisible: Boolean,
    notifGranted: Boolean,
    onNotifToggle: () -> Unit,
    exactRowVisible: Boolean,
    exactGranted: Boolean,
    onExactToggle: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var isPlayingAudio by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = strings.setupAlertsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.setupAlertsSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                if (notifRowVisible) {
                    SetupSwitchRow(
                        icon = Icons.Rounded.Notifications,
                        title = strings.setupNotifTitle,
                        subtitle = "Notify you when the prayer time arrives",
                        checked = notifGranted,
                        onCheckedChange = { onNotifToggle() },
                        testTag = "setup_notif_switch"
                    )
                }

                if (exactRowVisible) {
                    SetupSwitchRow(
                        icon = Icons.Rounded.Alarm,
                        title = strings.setupExactAlarmTitle,
                        subtitle = "Required for punctual Adhan sound during Doze mode",
                        checked = exactGranted,
                        onCheckedChange = { onExactToggle() },
                        testTag = "setup_exact_switch"
                    )
                }
            }
        }

        // Adhan Sound Test Card
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.md),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = strings.adhanToneMakkah,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Test Adhan audio output",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isPlayingAudio) {
                            AdhanPlayer.stop()
                            isPlayingAudio = false
                        } else {
                            AdhanPlayer.playAdhan(context, "makkah")
                            isPlayingAudio = true
                        }
                    },
                    shape = ButtonExpressiveShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlayingAudio) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primary,
                        contentColor = if (isPlayingAudio) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("setup_test_audio")
                ) {
                    Text(
                        text = if (isPlayingAudio) strings.stopAudio else strings.testAudio,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NourSpace.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.md),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(NourSpace.sm))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

