# Sensor and location honesty — trace (NUR-028)

Trace of every component that can be missing, disabled, or failing, from the
platform API all the way to Compose state, with the honest state each failure
lands in. The Qibla mathematics in `QiblaCalculator` are untouched.

## 1. Qibla sensors (`com.example.features.qibla.QiblaSensorManager`)

| Sensor / condition | Missing path | Behavior | Implemented honest state |
|---|---|---|---|
| Magnetometer absent (default = null) | `getDefaultSensor(TYPE_MAGNETIC_FIELD)` null | Initial `QiblaData.hasMagnetometer = false`; heading can never compute | Screen status `qiblaNoSensor` ("Heading sensors unavailable…"); app **starts in manual mode** (`manualMode = !hasMagnetometer`, QiblaScreen) |
| Accelerometer absent | `getDefaultSensor(TYPE_ACCELEROMETER)` null | Initial `QiblaData.hasAccelerometer = false` | Same `qiblaNoSensor` branch (heading requires both raw sensors on non-rotation devices) |
| Rotation vector absent (older device) | `getDefaultSensor(TYPE_ROTATION_VECTOR)` null | Falls back to raw accel+magnetometer fusion path — by design, not a failure | n/a (normal device capability split) |
| **`registerListener` refused** (sensor disabled, transient system refusal) — **was silent** | `startListening()` previously ignored the Boolean return; no events ever arrive | Screen froze at `qiblaWaiting` forever ("waiting for a heading reading") — the frozen-needle symptom NUR-028 describes | `startListening()` now records whether every heading-capable registration was accepted into `QiblaData.registrationFailed`; the screen adds the branch `qiblaState.registrationFailed → strings.compassUnavailable` between "no sensor" and "waiting" |
| Registration replaced by StopListening | onDispose → `unregisterListener` | Next entry re-registers; `registrationFailed` resets to false at the start of every `startListening()` | Re-attempt stays honest per session |

Manual Qibla slider continuity: the manual reference lives entirely in screen
state (`manualHeading`, `effectiveHeading = if (manualMode) manualHeading else …`
in QiblaScreen) and never depends on `hasHeading` or sensor events, so the
slider keeps working when the compass cannot lock. When leading manual mode,
the accuracy chip shows `accuracyManual`.

## 2. Facing-the-Kaaba banner (honesty principle)

The banner can only appear from real, calibrated sensor readings — the screen
computes `isFacingQibla = !manualMode && qiblaState.hasHeading &&
qiblaState.hasMagnetometer && accuracy ≥ SENSOR_STATUS_ACCURACY_MEDIUM &&
isDeviceFlat && angle'Q`. In manual mode the banner is structurally
impossible (the `"facing the Kaaba" banner must appear only from real
calibrated sensor readings`, per repo rules).

## 3. Location (`features/location`)

| Component | Missing path | Behavior | Implemented honest state |
|---|---|---|---|
| Google Play Services missing (GrapheneOS/CalyxOS) | `GoogleApiAvailability.isGooglePlayServicesAvailable != SUCCESS` (NUR-030 probe added) | The fused provider is now never contacted; decision falls to the platform chain | Resolver picks `PLATFORM_GPS/NETWORK`; `DeviceLocationHelper.Attempt.source` reports it; the location card title shows `pureDeviceGps` ("Using pure device GPS") |
| Fused call errors / times out (12 s + 5 s awaits) | `Tasks.await` throws or null | Caught; previously fell through to last-known lookups silently | Caught and folded into `fusedFailed = location == null`; resolver picks the prescribed platform chain |
| GPS provider disabled / no fix | `isProviderEnabled(GPS_PROVIDER) == false` | Previously passive provider might quietly serve | Resolver: `PLATFORM_NETWORK` next per the chain; fix null → `MANUAL` |
| NETWORK provider missing/disabled | same | — | Resolver: `MANUAL` |
| No automatic source at all | all of the above | — | `Attempt(MANUAL)`; permission-state UI surfaces honest failure: the card shows `unableToLocate` + "Open location settings"; the SetupWizard shows its `locationError` state; MainActivity toasts `unableGps`. Manual city entry exists and is proven: Settings → Select City dialog (holy cities + searchable world list) and the onboarding wizard's city picker |
| Any unexpected throw inside the helper | — | Outer catches degrade to `null`, never propagate | n/a (never throws, by design) |
| Location permissions denied | Accompanist state | Rationale card + grant CTA; no GPS call is made | Unchanged (NUR-030 explicitly leaves the permission flow identical) |

## 4. Decision coverage

The source decision itself is pure JVM
(`LocationSourceResolver.select(Facts)` + `LocationSourceResolverTest`),
covering: healthy-GMS/fused-wins, missing GMS, fused error on a GMS device,
GPS→NETWORK fallback, and the manual-entry terminal state.
