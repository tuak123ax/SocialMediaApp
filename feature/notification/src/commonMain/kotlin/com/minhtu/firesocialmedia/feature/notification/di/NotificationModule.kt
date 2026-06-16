package com.minhtu.firesocialmedia.feature.notification.di

import com.minhtu.firesocialmedia.feature.notification.navigation.NotificationNavGraphImpl
import com.minhtu.firesocialmedia.feature.notification.presentation.notification.NotificationViewModel
import com.minhtu.firesocialmedia.feature.notification.presentation.setting.notificationconfigs.NotificationConfigsViewModel
import com.minhtu.firesocialmedia.presentation.navigation.NotificationNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun notificationModule() = module {
    viewModel { NotificationViewModel(get(), get(), get(), get()) }
    viewModel { NotificationConfigsViewModel() }

    single<NotificationNavGraph> { NotificationNavGraphImpl() }
}

