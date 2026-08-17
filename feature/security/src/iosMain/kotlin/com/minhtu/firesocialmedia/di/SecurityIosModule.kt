package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.TwoFactorAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.security.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.IosSecurityDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.SecurityDatabaseService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.IosSecurityAuthService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.IosTwoFactorAuthService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.auth.security.IosAuthSessionService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.clipboard.security.IosClipboardService
import com.minhtu.firesocialmedia.ios.service.serviceimpl.crypto.IosSecurityCryptoService
import org.koin.dsl.module

fun securityIosModule() = module {
    single<TwoFactorAuthService> { IosTwoFactorAuthService() }
    single<SecurityCryptoService> { IosSecurityCryptoService() }
    single<SecurityDatabaseService> { IosSecurityDatabaseService() }
    single<SecurityAuthService> { IosSecurityAuthService() }
    single<ClipboardService> { IosClipboardService() }
    single<AuthSessionService> { IosAuthSessionService() }
}
