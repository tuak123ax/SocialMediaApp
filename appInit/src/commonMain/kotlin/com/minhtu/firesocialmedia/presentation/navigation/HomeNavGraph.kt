package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.home.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.search.entity.user.UserInstance

interface HomeNavGraph {
    /** Route string for the Home screen — used by Navigation.kt for navigation calls. */
    fun getHomeRoute(): String

    /** Route string for the UploadNewsfeed screen — used by Navigation.kt for navigation calls. */
    fun getUploadNewsfeedRoute(): String

    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        navigateToCallingScreen: Boolean,
        platformContext: PlatformContext?,
        deepLink: String = "",
        onNavigateToUploadNews: (updateNew: NewsInstance?) -> Unit,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToSearch: () -> Unit,
        onNavigateToSignIn: () -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToCallingScreen: suspend (CallingRequestData) -> Unit,
        onNavigateToCallingScreenWithUI: suspend () -> Unit,
        onObservePhoneCall: () -> Unit,
        incomingCallRequest: CallingRequestData?,
        onNavigateToPostInformation: () -> Unit,
        onShareNews: (String, NewsInstance) -> Unit,
        onNavigateToJoinGroup: () -> Unit
    )

    /**
     * Registers the UploadNewsfeed composable route into the NavGraph.
     * The implementation in :feature:home self-injects UploadNewfeedViewModel via Koin.
     */
    fun registerUploadNewsfeedRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        context: Any,
        getUpdateNew: () -> NewsInstance?
    )

    companion object {
        const val HOME_SCREEN_NAME = "HomeScreen"
        const val UPLOAD_NEWSFEED_SCREEN_NAME = "UploadNewsfeedScreen"
    }
}
