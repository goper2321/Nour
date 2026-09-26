package com.example.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Reduced-motion support.
 *
 * Compose has no first-class "the user asked for less motion" flag, but Android
 * does: the accessibility setting *Remove animations* (and the developer
 * "Animator duration scale = 0" option) is exposed as
 * `Settings.Global.ANIMATOR_DURATION_SCALE`, which is 0 when animations are off.
 *
 * We read that once per composition, publish it through [Local], and store it in
 * [NourMotion.reduced] so the animation-spec helpers in `Tokens.kt` - which have
 * no composable context - can collapse to an instant change.
 *
 * The case that matters most is continuous decorative motion (the sky
 * visualizer, the prayer-header glow), which can trigger vestibular symptoms.
 * When motion is reduced the UI still renders its final visual state; it simply
 * stops moving.
 */
object NourReduceMotion {

    /** True when the system animation scale is zeroed. Fails safe to "motion on". */
    fun isEnabled(context: Context): Boolean = runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }.getOrDefault(false)

    /** Read once and remembered for the current composition. */
    @Composable
    fun remember(): Boolean {
        val context = LocalContext.current
        return remember(context) { isEnabled(context) }
    }

    val Local: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }
}
