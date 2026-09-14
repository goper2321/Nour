package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.ui.PrayerScreen
import com.example.features.qibla.ui.QiblaScreen
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.ui.SettingsScreen
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.LocalizationManager
import com.example.ui.theme.MyApplicationTheme
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NourMainApp()
            }
        }
    }
}

@Composable
fun NourMainApp() {
    val context = LocalContext.current

    val savedLanguage = remember { LocalizationManager.getSavedLanguage(context) }
    var currentLanguage by remember { mutableStateOf(savedLanguage) }
    val strings = remember(currentLanguage) { AppStrings.forLanguage(currentLanguage) }
    val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    var currentDestination by remember { mutableStateOf(NourNavDestination.SALAH) }

    // User Prayer Settings state
    var userLocation by remember {
        mutableStateOf(UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0))
    }
    var calculationMethod by remember { mutableStateOf(CalculationMethod.MUSLIM_WORLD_LEAGUE) }
    var madhab by remember { mutableStateOf(Madhab.SHAFI_MALIKI_HANBALI) }
    var highLatRule by remember { mutableStateOf(HighLatitudeRule.ANGLE_BASED) }
    var adjustments by remember { mutableStateOf(PrayerAdjustments()) }

    // Schedule exact alarms for today's prayers on launch
    LaunchedEffect(userLocation, calculationMethod, madhab, adjustments) {
        val todayTimes = PrayerCalculationEngine.calculate(
            date = Date(),
            location = userLocation,
            method = calculationMethod,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )

        PrayerName.values().filter { it != PrayerName.SUNRISE }.forEach { p ->
            val timeMillis = todayTimes.getTimeFor(p).time
            if (timeMillis > System.currentTimeMillis()) {
                PrayerNotificationScheduler.scheduleExactAlarm(context, p, timeMillis, isPreReminder = false)
            }
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppLanguage provides currentLanguage,
        LocalAppStrings provides strings
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("nour_main_scaffold"),
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nour_bottom_navigation"),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 3.dp
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
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .testTag("nour_navigation_rail"),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            header = {
                                Spacer(modifier = Modifier.padding(top = 16.dp))
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

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = {
                                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        scaleIn(
                                            initialScale = 0.96f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                                        ))
                                    .togetherWith(
                                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                    )
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
                                        onOpenAdjustments = { currentDestination = NourNavDestination.SETTINGS },
                                        onOpenSettings = { currentDestination = NourNavDestination.SETTINGS }
                                    )
                                }
                                NourNavDestination.QIBLA -> {
                                    QiblaScreen(location = userLocation)
                                }
                                NourNavDestination.QURAN -> {
                                    QuranScreen()
                                }
                                NourNavDestination.SETTINGS -> {
                                    SettingsScreen(
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Nour - $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
