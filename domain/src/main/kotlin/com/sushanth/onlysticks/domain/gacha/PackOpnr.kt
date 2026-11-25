// Booster pack opening ("gacha").
// (Comments in this file were written by AI - Claude.)
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

/** The cards that came out of one pack. */
data class PackRes(val type: PackType, val cardList: List<Card>)

/** Opens packs using [rng]. Defaults to a securely seeded RNG; tests can pass a fixed seed. */
class PackOpnr(private val rng: DeterministicRng = DeterministicRng(SecureRandom().nextLong())) {
    /**
     * Rolls PACK_SIZE cards. Each slot may be a trainer (max 1), a power-up (max 2), or otherwise a stick
     * whose rarity comes from the pack type's weights.
     */
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

    /** Picks a rarity using the pack's weight table. */
    fun rollRar(type: PackType): Rarity = Rarity.entries[rng.weightedIdx(type.rarWeights)]

    /** Random 16-hex-digit card id, e.g. "card-00ab12...". */
    private fun newId(): String = "card-%016x".format(rng.nextLong())
}
