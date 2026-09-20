package com.example.features.prayer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.localization.formatNumerals
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.localization.LocalSurfaceStyle
import com.example.ui.theme.SurfaceStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourAlpha
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DaySwitcherBar(
    hazeState: HazeState? = null,
    selectedDate: Date,
    dayOffset: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isArabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    val hijri = remember(selectedDate) { HijriDateHelper.fromGregorian(selectedDate) }
    val dayLabel = remember(selectedDate, dayOffset, strings) {
        val date = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(selectedDate)
        when (dayOffset) {
            0 -> "${strings.dayToday} • $date"
            1 -> "${strings.dayTomorrow} • $date"
            -1 -> "${strings.dayYesterday} • $date"
            else -> date
        }
    }
    val surfaceMode = LocalSurfaceStyle.current
    val hazeOn = surfaceMode == SurfaceStyle.BLUR && hazeState != null
    val glassStyle = HazeStyle(
        backgroundColor = Color.Transparent,
        tint = HazeTint(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = NourAlpha.GlassTint)),
        blurRadius = 16.dp,
        noiseFactor = 0f
    )
    Surface(
        modifier = Modifier.fillMaxWidth()
            .testTag("day_switcher_bar")
            .then(
                if (hazeOn) Modifier
                    .clip(CardSubtleShape)
                    .hazeEffect(state = hazeState!!, style = glassStyle)
                else Modifier
            ),
        shape = CardSubtleShape,
        color = if (hazeOn) Color.Transparent
        else if (surfaceMode == SurfaceStyle.OPAQUE) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = NourAlpha.FrostedFill),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))) {
        Row(Modifier.fillMaxWidth().padding(NourSpace.xs), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDay,
                modifier = Modifier.size(NourSize.touchTarget).testTag("day_switcher_prev")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, strings.previousBtn)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                Text(formatNumerals(dayLabel), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                // One merged Hijri line (never clipped mid-stack).
                Text(
                    formatNumerals(
                        if (isArabicUi) hijri.formattedAr
                        else hijri.formattedEn
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (dayOffset != 0) {
                    TextButton(onClick = onResetToToday, modifier = Modifier.testTag("day_switcher_today_btn")) {
                        Text(strings.dayToday)
                    }
                }
            }
            IconButton(onClick = onNextDay,
                modifier = Modifier.size(NourSize.touchTarget).testTag("day_switcher_next")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, strings.nextBtn)
            }
        }
    }
}
