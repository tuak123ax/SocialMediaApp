package com.minhtu.firesocialmedia.platform

import androidx.compose.runtime.Composable
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.call.SharedCallData
import com.minhtu.firesocialmedia.presentation.navigation.SetUpNavigation as CommonSetUpNavigation

@Composable
actual fun SetUpNavigation(context: Any, platformContext : PlatformContext) {
    CommonSetUpNavigation(context = context, platformContext = platformContext)
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
    CommonSetUpNavigation(context = context, platformContext = platformContext)
}