// Choreographs the animation, sound and haptics for one resolved round.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena.stage

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.audio.SoundFx
import com.sushanth.onlysticks.audio.SoundMgr
import com.sushanth.onlysticks.domain.combat.CombatEvt
import com.sushanth.onlysticks.domain.combat.PlrId
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.ui.arena.RoundPb
import com.sushanth.onlysticks.ui.card.lblRes
import com.sushanth.onlysticks.ui.components.GameMotion
import com.sushanth.onlysticks.ui.components.readAnimrScl
import com.sushanth.onlysticks.ui.theme.GameCols
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Plays [curPb] on the stage: resets transient poses, then plays action events (hits, parries, rests)
 * one after another, then status effects (burns, breaks), then the KO if the match ended.
 * Always lasts at least MIN_ROUND_MS so rounds don't flash by.
 */
internal suspend fun StageFx.play(curPb: RoundPb, context: Context, colors: GameCols, haptic: HapticFeedback): Unit = coroutineScope {
    val soundMgr = SoundMgr.get(context)
    flts.clear()
    p1Anim.resetTransient()
    p2Anim.resetTransient()
    stageShake.snapTo(0f)
    sparkProg.snapTo(0f)

    val animScl = readAnimrScl(context)
    val evts = curPb.evts
    val koEvt = evts.filterIsInstance<CombatEvt.KnockedOut>().firstOrNull()

    if (animScl <= 0f) {
        if (koEvt != null) {
            animFor(koEvt.loser).fallDeg.snapTo(90f)
        }
        return@coroutineScope
    }

    val animr = RoundAnimr(
        fx = this@play,
        scope = this,
        context = context,
        colors = colors,
        haptic = haptic,
        sound = soundMgr,
    )

    val moveEvts = evts.filterIsInstance<CombatEvt.MoveRes>()
    moveEvts.forEach { ev ->
        val label = if (ev.autoSkip) context.getString(R.string.stage_zzz) else context.getString(ev.move.lblRes()).uppercase()
        animr.spawnFlt(
            text = label,
            xFrac = xFor(ev.plr),
            yFrac = 0.16f,
            color = colors.onWood,
            fontSizeSp = 18f,
            isBubble = true,
            riseDp = 12f,
        )
    }

    val actionEvts = evts.filter {
        it is CombatEvt.Hit ||
            it is CombatEvt.Countered ||
            it is CombatEvt.Clink ||
            it is CombatEvt.Whiff ||
            it is CombatEvt.Rested
    }

    actionEvts.forEachIndexed { index, evt ->
        val startMs = FIRST_ACTION_MS + index * ACTION_GAP_MS
        launch {
            delay(startMs)
            when (evt) {
                is CombatEvt.Hit -> animr.hit(evt)
                is CombatEvt.Countered -> animr.countered(evt)
                is CombatEvt.Clink -> animr.clink()
                is CombatEvt.Whiff -> animr.whiff(evt)
                is CombatEvt.Rested -> animr.rested(evt)
                else -> Unit
            }
        }
    }

    val statEvts = evts.filter {
        it is CombatEvt.Burned ||
            it is CombatEvt.Rcl ||
            it is CombatEvt.StickBrkn ||
            it is CombatEvt.PwrUpUsed ||
            it is CombatEvt.Swtch
    }
    val lastActionMs = if (actionEvts.isEmpty()) 0L else FIRST_ACTION_MS + (actionEvts.size - 1) * ACTION_GAP_MS
    val statStartMs = lastActionMs + 400L

    if (statEvts.isNotEmpty()) {
        launch {
            delay(statStartMs)
            statEvts.forEachIndexed { idx, evt ->
                val yStagger = (idx % 2) * 0.07f
                animr.status(evt, yStagger)
            }
        }
    }

    val preKoWaitMs = if (statEvts.isNotEmpty()) statStartMs + 350L else lastActionMs + 350L
    if (koEvt != null) {
        delay(preKoWaitMs)
        if (koEvt.winner == PlrId.P1) {
            soundMgr.play(SoundFx.VICTORY, volume = 0.9f)
        } else {
            soundMgr.play(SoundFx.DEFEAT, volume = 0.9f)
        }
        val loserAnim = animFor(koEvt.loser)
        launch {
            loserAnim.fallDeg.animateTo(90f, tween(600, easing = FastOutSlowInEasing))
        }
        delay(900L)
        delay((MIN_ROUND_MS - preKoWaitMs - 900L).coerceAtLeast(0L))
    } else {
        delay(maxOf(MIN_ROUND_MS, preKoWaitMs))
    }
}

