// The single activity hosting the whole Compose UI.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.sushanth.onlysticks.health.HpConnectStpSrc
import com.sushanth.onlysticks.health.StpSyncScheduler
import com.sushanth.onlysticks.ui.app.OnlySticksApp
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme
import kotlinx.coroutines.launch

/** Entry activity. Starts background services and shows [OnlySticksApp]. */
class MainActivity : ComponentActivity() {
    private val cont by lazy { (application as OnlySticksApplication).cont }

    /** Health Connect permission request (the result is read later, so the callback is empty). */
    private val reqStpPerm = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { }

    /** Runs start-up tasks, schedules step sync, asks for step permission on first launch and sets the UI. */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        cont.onAppStart()
        StpSyncScheduler.schedule(this)
        if (savedInstanceState == null) reqStpsIfNeeded()
        setContent {
            OnlySticksTheme {
                OnlySticksApp(cont)
            }
        }
    }

    /** Sends any pending stats to Google Play Games each time the app comes to the foreground. */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { cont.playStats.push(this@MainActivity) }
    }

    /** Requests step-count permission if Health Connect is installed and permission isn't granted yet. */
    private fun reqStpsIfNeeded() {
        if (!HpConnectStpSrc.isAvail(this)) return
        lifecycleScope.launch {
            if (!cont.stpSrc.hasPerm()) reqStpPerm.launch(HpConnectStpSrc.PERMISSIONS)
        }
    }
}
