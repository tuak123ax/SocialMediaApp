package com.minhtu.firesocialmedia

import androidx.compose.runtime.Composable

class MainApplication {
    companion object {
        @Composable
        fun MainApp(context : Any){
            SetUpNavigation(context)
        }
    }
}

// Define the expected function (platform-specific implementation will be in androidMain & iosMain)
//@Composable
//expect fun SetUpNavigation(context: Any)