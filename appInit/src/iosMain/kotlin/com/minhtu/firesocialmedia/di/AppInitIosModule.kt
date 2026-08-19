package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.appinit.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AppInitDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosAppInitDatabaseService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.appinit.IosAuthSessionService
import org.koin.dsl.module

fun appInitIosModule() = module {
    single<AppInitDatabaseService> { IosAppInitDatabaseService() }
    single<AuthSessionService> { IosAuthSessionService() }
}
