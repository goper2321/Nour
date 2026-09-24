package com.example.features.location

import com.example.ui.theme.NourSpace
import com.example.ui.theme.NourSize
import com.example.ui.theme.DialogExpressiveShape
import com.example.ui.theme.SheetExpressiveShape
import com.example.ui.components.NourScreenHeading
import com.example.ui.components.NourEmptyState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.prayer.models.UserLocation
import com.example.localization.LocalAppStrings
import com.example.localization.formatNumerals
import com.example.ui.theme.CardExpressiveShape
import com.example.ui.theme.PillShape
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

/**
 * Accompanist Permissions integration for Fine & Coarse Location.
 * Powers 100% offline, on-device prayer times calculation and Qibla calibration.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionState(): MultiplePermissionsState {
    return rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}

/**
 * Location Helper that fetches current device GPS location using FusedLocationProviderClient
 * with graceful fallback to Android system LocationManager (e.g. for offline or non-GMS devices).
 */
object DeviceLocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.IO) {
        try {
            // First try Google Play Services FusedLocationProviderClient
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            var fetchedLoc: Location? = null
            try {
                val task = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                val completed = com.google.android.gms.tasks.Tasks.await(task, 12, java.util.concurrent.TimeUnit.SECONDS)
                if (completed != null) {
                    fetchedLoc = completed
                } else {
                    // Fallback to last known location
                    val lastTask = fusedClient.lastLocation
                    fetchedLoc = com.google.android.gms.tasks.Tasks.await(lastTask, 5, java.util.concurrent.TimeUnit.SECONDS)
                }
            } catch (_: Exception) {
                // GMS unavailable, timed out, or last-known failed — try platform LocationManager.
            }

            if (fetchedLoc != null) return@withContext fetchedLoc

            // Fallback to Android standard LocationManager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val passiveLoc = locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            gpsLoc ?: netLoc ?: passiveLoc
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolves city name, country, and timezone offset using Geocoder and device timezone.
     */
    suspend fun resolveUserLocation(context: Context, location: Location): UserLocation = withContext(Dispatchers.IO) {
        val lat = location.latitude
        val lng = location.longitude
        var cityName = String.format(Locale.US, "%.3f°, %.3f°", lat, lng)
        var countryName = "Detected Coordinates"

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: cityName
                    countryName = addr.countryName ?: countryName
                }
            }
        } catch (_: Exception) {
            // Geocoder unavailable in some offline environments
        }

        val tz = TimeZone.getDefault()
        val offsetHours = tz.getOffset(System.currentTimeMillis()) / (1000.0 * 3600.0)

        UserLocation(
            latitude = lat,
            longitude = lng,
            cityName = cityName,
            countryName = countryName,
            timezoneOffsetHours = offsetHours,
            isAutoDetected = true
        )
    }
}

/**
 * Reusable Accompanist Location Permission Banner & Action Card.
 * Adheres strictly to M3 Expressive design tokens.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionCard(
    currentLocation: UserLocation,
    onLocationDetected: (UserLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val permissionState = rememberLocationPermissionState()
    val strings = LocalAppStrings.current
    var isDetecting by remember { mutableStateOf(false) }
    var detectionFailed by remember { mutableStateOf(false) }

    fun detectGps() {
        if (!isDetecting) {
            isDetecting = true
            detectionFailed = false
            coroutineScope.launch {
                val loc = DeviceLocationHelper.getCurrentLocation(context)
                if (loc != null) {
                    val userLoc = DeviceLocationHelper.resolveUserLocation(context, loc)
                    onLocationDetected(userLoc)
                } else {
                    detectionFailed = true
                }
                isDetecting = false
            }
        }
    }

    // When permission is newly granted, trigger automatic location retrieval
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted && !currentLocation.isAutoDetected) {
            detectGps()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_permission_card"),
        shape = CardExpressiveShape,
        colors = CardDefaults.cardColors(
            containerColor = if (permissionState.allPermissionsGranted)
                MaterialTheme.colorScheme.surfaceContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NourSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NourSpace.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NourSpace.md)
                ) {
                    Surface(
                        color = if (permissionState.allPermissionsGranted)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        shape = PillShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (permissionState.allPermissionsGranted)
                                    Icons.Default.LocationOn
                                else
                                    Icons.Rounded.LocationSearching,
                                contentDescription = null,
                                tint = if (permissionState.allPermissionsGranted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (permissionState.allPermissionsGranted)
                                strings.gpsActive
                            else
                                strings.locationPermissionRequired,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatNumerals(
                                if (currentLocation.isAutoDetected)
                                    "${currentLocation.cityName} • ${String.format(Locale.US, "%.2f°, %.2f°", currentLocation.latitude, currentLocation.longitude)}"
                                else
                                    "${strings.defaultLocationPrefix}: ${currentLocation.cityName}"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                if (permissionState.allPermissionsGranted) {
                    IconButton(
                        onClick = { detectGps() },
                        enabled = !isDetecting,
                        modifier = Modifier.testTag("refresh_gps_button")
                    ) {
                        if (isDetecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = strings.refreshGps,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (detectionFailed) {
                Text(strings.unableToLocate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error)
            }
            if (!permissionState.allPermissionsGranted) {
                Text(
                    text = strings.permissionRationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { permissionState.launchMultiplePermissionRequest() },
                        shape = PillShape,
                        modifier = Modifier
                            .testTag("request_location_permission_button")
                            .heightIn(min = 44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.grantPermission)
                    }
                }
            }
        }
    }
}
