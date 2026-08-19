package com.minhtu.firesocialmedia.di

import androidx.room.Room.databaseBuilder
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.appinit.AndroidAuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.auth.appinit.AuthSessionService
import com.minhtu.firesocialmedia.data.local.dao.CommentDao
import com.minhtu.firesocialmedia.data.local.dao.HomeCommentDao
import com.minhtu.firesocialmedia.data.local.dao.NewsDao
import com.minhtu.firesocialmedia.data.local.dao.NotificationDao
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.room.AppDatabase
import com.minhtu.firesocialmedia.data.local.room.MIGRATION_7_8
import com.minhtu.firesocialmedia.data.local.room.MIGRATION_8_9
import com.minhtu.firesocialmedia.data.local.room.MIGRATION_9_10
import com.minhtu.firesocialmedia.data.local.room.MIGRATION_10_11
import com.minhtu.firesocialmedia.data.remote.service.database.AppInitDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidAppInitDatabaseService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Builds the single shared [AppDatabase] (Phase 3, Option A) and registers each Dao as a bare
 * Koin singleton. Each feature's own Koin module (e.g. HomeAndroidModule, ProfileAndroidModule)
 * consumes the relevant Dao via `get()` to build its own split RoomService. See instruction.md
 * Phase 3 for the full rationale.
 */
fun appInitAndroidModule() = module {
    single {
        databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "RoomLocalDatabase"
        ).addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11).build()
    }
    single<AppInitDatabaseService> { AndroidAppInitDatabaseService() }
    single<UserDao> { get<AppDatabase>().userDao() }
    single<NewsDao> { get<AppDatabase>().newsDao() }
    single<NotificationDao> { get<AppDatabase>().notificationDao() }
    single<CommentDao> { get<AppDatabase>().commentDao() }
    single<HomeCommentDao> { get<AppDatabase>().homeCommentDao() }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
