// Application class: owns the [AppCont] and configures WorkManager.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks

import android.app.Application
import androidx.work.Configuration
import com.sushanth.onlysticks.account.PlayStats

/** Holds the app container and provides a WorkManager config that uses our worker factory. */
class OnlySticksApplication : Application(), Configuration.Provider {
    val cont: AppCont by lazy { AppCont(this) }

    override fun onCreate() {
        super.onCreate()
        PlayStats.init(this)
    }

    /** On-demand WorkManager configuration so workers receive their dependencies. */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(cont.workerFactory).build()
}
