package com.minhtu.firesocialmedia.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

@Suppress("unused")
fun initKoin(platformContext: PlatformContext, extraModules: List<Module> = emptyList()) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    PlatformContextHolder.instance = platformContext
    startKoin {
        modules(listOf(appModule()) + extraModules)
    }
}
