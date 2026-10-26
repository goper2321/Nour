package com.example.features.location

/**
 * NUR-030: where a location fix may come from, in the order the app trusts it.
 *
 * Google Play Services' FusedLocationProviderClient lives in GMS; phones
 * running GrapheneOS/CalyxOS or other de-Googled ROMs may have no GMS at all.
 * The Android platform's own LocationManager providers remain fully on-device
 * with no INTERNET permission, falling back to the manual city-entry presets
 * when even those produce nothing.
 */
enum class LocationSourceKind {
    /** Google Play Services fused provider (GMS present and healthy). */
    FUSED,

    /** Android platform LocationManager GPS_PROVIDER. */
    PLATFORM_GPS,

    /** Android platform LocationManager NETWORK_PROVIDER. */
    PLATFORM_NETWORK,

    /** No automatic source produced a fix — user picks their city manually. */
    MANUAL
}

/**
 * Pure source-selection logic, deliberately free of Android types so the
 * prescribed strategy (fused → GPS → NETWORK → manual entry) can be unit
 * tested on the JVM from synthetic facts.
 */
object LocationSourceResolver {

    /** What the runtime learned about the device's location sources. */
    data class Facts(
        /** `isGooglePlayServicesAvailable == ConnectionResult.SUCCESS`. */
        val playServicesAvailable: Boolean,
        /** GMS missing, the fused call errored/timed out, or returned no fix. */
        val fusedFailed: Boolean,
        /** `LocationManager.isProviderEnabled(GPS_PROVIDER)`. */
        val gpsEnabled: Boolean,
        /** `LocationManager.isProviderEnabled(NETWORK_PROVIDER)`. */
        val networkEnabled: Boolean
    )

    /**
     * Returns the source the fix should be attributed to. Fused wins when GMS
     * is healthy and the call did not fail; otherwise the prescribed platform
     * fallback chain applies (GPS_PROVIDER, then NETWORK_PROVIDER, then the
     * manual city picker).
     */
    fun select(facts: Facts): LocationSourceKind = when {
        facts.playServicesAvailable && !facts.fusedFailed -> LocationSourceKind.FUSED
        facts.gpsEnabled -> LocationSourceKind.PLATFORM_GPS
        facts.networkEnabled -> LocationSourceKind.PLATFORM_NETWORK
        else -> LocationSourceKind.MANUAL
    }
}
