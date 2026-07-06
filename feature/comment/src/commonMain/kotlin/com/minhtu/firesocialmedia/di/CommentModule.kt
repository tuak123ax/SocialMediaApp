package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.navigation.CommentNavGraphImpl
import com.minhtu.firesocialmedia.presentation.comment.CommentFeatureViewModel
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
