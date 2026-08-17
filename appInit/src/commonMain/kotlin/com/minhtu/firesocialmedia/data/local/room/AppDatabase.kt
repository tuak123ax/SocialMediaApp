package com.minhtu.firesocialmedia.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minhtu.firesocialmedia.data.local.dao.CommentDao
import com.minhtu.firesocialmedia.data.local.dao.HomeCommentDao
import com.minhtu.firesocialmedia.data.local.dao.NewsDao
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.entity.CommentEntity
import com.minhtu.firesocialmedia.data.local.entity.EnumConverters
import com.minhtu.firesocialmedia.data.local.entity.HomeCommentEntity
import com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity

/**
 * Single shared Room database (Phase 3, Option A). Entities/Daos are owned by their respective
 * feature modules (profile, home, notification, comment) and imported here; appInit is the only
 * module allowed to depend on all of them, so it hosts the composed [AppDatabase] plus the
 * cross-feature [EnumConverters]. Renamed from core's old `LocalDatabase` during the Phase 3 Room
 * split (see instruction.md).
 */
@Database(
    entities = [UserEntity::class, NewsEntity::class, NotificationEntity::class, LikedPostEntity::class, CommentEntity::class, HomeCommentEntity::class],
    version = 11,
    exportSchema = true
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun newsDao(): NewsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun commentDao() : CommentDao
    abstract fun homeCommentDao(): HomeCommentDao
}
