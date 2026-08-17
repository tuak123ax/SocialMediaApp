package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.appinit.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.database.AppInitDatabaseService
import com.minhtu.firesocialmedia.data.repository.appinit.UserRepositoryImpl
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.search.SessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun searchModule() = module {
    single<UserRepository> {
        UserRepositoryImpl(get<AuthSessionService>(), get<AppInitDatabaseService>())
    }
    factory { GetUserUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { SearchUserByNameUseCase(get()) }

    viewModel { SearchViewModel() }
    single { SessionViewModel(get(), get(), get()) }

    // Note: SearchNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.
}

