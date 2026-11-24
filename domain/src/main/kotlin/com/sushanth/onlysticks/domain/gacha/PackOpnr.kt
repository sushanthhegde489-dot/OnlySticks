package com.sushanth.onlysticks.domain.gacha

import com.sushanth.onlysticks.domain.config.Cfg
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TrnrBuff
import com.sushanth.onlysticks.domain.rng.DeterministicRng
import com.sushanth.onlysticks.domain.stats.StatGen
import java.security.SecureRandom

data class PackRes(val type: PackType, val cardList: List<Card>)

class PackOpnr(private val rng: DeterministicRng = DeterministicRng(SecureRandom().nextLong())) {
    fun open(type: PackType): PackRes {
        var trnrs = 0
        var pwrUps = 0
        val cardList = List(Cfg.PACK_SIZE) {
            if (trnrs < Cfg.Packs.MAX_TRAINERS && rng.nextDouble() < Cfg.Packs.TRAINER_CHANCE) {
                trnrs++
                Trnr(newId(), TrnrBuff.entries[rng.nextInt(0, TrnrBuff.entries.size)])
            } else if (pwrUps < Cfg.Packs.MAX_POWER_UPS && rng.nextDouble() < Cfg.Packs.POWER_UP_CHANCES[pwrUps]) {
                pwrUps++
                PwrUp(newId(), PwrUpType.entries[rng.nextInt(0, PwrUpType.entries.size)])
            } else {
                val id = newId()
                val rarity = rollRar(type)
                Stick(id, rarity, StatGen.roll(rarity, rng))
            }
        }
        return PackRes(type, cardList)
    }

    fun rollRar(type: PackType): Rarity = Rarity.entries[rng.weightedIdx(type.rarWeights)]

    private fun newId(): String = "card-%016x".format(rng.nextLong())
}
