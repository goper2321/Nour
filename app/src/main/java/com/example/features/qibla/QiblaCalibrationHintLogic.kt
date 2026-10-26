package com.example.features.qibla

/**
 * Pure decision logic for NUR-037 (additive calibration hint on the Qibla
 * screen).
 *
 * The hint is guidance "how to fix it", shown while a real magnetometer
 * heading exists but its accuracy is degraded — i.e. exactly when the status
 * line already says "calibration needed" but says nothing about what to do.
 * Gating rules, per the checklist item:
 * - Manual mode never shows it: the slider is a reference heading, not a
 *   measurement, so "calibrate" would be misleading there.
 * - No magnetometer or no heading yet: other statuses already own that case
 *   (no sensor / waiting); a figure-8 instruction cannot fix them.
 * - Accuracy at [SENSOR_STATUS_ACCURACY_MEDIUM] or better: calibrated enough;
 *   the hint stays quiet (honest surface, no nagging).
 *
 * Plain object on purpose so it stays JVM-testable; the screen only renders
 * the returned boolean. The compass math and the facing banner are untouched.
 */
object QiblaCalibrationHintLogic {

    /**
     * Mirror of `android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM`
     * (a compile-time constant) so this decision needs no Android import and
     * reads truthfully in JVM tests.
     */
    const val SENSOR_STATUS_ACCURACY_MEDIUM = 2

    fun shouldShowHint(
        manualMode: Boolean,
        hasMagnetometer: Boolean,
        hasHeading: Boolean,
        accuracyInt: Int
    ): Boolean = !manualMode && hasMagnetometer && hasHeading && accuracyInt < SENSOR_STATUS_ACCURACY_MEDIUM
}
