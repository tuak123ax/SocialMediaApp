package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.AndroidProfileCryptoService
import com.minhtu.firesocialmedia.android.service.serviceimpl.room.AndroidUserRoomService
import com.minhtu.firesocialmedia.data.local.dao.UserDao
import com.minhtu.firesocialmedia.data.local.service.crypto.ProfileCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.AndroidProfileAuthService
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.profile.AndroidAuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.profile.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidProfileDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.network.profile.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.profile.NetworkMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun profileAndroidModule() = module {
    single<UserRoomService> { AndroidUserRoomService(get<UserDao>()) }
    single<ProfileDatabaseService> { AndroidProfileDatabaseService() }
    single<ProfileCryptoService> { AndroidProfileCryptoService(androidContext()) }
    single<ProfileAuthService> { AndroidProfileAuthService() }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
