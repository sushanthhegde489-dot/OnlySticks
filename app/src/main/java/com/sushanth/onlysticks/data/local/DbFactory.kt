// Opens the encrypted database.
// (Comments in this file were written by AI - Claude.)
package com.sushanth.onlysticks.data.local

import android.content.Context
import androidx.room.Room
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** Creates the Room database backed by SQLCipher. */
object DbFactory {
    /** Loads the native SQLCipher library and builds the database with [passphrase] and all migrations. */
    fun create(context: Context, passphrase: ByteArray, name: String = AppDatabase.NAME): AppDatabase {
        System.loadLibrary("sqlcipher")
        return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
            .openHelperFactory(SupportOpenHelperFactory(passphrase))
            .addMigrations(*Migrations.ALL)
            .build()
    }
}
