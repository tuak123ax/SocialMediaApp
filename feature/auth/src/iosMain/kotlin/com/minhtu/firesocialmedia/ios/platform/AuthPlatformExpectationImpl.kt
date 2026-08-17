package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.domain.signin.GoogleSignInHandler
import com.minhtu.firesocialmedia.di.PlatformContext

@Composable
actual fun setupSignInLauncher(
    context: Any?,
    signInViewModel: GoogleSignInHandler,
    platformContext: PlatformContext
) {
    // No-op on iOS for Google Sign-In in this project setup
}
