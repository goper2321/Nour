package com.example

import android.app.Application
import android.util.Log
import com.example.database.NourDatabase
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.example.features.quran.data.QuranRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NourApp : Application() {
    lateinit var database: NourDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        instance = this
        database = NourDatabase.getInstance(this)
        PrayerNotificationScheduler.initChannels(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PrayerNotificationScheduler.scheduleOfflineRollingWindow(this@NourApp)
            } catch (t: Throwable) {
                Log.e("NourApp", "Failed to schedule offline rolling window", t)
            }
        }
        // Warm the full-text index away from the main thread so the first Quran
        // search never pays for normalizing all 6,236 verses.
        CoroutineScope(Dispatchers.Default).launch {
            try {
                QuranRepository.warmSearchIndex()
            } catch (t: Throwable) {
                Log.e("NourApp", "Failed to warm Quran search index", t)
            }
        }
    }

    companion object {
        lateinit var instance: NourApp
            private set
    }
}
