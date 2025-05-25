package com.minhtu.firesocialmedia

import androidx.compose.runtime.Composable

actual object MainApplication {
    @Composable
    actual fun MainApp(context : Any){
        SetUpNavigation(context)
    }
}

// Define the expected function (platform-specific implementation will be in androidMain & iosMain)
//@Composable
//expect fun SetUpNavigation(context: Any)