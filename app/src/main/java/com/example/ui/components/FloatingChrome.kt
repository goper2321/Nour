package com.example.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace

/**
 * Extra bottom padding a scroll surface must add so its last row respects the
 * floating bottom bar's clearance rule ([NourSize.floatingClearance]).
 *
 * NourMainActivity provides the measured value on the compact layout:
 * (bar band + clearance − the host's baseline bottom rhythm) so screens don't
 * need to know insets or bar geometry. Without a provider it falls back to
 * [NourSpace.lg], which is also what the expanded (rail) layout uses — no
 * floating bar there, so screens render exactly as before.
 *
 * The reader's top toolbar applies the same rule via its own measured
 * reserve; it is screen-local, not per-activity.
 */
val LocalFloatingBottomExtra: androidx.compose.runtime.ProvidableCompositionLocal<Dp> =
    compositionLocalOf { NourSpace.lg }
