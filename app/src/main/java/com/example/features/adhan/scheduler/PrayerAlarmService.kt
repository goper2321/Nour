package com.example.features.adhan.scheduler

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.database.NourDatabase
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.adhan.player.AdhanTones
import com.example.features.prayer.models.PrayerName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Background and Foreground service triggered by AlarmManager at calculated prayer times.
 *
 * Responsibilities:
 * - Promotes to Foreground Service to post immediate high-priority prayer notifications.
 * - Plays the selected Adhan audio (bundled recording or synthesized tone) with wake lock protection.
 * - Reschedules upcoming offline prayer alarms so notifications trigger reliably offline without internet.
 * - Handles notification actions (Mark as Prayed, Snooze, Dismiss, Stop Audio).
 */
class PrayerAlarmService : Service() {

    companion object {
        private const val TAG = "PrayerAlarmService"
        const val ACTION_TRIGGER_PRAYER = "com.aistudio.nour.ACTION_TRIGGER_PRAYER"
        const val ACTION_STOP_ADHAN = "com.aistudio.nour.ACTION_STOP_ADHAN"
        const val ACTION_RESCHEDULE_OFFLINE = "com.aistudio.nour.ACTION_RESCHEDULE_OFFLINE"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wakeLock: PowerManager.WakeLock? = null
    private var isForegroundActive = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PrayerNotificationScheduler.initChannels(this)
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_TRIGGER_PRAYER -> {
                val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
                val isPreReminder = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_PRE_REMINDER, false)
                val preMinutes = intent.getIntExtra(PrayerNotificationScheduler.EXTRA_PRE_MINUTES, 15).coerceIn(1, 120)
                val prayerName = try {
                    PrayerName.valueOf(prayerNameString)
                } catch (e: Exception) {
                    PrayerName.FAJR
                }

                val notificationId = PrayerNotificationScheduler.getNotificationId(prayerName, isPreReminder)

                // 1. Immediately promote to foreground notification to fulfill OS requirements within 5 seconds
                val initialNotification = PrayerNotificationScheduler.buildPrayerNotification(
                    context = this,
                    prayerName = prayerName,
                    isPreReminder = isPreReminder,
                    preMinutes = preMinutes,
                    isPlayingAdhan = !isPreReminder
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            notificationId,
                            initialNotification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(notificationId, initialNotification)
                    }
                    isForegroundActive = true
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting foreground service", e)
                }

                // 2. Play Adhan audio if this is the actual prayer time (not a pre-reminder)
                if (!isPreReminder) {
                    serviceScope.launch(Dispatchers.IO) {
                        try {
                            val db = NourDatabase.getInstance(applicationContext)
                            val settings = db.adhanSettingsDao().getSettingsForPrayer(prayerName.name)
                            val shouldPlay = settings == null || settings.isAdhanEnabled
                            if (shouldPlay) {
                                val tone = settings?.toneName ?: AdhanTones.MAKKAH
                                val customUri = settings?.customUri
                                val volume = settings?.volume ?: 1.0f

                                AdhanPlayer.playAdhan(
                                    context = applicationContext,
                                    toneName = tone,
                                    customUriString = customUri,
                                    volume = volume,
                                    onCompletion = {
                                        stopServiceAfterPlayback(notificationId, prayerName, isPreReminder, preMinutes)
                                    }
                                )
                            } else {
                                stopServiceAfterPlayback(notificationId, prayerName, isPreReminder, preMinutes)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error checking Adhan settings or playing audio", e)
                            stopServiceAfterPlayback(notificationId, prayerName, isPreReminder, preMinutes)
                        }
                    }
                } else {
                    // Pre-reminders do not play full audio, dismiss service while preserving the notification
                    stopServiceAfterPlayback(notificationId, prayerName, isPreReminder, preMinutes)
                }

                // 3. Roll forward offline schedule window in background to ensure ongoing offline reliability
                serviceScope.launch(Dispatchers.IO) {
                    try {
                        PrayerNotificationScheduler.scheduleOfflineRollingWindow(applicationContext)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error refreshing offline prayer window", e)
                    }
                }
            }

            ACTION_STOP_ADHAN -> {
                AdhanPlayer.stop()
                stopForegroundAndSelf()
            }

            ACTION_RESCHEDULE_OFFLINE -> {
                serviceScope.launch(Dispatchers.IO) {
                    try {
                        PrayerNotificationScheduler.scheduleOfflineRollingWindow(applicationContext)
                    } finally {
                        stopSelf(startId)
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun stopServiceAfterPlayback(
        notificationId: Int,
        prayerName: PrayerName,
        isPreReminder: Boolean,
        preMinutes: Int
    ) {
        // Update notification so "Stop Adhan" action is removed and it remains dismissible in tray
        try {
            val finalNotification = PrayerNotificationScheduler.buildPrayerNotification(
                context = this,
                prayerName = prayerName,
                isPreReminder = isPreReminder,
                preMinutes = preMinutes,
                isPlayingAdhan = false
            )
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.notify(notificationId, finalNotification)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating final notification", e)
        }

        stopForegroundAndSelf()
    }

    private fun stopForegroundAndSelf() {
        try {
            if (isForegroundActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(false)
                }
                isForegroundActive = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground", e)
        }
        releaseWakeLock()
        stopSelf()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Nour:PrayerAlarmServiceWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(3 * 60 * 1000L) // 3 minutes maximum timeout
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock", e)
        }
    }

    override fun onDestroy() {
        AdhanPlayer.stop()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }
}
