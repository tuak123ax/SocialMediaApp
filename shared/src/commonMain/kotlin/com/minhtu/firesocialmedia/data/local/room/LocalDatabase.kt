package com.minhtu.firesocialmedia.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minhtu.firesocialmedia.data.local.dao.NewDao
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.entity.EnumConverters
import com.minhtu.firesocialmedia.data.local.entity.NewEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class, NewEntity::class, NotificationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun newsDao(): NewDao
    abstract fun notificationDao(): NotificationDao
}