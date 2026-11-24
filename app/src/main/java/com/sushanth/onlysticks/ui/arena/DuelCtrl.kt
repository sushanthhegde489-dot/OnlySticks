package com.sushanth.onlysticks.ui.arena

import com.sushanth.onlysticks.domain.combat.TurnTmr
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import kotlinx.coroutines.flow.StateFlow

interface DuelCtrl {
    val state: StateFlow<ArenaUiState>
    val tmr: StateFlow<TurnTmr>
    fun selectMove(move: Move): Boolean
    fun selectSwtch(slot: Int?): Boolean
    fun selectPwrUp(slot: Int?): Boolean
    fun selectPwrUpTarget(target: PwrUpTarget): Boolean
    fun sbmt(): Boolean
    fun onTmrTick(elapsedMs: Long)
    fun pauseTmr()
    fun resumeTmr()
    fun leaveMatch()
}

sealed interface OppLbl {
    data class Bot(val tier: Int) : OppLbl
    data class Plr(val name: String) : OppLbl
}
