package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.NourAlpha
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
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxSize().padding(contentPadding)) { content() }
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

/** Consistent group label above settings/section card clusters. Exposed as a heading for TalkBack. */
@Composable
fun NourSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
fun NourPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
        color = if (emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
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
