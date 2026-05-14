package com.nammahaadi.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NammaHaadiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // OSMDroid config removed — using Google Maps now
    }
}
