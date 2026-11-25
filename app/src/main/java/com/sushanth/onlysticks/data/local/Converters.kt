// Room type converters for enums.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local

import androidx.room.TypeConverter
import com.sushanth.onlysticks.data.local.entity.CardKind
import com.sushanth.onlysticks.domain.model.PwrUpType
import com.sushanth.onlysticks.domain.model.Rarity
import com.sushanth.onlysticks.domain.model.TrnrBuff

/** Stores enums as their names in the database. */
class Converters {
    @TypeConverter
    fun fromRar(value: Rarity?): String? = value?.name

    @TypeConverter
    fun toRar(value: String?): Rarity? = value?.let(Rarity::valueOf)

    @TypeConverter
    fun fromPwrUpType(value: PwrUpType?): String? = value?.name

    @TypeConverter
    fun toPwrUpType(value: String?): PwrUpType? = value?.let(PwrUpType::valueOf)

    @TypeConverter
    fun fromTrnrBuff(value: TrnrBuff?): String? = value?.name

    @TypeConverter
    fun toTrnrBuff(value: String?): TrnrBuff? = value?.let(TrnrBuff::valueOf)

    @TypeConverter
    fun fromCardKind(value: CardKind?): String? = value?.name

    @TypeConverter
    fun toCardKind(value: String?): CardKind? = value?.let(CardKind::valueOf)
}
