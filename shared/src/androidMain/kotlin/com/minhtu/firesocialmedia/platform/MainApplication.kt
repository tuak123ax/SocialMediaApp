package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable

actual object MainApplication {
    @Composable
    actual fun MainApp(context : Any){
        SetUpNavigation(context)
    }
}