package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.AndroidAuthService
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.AndroidSignInLookupService
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.AndroidAuthCryptoService
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.auth.AndroidAuthSessionService
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.SignInLookupService
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.auth.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.auth.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.data.remote.auth.service.security.IpInfoRemoteDataSource
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidAuthDatabaseService
import com.minhtu.firesocialmedia.platform.AppConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun authAndroidModule() = module {
    single<AuthCryptoService> { AndroidAuthCryptoService(androidContext()) }
    single<SignInLookupService> { AndroidSignInLookupService() }
    single { IpInfoRemoteDataSource(createHttpClient(), AppConfig.ipInfoApiKey) }
    single<AuthDatabaseService> { AndroidAuthDatabaseService(androidContext()) }
    single<AuthService> { AndroidAuthService() }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
