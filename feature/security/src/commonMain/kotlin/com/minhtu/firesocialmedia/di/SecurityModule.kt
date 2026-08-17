package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.security.AuthSessionService
import com.minhtu.firesocialmedia.domain.repository.SecuritySettingsRepository
import com.minhtu.firesocialmedia.domain.repository.SettingsRepository
import com.minhtu.firesocialmedia.domain.repository.ShowImageRepository
import com.minhtu.firesocialmedia.domain.repository.TwoFactorAuthRepository
import com.minhtu.firesocialmedia.domain.repository.security.UserRepository
import com.minhtu.firesocialmedia.data.repository.SecuritySettingsRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.SettingsRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.ShowImageRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.TwoFactorAuthRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.security.UserRepositoryImpl
import com.minhtu.firesocialmedia.data.remote.service.auth.TwoFactorAuthService
import com.minhtu.firesocialmedia.data.remote.service.auth.SecurityAuthService
import com.minhtu.firesocialmedia.data.remote.service.clipboard.security.ClipboardService
import com.minhtu.firesocialmedia.data.remote.service.database.SecurityDatabaseService
import com.minhtu.firesocialmedia.domain.usecases.common.security.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.BuildOtpAuthUrlUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ChangePasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.CopyUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.DeleteLoginSessionUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Disable2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Enable2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.FetchLoginHistoryListUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.GenerateSecretFor2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Get2FAVerifiedStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.LogoutSessionUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ObserveSessionStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.StopObserveSessionStatusUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserTimestampUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateVerify2FASuccessUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.ValidateNewPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.Verify2FAUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyBackupCodeUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.data.local.service.crypto.SecurityCryptoService
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.security.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.showimage.DownloadImageUseCase
import com.minhtu.firesocialmedia.presentation.changepassword.ChangePasswordViewModel
import com.minhtu.firesocialmedia.presentation.loginhistory.LoginHistoryViewModel
import com.minhtu.firesocialmedia.presentation.settings.AccountViewModel
import com.minhtu.firesocialmedia.presentation.settings.SecuritySettingsViewModel
import com.minhtu.firesocialmedia.presentation.showimage.ShowImageViewModel
import com.minhtu.firesocialmedia.presentation.twofa.BackUpCodeViewModel
import com.minhtu.firesocialmedia.presentation.twofa.TwoFAViewModel
import com.minhtu.firesocialmedia.presentation.twofa.TwoFactorEnabledViewModel
import com.minhtu.firesocialmedia.presentation.twofa.VerifyOTPViewModel
import com.minhtu.firesocialmedia.security.presentation.loading.LoadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun securityModule() = module {
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get<SecurityAuthService>(),
            get<SecurityDatabaseService>(),
            get<SecurityCryptoService>()
        )
    }

    single<SecuritySettingsRepository> {
        SecuritySettingsRepositoryImpl(
            get<SecurityAuthService>(),
            get<SecurityDatabaseService>(),
            get<ClipboardService>()
        )
    }

    single<ShowImageRepository> {
        ShowImageRepositoryImpl(
            get<SecurityDatabaseService>()
        )
    }

    single<TwoFactorAuthRepository> {
        TwoFactorAuthRepositoryImpl(
            get<TwoFactorAuthService>(),
            get<SecurityDatabaseService>()
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(get<AuthSessionService>())
    }

    // ── Use Cases ────────────────────//
    factory { BuildOtpAuthUrlUseCase() }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }
    factory { CopyUseCase(get()) }
    factory { DeleteLoginSessionUseCase(get()) }
    factory { Disable2FAUseCase(get(), get()) }
    factory { Enable2FAUseCase(get()) }
    factory { FetchLoginHistoryListUseCase(get()) }
    factory { GenerateSecretFor2FAUseCase(get()) }
    factory { Get2FAVerifiedStatusUseCase(get()) }
    factory { LogoutSessionUseCase(get()) }
    factory { ObserveSessionStatusUseCase(get()) }
    factory { StopObserveSessionStatusUseCase(get()) }
    factory { UpdateUserTimestampUseCase(get()) }
    factory { UpdateVerify2FASuccessUseCase(get()) }
    factory { ValidateNewPasswordUseCase() }
    factory { Verify2FAUseCase(get(), get()) }
    factory { VerifyBackupCodeUseCase(get(), get()) }
    factory { VerifyCurrentPasswordUseCase(get()) }
    factory { DownloadImageUseCase(get()) }
    factory { ClearAccountUseCase(get<SecurityCryptoService>()) }
    factory { ClearLocalDataUseCase(get()) }

    // ── ViewModels ────────────────────//
    viewModel { SecuritySettingsViewModel(get()) }
    viewModel { ChangePasswordViewModel(get(), get(), get()) }
    viewModel { LoginHistoryViewModel(get(), get(), get(), get(), get()) }
    viewModel { BackUpCodeViewModel(get(), get()) }
    viewModel { TwoFactorEnabledViewModel(get(), get()) }
    viewModel { TwoFAViewModel(get(), get(), get()) }
    viewModel { VerifyOTPViewModel(get(), get(), get()) }
    viewModel { ShowImageViewModel(get()) }
    viewModel { LoadingViewModel() }

    single {
        AccountViewModel(get(), get())
    }

    // Note: SecurityNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.
}
