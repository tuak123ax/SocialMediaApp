package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.application.interactor.NewsInteractorImpl
import com.minhtu.firesocialmedia.data.remote.service.auth.home.AuthSessionService
import com.minhtu.firesocialmedia.application.interactor.UserInteractorImpl
import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.data.local.service.room.HomeUserRoomService
import com.minhtu.firesocialmedia.data.repository.HomeDbRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.NewsRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.home.UserRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.local.ClearNewsLocalDataUseCaseImpl
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.home.ClearAccountUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearLocalDataUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.ClearLocalFriendsUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.DeleteNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetLatestNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveCurrentUserInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.SearchUserByNameUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNewsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreUserFriendsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.home.GetAllMembersInGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.home.GetGroupConfigsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.home.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeleteAllDraftPostsUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeleteDraftPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.FetchPollUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.LoadAllVotersUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.LoadMyVotesUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SaveNewToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SubmitVoteUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.UpdateNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.home.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.sync.LoadNewsPostedWhenOfflineUseCase
import com.minhtu.firesocialmedia.domain.usecases.sync.SyncDataUseCase
import com.minhtu.firesocialmedia.presentation.home.HomeAccountViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.postinformation.PostInformationViewModel
import com.minhtu.firesocialmedia.presentation.share.ShareViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.home.presentation.loading.LoadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun homeModule() = module {
    // -- Repository --
    single<NewsRepository> {
        NewsRepositoryImpl(
            get<com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService>(),
            get<HomeNewsRoomService>(),
            get<NetworkMonitor>()
        )
    }

    single<HomeDbRepository> {
        HomeDbRepositoryImpl(
            get<com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService>(),
            get<HomeNewsRoomService>(),
            get<HomeUserRoomService>(),
            get<NetworkMonitor>()
        )
    }
    single {
        ClearNewsLocalDataUseCaseImpl(get<HomeNewsRoomService>())
    }
    single<UserRepository> {
        UserRepositoryImpl(
            get<AuthSessionService>(),
            get<com.minhtu.firesocialmedia.data.remote.service.database.HomeDatabaseService>()
        )
    }

    // -- Use Cases --
    factory { ClearLocalDataUseCase(get(), get()) }
    factory { ClearLocalFriendsUseCase(get()) }
    factory { DeleteNewsFromDatabaseUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }
    factory { GetLatestNewsUseCase(get()) }
    factory { GetUserUseCase(get()) }
    factory { SaveCurrentUserInfoUseCase(get<HomeCryptoService>()) }
    factory { SaveLikedPostUseCase(get()) }
    factory { SearchUserByNameUseCase(get()) }
    factory { StoreNewsToRoomUseCase(get()) }
    factory { StoreUserFriendsToRoomUseCase(get()) }
    factory { UpdateFCMTokenUseCase(get()) }
    factory { UpdateLikeCountForNewUseCase(get()) }

    // Newsfeed Use Cases
    factory { DeleteAllDraftPostsUseCase(get()) }
    factory { DeleteDraftPostUseCase(get()) }
    factory { DeletePollUseCase(get()) }
    factory { FetchPollUseCase(get()) }
    factory { LoadAllVotersUseCase(get()) }
    factory { LoadMyVotesUseCase(get()) }
    factory { SaveNewToDatabaseUseCase(get()) }
    factory { SubmitVoteUseCase(get()) }
    factory { UpdateNewsFromDatabaseUseCase(get()) }

    // Notification-related in home context
    factory { FindNewByIdInDbUseCase(get()) }
    // SaveNotificationToDatabaseUseCase is bound by feature:notification's own Koin module;
    // resolved here via the flat app-level container since home depends on feature:notification.

    // Group-related use cases used within home context (uploading news to a group)
    factory { GetAllMembersInGroupUseCase(get()) }
    factory { GetGroupConfigsUseCase(get()) }
    factory { SaveNewToGroupUseCase(get()) }

    // Sync Use Cases
    factory { LoadNewsPostedWhenOfflineUseCase(get()) }
    factory { SyncDataUseCase(get(), get()) }

    // Account-related use case (logout flow)
    factory { ClearAccountUseCase(get<HomeCryptoService>()) }

    // -- Interactor --
    factory<NewsInteractor> {
        NewsInteractorImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory<UserInteractor> {
        UserInteractorImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // Note: HomeNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.

    // Register under contract interface so koinInject<ContractType>() resolves in commonMain.
    single { HomeViewModel(get(), get(), get()) }
    single { ShareViewModel(get(), get(), get()) }
    single {
        UploadNewfeedViewModel(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get()
        )
    }
    viewModel { PostInformationViewModel(get()) }
    viewModel { HomeAccountViewModel(get()) }
    viewModel { LoadingViewModel() }
}
