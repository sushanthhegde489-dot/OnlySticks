package com.sushanth.onlysticks.domain.combat

import com.sushanth.onlysticks.domain.config.Cfg

data class TurnTmr(
    val durMs: Long = Cfg.TURN_TIMER_MS,
    val leftMs: Long = durMs,
    val phase: Phase = Phase.RUNNING,
) {
    enum class Phase { RUNNING, PAUSED, SUBMITTED, AUTO_SKIPPED }
    sealed interface Event {
        data object AutoSkip : Event
    }
    val isActive: Boolean get() = phase == Phase.RUNNING
    val isDone: Boolean get() = phase == Phase.SUBMITTED || phase == Phase.AUTO_SKIPPED
    val fracLeft: Float get() = if (durMs <= 0) 0f else leftMs.toFloat() / durMs
    fun tick(elapsedMs: Long): Pair<TurnTmr, Event?> {
        require(elapsedMs >= 0) { "elapsed time cannot be negative" }
        if (phase != Phase.RUNNING) return this to null
        val left = (leftMs - elapsedMs).coerceAtLeast(0)
        return if (left == 0L) copy(leftMs = 0, phase = Phase.AUTO_SKIPPED) to Event.AutoSkip
        else copy(leftMs = left) to null
    }
    fun sbmt(): TurnTmr = if (phase == Phase.RUNNING || phase == Phase.PAUSED) copy(phase = Phase.SUBMITTED) else this

    fun pause(): TurnTmr = if (phase == Phase.RUNNING) copy(phase = Phase.PAUSED) else this
    fun resume(): TurnTmr = if (phase == Phase.PAUSED) copy(phase = Phase.RUNNING) else this

    fun reset(): TurnTmr = TurnTmr(durMs)
}
