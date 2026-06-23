package com.minhtu.firesocialmedia.feature.comment.di

import com.minhtu.firesocialmedia.feature.comment.navigation.CommentNavGraphImpl
import com.minhtu.firesocialmedia.feature.comment.presentation.comment.CommentFeatureViewModel
import com.minhtu.firesocialmedia.presentation.comment.CommentScreenApi
import com.minhtu.firesocialmedia.presentation.comment.CommentViewModelContract
import org.koin.dsl.module

fun commentModule() = module {
    single<CommentScreenApi> { CommentNavGraphImpl() }
    single<CommentViewModelContract> {
        CommentFeatureViewModel(
            get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
