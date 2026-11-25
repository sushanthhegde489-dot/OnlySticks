// String resources and icons for game enums.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.ui.card

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.sushanth.onlysticks.R
import com.sushanth.onlysticks.domain.model.Move
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.ui.components.GameIcos

/** Display name string resource for a move. */
@StringRes
fun Move.lblRes(): Int = when (this) {
    Move.JAB -> R.string.move_jab
    Move.LUNGE -> R.string.move_lunge
    Move.PARRY -> R.string.move_parry
    Move.SLASH -> R.string.move_slash
    Move.REST -> R.string.move_rest
    Move.EXHAUSTION_STRIKE -> R.string.move_exh_strike
}

/** Display name string resource for a power-up type. */
@StringRes
fun PwrUpType.lblRes(): Int = when (this) {
    PwrUpType.LIGHTER -> R.string.powerup_lighter
    PwrUpType.WATER_BOTTLE -> R.string.powerup_water_bottle
}

/** Display name string resource for a trainer buff. */
@StringRes
fun TrnrBuff.lblRes(): Int = when (this) {
    TrnrBuff.MAX_HP -> R.string.trnr_buff_max_hp
    TrnrBuff.STAMINA -> R.string.trnr_buff_stam
    TrnrBuff.DEFENSE -> R.string.trnr_buff_defense
    TrnrBuff.DECK_LIMIT -> R.string.trnr_buff_deck_limit
}

/** Description string resource for a trainer buff. */
@StringRes
fun TrnrBuff.descRes(): Int = when (this) {
    TrnrBuff.MAX_HP -> R.string.trnr_buff_max_hp_desc
    TrnrBuff.STAMINA -> R.string.trnr_buff_stam_desc
    TrnrBuff.DEFENSE -> R.string.trnr_buff_defense_desc
    TrnrBuff.DECK_LIMIT -> R.string.trnr_buff_deck_limit_desc
}

/** Icon for a move. */
fun Move.icon(): ImageVector = when (this) {
    Move.JAB -> GameIcos.Jab
    Move.LUNGE -> GameIcos.Lunge
    Move.PARRY -> GameIcos.Parry
    Move.SLASH -> GameIcos.Slash
    Move.REST -> GameIcos.Rest
    Move.EXHAUSTION_STRIKE -> GameIcos.ExhStrike
}
