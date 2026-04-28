package com.minhtu.firesocialmedia.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.call.SharedCallData
import com.minhtu.firesocialmedia.domain.entity.home.deeplinks.DeepLinksData
import com.minhtu.firesocialmedia.presentation.toast.ToastController.ToastHost
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
    if(sessionId != null && callerId != null && calleeId != null) {
        SharedCallData.sessionId = sessionId
        SharedCallData.callerId = callerId
        SharedCallData.calleeId = calleeId
        SharedCallData.navigateToCallingScreenFromNotification = true
    }
    Box{
        CommonSetUpNavigation(context = context, platformContext = platformContext)
    }
}

@Composable
actual fun SetUpNavigationWithDeepLink(context : Any,
                                       deepLink : String,
                                       platformContext: PlatformContext) {
    DeepLinksData.deepLink = deepLink
    Box{
        CommonSetUpNavigation(context = context, platformContext = platformContext)
        ToastHost()
    }
}