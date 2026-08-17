package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.home.AuthSessionService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.home.IosAuthSessionService
import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomService
import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentStorageService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosHomeCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosHomeDatabaseService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosHomeCryptoService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.room.IosHomeCommentRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.IosHomeCommentStorageService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.room.IosNewsRoomService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.home.ClipboardService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.clipboard.home.IosClipboardService
import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.home.IosNetworkMonitor
import org.koin.dsl.module

fun homeIosModule() = module {
    single<HomeNewsRoomService> { IosNewsRoomService() }
    single<HomeDatabaseService> { IosHomeDatabaseService() }
    single<HomeCommentStorageService> { IosHomeCommentStorageService() }
    single<HomeCommentRoomService> { IosHomeCommentRoomService() }
    single<HomeCommentDatabaseService> { IosHomeCommentDatabaseService() }
    single<HomeCryptoService> { IosHomeCryptoService() }
    single<ClipboardService> { IosClipboardService() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<AuthSessionService> { IosAuthSessionService() }
}
