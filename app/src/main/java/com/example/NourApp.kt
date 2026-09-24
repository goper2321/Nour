package com.example

import android.app.Application
import android.util.Log
import com.example.database.NourDatabase
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
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
    }

    companion object {
        lateinit var instance: NourApp
            private set
    }
}
