package com.sushanth.onlysticks.domain.combat

import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.Move.EXHAUSTION_STRIKE as X
import com.sushanth.onlysticks.domain.model.Move.JAB as J
import com.sushanth.onlysticks.domain.model.Move.LUNGE as L
import com.sushanth.onlysticks.domain.model.Move.PARRY as P
import com.sushanth.onlysticks.domain.model.Move.REST as R
import com.sushanth.onlysticks.domain.model.Move.SLASH as S

sealed interface SideOutcome {
    data object Hit : SideOutcome
    data class Blocked(val counterFrac: Double) : SideOutcome
    data object ParrySuccess : SideOutcome
    data object ParryClink : SideOutcome
    data object ParryWhiff : SideOutcome
    data object Rest : SideOutcome
}

enum class Order { INITIATIVE, ROW_FIRST, COL_FIRST }

data class Cell(val row: SideOutcome, val col: SideOutcome, val order: Order = Order.INITIATIVE)

object InteractionMatrix {
    private val hit = SideOutcome.Hit
    private val rest = SideOutcome.Rest
    private val ok = SideOutcome.ParrySuccess
    private val clink = SideOutcome.ParryClink
    private val whiff = SideOutcome.ParryWhiff
    private val jabBlocked = SideOutcome.Blocked(0.50)
    private val lungeBlocked = SideOutcome.Blocked(0.75)
    private val slashBlocked = SideOutcome.Blocked(0.50)
    private val exhaustBlocked = SideOutcome.Blocked(0.50)
    val table: Map<Pair<Move, Move>, Cell> = mapOf(
        (J to J) to Cell(hit, hit),
        (J to L) to Cell(hit, hit, Order.ROW_FIRST),
        (J to P) to Cell(jabBlocked, ok),
        (J to S) to Cell(hit, hit),
        (J to R) to Cell(hit, rest),
        (J to X) to Cell(hit, hit),
        (L to J) to Cell(hit, hit, Order.COL_FIRST),
        (L to L) to Cell(hit, hit),
        (L to P) to Cell(lungeBlocked, ok),
        (L to S) to Cell(hit, hit, Order.COL_FIRST),
        (L to R) to Cell(hit, rest),
        (L to X) to Cell(hit, hit),
        (P to J) to Cell(ok, jabBlocked),
        (P to L) to Cell(ok, lungeBlocked),
        (P to P) to Cell(clink, clink),
        (P to S) to Cell(ok, slashBlocked),
        (P to R) to Cell(whiff, rest),
        (P to X) to Cell(ok, exhaustBlocked),
        (S to J) to Cell(hit, hit),
        (S to L) to Cell(hit, hit, Order.ROW_FIRST),
        (S to P) to Cell(slashBlocked, ok),
        (S to S) to Cell(hit, hit),
        (S to R) to Cell(hit, rest),
        (S to X) to Cell(hit, hit),
        (R to J) to Cell(rest, hit),
        (R to L) to Cell(rest, hit),
        (R to P) to Cell(rest, whiff),
        (R to S) to Cell(rest, hit),
        (R to R) to Cell(rest, rest),
        (R to X) to Cell(rest, hit),
        (X to J) to Cell(hit, hit),
        (X to L) to Cell(hit, hit),
        (X to P) to Cell(exhaustBlocked, ok),
        (X to S) to Cell(hit, hit),
        (X to R) to Cell(hit, rest),
        (X to X) to Cell(hit, hit),
    )
    fun cell(row: Move, col: Move): Cell = table.getValue(row to col)
}
