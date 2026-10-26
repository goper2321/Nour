package com.example.features.settings.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material.icons.rounded.Alarm
import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.SheetExpressiveShape
import com.example.ui.components.NourEmptyState
import com.example.ui.components.LocalFloatingBottomExtra
import com.example.ui.components.NourSectionHeader
import com.example.ui.components.NourSegmentOption
import com.example.ui.components.NourSegmentedControl
import com.example.ui.components.nourCardSurfaceColor
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import com.example.localization.Numerals
import com.example.features.settings.CrashLogReader
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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.database.AdhanSettingsEntity
import com.example.database.NourDatabase
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.adhan.player.AdhanTones
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
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
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.text.style.TextAlign
import com.example.BuildConfig
import com.example.features.quran.data.QuranCorpus
import com.example.features.quran.data.QuranSources
import com.example.features.settings.DiagnosticsMath
import com.example.features.settings.NourPreferences
import com.example.localization.AppStrings

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
@OptIn(ExperimentalLayoutApi::class)
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
        colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
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
            FlowRow(
                // Each option is a 48 dp touch target with the visible circle
                // drawn inside it, so the swatch rhythm is unchanged but the
                // hit area meets the 48 dp minimum. Four per row keeps every
                // preset visible without a clipped, scroll-only last swatch.
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.spacedBy(NourSpace.xs),
                verticalArrangement = Arrangement.spacedBy(NourSpace.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .testTag("seed_presets")
            ) {
                SeedColorSchemes.PRESET_SEEDS_ARGB.forEachIndexed { index, preset ->
                    val isSelected = currentSeed == preset
                    val swatch = Color(preset)
                    // The tick is drawn in whichever of black/white stays
                    // legible on the swatch, and the row is a radio group, so
                    // the active colour is never signalled by the border tint
                    // alone.
                    val tickColor = if (swatch.luminance() > 0.5f) Color.Black else Color.White
                    Box(
                        modifier = Modifier
                            .size(NourSize.touchTarget)
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSeedChange(preset) }
                            )
                            .semantics {
                                contentDescription = strings.seedPresetLabel.format(seedHex(preset))
                            }
                            .testTag("seed_preset_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = swatch,
                            border = BorderStroke(
                                if (isSelected) NourSize.selectedBorder else NourSize.border,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.size(NourSize.badge)
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = tickColor,
                                        modifier = Modifier.size(NourSize.smallIcon)
                                    )
                                }
                            }
                        }
                    }
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
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = seedHex(currentSeed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
            // NUR-015: single-line labels must ellipsize, never clip.
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

@OptIn(ExperimentalPermissionsApi::class)
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
    use24Hour: Boolean = true,
    onTimeFormatChange: (Boolean) -> Unit = {},
    seedColorArgb: Int? = null,
    onSeedChange: (Int?) -> Unit = {},
    onReopenSetup: () -> Unit = {},
    modifier: Modifier = Modifier,
    themeMode: String = "system",
    dynamicColors: Boolean = false,
    onThemeModeChange: (String) -> Unit = {},
    onDynamicColorsChange: (Boolean) -> Unit = {},
    // NUR-008: high-contrast theme toggle (persists via NourPreferences).
    highContrast: Boolean = false,
    onHighContrastChange: (Boolean) -> Unit = {}
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

    Box(
        modifier = modifier.fillMaxSize()
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            // Keep the inline seed-colour hex field scrollable above the soft
            // keyboard. The root frame opts out of window insets (the compact
            // floating navigation bar handles the system bars), so screens own
            // IME insets.
            .imePadding()
            .testTag("settings_screen_list"),
        // Horizontal margin comes from the global NourContentFrame gutter.
        // Bottom is absolute: the system inset + the bar's band plus its gap
        // below and an 8 dp breath above. The last row rests one small breath
        // above the band's edge — readable, never hidden — while scrolling
        // rows sweep under the pill and the gesture area.
        contentPadding = PaddingValues(top = NourSpace.lg, bottom = LocalFloatingBottomExtra.current),
        verticalArrangement = Arrangement.spacedBy(NourSpace.lg)
    ) {
        item {
            NourSectionHeader(strings.appearanceTitle)
        }
        item {
            Card(
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(
                    Modifier.padding(NourSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.md)
                ) {
                    Text(strings.themeTitle, style = MaterialTheme.typography.titleMedium)
                    NourSegmentedControl(
                        options = listOf(
                            NourSegmentOption("system", strings.themeSystem),
                            NourSegmentOption("light", strings.themeLight),
                            NourSegmentOption("dark", strings.themeDark)
                        ),
                        selected = themeMode,
                        onSelected = onThemeModeChange,
                        modifier = Modifier.testTag("settings_theme_control"),
                        role = Role.RadioButton
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(strings.dynamicColorsTitle, style = MaterialTheme.typography.titleMedium)
                            Text(if (android.os.Build.VERSION.SDK_INT >= 31) strings.dynamicColorsDesc else strings.dynamicColorsRequires,
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),checked = dynamicColors, onCheckedChange = onDynamicColorsChange,
                            enabled = android.os.Build.VERSION.SDK_INT >= 31,
                            // NUR-011: this switch had no modifier at all, so
                            // TalkBack read it as an empty element; the row
                            // title (already localized) is its label.
                            modifier = Modifier.semantics { contentDescription = strings.dynamicColorsTitle })
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                    // NUR-008: same family as the dynamic-colors toggle — an
                    // opaque token-layer switch (M3 contrast level 1.0), so it
                    // composes with light, dark and custom seeds.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(strings.highContrastTitle, style = MaterialTheme.typography.titleMedium)
                            Text(strings.highContrastDesc,
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),checked = highContrast, onCheckedChange = onHighContrastChange,
                            enabled = true,
                            modifier = Modifier.semantics { contentDescription = strings.highContrastTitle })
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
        item { NourSectionHeader(strings.sectionLanguageFormats) }
        // App Language Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_language_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
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
                                        modifier = Modifier.size(NourSize.rowIcon)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(NourSpace.md))
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
                        Spacer(modifier = Modifier.width(NourSpace.sm))
                        TextButton(
                            onClick = { showLanguageDialog = true },
                            modifier = Modifier
                                .testTag("btn_change_language")
                                .heightIn(min = NourSize.touchTarget),
                            contentPadding = PaddingValues(horizontal = NourSpace.sm)
                        ) {
                            Text(
                                strings.changeLocation,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
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
                    Spacer(modifier = Modifier.height(NourSpace.sm))
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
                            Spacer(modifier = Modifier.width(NourSpace.sm))
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
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.timeFormatTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(NourSpace.sm))
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
                            Spacer(modifier = Modifier.width(NourSpace.sm))
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

        item { NourSectionHeader(strings.sectionLocation) }
        // Location & City
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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
                        TextButton(
                            onClick = { showLocationDialog = true },
                            modifier = Modifier.heightIn(min = NourSize.touchTarget),
                            contentPadding = PaddingValues(horizontal = NourSpace.sm)
                        ) {
                            Text(strings.changeLocation, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        item { NourSectionHeader(strings.sectionPrayerCalc) }
        // Calculation Authority & Method
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.calculationAuthority,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(NourSpace.sm))

                    var methodMenuOpen by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { methodMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardSubtleShape
                    ) {
                        Text(text = calculationMethod.title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.madhabTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(NourSpace.sm))

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
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Text(
                        text = strings.highLatTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(NourSpace.sm))

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
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
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
                    Spacer(modifier = Modifier.width(NourSpace.sm))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = strings.tuneButton,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(NourSize.icon)
                    )
                }
            }
        }

        item { NourSectionHeader(strings.sectionAdhan) }
        // Adhan & Notifications Sound Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
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
                            modifier = Modifier.heightIn(min = NourSize.touchTarget)
                        ) {
                            Text(if (isTestingAdhan) strings.stopAudio else strings.testAudio, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(modifier = Modifier.height(NourSpace.md))

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
                            modifier = Modifier
                                .testTag("alarms_master_switch")
                                // NUR-011: switch was unlabeled for TalkBack.
                                .semantics { contentDescription = strings.alarmsMasterTitle }
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
                        // Refresh when returning from system settings (ON_RESUME).
                        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                    exactTick++
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                        }
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
                                    modifier = Modifier.size(NourSize.rowIcon)
                                )
                                Spacer(modifier = Modifier.width(NourSpace.sm))
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
                                modifier = Modifier
                                    .testTag("exact_alarm_system_switch")
                                    // NUR-011: mirrors the exact-alarm permission.
                                    .semantics { contentDescription = strings.alarmsSystemSettings }
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline),
                            modifier = Modifier.padding(vertical = NourSpace.sm)
                        )
                    }

                    // Notification permission row (Android 13+). Gives users a way to
                    // grant — or re-grant — POST_NOTIFICATIONS after onboarding, so a
                    // dismissed prompt never silently blocks prayer alerts forever.
                    if (Build.VERSION.SDK_INT >= 33) {
                        val notifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                        val notifGranted = notifPermission.status is PermissionStatus.Granted
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
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(NourSize.rowIcon)
                                )
                                Spacer(modifier = Modifier.width(NourSpace.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.setupNotifTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (notifGranted) strings.notifPermissionGranted else strings.notifPermissionNeeded,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                checked = notifGranted,
                                onCheckedChange = {
                                    if (notifGranted) {
                                        try {
                                            context.startActivity(
                                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                }
                                            )
                                        } catch (_: Exception) {
                                            // Some OEM builds omit the notification settings
                                            // screen. Fall back to the app's own details page,
                                            // and tell the user if even that fails rather than
                                            // swallowing the error silently.
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
                                                    strings.notifSettingsUnavailable,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    } else {
                                        notifPermission.launchPermissionRequest()
                                    }
                                },
                                enabled = alarmsEnabled,
                                modifier = Modifier
                                    .testTag("notification_permission_switch")
                                    // NUR-011: notification row switch spoke empty.
                                    .semantics { contentDescription = strings.setupNotifTitle }
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
                    Spacer(modifier = Modifier.height(NourSpace.xs))
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
                    Spacer(modifier = Modifier.height(NourSpace.sm))
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
                    Spacer(modifier = Modifier.height(NourSpace.sm))
                    Text(
                        text = strings.prayerNotificationsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(NourSpace.md))

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prayerTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.notificationsEnabledLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onPrimary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant),
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
                                    },
                                    modifier = Modifier
                                        .testTag("prayer_notification_switch_${pName.lowercase()}")
                                        // NUR-011: per-prayer switch carries the prayer name.
                                        .semantics { contentDescription = prayerTitle }
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
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                                    },
                                    // NUR-011: this switch has no testTag and had
                                    // no semantics either; label it for TalkBack.
                                    modifier = Modifier.semantics { contentDescription = strings.preReminderTitle }
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                    }
                }
            }
        }

        item { NourSectionHeader(strings.sectionPrivacy) }
        // Privacy & Offline Audit Badge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(modifier = Modifier.padding(NourSpace.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(NourSpace.sm))
                        Text(
                            text = strings.privacyPledgeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ${strings.privacyPledgeBullet1}\n• ${strings.privacyPledgeBullet2}\n• ${strings.privacyPledgeBullet3}\n• ${strings.privacyPledgeBullet4}\n• ${strings.privacyPledgeBullet5}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // First-Time Setup Wizard Re-run Card
        item {
            Card(
                onClick = onReopenSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_rerun_setup_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NourSpace.lg),
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
                                    imageVector = Icons.Rounded.Explore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(NourSize.rowIcon)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = strings.setupRerunWizard,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.setupRerunWizardDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(NourSize.icon)
                    )
                }
            }
        }

        // NUR-001: Diagnostics — the crash logs CrashReporter stored on this
        // device, newest first, with per-file Copy and Share actions. The
        // actions use the system clipboard and the standard share sheet, both
        // fully offline; Nour has no upload flow and nothing here is automatic.
        item { NourSectionHeader(strings.diagnosticsTitle) }
        item {
            val crashLogs = remember { CrashLogReader.loadCrashLogEntries(context) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_diagnostics_card"),
                shape = CardExpressiveShape,
                colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
                border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
            ) {
                Column(
                    Modifier.padding(NourSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.md)
                ) {
                    Text(
                        text = strings.diagnosticsNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (crashLogs.isEmpty()) {
                        Text(
                            text = strings.diagnosticsEmpty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        crashLogs.forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                                Text(
                                    text = entry.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                val meta = listOfNotNull(
                                    entry.headerTime,
                                    entry.version?.let { "v$it" }
                                ).joinToString(" • ")
                                Text(
                                    text = meta.ifBlank { entry.fileName },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                                ) {
                                    // Copy: the full log onto the system clipboard.
                                    IconButton(
                                        onClick = {
                                            runCatching {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                clipboard?.setPrimaryClip(
                                                    android.content.ClipData.newPlainText("Nour crash log", entry.fullText)
                                                )
                                            }
                                        },
                                        modifier = Modifier.testTag("diagnostics_copy_${entry.fileName}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = strings.diagnosticsCopyDesc
                                        )
                                    }
                                    // Share: the standard offline share sheet; the
                                    // user chooses how to send it (e.g. email).
                                    IconButton(
                                        onClick = {
                                            runCatching {
                                                val send = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, entry.fullText)
                                                }
                                                context.startActivity(Intent.createChooser(send, null))
                                            }
                                        },
                                        modifier = Modifier.testTag("diagnostics_share_${entry.fileName}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = strings.diagnosticsShareDesc
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // NUR-033: factual on-device diagnostics (support info). Pure
        // introspection only — no sensors, no network, no export. Rows are
        // long-press copyable; see DeviceDiagnosticsCard below.
        item { NourSectionHeader(strings.deviceDiagnosticsTitle) }
        item {
            DeviceDiagnosticsCard(
                calculationMethod = calculationMethod,
                madhab = madhab,
                highLatRule = highLatRule,
                strings = strings
            )
        }

        // NUR-035: "About this Quran" — where the scripture text came from,
        // shown in-app; the text itself is English documentation (see
        // QuranSources); the honest tafsir line is localized.
        item { NourSectionHeader(strings.quranSourcesTitle) }
        item { QuranSourcesCard(strings) }
    }
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
                    modifier = Modifier.heightIn(min = NourSize.touchTarget),
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
                        Spacer(modifier = Modifier.width(NourSpace.sm))
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
                Column(
                    // Let the city search field and result list rise above the
                    // soft keyboard (matches the SetupWizard search dialog).
                    modifier = Modifier.imePadding(),
                    verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
                ) {
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
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
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
                                        modifier = Modifier.size(NourSize.smallIcon)
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

// ============================================================
// NUR-033 — "Device diagnostics" (support info, pure introspection)
// ============================================================

/** One snapshot of everything the diagnostics card shows; gathered on Dispatchers.IO. */
private data class DeviceDiagStats(
    val versionLine: String,
    val nextPrayerName: String?,
    val nextTriggerMillis: Long?,
    val exactOk: Boolean,
    val lastFixAge: String?,
    val prayerLogs: Int,
    val bookmarkRows: Int,
    val khatmahRows: Int,
    val adhanSettingRows: Int,
    val actualTextHash: String?
)

/**
 * NUR-033: bundles every factual observation the card shows into one IO
 * function so the composable stays render-only. Pure introspection only —
 * no sensors, no network. The caller runs this in runCatching: if any piece
 * cannot be read, the card simply reveals nothing rather than an invented
 * number.
 */
private suspend fun gatherDeviceDiagStats(context: Context, db: NourDatabase): DeviceDiagStats {
    val prefs = NourPreferences(context)
    val packageInfo = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.getOrNull()
    val buildTime = runCatching { BuildConfig.NOUR_BUILD_TIME }.getOrDefault("")
    val versionLine = listOfNotNull(
        packageInfo?.versionName,
        buildTime.ifBlank { null }
    ).joinToString(" · ")
    val next = DiagnosticsMath.nextSchedule(prefs.nextAdhanRecords(), System.currentTimeMillis())
    val now = System.currentTimeMillis()
    return DeviceDiagStats(
        versionLine = versionLine.ifBlank { "—" },
        nextPrayerName = next?.first,
        nextTriggerMillis = next?.second,
        exactOk = prefs.adhanExactOk(),
        lastFixAge = DiagnosticsMath.formatFixAge(prefs.lastLocationFixMillis, now),
        prayerLogs = db.prayerLogDao().countAll(),
        bookmarkRows = db.bookmarkDao().countAll(),
        khatmahRows = db.khatmahDao().countAll(),
        adhanSettingRows = db.adhanSettingsDao().countAll(),
        actualTextHash = QuranCorpus.actualTextHash()
    )
}

/**
 * NUR-033: copies one diagnostics value to the system clipboard (fully
 * offline, same mechanism the crash-log card uses) and confirms with a
 * localized Copy toast.
 */
private fun copyDiagnosticsValue(context: Context, value: String, strings: AppStrings) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Nour", value))
    }
    Toast.makeText(context, strings.copyAction, Toast.LENGTH_SHORT).show()
}

/**
 * One factual diagnostics row: label left, value right, both wrapping at
 * large font scales so nothing is clipped. Long-press copies the value;
 * TalkBack exposes the same action as a custom accessibility action.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiagRow(
    label: String,
    value: String,
    tag: String,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NourSize.touchTarget)
            .combinedClickable(
                onClick = {},
                onLongClick = { copyDiagnosticsValue(context, value, strings) }
            )
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(strings.copyAction) {
                        copyDiagnosticsValue(context, value, strings)
                        true
                    }
                )
            }
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
    }
}

/**
 * NUR-033 — the "Device diagnostics" card. Gathered once per entry into the
 * screen; the next-Adhan row comes from the app's own additive records
 * (AlarmManager cannot be introspected) and is labelled honestly as exact or
 * as an inexact fallback. The corpus hash row shows the *actual* digest of
 * the bundled Uthmani file and whether it matches the pinned constant.
 */
@Composable
internal fun DeviceDiagnosticsCard(
    calculationMethod: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { NourDatabase.getInstance(context) }
    var stats by remember { mutableStateOf<DeviceDiagStats?>(null) }

    LaunchedEffect(Unit) {
        stats = runCatching { withContext(Dispatchers.IO) { gatherDeviceDiagStats(context, db) } }
            .getOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_device_diagnostics_card"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
    ) {
        Column(
            Modifier.padding(NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
        ) {
            Text(
                text = strings.diagCopyHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val s = stats
            if (s != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
                DiagRow(strings.diagAppVersion, s.versionLine, "diag_row_version", strings)
                DiagRow(strings.calculationAuthority, calculationMethod.title, "diag_row_method", strings)
                DiagRow(strings.madhabTitle, madhab.title, "diag_row_madhab", strings)
                DiagRow(strings.highLatTitle, highLatRule.title, "diag_row_highlat", strings)
                DiagRow(strings.diagHijriMethodLabel, strings.diagHijriMethodValue, "diag_row_hijri", strings)
                DiagRow(
                    strings.diagLastFixLabel,
                    s.lastFixAge ?: strings.diagLastFixNever,
                    "diag_row_lastfix",
                    strings
                )
                val nextAdhanValue = s.nextTriggerMillis?.let { millis ->
                    val prayerLabel = s.nextPrayerName
                        ?.let { runCatching { PrayerName.valueOf(it).displayName }.getOrDefault(it) }
                        .orEmpty()
                    val exactness = if (s.exactOk) strings.diagAdhanExact else strings.diagAdhanInexact
                    listOfNotNull(
                        prayerLabel.ifBlank { null },
                        DiagnosticsMath.formatScheduleStamp(millis),
                        exactness
                    ).joinToString(" · ")
                } ?: strings.diagAdhanNone
                DiagRow(strings.diagNextAdhanLabel, nextAdhanValue, "diag_row_nextadhan", strings)
                DiagRow(strings.diagRowsPrayerLogs, formatNumerals(s.prayerLogs.toString()), "diag_row_logs", strings)
                DiagRow(strings.diagRowsBookmarks, formatNumerals(s.bookmarkRows.toString()), "diag_row_bookmarks", strings)
                DiagRow(strings.diagRowsKhatmah, formatNumerals(s.khatmahRows.toString()), "diag_row_khatmah", strings)
                DiagRow(strings.diagRowsAdhanSettings, formatNumerals(s.adhanSettingRows.toString()), "diag_row_adhansettings", strings)
                val intact = DiagnosticsMath.isCorpusIntact(s.actualTextHash, QuranCorpus.TEXT_HASH)
                val corpusValue = DiagnosticsMath.shortHash(s.actualTextHash) + " · " +
                    if (intact) strings.diagCorpusIntact else strings.diagCorpusFailed
                DiagRow(strings.diagCorpusHashLabel, corpusValue, "diag_row_corpus", strings)
            }
        }
    }
}

// ============================================================
// NUR-035 — "About this Quran" (in-app provenance)
// ============================================================

/**
 * NUR-035 — provenance card: where the bundled scripture text came from
 * (Tanzil Uthmani v1.1), licences, the pin-and-verify integrity note, and
 * the honest line that other translations and tafsir are not included. The
 * documentation text itself is English on purpose (it mirrors Tanzil's own
 * licence wording and lives beside the corpus it describes); the section
 * title and the tafsir honesty line are localized. URLs are plain text.
 */
@Composable
internal fun QuranSourcesCard(
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val lines = remember {
        QuranSources.lines(QuranCorpus.TEXT_HASH, QuranCorpus.EN_SAHIH_HASH)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_quran_sources_card"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))
    ) {
        Column(
            Modifier.padding(NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.sm)
        ) {
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.Hairline))
            Text(
                text = strings.quranSourcesNotBundled,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
