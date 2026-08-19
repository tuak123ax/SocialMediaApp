package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.comment.NetworkMonitor
import com.minhtu.firesocialmedia.application.interactor.CommentInteractorImpl
import com.minhtu.firesocialmedia.data.remote.service.database.CommentDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.database.CommentStorageService
import com.minhtu.firesocialmedia.data.local.service.room.CommentRoomService
import com.minhtu.firesocialmedia.data.repository.CommentDbRepositoryImpl
import com.minhtu.firesocialmedia.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.data.repository.local.ClearCommentLocalDataUseCaseImpl
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.CommentRepository
import com.minhtu.firesocialmedia.domain.repository.comment.UserRepository
import com.minhtu.firesocialmedia.data.repository.comment.UserRepositoryImpl
import com.minhtu.firesocialmedia.domain.usecases.comment.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.DeleteSubCommentFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.GetAllCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveSubCommentToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.data.repository.CommentRepositoryImpl
import com.minhtu.firesocialmedia.data.remote.service.clipboard.comment.ClipboardService
import com.minhtu.firesocialmedia.navigation.CommentNavGraphImpl
import com.minhtu.firesocialmedia.presentation.comment.CommentFeatureViewModel
import org.koin.dsl.module

fun commentModule() = module {
    single<CommentRepository> {
        CommentRepositoryImpl(
            get<CommentDatabaseService>()
        )
    }
    single<CommentDbRepository> {
        CommentDbRepositoryImpl(
            get<CommentDatabaseService>(),
            get<CommentStorageService>(),
            get<CommentRoomService>(),
            get<NetworkMonitor>()
        )
    }
    single {
        ClearCommentLocalDataUseCaseImpl(get<CommentRoomService>())
    }
    single<UserRepository> {
        UserRepositoryImpl(get<CommentDatabaseService>())
    }

    // Use cases for CommentInteractorImpl
    factory { SaveCommentToDatabaseUseCase(get()) }
    factory { SaveSubCommentToDatabaseUseCase(get()) }
    factory { DeleteCommentFromDatabaseUseCase(get()) }
    factory { DeleteSubCommentFromDatabaseUseCase(get()) }
    factory { GetAllCommentsUseCase(get()) }

    // Use cases for CommentFeatureViewModel
    factory { SaveLikedCommentsUseCase(get()) }
    factory { UpdateCommentCountForNewUseCase(get()) }
    factory { UpdateReplyCountForCommentUseCase(get()) }
    factory { UpdateLikeCountForCommentUseCase(get()) }
    factory { UpdateLikeCountForSubCommentUseCase(get()) }
    // SaveNotificationToDatabaseUseCase is bound by feature:notification's own Koin module;
    // resolved here via the flat app-level container since comment depends on feature:notification.
    factory { GetUserUseCase(get()) }

    factory<CommentInteractor> { CommentInteractorImpl(get(), get(), get(), get(), get()) }

    single { CommentNavGraphImpl() }
    single {
        CommentFeatureViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get<ClipboardService>()
        )
    }
}
