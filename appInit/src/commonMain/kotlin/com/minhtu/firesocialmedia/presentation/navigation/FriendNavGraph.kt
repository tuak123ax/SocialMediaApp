package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.search.entity.user.UserInstance

interface FriendNavGraph {
    fun getFriendRoute(): String

    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        onNavigateToUserInformation: (UserInstance) -> Unit
    )

    companion object {
        const val FRIEND_SCREEN_ROUTE = "FriendScreen"
    }
}
