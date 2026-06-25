package com.minhtu.firesocialmedia.feature.friend.di

import com.minhtu.firesocialmedia.feature.friend.navigation.FriendNavGraphImpl
import com.minhtu.firesocialmedia.feature.friend.presentation.friend.FriendViewModel
import com.minhtu.firesocialmedia.presentation.friend.FriendViewModelContract
import com.minhtu.firesocialmedia.presentation.navigation.FriendNavGraph
import org.koin.dsl.module

fun friendModule() = module {
    single<FriendNavGraph> { FriendNavGraphImpl() }
    single<FriendViewModelContract> { FriendViewModel(get(), get()) }
}
