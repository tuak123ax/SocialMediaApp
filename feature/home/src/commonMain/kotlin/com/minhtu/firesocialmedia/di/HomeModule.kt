package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.navigation.HomeNavGraphImpl
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.navigation.HomeNavGraph
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModelContract
import org.koin.dsl.module

fun homeModule() = module {
    // Home navigation graph
    single<HomeNavGraph> { HomeNavGraphImpl() }

    // Register under contract interface so koinInject<ContractType>() resolves in commonMain.
    single<HomeViewModelContract> { HomeViewModel(get(), get(), get(), get()) }
    single<UploadNewfeedViewModelContract> {
        UploadNewfeedViewModel(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get()
        )
    }
}