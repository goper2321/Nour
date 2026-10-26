package com.example

import android.app.Application
import com.example.database.NourDatabase

/**
 * Nour's Application shell. All startup work lives in [AppInitializer] —
 * a named, ordered step list (NUR-019) — so this class stays a thin shell:
 * one entrypoint call, the durable [database] handle, and the [instance]
 * singleton that AppInitializer binds.
 */
class NourApp : Application() {
    lateinit var database: NourDatabase
        internal set

    override fun onCreate() {
        super.onCreate()
        AppInitializer.attach(this)
    }

    companion object {
        lateinit var instance: NourApp
            internal set
    }
}
