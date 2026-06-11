package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.feature.auth.di.authModule
import com.minhtu.firesocialmedia.feature.group.di.groupModule
import com.minhtu.firesocialmedia.feature.home.di.homeModule
import com.minhtu.firesocialmedia.feature.profile.di.profileModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

@Suppress("unused")
fun initKoin(platformContext: PlatformContext) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    PlatformContextHolder.instance = platformContext
    startKoin {
        modules(appModule(), authModule(), homeModule(), profileModule(), groupModule())
    }
}