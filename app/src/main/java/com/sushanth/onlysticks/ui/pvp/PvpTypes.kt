package com.sushanth.onlysticks.ui.pvp

import com.sushanth.onlysticks.domain.model.Stick

sealed interface PvpPhase {
    data object Idle : PvpPhase
    data class Hosting(val address: String?, val port: Int) : PvpPhase
    data object Joining : PvpPhase
    data object Handshaking : PvpPhase
    data object InMatch : PvpPhase
    data class Failed(val reason: PvpFailure) : PvpPhase
}

enum class PvpFailure { CANNOT_HOST, CANNOT_CONNECT, DISCONNECTED, PROTOCOL, STAKES_NOT_MATCHED, OPPONENT_DECK_INVALID, MY_LOADOUT_INVALID }

interface PvpMatchSink {
    fun onStarted(myBet: List<Stick>) {}
    fun onVoided() {}
    fun onPwrUpUsed(cardId: String) {}
    fun onSettled(won: Boolean, myBet: List<Stick>, theirBet: List<Stick>) {}

    companion object {
        val NONE = object : PvpMatchSink {}
    }
}

data class LanHost(val name: String, val address: String, val port: Int, val betVal: Int? = null)
