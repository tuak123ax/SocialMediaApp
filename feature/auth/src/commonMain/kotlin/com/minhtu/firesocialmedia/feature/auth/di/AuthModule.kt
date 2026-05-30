package com.minhtu.firesocialmedia.feature.auth.di

import com.minhtu.firesocialmedia.core.domain.repository.AuthenticationRepository
import com.minhtu.firesocialmedia.core.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.forgotpassword.CheckIfEmailExistsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.forgotpassword.SendEmailResetPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckLocalAccountUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.CheckUserExistsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.HandleSignInGoogleResultUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.RememberPasswordUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signin.SignInUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.signup.SignUpUseCase
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.feature.auth.data.repository.AuthenticationRepositoryImpl
import com.minhtu.firesocialmedia.feature.auth.navigation.AuthNavGraphImpl
import com.minhtu.firesocialmedia.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.minhtu.firesocialmedia.feature.auth.presentation.signin.SignInViewModel
import com.minhtu.firesocialmedia.feature.auth.presentation.signup.SignUpViewModel
import com.minhtu.firesocialmedia.presentation.navigation.AuthNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun authModule() = module {
    // ── Repositories (singletons) ────────────────────//
    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(
            get<PlatformContext>().auth,
            get<PlatformContext>().database,
            get<PlatformContext>().crypto
        )
    }

    // Sign in Use Cases
    factory { CheckLocalAccountUseCase(get()) }
    factory { CheckUserExistsUseCase(get()) }
    factory { HandleSignInGoogleResultUseCase(get()) }
    factory { RememberPasswordUseCase(get()) }
    factory { SaveLoginActivityInfoUseCase(get()) }
    factory { SignInUseCase(get()) }

    // Sign up Use Cases
    factory { SignUpUseCase(get()) }

    // Forgot password use cases
    factory { CheckIfEmailExistsUseCase(get()) }
    factory { SendEmailResetPasswordUseCase(get()) }

    // Shared common use cases needed by auth ViewModels
    factory { GetCurrentUserUidUseCase(get()) }
    factory { GetUserUseCase(get()) }

    // ── ViewModels ────────────────────//
    viewModel { SignInViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get(), get()) }

    // SignInViewModel as singleton GoogleSignInHandler - shared between UI and platform launcher
    single<GoogleSignInHandler> {
        SignInViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }

    // Auth navigation graph
    single<AuthNavGraph> { AuthNavGraphImpl() }
}