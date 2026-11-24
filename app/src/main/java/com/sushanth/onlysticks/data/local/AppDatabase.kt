package com.sushanth.onlysticks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sushanth.onlysticks.data.local.dao.ActiveMatchDao
import com.sushanth.onlysticks.data.local.dao.CardDao
import com.sushanth.onlysticks.data.local.dao.CoinsLdgrDao
import com.sushanth.onlysticks.data.local.dao.DeckDao
import com.sushanth.onlysticks.data.local.dao.PlrProfDao
import com.sushanth.onlysticks.data.local.dao.PvpEscrwDao
import com.sushanth.onlysticks.data.local.dao.SettDao
import com.sushanth.onlysticks.data.local.dao.StoreBuyDao
import com.sushanth.onlysticks.data.local.dao.TrnrDao
import com.sushanth.onlysticks.data.local.entity.ActiveMatchEntity
import com.sushanth.onlysticks.data.local.entity.CardEntity
import com.sushanth.onlysticks.data.local.entity.CoinsLdgrEntity
import com.sushanth.onlysticks.data.local.entity.DeckEntity
import com.sushanth.onlysticks.data.local.entity.DeckSlotEntity
import com.sushanth.onlysticks.data.local.entity.PlrProfEntity
import com.sushanth.onlysticks.data.local.entity.PvpEscrwEntity
import com.sushanth.onlysticks.data.local.entity.SettEntity
import com.sushanth.onlysticks.data.local.entity.StoreBuyEntity
import com.sushanth.onlysticks.data.local.entity.TrnrEntity

@Database(
    entities = [
        CardEntity::class,
        DeckEntity::class,
        DeckSlotEntity::class,
        TrnrEntity::class,
        CoinsLdgrEntity::class,
        PlrProfEntity::class,
        ActiveMatchEntity::class,
        StoreBuyEntity::class,
        PvpEscrwEntity::class,
        SettEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao
    abstract fun trnrDao(): TrnrDao
    abstract fun coinsLdgrDao(): CoinsLdgrDao
    abstract fun plrProfDao(): PlrProfDao
    abstract fun activeMatchDao(): ActiveMatchDao
    abstract fun storeBuyDao(): StoreBuyDao
    abstract fun pvpEscrwDao(): PvpEscrwDao
    abstract fun settDao(): SettDao

    suspend fun clearPlrColl() {
        deckDao().deleteAll()
        cardDao().deleteAll()
        trnrDao().deleteAll()
        coinsLdgrDao().deleteAll()
        storeBuyDao().deleteAll()
    }

    companion object {
        const val NAME = "stickdeck.db"
    }
}
