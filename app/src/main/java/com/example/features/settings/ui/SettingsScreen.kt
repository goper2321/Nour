package com.example.features.settings.ui

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.compose.material.icons.rounded.Alarm
import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.SheetExpressiveShape
import com.example.ui.components.NourEmptyState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import com.example.localization.Numerals
import com.example.ui.theme.SeedColorSchemes
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.features.adhan.player.AdhanTones
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.location.HOLY_CITIES
import com.example.features.location.WORLD_LOCATIONS
import com.example.features.location.WorldLocation
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.formatNumerals
import com.example.ui.components.StatusBlurStrip
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** "#RRGGBB" for an ARGB int (alpha dropped: seeds are opaque). */
private fun seedHex(argb: Int): String = "#%06X".format(argb and 0xFFFFFF)

/** Parses "#RRGGBB"/"RRGGBB" (any Unicode decimal digits accepted) or null. */
private fun parseHexSeed(text: String): Int? {
    val clean = Numerals.toWestern(text.trim()).removePrefix("#")
    if (clean.length != 6) return null
    if (!clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
    return try {
        android.graphics.Color.parseColor("#$clean")
    } catch (_: Exception) {
        null
    }
}

private fun hsvOf(argb: Int): FloatArray =
    FloatArray(3).also { android.graphics.Color.colorToHSV(argb, it) }

/**
 * Theme seed-color card: curated presets plus a custom HSV/hex picker with
 * live preview. Every change flows straight into the M3 tonal system, so the
 * whole interface re-themes in real time. Visual language matches the
 * surrounding settings cards; only this card's content is new.
 */
@Composable
private fun SeedColorCard(
    seedColorArgb: Int?,
    onSeedChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentSeed = seedColorArgb ?: SeedColorSchemes.DEFAULT_SEED_ARGB
    val baseHsv = remember(currentSeed) { hsvOf(currentSeed) }
    var hue by remember(currentSeed) { mutableFloatStateOf(baseHsv[0]) }
    var saturation by remember(currentSeed) { mutableFloatStateOf(baseHsv[1]) }
    var value by remember(currentSeed) { mutableFloatStateOf(baseHsv[2]) }
    fun push(h: Float, s: Float, v: Float) {
        onSeedChange(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v)))
    }

    var hexFocused by remember { mutableStateOf(false) }
    var hexText by remember { mutableStateOf(seedHex(currentSeed)) }
    LaunchedEffect(currentSeed) {
        if (!hexFocused) hexText = seedHex(currentSeed)
    }
    val hexParsed = remember(hexText) { parseHexSeed(hexText) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(NourSpace.lg)) {
            Text(
                text = strings.seedColorTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = strings.seedColorSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(NourSpace.md))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(NourSpace.sm),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("seed_presets")
            ) {
                items(SeedColorSchemes.PRESET_SEEDS_ARGB.size) { index ->
                    val preset = SeedColorSchemes.PRESET_SEEDS_ARGB[index]
                    val isSelected = currentSeed == preset
                    Surface(
                        shape = CircleShape,
                        color = Color(preset),
                        border = BorderStroke(
                            NourSize.border,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onSeedChange(preset) }
                            .testTag("seed_preset_$index")
                    ) {}
                }
            }
            Spacer(modifier = Modifier.height(NourSpace.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        color = Color(currentSeed),
                        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("seed_preview")
                    ) {}
                    Column {
                        Text(
                            text = strings.seedCustomTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = seedHex(currentSeed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
                TextButton(
                    onClick = { onSeedChange(null) },
                    modifier = Modifier.testTag("seed_reset")
                ) { Text(strings.seedReset) }
            }
            Spacer(modifier = Modifier.height(NourSpace.sm))
            GradientSlider(
                label = strings.seedHue,
                value = hue,
                range = 0f..360f,
                brush = Brush.horizontalGradient(
                    List(7) { i ->
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f)))
                    }
                ),
                onChange = { hue = it; push(it, saturation, value) }
            )
            GradientSlider(
                label = strings.seedSaturation,
                value = saturation,
                range = 0f..1f,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0f, value))),
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, value)))
                    )
                ),
                onChange = { saturation = it; push(hue, it, value) }
            )
            GradientSlider(
                label = strings.seedBrightness,
                value = value,
                range = 0f..1f,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Black,
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))
                    )
                ),
                onChange = { value = it; push(hue, saturation, it) }
            )
            Spacer(modifier = Modifier.height(NourSpace.xs))
            OutlinedTextField(
                value = hexText,
                onValueChange = { hexText = it },
                label = { Text(strings.seedHexLabel) },
                isError = hexParsed == null,
                supportingText = { if (hexParsed == null) Text(strings.seedHexError) },
                singleLine = true,
                shape = CardSubtleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { hexFocused = it.isFocused }
                    .testTag("seed_hex_field")
            )
        }
    }
}

