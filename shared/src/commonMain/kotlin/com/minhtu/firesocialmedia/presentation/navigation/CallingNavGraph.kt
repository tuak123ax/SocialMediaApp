package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.core.domain.entity.call.OfferAnswer
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

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
    )
}
