// Common interface for the duel screen, implemented by both the bot and PvP ViewModels.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena

import com.sushanth.onlysticks.domain.combat.TurnTmr
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpTarget
import kotlinx.coroutines.flow.StateFlow

/** What the duel screen needs from a match controller. Lets [DuelScr] work for bot and PvP matches alike. */
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

/** How the opponent is labelled on screen. */
sealed interface OppLbl {
    data class Bot(val tier: Int) : OppLbl
    data class Plr(val name: String) : OppLbl
}
