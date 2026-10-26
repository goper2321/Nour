package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.features.quran.ui.ScriptureBox
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * NUR-016 — Arabic scripture RTL direction independence.
 *
 * Proves the [ScriptureBox] helper scopes Arabic scripture blocks to
 * [LayoutDirection.Rtl] regardless of the interface's layout direction, and
 * that a block OUTSIDE the box keeps the interface's own direction (so a
 * translation below an Arabic verse still reads left-to-right in a French
 * interface).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class QuranScriptureBoxTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun scriptureBoxForcesRtlInsideAnLtrInterface() {
        var observedInside: LayoutDirection? = null

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                // App-language provider defaults to English -> interface LTR.
                ScriptureBox(Modifier.fillMaxWidth()) {
                    observedInside = LocalLayoutDirection.current
                }
            }
        }
        compose.waitForIdle()

        assertEquals(LayoutDirection.Rtl, observedInside)
    }

    @Test
    fun arabicVerseBlockIsRtlWhileTranslationBelowStaysLtr() {
        var observedScripture: LayoutDirection? = null
        var observedTranslation: LayoutDirection? = null

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                Column(Modifier.fillMaxWidth()) {
                    // Arabic verse block above.
                    ScriptureBox(Modifier.fillMaxWidth()) {
                        observedScripture = LocalLayoutDirection.current
                    }
                    // Translation below keeps the interface (LTR) direction.
                    Box(Modifier.fillMaxWidth()) {
                        observedTranslation = LocalLayoutDirection.current
                        Text("Translation of the verse")
                    }
                }
            }
        }
        compose.waitForIdle()

        assertEquals(LayoutDirection.Rtl, observedScripture)
        assertEquals(LayoutDirection.Ltr, observedTranslation)
    }

    @Test
    fun neighboursKeepTheirOwnDirectionUnderAnRtlInterface() {
        var observedScripture: LayoutDirection? = null
        var observedInterfaceBlock: LayoutDirection? = null

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                // Simulate an Arabic interface: surrounding content is RTL.
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth()) {
                            observedInterfaceBlock = LocalLayoutDirection.current
                            Text("Interface text")
                        }
                        ScriptureBox(Modifier.fillMaxWidth()) {
                            observedScripture = LocalLayoutDirection.current
                        }
                    }
                }
            }
        }
        compose.waitForIdle()

        // Scripture stays RTL and the interface block stays RTL because the
        // interface itself is RTL — the box neither forces nor overrides it.
        assertEquals(LayoutDirection.Rtl, observedScripture)
        assertEquals(LayoutDirection.Rtl, observedInterfaceBlock)
    }
}
