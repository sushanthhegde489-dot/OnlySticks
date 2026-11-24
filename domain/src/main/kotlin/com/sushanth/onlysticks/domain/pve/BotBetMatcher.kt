package com.sushanth.onlysticks.domain.pve

import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.combat.DamageCalc
import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.economy.BetRules
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

object BotBetMatcher {
    private data class Plan(val rarities: List<Rarity>) {
        val minVal: Int = rarities.sumOf { ceil(mult(it) * minSum(it)).toInt() }
        val maxVal: Int = rarities.sumOf { floor(mult(it) * maxSum(it)).toInt() }
    }
    private val ALL_PLANS: List<Plan> = buildList {
        val r = Rarity.entries
        for (a in r.indices) {
            add(Plan(listOf(r[a])))
            for (b in a until r.size) {
                add(Plan(listOf(r[a], r[b])))
                for (c in b until r.size) add(Plan(listOf(r[a], r[b], r[c])))
            }
        }
    }.filter { BetRules.validBetCnt(it.rarities.size) }
    fun matchBet(plrBetVal: Int, tier: Int, rng: DeterministicRng): List<Card> {
        val diff = BotDifficulty(tier)
        val target = plrBetVal.coerceAtLeast(0)
        val low = target * (1 - Cfg.Economy.STAKE_TOLERANCE)
        val high = target / (1 - Cfg.Economy.STAKE_TOLERANCE)
        val candidates = ALL_PLANS.filter { it.maxVal >= low && it.minVal <= high }.toMutableList()

        var best: List<Card>? = null
        while (candidates.isNotEmpty()) {
            val plan = candidates.removeAt(pickPlan(candidates, diff, rng))
            val bet = build(plan, target, tier, rng)
            if (BetRules.withinTolerance(target, Appr.tot(bet))) return bet
            if (best == null || closer(bet, best, target)) best = bet
        }
        if (best != null) return best
        val fallback = ALL_PLANS.minBy { distance(it, target) }
        return build(fallback, target, tier, rng)
    }
    private fun pickPlan(plans: List<Plan>, diff: BotDifficulty, rng: DeterministicRng): Int {
        val weights = plans.map { plan -> plan.rarities.fold(1) { acc, r -> acc * diff.rarWeights[r.ordinal] } }
        return if (weights.sum() > 0) rng.weightedIdx(weights) else rng.nextInt(0, plans.size)
    }
    private fun build(plan: Plan, target: Int, tier: Int, rng: DeterministicRng): List<Card> {
        val goal = target.coerceIn(plan.minVal, plan.maxVal)
        val values = splitExact(goal, plan.rarities.map { valRange(it) }, rng)
        return plan.rarities.zip(values).mapIndexed { n, (rarity, value) ->
            val sum = DamageCalc.roundHalfUp(value / mult(rarity)).coerceIn(minSum(rarity), maxSum(rarity))
            val (d, s, a) = splitExact(sum, listOf(rarity.durability, rarity.speed, rarity.attack), rng)
            Stick("bot-$tier-stake-$n", rarity, BaseStats(d, s, a))
        }
    }
    private fun splitExact(tot: Int, ranges: List<IntRange>, rng: DeterministicRng): List<Int> {
        var extra = tot - ranges.sumOf { it.first }
        return ranges.mapIndexed { i, range ->
            val room = range.last - range.first
            val laterRoom = ranges.drop(i + 1).sumOf { it.last - it.first }
            val give = rng.nextInt((extra - laterRoom).coerceAtLeast(0)..minOf(room, extra))
            extra -= give
            range.first + give
        }
    }
    private fun closer(a: List<Card>, b: List<Card>, target: Int) =
        abs(Appr.tot(a) - target) < abs(Appr.tot(b) - target)
    private fun distance(plan: Plan, target: Int): Int = when {
        target < plan.minVal -> plan.minVal - target
        target > plan.maxVal -> target - plan.maxVal
        else -> 0
    }
    private fun mult(r: Rarity) = Cfg.Appr.tierMult(r)
    private fun minSum(r: Rarity) = r.durability.first + r.speed.first + r.attack.first
    private fun maxSum(r: Rarity) = r.durability.last + r.speed.last + r.attack.last
    private fun valRange(r: Rarity) = ceil(mult(r) * minSum(r)).toInt()..floor(mult(r) * maxSum(r)).toInt()
}
