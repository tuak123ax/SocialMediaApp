package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.ToastController.ToastHost
import com.minhtu.firesocialmedia.presentation.navigation.SetUpNavigation as CommonSetUpNavigation

@Composable
actual fun SetUpNavigation(context: Any, platformContext : PlatformContext) {
    Box {
        CommonSetUpNavigation(context = context, platformContext = platformContext)
        ToastHost()
    }
}

@Composable
actual fun SetUpNavigation(context : Any,
                           platformContext: PlatformContext,
                           sessionId : String?,
                           callerId : String?,
                           calleeId : String?) {
    val hasCallData = sessionId != null && callerId != null && calleeId != null
    Box{
        CommonSetUpNavigation(
            context = context,
            platformContext = platformContext,
            callSessionId = sessionId,
            callCallerId = callerId,
            callCalleeId = calleeId,
            navigateToCallingScreenFromNotification = hasCallData
        )
    }
}

@Composable
actual fun SetUpNavigationWithDeepLink(context : Any,
                                       deepLink : String,
                                       platformContext: PlatformContext) {
    Box{
        CommonSetUpNavigation(context = context, platformContext = platformContext, deepLink = deepLink)
        ToastHost()
    }
}