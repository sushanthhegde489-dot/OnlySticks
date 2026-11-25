// Builds the bot's side of a card wager so it roughly matches the player's stake value.
// (Comments in this file were written by AI - Claude.)
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

/** Generates a bot stake (1-3 sticks) whose appraised value is within tolerance of the player's bet. */
object BotBetMatcher {
    /** A candidate stake shape: a list of rarities, with the min/max appraisal it could possibly reach. */
    private data class Plan(val rarities: List<Rarity>) {
        val minVal: Int = rarities.sumOf { ceil(mult(it) * minSum(it)).toInt() }
        val maxVal: Int = rarities.sumOf { floor(mult(it) * maxSum(it)).toInt() }
    }
    /** Every combination of 1..3 rarities (order-independent) that is a legal bet size. */
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
    /**
     * Returns sticks worth roughly [plrBetVal]. Tries plans weighted by the bot tier's rarity preferences
     * until one lands within tolerance; otherwise returns the closest attempt (or the nearest plan as a fallback).
     */
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
    /** Picks a plan index, favouring rarities this bot tier normally uses. */
    private fun pickPlan(plans: List<Plan>, diff: BotDifficulty, rng: DeterministicRng): Int {
        val weights = plans.map { plan -> plan.rarities.fold(1) { acc, r -> acc * diff.rarWeights[r.ordinal] } }
        return if (weights.sum() > 0) rng.weightedIdx(weights) else rng.nextInt(0, plans.size)
    }
    /** Creates real sticks for a plan, splitting the target value across cards and each card's three stats. */
    private fun build(plan: Plan, target: Int, tier: Int, rng: DeterministicRng): List<Card> {
        val goal = target.coerceIn(plan.minVal, plan.maxVal)
        val values = splitExact(goal, plan.rarities.map { valRange(it) }, rng)
        return plan.rarities.zip(values).mapIndexed { n, (rarity, value) ->
            val sum = DamageCalc.roundHalfUp(value / mult(rarity)).coerceIn(minSum(rarity), maxSum(rarity))
            val (d, s, a) = splitExact(sum, listOf(rarity.durability, rarity.speed, rarity.attack), rng)
            Stick("bot-$tier-stake-$n", rarity, BaseStats(d, s, a))
        }
    }
    /** Randomly splits [tot] into values that each fall within the matching range and add up exactly to [tot]. */
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
    /** True if stake [a] is closer to [target] than stake [b]. */
    private fun closer(a: List<Card>, b: List<Card>, target: Int) =
        abs(Appr.tot(a) - target) < abs(Appr.tot(b) - target)
    /** How far [target] is outside the plan's achievable value range (0 if inside). */
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
