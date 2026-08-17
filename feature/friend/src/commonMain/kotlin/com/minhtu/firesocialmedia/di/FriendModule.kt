package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.data.remote.service.auth.friend.AuthSessionService
import com.minhtu.firesocialmedia.data.repository.FriendDbRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.friend.UserRepositoryImpl
import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository
import com.minhtu.firesocialmedia.domain.repository.friend.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.friend.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.friend.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.presentation.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.friend.SessionViewModel
import org.koin.dsl.module

fun friendModule() = module {
    single<FriendDbRepository> {
        FriendDbRepositoryImpl(
            get<com.minhtu.firesocialmedia.data.remote.service.database.FriendDatabaseService>()
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            get<AuthSessionService>(),
            get<com.minhtu.firesocialmedia.data.remote.service.database.FriendDatabaseService>()
        )
    }

    factory { SaveFriendUseCase(get()) }
    factory { SaveFriendRequestUseCase(get()) }
    factory { GetUserUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }

    // Note: FriendNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.
    single { FriendViewModel(get(), get()) }
    single { SessionViewModel(get(), get()) }
}
