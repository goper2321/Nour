package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.NourElevation
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape
import com.example.ui.theme.SegmentedControlShape

/** One destination in the floating navigation bar / rail. */
data class NourNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * Floating, opaque bottom navigation for compact windows. Keeps an 8 dp gap
 * above the system navigation inset — it never touches the gesture area —
 * with the usual side gutters. The host budgets the inset, that gap, the
 * bar's real measured band plus an 8 dp breathing gap above the band, so
 * resting content sits one small breath above the band's edge at the scroll
 * ends: readable, never hidden behind the pill. The list itself stays free
 * to draw under the system gesture area while scrolling.
 *
 * Selection is deliberately restrained: accent tint, filled icon and a short
 * 16 x 3 dp mark. No glass, no blur, no translucent background.
 */
@Composable
fun NourFloatingNavigationBar(
    items: List<NourNavItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onHeightMeasured: (Dp) -> Unit = {}
) {
    val measuredDensity = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = NourSpace.md, vertical = NourSpace.sm),
        contentAlignment = Alignment.Center
    ) {
        val twoLabelLines = LocalDensity.current.fontScale > 1.05f || maxWidth < 360.dp
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(nourNavigationBarHeight(twoLabelLines))
                .onGloballyPositioned { coords ->
                    // Real laid-out height, so callers can reserve the band
                    // plus the breathing gap (never an assumed token) at any
                    // font scale.
                    onHeightMeasured(measuredDensity.run { coords.size.height.toDp() })
                }
                .testTag("nour_navigation_bar"),
            shape = PillShape,
            color = nourFloatingSurfaceColor(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = NourElevation.floating
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .selectableGroup(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    FloatingNavItem(
                        item = item,
                        selected = index == selectedIndex,
                        onClick = { onSelected(index) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
    }
}

/**
 * Height for the floating bar. On a typical phone at the default font scale
 * the shipped labels fit on one line and the bar keeps its familiar 68 dp
 * footprint. Reserve two label lines at narrow widths or larger fonts rather
 * than clipping the longer translations.
 */
@Composable
private fun nourNavigationBarHeight(twoLabelLines: Boolean): Dp {
    val density = LocalDensity.current
    val lines = if (twoLabelLines) 2 else 1
    val textHeight = with(density) {
        MaterialTheme.typography.labelSmall.lineHeight.toDp() * lines
    }
    // 45 dp = 24 dp icon + 3 dp + 3 dp gaps + 3 dp selection mark + 12 dp row padding.
    return maxOf(NourSize.navBarHeight, textHeight + 45.dp)
}

@Composable
private fun FloatingNavItem(
    item: NourNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(NourMotion.duration(160)),
        label = "navItemColor"
    )
    Box(
        modifier = modifier
            .nourPressScale(interactionSource, pressedScale = 0.95f)
            .clip(RoundedCornerShape(20.dp))
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick
            )
            .testTag(item.testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(NourSize.icon)
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Box(
                Modifier
                    .size(width = 16.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            )
        }
    }
}

/**
 * Floating leading rail for expanded windows. Same opaque surface and
 * restrained selection language as the bottom bar; the host reserves
 * [NourSize.navRailReserve] so it never overlaps content.
 */
@Composable
fun NourFloatingNavigationRail(
    items: List<NourNavItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(vertical = NourSpace.sm),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.width(NourSize.navRailWidth),
            shape = CardExpressiveShape,
            color = nourFloatingSurfaceColor(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = NourElevation.floating
        ) {
            Column(
                modifier = Modifier
                    .padding(6.dp)
                    .verticalScroll(rememberScrollState())
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(NourSpace.xs)
            ) {
                items.forEachIndexed { index, item ->
                    FloatingRailItem(
                        item = item,
                        selected = index == selectedIndex,
                        onClick = { onSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingRailItem(
    item: NourNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(NourMotion.duration(160)),
        label = "railItemColor"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = NourSize.touchTarget)
            .nourPressScale(interactionSource, pressedScale = 0.95f)
            .clip(RoundedCornerShape(18.dp))
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick
            )
            .testTag(item.testTag)
            .padding(vertical = NourSpace.xs),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(NourSize.icon)
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Box(
                Modifier
                    .size(width = 14.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            )
        }
    }
}

/**
 * Top floating toolbar for contextual actions. Solid chrome, 24 dp corners,
 * 52 dp minimum height and 48 dp action targets. Reports its real laid-out
 * height via [onHeightMeasured] so overlaying screens can reserve the band
 * plus a small 8 dp breathing gap at every font scale.
 */
@Composable
fun NourFloatingToolbar(
    modifier: Modifier = Modifier,
    onHeightMeasured: (Dp) -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val measuredDensity = LocalDensity.current
    Surface(
        modifier = modifier.onGloballyPositioned { coords ->
            onHeightMeasured(measuredDensity.run { coords.size.height.toDp() })
        },
        shape = PillShape,
        color = nourFloatingSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = NourElevation.toolbar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NourSize.toolbarHeight)
                .padding(horizontal = 6.dp, vertical = NourSpace.xs),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/** One option in a [NourSegmentedControl]. */
data class NourSegmentOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val testTag: String? = null
)

/**
 * Height needed for a [NourSegmentedControl] whose label may wrap to two
 * lines. Exposed so screens that overlay the control can reserve the same
 * footprint at every font scale.
 */
@Composable
fun nourSegmentedControlHeight(): Dp {
    // Convert one label line at the current (possibly non-linear) font scale,
    // then multiply. Converting 40.sp in one step is not equivalent to two
    // 20.sp lines on Android 14+ non-linear font scaling.
    val lineHeight = MaterialTheme.typography.labelLarge.lineHeight
    val twoLines = with(LocalDensity.current) { lineHeight.toDp() * 2 }
    return maxOf(52.dp, twoLines + 8.dp)
}

/**
 * iOS-style segmented control: a solid track with a raised, opaque selected
 * thumb. Use it for two to four mutually exclusive views, never as a button
 * row. Labels wrap to at most two lines rather than shrinking type or being
 * clipped; the control grows when the text needs more height.
 */
@Composable
fun <T> NourSegmentedControl(
    options: List<NourSegmentOption<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role = Role.Tab
) {
    val darkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val trackContainer = if (darkSurface) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val selectedContainer = if (darkSurface) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    Surface(
        modifier = modifier.fillMaxWidth().height(nourSegmentedControlHeight()),
        shape = SegmentedControlShape,
        color = trackContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option.value == selected
                val background by animateColorAsState(
                    targetValue = if (isSelected) selectedContainer else Color.Transparent,
                    animationSpec = tween(NourMotion.duration(180)),
                    label = "segmentBackground"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(NourMotion.duration(180)),
                    label = "segmentContent"
                )
                val interactionSource = remember { MutableInteractionSource() }
                val shadowModifier = if (isSelected) {
                    Modifier.shadow(NourElevation.control, PillShape)
                } else {
                    Modifier
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(shadowModifier)
                        .clip(PillShape)
                        .background(background)
                        .then(
                            if (isSelected) {
                                Modifier.border(NourSize.border, MaterialTheme.colorScheme.outline, PillShape)
                            } else {
                                Modifier
                            }
                        )
                        .selectable(
                            selected = isSelected,
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            enabled = enabled,
                            role = role,
                            onClick = { onSelected(option.value) }
                        )
                        .then(if (option.testTag != null) Modifier.testTag(option.testTag) else Modifier)
                        .padding(horizontal = NourSpace.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (option.icon != null) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = contentColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
