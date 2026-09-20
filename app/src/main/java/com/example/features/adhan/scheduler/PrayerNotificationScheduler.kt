package com.example.features.adhan.scheduler

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.features.settings.NourPreferences
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalizationManager
import com.example.localization.Numerals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerNotificationScheduler {
    private const val TAG = "PrayerNotificationScheduler"

    const val CHANNEL_PRAYER_ID = "nour_prayer_notifications"
    const val CHANNEL_COUNTDOWN_ID = "nour_live_countdown"

    const val ACTION_PRAYER_ALARM = "com.aistudio.nour.ACTION_PRAYER_ALARM"
    const val ACTION_MARK_PRAYED = "com.aistudio.nour.ACTION_MARK_PRAYED"
    const val ACTION_SNOOZE = "com.aistudio.nour.ACTION_SNOOZE"
    const val ACTION_DISMISS = "com.aistudio.nour.ACTION_DISMISS"
    const val ACTION_STOP_ADHAN = "com.aistudio.nour.ACTION_STOP_ADHAN"

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_IS_PRE_REMINDER = "extra_is_pre_reminder"
    const val EXTRA_PRE_MINUTES = "extra_pre_minutes"

    fun initChannels(context: Context) {
        val strings = AppStrings.forLanguage(LocalizationManager.getSavedLanguage(context))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                strings.channelPrayer,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = strings.channelPrayerDesc
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val countdownChannel = NotificationChannel(
                CHANNEL_COUNTDOWN_ID,
                strings.channelCountdown,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = strings.channelCountdownDesc
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(countdownChannel)
        }
    }

    fun getRequestCode(prayerName: PrayerName, isPreReminder: Boolean, dayOffset: Int): Int {
        val base = dayOffset * 100 + prayerName.ordinal
        return if (isPreReminder) base + 1000 else base
    }

    fun getNotificationId(prayerName: PrayerName, isPreReminder: Boolean): Int {
        return prayerName.ordinal + (if (isPreReminder) 50 else 10)
    }

    /**
     * Schedules an exact alarm with the system AlarmManager for a calculated prayer time.
     */
    fun scheduleExactAlarm(
        context: Context,
        prayerName: PrayerName,
        triggerAtMillis: Long,
        isPreReminder: Boolean = false,
        preMinutes: Int = 15,
        dayOffset: Int = 0,
        isSnooze: Boolean = false
    ) {
        if (triggerAtMillis < System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_IS_PRE_REMINDER, isPreReminder)
            putExtra(EXTRA_PRE_MINUTES, preMinutes)
        }

        val requestCode = if (isSnooze) {
            2000 + prayerName.ordinal
        } else {
            getRequestCode(prayerName, isPreReminder, dayOffset)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In Android 12+, if exact alarm permission is restricted, gracefully fallback
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule alarm", e2)
            }
        }
    }

    /**
     * Cancels both the Adhan alarm and the pre-reminder alarm for one prayer across all rolling window days.
     */
    fun cancelPrayerAlarms(context: Context, prayerName: PrayerName) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (dayOffset in 0..7) {
            listOf(false, true).forEach { isPre ->
                val requestCode = getRequestCode(prayerName, isPre, dayOffset)
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    /**
     * Cancels every scheduled prayer alarm and pre-reminder (all prayers,
     * rolling window days 0..7, plus snooze alarms). Used by the master
     * prayer-alarms switch so the user can fully silence the app.
     */
    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        PrayerName.values().forEach { prayer ->
            cancelPrayerAlarms(context, prayer)
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
            }
            val snoozeIntent = PendingIntent.getBroadcast(
                context,
                2000 + prayer.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(snoozeIntent)
            snoozeIntent.cancel()
        }
    }

    /**
     * Schedules a single day's Adhan alerts and pre-Adhan reminders for every prayer,
     * honoring each prayer's saved database preferences.
     */
    suspend fun scheduleDay(
        context: Context,
        times: com.example.features.prayer.models.PrayerTimes,
        dayOffset: Int = 0
    ) {
        if (!NourPreferences(context).alarmsEnabled) return
        val db = NourDatabase.getInstance(context)
        val now = System.currentTimeMillis()
        PrayerName.values().filter { it != PrayerName.SUNRISE }.forEach { p ->
            val settings = db.adhanSettingsDao().getSettingsForPrayer(p.name)
            if (settings == null || settings.isAdhanEnabled) {
                val atMillis = times.getTimeFor(p).time
                if (atMillis > now) {
                    scheduleExactAlarm(context, p, atMillis, isPreReminder = false, dayOffset = dayOffset)
                }
            }
            val preMinutes = (settings?.preReminderMinutes ?: 15).coerceIn(1, 120)
            if (settings?.isPreReminderEnabled == true) {
                val preMillis = times.getTimeFor(p).time - preMinutes * 60_000L
                if (preMillis > now) {
                    scheduleExactAlarm(
                        context,
                        p,
                        preMillis,
                        isPreReminder = true,
                        preMinutes = preMinutes,
                        dayOffset = dayOffset
                    )
                }
            }
        }
    }

    /**
     * Calculates prayer times offline and schedules a rolling multi-day window of alarms (default 7 days).
     * This guarantees that exact alarms will trigger accurately offline even without any network connectivity
     * or without opening the app for days.
     */
    suspend fun scheduleOfflineRollingWindow(context: Context, daysAhead: Int = 7) {
        val prefs = NourPreferences(context)
        if (!prefs.alarmsEnabled) return
        val location = prefs.savedLocation
            ?: UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)
        val method = prefs.calculationMethod
        val madhab = prefs.madhab
        val highLatRule = prefs.highLatRule
        val adjustments = prefs.adjustments

        val calendar = Calendar.getInstance()
        for (dayOffset in 0 until daysAhead) {
            val dateForDay = calendar.time
            val times = PrayerCalculationEngine.calculate(
                date = dateForDay,
                location = location,
                method = method,
                madhab = madhab,
                highLatRule = highLatRule,
                adjustments = adjustments
            )
            scheduleDay(context, times, dayOffset = dayOffset)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    /**
     * Builds a rich, accessible Material 3 prayer alert notification.
     */
    fun buildPrayerNotification(
        context: Context,
        prayerName: PrayerName,
        isPreReminder: Boolean,
        preMinutes: Int,
        isPlayingAdhan: Boolean
    ): Notification {
        val strings = AppStrings.forLanguage(LocalizationManager.getSavedLanguage(context))
        val prayerLocalized = when (prayerName) {
            PrayerName.FAJR -> strings.fajr
            PrayerName.SUNRISE -> strings.sunrise
            PrayerName.DHUHR -> strings.dhuhr
            PrayerName.ASR -> strings.asr
            PrayerName.MAGHRIB -> strings.maghrib
            PrayerName.ISHA -> strings.isha
        }
        val eastern = NourPreferences(context).numeralEastern
        val isArabicUi = LocalizationManager.getSavedLanguage(context) == AppLanguage.ARABIC
        val title = Numerals.convert(
            if (isPreReminder) {
                String.format(strings.reminderTitlePattern, prayerLocalized, preMinutes)
            } else if (isArabicUi) {
                String.format(strings.adhanTitlePattern, prayerLocalized, prayerName.arabicName)
            } else {
                String.format(strings.adhanTitleBarePattern, prayerLocalized)
            }, eastern
        )

        val body = Numerals.convert(
            if (isPreReminder) {
                strings.reminderBody
            } else {
                String.format(strings.adhanBodyPattern, prayerLocalized)
            }, eastern
        )

        val openIntent = PendingIntent.getActivity(
            context,
            prayerName.ordinal,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark as Prayed
        val prayedIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = ACTION_MARK_PRAYED
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
        }
        val prayedPending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 200,
            prayedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 10 minutes
        val snoozeIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 300,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Intent
        val dismissIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
        }
        val dismissPending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 400,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openIntent)
            .setDeleteIntent(dismissPending)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, strings.prayedAction, prayedPending)
            .addAction(android.R.drawable.ic_lock_idle_alarm, strings.snoozeAction, snoozePending)

        if (isPlayingAdhan) {
            val stopIntent = Intent(context, PrayerActionReceiver::class.java).apply {
                action = ACTION_STOP_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            }
            val stopPending = PendingIntent.getBroadcast(
                context,
                prayerName.ordinal + 500,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, strings.stopAudio, stopPending)
        }

        return builder.build()
    }

    /**
     * Updates or creates the live countdown status notification.
     */
    fun updateLiveCountdownNotification(
        context: Context,
        nextPrayerName: String,
        remainingFormatted: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val openAppIntent = PendingIntent.getActivity(
            context,
            999,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val strings = AppStrings.forLanguage(LocalizationManager.getSavedLanguage(context))
        val eastern = NourPreferences(context).numeralEastern
        val notification = NotificationCompat.Builder(context, CHANNEL_COUNTDOWN_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(Numerals.convert(String.format(strings.nextPrayerPattern, nextPrayerName), eastern))
            .setContentText(Numerals.convert(String.format(strings.countdownPattern, remainingFormatted), eastern))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}

/**
 * Handles AlarmManager alarms and system boot/time updates to launch the prayer alarm service
 * or reschedule offline calculations.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.intent.action.TIME_SET",
            Intent.ACTION_DATE_CHANGED -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        PrayerNotificationScheduler.scheduleOfflineRollingWindow(context)
                    } catch (e: Exception) {
                        Log.e("PrayerAlarmReceiver", "Error recalculating offline prayer schedule", e)
                    } finally {
                        pending.finish()
                    }
                }
                return
            }

            PrayerNotificationScheduler.ACTION_PRAYER_ALARM -> {
                val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
                val isPreReminder = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_PRE_REMINDER, false)
                val preMinutes = intent.getIntExtra(PrayerNotificationScheduler.EXTRA_PRE_MINUTES, 15).coerceIn(1, 120)

                val serviceIntent = Intent(context, PrayerAlarmService::class.java).apply {
                    this.action = PrayerAlarmService.ACTION_TRIGGER_PRAYER
                    putExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME, prayerNameString)
                    putExtra(PrayerNotificationScheduler.EXTRA_IS_PRE_REMINDER, isPreReminder)
                    putExtra(PrayerNotificationScheduler.EXTRA_PRE_MINUTES, preMinutes)
                }

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.e("PrayerAlarmReceiver", "Failed to start PrayerAlarmService, falling back to direct notification", e)
                    val prayerName = try { PrayerName.valueOf(prayerNameString) } catch (ex: Exception) { PrayerName.FAJR }
                    val notification = PrayerNotificationScheduler.buildPrayerNotification(
                        context, prayerName, isPreReminder, preMinutes, isPlayingAdhan = false
                    )
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(PrayerNotificationScheduler.getNotificationId(prayerName, isPreReminder), notification)
                }
            }
        }
    }
}

/**
 * Handles actions from the prayer notification: Mark as Prayed, Snooze, Dismiss, Stop Adhan.
 */
class PrayerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val prayerName = try { PrayerName.valueOf(prayerNameString) } catch (e: Exception) { PrayerName.FAJR }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Stop any running Adhan audio playback
        AdhanPlayer.stop()

        // Inform the PrayerAlarmService to stop if running
        try {
            val stopIntent = Intent(context, PrayerAlarmService::class.java).apply {
                action = PrayerAlarmService.ACTION_STOP_ADHAN
            }
            context.startService(stopIntent)
        } catch (_: Exception) {}

        when (intent.action) {
            PrayerNotificationScheduler.ACTION_MARK_PRAYED -> {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = NourDatabase.getInstance(context)
                        db.prayerLogDao().insertOrUpdateLog(
                            PrayerLogEntity(
                                date = todayStr,
                                prayerName = prayerNameString,
                                status = "PRAYED",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("PrayerActionReceiver", "Failed to log prayer", e)
                    } finally {
                        pending.finish()
                    }
                }
                notificationManager.cancel(PrayerNotificationScheduler.getNotificationId(prayerName, false))
                notificationManager.cancel(PrayerNotificationScheduler.getNotificationId(prayerName, true))
            }
            PrayerNotificationScheduler.ACTION_SNOOZE -> {
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L)
                PrayerNotificationScheduler.scheduleExactAlarm(
                    context, prayerName, snoozeTime, isPreReminder = false, dayOffset = 0, isSnooze = true
                )
                notificationManager.cancel(PrayerNotificationScheduler.getNotificationId(prayerName, false))
                notificationManager.cancel(PrayerNotificationScheduler.getNotificationId(prayerName, true))
            }
            PrayerNotificationScheduler.ACTION_STOP_ADHAN,
            PrayerNotificationScheduler.ACTION_DISMISS -> {
                notificationManager.cancel(PrayerNotificationScheduler.getNotificationId(prayerName, false))
                notificationManager.cancel(PrayerNotificationScheduler.getNotificationId(prayerName, true))
            }
        }
    }
}

