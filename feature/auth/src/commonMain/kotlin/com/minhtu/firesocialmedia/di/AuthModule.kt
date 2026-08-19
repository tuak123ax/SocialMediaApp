package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository
import com.minhtu.firesocialmedia.data.repository.auth.UserRepositoryImpl
import com.minhtu.firesocialmedia.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.auth.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.domain.usecases.signup.SignUpUseCase
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.AuthService
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.SignInLookupService
import com.minhtu.firesocialmedia.data.remote.auth.service.auth.auth.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.auth.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.data.remote.auth.service.security.IpInfoRemoteDataSource
import com.minhtu.firesocialmedia.data.repository.AuthenticationRepositoryImpl
import com.minhtu.firesocialmedia.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.presentation.information.InformationViewModel
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun authModule() = module {
    // ── Repositories (singletons) ────────────────────//
    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(
            get<AuthService>(),
            get<AuthDatabaseService>(),
            get<SignInLookupService>(),
            get<AuthCryptoService>()
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            get<AuthSessionService>(),
            get<AuthDatabaseService>()
        )
    }

    // Sign in Use Cases
    factory { CheckLocalAccountUseCase(get()) }
    factory { CheckUserExistsUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { GetFCMTokenUseCase(get<AuthCryptoService>()) }
    factory { GetUserUseCase(get()) }
    factory { HandleSignInGoogleResultUseCase(get()) }
    factory { RememberPasswordUseCase(get()) }
    factory { SaveLoginActivityInfoUseCase(get<AuthDatabaseService>(), get<IpInfoRemoteDataSource>()) }
    factory { SignInUseCase(get()) }

    // Sign up Use Cases
    factory { SignUpUseCase(get()) }

    // Information Use Cases (sign-up flow)
    factory { SaveSignUpInformationUseCase(get()) }

    // Forgot password use cases
    factory { CheckIfEmailExistsUseCase(get()) }
    factory { SendEmailResetPasswordUseCase(get()) }

    // ── ViewModels ────────────────────//
    viewModel { SignInViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get(), get()) }
    viewModel { InformationViewModel(get(), get(), get(), get()) }
    viewModel { LoadingViewModel() }

    // SignInViewModel as singleton GoogleSignInHandler - shared between UI and platform launcher
    single<GoogleSignInHandler> {
        SignInViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }

    // Note: AuthNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.
}