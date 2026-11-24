package com.sushanth.onlysticks

import android.app.Application
import androidx.work.Configuration
import com.sushanth.onlysticks.account.PlayStats

class OnlySticksApplication : Application(), Configuration.Provider {
    val cont: AppCont by lazy { AppCont(this) }

    override fun onCreate() {
        super.onCreate()
        PlayStats.init(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(cont.workerFactory).build()
}
