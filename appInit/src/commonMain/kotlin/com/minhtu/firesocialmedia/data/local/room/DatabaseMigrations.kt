package com.minhtu.firesocialmedia.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE UserFriends ADD COLUMN phone TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE UserFriends ADD COLUMN background TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE News ADD COLUMN type TEXT")
        connection.execSQL("ALTER TABLE News ADD COLUMN pollId TEXT")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `HomeComments` (" +
                "`id` TEXT NOT NULL, `posterId` TEXT NOT NULL, `posterName` TEXT NOT NULL, " +
                "`avatar` TEXT NOT NULL, `message` TEXT NOT NULL, `video` TEXT NOT NULL, " +
                "`image` TEXT NOT NULL, `likeCount` INTEGER NOT NULL, `commentCount` INTEGER NOT NULL, " +
                "`timePosted` INTEGER NOT NULL, `selectedNewId` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_HomeComments_id` ON `HomeComments` (`id`)"
        )
    }
}
