package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.features.onboarding.ui.SetupWizard
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SetupWizardTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun setupWizard_fullNavigationFlowCompletesSuccessfully() {
        var completed = false
        var currentLang = AppLanguage.ENGLISH
        var userLoc = UserLocation(21.42, 39.83, "Makkah", "Saudi Arabia", 3.0, false)
        var alarmsOn = true
        var calcMethod = CalculationMethod.UMM_AL_QURA
        var madhabChoice = Madhab.SHAFI_MALIKI_HANBALI
        var use24h = true
        var easternNum = false

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SetupWizard(
                    currentLanguage = currentLang,
                    onLanguageChange = { currentLang = it },
                    userLocation = userLoc,
                    onLocationChange = { userLoc = it },
                    alarmsEnabled = alarmsOn,
                    onAlarmsEnabledChange = { alarmsOn = it },
                    calculationMethod = calcMethod,
                    onCalculationMethodChange = { calcMethod = it },
                    madhab = madhabChoice,
                    onMadhabChange = { madhabChoice = it },
                    useEasternNumerals = easternNum,
                    onEasternNumeralsChange = { easternNum = it },
                    use24Hour = use24h,
                    on24HourChange = { use24h = it },
                    onSetupComplete = { completed = true }
                )
            }
        }

        // Step 0: Welcome Screen
        compose.onNodeWithTag("setup_wizard_root").assertIsDisplayed()
        compose.onNodeWithTag("setup_next_button").assertIsDisplayed().performClick()

        // Step 1: Language Screen
        compose.waitForIdle()
        compose.onNodeWithTag("setup_time_format_toggle").assertExists()
        compose.onNodeWithTag("setup_next_button").performClick()

        // Step 2: Location Screen
        compose.waitForIdle()
        compose.onNodeWithTag("setup_open_city_picker").assertExists()
        compose.onNodeWithTag("setup_next_button").performClick()

        // Step 3: Adhan Screen
        compose.waitForIdle()
        compose.onNodeWithTag("setup_alarms_master_switch").assertExists()
        compose.onNodeWithTag("setup_next_button").performClick()

        // Step 4: Ready Screen
        compose.waitForIdle()
        compose.onNodeWithTag("setup_finish_button").assertExists().performClick()

        compose.runOnIdle {
            assertTrue("Expected onSetupComplete to have been called", completed)
        }
    }

    @Test
    fun setupWizard_skipButtonCompletesSetupDirectly() {
        var completed = false

        compose.setContent {
            MyApplicationTheme(dynamicColor = false) {
                SetupWizard(
                    currentLanguage = AppLanguage.ENGLISH,
                    onLanguageChange = {},
                    userLocation = UserLocation(21.42, 39.83, "Makkah", "Saudi Arabia", 3.0, false),
                    onLocationChange = {},
                    alarmsEnabled = true,
                    onAlarmsEnabledChange = {},
                    onSetupComplete = { completed = true }
                )
            }
        }

        // Click Skip from the first step
        compose.waitForIdle()
        compose.onNodeWithTag("setup_skip_button").assertExists().performClick()

        compose.runOnIdle {
            assertTrue("Expected onSetupComplete to have been called on skip", completed)
        }
    }
}
