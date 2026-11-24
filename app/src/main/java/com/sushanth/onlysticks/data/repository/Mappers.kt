package com.sushanth.onlysticks.data.repository

import com.sushanth.onlysticks.data.local.entity.CardEntity
import com.sushanth.onlysticks.data.local.entity.CardKind
import com.sushanth.onlysticks.data.local.entity.TrnrEntity
import com.sushanth.onlysticks.domain.model.BaseStats
import com.sushanth.onlysticks.domain.model.Card
import com.sushanth.onlysticks.domain.model.PwrUp
import com.sushanth.onlysticks.domain.model.Stick
import com.sushanth.onlysticks.domain.model.Trnr
import com.sushanth.onlysticks.domain.model.TuningFlag
import com.sushanth.onlysticks.domain.stats.effectiveStats
import com.sushanth.onlysticks.domain.trainer.TrainerProgression
import com.sushanth.onlysticks.domain.trainer.TrnrRecord

object CardMapper {
    fun toEntity(card: Card, createdAt: Long): CardEntity = when (card) {
        is Stick -> CardEntity(
            id = card.id,
            kind = CardKind.STICK,
            customName = card.customName,
            rarity = card.rarity,
            durability = card.base.durability,
            speed = card.base.speed,
            attack = card.base.attack,
            weightFlag = TuningFlag.WEIGHT in card.tuning,
            gripFlag = TuningFlag.GRIP in card.tuning,
            qualityFlag = TuningFlag.QUALITY in card.tuning,
            maxDurability = card.effectiveStats.durability,
            createdAt = createdAt,
            signature = "",
        )
        is PwrUp -> CardEntity(
            id = card.id,
            kind = CardKind.POWER_UP,
            customName = card.customName,
            powerUpType = card.type,
            createdAt = createdAt,
            signature = "",
        )
        is Trnr -> throw IllegalArgumentException("Trainers are stored in the trainers table")
    }
    fun toDomain(entity: CardEntity): Card? = when (entity.kind) {
        CardKind.STICK -> entity.rarity?.let { rarity ->
            Stick(
                id = entity.id,
                rarity = rarity,
                base = BaseStats(entity.durability, entity.speed, entity.attack),
                tuning = buildSet {
                    if (entity.weightFlag) add(TuningFlag.WEIGHT)
                    if (entity.gripFlag) add(TuningFlag.GRIP)
                    if (entity.qualityFlag) add(TuningFlag.QUALITY)
                },
                customName = entity.customName,
            )
        }
        CardKind.POWER_UP -> entity.powerUpType?.let { PwrUp(entity.id, it, entity.customName) }
    }
}
object TrnrMapper {
    fun toDomain(entity: TrnrEntity): Trnr =
        Trnr(id = entity.id, buff = entity.buff, affinity = entity.affinity, customName = entity.customName)
    fun toRecord(entity: TrnrEntity): TrnrRecord = TrnrRecord(toDomain(entity), entity.wins)
    fun toEntity(trainer: Trnr, createdAt: Long, wins: Int = 0): TrnrEntity = TrnrEntity(
        id = trainer.id,
        buff = trainer.buff,
        affinity = trainer.affinity,
        rank = TrainerProgression.rankFor(trainer.affinity),
        wins = wins,
        customName = trainer.customName,
        createdAt = createdAt,
    )
    fun update(entity: TrnrEntity, record: TrnrRecord): TrnrEntity = entity.copy(
        affinity = record.trainer.affinity,
        rank = TrainerProgression.rankFor(record.trainer.affinity),
        wins = record.wins,
        customName = record.trainer.customName,
    )
}
