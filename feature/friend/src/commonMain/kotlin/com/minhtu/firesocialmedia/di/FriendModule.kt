package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.navigation.FriendNavGraphImpl
import com.minhtu.firesocialmedia.presentation.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.friend.FriendViewModelContract
import com.minhtu.firesocialmedia.presentation.navigation.FriendNavGraph
import org.koin.dsl.module

fun friendModule() = module {
    single<FriendNavGraph> { FriendNavGraphImpl() }
    single<FriendViewModelContract> { FriendViewModel(get(), get()) }
}