/** Delay before the first action animation. */
private const val FIRST_ACTION_MS = 350L
/** Gap between consecutive action animations. */
private const val ACTION_GAP_MS = 550L
/** Minimum total length of a round replay. */
private const val MIN_ROUND_MS = 1700L

/** Plays the individual animations for each kind of event. */
private class RoundAnimr(
    private val fx: StageFx,
    private val scope: CoroutineScope,
    private val context: Context,
    private val colors: GameCols,
    private val haptic: HapticFeedback,
    private val sound: SoundMgr,
) {
    /** Adds a floating number or bubble over the stage. */
    fun spawnFlt(
        text: String,
        xFrac: Float,
        yFrac: Float,
        color: Color,
        fontSizeSp: Float = 30f,
        isBubble: Boolean = false,
        durMs: Long = 900L,
        riseDp: Float = 40f,
    ) {
        val flt = StageFlt(
            id = fx.nextFltId++,
            text = text,
            xFrac = xFrac,
            yFrac = yFrac,
            color = color,
            fontSizeSp = fontSizeSp,
            isBubble = isBubble,
            riseDp = riseDp,
        )
        fx.flts.add(flt)
        scope.launch {
            launch { flt.scale.animateTo(1f, GameMotion.bouncy()) }
            flt.progress.animateTo(1f, tween(durMs.toInt(), easing = LinearEasing))
            fx.flts.remove(flt)
        }
    }

    /** Attack that landed: lunge, swing, knockback, flash, damage number, sound and haptic (bigger for crits). */
    fun hit(evt: CombatEvt.Hit) {
        val attackerAnim = fx.animFor(evt.attacker)
        val defender = evt.attacker.other
        val defenderAnim = fx.animFor(defender)
        val defenderX = fx.xFor(defender)

        scope.launch {
            when (evt.move) {
                Move.JAB -> {
                    attackerAnim.lungeX.animateTo(0.14f, tween(60, easing = FastOutSlowInEasing))
                    attackerAnim.lungeX.animateTo(0f, tween(60))
                }
                Move.LUNGE -> {
                    attackerAnim.lungeX.animateTo(0.32f, tween(110, easing = FastOutSlowInEasing))
                    attackerAnim.lungeX.animateTo(0f, tween(110))
                }
                Move.SLASH -> {
                    launch {
                        attackerAnim.stickSwingDeg.snapTo(-50f)
                        attackerAnim.stickSwingDeg.animateTo(65f, tween(120, easing = FastOutSlowInEasing))
                        attackerAnim.stickSwingDeg.animateTo(0f, tween(80))
                    }
                    attackerAnim.lungeX.animateTo(0.22f, tween(100, easing = FastOutSlowInEasing))
                    attackerAnim.lungeX.animateTo(0f, tween(100))
                }
                Move.EXHAUSTION_STRIKE -> {
                    attackerAnim.lungeX.animateTo(0.12f, tween(140, easing = LinearEasing))
                    attackerAnim.lungeX.animateTo(0f, tween(120, easing = LinearEasing))
                }
                else -> {
                    attackerAnim.lungeX.animateTo(0.14f, tween(70))
                    attackerAnim.lungeX.animateTo(0f, tween(70))
                }
            }
        }

        scope.launch {
            defenderAnim.flashAlpha.snapTo(0.85f)
            defenderAnim.flashAlpha.animateTo(0f, tween(240))
        }
        scope.launch {
            defenderAnim.knockbackDp.snapTo(10f)
            defenderAnim.knockbackDp.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
        }

        runCatching {
            haptic.performHapticFeedback(
                if (evt.crit) HapticFeedbackType.LongPress
                else HapticFeedbackType.TextHandleMove,
            )
        }
        if (evt.crit) {
            sound.play(SoundFx.CRIT, volume = 1.0f)
        } else {
            sound.play(SoundFx.HIT, volume = 0.9f)
        }

        if (evt.crit) {
            scope.launch {
                fx.stageShake.snapTo(1f)
                fx.stageShake.animateTo(0f, tween(450, easing = LinearEasing))
            }
            spawnFlt(
                text = context.getString(R.string.stage_crit),
                xFrac = defenderX,
                yFrac = 0.20f,
                color = colors.crit,
                fontSizeSp = 22f,
                riseDp = 44f,
            )
            spawnFlt(
                text = context.getString(R.string.stage_minus, evt.dmg),
                xFrac = defenderX,
                yFrac = 0.32f,
                color = colors.crit,
                fontSizeSp = 40f,
                riseDp = 40f,
            )
        } else {
            spawnFlt(
                text = context.getString(R.string.stage_minus, evt.dmg),
                xFrac = defenderX,
                yFrac = 0.32f,
                color = Color.White,
                fontSizeSp = 30f,
                riseDp = 40f,
            )
        }
    }

    /** Parried attack: block pose for the parrier and counter damage on the attacker. */
    fun countered(evt: CombatEvt.Countered) {
        sound.play(SoundFx.PARRY, volume = 0.95f)
        val parrierAnim = fx.animFor(evt.parrier)
        val attackerAnim = fx.animFor(evt.attacker)
        val parrierX = fx.xFor(evt.parrier)
        val attackerX = fx.xFor(evt.attacker)

        scope.launch {
            parrierAnim.blockPose.animateTo(1f, tween(90))
            delay(260)
            parrierAnim.blockPose.animateTo(0f, tween(120))
        }
        scope.launch {
            attackerAnim.lungeX.animateTo(0.20f, tween(90))
            attackerAnim.flashAlpha.snapTo(0.75f)
            launch { attackerAnim.flashAlpha.animateTo(0f, tween(220)) }
            launch {
                attackerAnim.knockbackDp.snapTo(10f)
                attackerAnim.knockbackDp.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
            }
            attackerAnim.lungeX.animateTo(0f, tween(140))
        }
        scope.launch {
            fx.sparkXFrac = if (evt.parrier == PlrId.P1) 0.40f else 0.60f
            fx.sparkGoldOnly = true
            fx.sparkProg.burst(320)
        }
        spawnFlt(
            text = context.getString(R.string.stage_parry),
            xFrac = parrierX,
            yFrac = 0.28f,
            color = colors.leaf,
            fontSizeSp = 28f,
            riseDp = 36f,
        )
        spawnFlt(
            text = context.getString(R.string.stage_minus, evt.dmg),
            xFrac = attackerX,
            yFrac = 0.32f,
            color = Color.White,
            fontSizeSp = 30f,
            riseDp = 40f,
        )
    }

    /** Both players parried: a clink spark between them. */
    fun clink() {
        sound.play(SoundFx.PARRY, volume = 0.8f, rate = 1.15f)
        scope.launch {
            fx.p1Anim.lungeX.animateTo(0.16f, tween(100))
            fx.p1Anim.lungeX.animateTo(0f, tween(140))
        }
        scope.launch {
            fx.p2Anim.lungeX.animateTo(0.16f, tween(100))
            fx.p2Anim.lungeX.animateTo(0f, tween(140))
        }
        scope.launch {
            fx.sparkXFrac = 0.5f
            fx.sparkGoldOnly = false
            fx.sparkProg.burst(340)
        }
        spawnFlt(
            text = context.getString(R.string.stage_clink),
            xFrac = 0.5f,
            yFrac = 0.28f,
            color = colors.gold,
            fontSizeSp = 30f,
            riseDp = 36f,
        )
    }

    /** Parry with nothing to block. */
    fun whiff(evt: CombatEvt.Whiff) {
        sound.play(SoundFx.WHOOSH, volume = 0.7f)
        val whoAnim = fx.animFor(evt.plr)
        scope.launch {
            whoAnim.stickSwingDeg.snapTo(-35f)
            whoAnim.stickSwingDeg.animateTo(50f, tween(140))
            whoAnim.stickSwingDeg.animateTo(0f, tween(100))
        }
        spawnFlt(
            text = context.getString(R.string.stage_miss),
            xFrac = fx.xFor(evt.plr),
            yFrac = 0.30f,
            color = MissGrey,
            fontSizeSp = 28f,
            riseDp = 36f,
        )
    }

    /** Resting animation with stamina recovered. */
    fun rested(evt: CombatEvt.Rested) {
        sound.play(SoundFx.REST, volume = 0.65f)
        val whoAnim = fx.animFor(evt.plr)
        scope.launch {
            whoAnim.crouch.animateTo(1f, tween(140))
            delay(260)
            whoAnim.crouch.animateTo(0f, tween(150))
        }
        spawnFlt(
            text = context.getString(R.string.stage_rest_gain, evt.recovered),
            xFrac = fx.xFor(evt.plr),
            yFrac = 0.30f,
            color = colors.stam,
            fontSizeSp = 28f,
            riseDp = 38f,
        )
    }

    /** Status effects: switches, power-ups, burns, recoil and broken sticks. */
    fun status(evt: CombatEvt, yStagger: Float) {
        when (evt) {
            is CombatEvt.Burned -> {
                val whoAnim = fx.animFor(evt.plr)
                whoAnim.ignitedOverride = true
                scope.launch {
                    whoAnim.burnBurst.burst(420)
                }
                spawnFlt(
                    text = context.getString(R.string.stage_burn, evt.amount),
                    xFrac = fx.xFor(evt.plr),
                    yFrac = 0.28f - yStagger,
                    color = colors.fire,
                    fontSizeSp = 26f,
                    riseDp = 36f,
                )
            }

            is CombatEvt.Rcl -> {
                val whoAnim = fx.animFor(evt.plr)
                scope.launch {
                    whoAnim.flashAlpha.snapTo(0.6f)
                    whoAnim.flashAlpha.animateTo(0f, tween(200))
                }
                spawnFlt(
                    text = context.getString(R.string.stage_minus, evt.dmg),
                    xFrac = fx.xFor(evt.plr),
                    yFrac = 0.34f - yStagger,
                    color = colors.fire,
                    fontSizeSp = 28f,
                    riseDp = 36f,
                )
            }

            is CombatEvt.StickBrkn -> {
                sound.play(SoundFx.BREAK, volume = 0.95f)
                val whoAnim = fx.animFor(evt.plr)
                whoAnim.stickSnapped = true
                scope.launch {
                    whoAnim.splinters.burst(520, easing = LinearEasing)
                }
                spawnFlt(
                    text = context.getString(R.string.stage_snap),
                    xFrac = fx.xFor(evt.plr),
                    yFrac = 0.24f - yStagger,
                    color = colors.amber,
                    fontSizeSp = 28f,
                    riseDp = 38f,
                )
            }

            is CombatEvt.PwrUpUsed -> {
                val whoAnim = fx.animFor(evt.target)
                when (evt.type) {
                    PwrUpType.LIGHTER -> {
                        whoAnim.ignitedOverride = true
                        scope.launch {
                            whoAnim.burnBurst.burst(400)
                        }
                    }
                    PwrUpType.WATER_BOTTLE -> {
                        whoAnim.wetOverride = true
                        scope.launch {
                            whoAnim.waterSplash.burst(420)
                        }
                    }
                }
            }

            is CombatEvt.Swtch -> {
                val whoAnim = fx.animFor(evt.plr)
                whoAnim.stickSnapped = false
                scope.launch {
                    whoAnim.poof.burst(360)
                }
            }

            else -> Unit
        }
    }
}

/** Animates 0 to 1 then snaps back to 0 (used for one-shot bursts). */
private suspend fun Animatable<Float, AnimationVector1D>.burst(
    durMs: Int,
    easing: Easing = FastOutSlowInEasing,
) {
    snapTo(0.01f)
    animateTo(1f, tween(durMs, easing = easing))
    snapTo(0f)
}
