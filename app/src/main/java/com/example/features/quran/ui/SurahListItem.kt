package com.example.features.quran.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NourAlpha
import com.example.features.quran.models.Surah
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.formatNumerals
import com.example.ui.components.nourCardSurfaceColor
import com.example.ui.theme.CardSubtleShape
import com.example.ui.theme.ListRowShape
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace

/** Compact directory row shared by the library and search results. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurahListItem(surah: Surah, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val revelation = when (surah.revelationType) {
        com.example.features.quran.models.RevelationType.MECCAN -> strings.meccan
        com.example.features.quran.models.RevelationType.MEDINAN -> strings.medinan
    }
    val arabicUi = LocalAppLanguage.current == AppLanguage.ARABIC
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = ListRowShape,
        colors = CardDefaults.cardColors(containerColor = nourCardSurfaceColor()),
        border = BorderStroke(NourSize.border, MaterialTheme.colorScheme.outlineVariant.copy(alpha = NourAlpha.CardBorder))) {
        Row(Modifier.padding(horizontal = NourSpace.lg, vertical = NourSpace.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NourSpace.md)) {
            Surface(shape = CardSubtleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(NourSize.badge)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        formatNumerals(surah.number.toString()),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        // NUR-015: ellipsize rather than clip (audit parity).
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(NourSpace.sm),
                verticalArrangement = Arrangement.spacedBy(NourSpace.xs)) {
                Column(Modifier.weight(1f)) {
                    Text(if (arabicUi) surah.nameArabic else surah.nameEnglish, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Text(formatNumerals("${surah.ayahCount} ${strings.versesWord} · $revelation"),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    if (!arabicUi) {
                        Text(surah.englishMeaning, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                if (!arabicUi) {
                    Text(surah.nameArabic, style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = NourSpace.xs))
                }
            }
        }
    }
}
