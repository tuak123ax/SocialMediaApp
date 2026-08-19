package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.profile.NetworkMonitor
import com.minhtu.firesocialmedia.data.remote.service.auth.profile.AuthSessionService
import com.minhtu.firesocialmedia.data.local.service.crypto.ProfileCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.LikedPostRoomService
import com.minhtu.firesocialmedia.data.local.service.room.UserRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import com.minhtu.firesocialmedia.data.repository.NetworkRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.profile.UserRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.call.profile.CallRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.local.StoreUserFriendsLocalUseCaseImpl
import com.minhtu.firesocialmedia.data.repository.news.ProfileNewsRepositoryImpl
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.data.repository.ProfileFriendDbRepositoryImpl
import com.minhtu.firesocialmedia.domain.repository.NetworkRepository
import com.minhtu.firesocialmedia.domain.repository.ProfileFriendDbRepository
import com.minhtu.firesocialmedia.domain.repository.call.profile.CallRepository
import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.profile.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.GetNewsByUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserAvatarUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserStringFieldUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.profile.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.presentation.personalinformation.PersonalInformationViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.ProfileFriendViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel
import com.minhtu.firesocialmedia.presentation.profile.SessionViewModel
import com.minhtu.firesocialmedia.presentation.profile.EngagementViewModel
import com.minhtu.firesocialmedia.profile.presentation.loading.LoadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun profileModule() = module {
    // Note: ProfileNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.

    // Profile-owned repository
    single<NetworkRepository> {
        NetworkRepositoryImpl(get<NetworkMonitor>())
    }
    single<CallRepository> {
        CallRepositoryImpl(get<ProfileDatabaseService>())
    }
    single<UserRepository> {
        UserRepositoryImpl(
            get<AuthSessionService>(),
            get<ProfileDatabaseService>(),
            get<ProfileCryptoService>(),
            get<UserRoomService>(),
            get<LikedPostRoomService>(),
            get<NetworkMonitor>()
        )
    }
    single {
        StoreUserFriendsLocalUseCaseImpl(get<UserRoomService>())
    }
    factory { GetUserUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { SaveLikedPostUseCase(get()) }
    single<ProfileNewsRepository> {
        ProfileNewsRepositoryImpl(
            get<ProfileDatabaseService>(),
            get<NetworkMonitor>()
        )
    }
    factory { GetNewByIdUseCase(get()) }
    factory { GetNewsByUserUseCase(get()) }
    factory { DeleteNewsUseCase(get()) }
    factory { UpdateLikeCountForNewUseCase(get()) }
    factory { DeletePollUseCase(get()) }

    // Profile-owned use case
    factory { CheckCalleeAvailableUseCase(get()) }
    factory { CheckInternetConnectionUseCase(get()) }
    factory { UpdateUserAvatarUseCase(get<ProfileDatabaseService>()) }
    factory { UpdateUserBackgroundUseCase(get<ProfileDatabaseService>()) }
    factory { UpdateUserStringFieldUseCase(get<ProfileDatabaseService>()) }
    factory { VerifyCurrentPasswordUseCase(get<ProfileAuthService>()) }
    // SaveNotificationToDatabaseUseCase is bound by feature:notification's own Koin module;
    // resolved here via the flat app-level container since profile depends on feature:notification.
    factory { SaveLikeNotificationUseCase(get(), get()) }

    // Friend-request actions duplicated locally (eliminates profile → friend Gradle edge)
    single<ProfileFriendDbRepository> {
        ProfileFriendDbRepositoryImpl(get<ProfileDatabaseService>())
    }
    factory { ProfileSaveFriendUseCase(get()) }
    factory { ProfileSaveFriendRequestUseCase(get()) }

    // ViewModels
    // 3rd arg (SaveNotificationToDatabaseUseCase) resolves via feature:notification's Koin module.
    viewModel {
        UserInformationViewModel(
            get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
    viewModel {
        PersonalInformationViewModel(get(), get(), get(), get())
    }
    single { SessionViewModel(get(), get()) }
    single { EngagementViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ProfileFriendViewModel(get(), get()) }
    viewModel { LoadingViewModel() }
}