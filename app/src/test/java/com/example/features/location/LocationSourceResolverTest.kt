package com.example.features.location

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * NUR-030: the prescribed location source strategy,
 * fused → LocationManager.GPS_PROVIDER → NETWORK_PROVIDER → manual entry,
 * decided purely from device facts.
 */
class LocationSourceResolverTest {

    private fun select(
        play: Boolean,
        fusedFailed: Boolean,
        gps: Boolean,
        network: Boolean
    ): LocationSourceKind =
        LocationSourceResolver.select(
            LocationSourceResolver.Facts(
                playServicesAvailable = play,
                fusedFailed = fusedFailed,
                gpsEnabled = gps,
                networkEnabled = network
            )
        )

    @Test
    fun healthyGmsWithFusedFixWins() {
        assertEquals(LocationSourceKind.FUSED, select(play = true, fusedFailed = false, gps = true, network = true))
    }

    @Test
    fun missingGmsFallsToPlatformGps() {
        // GrapheneOS/CalyxOS: no GMS at all.
        assertEquals(LocationSourceKind.PLATFORM_GPS, select(play = false, fusedFailed = false, gps = true, network = true))
    }

    @Test
    fun fusedErrorOnGmsDeviceFallsToPlatformGps() {
        // GMS present but the fused call errored, timed out, or returned no fix.
        assertEquals(LocationSourceKind.PLATFORM_GPS, select(play = true, fusedFailed = true, gps = true, network = true))
    }

    @Test
    fun disabledGpsFallsToNetworkProvider() {
        assertEquals(LocationSourceKind.PLATFORM_NETWORK, select(play = false, fusedFailed = false, gps = false, network = true))
    }

    @Test
    fun networkOnlyDevicesStillResolve() {
        assertEquals(LocationSourceKind.PLATFORM_NETWORK, select(play = false, fusedFailed = false, gps = false, network = true))
    }

    @Test
    fun everySourceUnavailableMeansManualEntry() {
        assertEquals(LocationSourceKind.MANUAL, select(play = false, fusedFailed = false, gps = false, network = false))
    }
}
