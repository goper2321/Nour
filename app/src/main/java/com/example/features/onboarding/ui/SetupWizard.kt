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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.sp
import com.example.database.AdhanSettingsEntity
import com.example.database.NourDatabase
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.adhan.player.AdhanTones
import com.example.features.location.DeviceLocationHelper
import com.example.features.location.HOLY_CITIES
import com.example.features.location.WORLD_LOCATIONS
import com.example.features.location.WorldLocation
import com.example.features.location.rememberLocationPermissionState
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.Numerals
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.ButtonExpressiveShape
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.ChipExpressiveShape
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.ListRowShape
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

/**
 * Material 3 Expressive First-Time Setup Guide:
 * Step 0: Welcome & Privacy Sanctuary
 * Step 1: Language Selection & Regional Formatting (Western/Eastern Numerals, 12h/24h)
 * Step 2: Location, Calculation Method, Madhab & Live Prayer Schedule Preview
 * Step 3: Adhan & Punctual Alerts (Voice picker, exact alarm, early reminder, interactive audio tester)
 * Step 4: Celebration & Configuration Review ("Enter Nour")
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupWizard(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    userLocation: UserLocation,
    onLocationChange: (UserLocation) -> Unit,
    onSetupComplete: () -> Unit,
    alarmsEnabled: Boolean = true,
    onAlarmsEnabledChange: (Boolean) -> Unit = {},
    calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    onCalculationMethodChange: (CalculationMethod) -> Unit = {},
    madhab: Madhab = Madhab.SHAFI_MALIKI_HANBALI,
    onMadhabChange: (Madhab) -> Unit = {},
    useEasternNumerals: Boolean = false,
    onEasternNumeralsChange: (Boolean) -> Unit = {},
    use24Hour: Boolean = true,
    on24HourChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    var step by rememberSaveable { mutableIntStateOf(0) }
    var selectedToneId by rememberSaveable { mutableStateOf(AdhanTones.MAKKAH) }
    var earlyReminderEnabled by rememberSaveable { mutableStateOf(false) }
    var isTestingAudio by remember { mutableStateOf(false) }

    // Stop audio if composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            AdhanPlayer.stop()
        }
    }

    // ---- Location auto-detect state ----
    val locationPermissions = rememberLocationPermissionState()
    var detectingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var showCitySearchDialog by rememberSaveable { mutableStateOf(false) }

    fun detectLocation() {
        if (detectingLocation) return
        detectingLocation = true
        locationError = false
        scope.launch {
            val loc = DeviceLocationHelper.getCurrentLocation(context)
            if (loc != null) {
                onLocationChange(DeviceLocationHelper.resolveUserLocation(context, loc))
                locationError = false
            } else {
                locationError = true
            }
            detectingLocation = false
        }
    }

    // Auto-detect location when reaching Step 2 if permissions already granted
    LaunchedEffect(step, locationPermissions.allPermissionsGranted) {
        if (step == 2 && locationPermissions.allPermissionsGranted &&
            !userLocation.isAutoDetected && !detectingLocation
        ) {
            detectLocation()
        }
    }

    // ---- Notification & Exact Alarm permissions ----
    val notifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val notifGranted = if (Build.VERSION.SDK_INT >= 33) {
        notifPermission.status is PermissionStatus.Granted
    } else {
        true
    }

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

    // Finalize setup: save adhan settings for all 5 prayers, stop audio, complete
    fun finishSetup() {
        AdhanPlayer.stop()
        isTestingAudio = false
        scope.launch(Dispatchers.IO) {
            try {
                val prayerNames = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
                for (name in prayerNames) {
                    db.adhanSettingsDao().saveSettings(
                        AdhanSettingsEntity(
                            prayerName = name,
                            isAdhanEnabled = alarmsEnabled,
                            toneName = selectedToneId,
                            customUri = null,
                            volume = 1.0f,
                            preReminderMinutes = 15,
                            isPreReminderEnabled = earlyReminderEnabled
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
        onSetupComplete()
    }

    val totalSteps = 5
    val stepTitles = listOf(
        strings.setupStepWelcome,
        strings.setupStepLanguage,
        strings.setupStepLocation,
        strings.setupStepAdhan,
        strings.setupStepReady
    )

    Column(
        modifier = modifier
            .testTag("setup_wizard_root")
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            // Shares the app-wide side gutter (Setup runs outside NourContentFrame).
            .padding(horizontal = NourSpace.lg, vertical = NourSpace.md),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ====================================================================
        // Top Expressive Stepper Header
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NourSpace.xs)
        ) {
            // App Branding Bar & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = NourSpace.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nour Brand Badge
                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = NourSpace.sm, vertical = NourSpace.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(NourSize.icon)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mosque,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "NOUR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = strings.setupFeatureOfflineTitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Skip button on steps 0..3
                if (step < totalSteps - 1) {
                    TextButton(
                        onClick = { finishSetup() },
                        modifier = Modifier
                            .testTag("setup_skip_button")
                    ) {
                        Text(
                            text = strings.setupSkip,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Animated Stepper Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = NourSpace.xs, bottom = NourSpace.xs),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSteps) { index ->
                    val isCurrent = index == step
                    val isPast = index < step
                    val pillWidth by animateDpAsState(
                        targetValue = if (isCurrent) 38.dp else if (isPast) 12.dp else 8.dp,
                        animationSpec = NourMotion.spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "stepper_pill_$index"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(pillWidth)
                            .clip(PillShape)
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)
                                }
                            )
                            .testTag("setup_step_indicator_$index")
                    )
                }
            }

            // Step Label & Title
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(NourMotion.duration(180))) togetherWith fadeOut(tween(NourMotion.duration(180))) },
                label = "step_title_label",
                modifier = Modifier.fillMaxWidth()
            ) { currentStep ->
                val title = stepTitles.getOrElse(currentStep) { "" }
                Text(
                    text = String.format(
                        strings.setupStepProgressPattern,
                        currentStep + 1,
                        totalSteps
                    ) + " • " + title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ====================================================================
        // Step Content Area (Smooth horizontal slide & fade)
        // ====================================================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = NourSpace.xs)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                fadeIn(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } +
                                        fadeOut(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow))
                            )
                    } else {
                        (slideInHorizontally(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } +
                                fadeIn(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                        fadeOut(animationSpec = NourMotion.spring(stiffness = Spring.StiffnessMediumLow))
                            )
                    }
                },
                label = "setup_step_body",
                modifier = Modifier.fillMaxSize()
            ) { currentStep ->
                when (currentStep) {
                    0 -> SetupWelcomeStep(
                        onNext = { step = 1 }
                    )
                    1 -> SetupLanguageStep(
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange,
                        useEasternNumerals = useEasternNumerals,
                        onEasternNumeralsChange = onEasternNumeralsChange,
                        use24Hour = use24Hour,
                        on24HourChange = on24HourChange
                    )
                    2 -> SetupLocationStep(
                        userLocation = userLocation,
                        permissionGranted = locationPermissions.allPermissionsGranted,
                        detecting = detectingLocation,
                        hasError = locationError,
                        onUseLocation = {
                            if (locationPermissions.allPermissionsGranted) detectLocation()
                            else locationPermissions.launchMultiplePermissionRequest()
                        },
                        onOpenCityPicker = { showCitySearchDialog = true },
                        calculationMethod = calculationMethod,
                        onCalculationMethodChange = onCalculationMethodChange,
                        madhab = madhab,
                        onMadhabChange = onMadhabChange,
                        use24Hour = use24Hour,
                        useEasternNumerals = useEasternNumerals
                    )
                    3 -> SetupAlertsStep(
                        alarmsEnabled = alarmsEnabled,
                        onAlarmsEnabledChange = onAlarmsEnabledChange,
                        notifRowVisible = Build.VERSION.SDK_INT >= 33,
                        notifGranted = notifGranted,
                        onNotifToggle = {
                            if (notifGranted) openAppNotificationSettings()
                            else notifPermission.launchPermissionRequest()
                        },
                        exactRowVisible = Build.VERSION.SDK_INT >= 31,
                        canExact = canExact,
                        onExactClick = { openExactAlarmSettings() },
                        selectedToneId = selectedToneId,
                        onToneSelect = { selectedToneId = it },
                        isTestingAudio = isTestingAudio,
                        onTestAudioToggle = {
                            if (isTestingAudio) {
                                AdhanPlayer.stop()
                                isTestingAudio = false
                            } else {
                                isTestingAudio = true
                                AdhanPlayer.playAdhan(
                                    context = context,
                                    toneName = selectedToneId,
                                    onCompletion = { isTestingAudio = false }
                                )
                            }
                        },
                        earlyReminderEnabled = earlyReminderEnabled,
                        onEarlyReminderChange = { earlyReminderEnabled = it }
                    )
                    else -> SetupReadyStep(
                        userLocation = userLocation,
                        calculationMethod = calculationMethod,
                        madhab = madhab,
                        selectedToneId = selectedToneId,
                        alarmsEnabled = alarmsEnabled,
                        currentLanguage = currentLanguage,
                        use24Hour = use24Hour,
                        useEasternNumerals = useEasternNumerals
                    )
                }
            }
        }

        // ====================================================================
        // Bottom Expressive Action Bar
        // ====================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = NourSpace.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (step > 0) {
                    OutlinedButton(
                        onClick = {
                            AdhanPlayer.stop()
                            isTestingAudio = false
                            step--
                        },
                        shape = ButtonExpressiveShape,
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .heightIn(min = NourSize.touchTarget)
                            .testTag("setup_back")
                            .testTag("setup_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.setupBack,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        AdhanPlayer.stop()
                        isTestingAudio = false
                        if (step < totalSteps - 1) {
                            step++
                        } else {
                            finishSetup()
                        }
                    },
                    shape = ButtonExpressiveShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .heightIn(min = NourSize.touchTarget)
                        .testTag(if (step == totalSteps - 1) "setup_finish_button" else "setup_next_button")
                        .testTag(if (step == totalSteps - 1) "setup_finish_btn" else "setup_next")
                        .testTag("setup_next_btn")
                ) {
                    Text(
                        text = when (step) {
                            0 -> strings.setupStart
                            totalSteps - 1 -> strings.setupEnterApp
                            else -> strings.setupNext
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(NourSpace.sm))
                    Icon(
                        imageVector = if (step == totalSteps - 1)
                            Icons.Rounded.Check
                        else
                            Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // World & Holy Cities Search Modal Dialog
    if (showCitySearchDialog) {
        WorldCityPickerDialog(
            onDismiss = { showCitySearchDialog = false },
            onSelectLocation = { newLoc ->
                onLocationChange(newLoc)
                showCitySearchDialog = false
            }
        )
    }
}

// ============================================================================
// Step 0: Welcome & Privacy Sanctuary
// ============================================================================
@Composable
private fun SetupWelcomeStep(
    onNext: () -> Unit
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
        Spacer(modifier = Modifier.height(NourSpace.xs))

        // Bismillah Calligraphic Badge
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.padding(top = NourSpace.xs)
        ) {
            Text(
                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = NourSpace.lg, vertical = 6.dp)
            )
        }

        // Hero Mosque Badge with Radial Glow
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
                modifier = Modifier.size(54.dp)
            )
        }

        // Title & Mission Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = NourSpace.sm)
        ) {
            Text(
                text = strings.setupWelcomeTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(NourSpace.xs))
            Text(
                text = strings.setupWelcomeSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 4 Core Feature Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            SetupFeatureCard(
                icon = Icons.Rounded.WifiOff,
                iconColor = MaterialTheme.colorScheme.primary,
                title = strings.setupFeatureOfflineTitle,
                description = strings.setupFeatureOfflineDesc
            )
            SetupFeatureCard(
                icon = Icons.Rounded.Shield,
                iconColor = MaterialTheme.colorScheme.tertiary,
                title = strings.setupFeaturePrivacyTitle,
                description = strings.setupFeaturePrivacyDesc
            )
            SetupFeatureCard(
                icon = Icons.Rounded.Alarm,
                iconColor = MaterialTheme.colorScheme.secondary,
                title = strings.setupFeatureAdhanTitle,
                description = strings.setupFeatureAdhanDesc
            )
            SetupFeatureCard(
                icon = Icons.Rounded.Mosque,
                iconColor = MaterialTheme.colorScheme.primary,
                title = strings.setupFeatureQuranTitle,
                description = strings.setupFeatureQuranDesc
            )
        }

        // Recommended Defaults Chip
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = strings.setupRecommendedDefaults,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.sm)
            )
        }

        Spacer(modifier = Modifier.height(NourSpace.md))
    }
}

@Composable
private fun SetupFeatureCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Card(
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NourSpace.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(NourSize.rowIcon)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ============================================================================
// Step 1: Language Selection & Regional Formatting
// ============================================================================
@Composable
private fun SetupLanguageStep(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    useEasternNumerals: Boolean,
    onEasternNumeralsChange: (Boolean) -> Unit,
    use24Hour: Boolean,
    on24HourChange: (Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }

    val languages = remember { AppLanguage.entries }
    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) languages
        else languages.filter {
            it.nativeName.contains(searchQuery, ignoreCase = true) ||
                    it.displayName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        // Section Header
        StepHeader(
            icon = Icons.Rounded.Language,
            title = strings.sectionLanguageFormats,
            subtitle = strings.languageSectionSubtitle
        )

        // Regional Format Controls (Numerals & Time)
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md)
            ) {
                // Numerals Selector
                Column(verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                    Text(
                        text = strings.numeralTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
                    ) {
                        FormatChoiceChip(
                            title = "Western (123)",
                            sample = "1 2 3",
                            isSelected = !useEasternNumerals,
                            onClick = { onEasternNumeralsChange(false) },
                            testTag = "setup_numeral_western",
                            modifier = Modifier.weight(1f)
                        )
                        FormatChoiceChip(
                            title = "Arabic (١٢٣)",
                            sample = "١ ٢ ٣",
                            isSelected = useEasternNumerals,
                            onClick = { onEasternNumeralsChange(true) },
                            testTag = "setup_numeral_eastern",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                // Time Format Selector
                Column(verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                    Text(
                        text = strings.timeFormatTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_time_format_toggle"),
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
                    ) {
                        FormatChoiceChip(
                            title = strings.timeFormat12,
                            sample = "5:30 PM",
                            isSelected = !use24Hour,
                            onClick = { on24HourChange(false) },
                            testTag = "setup_time_12h",
                            modifier = Modifier.weight(1f)
                        )
                        FormatChoiceChip(
                            title = strings.timeFormat24,
                            sample = "17:30",
                            isSelected = use24Hour,
                            onClick = { on24HourChange(true) },
                            testTag = "setup_time_24h",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Language Search Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(strings.languageDialogTitle) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = strings.clearSearch)
                    }
                }
            },
            singleLine = true,
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_language_search_input")
        )

        // Language List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filteredLanguages.forEach { lang ->
                val isSelected = currentLanguage == lang
                Card(
                    onClick = { onLanguageChange(lang) },
                    shape = ListRowShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("setup_language_${lang.code}")
                        .testTag("setup_language_item_${lang.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NourSpace.md, vertical = NourSpace.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(NourSize.icon)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(NourSpace.md))
    }
}

@Composable
private fun FormatChoiceChip(
    title: String,
    sample: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = ChipExpressiveShape,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)
        ),
        modifier = modifier
            .heightIn(min = NourSize.touchTarget)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = NourSpace.sm, vertical = NourSpace.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sample,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(NourSize.smallIcon)
                )
            }
        }
    }
}

// ============================================================================
// Step 2: Location, Calculation Authority & Live Prayer Times Preview
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupLocationStep(
    userLocation: UserLocation,
    permissionGranted: Boolean,
    detecting: Boolean,
    hasError: Boolean,
    onUseLocation: () -> Unit,
    onOpenCityPicker: () -> Unit,
    calculationMethod: CalculationMethod,
    onCalculationMethodChange: (CalculationMethod) -> Unit,
    madhab: Madhab,
    onMadhabChange: (Madhab) -> Unit,
    use24Hour: Boolean,
    useEasternNumerals: Boolean
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    // Calculate today's prayer times live for the selected city and authority!
    val todayTimes = remember(userLocation, calculationMethod, madhab) {
        PrayerCalculationEngine.calculate(
            date = Date(),
            location = userLocation,
            method = calculationMethod,
            madhab = madhab,
            highLatRule = HighLatitudeRule.ANGLE_BASED,
            adjustments = PrayerAdjustments()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        StepHeader(
            icon = Icons.Rounded.LocationOn,
            title = strings.sectionLocation,
            subtitle = strings.setupLocationSubtitle
        )

        // Current Location Status Card
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userLocation.cityName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userLocation.countryName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Auto-detected or Manual Badge
                    Surface(
                        shape = PillShape,
                        color = if (userLocation.isAutoDetected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (userLocation.isAutoDetected) "GPS" else strings.accuracyManual,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (userLocation.isAutoDetected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = NourSpace.sm, vertical = NourSpace.xs)
                        )
                    }
                }

                // Coordinates & Timezone Offset
                Surface(
                    shape = ChipExpressiveShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = NourSpace.sm, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Numerals.convert(
                                String.format(Locale.US, "%.2f° N, %.2f° E", userLocation.latitude, userLocation.longitude),
                                useEasternNumerals
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val tzSign = if (userLocation.timezoneOffsetHours >= 0) "+" else ""
                        Text(
                            text = Numerals.convert("UTC${tzSign}${userLocation.timezoneOffsetHours.toInt()}:00", useEasternNumerals),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // GPS Detection Error message if any
                if (hasError) {
                    Text(
                        text = strings.unableToLocate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Actions: GPS & Manual City Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
                ) {
                    Button(
                        onClick = onUseLocation,
                        enabled = !detecting,
                        shape = ButtonExpressiveShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = NourSize.touchTarget)
                            .testTag("setup_use_gps")
                            .testTag("setup_use_gps_btn")
                    ) {
                        if (detecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.setupUseMyLocation,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenCityPicker,
                        shape = ButtonExpressiveShape,
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = NourSize.touchTarget)
                            .testTag("setup_open_city_picker")
                            .testTag("setup_select_city")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.selectCity,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Calculation Authority & Madhab Selector
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md)
            ) {
                // Method Selector
                var methodExpanded by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                    Text(
                        text = strings.calculationAuthority,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        onClick = { methodExpanded = true },
                        shape = ChipExpressiveShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = NourSize.touchTarget)
                            .testTag("setup_method_dropdown")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = calculationMethod.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            CalculationMethod.entries.forEach { method ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = method.title,
                                            fontWeight = if (method == calculationMethod) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onCalculationMethodChange(method)
                                        methodExpanded = false
                                    },
                                    leadingIcon = if (method == calculationMethod) {
                                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                                    } else null,
                                    modifier = Modifier.testTag("setup_method_item_${method.name}")
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                // Madhab Selector
                Column(verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                    Text(
                        text = strings.madhabTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
                    ) {
                        FormatChoiceChip(
                            title = "Standard (Shafi'i/Maliki/Hanbali)",
                            sample = "1x Shadow",
                            isSelected = madhab == Madhab.SHAFI_MALIKI_HANBALI,
                            onClick = { onMadhabChange(Madhab.SHAFI_MALIKI_HANBALI) },
                            testTag = "setup_madhab_standard",
                            modifier = Modifier.weight(1f)
                        )
                        FormatChoiceChip(
                            title = "Hanafi",
                            sample = "2x Shadow (Later Asr)",
                            isSelected = madhab == Madhab.HANAFI,
                            onClick = { onMadhabChange(Madhab.HANAFI) },
                            testTag = "setup_madhab_hanafi",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Live Prayer Schedule Preview
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.setupLiveTimesTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NourSpace.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = strings.calculatedBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // 6 Prayer Times Horizontal Row
                val prayers = listOf(
                    Triple(PrayerName.FAJR, strings.fajr, Icons.Rounded.DarkMode),
                    Triple(PrayerName.SUNRISE, strings.sunrise, Icons.Rounded.WbSunny),
                    Triple(PrayerName.DHUHR, strings.dhuhr, Icons.Rounded.LightMode),
                    Triple(PrayerName.ASR, strings.asr, Icons.Rounded.LightMode),
                    Triple(PrayerName.MAGHRIB, strings.maghrib, Icons.Rounded.DarkMode),
                    Triple(PrayerName.ISHA, strings.isha, Icons.Rounded.DarkMode)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(prayers, key = { it.first.name }) { (prayer, label, icon) ->
                        val timeStr = todayTimes.formattedTime(prayer, use24Hour)
                        val formattedTime = Numerals.convert(timeStr, useEasternNumerals)

                        Surface(
                            shape = ChipExpressiveShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.widthIn(min = 92.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(NourSpace.sm),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Text(
                    text = strings.setupLiveTimesDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(NourSpace.md))
    }
}

// ============================================================================
// Step 3: Adhan & Punctual Alerts
// ============================================================================
@Composable
private fun SetupAlertsStep(
    alarmsEnabled: Boolean,
    onAlarmsEnabledChange: (Boolean) -> Unit,
    notifRowVisible: Boolean,
    notifGranted: Boolean,
    onNotifToggle: () -> Unit,
    exactRowVisible: Boolean,
    canExact: Boolean,
    onExactClick: () -> Unit,
    selectedToneId: String,
    onToneSelect: (String) -> Unit,
    isTestingAudio: Boolean,
    onTestAudioToggle: () -> Unit,
    earlyReminderEnabled: Boolean,
    onEarlyReminderChange: (Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        StepHeader(
            icon = Icons.Rounded.NotificationsActive,
            title = strings.sectionAdhan,
            subtitle = strings.setupAlertsSubtitle
        )

        // Master Prayer Alarms Switch Card
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(NourSize.badge)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(NourSize.rowIcon)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = strings.alarmsMasterTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.alarmsMasterDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = alarmsEnabled,
                    onCheckedChange = onAlarmsEnabledChange,
                    modifier = Modifier
                        .testTag("setup_alarms_master_switch")
                        .testTag("setup_alerts_switch")
                )
            }
        }

        // Notification Permission Row (Android 13+)
        if (notifRowVisible) {
            Card(
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NourSpace.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (notifGranted)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(NourSize.badge)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = if (notifGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(NourSize.rowIcon)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = strings.setupNotifTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (notifGranted) strings.notifPermissionGranted else strings.notifPermissionNeeded,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = notifGranted,
                        onCheckedChange = { onNotifToggle() },
                        modifier = Modifier.testTag("setup_notif_switch")
                    )
                }
            }
        }

        // Exact Alarm Permission (Android 12+)
        if (exactRowVisible) {
            Card(
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NourSpace.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (canExact)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(NourSize.badge)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = if (canExact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(NourSize.rowIcon)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = strings.setupExactAlarmTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (canExact) strings.exactAlarmActive else strings.exactAlarmRecommended,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!canExact) {
                        Button(
                            onClick = onExactClick,
                            shape = PillShape,
                            modifier = Modifier
                                .testTag("setup_exact_alarm")
                                .testTag("setup_exact_alarm_btn")
                        ) {
                            Text(strings.enableAction)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(NourSize.icon)
                        )
                    }
                }
            }
        }

        // Adhan Voice Selection & Interactive Audio Tester
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.md)
            ) {
                Text(
                    text = strings.adhanTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // 3 Voices Choices
                val tones = listOf(
                    Triple(AdhanTones.MAKKAH, strings.adhanToneMakkah, strings.adhanToneMakkahDesc),
                    Triple(AdhanTones.HAYYA, strings.adhanToneHayya, strings.adhanToneHayyaDesc),
                    Triple(AdhanTones.GENTLE, strings.adhanToneGentle, strings.adhanToneGentleDesc)
                )

                tones.forEach { (toneId, name, desc) ->
                    val isSelected = selectedToneId == toneId
                    Surface(
                        onClick = { onToneSelect(toneId) },
                        shape = ListRowShape,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("setup_adhan_tone_$toneId")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(NourSize.smallIcon)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                // Interactive Audio Test Button & Animated Waveform
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
                    ) {
                        if (isTestingAudio) {
                            AnimatedSoundwaveVisualizer()
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(NourSize.icon)
                            )
                        }

                        Text(
                            text = if (isTestingAudio) "Playing Adhan..." else "Preview Selected Voice",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onTestAudioToggle,
                        shape = ButtonExpressiveShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTestingAudio)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .testTag("setup_test_audio")
                            .testTag("setup_test_audio_btn")
                    ) {
                        Icon(
                            imageVector = if (isTestingAudio) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTestingAudio) strings.stopAudio else strings.testAudio)
                    }
                }
            }
        }

        // Early Reminder (Pre-Adhan 15m)
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NourSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.setupPreAdhanTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.setupPreAdhanSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = earlyReminderEnabled,
                    onCheckedChange = onEarlyReminderChange,
                    modifier = Modifier.testTag("setup_early_reminder_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(NourSpace.md))
    }
}

/**
 * Animated Soundwave Visualizer with bouncing bars.
 *
 * Purely decorative. When the user has reduced motion enabled the bars are drawn
 * at a fixed mid-height instead of looping forever, so the control still reads
 * as an audio indicator without continuous movement.
 */
