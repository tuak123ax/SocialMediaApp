package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.auth.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.auth.SignInLookupService
import com.minhtu.firesocialmedia.data.remote.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.IosAuthDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.security.IpInfoRemoteDataSource
import com.minhtu.firesocialmedia.di.createHttpClient
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.auth.IosAuthSessionService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.IosAuthService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.IosSignInLookupService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosAuthCryptoService
import com.minhtu.firesocialmedia.platform.AppConfig
import org.koin.dsl.module

fun authIosModule() = module {
    single<AuthCryptoService> { IosAuthCryptoService() }
    single<SignInLookupService> { IosSignInLookupService() }
    single { IpInfoRemoteDataSource(createHttpClient(), AppConfig.ipInfoApiKey) }
    single<AuthDatabaseService> { IosAuthDatabaseService() }
    single<AuthService> { IosAuthService() }
    single<AuthSessionService> { IosAuthSessionService() }
}
