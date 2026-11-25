// Animation state classes and colours used by the arena stage.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.arena.stage

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.sushanth.onlysticks.domain.combat.PlrId

/** Top of the sky gradient. */
internal val SkyTop = Color(0xFFFFF1C9)

/** Bottom of the sky gradient. */
internal val SkyBottom = Color(0xFFD8EFB0)

internal val SunCol = Color(0xFFFFE08A)

/** Fighting ring fill and outline. */
internal val DirtFill = Color(0xFFC9955C)

internal val DirtOutl = Color(0xFF8A5A2B)

/** Fighter head colour. */
internal val HeadFill = Color(0xFFFFF3D6)

/** Colour for "miss" text. */
internal val MissGrey = Color(0xFFB0BEC5)

/** Every animatable value that controls one fighter's pose and effects. */
internal class FgtrAnimState {
    val lungeX = Animatable(0f)
    val knockbackDp = Animatable(0f)
    val flashAlpha = Animatable(0f)
    val stickSwingDeg = Animatable(0f)
    val blockPose = Animatable(0f)
    val crouch = Animatable(0f)
    val fallDeg = Animatable(0f)
    val burnBurst = Animatable(0f)
    val waterSplash = Animatable(0f)
    val poof = Animatable(0f)
    val splinters = Animatable(0f)
    /** Overrides shown during playback before the combat state catches up (e.g. stick snapped mid-animation). */
    var stickSnapped by mutableStateOf(false)
    var ignitedOverride by mutableStateOf(false)
    var wetOverride by mutableStateOf(false)

    /** Snaps all temporary animations back to rest before a new round plays. */
    suspend fun resetTransient() {
        lungeX.snapTo(0f)
        knockbackDp.snapTo(0f)
        flashAlpha.snapTo(0f)
        stickSwingDeg.snapTo(0f)
        blockPose.snapTo(0f)
        crouch.snapTo(0f)
        burnBurst.snapTo(0f)
        waterSplash.snapTo(0f)
        poof.snapTo(0f)
        splinters.snapTo(0f)
        stickSnapped = false
        ignitedOverride = false
        wetOverride = false
    }
}

/** One floating text on the stage, positioned as fractions of the stage size. */
internal class StageFlt(
    val id: Int,
    val text: String,
    val xFrac: Float,
    val yFrac: Float,
    val color: Color,
    val fontSizeSp: Float = 30f,
    val isBubble: Boolean = false,
    val riseDp: Float = 40f,
) {
    val progress = Animatable(0f)
    val scale = Animatable(if (isBubble) 0.7f else 0.45f)
}

/** Shared animation state for the whole stage: both fighters, screen shake, sparks and floaters. */
internal class StageFx {
    val p1Anim = FgtrAnimState()
    val p2Anim = FgtrAnimState()
    val stageShake = Animatable(0f)
    val sparkProg = Animatable(0f)
    var sparkXFrac by mutableStateOf(0.5f)
    var sparkGoldOnly by mutableStateOf(true)
    val flts = mutableStateListOf<StageFlt>()
    var nextFltId = 1

    /** Animation state for a player. */
    fun animFor(plr: PlrId): FgtrAnimState = if (plr == PlrId.P1) p1Anim else p2Anim
    /** Horizontal position of a player's fighter (fraction of stage width). */
    fun xFor(plr: PlrId): Float = if (plr == PlrId.P1) 0.28f else 0.72f
}
