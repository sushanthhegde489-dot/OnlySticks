package com.sushanth.onlysticks.ui.card

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.sushanth.onlysticks.domain.appraisal.Appr
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.ui.theme.OnlySticksTheme

internal object PrvwCards {
    fun stick(rarity: Rarity): Stick {
        val mid = { r: IntRange -> (r.first + r.last) / 2 }
        return Stick(
            id = "preview-${rarity.code}",
            rarity = rarity,
            base = BaseStats(mid(rarity.durability), mid(rarity.speed), mid(rarity.attack)),
            tuning = if (rarity == Rarity.RARE) setOf(TuningFlag.GRIP) else emptySet(),
        )
    }
    val lighter = PwrUp("preview-lighter", PwrUpType.LIGHTER)
    val trainer = Trnr("preview-trainer", TrnrBuff.MAX_HP, affinity = 320)
}

internal class RarProv : PreviewParameterProvider<Rarity> {
    override val values = Rarity.entries.asSequence()
}

@Preview(name = "Light", widthDp = 260)
@Preview(name = "Dark", widthDp = 260, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Font 200%", widthDp = 260, fontScale = 2f)
@Composable
internal fun PokemonCardRarPrvw(@PreviewParameter(RarProv::class) rarity: Rarity) {
    OnlySticksTheme {
        val stick = PrvwCards.stick(rarity)
        PokemonCardV(
            card = stick,
            apprVal = Appr.value(stick),
            trnrSynergy = rarity.ordinal % 2 == 0,
            onNickChange = {},
            modifier = Modifier.width(260.dp),
        )
    }
}

@Preview(name = "Power-up", widthDp = 260)
@Preview(name = "Trainer dark", widthDp = 260, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PokemonCardItemPrvw() {
    OnlySticksTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PokemonCardV(PrvwCards.lighter, 120, trnrSynergy = false, onNickChange = {})
            PokemonCardV(PrvwCards.trainer, 500, trnrSynergy = true, onNickChange = {})
        }
    }
}
