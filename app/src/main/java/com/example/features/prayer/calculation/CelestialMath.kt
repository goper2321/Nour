package com.example.features.prayer.calculation

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Compact low-precision astronomy for the live sky background.
 *
 * Computes true Sun altitude/azimuth and Moon altitude/azimuth/phase from the
 * device date, latitude and longitude (standard solar theory plus a truncated
 * lunar theory, good to ~1-2 degrees — far beyond what a stylized backdrop
 * needs). Pure math, no network, no dependencies.
 */
object CelestialMath {

    /** Horizontal coordinates: altitude above horizon, azimuth from north. */
    data class BodyPosition(
        val altitudeDeg: Double,
        val azimuthDeg: Double,
        /** Hour angle in degrees (-180..180); negative before transit. */
        val hourAngleDeg: Double
    )

    data class MoonInfo(
        val position: BodyPosition,
        /** Illuminated fraction 0 (new) .. 1 (full). */
        val illumination: Double,
        /** 0 (new) .. 1 (next new). */
        val phase01: Double,
        val waxing: Boolean
    )

    private const val DEG = Math.PI / 180.0

    private fun norm360(x: Double): Double {
        var v = x % 360.0
        if (v < 0) v += 360.0
        return v
    }

    private fun norm180(x: Double): Double {
        val v = norm360(x)
        return if (v > 180.0) v - 360.0 else v
    }

    private fun julianDay(millis: Long): Double = millis / 86400000.0 + 2440587.5

    /** Sun's apparent ecliptic longitude in degrees. */
    private fun sunEclipticLongitude(d: Double): Double {
        val meanLongitude = norm360(280.460 + 0.9856474 * d)
        val meanAnomaly = norm360(357.528 + 0.9856003 * d) * DEG
        return norm360(
            meanLongitude + 1.915 * sin(meanAnomaly) + 0.020 * sin(2.0 * meanAnomaly)
        )
    }

    /** Moon ecliptic longitude/latitude plus lunation phase 0..1. */
    private fun moonEcliptic(d: Double, sunLongitude: Double): Triple<Double, Double, Double> {
        val meanLongitude = norm360(218.316 + 13.176396 * d)
        val meanAnomaly = norm360(134.963 + 13.064993 * d) * DEG
        val argLat = norm360(93.272 + 13.229350 * d) * DEG
        val meanElongation = norm360(meanLongitude - sunLongitude) * DEG
        val sunAnomaly = norm360(357.528 + 0.9856003 * d) * DEG
        val lambda = norm360(
            meanLongitude +
                6.289 * sin(meanAnomaly) +
                1.274 * sin(2.0 * meanElongation - meanAnomaly) +
                0.658 * sin(2.0 * meanElongation) +
                0.214 * sin(2.0 * meanAnomaly) -
                0.186 * sin(sunAnomaly)
        )
        val beta = 5.128 * sin(argLat) + 0.281 * sin(meanAnomaly + argLat)
        return Triple(lambda, beta, norm360(meanLongitude - sunLongitude) / 360.0)
    }

    /** Ecliptic (lambda, beta) to equatorial (RA, Dec), all degrees. */
    private fun equatorial(lambdaDeg: Double, betaDeg: Double, d: Double): Pair<Double, Double> {
        val obliquity = (23.439 - 0.0000004 * d) * DEG
        val lam = lambdaDeg * DEG
        val bet = betaDeg * DEG
        val ra = atan2(
            sin(lam) * cos(obliquity) - tan(bet) * sin(obliquity),
            cos(lam)
        ) / DEG
        val dec = asin(
            sin(bet) * cos(obliquity) + cos(bet) * sin(obliquity) * sin(lam)
        ) / DEG
        return norm360(ra) to dec
    }

    private fun localSiderealDeg(julianDay: Double, longitude: Double): Double {
        val gmst = norm360(280.46061837 + 360.98564736629 * (julianDay - 2451545.0))
        return norm360(gmst + longitude)
    }

    private fun horizontal(
        raDeg: Double,
        decDeg: Double,
        latitude: Double,
        lstDeg: Double
    ): BodyPosition {
        val hourAngle = norm180(lstDeg - raDeg)
        val latRad = latitude * DEG
        val decRad = decDeg * DEG
        val hourRad = hourAngle * DEG
        val altitude = asin(
            sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(hourRad)
        ) / DEG
        val azimuth = norm360(
            atan2(
                sin(hourRad),
                cos(hourRad) * sin(latRad) - tan(decRad) * cos(latRad)
            ) / DEG + 180.0
        )
        return BodyPosition(altitude, azimuth, hourAngle)
    }

    fun sunPosition(dateMillis: Long, latitude: Double, longitude: Double): BodyPosition {
        val jd = julianDay(dateMillis)
        val d = jd - 2451545.0
        val (ra, dec) = equatorial(sunEclipticLongitude(d), 0.0, d)
        return horizontal(ra, dec, latitude, localSiderealDeg(jd, longitude))
    }

    fun moonInfo(dateMillis: Long, latitude: Double, longitude: Double): MoonInfo {
        val jd = julianDay(dateMillis)
        val d = jd - 2451545.0
        val sunLongitude = sunEclipticLongitude(d)
        val (moonLongitude, moonLatitude, phase01) = moonEcliptic(d, sunLongitude)
        val (ra, dec) = equatorial(moonLongitude, moonLatitude, d)
        val position = horizontal(ra, dec, latitude, localSiderealDeg(jd, longitude))
        val elongationRad = norm180(moonLongitude - sunLongitude) * DEG
        val illumination = ((1.0 - cos(elongationRad)) / 2.0).coerceIn(0.0, 1.0)
        return MoonInfo(position, illumination, phase01, phase01 < 0.5)
    }
}
