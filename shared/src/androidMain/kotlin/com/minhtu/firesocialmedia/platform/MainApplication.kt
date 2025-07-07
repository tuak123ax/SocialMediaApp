package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.di.PlatformContext

actual object MainApplication {
    @Composable
    actual fun MainApp(context : Any, platformContext: PlatformContext){
        SetUpNavigation(context, platformContext)
    }
}