package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.friend.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.FriendDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosFriendDatabaseService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.friend.IosAuthSessionService
import org.koin.dsl.module

fun friendIosModule() = module {
    single<FriendDatabaseService> { IosFriendDatabaseService() }
    single<AuthSessionService> { IosAuthSessionService() }
}
