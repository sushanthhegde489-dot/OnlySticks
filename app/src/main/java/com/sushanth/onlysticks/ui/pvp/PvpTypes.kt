// Types shared by the PvP lobby and match ViewModel.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.pvp

import com.sushanth.onlysticks.domain.model.Stick

/** Where the PvP connection flow currently is. */
sealed interface PvpPhase {
    data object Idle : PvpPhase
    data class Hosting(val address: String?, val port: Int) : PvpPhase
    data object Joining : PvpPhase
    data object Handshaking : PvpPhase
    data object InMatch : PvpPhase
    data class Failed(val reason: PvpFailure) : PvpPhase
}

/** Why a PvP connection or match failed. */
enum class PvpFailure { CANNOT_HOST, CANNOT_CONNECT, DISCONNECTED, PROTOCOL, STAKES_NOT_MATCHED, OPPONENT_DECK_INVALID, MY_LOADOUT_INVALID }

/** Callbacks for PvP match lifecycle events, used to escrow and settle staked cards. [NONE] ignores all. */
interface PvpMatchSink {
    fun onStarted(myBet: List<Stick>) {}
    fun onVoided() {}
    fun onPwrUpUsed(cardId: String) {}
    fun onSettled(won: Boolean, myBet: List<Stick>, theirBet: List<Stick>) {}

    companion object {
        val NONE = object : PvpMatchSink {}
    }
}

/** A game discovered on the local network. */
data class LanHost(val name: String, val address: String, val port: Int, val betVal: Int? = null)
