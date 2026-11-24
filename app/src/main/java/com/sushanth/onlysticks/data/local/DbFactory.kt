package com.sushanth.onlysticks.data.local

import android.content.Context
import androidx.room.Room
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

object DbFactory {
    fun create(context: Context, passphrase: ByteArray, name: String = AppDatabase.NAME): AppDatabase {
        System.loadLibrary("sqlcipher")
        return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
            .openHelperFactory(SupportOpenHelperFactory(passphrase))
            .addMigrations(*Migrations.ALL)
            .build()
    }
}
