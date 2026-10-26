package com.example.features.qibla

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaCalibrationHintLogicTest {

    @Test
    fun `constant mirrors SensorManager accuracy levels`() {
        // SENSOR_STATUS_ACCURACY_LOW/GOOD(=2)/HIGH in android.hardware.
        org.junit.Assert.assertEquals(2, QiblaCalibrationHintLogic.SENSOR_STATUS_ACCURACY_MEDIUM)
    }

    @Test
    fun `sensor mode with degraded accuracy shows hint`() {
        assertTrue(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = false,
                hasMagnetometer = true,
                hasHeading = true,
                accuracyInt = 1 // SensorManager.SENSOR_STATUS_ACCURACY_LOW
            )
        )
    }

    @Test
    fun `medium or high accuracy hides hint`() {
        assertFalse(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = false,
                hasMagnetometer = true,
                hasHeading = true,
                accuracyInt = QiblaCalibrationHintLogic.SENSOR_STATUS_ACCURACY_MEDIUM
            )
        )
        assertFalse(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = false,
                hasMagnetometer = true,
                hasHeading = true,
                accuracyInt = 3 // SensorManager.SENSOR_STATUS_ACCURACY_HIGH
            )
        )
    }

    @Test
    fun `unreliable reading (0) shows hint`() {
        // SENSOR_STATUS_ACCURACY_UNRELIABLE: a heading exists but cannot be
        // trusted, which is exactly the "needs calibration" case.
        assertTrue(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = false,
                hasMagnetometer = true,
                hasHeading = true,
                accuracyInt = 0
            )
        )
    }

    @Test
    fun `manual mode never shows hint`() {
        assertFalse(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = true,
                hasMagnetometer = true,
                hasHeading = true,
                accuracyInt = 0
            )
        )
    }

    @Test
    fun `no magnetometer or no heading never shows hint`() {
        assertFalse(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = false,
                hasMagnetometer = false,
                hasHeading = true,
                accuracyInt = 0
            )
        )
        assertFalse(
            QiblaCalibrationHintLogic.shouldShowHint(
                manualMode = false,
                hasMagnetometer = true,
                hasHeading = false,
                accuracyInt = 0
            )
        )
    }
}