/** Labeled slider over a gradient track (track drawn behind a track-less slider). */
@Composable
private fun GradientSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    brush: Brush,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(PillShape)
                    .background(brush)
            )
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = range,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

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
    onAdhanSettingsChanged: () -> Unit = {},
    alarmsEnabled: Boolean = true,
    onAlarmsEnabledChange: (Boolean) -> Unit = {},
    onOpenExactAlarmSettings: () -> Unit = {},
    useEasternNumerals: Boolean = false,
    onNumeralsChange: (Boolean) -> Unit = {},
    use24Hour: Boolean = false,
    onTimeFormatChange: (Boolean) -> Unit = {},
    surfaceStyle: com.example.ui.theme.SurfaceStyle = com.example.ui.theme.SurfaceStyle.OPAQUE,
    onSurfaceStyleChange: (com.example.ui.theme.SurfaceStyle) -> Unit = {},
    seedColorArgb: Int? = null,
    onSeedChange: (Int?) -> Unit = {},
    modifier: Modifier = Modifier,
    themeMode: String = "system",
    dynamicColors: Boolean = false,
    onThemeModeChange: (String) -> Unit = {},
    onDynamicColorsChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }
    val strings = LocalAppStrings.current

    val adhanSettingsList by db.adhanSettingsDao().getAllSettings().collectAsState(initial = emptyList())
    val adhanGlobalToneId = AdhanTones.normalize(adhanSettingsList.firstOrNull { it.prayerName == "FAJR" }?.toneName)
    val adhanGlobalVolume = adhanSettingsList.firstOrNull { it.prayerName == "FAJR" }?.volume ?: 1f

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAdjustmentsDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isTestingAdhan by remember { mutableStateOf(false) }

    val stripHaze = remember { HazeState() }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .hazeSource(state = stripHaze)
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(NourSpace.lg),
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
        item {
            Text(strings.appearanceTitle, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(NourSpace.lg)) {
                    Text(strings.themeTitle, style = MaterialTheme.typography.titleMedium)
                    listOf("system" to strings.themeSystem, "light" to strings.themeLight, "dark" to strings.themeDark).forEach { (value, label) ->
                        Row(Modifier.fillMaxWidth().selectable(selected = themeMode == value,
                            role = Role.RadioButton, onClick = { onThemeModeChange(value) })
                            .padding(vertical = NourSpace.xs), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant), selected = themeMode == value, onClick = null)
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(strings.dynamicColorsTitle, style = MaterialTheme.typography.titleMedium)
                            Text(if (android.os.Build.VERSION.SDK_INT >= 31) strings.dynamicColorsDesc else strings.dynamicColorsRequires,
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),checked = dynamicColors, onCheckedChange = onDynamicColorsChange,
                            enabled = android.os.Build.VERSION.SDK_INT >= 31)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                    Text(
                        text = strings.surfacesTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(NourSpace.sm))
                    listOf(
                        com.example.ui.theme.SurfaceStyle.OPAQUE to strings.surfaceOpaque,
                        com.example.ui.theme.SurfaceStyle.TRANSLUCENT to strings.surfaceTransparent,
                        com.example.ui.theme.SurfaceStyle.BLUR to strings.surfaceBlur
                    ).forEach { (mode, label) ->
                        Row(Modifier.fillMaxWidth().selectable(selected = surfaceStyle == mode,
                            role = Role.RadioButton, onClick = { onSurfaceStyleChange(mode) })
                            .padding(vertical = NourSpace.xs)
                            .testTag("surface_option_${mode.code}"),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant), selected = surfaceStyle == mode, onClick = null)
                            Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                    }
                }
            }
        }
        item {
            SeedColorCard(
                seedColorArgb = seedColorArgb,
                onSeedChange = onSeedChange,
                modifier = Modifier.testTag("settings_seed_card")
            )
        }
        item { Text(strings.sectionLanguageFormats, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        // App Language Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_language_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
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
                                        contentDescription = strings.languageSectionTitle,
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
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${currentLanguage.nativeName} (${currentLanguage.displayName})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                            modifier = Modifier
                                .testTag("btn_change_language")
                                .heightIn(min = 44.dp)
                        ) {
                            Text(
                                strings.changeLocation,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Numeral system toggle (0123456789 / ٠١٢٣٤٥٦٧٨٩)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_numerals_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.numeralTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = strings.numeralSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(false to "0123456789", true to "٠١٢٣٤٥٦٧٨٩").forEach { (value, sample) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = useEasternNumerals == value,
                                    role = Role.RadioButton,
                                    onClick = { onNumeralsChange(value) }
                                )
                                .padding(vertical = NourSpace.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant), selected = useEasternNumerals == value, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                sample,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Text(
                        text = strings.numeralNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Clock format toggle (12-hour / 24-hour)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_timeformat_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.timeFormatTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        false to formatNumerals("5:30 PM"),
                        true to formatNumerals("17:30")
                    ).forEach { (value, sample) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = use24Hour == value,
                                    role = Role.RadioButton,
                                    onClick = { onTimeFormatChange(value) }
                                )
                                .padding(vertical = NourSpace.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant), selected = use24Hour == value, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (value) strings.timeFormat24 else strings.timeFormat12,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = sample,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Text(strings.sectionLocation, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        // Location & City
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.activeLocation,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${location.cityName}, ${location.countryName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatNumerals("Lat: ${String.format("%.4f", location.latitude)}, Lng: ${String.format("%.4f", location.longitude)}"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showLocationDialog = true },
                            shape = PillShape,
                            modifier = Modifier.heightIn(min = 44.dp)
                        ) {
                            Text(strings.changeLocation, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        item { Text(strings.sectionPrayerCalc, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        // Calculation Authority & Method
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.calculationAuthority,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.madhabTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Madhab.values().forEach { m ->
                        // Mutually exclusive choice: expose radio semantics instead of independent switches
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = madhab == m,
                                    role = Role.RadioButton,
                                    onClick = { onMadhabChange(m) }
                                )
                                .padding(vertical = NourSpace.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                        ) {
                            RadioButton(colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant), selected = madhab == m, onClick = null)
                            Text(text = m.title, style = MaterialTheme.typography.bodyMedium)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.highLatTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var highLatMenuOpen by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { highLatMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardSubtleShape
                    ) {
                        Text(highLatRule.title)
                    }

                    DropdownMenu(
                        expanded = highLatMenuOpen,
                        onDismissRequest = { highLatMenuOpen = false }
                    ) {
                        HighLatitudeRule.values().forEach { rule ->
                            DropdownMenuItem(
                                text = { Text(rule.title) },
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

        // Per-Prayer Minute Tuning: whole card is tappable, chevron hints at it
        item {
            Card(
                onClick = { showAdjustmentsDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
                            text = strings.perPrayerAdjustments,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatNumerals("${strings.fajr}: ${adjustments.fajr}m, ${strings.sunrise}: ${adjustments.sunrise}m, ${strings.dhuhr}: ${adjustments.dhuhr}m, ${strings.asr}: ${adjustments.asr}m, ${strings.maghrib}: ${adjustments.maghrib}m, ${strings.isha}: ${adjustments.isha}m"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.tuneButton,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = strings.tuneButton,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item { Text(strings.sectionAdhan, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        // Adhan & Notifications Sound Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.adhanTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
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
                                        toneName = adhanGlobalToneId,
                                        volume = adhanGlobalVolume,
                                        onCompletion = { isTestingAdhan = false }
                                    )
                                }
                            },
                            shape = PillShape,
                            modifier = Modifier.heightIn(min = 44.dp)
                        ) {
                            Text(if (isTestingAdhan) strings.stopAudio else strings.testAudio, maxLines = 1, softWrap = false)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Master prayer-alarms switch: off cancels every scheduled
                    // alarm and stops all future scheduling (see NourPreferences).
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.alarmsMasterTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.alarmsMasterDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            checked = alarmsEnabled,
                            onCheckedChange = onAlarmsEnabledChange,
                            modifier = Modifier.testTag("alarms_master_switch")
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline),
                        modifier = Modifier.padding(vertical = NourSpace.sm)
                    )

                    // System "Alarms & reminders" permission row (Android 12+).
                    // The switch mirrors the OS grant and opens system settings.
                    if (Build.VERSION.SDK_INT >= 31) {
                        val alarmManager = remember {
                            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                        }
                        var exactTick by remember { mutableIntStateOf(0) }
                        val canExact = remember(exactTick, alarmsEnabled) {
                            alarmManager?.canScheduleExactAlarms() ?: false
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.alarmsSystemSettings,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                checked = canExact && alarmsEnabled,
                                onCheckedChange = {
                                    exactTick++
                                    onOpenExactAlarmSettings()
                                },
                                enabled = alarmsEnabled,
                                modifier = Modifier.testTag("exact_alarm_system_switch")
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline),
                            modifier = Modifier.padding(vertical = NourSpace.sm)
                        )
                    }

                    // Global Adhan voice (applies to all prayers) + volume + credit
                    var voiceMenuOpen by remember { mutableStateOf(false) }
                    Text(
                        text = strings.adhanVoiceTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { voiceMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardSubtleShape
                    ) {
                        Text(
                            text = AdhanTones.displayName(adhanGlobalToneId, strings),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(
                        expanded = voiceMenuOpen,
                        onDismissRequest = { voiceMenuOpen = false }
                    ) {
                        AdhanTones.allIds().forEach { toneId ->
                            DropdownMenuItem(
                                text = { Text(AdhanTones.displayName(toneId, strings)) },
                                onClick = {
                                    voiceMenuOpen = false
                                    coroutineScope.launch {
                                        adhanSettingsList.forEach { row ->
                                            db.adhanSettingsDao().saveSettings(row.copy(toneName = toneId))
                                        }
                                        listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA").forEach { pName ->
                                            if (adhanSettingsList.none { it.prayerName == pName }) {
                                                db.adhanSettingsDao().saveSettings(AdhanSettingsEntity(pName, toneName = toneId))
                                            }
                                        }
                                        onAdhanSettingsChanged()
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.adhanVolumeTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatNumerals("${(adhanGlobalVolume * 100).toInt()}%"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    var volumeSliderPos by remember(adhanGlobalVolume) { mutableFloatStateOf(adhanGlobalVolume) }
                    Slider(
                        value = volumeSliderPos,
                        onValueChange = { volumeSliderPos = it },
                        onValueChangeFinished = {
                            coroutineScope.launch {
                                adhanSettingsList.forEach { row ->
                                    db.adhanSettingsDao().saveSettings(row.copy(volume = volumeSliderPos))
                                }
                                onAdhanSettingsChanged()
                            }
                        },
                        valueRange = 0f..1f
                    )
                    Text(
                        text = "Recordings: Beautiful Adhan by Adam-synagda (CC0, Wikimedia Commons); short Hayya excerpt cut from the same recording",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val prayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
                    var toneMenuFor by remember { mutableStateOf<String?>(null) }
                    var minutesMenuFor by remember { mutableStateOf<String?>(null) }
                    val reminderMinuteOptions = listOf(5, 10, 15, 20, 30)
                    prayers.forEach { pName ->
                        val setting = adhanSettingsList.firstOrNull { it.prayerName == pName }
                        val isEnabled = setting?.isAdhanEnabled ?: true
                        val reminderOn = setting?.isPreReminderEnabled ?: false
                        val reminderMinutes = setting?.preReminderMinutes ?: 15
                        val prayerTitle = when (pName) {
                            "FAJR" -> strings.fajr
                            "DHUHR" -> strings.dhuhr
                            "ASR" -> strings.asr
                            "MAGHRIB" -> strings.maghrib
                            "ISHA" -> strings.isha
                            else -> pName
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = NourSpace.xs)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prayerTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            val newSetting = (setting ?: AdhanSettingsEntity(pName)).copy(isAdhanEnabled = checked)
                                            db.adhanSettingsDao().saveSettings(newSetting)
                                            if (!checked) {
                                                PrayerNotificationScheduler.cancelPrayerAlarms(context, PrayerName.valueOf(pName))
                                            }
                                            onAdhanSettingsChanged()
                                        }
                                    }
                                )
                            }
                            OutlinedButton(
                                onClick = { toneMenuFor = pName },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CardSubtleShape,
                                contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.xs)
                            ) {
                                Text(
                                    text = AdhanTones.displayName(setting?.toneName, strings),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = toneMenuFor == pName,
                                onDismissRequest = { toneMenuFor = null }
                            ) {
                                AdhanTones.allIds().forEach { toneId ->
                                    DropdownMenuItem(
                                        text = { Text(AdhanTones.displayName(toneId, strings)) },
                                        onClick = {
                                            toneMenuFor = null
                                            coroutineScope.launch {
                                                val newSetting = (setting ?: AdhanSettingsEntity(pName)).copy(toneName = toneId)
                                                db.adhanSettingsDao().saveSettings(newSetting)
                                                onAdhanSettingsChanged()
                                            }
                                        }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.preReminderTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { minutesMenuFor = pName },
                                    enabled = reminderOn,
                                    shape = CardSubtleShape,
                                    contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.xs)
                                ) {
                                    Text(
                                        text = formatNumerals("$reminderMinutes ${strings.minutesShort}"),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                DropdownMenu(
                                    expanded = minutesMenuFor == pName,
                                    onDismissRequest = { minutesMenuFor = null }
                                ) {
                                    reminderMinuteOptions.forEach { mins ->
                                        DropdownMenuItem(
                                            text = { Text(formatNumerals("$mins ${strings.minutesShort}")) },
                                            onClick = {
                                                minutesMenuFor = null
                                                coroutineScope.launch {
                                                    val newSetting = (setting ?: AdhanSettingsEntity(pName)).copy(preReminderMinutes = mins)
                                                    db.adhanSettingsDao().saveSettings(newSetting)
                                                    onAdhanSettingsChanged()
                                                }
                                            }
                                        )
                                    }
                                }
                                Switch(colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    checked = reminderOn,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            val newSetting = (setting ?: AdhanSettingsEntity(pName)).copy(isPreReminderEnabled = checked)
                                            db.adhanSettingsDao().saveSettings(newSetting)
                                            if (!checked) {
                                                PrayerNotificationScheduler.cancelPrayerAlarms(context, PrayerName.valueOf(pName))
                                                onAdhanSettingsChanged()
                                            } else {
                                                onAdhanSettingsChanged()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        item { Text(strings.sectionPrivacy, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        // Privacy & Offline Audit Badge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.privacyPledgeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
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
        StatusBlurStrip(
            hazeState = stripHaze,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // Per-Prayer Minute Tuning Dialog
    if (showAdjustmentsDialog) {
        var fajrAdj by remember { mutableFloatStateOf(adjustments.fajr.toFloat()) }
        var sunriseAdj by remember { mutableFloatStateOf(adjustments.sunrise.toFloat()) }
        var dhuhrAdj by remember { mutableFloatStateOf(adjustments.dhuhr.toFloat()) }
        var asrAdj by remember { mutableFloatStateOf(adjustments.asr.toFloat()) }
        var maghribAdj by remember { mutableFloatStateOf(adjustments.maghrib.toFloat()) }
        var ishaAdj by remember { mutableFloatStateOf(adjustments.isha.toFloat()) }

        @Composable
        fun TuneSliderRow(
            name: String,
            value: Float,
            testTag: String,
            onChange: (Float) -> Unit
        ) {
            val intValue = value.roundToInt()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatNumerals((if (intValue > 0) "+" else "") + "${intValue}m"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Slider(
                    value = value,
                    onValueChange = onChange,
                    valueRange = -30f..30f,
                    steps = 59,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                )
            }
        }

        AlertDialog(
            onDismissRequest = { showAdjustmentsDialog = false },
            shape = DialogExpressiveShape,
            title = { Text(strings.tuneDialogTitle, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(NourSpace.sm)) {
                    Text(strings.tuneDialogDesc, style = MaterialTheme.typography.bodySmall)
                    TuneSliderRow(strings.fajr, fajrAdj, "tune_slider_fajr") { fajrAdj = it }
                    TuneSliderRow(strings.sunrise, sunriseAdj, "tune_slider_sunrise") { sunriseAdj = it }
                    TuneSliderRow(strings.dhuhr, dhuhrAdj, "tune_slider_dhuhr") { dhuhrAdj = it }
                    TuneSliderRow(strings.asr, asrAdj, "tune_slider_asr") { asrAdj = it }
                    TuneSliderRow(strings.maghrib, maghribAdj, "tune_slider_maghrib") { maghribAdj = it }
                    TuneSliderRow(strings.isha, ishaAdj, "tune_slider_isha") { ishaAdj = it }
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.heightIn(min = 44.dp),
                    onClick = {
                        onAdjustmentsChange(
                            PrayerAdjustments(
                                fajr = fajrAdj.roundToInt(),
                                sunrise = sunriseAdj.roundToInt(),
                                dhuhr = dhuhrAdj.roundToInt(),
                                asr = asrAdj.roundToInt(),
                                maghrib = maghribAdj.roundToInt(),
                                isha = ishaAdj.roundToInt()
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

    // Location Dialog: holy cities + searchable list of all 195 countries.
    // UTC offsets resolve live from each capital's IANA timezone (DST-aware).
    if (showLocationDialog) {
        var locationQuery by remember { mutableStateOf("") }
        val filteredWorld = remember(locationQuery) {
            WORLD_LOCATIONS.filter { it.matches(locationQuery.trim()) }
        }

        @Composable
        fun WorldLocationRow(
            world: WorldLocation,
            isSelected: Boolean,
            onPick: () -> Unit
        ) {
            Surface(
                shape = CardSubtleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onPick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardSubtleShape,
                    contentPadding = PaddingValues(horizontal = NourSpace.md, vertical = NourSpace.xs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = world.capital,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = world.country,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatNumerals(world.utcLabel()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            shape = DialogExpressiveShape,
            title = { Text(strings.selectCity, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NourSpace.sm)) {
                    OutlinedTextField(
                        value = locationQuery,
                        onValueChange = { locationQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(strings.searchLocationHint) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (locationQuery.isNotBlank()) {
                                IconButton(onClick = { locationQuery = "" }) {
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
                        modifier = Modifier.heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                    ) {
                        if (locationQuery.isBlank()) {
                            item {
                                Text(
                                    text = strings.holyCitiesTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(HOLY_CITIES) { holy ->
                                WorldLocationRow(
                                    world = holy,
                                    isSelected = location.cityName == holy.capital &&
                                        location.countryName == holy.country,
                                    onPick = {
                                        onLocationChange(holy.toUserLocation())
                                        showLocationDialog = false
                                    }
                                )
                            }
                            item {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                        items(filteredWorld) { world ->
                            WorldLocationRow(
                                world = world,
                                isSelected = location.cityName == world.capital &&
                                    location.countryName == world.country,
                                onPick = {
                                    onLocationChange(world.toUserLocation())
                                    showLocationDialog = false
                                }
                            )
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
            shape = DialogExpressiveShape,
            title = {
                Text(
                    text = strings.languageDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
                ) {
                    items(AppLanguage.entries.size) { index ->
                        val lang = AppLanguage.entries[index]
                        val isSelected = currentLanguage == lang
                        Surface(
                            shape = CardSubtleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant,
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
                                    .padding(horizontal = NourSpace.lg, vertical = NourSpace.lg),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.nativeName,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = strings.prayedStatus,
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
