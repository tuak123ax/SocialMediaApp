package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.notification.NetworkMonitor
import com.minhtu.firesocialmedia.application.interactor.NotificationInteractorImpl
import com.minhtu.firesocialmedia.data.remote.service.auth.notification.AuthSessionService
import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationRemoteService
import com.minhtu.firesocialmedia.data.repository.NotificationRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.notification.UserRepositoryImpl
import com.minhtu.firesocialmedia.data.repository.local.PreloadNotificationsUseCaseImpl
import com.minhtu.firesocialmedia.data.repository.local.StoreNotificationsLocalUseCaseImpl
import com.minhtu.firesocialmedia.data.repository.news.NotificationNewsRepositoryImpl
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository
import com.minhtu.firesocialmedia.domain.repository.news.NotificationNewsRepository
import com.minhtu.firesocialmedia.domain.usecases.local.StoreNotificationsLocalUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNotificationsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.GetAllNotificationOfUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetUserUseCase
import com.minhtu.firesocialmedia.presentation.notification.NotificationViewModel
import com.minhtu.firesocialmedia.presentation.notification.NotificationsViewModel
import com.minhtu.firesocialmedia.presentation.notification.SessionViewModel
import com.minhtu.firesocialmedia.presentation.setting.notificationconfigs.NotificationConfigsViewModel
import com.minhtu.firesocialmedia.notification.presentation.loading.LoadingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun notificationModule() = module {
    // -- Repository --
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            get<NotificationRemoteService>(),
            get<NotificationRoomService>(),
            get<NetworkMonitor>()
        )
    }
    single<StoreNotificationsLocalUseCase> {
        StoreNotificationsLocalUseCaseImpl(get<NotificationRoomService>())
    }
    single<NotificationNewsRepository> {
        NotificationNewsRepositoryImpl(
            get<NotificationDatabaseService>(),
            get<NetworkMonitor>()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            get<AuthSessionService>(),
            get<NotificationDatabaseService>()
        )
    }

    // -- Use Cases --
    factory { DeleteAllNotificationsUseCase(get()) }
    factory { DeleteNotificationFromDatabaseUseCase(get()) }
    factory { GetAllNotificationOfUserUseCase(get()) }
    factory { SaveNotificationToDatabaseUseCase(get()) }
    factory { UpdateIsReadStatusOfNotificationUseCase(get()) }
    factory { StoreNotificationsToRoomUseCase(get()) }
    factory { FindNewByIdInDbUseCase(get()) }
    factory { GetUserUseCase(get()) }
    factory { GetCurrentUserUidUseCase(get()) }

    // -- Interactor --
    factory<NotificationInteractor> {
        NotificationInteractorImpl(
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { NotificationViewModel(get(), get(), get(), get()) }
    viewModel { NotificationConfigsViewModel() }
    viewModel { LoadingViewModel() }

    single {
        NotificationsViewModel(get<GetCurrentUserUidUseCase>(), get())
    }
    single {
        PreloadNotificationsUseCaseImpl(get<NotificationsViewModel>())
    }

    single { SessionViewModel(get(), get()) }

    // Note: NotificationNavGraph binding moved to appInit's own Koin module (Phase 3) —
    // it's a composition-root/cross-feature nav contract, not feature-owned infra.
}
