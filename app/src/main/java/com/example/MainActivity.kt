package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.tween
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourReduceMotion
import com.example.ui.theme.NourSize
import com.example.ui.components.NourContentFrame
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.example.features.location.DeviceLocationHelper
import com.example.features.location.rememberLocationPermissionState
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.ui.PrayerScreen
import com.example.features.qibla.ui.QiblaScreen
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.NourPreferences
import com.example.features.onboarding.ui.SetupWizard
import com.example.features.settings.ui.SettingsScreen
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.LocalUseEasternNumerals
import com.example.localization.LocalUse24HourFormat
import com.example.localization.LocalizationManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NourSpace
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import java.util.Date

enum class NourNavDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SALAH(Icons.Rounded.Schedule, Icons.Outlined.Schedule, "nav_salah"),
    QIBLA(Icons.Rounded.Explore, Icons.Outlined.Explore, "nav_qibla"),
    QURAN(Icons.Rounded.AutoStories, Icons.Outlined.AutoStories, "nav_quran"),
    SETTINGS(Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_settings");

    fun getTitle(strings: AppStrings): String = when (this) {
        SALAH -> strings.navSalah
        QIBLA -> strings.navQibla
        QURAN -> strings.navQuran
        SETTINGS -> strings.navSettings
    }
}

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalizationManager.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val appearance = remember { NourPreferences(context) }
            var themeMode by remember { mutableStateOf(appearance.themeMode) }
            var dynamicColors by remember { mutableStateOf(appearance.dynamicColors) }
            var seedColorArgb by remember { mutableStateOf(appearance.seedColorArgb) }
            val reduceMotion = NourReduceMotion.remember()
            // Publish to the animation-spec helpers (no composable context there).
            SideEffect { NourMotion.reduced = reduceMotion }
            CompositionLocalProvider(NourReduceMotion.Local provides reduceMotion) {
            MyApplicationTheme(
                darkTheme = when (themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> androidx.compose.foundation.isSystemInDarkTheme()
                },
                dynamicColor = dynamicColors,
                seedColorArgb = seedColorArgb
            ) {
                NourMainApp(themeMode, dynamicColors,
                    onThemeModeChange = { themeMode = it; appearance.themeMode = it },
                    onDynamicColorsChange = { dynamicColors = it; appearance.dynamicColors = it },
                    seedColorArgb = seedColorArgb,
                    onSeedChange = { argb ->
                        seedColorArgb = argb
                        appearance.seedColorArgb = argb
                        if (argb != null && dynamicColors) {
                            dynamicColors = false
                            appearance.dynamicColors = false
                        }
                    })
            }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NourMainApp(
    themeMode: String = "system",
    dynamicColors: Boolean = false,
    onThemeModeChange: (String) -> Unit = {},
    onDynamicColorsChange: (Boolean) -> Unit = {},
    seedColorArgb: Int? = null,
    onSeedChange: (Int?) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionState = rememberLocationPermissionState()
    val savedLanguage = remember { LocalizationManager.getSavedLanguage(context) }
    var currentLanguage by remember { mutableStateOf(savedLanguage) }
    val strings = remember(currentLanguage) { AppStrings.forLanguage(currentLanguage) }
    val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Keep system locale (dates, numbers, permission dialogs) in sync with in-app choice
    LaunchedEffect(currentLanguage) { LocalizationManager.applySystemLocale(context, currentLanguage) }

    val prefs = remember { NourPreferences(context) }

    var useEasternNumerals by remember { mutableStateOf(prefs.numeralEastern) }
    LaunchedEffect(useEasternNumerals) { prefs.numeralEastern = useEasternNumerals }
    var use24Hour by remember { mutableStateOf(prefs.time24h) }
    LaunchedEffect(use24Hour) { prefs.time24h = use24Hour }
    var alarmsEnabled by remember { mutableStateOf(prefs.alarmsEnabled) }

    var currentDestination by remember { mutableStateOf(NourNavDestination.SALAH) }
    var setupDone by remember {
        mutableStateOf(
            prefs.setupCompleted || context
                .getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
                .getBoolean("has_asked_location_first_time", false)
        )
    }

    // User Prayer Settings state, restored from durable preferences
    var userLocation by remember {
        mutableStateOf(
            prefs.savedLocation ?: UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)
        )
    }
    var calculationMethod by remember { mutableStateOf(prefs.calculationMethod) }
    var madhab by remember { mutableStateOf(prefs.madhab) }
    var highLatRule by remember { mutableStateOf(prefs.highLatRule) }
    var adjustments by remember { mutableStateOf(prefs.adjustments) }

    // Persist prayer preferences whenever they change
    LaunchedEffect(userLocation) { prefs.savedLocation = userLocation }
    LaunchedEffect(calculationMethod) { prefs.calculationMethod = calculationMethod }
    LaunchedEffect(madhab) { prefs.madhab = madhab }
    LaunchedEffect(highLatRule) { prefs.highLatRule = highLatRule }
    LaunchedEffect(adjustments) { prefs.adjustments = adjustments }

    // Helper to refresh location via GPS
    val refreshLocation: () -> Unit = {
        coroutineScope.launch {
            val loc = DeviceLocationHelper.getCurrentLocation(context)
            if (loc != null) {
                val resolved = DeviceLocationHelper.resolveUserLocation(context, loc)
                userLocation = resolved
                Toast.makeText(context, "${strings.locationUpdatedPrefix}: ${resolved.cityName}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, strings.unableGps, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Automatically retrieve location when permission is granted and location is not yet auto-detected
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted && !userLocation.isAutoDetected) {
            val loc = DeviceLocationHelper.getCurrentLocation(context)
            if (loc != null) {
                val resolved = DeviceLocationHelper.resolveUserLocation(context, loc)
                userLocation = resolved
            }
        }
    }

    val handleRequestLocationUpdate: () -> Unit = {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        } else {
            refreshLocation()
        }
    }

    // Re-run scheduling whenever Adhan switches change in Settings
    var adhanScheduleTick by remember { mutableStateOf(0) }


    // Schedule Adhan alerts + pre-Adhan reminders for today's prayers.
    // Skipped entirely while the master prayer-alarms switch is off.
    LaunchedEffect(userLocation, calculationMethod, madhab, highLatRule, adjustments, adhanScheduleTick, alarmsEnabled) {
        prefs.alarmsEnabled = alarmsEnabled
        if (!alarmsEnabled) return@LaunchedEffect
        val todayTimes = PrayerCalculationEngine.calculate(
            date = Date(),
            location = userLocation,
            method = calculationMethod,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )

        PrayerNotificationScheduler.scheduleDay(context, todayTimes)
        PrayerNotificationScheduler.scheduleOfflineRollingWindow(context)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppLanguage provides currentLanguage,
        LocalAppStrings provides strings,
        LocalUseEasternNumerals provides useEasternNumerals,
        LocalUse24HourFormat provides use24Hour
    ) {
        if (!setupDone) {
            SetupWizard(
                currentLanguage = currentLanguage,
                onLanguageChange = { newLang ->
                    currentLanguage = newLang
                    LocalizationManager.saveLanguage(context, newLang)
                },
                userLocation = userLocation,
                onLocationChange = { userLocation = it },
                alarmsEnabled = alarmsEnabled,
                onAlarmsEnabledChange = { enabled ->
                    alarmsEnabled = enabled
                    if (enabled) {
                        adhanScheduleTick++
                    } else {
                        coroutineScope.launch {
                            PrayerNotificationScheduler.cancelAllAlarms(context)
                        }
                    }
                },
                calculationMethod = calculationMethod,
                onCalculationMethodChange = { calculationMethod = it },
                madhab = madhab,
                onMadhabChange = { madhab = it },
                useEasternNumerals = useEasternNumerals,
                onEasternNumeralsChange = { useEasternNumerals = it },
                use24Hour = use24Hour,
                on24HourChange = { use24Hour = it },
                onSetupComplete = {
                    prefs.setupCompleted = true
                    context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("has_asked_location_first_time", true).apply()
                    setupDone = true
                }
            )
        } else {
            // Predictive back (enabled by default for targetSdk 36): from any
            // secondary destination, back returns to the prayer screen instead of
            // unexpectedly exiting the app. On the Salah tab the system default
            // (leave the app) still applies. Screens with an internal layer - the
            // Quran reader - register their own BackHandler, which is composed
            // later and therefore takes precedence while that layer is open.
            BackHandler(enabled = currentDestination != NourNavDestination.SALAH) {
                currentDestination = NourNavDestination.SALAH
            }
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= NourSize.compactBreakpoint

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("nour_main_scaffold"),
                // Match the screens' own surface so the global side gutter
                // (applied in NourContentFrame) reads as one continuous page.
                containerColor = MaterialTheme.colorScheme.surface,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nour_bottom_navigation"),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = NourSize.flat
                        ) {
                            NourNavDestination.values().forEach { destination ->
                                val isSelected = currentDestination == destination
                                val title = destination.getTitle(strings)
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.testTag(destination.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .testTag("nour_navigation_rail"),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            header = {
                                Spacer(modifier = Modifier.padding(top = NourSpace.xl))
                            }
                        ) {
                            NourNavDestination.values().forEach { destination ->
                                val isSelected = currentDestination == destination
                                val title = destination.getTitle(strings)
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.testTag(destination.testTag)
                                )
                            }
                        }
                    }

                    NourContentFrame(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                            // On the rail (expanded) layout there is no bottom bar,
                            // so apply the navigation-bar inset here or the last row
                            // can sit under the gesture/navigation bar. On the compact
                            // layout the NavigationBar already accounts for it.
                            .then(
                                if (isExpandedScreen) {
                                    Modifier.windowInsetsPadding(
                                        WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                                    )
                                } else Modifier
                            ),
                        maxWidth = when (currentDestination) {
                            NourNavDestination.SALAH -> androidx.compose.ui.unit.Dp.Infinity
                            NourNavDestination.QURAN -> NourSize.readingWidth
                            else -> NourSize.contentWidth
                        }
                    ) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = {
                                fadeIn(tween(NourMotion.duration(NourMotion.medium), easing = NourMotion.easing))
                                    .togetherWith(fadeOut(tween(NourMotion.duration(NourMotion.short), easing = NourMotion.easing)))
                            },
                            label = "m3e_nav_transition"
                        ) { destination ->
                            when (destination) {
                                NourNavDestination.SALAH -> {
                                    PrayerScreen(
                                        location = userLocation,
                                        calculationMethod = calculationMethod,
                                        madhab = madhab,
                                        highLatRule = highLatRule,
                                        adjustments = adjustments,
                                        onRequestLocationUpdate = handleRequestLocationUpdate
                                    )
                                }
                                NourNavDestination.QIBLA -> {
                                    QiblaScreen(
                                        location = userLocation,
                                        onLocationUpdated = { userLocation = it }
                                    )
                                }
                                NourNavDestination.QURAN -> {
                                    QuranScreen()
                                }
                                NourNavDestination.SETTINGS -> {
                                    SettingsScreen(
                                        themeMode = themeMode,
                                        dynamicColors = dynamicColors,
                                        onThemeModeChange = onThemeModeChange,
                                        onDynamicColorsChange = onDynamicColorsChange,
                                        location = userLocation,
                                        calculationMethod = calculationMethod,
                                        madhab = madhab,
                                        highLatRule = highLatRule,
                                        adjustments = adjustments,
                                        currentLanguage = currentLanguage,
                                        onLocationChange = { userLocation = it },
                                        onMethodChange = { calculationMethod = it },
                                        onMadhabChange = { madhab = it },
                                        onHighLatRuleChange = { highLatRule = it },
                                        onAdjustmentsChange = { adjustments = it },
                                        onLanguageChange = { newLang ->
                                            currentLanguage = newLang
                                            LocalizationManager.saveLanguage(context, newLang)
                                        },
                                        useEasternNumerals = useEasternNumerals,
                                        onNumeralsChange = { useEasternNumerals = it },
                                        use24Hour = use24Hour,
                                        onTimeFormatChange = { use24Hour = it },
                                        seedColorArgb = seedColorArgb,
                                        onSeedChange = onSeedChange,
                                        onReopenSetup = { setupDone = false },
                                        onAdhanSettingsChanged = { adhanScheduleTick++ },
                                        alarmsEnabled = alarmsEnabled,
                                        onAlarmsEnabledChange = { enabled ->
                                            alarmsEnabled = enabled
                                            if (enabled) {
                                                adhanScheduleTick++
                                            } else {
                                                coroutineScope.launch {
                                                    PrayerNotificationScheduler.cancelAllAlarms(context)
                                                }
                                            }
                                        },
                                        onOpenExactAlarmSettings = {
                                            if (Build.VERSION.SDK_INT >= 31) {
                                                try {
                                                    context.startActivity(
                                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                            data = Uri.parse("package:${context.packageName}")
                                                        }
                                                    )
                                                } catch (_: Exception) {
                                                    // Some OEM builds omit this settings screen.
                                                    // Fall back to the app's own details page, and
                                                    // tell the user if even that fails rather
                                                    // than swallowing the error silently.
                                                    val fallback = Intent(
                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                        Uri.parse("package:${context.packageName}")
                                                    )
                                                    val ok = runCatching {
                                                        context.startActivity(fallback)
                                                    }.isSuccess
                                                    if (!ok) {
                                                        Toast.makeText(
                                                            context,
                                                            strings.exactAlarmSettingsUnavailable,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

