package com.example.features.prayer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Atmospheric Day Switcher placed directly over the prayer times list.
 * Allows smooth day navigation (yesterday, today, tomorrow, and future/past days)
 * with a single-tap reset to Today when navigated away.
 */
@Composable
fun DaySwitcherBar(
    selectedDate: Date,
    dayOffset: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabel = remember(selectedDate, dayOffset) {
        val shortSdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        when (dayOffset) {
            0 -> "Today • ${shortSdf.format(selectedDate)}"
            1 -> "Tomorrow • ${shortSdf.format(selectedDate)}"
            -1 -> "Yesterday • ${shortSdf.format(selectedDate)}"
            else -> {
                val fullSdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                fullSdf.format(selectedDate)
            }
        }
    }

    val textShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.6f),
            offset = Offset(0f, 1.5f),
            blurRadius = 4f
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_switcher_bar"),
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Day Button
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("day_switcher_prev")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Content: Animated Date text & optional "Today" badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    targetState = dayLabel,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally()).togetherWith(fadeOut() + slideOutHorizontally())
                    },
                    label = "day_label_anim"
                ) { label ->
                    Text(
                        text = label,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = textShadow
                        )
                    )
                }

                // If not viewing today, show quick-return "Today" capsule
                if (dayOffset != 0) {
                    Surface(
                        onClick = onResetToToday,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.testTag("day_switcher_today_btn")
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Next Day Button
            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("day_switcher_next")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
