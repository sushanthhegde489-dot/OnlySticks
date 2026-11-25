// Turns combat events into human-readable battle log lines.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena

import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.combat.CombatEvt
import com.sushanth.onlysticks.domain.combat.PlrId
import com.sushanth.onlysticks.ui.card.lblRes
import com.sushanth.onlysticks.ui.common.UiTxt
import com.sushanth.onlysticks.ui.common.uiTxt

/** Converts [CombatEvt]s into localised log text. P1 is always "you". */
object ArenaLog {
    /** "You" or "Opponent". */
    fun who(plr: PlrId): UiTxt = uiTxt(if (plr == PlrId.P1) R.string.arena_you else R.string.arena_opp)

    /** Chooses between the "you" and "opponent" version of a string. */
    private fun pick(plr: PlrId, youRes: Int, oppRes: Int): Int = if (plr == PlrId.P1) youRes else oppRes

    /** Slots are shown 1-based. */
    private fun slot(index: Int): Int = index + 1

    /** Log line for one event. */
    fun line(evt: CombatEvt): UiTxt = when (evt) {
        is CombatEvt.Swtch -> if (evt.automatic) {
            uiTxt(pick(evt.plr, R.string.log_swtch_auto_you, R.string.log_swtch_auto_opp), slot(evt.toSlot))
        } else {
            uiTxt(R.string.log_swtch, who(evt.plr), slot(evt.toSlot))
        }
        is CombatEvt.PwrUpUsed -> uiTxt(
            R.string.log_powerup, who(evt.plr), uiTxt(evt.type.lblRes()),
            uiTxt(if (evt.target == PlrId.P1) R.string.log_target_you else R.string.log_target_opp),
        )
        is CombatEvt.Init -> uiTxt(
            if (evt.tieBrkn) R.string.log_init_tie else R.string.log_init, who(evt.first),
        )
        is CombatEvt.MoveRes -> if (evt.autoSkip) uiTxt(R.string.log_auto_skip, who(evt.plr))
        else uiTxt(R.string.log_move, who(evt.plr), uiTxt(evt.move.lblRes()))
        is CombatEvt.Hit -> uiTxt(
            if (evt.crit) R.string.log_hit_crit else R.string.log_hit,
            who(evt.attacker), uiTxt(evt.move.lblRes()), evt.dmg,
        )
        is CombatEvt.Countered -> uiTxt(pick(evt.parrier, R.string.log_countered_you, R.string.log_countered_opp), evt.dmg)
        is CombatEvt.Rcl -> uiTxt(R.string.log_rcl, who(evt.plr), evt.dmg)
        is CombatEvt.Clink -> uiTxt(R.string.log_clink)
        is CombatEvt.Whiff -> uiTxt(R.string.log_whiff, who(evt.plr))
        is CombatEvt.Rested -> uiTxt(R.string.log_rested, who(evt.plr), evt.recovered)
        is CombatEvt.Burned -> uiTxt(pick(evt.plr, R.string.log_burned_you, R.string.log_burned_opp), slot(evt.slot), evt.amount)
        is CombatEvt.StickBrkn -> uiTxt(pick(evt.plr, R.string.log_stick_brkn_you, R.string.log_stick_brkn_opp), slot(evt.slot))
        is CombatEvt.KnockedOut -> uiTxt(pick(evt.winner, R.string.log_knocked_out_you, R.string.log_knocked_out_opp))
    }

    /** Log lines for a list of events. */
    fun lines(evts: List<CombatEvt>): List<UiTxt> = evts.map(::line)
}
