package com.sushanth.onlysticks.ui.arena.stage

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.sushanth.onlysticks.domain.combat.PlrId

internal val SkyTop = Color(0xFFFFF1C9)

internal val SkyBottom = Color(0xFFD8EFB0)

internal val SunCol = Color(0xFFFFE08A)

internal val DirtFill = Color(0xFFC9955C)

internal val DirtOutl = Color(0xFF8A5A2B)

internal val HeadFill = Color(0xFFFFF3D6)

internal val MissGrey = Color(0xFFB0BEC5)

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
    var stickSnapped by mutableStateOf(false)
    var ignitedOverride by mutableStateOf(false)
    var wetOverride by mutableStateOf(false)

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

internal class StageFx {
    val p1Anim = FgtrAnimState()
    val p2Anim = FgtrAnimState()
    val stageShake = Animatable(0f)
    val sparkProg = Animatable(0f)
    var sparkXFrac by mutableStateOf(0.5f)
    var sparkGoldOnly by mutableStateOf(true)
    val flts = mutableStateListOf<StageFlt>()
    var nextFltId = 1

    fun animFor(plr: PlrId): FgtrAnimState = if (plr == PlrId.P1) p1Anim else p2Anim
    fun xFor(plr: PlrId): Float = if (plr == PlrId.P1) 0.28f else 0.72f
}
