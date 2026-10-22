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
 * Floating clearance rule verification ([NourSize.floatingClearance]).
 *
 * Renders the two floating chrome surfaces and asserts the layout contract:
 * at the scroll ends the pill floats entirely on its own empty slot — one
 * clearance of air between the band and any resting content — and the reserve
 * follows the pill's real measured band so large font scales (where assumed
 * heights used to drift by 30-48 dp) stay exact.
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

    // ---------- Reader toolbar: measured band plus clearance, at every font scale ----------

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

    private fun assertReaderToolbarClearance(fontLabel: String) {
        val pill = boundsOf("surah_reader_toolbar")
        val header = boundsOf("reader_header_card")
        // The header card (first list item) starts exactly one clearance above
        // the toolbar's bottom edge: reserve top == sm(8) + pillHeight +
        // clearance; pill bottom == sm + pillHeight ⇒ header.top − pill.bottom
        // == clearance. Nothing of the card sits behind the pill.
        assertDpClose(NourSize.floatingClearance, header.top - pill.bottom,
            "Reader $fontLabel: air between toolbar and resting card must equal the clearance")
        // The pill actually sits sm below the very top (sanity).
        assertDpClose(NourSpace.sm, pill.top, "Reader $fontLabel: toolbar top offset")
        // The pill grows with font scale — assert the reserve tracks the real
        // measured band, so the air above it is always the clearance, never a
        // leftover token amount.
        assertDpClose(header.top - NourSize.floatingClearance, pill.bottom,
            "Reader $fontLabel: reserve tracks the measured band plus clearance")
    }

    @Test
    fun readerToolbarClearanceDefaultFont() {
        readerScreen(cards = true, fontScale = 1f)
        assertReaderToolbarClearance("font 1.0")
    }

    @Test
    fun readerToolbarClearanceFont160() {
        readerScreen(cards = true, fontScale = 1.6f)
        assertReaderToolbarClearance("font 1.6")
    }

    @Test
    fun readerToolbarClearanceFont200() {
        readerScreen(cards = true, fontScale = 2f)
        assertReaderToolbarClearance("font 2.0")
    }

    @Test
    fun readerToolbarClearanceContinuousMode() {
        readerScreen(cards = false, fontScale = 1f)
        assertReaderToolbarClearance("continuous")
    }

    // ---------- Bottom bar: measured band plus clearance, at every font scale ----------

    /**
     * Mimics NourMainActivity's compact arrangement: a host box whose content
     * area keeps the baseline 16 dp, plus the measured band + clearance slot
     * that screens consume via [LocalFloatingBottomExtra]; the bar overlays.
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
                        // Exact MainActivity formula (Robolectric nav inset is 0).
                        val extra = (NourSpace.sm + barHeight + NourSize.floatingClearance - NourSpace.lg)
                            .coerceAtLeast(NourSpace.lg)
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(bottom = NourSpace.lg + extra)
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
    fun bottomBarReserveTracksMeasuredBand() {
        bottomBarScreen(fontScale = 1f)
        val content = boundsOf("probe_content")
        val pill = boundsOf("nour_navigation_bar")
        // When the list scrolls to its very end, the last row ends exactly one
        // clearance above the pill's band: the band top distance from the
        // screen bottom is the pill's top edge; add the clearance slot.
        assertDpClose(pill.topFromEdge + NourSize.floatingClearance, content.bottomFromEdge,
            "Bottom bar: content reserve must equal measured band plus the clearance")
        // And the pill band really starts sm above the nav inset (Robolectric: 0).
        assertDpClose(NourSpace.sm, pill.bottomFromEdge, "Bottom bar: pill bottom gap")
    }

    @Test
    fun bottomBarReserveTrackScaleFont200() {
        bottomBarScreen(fontScale = 2f)
        val content = boundsOf("probe_content")
        val pill = boundsOf("nour_navigation_bar")
        assertDpClose(pill.topFromEdge + NourSize.floatingClearance, content.bottomFromEdge,
            "Bottom bar @200%: reserve follows the taller two-line bar")
    }
}
