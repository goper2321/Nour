package com.example

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.features.quran.data.QuranRepository
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.NourPreferences
import com.example.ui.components.LocalFloatingBottomExtra
import com.example.ui.components.NourFloatingNavigationBar
import com.example.ui.components.NourNavItem
import com.example.ui.components.NourContentFrame
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NourSize
import com.example.ui.theme.NourSpace
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Floating chrome contract verification.
 *
 * Renders the two floating chrome surfaces and asserts the band-plus-breathing
 * contract: the pill keeps an 8 dp gap above the system gesture area, and the
 * screens' absolute bottom reserve is the inset + that gap + the bar's real
 * measured band + an 8 dp breath — resting content sits one small breath
 * above the band's edge (readable, never hidden behind the pill) at every
 * font scale.
 *
 * Structural checks only: sensors, TalkBack and real IME behaviour still need
 * a physical device.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class FloatingChromeTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun pauseClock() {
        compose.mainClock.autoAdvance = false
    }

    private fun settle(frames: Int = 6) {
        repeat(frames) { compose.mainClock.advanceTimeByFrame() }
    }

    @Composable
    private fun ScaledDensity(fontScale: Float, content: @Composable () -> Unit) {
        val base = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale), content = content)
    }

    /** Laid-out bounds in dp of a tagged node. */
    private data class BoundsDp(val top: Dp, val bottom: Dp, val screenHeight: Dp) {
        val bottomFromEdge: Dp get() = screenHeight - bottom
        val topFromEdge: Dp get() = screenHeight - top
    }

    private fun boundsOf(tag: String): BoundsDp {
        val node = compose.onNodeWithTag(tag).fetchSemanticsNode()
        val density = compose.density.density
        fun pxToDp(px: Float): Dp = Dp(px / density)
        return BoundsDp(
            top = pxToDp(node.positionInRoot.y),
            bottom = pxToDp(node.positionInRoot.y + node.size.height),
            screenHeight = pxToDp(
                compose.onRoot().fetchSemanticsNode().size.height.toFloat()
            )
        )
    }

    private fun assertDpClose(expected: Dp, actual: Dp, message: String) {
        val delta = kotlin.math.abs(expected.value - actual.value)
        assertEquals(message, 0f, delta, 0.6f)
    }

    // ---------- Reader toolbar: resting card one breath above the band ----------

    private fun readerScreen(cards: Boolean, fontScale: Float = 1f) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NourPreferences(context).readingMode = if (cards) "CARDS" else "CONTINUOUS"
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(fontScale) {
                    NourContentFrame {
                        QuranScreen(
                            openSurahRequest = QuranRepository.getSurah(1),
                            onOpenSurahConsumed = {}
                        )
                    }
                }
            }
        }
        settle()
    }

    private fun assertReaderToolbarBreath(fontLabel: String) {
        val pill = boundsOf("surah_reader_toolbar")
        val header = boundsOf("reader_header_card")
        // Reserve = the toolbar's real measured band plus an 8 dp breath: the
        // first card sits one small breath below the pill's bottom edge —
        // readable, never hidden, and no slim dead band between them.
        assertDpClose(pill.bottom + NourSpace.sm, header.top,
            "Reader $fontLabel: card sits one 8 dp breath below the toolbar band")
        // The pill actually sits sm below the very top (sanity).
        assertDpClose(NourSpace.sm, pill.top, "Reader $fontLabel: toolbar top offset")
    }

    @Test
    fun readerToolbarBreathBelowBandDefaultFont() {
        readerScreen(cards = true, fontScale = 1f)
        assertReaderToolbarBreath("font 1.0")
    }

    @Test
    fun readerToolbarBreathBelowBandFont160() {
        readerScreen(cards = true, fontScale = 1.6f)
        assertReaderToolbarBreath("font 1.6")
    }

    @Test
    fun readerToolbarBreathBelowBandFont200() {
        readerScreen(cards = true, fontScale = 2f)
        assertReaderToolbarBreath("font 2.0")
    }

    @Test
    fun readerToolbarBreathBelowBandContinuousMode() {
        readerScreen(cards = false, fontScale = 1f)
        assertReaderToolbarBreath("continuous")
    }

    // ---------- Bottom bar: resting content one breath above the band ----------

    /**
     * Mimics NourMainActivity's compact arrangement: the content's bottom
     * padding is absolute — host formula = system inset (0 in Robolectric) +
     * the bar's 8 dp gap above it + the bar's real measured band + an 8 dp
     * breath — so resting content sits one small breath above the band's
     * edge while the pill keeps its own gap above the gesture area.
     */
    private fun bottomBarScreen(fontScale: Float = 1f) {
        val items = listOf(
            NourNavItem("Salah", Icons.Rounded.Schedule, Icons.Outlined.Schedule, "nav_salah"),
            NourNavItem("Qibla", Icons.Rounded.Explore, Icons.Outlined.Explore, "nav_qibla"),
            NourNavItem("Quran", Icons.Rounded.AutoStories, Icons.Outlined.AutoStories, "nav_quran"),
            NourNavItem("Settings", Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_settings")
        )
        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                ScaledDensity(fontScale) {
                    Box(Modifier.fillMaxSize()) {
                        var barHeight by remember { mutableStateOf(NourSize.navBarHeight) }
                        // Exact MainActivity formula (Robolectric nav inset is 0):
                        // inset + the bar's 8 dp gap + measured band + 8 dp breath.
                        val extra = (NourSpace.sm + barHeight + NourSpace.sm)
                            .coerceAtLeast(0.dp)
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(bottom = extra)
                                .testTag("probe_content"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(6) { row ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .size(0.dp, 64.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHighest,
                                            RoundedCornerShape(16.dp)
                                        )
                                )
                            }
                        }
                        NourFloatingNavigationBar(
                            items = items,
                            selectedIndex = 0,
                            onSelected = {},
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .testTag("probe_bar_wrapper"),
                            onHeightMeasured = { barHeight = it }
                        )
                    }
                }
            }
        }
        settle()
    }

    @Test
    fun bottomBarReserveBreathAboveBandAtScrollEnd() {
        bottomBarScreen(fontScale = 1f)
        val content = boundsOf("probe_content")
        val pill = boundsOf("nour_navigation_bar")
        // Reserve = inset + the bar's 8 dp gap + measured band + 8 dp breath:
        // resting content sits one small breath above the band's edge —
        // readable, never hidden behind the pill (Robolectric nav inset is 0).
        assertDpClose(pill.topFromEdge + NourSpace.sm, content.bottomFromEdge,
            "Bottom bar: content reserve = measured band + one 8 dp breath")
        // The pill keeps its 8 dp gap above the system gesture area.
        assertDpClose(NourSpace.sm, pill.bottomFromEdge, "Bottom bar: gap above the inset")
    }

    @Test
    fun bottomBarReserveBreathAboveBandFont200() {
        bottomBarScreen(fontScale = 2f)
        val content = boundsOf("probe_content")
        val pill = boundsOf("nour_navigation_bar")
        assertDpClose(pill.topFromEdge + NourSpace.sm, content.bottomFromEdge,
            "Bottom bar @200%: reserve = taller two-line band + one 8 dp breath")
    }
}
