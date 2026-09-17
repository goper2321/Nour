package com.example.features.settings.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Translate
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.PillShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.AdhanSettingsEntity
import com.example.database.NourDatabase
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    location: UserLocation,
    calculationMethod: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments,
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onLocationChange: (UserLocation) -> Unit,
    onMethodChange: (CalculationMethod) -> Unit,
    onMadhabChange: (Madhab) -> Unit,
    onHighLatRuleChange: (HighLatitudeRule) -> Unit,
    onAdjustmentsChange: (PrayerAdjustments) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }
    val strings = LocalAppStrings.current

    val adhanSettingsList by db.adhanSettingsDao().getAllSettings().collectAsState(initial = emptyList())

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAdjustmentsDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isTestingAdhan by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.settingsSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // App Language Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_language_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Translate,
                                        contentDescription = "Language",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.languageSectionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentLanguage.nativeName} (${currentLanguage.displayName})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = strings.languageSectionSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showLanguageDialog = true },
                            shape = PillShape,
                            modifier = Modifier.testTag("btn_change_language")
                        ) {
                            Text(strings.changeLocation)
                        }
                    }
                }
            }
        }

        // Location & City
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.activeLocation,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${location.cityName}, ${location.countryName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Lat: ${String.format("%.4f", location.latitude)}, Lng: ${String.format("%.4f", location.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showLocationDialog = true },
                            shape = PillShape
                        ) {
                            Text(strings.changeLocation)
                        }
                    }
                }
            }
        }

        // Calculation Authority & Method
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.calculationAuthority,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var methodMenuOpen by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { methodMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardSubtleShape
                    ) {
                        Text(text = calculationMethod.title, maxLines = 1)
                    }

                    DropdownMenu(
                        expanded = methodMenuOpen,
                        onDismissRequest = { methodMenuOpen = false }
                    ) {
                        CalculationMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.title) },
                                onClick = {
                                    onMethodChange(method)
                                    methodMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Madhab & Asr Calculation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.madhabTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Madhab.values().forEach { m ->
                        val localizedTitle = when (m) {
                            Madhab.STANDARD -> strings.madhabStandard
                            Madhab.HANAFI -> strings.madhabHanafi
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = localizedTitle, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = madhab == m,
                                onCheckedChange = { if (it) onMadhabChange(m) }
                            )
                        }
                    }
                }
            }
        }

        // High Latitude Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.highLatTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    fun getLocalizedHighLat(rule: HighLatitudeRule): String = when (rule) {
                        HighLatitudeRule.ANGLE_BASED -> strings.highLatAngleBased
                        HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> strings.highLatMiddleOfNight
                        HighLatitudeRule.ONE_SEVENTH_OF_THE_NIGHT -> strings.highLatOneSeventh
                        HighLatitudeRule.NONE -> strings.highLatNone
                    }

                    var highLatMenuOpen by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { highLatMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardSubtleShape
                    ) {
                        Text(getLocalizedHighLat(highLatRule))
                    }

                    DropdownMenu(
                        expanded = highLatMenuOpen,
                        onDismissRequest = { highLatMenuOpen = false }
                    ) {
                        HighLatitudeRule.values().forEach { rule ->
                            DropdownMenuItem(
                                text = { Text(getLocalizedHighLat(rule)) },
                                onClick = {
                                    onHighLatRuleChange(rule)
                                    highLatMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Per-Prayer Minute Tuning
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.perPrayerAdjustments,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.fajr}: ${adjustments.fajr}m, ${strings.dhuhr}: ${adjustments.dhuhr}m, ${strings.asr}: ${adjustments.asr}m, ${strings.maghrib}: ${adjustments.maghrib}m, ${strings.isha}: ${adjustments.isha}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showAdjustmentsDialog = true },
                            shape = PillShape
                        ) {
                            Text(strings.tuneButton)
                        }
                    }
                }
            }
        }

        // Adhan & Notifications Sound Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.adhanTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = {
                                if (isTestingAdhan) {
                                    AdhanPlayer.stop()
                                    isTestingAdhan = false
                                } else {
                                    isTestingAdhan = true
                                    AdhanPlayer.playAdhan(
                                        context = context,
                                        toneName = "Makkah Adhan",
                                        volume = 1.0f,
                                        onCompletion = { isTestingAdhan = false }
                                    )
                                }
                            },
                            shape = PillShape
                        ) {
                            Text(if (isTestingAdhan) strings.stopAudio else strings.testAudio)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val prayers = listOf(
                        "FAJR" to strings.fajr,
                        "DHUHR" to strings.dhuhr,
                        "ASR" to strings.asr,
                        "MAGHRIB" to strings.maghrib,
                        "ISHA" to strings.isha
                    )
                    prayers.forEach { (pKey, pLabel) ->
                        val setting = adhanSettingsList.firstOrNull { it.prayerName == pKey }
                        val isEnabled = setting?.isAdhanEnabled ?: true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = pLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = setting?.toneName ?: strings.makkahAdhan, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        val newSetting = (setting ?: AdhanSettingsEntity(pKey)).copy(isAdhanEnabled = checked)
                                        db.adhanSettingsDao().saveSettings(newSetting)
                                    }
                                }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Privacy & Offline Audit Badge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.privacyPledgeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ${strings.privacyPledgeBullet1}\n• ${strings.privacyPledgeBullet2}\n• ${strings.privacyPledgeBullet3}\n• ${strings.privacyPledgeBullet4}\n• ${strings.privacyPledgeBullet5}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Per-Prayer Minute Tuning Dialog
    if (showAdjustmentsDialog) {
        var fajrAdj by remember { mutableStateOf(adjustments.fajr.toString()) }
        var sunriseAdj by remember { mutableStateOf(adjustments.sunrise.toString()) }
        var dhuhrAdj by remember { mutableStateOf(adjustments.dhuhr.toString()) }
        var asrAdj by remember { mutableStateOf(adjustments.asr.toString()) }
        var maghribAdj by remember { mutableStateOf(adjustments.maghrib.toString()) }
        var ishaAdj by remember { mutableStateOf(adjustments.isha.toString()) }

        AlertDialog(
            onDismissRequest = { showAdjustmentsDialog = false },
            shape = CardExpressiveShape,
            title = { Text(strings.tunePrayerMinutesTitle, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.tunePrayerMinutesDesc, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fajrAdj, onValueChange = { fajrAdj = it }, label = { Text(strings.fajr) }, modifier = Modifier.weight(1f), singleLine = true, shape = CardSubtleShape)
                        OutlinedTextField(value = sunriseAdj, onValueChange = { sunriseAdj = it }, label = { Text(strings.sunrise) }, modifier = Modifier.weight(1f), singleLine = true, shape = CardSubtleShape)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dhuhrAdj, onValueChange = { dhuhrAdj = it }, label = { Text(strings.dhuhr) }, modifier = Modifier.weight(1f), singleLine = true, shape = CardSubtleShape)
                        OutlinedTextField(value = asrAdj, onValueChange = { asrAdj = it }, label = { Text(strings.asr) }, modifier = Modifier.weight(1f), singleLine = true, shape = CardSubtleShape)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = maghribAdj, onValueChange = { maghribAdj = it }, label = { Text(strings.maghrib) }, modifier = Modifier.weight(1f), singleLine = true, shape = CardSubtleShape)
                        OutlinedTextField(value = ishaAdj, onValueChange = { ishaAdj = it }, label = { Text(strings.isha) }, modifier = Modifier.weight(1f), singleLine = true, shape = CardSubtleShape)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdjustmentsChange(
                            PrayerAdjustments(
                                fajr = fajrAdj.toIntOrNull() ?: 0,
                                sunrise = sunriseAdj.toIntOrNull() ?: 0,
                                dhuhr = dhuhrAdj.toIntOrNull() ?: 0,
                                asr = asrAdj.toIntOrNull() ?: 0,
                                maghrib = maghribAdj.toIntOrNull() ?: 0,
                                isha = ishaAdj.toIntOrNull() ?: 0
                            )
                        )
                        showAdjustmentsDialog = false
                    },
                    shape = PillShape
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustmentsDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
        val predefinedLocations = listOf(
            UserLocation(21.4225, 39.8262, "Makkah al-Mukarramah", "Saudi Arabia", 3.0),
            UserLocation(24.4672, 39.6111, "Madinah al-Munawwarah", "Saudi Arabia", 3.0),
            UserLocation(31.7761, 35.2358, "Al-Quds (Jerusalem)", "Palestine", 3.0),
            UserLocation(40.7128, -74.0060, "New York", "United States", -5.0),
            UserLocation(51.5074, -0.1278, "London", "United Kingdom", 0.0),
            UserLocation(48.8566, 2.3522, "Paris", "France", 1.0),
            UserLocation(30.0444, 31.2357, "Cairo", "Egypt", 2.0),
            UserLocation(33.5731, -7.5898, "Casablanca", "Morocco", 1.0),
            UserLocation(59.3293, 18.0686, "Stockholm", "Sweden", 1.0),
            UserLocation(3.1390, 101.6869, "Kuala Lumpur", "Malaysia", 8.0),
            UserLocation(-6.2088, 106.8456, "Jakarta", "Indonesia", 7.0),
            UserLocation(24.8607, 67.0011, "Karachi", "Pakistan", 5.0),
            UserLocation(25.2048, 55.2708, "Dubai", "UAE", 4.0),
            UserLocation(41.0082, 28.9784, "Istanbul", "Turkey", 3.0)
        )

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            shape = CardExpressiveShape,
            title = { Text(strings.selectCity, style = MaterialTheme.typography.titleLarge) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(predefinedLocations.size) { index ->
                        val loc = predefinedLocations[index]
                        Surface(
                            shape = CardSubtleShape,
                            color = if (location.cityName == loc.cityName) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    onLocationChange(loc)
                                    showLocationDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CardSubtleShape
                            ) {
                                Text(
                                    text = "${loc.cityName}, ${loc.countryName}",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text(strings.close) }
            }
        )
    }

    // App Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            shape = CardExpressiveShape,
            title = {
                Text(
                    text = strings.languageDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppLanguage.entries.size) { index ->
                        val lang = AppLanguage.entries[index]
                        val isSelected = currentLanguage == lang
                        Surface(
                            shape = CardSubtleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                }
                                .testTag("language_option_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.nativeName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }
}
