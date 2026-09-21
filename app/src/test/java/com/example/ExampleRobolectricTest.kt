package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.features.qibla.QiblaCalculator
import com.example.features.quran.data.QuranRepository
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nour", appName)
    }

    @Test
    fun `prayer calculation engine returns valid chronological prayer times`() {
        val makkah = UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0)
        val times = PrayerCalculationEngine.calculate(
            date = Date(),
            location = makkah,
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI_MALIKI_HANBALI,
            highLatRule = HighLatitudeRule.ANGLE_BASED,
            adjustments = PrayerAdjustments()
        )

        assertNotNull(times.fajr)
        assertNotNull(times.sunrise)
        assertNotNull(times.dhuhr)
        assertNotNull(times.asr)
        assertNotNull(times.maghrib)
        assertNotNull(times.isha)

        assertTrue(times.fajr.before(times.sunrise))
        assertTrue(times.sunrise.before(times.dhuhr))
        assertTrue(times.dhuhr.before(times.asr))
        assertTrue(times.asr.before(times.maghrib))
        assertTrue(times.maghrib.before(times.isha))
    }

    @Test
    fun `qibla calculator computes accurate direction and distance to Kaaba`() {
        // Cairo: ~30°N, 31°E -> Qibla should be approximately South-East (~135° to 137°)
        val cairoQibla = QiblaCalculator.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue("Cairo Qibla angle should be between 130 and 140 degrees", cairoQibla in 130f..140f)

        val distance = QiblaCalculator.calculateDistanceKm(30.0444, 31.2357)
        assertTrue("Cairo distance to Makkah should be around 1200-1400 km", distance in 1100.0..1500.0)
    }

    @Test
    fun `quran repository loads 114 surahs and handles offline search`() {
        assertEquals(114, QuranRepository.ALL_SURAHS.size)

        val alFatiha = QuranRepository.getSurah(1)
        assertEquals("Al-Fatihah", alFatiha.nameEnglish)
        assertEquals(7, alFatiha.ayahCount)
        assertEquals(1, alFatiha.startPage)

        val page1 = QuranRepository.getMushafPage(1)
        assertEquals(7, page1.ayahs.size)

        val searchResults = QuranRepository.search("بسم الله")
        assertTrue("Search for Bismillah should yield results", searchResults.isNotEmpty())
    }

    @Test
    fun `quran repository returns ayahs for whole surah reading and supports arabic digits`() {
        val ikhlasAyahs = QuranRepository.getAyahsForSurah(112)
        assertEquals(4, ikhlasAyahs.size)
        assertEquals("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ قُلْ هُوَ ٱللَّهُ أَحَدٌ", ikhlasAyahs[0].arabicText)

        val kawtharAyahs = QuranRepository.getAyahsForSurah(108)
        assertEquals(3, kawtharAyahs.size)

        val nasAyahs = QuranRepository.getAyahsForSurah(114)
        assertEquals(6, nasAyahs.size)

        val fatihaAyahs = QuranRepository.getAyahsForSurah(1)
        assertEquals(7, fatihaAyahs.size)

        assertEquals("١", QuranRepository.toArabicDigits(1))
        assertEquals("٢٣", QuranRepository.toArabicDigits(23))
        assertEquals("١١٤", QuranRepository.toArabicDigits(114))
    }

    @Test
    fun `hijri date helper returns non-empty formatted dates`() {
        val hijri = HijriDateHelper.fromGregorian(Date())
        assertTrue(hijri.formattedEn.contains("AH"))
        assertTrue(hijri.formattedAr.contains("هـ"))
        assertTrue(hijri.year >= 1445)
    }

    @Test
    fun `app language enumeration supports codes and RTL attributes`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode("ar"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromCode("fr"))
        assertEquals(AppLanguage.TURKISH, AppLanguage.fromCode("tr"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("id"))
        assertEquals(AppLanguage.URDU, AppLanguage.fromCode("ur"))
        // Fallback
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("unknown"))

        assertTrue(AppLanguage.ARABIC.isRtl)
        assertTrue(AppLanguage.URDU.isRtl)
        assertFalse(AppLanguage.ENGLISH.isRtl)
        assertFalse(AppLanguage.FRENCH.isRtl)
    }

    @Test
    fun `localization manager saves and retrieves user language preference`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        LocalizationManager.saveLanguage(context, AppLanguage.ARABIC)
        val loadedLang = LocalizationManager.getSavedLanguage(context)
        assertEquals(AppLanguage.ARABIC, loadedLang)

        LocalizationManager.saveLanguage(context, AppLanguage.FRENCH)
        assertEquals(AppLanguage.FRENCH, LocalizationManager.getSavedLanguage(context))
    }

    @Test
    fun `app strings provide translations for all supported languages`() {
        for (lang in AppLanguage.entries) {
            val strings = AppStrings.forLanguage(lang)
            assertNotNull(strings)
            assertTrue("Nav settings should not be empty for ${lang.name}", strings.navSettings.isNotBlank())
            assertTrue("Settings title should not be empty for ${lang.name}", strings.settingsTitle.isNotBlank())
            assertTrue("Language title should not be empty for ${lang.name}", strings.languageSectionTitle.isNotBlank())
            assertTrue("Next badge should not be empty for ${lang.name}", strings.nextBadge.isNotBlank())
        }
    }

    @Test
    fun `notification channels are initialized with high importance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.features.adhan.scheduler.PrayerNotificationScheduler.initChannels(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channel = notificationManager.getNotificationChannel(com.example.features.adhan.scheduler.PrayerNotificationScheduler.CHANNEL_PRAYER_ID)
        assertNotNull(channel)
        assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertTrue(channel.shouldVibrate())
    }

    @Test
    fun `alarm scheduler schedules exact alarm in AlarmManager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val futureTime = System.currentTimeMillis() + 3600_000L

        com.example.features.adhan.scheduler.PrayerNotificationScheduler.scheduleExactAlarm(
            context = context,
            prayerName = PrayerName.MAGHRIB,
            triggerAtMillis = futureTime,
            isPreReminder = false,
            dayOffset = 0
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val shadowAlarmManager = org.robolectric.Shadows.shadowOf(alarmManager)
        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(scheduledAlarm)
        assertEquals(futureTime, scheduledAlarm!!.triggerAtTime)
    }

    @Test
    fun `prayer notification builder creates valid rich notification with actions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.features.adhan.scheduler.PrayerNotificationScheduler.initChannels(context)

        val notification = com.example.features.adhan.scheduler.PrayerNotificationScheduler.buildPrayerNotification(
            context = context,
            prayerName = PrayerName.FAJR,
            isPreReminder = false,
            preMinutes = 15,
            isPlayingAdhan = true
        )

        assertNotNull(notification)
        assertNotNull(notification.actions)
        // Verify at least Mark as Prayed, Snooze, and Stop Adhan actions are present
        assertTrue(notification.actions.size >= 3)
    }

    @Test
    fun `offline rolling window scheduler populates alarms for upcoming prayers`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.features.adhan.scheduler.PrayerNotificationScheduler.scheduleOfflineRollingWindow(context, daysAhead = 2)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val shadowAlarmManager = org.robolectric.Shadows.shadowOf(alarmManager)
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        assertNotNull(scheduledAlarms)
        assertTrue("Expected offline prayer alarms to be scheduled", scheduledAlarms.isNotEmpty())
    }

    @Test
    fun `disabling prayer notification cancels specific prayer alarms from AlarmManager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val futureTime = System.currentTimeMillis() + 3600_000L

        // Schedule an alarm for Fajr
        com.example.features.adhan.scheduler.PrayerNotificationScheduler.scheduleExactAlarm(
            context = context,
            prayerName = PrayerName.FAJR,
            triggerAtMillis = futureTime,
            isPreReminder = false,
            dayOffset = 0
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val shadowAlarmManager = org.robolectric.Shadows.shadowOf(alarmManager)
        assertNotNull(shadowAlarmManager.nextScheduledAlarm)

        // Cancel alarms for Fajr (as performed when user toggles off the prayer notification)
        com.example.features.adhan.scheduler.PrayerNotificationScheduler.cancelPrayerAlarms(
            context = context,
            prayerName = PrayerName.FAJR
        )

        // Verify alarm is cancelled
        val remainingAlarms = shadowAlarmManager.scheduledAlarms.filter {
            it.operation != null
        }
        assertTrue("Alarm for Fajr should be cancelled", remainingAlarms.none {
            it.triggerAtTime == futureTime
        })
    }
}
