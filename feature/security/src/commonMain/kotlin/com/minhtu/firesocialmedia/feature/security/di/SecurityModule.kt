package com.minhtu.firesocialmedia.feature.security.di

import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.DeleteLoginSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Disable2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.GenerateSecretFor2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.LogoutSessionUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.Verify2FAUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyBackupCodeUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.feature.security.navigation.SecurityNavGraphImpl
import com.minhtu.firesocialmedia.feature.security.presentation.changepassword.ChangePasswordViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.loginhistory.LoginHistoryViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.settings.SecuritySettingsViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.BackUpCodeViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.TwoFAViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.TwoFactorEnabledViewModel
import com.minhtu.firesocialmedia.feature.security.presentation.twofa.VerifyOTPViewModel
import com.minhtu.firesocialmedia.presentation.navigation.SecurityNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun securityModule() = module {
    // ── Use Cases ────────────────────//
    factory { BuildOtpAuthUrlUseCase() }
    factory { ChangePasswordUseCase(get()) }
    factory { CopyUseCase(get()) }
    factory { DeleteLoginSessionUseCase(get()) }
    factory { Disable2FAUseCase(get()) }
    factory { Enable2FAUseCase(get()) }
    factory { FetchLoginHistoryListUseCase(get()) }
    factory { GenerateSecretFor2FAUseCase(get()) }
    factory { LogoutSessionUseCase(get()) }
    factory { UpdateUserTimestampUseCase(get()) }
    factory { UpdateVerify2FASuccessUseCase(get()) }
    factory { ValidateNewPasswordUseCase() }
    factory { Verify2FAUseCase(get()) }
    factory { VerifyBackupCodeUseCase(get()) }
    factory { VerifyCurrentPasswordUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { ClearAccountUseCase(get()) }

    // ── ViewModels ────────────────────//
    viewModel { SecuritySettingsViewModel(get()) }
    viewModel { ChangePasswordViewModel(get(), get(), get()) }
    viewModel { LoginHistoryViewModel(get(), get(), get(), get(), get()) }
    viewModel { BackUpCodeViewModel(get(), get()) }
    viewModel { TwoFactorEnabledViewModel(get(), get()) }
    viewModel { TwoFAViewModel(get(), get(), get()) }
    viewModel { VerifyOTPViewModel(get(), get(), get()) }

    // ── Navigation graph ────────────────────//
    single<SecurityNavGraph> { SecurityNavGraphImpl() }
}

