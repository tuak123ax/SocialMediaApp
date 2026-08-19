package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.di.PlatformContext

expect object MainApplication {
    @Composable
    fun MainApp(context: Any, platformContext: PlatformContext)

    @Composable
    fun MainAppWithDeepLink(context: Any, deepLink: String, platformContext: PlatformContext)

    @Composable
    fun MainAppFromNotification(
        context: Any,
        platformContext: PlatformContext,
        sessionId: String?,
        callerId: String?,
        calleeId: String?
    )
}

@Composable
expect fun SetUpNavigation(
    context: Any,
    platformContext: PlatformContext
)

@Composable
expect fun SetUpNavigation(
    context: Any,
    platformContext: PlatformContext,
    sessionId: String?,
    callerId: String?,
    calleeId: String?
)

@Composable
expect fun SetUpNavigationWithDeepLink(
    context: Any,
    deepLink: String,
    platformContext: PlatformContext
)