@Composable
private fun AnimatedSoundwaveVisualizer() {
    val reducedMotion = NourMotion.reduced
    // Static mid-range heights used when motion is reduced.
    val (h1, h2, h3) = if (reducedMotion) {
        Triple(14f, 14f, 14f)
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "soundwave_transition")
        val a by infiniteTransition.animateFloat(
            initialValue = 6f,
            targetValue = 22f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar1"
        )
        val b by infiniteTransition.animateFloat(
            initialValue = 20f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(320, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar2"
        )
        val c by infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = 24f,
            animationSpec = infiniteRepeatable(
                animation = tween(480, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar3"
        )
        Triple(a, b, c)
    }
    val height1 = h1
    val height2 = h2
    val height3 = h3

    Row(
        modifier = Modifier
            .size(width = 28.dp, height = 24.dp)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(height1.dp)
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(height2.dp)
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(height3.dp)
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

// ============================================================================
// Step 4: Celebration & Configuration Review
// ============================================================================
@Composable
private fun SetupReadyStep(
    userLocation: UserLocation,
    calculationMethod: CalculationMethod,
    madhab: Madhab,
    selectedToneId: String,
    alarmsEnabled: Boolean,
    currentLanguage: AppLanguage,
    use24Hour: Boolean,
    useEasternNumerals: Boolean
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
        Spacer(modifier = Modifier.height(NourSpace.xs))

        // Celebratory Emblem
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
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }

        // Celebration Title & Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = NourSpace.sm)
        ) {
            Text(
                text = strings.setupReadyTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(NourSpace.xs))
            Text(
                text = strings.setupReadySubtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Configuration Card
        Card(
            shape = CardExpressiveShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NourSpace.md),
                verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                SummaryReviewRow(
                    label = strings.setupSummaryLocation,
                    value = "${userLocation.cityName}, ${userLocation.countryName}",
                    icon = Icons.Rounded.LocationOn
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SummaryReviewRow(
                    label = strings.setupSummaryMethod,
                    value = "${calculationMethod.title} • ${madhab.title}",
                    icon = Icons.Rounded.Tune
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SummaryReviewRow(
                    label = strings.setupSummaryAdhan,
                    value = "${AdhanTones.displayName(selectedToneId, strings)} • ${if (alarmsEnabled) "Alerts On" else "Muted"}",
                    icon = Icons.Rounded.Alarm
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SummaryReviewRow(
                    label = strings.setupSummaryLanguage,
                    value = "${currentLanguage.nativeName} • ${if (use24Hour) "24h" else "12h"} • ${if (useEasternNumerals) "١٢٣" else "123"}",
                    icon = Icons.Rounded.Language
                )
            }
        }

        // Reassurance Note
        Surface(
            shape = CardSubtleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(NourSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(NourSize.smallIcon)
                )
                Text(
                    text = strings.setupReassurance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(NourSpace.md))
    }
}

@Composable
private fun SummaryReviewRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// Step Header Helper
// ============================================================================
@Composable
private fun StepHeader(
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
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(NourSize.rowIcon)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// World City Picker Modal Dialog
// ============================================================================
@Composable
private fun WorldCityPickerDialog(
    onDismiss: () -> Unit,
    onSelectLocation: (UserLocation) -> Unit
) {
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        if (query.isBlank()) WORLD_LOCATIONS
        else WORLD_LOCATIONS.filter {
            it.capital.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.selectCity,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(strings.searchLocationHint) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = strings.clearSearch)
                            }
                        }
                    },
                    singleLine = true,
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_city_search")
                        .testTag("setup_city_search_input")
                )

                Spacer(modifier = Modifier.height(NourSpace.sm))

                // Holy Cities Quick Row
                if (query.isBlank()) {
                    Text(
                        text = strings.holyCitiesLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = NourSpace.xs)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NourSpace.xs)
                    ) {
                        HOLY_CITIES.forEach { holy ->
                            Surface(
                                onClick = {
                                    onSelectLocation(holy.toUserLocation())
                                },
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = holy.capital,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = NourSpace.sm)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(NourSpace.sm))
                }

                // Filtered List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                ) {
                    items(filtered) { item ->
                        Surface(
                            onClick = {
                                onSelectLocation(item.toUserLocation())
                            },
                            shape = ListRowShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = NourSize.touchTarget)
                                .testTag("setup_city_item_${item.capital}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.capital,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.country,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = item.utcLabel(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        },
        shape = DialogExpressiveShape
    )
}
