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
import com.example.ui.components.LocalFloatingBottomExtra
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.NourFloatingNavigationBar
import com.example.ui.components.NourFloatingNavigationRail
import com.example.ui.components.NourNavItem
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

private val NourNavDestinationSaver: Saver<NourNavDestination, String> = Saver(
    save = { it.name },
    restore = { NourNavDestination.valueOf(it) }
)

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalizationManager.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            // Android's default contrast scrim paints a thin grey band across
            // the system gesture area: it dimmed whatever our page drew there
            // and read as a dead strip at the bottom of every screen. Our
            // surfaces are opaque and full-contrast — draw our own page
            // cleanly under the gesture pill instead.
            window.isNavigationBarContrastEnforced = false
        }
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
                    onDynamicColorsChange = { enabled ->
                        dynamicColors = enabled
                        appearance.dynamicColors = enabled
                        if (enabled) {
                            seedColorArgb = null
                            appearance.seedColorArgb = null
                        }
                    },
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

    var currentDestination by rememberSaveable(stateSaver = NourNavDestinationSaver) {
        mutableStateOf(NourNavDestination.SALAH)
    }
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
            // Live bottom-bar height, measured by the bar itself. The screens'
            // bottom reserve derives from it so the reserve is the pill's real
            // band — not an assumed token — at every font scale.
            var floatingBarHeight by remember { mutableStateOf(NourSize.navBarHeight) }
            val navItems = NourNavDestination.values().map { destination ->
                NourNavItem(
                    label = destination.getTitle(strings),
                    selectedIcon = destination.selectedIcon,
                    unselectedIcon = destination.unselectedIcon,
                    testTag = destination.testTag
                )
            }

            Box(Modifier.fillMaxSize().testTag("nour_main_scaffold")) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                // Match the screens' own surface so the global side gutter
                // (applied in NourContentFrame) reads as one continuous page.
                containerColor = MaterialTheme.colorScheme.surface,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {}
            ) { innerPadding ->
                val navBottomInset = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
                // NO bottom padding here: the scroll content stays free to
                // draw under the system gesture area — the list scrolls
                // beneath the gesture pill, so the zone is part of the page,
                // not a dead band. Each layout budgets its own bottom inset
                // via the screens' LocalFloatingBottomExtra below.
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {                    if (isExpandedScreen) {
                        NourFloatingNavigationRail(
                            items = navItems,
                            selectedIndex = currentDestination.ordinal,
                            onSelected = { index ->
                                currentDestination = NourNavDestination.values()[index]
                            },
                            modifier = Modifier
                                .width(NourSize.navRailReserve)
                                .fillMaxHeight()
                                .testTag("nour_navigation_rail")
                        )
                    }

                    AnimatedContent(
                        targetState = currentDestination,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                        transitionSpec = {
                            fadeIn(tween(NourMotion.duration(NourMotion.medium), easing = NourMotion.easing))
                                .togetherWith(fadeOut(tween(NourMotion.duration(NourMotion.short), easing = NourMotion.easing)))
                        },
                        label = "m3e_nav_transition"
                    ) { destination ->
                        // Keep the frame inside the transition: the outgoing
                        // screen must not be re-measured with the incoming
                        // screen's gutter while it fades.
                        // Floating breathing rule: the screens' bottom padding
                        // is absolute — the system navigation inset + the 8 dp
                        // gap the bar keeps above it + the bar's real measured
                        // band + an 8 dp breath — so the last resting row sits
                        // one small breath above the band's edge (readable,
                        // never hidden behind the pill), while the list stays
                        // free to draw under the gesture area while scrolling.
                        // Rail (expanded) layout has no bottom bar: the inset
                        // plus the 16 dp baseline.
                        val floatingBottomExtra = if (isExpandedScreen) {
                            navBottomInset + NourSpace.lg
                        } else {
                            (navBottomInset + NourSpace.sm + floatingBarHeight + NourSpace.sm)
                                .coerceAtLeast(0.dp)
                        }
                        CompositionLocalProvider(
                            LocalFloatingBottomExtra provides floatingBottomExtra
                        ) {
                            NourContentFrame(
                                modifier = Modifier.fillMaxSize(),
                                maxWidth = when (destination) {
                                    NourNavDestination.SALAH -> androidx.compose.ui.unit.Dp.Infinity
                                    NourNavDestination.QURAN -> NourSize.readingWidth
                                    else -> NourSize.contentWidth
                                },
                                // Salah's sky hero bleeds edge-to-edge; the screen
                                // insets its own content (cards keep the shared
                                // NourSpace.lg measure). Other screens use the frame.
                                contentPadding = if (destination == NourNavDestination.SALAH)
                                    PaddingValues(0.dp)
                                else
                                    PaddingValues(horizontal = NourSpace.lg)
                            ) {
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

            if (!isExpandedScreen) {
                NourFloatingNavigationBar(
                    items = navItems,
                    selectedIndex = currentDestination.ordinal,
                    onSelected = { index ->
                        currentDestination = NourNavDestination.values()[index]
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .testTag("nour_bottom_navigation"),
                    onHeightMeasured = { floatingBarHeight = it }
                )
            }
            }
        }
    }
}
}
}
