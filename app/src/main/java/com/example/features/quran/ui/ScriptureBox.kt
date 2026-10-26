package com.example.features.quran.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * NUR-016 — Arabic scripture direction independence.
 *
 * Renders an Arabic scripture block inside its own RTL layout scope, so the
 * block's paragraph direction comes from the TEXT's script, never from the
 * interface language. A user reading the interface in French (LTR) and a user
 * reading it in Arabic (RTL) get the same right-to-left scripture behaviour.
 *
 * Scope discipline:
 *  - Wrap ONLY Arabic scripture blocks (verse text). Translations, tafsir and
 *    other Latin/localisation text stay outside the box and keep the
 *    interface's own direction (acceptance: "translation below reads LTR").
 *  - The box must not mirror surrounding scaffolding: it owns exactly the
 *    scripture block, never a whole screen or a Row the block shares with
 *    interface-language text (mixed rows stay in the caller's direction;
 *    Arabic runs inside a shared string are protected by the existing
 *    [String.isolateRtlDirection] bidi isolates).
 *
 * Line alignment stays visually END-ALIGNED to the right edge for scripture:
 * call sites keep their right-text-align under the box (absolute `Right`
 * alignment is direction-of-scope independent), and every wrapped block uses
 * symmetric all-side padding, so RTL mirroring of paddings is a no-op here.
 */
@Composable
internal fun ScriptureBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
            content()
        }
    }
}
