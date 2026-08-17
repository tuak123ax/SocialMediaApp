package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.friend.AndroidAuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.auth.friend.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidFriendDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.FriendDatabaseService
import org.koin.dsl.module

fun friendAndroidModule() = module {
    single<FriendDatabaseService> { AndroidFriendDatabaseService() }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
