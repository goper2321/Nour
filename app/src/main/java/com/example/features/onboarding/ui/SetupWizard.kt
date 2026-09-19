package com.example.features.onboarding.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.features.location.DeviceLocationHelper
import com.example.features.location.rememberLocationPermissionState
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * First-launch setup: welcome → language → location → prayer alerts.
 * Every step is skippable via Next; nothing here is forced.
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
    // Saveable: applying a language recreates the activity on some devices;
    // the wizard must resume on the same step instead of restarting.
    var step by rememberSaveable { mutableIntStateOf(0) }

    // ---- location state ----
    val locationPermissions = rememberLocationPermissionState()
    var detecting by remember { mutableStateOf(false) }
    fun detectLocation() {
        if (detecting) return
        detecting = true
        scope.launch {
            val loc = DeviceLocationHelper.getCurrentLocation(context)
            if (loc != null) {
                onLocationChange(DeviceLocationHelper.resolveUserLocation(context, loc))
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
            .padding(NourSpace.xl),
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
        // Progress dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(4) { index ->
                Surface(
                    shape = CircleShape,
                    color = if (index <= step) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                ) {}
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "setup_step",
            modifier = Modifier.weight(1f)
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
                    onUseLocation = {
                        if (locationPermissions.allPermissionsGranted) detectLocation()
                        else locationPermissions.launchMultiplePermissionRequest()
                    }
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

        // Bottom navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(
                    onClick = { step-- },
                    modifier = Modifier.testTag("setup_back")
                ) { Text(strings.setupBack) }
            } else {
                TextButton(
                    onClick = onSetupComplete,
                    modifier = Modifier.testTag("setup_skip")
                ) { Text(strings.setupSkip) }
            }
            Button(
                onClick = { if (step < 3) step++ else onSetupComplete() },
                shape = PillShape,
                modifier = Modifier.testTag("setup_next")
            ) { Text(if (step == 3) strings.setupStart else strings.setupNext) }
        }
    }
}

@Composable
private fun SetupWelcomeStep(onStart: () -> Unit, onSkip: () -> Unit) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.setupWelcomeTitle,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(NourSpace.md))
        Text(
            text = strings.privacyPledgeBullet5,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SetupLanguageStep(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Text(
            text = strings.languageSectionTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = strings.languageSectionSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            AppLanguage.entries.forEach { lang ->
                val isSelected = currentLanguage == lang
                Surface(
                    shape = CardSubtleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
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
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupLocationStep(
    userLocation: UserLocation,
    permissionGranted: Boolean,
    detecting: Boolean,
    onUseLocation: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Text(
            text = strings.setupLocationTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = strings.setupLocationSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userLocation.isAutoDetected) userLocation.cityName
                        else "${userLocation.cityName}, ${userLocation.countryName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Text(
                        text = if (permissionGranted) strings.gpsActive
                        else strings.locationPermissionRequired,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (detecting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Button(
                        onClick = onUseLocation,
                        shape = PillShape,
                        modifier = Modifier.testTag("setup_use_location")
                    ) {
                        Text(strings.setupUseMyLocation, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupAlertsStep(
    notifRowVisible: Boolean,
    notifGranted: Boolean,
    onNotifToggle: () -> Unit,
    exactRowVisible: Boolean,
    exactGranted: Boolean,
    onExactToggle: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Text(
            text = strings.setupAlertsTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = strings.setupAlertsSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(NourSpace.lg)) {
                if (notifRowVisible) {
                    SetupSwitchRow(
                        title = strings.setupNotifTitle,
                        checked = notifGranted,
                        onCheckedChange = { onNotifToggle() },
                        testTag = "setup_notif_switch"
                    )
                }
                if (exactRowVisible) {
                    SetupSwitchRow(
                        title = strings.setupExactAlarmTitle,
                        checked = exactGranted,
                        onCheckedChange = { onExactToggle() },
                        testTag = "setup_exact_switch"
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSwitchRow(
    title: String,
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
