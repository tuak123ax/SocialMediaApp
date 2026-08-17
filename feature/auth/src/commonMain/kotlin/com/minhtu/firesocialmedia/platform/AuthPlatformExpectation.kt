package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.signin.GoogleSignInHandler

@Composable
expect fun setupSignInLauncher(context: Any?, signInViewModel: GoogleSignInHandler, platformContext: PlatformContext)
