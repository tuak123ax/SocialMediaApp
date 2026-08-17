package com.minhtu.firesocialmedia.di

@Suppress("unused")
fun doInitAllKoin(platformContext: PlatformContext) {
    initKoin(
        platformContext,
        allFeatureModules() + listOf(
            appInitModule(), appInitIosModule(), authIosModule(), callingIosModule(), securityIosModule(),
            groupIosModule(), commentIosModule(), homeIosModule(), profileIosModule(),
            notificationIosModule(), friendIosModule()
        )
    )
}
