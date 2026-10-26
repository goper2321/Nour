package com.example.features.prayer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.localization.formatNumerals
import androidx.compose.ui.unit.dp
import com.example.ui.components.NourCircularIconButton
import com.example.ui.components.nourFloatingSurfaceColor
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DaySwitcherBar(
    selectedDate: Date,
    dayOffset: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isArabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    // NUR-010: horizontal fling travel accumulated in pixel coordinates.
    var swipeTravelPx by remember { mutableStateOf(0f) }
    Surface(
        modifier = modifier.fillMaxWidth()
            .testTag("day_switcher_bar")
            // NUR-010: horizontal fling on the WHOLE bar (not just the padded
            // row) advances or retreats one day through the SAME state
            // mutation the pills use (onPreviousDay/onNextDay). Taps stay
            // primary: the drag detector only claims horizontal drags past
            // the touch slop, so every tap on the pills, the date area
            // (reset-to-today) and, outside this bar, the times card is
            // untouched. RTL flips the swipe-to-day mapping (see
            // DaySwipeGestureLogic). Mounted on the Surface modifier chain so
            // a flick that lands in the bar's outer padding still counts.
            .pointerInput(isRtl) {
                detectHorizontalDragGestures(
                    onDragStart = { swipeTravelPx = 0f },
                    onDragCancel = { swipeTravelPx = 0f },
                    onDragEnd = {
                        val minTravelPx = DaySwipeGestureLogic.MIN_TRAVEL_DP.dp.toPx()
                        when (DaySwipeGestureLogic.directionForTravel(swipeTravelPx, minTravelPx, isRtl)) {
                            DaySwipeDirection.NEXT -> onNextDay()
                            DaySwipeDirection.PREVIOUS -> onPreviousDay()
                            null -> Unit
                        }
                        swipeTravelPx = 0f
                    }
                ) { _, dragAmount ->
                    // NUR-010: dragAmount is the already-sliced horizontal
                    // delta handed over by the detector. (change.position
                    // Change() would be Offset.Zero here — the change has
                    // been consumed by the gesture by the time this runs.)
                    swipeTravelPx += dragAmount
                }
            },
        shape = CardSubtleShape,
        color = nourFloatingSurfaceColor(),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(NourSpace.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NourCircularIconButton(
                onClick = onPreviousDay,
                contentDescription = strings.previousBtn,
                modifier = Modifier.testTag("day_switcher_prev")
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(NourSize.smallIcon)
                )
            }
            // Tapping the date area returns to today. It is a real button
            // only while browsing another day; on today itself it is inert
            // so TalkBack never offers a dead "activate" action.
            //
            // NUR-010: wrapped in a subtle cross-fade keyed on the selected
            // day, routed through NourMotion.duration(...) so the app's
            // "Remove animations" setting collapses it to an instant swap.
            // Rest state is pixel-identical to the previous static column.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = NourSize.touchTarget)
                    .clickable(
                        enabled = dayOffset != 0,
                        onClickLabel = strings.dayToday,
                        role = Role.Button,
                        onClick = onResetToToday
                    )
                    .testTag("day_switcher_reset")
                    .padding(vertical = NourSpace.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
            ) {
                AnimatedContent(
                    targetState = selectedDate,
                    transitionSpec = {
                        fadeIn(tween(NourMotion.duration(NourMotion.short))) togetherWith
                            fadeOut(tween(NourMotion.duration(NourMotion.short)))
                    },
                    contentAlignment = Alignment.TopCenter,
                    label = "day_switcher_label"
                ) { activeDate ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
                    ) {
                        val hijri = remember(activeDate) { HijriDateHelper.fromGregorian(activeDate) }
                        val dayLabel = remember(activeDate, dayOffset, strings) {
                            val date = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(activeDate)
                            when (dayOffset) {
                                0 -> "${strings.dayToday} • $date"
                                1 -> "${strings.dayTomorrow} • $date"
                                -1 -> "${strings.dayYesterday} • $date"
                                else -> date
                            }
                        }
                        Text(formatNumerals(dayLabel), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        // One merged Hijri line, labelled "(calculated)": the app uses
                        // a classical tabular calculation, not a moon sighting, and
                        // the label must say so honestly (NUR-036). The line may wrap
                        // to two lines at very large font scales instead of clipping.
                        Text(
                            formatNumerals(
                                if (isArabicUi) hijri.formattedAr
                                else hijri.formattedEn
                            ) + " " + strings.hijriCalculatedSuffix,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            NourCircularIconButton(
                onClick = onNextDay,
                contentDescription = strings.nextBtn,
                modifier = Modifier.testTag("day_switcher_next")
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(NourSize.smallIcon)
                )
            }
        }
    }
}
