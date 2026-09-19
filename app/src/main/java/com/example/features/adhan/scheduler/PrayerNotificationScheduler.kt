package com.example.features.adhan.scheduler

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalizationManager
import com.example.localization.Numerals
import com.example.features.settings.NourPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrayerNotificationScheduler {
    const val CHANNEL_PRAYER_ID = "nour_prayer_notifications"
    const val CHANNEL_COUNTDOWN_ID = "nour_live_countdown"

    const val ACTION_PRAYER_ALARM = "com.aistudio.nour.ACTION_PRAYER_ALARM"
    const val ACTION_MARK_PRAYED = "com.aistudio.nour.ACTION_MARK_PRAYED"
    const val ACTION_SNOOZE = "com.aistudio.nour.ACTION_SNOOZE"
    const val ACTION_DISMISS = "com.aistudio.nour.ACTION_DISMISS"

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

    /**
     * Schedules an exact alarm with the system AlarmManager.
     */
    fun scheduleExactAlarm(
        context: Context,
        prayerName: PrayerName,
        triggerAtMillis: Long,
        isPreReminder: Boolean = false,
        preMinutes: Int = 15
    ) {
        if (triggerAtMillis < System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_IS_PRE_REMINDER, isPreReminder)
            putExtra(EXTRA_PRE_MINUTES, preMinutes)
        }

        val requestCode = prayerName.ordinal + (if (isPreReminder) 100 else 0)
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
            // In Android 12+, SCHEDULE_EXACT_ALARM can require user setting if revoked
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * Cancels both the Adhan alarm and the pre-reminder alarm for one prayer.
     * PendingIntents match the scheduled ones (same action, component, request codes).
     */
    fun cancelPrayerAlarms(context: Context, prayerName: PrayerName) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        listOf(false to prayerName.ordinal, true to prayerName.ordinal + 100).forEach { (isPre, requestCode) ->
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

    /**
     * Schedules today's Adhan alerts and pre-Adhan reminders for every prayer,
     * honoring each prayer's saved switches (adhan on/off, reminder on/off + minutes).
     * Prayers without a saved row default to Adhan on, reminder off.
     */
    suspend fun scheduleDay(
        context: Context,
        times: com.example.features.prayer.models.PrayerTimes
    ) {
        val db = NourDatabase.getInstance(context)
        val now = System.currentTimeMillis()
        PrayerName.values().filter { it != PrayerName.SUNRISE }.forEach { p ->
            val settings = db.adhanSettingsDao().getSettingsForPrayer(p.name)
            if (settings == null || settings.isAdhanEnabled) {
                val atMillis = times.getTimeFor(p).time
                if (atMillis > now) {
                    scheduleExactAlarm(context, p, atMillis, isPreReminder = false)
                }
            }
            val preMinutes = (settings?.preReminderMinutes ?: 15).coerceIn(1, 120)
            if (settings?.isPreReminderEnabled == true) {
                val preMillis = times.getTimeFor(p).time - preMinutes * 60_000L
                if (preMillis > now) {
                    scheduleExactAlarm(context, p, preMillis, isPreReminder = true, preMinutes = preMinutes)
                }
            }
        }
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
        val eastern = com.example.features.settings.NourPreferences(context).numeralEastern
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

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Boot, timezone, and locale broadcasts share this receiver. They must
        // never deliver a prayer notification; silently reschedule instead.
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                rescheduleAfterSystemEvent(context)
                return
            }
        }

        val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val isPreReminder = intent.getBooleanExtra(PrayerNotificationScheduler.EXTRA_IS_PRE_REMINDER, false)
        val preMinutes = intent.getIntExtra(PrayerNotificationScheduler.EXTRA_PRE_MINUTES, 15).coerceIn(1, 120)
        val prayerName = try {
            PrayerName.valueOf(prayerNameString)
        } catch (e: Exception) {
            PrayerName.FAJR
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = PendingIntent.getActivity(
            context,
            prayerName.ordinal,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark as Prayed
        val prayedIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = PrayerNotificationScheduler.ACTION_MARK_PRAYED
            putExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME, prayerName.name)
        }
        val prayedPending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 200,
            prayedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 10 minutes
        val snoozeIntent = Intent(context, PrayerActionReceiver::class.java).apply {
            action = PrayerNotificationScheduler.ACTION_SNOOZE
            putExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME, prayerName.name)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 300,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val strings = AppStrings.forLanguage(LocalizationManager.getSavedLanguage(context))
        val prayerLocalized = when (prayerName) {
            com.example.features.prayer.models.PrayerName.FAJR -> strings.fajr
            com.example.features.prayer.models.PrayerName.SUNRISE -> strings.sunrise
            com.example.features.prayer.models.PrayerName.DHUHR -> strings.dhuhr
            com.example.features.prayer.models.PrayerName.ASR -> strings.asr
            com.example.features.prayer.models.PrayerName.MAGHRIB -> strings.maghrib
            com.example.features.prayer.models.PrayerName.ISHA -> strings.isha
        }
        val eastern = com.example.features.settings.NourPreferences(context).numeralEastern
        // Arabic script appears only in Arabic UI: otherwise the bare pattern
        // keeps notifications in the selected language ("Fajr", not "الفجر (الفجر)").
        val isArabicUi = LocalizationManager.getSavedLanguage(context) == com.example.localization.AppLanguage.ARABIC
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

        val builder = NotificationCompat.Builder(context, PrayerNotificationScheduler.CHANNEL_PRAYER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, strings.prayedAction, prayedPending)
            .addAction(android.R.drawable.ic_lock_idle_alarm, strings.snoozeAction, snoozePending)

        val notificationId = prayerName.ordinal + (if (isPreReminder) 50 else 10)
        notificationManager.notify(notificationId, builder.build())

        // Play Adhan Audio if it's the actual prayer time. goAsync keeps the
        // receiver alive for the DB read inside the ~10s broadcast window.
        if (!isPreReminder) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = NourDatabase.getInstance(context)
                    val settings = db.adhanSettingsDao().getSettingsForPrayer(prayerName.name)
                    if (settings == null || settings.isAdhanEnabled) {
                        AdhanPlayer.playAdhan(
                            context = context,
                            toneName = settings?.toneName ?: "Makkah Adhan",
                            customUriString = settings?.customUri,
                            volume = settings?.volume ?: 1.0f
                        )
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }

    /**
     * Recomputes and schedules today's remaining prayers from persisted
     * preferences after boot or a system time/timezone change.
     */
    private fun rescheduleAfterSystemEvent(context: Context) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = NourPreferences(context)
                val location = prefs.savedLocation
                    ?: UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)
                val times = PrayerCalculationEngine.calculate(
                    date = Date(),
                    location = location,
                    method = prefs.calculationMethod,
                    madhab = prefs.madhab,
                    highLatRule = prefs.highLatRule,
                    adjustments = prefs.adjustments
                )
                PrayerNotificationScheduler.scheduleDay(context, times)
            } finally {
                pending.finish()
            }
        }
    }
}

class PrayerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerNameString = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Stop any running Adhan playback
        AdhanPlayer.stop()

        when (intent.action) {
            PrayerNotificationScheduler.ACTION_MARK_PRAYED -> {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                CoroutineScope(Dispatchers.IO).launch {
                    val db = NourDatabase.getInstance(context)
                    db.prayerLogDao().insertOrUpdateLog(
                        PrayerLogEntity(
                            date = todayStr,
                            prayerName = prayerNameString,
                            status = "PRAYED",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                notificationManager.cancelAll()
            }
            PrayerNotificationScheduler.ACTION_SNOOZE -> {
                val prayerName = try { PrayerName.valueOf(prayerNameString) } catch (e: Exception) { PrayerName.FAJR }
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L)
                PrayerNotificationScheduler.scheduleExactAlarm(context, prayerName, snoozeTime, isPreReminder = false)
                notificationManager.cancelAll()
            }
            PrayerNotificationScheduler.ACTION_DISMISS -> {
                notificationManager.cancelAll()
            }
        }
    }
}
