// The per-turn countdown timer. Pure state machine; the UI drives it with tick().
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.domain.combat

import com.sushanth.onlysticks.domain.config.Cfg

/** Turn timer state: total duration, time left and current phase. */
data class TurnTmr(
    val durMs: Long = Cfg.TURN_TIMER_MS,
    val leftMs: Long = durMs,
    val phase: Phase = Phase.RUNNING,
) {
    enum class Phase { RUNNING, PAUSED, SUBMITTED, AUTO_SKIPPED }
    /** Emitted by [tick] when time runs out and the turn should be auto-skipped. */
    sealed interface Event {
        data object AutoSkip : Event
    }
    val isActive: Boolean get() = phase == Phase.RUNNING
    val isDone: Boolean get() = phase == Phase.SUBMITTED || phase == Phase.AUTO_SKIPPED
    /** Fraction of time remaining (1.0 = full, 0.0 = expired). Used for the countdown ring. */
    val fracLeft: Float get() = if (durMs <= 0) 0f else leftMs.toFloat() / durMs
    /** Advances the timer by [elapsedMs]. Returns the new timer and an AutoSkip event if it just expired. */
    fun tick(elapsedMs: Long): Pair<TurnTmr, Event?> {
        require(elapsedMs >= 0) { "elapsed time cannot be negative" }
        if (phase != Phase.RUNNING) return this to null
        val left = (leftMs - elapsedMs).coerceAtLeast(0)
        return if (left == 0L) copy(leftMs = 0, phase = Phase.AUTO_SKIPPED) to Event.AutoSkip
        else copy(leftMs = left) to null
    }
    /** Marks the turn as submitted (stops the timer). */
    fun sbmt(): TurnTmr = if (phase == Phase.RUNNING || phase == Phase.PAUSED) copy(phase = Phase.SUBMITTED) else this

    fun pause(): TurnTmr = if (phase == Phase.RUNNING) copy(phase = Phase.PAUSED) else this
    fun resume(): TurnTmr = if (phase == Phase.PAUSED) copy(phase = Phase.RUNNING) else this

    /** A fresh running timer with the same duration. */
    fun reset(): TurnTmr = TurnTmr(durMs)
}
