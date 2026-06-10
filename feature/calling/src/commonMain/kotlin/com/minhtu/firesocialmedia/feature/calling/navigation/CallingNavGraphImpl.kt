package com.minhtu.firesocialmedia.feature.calling.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.feature.calling.presentation.audiocall.Calling
import com.minhtu.firesocialmedia.feature.calling.presentation.audiocall.CallingViewModel
import com.minhtu.firesocialmedia.feature.calling.presentation.videocall.VideoCall
import com.minhtu.firesocialmedia.feature.calling.presentation.videocall.VideoCallViewModel
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.CallingNavGraph
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.utils.NavigationHandler
import org.koin.compose.viewmodel.koinViewModel

class CallingNavGraphImpl : CallingNavGraph {

    override fun getCallingRoute(): String = Calling.getScreenName()

    override fun getVideoCallRoute(): String = VideoCall.getScreenName()

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        localImageLoaderValue: ProvidedValue<*>,
        getCallee: () -> UserInstance?,
        getCaller: () -> UserInstance?,
        getSessionId: () -> String,
        getRemoteOffer: () -> OfferAnswer?,
        getRemoteVideoOffer: () -> OfferAnswer?,
        onSetRemoteVideoOffer: (OfferAnswer?) -> Unit,
        onSetSessionId: (String) -> Unit,
        onNavigateToVideoCall: () -> Unit,
        onStopCallAndNavigateBack: () -> Unit,
        getHomeRoute: () -> String
    ) {
        navGraphBuilder.composable(
            route = Calling.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val callingViewModel: CallingViewModel = koinViewModel()
            val navigationHandler = com.minhtu.firesocialmedia.platform.rememberNavigationHandler(navController)
            val caller = getCaller()
            val callee = getCallee()
            if (caller != null && callee != null) {
                Calling.CallingScreen(
                    localImageLoaderValue = localImageLoaderValue,
                    sessionId = getSessionId(),
                    callee = callee,
                    caller = caller,
                    currentUser = homeViewModel.currentUser,
                    remoteOffer = getRemoteOffer(),
                    navigateToCallingScreenFromNotification = com.minhtu.firesocialmedia.core.domain.entity.call.SharedCallData.navigateToCallingScreenFromNotification,
                    callingViewModel = callingViewModel,
                    homeViewModel = homeViewModel,
                    navHandler = navigationHandler,
                    onStopCallAndNavigateBack = {
                        if (navigationHandler.getCurrentRoute() != getHomeRoute()) {
                            navigationHandler.navigateBack()
                        }
                        homeViewModel.resetCallEvent()
                    },
                    onNavigateToVideoCall = { sessionID, videoOffer ->
                        callingViewModel.setPendingVideoOfferForAccept(videoOffer, sessionID)
                        onSetSessionId(sessionID)
                        onSetRemoteVideoOffer(videoOffer)
                        onNavigateToVideoCall()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            } else {
                showToast("Cannot get caller and callee information. Cannot show calling screen!")
                navController.popBackStack()
            }
        }

        navGraphBuilder.composable(
            route = VideoCall.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val videoCallViewModel: VideoCallViewModel = koinViewModel()
            val navigationHandler = com.minhtu.firesocialmedia.platform.rememberNavigationHandler(navController)
            VideoCall.VideoCallScreen(
                sessionId = getSessionId(),
                caller = getCaller(),
                callee = getCallee(),
                currentUserId = homeViewModel.currentUser?.uid,
                remoteVideoOffer = getRemoteVideoOffer(),
                videoCallViewModel = videoCallViewModel,
                loadingViewModel = loadingViewModel,
                onNavigateBack = { navigationHandler.navigateBack() }
            )
        }
    }
}


