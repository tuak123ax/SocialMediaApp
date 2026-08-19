package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.search.entity.user.UserInstance

interface CallingNavGraph {

    /** Route for the audio calling screen. */
    fun getCallingRoute(): String

    /** Route for the video call screen. */
    fun getVideoCallRoute(): String

    /**
     * Registers both the Calling and VideoCall composable routes into the NavGraph.
     */
    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        localImageLoaderValue: ProvidedValue<*>,
        getCallee: () -> UserInstance?,
        getCaller: () -> UserInstance?,
        getSessionId: () -> String,
        getRemoteOffer: () -> OfferAnswer?,
        getRemoteVideoOffer: () -> OfferAnswer?,
        navigateToCallingScreenFromNotification: Boolean = false,
        onSetRemoteVideoOffer: (OfferAnswer?) -> Unit,
        onSetSessionId: (String) -> Unit,
        onNavigateToVideoCall: () -> Unit,
        onStopCallAndNavigateBack: () -> Unit,
        getHomeRoute: () -> String
    )
}
