// Flips combat state/events so each device can always draw "me" as P1 (used in PvP as the guest).
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.combat

object Perspective {
    /** Returns [s] as seen by [me]: if I'm P2, players are swapped and the winner flipped. */
    fun state(s: CombatState, me: PlrId): CombatState =
        if (me == PlrId.P1) s
        else CombatState(p1 = s.p2.copy(id = PlrId.P1), p2 = s.p1.copy(id = PlrId.P2), round = s.round, winner = s.winner?.other)

    /** Same flip as [state] but for a list of events. */
    fun evts(evts: List<CombatEvt>, me: PlrId): List<CombatEvt> =
        if (me == PlrId.P1) evts else evts.map(::flip)

    /** Swaps every player reference inside a single event. */
    private fun flip(e: CombatEvt): CombatEvt = when (e) {
        is CombatEvt.Swtch -> e.copy(plr = e.plr.other)
        is CombatEvt.PwrUpUsed -> e.copy(plr = e.plr.other, target = e.target.other)
        is CombatEvt.Init -> e.copy(first = e.first.other, p1Spd = e.p2Spd, p2Spd = e.p1Spd)
        is CombatEvt.MoveRes -> e.copy(plr = e.plr.other)
        is CombatEvt.Hit -> e.copy(attacker = e.attacker.other)
        is CombatEvt.Countered -> e.copy(parrier = e.parrier.other, attacker = e.attacker.other)
        is CombatEvt.Rcl -> e.copy(plr = e.plr.other)
        is CombatEvt.Clink -> e
        is CombatEvt.Whiff -> e.copy(plr = e.plr.other)
        is CombatEvt.Rested -> e.copy(plr = e.plr.other)
        is CombatEvt.Burned -> e.copy(plr = e.plr.other)
        is CombatEvt.StickBrkn -> e.copy(plr = e.plr.other)
        is CombatEvt.KnockedOut -> e.copy(winner = e.winner.other, loser = e.loser.other)
    }
}
