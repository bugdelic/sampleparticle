package com.hirotakaster.particle

import android.app.Application

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

/**
 * Created by niisato on 2017/10/31.
 */

class ParticleApp : Application() {
    private var mTracker: Tracker? = null

    private var sAnalytics: GoogleAnalytics? = null

    override fun onCreate() {
        super.onCreate()

        sAnalytics = GoogleAnalytics.getInstance(this)
    }

    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    val defaultTracker: Tracker
        @Synchronized get() {
            if (mTracker == null) {
                val analytics = GoogleAnalytics.getInstance(this)
                mTracker = analytics.newTracker(R.xml.global_tracker)
            }
            return mTracker!!
        }
}
