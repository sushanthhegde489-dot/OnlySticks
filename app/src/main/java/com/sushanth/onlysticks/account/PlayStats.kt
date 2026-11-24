package com.sushanth.onlysticks.account

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.data.repository.Stat
import com.sushanth.onlysticks.data.repository.StatsRepository
import kotlinx.coroutines.CancellationException

class PlayStats(context: Context, private val stats: StatsRepository) {
    private val appContext = context.applicationContext

    suspend fun push(act: Activity) {
        if (!isOn(appContext)) return
        try {
            if (!PlayGames.getGamesSignInClient(act).isAuthenticated().await().isAuthenticated) return
            val events = PlayGames.getEventsClient(act)
            for ((stat, n) in stats.unsent()) {
                val id = appContext.getString(eventRes(stat))
                if (id.isBlank()) continue
                events.increment(id, n.toInt())
                stats.markSent(stat, n)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "couldn't send stats to Play Games", e)
        }
    }

    private fun eventRes(stat: Stat): Int = when (stat) {
        Stat.PVE_PLAYED -> R.string.event_pve_played
        Stat.PVE_WON -> R.string.event_pve_won
        Stat.PVE_LOST -> R.string.event_pve_lost
        Stat.PVP_PLAYED -> R.string.event_pvp_played
        Stat.PVP_WON -> R.string.event_pvp_won
        Stat.PACKS_OPENED -> R.string.event_packs_opened
    }

    companion object {
        private const val TAG = "PlayStats"

        fun isOn(ctx: Context) = ctx.getString(R.string.game_services_project_id).isNotBlank()

        fun init(ctx: Context) {
            if (isOn(ctx)) PlayGamesSdk.initialize(ctx)
        }
    }
}
