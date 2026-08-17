package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.crypto.ProfileCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService
import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.profile.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.IosProfileDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.IosProfileAuthService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.profile.IosAuthSessionService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosProfileCryptoService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.room.IosUserRoomService
import com.minhtu.firesocialmedia.network.profile.NetworkMonitor
import com.minhtu.firesocialmedia.core.connectivity.profile.IosNetworkMonitor
import org.koin.dsl.module

fun profileIosModule() = module {
    single<UserRoomService> { IosUserRoomService() }
    single<ProfileDatabaseService> { IosProfileDatabaseService() }
    single<ProfileCryptoService> { IosProfileCryptoService() }
    single<ProfileAuthService> { IosProfileAuthService() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<AuthSessionService> { IosAuthSessionService() }
}
