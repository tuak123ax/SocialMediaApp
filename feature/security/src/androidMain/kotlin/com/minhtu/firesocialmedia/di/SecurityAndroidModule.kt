package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.AndroidSecurityAuthService
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.AndroidTwoFactorAuthService
import com.minhtu.firesocialmedia.android.service.serviceimpl.auth.security.AndroidAuthSessionService
import com.minhtu.firesocialmedia.android.service.serviceimpl.clipboard.security.AndroidClipboardService
import com.minhtu.firesocialmedia.android.service.serviceimpl.crypto.AndroidSecurityCryptoService
import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.TwoFactorAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.security.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.AndroidSecurityDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.SecurityDatabaseService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun securityAndroidModule() = module {
    single<TwoFactorAuthService> { AndroidTwoFactorAuthService() }
    single<SecurityCryptoService> { AndroidSecurityCryptoService(androidContext()) }
    single<SecurityDatabaseService> { AndroidSecurityDatabaseService(androidContext()) }
    single<SecurityAuthService> { AndroidSecurityAuthService() }
    single<ClipboardService> { AndroidClipboardService(androidContext()) }
    single<AuthSessionService> { AndroidAuthSessionService() }
}
