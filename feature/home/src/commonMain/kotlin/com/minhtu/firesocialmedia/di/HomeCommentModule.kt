package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.home.NetworkMonitor
import com.minhtu.firesocialmedia.application.interactor.HomeCommentInteractorImpl
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.HomeCommentStorageService
import com.minhtu.firesocialmedia.data.local.service.room.HomeCommentRoomService
import com.minhtu.firesocialmedia.data.repository.HomeCommentDbRepositoryImpl
import com.minhtu.firesocialmedia.domain.interactor.comment.HomeCommentInteractor
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.HomeCommentRepository
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeDeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeDeleteSubCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeGetAllCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveSubCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.data.repository.HomeCommentRepositoryImpl
import com.minhtu.firesocialmedia.data.remote.service.clipboard.home.ClipboardService
import com.minhtu.firesocialmedia.presentation.comment.HomeCommentFeatureViewModel
import org.koin.dsl.module

/**
 * Home's own independent DI wiring for its cloned comment stack, mirroring feature/comment's
 * `commentModule()`. Kept entirely within :feature:home so home no longer needs
 * `implementation(project(":feature:comment"))` (see instruction.md).
 */
fun homeCommentModule() = module {
    single<HomeCommentRepository> {
        HomeCommentRepositoryImpl(
            get<HomeCommentDatabaseService>()
        )
    }
    single<HomeCommentDbRepository> {
        HomeCommentDbRepositoryImpl(
            get<HomeCommentDatabaseService>(),
            get<HomeCommentStorageService>(),
            get<HomeCommentRoomService>(),
            get<NetworkMonitor>()
        )
    }

    // Use cases for HomeCommentInteractorImpl
    factory { HomeSaveCommentToDatabaseUseCase(get()) }
    factory { HomeSaveSubCommentToDatabaseUseCase(get()) }
    factory { HomeDeleteCommentFromDatabaseUseCase(get()) }
    factory { HomeDeleteSubCommentFromDatabaseUseCase(get()) }
    factory { HomeGetAllCommentsUseCase(get()) }

    // Use cases for HomeCommentFeatureViewModel
    factory { HomeSaveLikedCommentsUseCase(get()) }
    factory { HomeUpdateCommentCountForNewUseCase(get()) }
    factory { HomeUpdateReplyCountForCommentUseCase(get()) }
    factory { HomeUpdateLikeCountForCommentUseCase(get()) }
    factory { HomeUpdateLikeCountForSubCommentUseCase(get()) }
    // Note: SaveNotificationToDatabaseUseCase (notification.home) is already registered by
    // HomeModule.kt's homeModule() - reused here as-is, not re-registered.

    factory<HomeCommentInteractor> { HomeCommentInteractorImpl(get(), get(), get(), get(), get()) }

    single {
        HomeCommentFeatureViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get<ClipboardService>()
        )
    }
}
