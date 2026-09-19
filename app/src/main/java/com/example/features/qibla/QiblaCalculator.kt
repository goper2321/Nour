package com.example.features.qibla

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class QiblaData(
    val qiblaBearing: Float = 0f, // Great circle direction to Kaaba relative to True North (0-360°)
    val distanceKm: Double = 0.0, // Distance to Kaaba in kilometers
    val deviceHeading: Float = 0f, // Device azimuth relative to True North (0-360°)
    val magneticHeading: Float = 0f, // Raw magnetic heading before declination
    val declination: Float = 0f, // Magnetic declination at GPS coordinates
    val relativeAngle: Float = 0f, // Angle to rotate compass needle towards Kaaba (0-360°)
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val hasHeading: Boolean = false,
    val isFacingQibla: Boolean = false, // Within +/- 3 degrees of Kaaba
    val pitch: Float = 0f, // Phone pitch tilt in degrees
    val roll: Float = 0f, // Phone roll tilt in degrees
    val isDeviceFlat: Boolean = true, // Whether device is held sufficiently flat for optimal accuracy (< 35°)
    val sunAzimuth: Float = 0f, // Sun bearing for celestial cross-check
    val hasAccelerometer: Boolean = true,
    val hasMagnetometer: Boolean = true
)

object QiblaCalculator {
    const val KAABA_LAT = 21.422487
    const val KAABA_LNG = 39.826206

    /**
     * Calculates the great-circle forward azimuth bearing to the Kaaba from user coordinates.
     * Formula:
     * θ = atan2( sin(Δlong) * cos(lat2), cos(lat1) * sin(lat2) − sin(lat1) * cos(lat2) * cos(Δlong) )
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val deltaLngRad = Math.toRadians(KAABA_LNG - userLng)

        val y = sin(deltaLngRad) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLngRad)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360f) % 360f
        return bearing
    }

    /**
     * Calculates the great-circle distance to the Kaaba in kilometers using the Haversine formula.
     */
    fun calculateDistanceKm(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Earth mean radius in km
        val dLat = Math.toRadians(KAABA_LAT - userLat)
        val dLng = Math.toRadians(KAABA_LNG - userLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LAT)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Approximates current Sun azimuth for visual sun cross-check.
     */
    fun calculateSunAzimuth(userLat: Double, userLng: Double, date: Date = Date()): Float {
        val cal = Calendar.getInstance().apply { time = date }
        val hour = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0)
        val solarHourAngle = (hour - 12.0) * 15.0
        val approxSunAzimuth = (180.0 + solarHourAngle + (39.8 - userLng) * 0.25).toFloat()
        return (approxSunAzimuth + 360f) % 360f
    }
}

/**
 * Sensor Manager integrating the device Accelerometer and Magnetometer sensors.
 * Calculates True-North Qibla direction based on the user's current GPS location,
 * including low-pass smoothing and Geomagnetic Declination compensation.
 */
class QiblaSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _qiblaState = MutableStateFlow(
        QiblaData(
            hasAccelerometer = accelerometer != null,
            hasMagnetometer = (accelerometer != null && magnetometer != null) || rotationVectorSensor != null
        )
    )
    val qiblaState: StateFlow<QiblaData> = _qiblaState.asStateFlow()

    private var userLat: Double = QiblaCalculator.KAABA_LAT
    private var userLng: Double = QiblaCalculator.KAABA_LNG
    private var userAltitude: Double = 0.0

    // Sensor Fusion Buffers
    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Accelerometer & Magnetometer reading buffers with low-pass filtering
    private val gravityValues = FloatArray(3)
    private val geoMagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeoMagnetic = false
    private var magneticAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

    // Low-pass filter smoothing coefficient (alpha)
    private val alpha = 0.20f

    fun setLocation(lat: Double, lng: Double, altitude: Double = 0.0) {
        userLat = lat
        userLng = lng
        userAltitude = altitude
        updateCalculations(
            deviceHeading = _qiblaState.value.deviceHeading,
            rawMagneticHeading = _qiblaState.value.magneticHeading,
            pitch = _qiblaState.value.pitch,
            roll = _qiblaState.value.roll,
            accuracy = _qiblaState.value.accuracy
        )
    }

    fun startListening() {
        hasGravity = false
        hasGeoMagnetic = false
        magneticAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
        _qiblaState.value = _qiblaState.value.copy(hasHeading = false, accuracy = magneticAccuracy)
        // A rotation vector can supply heading when either raw sensor is missing.
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        if ((accelerometer == null || magnetometer == null) && rotationVectorSensor != null) {
            sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        var accuracy = event.accuracy

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Apply low-pass filter to smooth gravity vector
                gravityValues[0] = gravityValues[0] + alpha * (event.values[0] - gravityValues[0])
                gravityValues[1] = gravityValues[1] + alpha * (event.values[1] - gravityValues[1])
                gravityValues[2] = gravityValues[2] + alpha * (event.values[2] - gravityValues[2])
                hasGravity = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticAccuracy = event.accuracy
                // Apply low-pass filter to smooth magnetic vector
                geoMagneticValues[0] = geoMagneticValues[0] + alpha * (event.values[0] - geoMagneticValues[0])
                geoMagneticValues[1] = geoMagneticValues[1] + alpha * (event.values[1] - geoMagneticValues[1])
                geoMagneticValues[2] = geoMagneticValues[2] + alpha * (event.values[2] - geoMagneticValues[2])
                hasGeoMagnetic = true
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                if (!hasGravity || !hasGeoMagnetic) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    computeFromOrientation(orientationAngles, accuracy)
                    return
                }
            }
        }

        // When both Accelerometer & Magnetometer vectors are available, compute rotation matrix
        if (hasGravity && hasGeoMagnetic) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                inclinationMatrix,
                gravityValues,
                geoMagneticValues
            )

            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                computeFromOrientation(orientationAngles, magneticAccuracy)
            }
        }
    }

    private fun computeFromOrientation(angles: FloatArray, accuracy: Int) {
        // angles[0]: azimuth (rotation around -z axis in radians)
        // angles[1]: pitch (rotation around -x axis in radians)
        // angles[2]: roll (rotation around y axis in radians)
        val azimuthRad = angles[0]
        val pitchDeg = Math.toDegrees(angles[1].toDouble()).toFloat()
        val rollDeg = Math.toDegrees(angles[2].toDouble()).toFloat()

        var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
        azimuthDeg = (azimuthDeg + 360f) % 360f

        // Calculate Geomagnetic Declination at the user's GPS coordinates to obtain True North
        val declination = try {
            val geoField = GeomagneticField(
                userLat.toFloat(),
                userLng.toFloat(),
                userAltitude.toFloat(),
                System.currentTimeMillis()
            )
            geoField.declination
        } catch (_: Exception) {
            0f
        }

        val trueNorthHeading = (azimuthDeg + declination + 360f) % 360f
        if (!trueNorthHeading.isFinite() || !pitchDeg.isFinite() || !rollDeg.isFinite()) return
        _qiblaState.value = _qiblaState.value.copy(hasHeading = true)

        updateCalculations(
            deviceHeading = trueNorthHeading,
            rawMagneticHeading = azimuthDeg,
            pitch = pitchDeg,
            roll = rollDeg,
            accuracy = accuracy
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) magneticAccuracy = accuracy
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            _qiblaState.value = _qiblaState.value.copy(accuracy = accuracy)
        }
    }

    private fun updateCalculations(
        deviceHeading: Float,
        rawMagneticHeading: Float,
        pitch: Float,
        roll: Float,
        accuracy: Int
    ) {
        val qiblaBearing = QiblaCalculator.calculateQiblaBearing(userLat, userLng)
        val distanceKm = QiblaCalculator.calculateDistanceKm(userLat, userLng)
        val relativeAngle = (qiblaBearing - deviceHeading + 360f) % 360f
        val isFacingQibla = relativeAngle in 357f..360f || relativeAngle in 0f..3f
        val sunAzimuth = QiblaCalculator.calculateSunAzimuth(userLat, userLng)

        // Device is flat if pitch and roll tilt are within 35 degrees
        val isDeviceFlat = abs(pitch) < 35f && abs(roll) < 35f

        val declination = (deviceHeading - rawMagneticHeading + 540f) % 360f - 180f

        _qiblaState.value = QiblaData(
            qiblaBearing = qiblaBearing,
            distanceKm = distanceKm,
            deviceHeading = deviceHeading,
            magneticHeading = rawMagneticHeading,
            declination = declination,
            relativeAngle = relativeAngle,
            accuracy = accuracy,
            hasHeading = _qiblaState.value.hasHeading,
            isFacingQibla = isFacingQibla && _qiblaState.value.hasHeading &&
                accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && isDeviceFlat,
            pitch = pitch,
            roll = roll,
            isDeviceFlat = isDeviceFlat,
            sunAzimuth = sunAzimuth,
            hasAccelerometer = accelerometer != null,
            hasMagnetometer = (accelerometer != null && magnetometer != null) || rotationVectorSensor != null
        )
    }
}
