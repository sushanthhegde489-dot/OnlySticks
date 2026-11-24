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

class MainActivity : ComponentActivity() {
    private val cont by lazy { (application as OnlySticksApplication).cont }

    private val reqStpPerm = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { }

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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { cont.playStats.push(this@MainActivity) }
    }

    private fun reqStpsIfNeeded() {
        if (!HpConnectStpSrc.isAvail(this)) return
        lifecycleScope.launch {
            if (!cont.stpSrc.hasPerm()) reqStpPerm.launch(HpConnectStpSrc.PERMISSIONS)
        }
    }
}
