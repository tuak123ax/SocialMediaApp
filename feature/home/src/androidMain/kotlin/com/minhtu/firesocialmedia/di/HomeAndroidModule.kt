package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.home.AndroidAuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.auth.home.AuthSessionService
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.AndroidHomeCryptoService
import com.minhtu.firesocialmedia.android.service.serviceimpl.room.AndroidHomeCommentRoomService
import com.minhtu.firesocialmedia.android.service.serviceimpl.room.AndroidNewsRoomService
import com.minhtu.firesocialmedia.data.local.dao.HomeCommentDao
import com.minhtu.firesocialmedia.data.local.dao.NewsDao
import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomService
import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidHomeCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidHomeCommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidHomeDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.home.ClipboardService
import com.minhtu.firesocialmedia.android.service.serviceimpl.clipboard.home.AndroidClipboardService
import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.home.NetworkMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun homeAndroidModule() = module {
    single<HomeNewsRoomService> { AndroidNewsRoomService(androidContext(), get<NewsDao>()) }
    single<HomeDatabaseService> { AndroidHomeDatabaseService(androidContext()) }
    single<HomeCommentStorageService> { AndroidHomeCommentStorageService() }
    single<HomeCommentRoomService> { AndroidHomeCommentRoomService(get<HomeCommentDao>()) }
    single<HomeCommentDatabaseService> { AndroidHomeCommentDatabaseService() }
    single<HomeCryptoService> { AndroidHomeCryptoService(androidContext()) }
    single<ClipboardService> { AndroidClipboardService(androidContext()) }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
