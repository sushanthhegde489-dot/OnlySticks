package com.sushanth.onlysticks.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `active_match` (`matchId` TEXT NOT NULL, `botTier` INTEGER NOT NULL, " +
                    "`trainerId` TEXT, `startedAt` INTEGER NOT NULL, PRIMARY KEY(`matchId`))",
            )
        }
    }
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `store_purchases` (`weekStart` INTEGER NOT NULL, `slot` INTEGER NOT NULL, " +
                    "`boughtAt` INTEGER NOT NULL, PRIMARY KEY(`weekStart`, `slot`))",
            )
            db.execSQL("CREATE TABLE IF NOT EXISTS `pvp_escrow` (`cardId` TEXT NOT NULL, PRIMARY KEY(`cardId`))")
            db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `googleEmail` TEXT")
            db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `lastBackupAt` INTEGER")
        }
    }
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
            db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `isAdmin` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `tutorialDone` INTEGER NOT NULL DEFAULT 0")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
