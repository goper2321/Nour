package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.localization.LocalSurfaceStyle
import com.example.ui.theme.NourAlpha
import com.example.ui.theme.SurfaceStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import com.example.ui.theme.PillShape

@Composable
fun NourContentFrame(
    modifier: Modifier = Modifier,
    maxWidth: Dp = NourSize.contentWidth,
    content: @Composable () -> Unit
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxSize()) { content() }
    }
}

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

@Composable
fun NourPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = null,
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
        color = if (emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = NourSpace.md, vertical = NourSpace.xs))
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

/**
 * Blurred status-bar backdrop for full-screen pages: a glass strip over the
 * system status area that softens whatever scrolls beneath it (sky, lists)
 * for icon contrast. Must be placed as a LATER SIBLING of a hazeSource
 * subtree — never inside it (the blur engine excludes its own source).
 * Purely visual: no text, so no localization needed.
 */
@Composable
fun StatusBlurStrip(
    hazeState: HazeState?,
    modifier: Modifier = Modifier
) {
    val surfaceMode = LocalSurfaceStyle.current
    val hazeOn = surfaceMode == SurfaceStyle.BLUR && hazeState != null
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .then(
                if (hazeOn) Modifier.hazeEffect(
                    state = hazeState!!,
                    style = HazeStyle(
                        backgroundColor = Color.Transparent,
                        tint = HazeTint(
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = NourAlpha.SelectedTint)
                        ),
                        blurRadius = 16.dp,
                        noiseFactor = 0f
                    )
                ) else Modifier
            )
            .background(
                when (surfaceMode) {
                    SurfaceStyle.OPAQUE -> MaterialTheme.colorScheme.surfaceContainer
                    SurfaceStyle.TRANSLUCENT -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = NourAlpha.Scrim)
                    else -> Color.Transparent
                }
            )
    )
}
