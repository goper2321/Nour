package com.example

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NourMotion
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * NUR-012: the progress fill must hit its endpoints *exactly* under the
 * "Remove animations" setting — NourMotion.duration collapses the tween to
 * 0 ms, so the first frame the animation actually computes renders the target
 * exactly (no halfway "dead progress" stop and no decorative sweep). Compose's
 * Animatable only *computes* its first value on a frame after the target-change
 * effects have run, so each read happens after a bounded frame advance. The
 * non-reduced companion proves the tween really animates when the setting is
 * untouched (it starts well below the midpoint and reaches 100% only at the
 * end of its 2000 ms duration).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReducedMotionBarEndpointsTest {
    @get:Rule
    val compose = createComposeRule()

    @After
    fun restoreMotionSetting() {
        NourMotion.reduced = false
    }

    private fun currentBarValue(): Float =
        compose.onNodeWithTag("nur012_bar")
            .fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
            .current

    @Test
    fun reducedMotion_barRestsOn0_thenLandsExactlyOn100() {
        NourMotion.reduced = true
        compose.mainClock.autoAdvance = false
        val target = mutableFloatStateOf(0f)

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                val animated by animateFloatAsState(
                    targetValue = target.floatValue,
                    animationSpec = tween(
                        durationMillis = NourMotion.duration(2000),
                        easing = LinearEasing
                    ),
                    label = "nur012_fill"
                )
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier.testTag("nur012_bar")
                )
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        assertEquals("reduced-motion render at value 0 must equal the animated endpoint 0", 0f, currentBarValue(), 1e-6f)

        target.floatValue = 1f
        // The Animatable only COMPUTES its first value on a frame after the
        // target-change effects have run, so one `advanceTimeByFrame()` after
        // the change is too early to read anything (the old single-frame read
        // saw the untouched 0.0 and looked "dead"). So advance a bounded
        // number of frames and hold two invariants across every frame we
        // visit: (1) nothing halfway — under reduced motion the tween
        // duration collapses to 0 ms, and a finished 0 ms tween returns its
        // target exactly, so the bar can only be at 0 or at 1, never in
        // between; (2) the endpoint is actually reached — never dead.
        var landed = false
        repeat(3) {
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
            val value = currentBarValue()
            assertTrue(
                "reduced motion must never sweep halfway (saw $value mid-fill)",
                value == 0f || value == 1f
            )
            if (value == 1f) {
                landed = true
                return@repeat
            }
        }
        compose.waitForIdle()
        assertTrue(
            "under reduced motion the fill must land exactly on 100% once the animation computes — never halfway, never dead",
            landed
        )
    }

    @Test
    fun normalMotion_barAnimatesThroughItsDuration() {
        NourMotion.reduced = false
        compose.mainClock.autoAdvance = false
        val target = mutableFloatStateOf(0f)

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                val animated by animateFloatAsState(
                    targetValue = target.floatValue,
                    animationSpec = tween(
                        durationMillis = NourMotion.duration(2000),
                        easing = LinearEasing
                    ),
                    label = "nur012_fill"
                )
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier.testTag("nur012_bar")
                )
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        assertEquals(0f, currentBarValue(), 1e-6f)

        target.floatValue = 1f
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        // One frame (~16 ms) into a 2000 ms linear tween: barely started.
        assertTrue(
            "with animations enabled the fill must still be near the start after one frame",
            currentBarValue() < 0.1f
        )

        compose.mainClock.advanceTimeBy(2000)
        compose.waitForIdle()
        // The tween runs exactly NourMotion.duration(2000) ms. The frame window
        // advanced above leaves the bar just short of its endpoint (0.984 in
        // the first batch's failed run: the last window frame sits at
        // playTime ≈ 1966.7 ms, not 2000 ms), because the animation's own
        // first frames consumed a slice of the window before its start time.
        // So advance a bounded number of frames until the play time reaches
        // the 2000 ms duration, and require the exact endpoint there — the
        // fill completes at the end of the duration, not before, not never.
        var completed = false
        repeat(3) {
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
            if (currentBarValue() == 1f) {
                completed = true
                return@repeat
            }
        }
        compose.waitForIdle()
        assertTrue(
            "the fill completes exactly at the end of NourMotion.duration(2000)",
            completed
        )
    }
}
