package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.InputExpressiveShape
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.NourMotion
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape

@Composable
fun NourContentFrame(
    modifier: Modifier = Modifier,
    maxWidth: Dp = NourSize.contentWidth,
    /**
     * Global side gutter. Applied once here so every screen shares the same
     * left/right margin; screens whose own background must bleed to the edge
     * can pass [PaddingValues] zero and inset their content instead.
     */
    contentPadding: PaddingValues = PaddingValues(horizontal = NourSpace.lg),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxSize().padding(contentPadding)) { content() }
    }
}

/**
 * Opaque floating chrome colour: white over the light grouped background,
 * elevated charcoal over the dark background. Never translucent, never blur.
 */
@Composable
fun nourFloatingSurfaceColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

/** Opaque grouped-list card colour (white in light, raised charcoal in dark). */
@Composable
fun nourCardSurfaceColor(): Color = MaterialTheme.colorScheme.surfaceContainerLow

@Composable
fun NourScreenHeading(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(NourSpace.sm)) {
        Text(title, style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Consistent group label above settings/section card clusters. Exposed as a heading for TalkBack. */
@Composable
fun NourSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
fun NourPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = NourSize.flat)
    ) {
        Column(Modifier.padding(NourSpace.lg), verticalArrangement = Arrangement.spacedBy(NourSpace.md), content = content)
    }
}

@Composable
fun NourBadge(text: String, emphasized: Boolean = false, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = PillShape,
        color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (emphasized) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = NourSpace.md, vertical = 5.dp))
    }
}

@Composable
fun NourEmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(NourSpace.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------- Shared interaction ----------

/**
 * Short, restrained press feedback for compact controls. Routes through
 * [NourMotion.spring], so "Remove animations" turns it into an instant change.
 */
@Composable
internal fun Modifier.nourPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = NourMotion.spring(),
        label = "nourPressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ---------- Apple HIG / iOS 26–27 reusable primitives ----------

/**
 * Circular icon action. The visible circle is 44 dp while the layout keeps the
 * Android 48 dp minimum touch target. [contentDescription] is applied to the
 * single focusable node; pass `null` to the inner Icon to avoid duplicate
 * announcements.
 */
@Composable
fun NourCircularIconButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visualSize: Dp = NourSize.circularControl,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(NourSize.touchTarget)
            .nourPressScale(interactionSource)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                if (!contentDescription.isNullOrBlank()) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(visualSize),
            shape = CircleShape,
            color = if (enabled) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
            contentColor = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { icon() }
        }
    }
}

enum class NourButtonVariant { Primary, Tonal, Outlined }

/**
 * Capsule action. Use Primary for the single most important action on a
 * surface, Tonal for supporting actions, and Outlined for controls that need
 * to stay quiet (for example the Qibla manual-mode toggle).
 */
@Composable
fun NourCapsuleButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: NourButtonVariant = NourButtonVariant.Primary,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = MaterialTheme.colorScheme
    val container = when (variant) {
        NourButtonVariant.Primary -> colors.primary
        NourButtonVariant.Tonal -> colors.secondaryContainer
        NourButtonVariant.Outlined -> nourFloatingSurfaceColor()
    }
    val content = when (variant) {
        NourButtonVariant.Primary -> colors.onPrimary
        NourButtonVariant.Tonal -> colors.onSecondaryContainer
        NourButtonVariant.Outlined -> colors.primary
    }
    val border = when (variant) {
        NourButtonVariant.Outlined -> BorderStroke(NourSize.border, colors.outlineVariant)
        else -> null
    }
    val effectiveContainer = if (enabled) container else colors.surfaceContainer
    val effectiveContent = if (enabled) content else colors.onSurface.copy(alpha = 0.38f)

    Row(
        modifier = modifier
            .heightIn(min = NourSize.touchTarget)
            .nourPressScale(interactionSource)
            .clip(PillShape)
            .background(effectiveContainer)
            .then(if (border != null) Modifier.border(border, PillShape) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = NourSpace.xl, vertical = NourSpace.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NourSpace.sm)
    ) {
        leadingIcon?.invoke()
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = effectiveContent
        )
        trailingIcon?.invoke()
    }
}

/**
 * iOS-style search field. Solid surface, hairline border, accent only on focus.
 * The optional clear action keeps its 48 dp target and localized description.
 * [fieldModifier] is applied to the editable text node, so tests and semantics
 * can target the field itself rather than the whole surface.
 */
@Composable
fun NourSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    clearContentDescription: String? = null,
    focusRequester: FocusRequester? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = androidx.compose.animation.core.tween(NourMotion.duration(160)),
        label = "nourSearchBorder"
    )
    val requesterModifier = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier

    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = NourSize.input),
        shape = InputExpressiveShape,
        color = nourFloatingSurfaceColor(),
        border = BorderStroke(if (focused) NourSize.selectedBorder else NourSize.border, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(start = NourSpace.lg, end = NourSpace.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(NourSize.smallIcon)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = NourSpace.md)
                    .then(fieldModifier)
                    .then(requesterModifier)
                    .onFocusChanged { focused = it.isFocused },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                // NUR-015: ellipsize rather than clip a long
                                // placeholder at small field widths.
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (value.isNotEmpty() && clearContentDescription != null) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = clearContentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(Modifier.size(NourSpace.sm))
            }
        }
    }
}

@Composable
fun NourLoadingState(message: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(NourSpace.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (!message.isNullOrBlank()) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NourErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier.fillMaxWidth().padding(NourSpace.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NourSpace.md)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { heading() })
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onRetry != null && !retryLabel.isNullOrBlank()) {
            NourCapsuleButton(onClick = onRetry, text = retryLabel)
        }
    }
}
