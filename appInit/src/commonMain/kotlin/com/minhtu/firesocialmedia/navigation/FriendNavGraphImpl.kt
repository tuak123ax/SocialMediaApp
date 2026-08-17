package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser
import com.minhtu.firesocialmedia.friend.entity.user.toDto
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.presentation.friend.Friend
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.FriendNavGraph

class FriendNavGraphImpl : FriendNavGraph {
    override fun getFriendRoute(): String = FriendNavGraph.FRIEND_SCREEN_ROUTE

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        onNavigateToUserInformation: (UserInstance) -> Unit
    ) {
        navGraphBuilder.composable(
            route = getFriendRoute(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            Friend.FriendScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                onNavigateToUserInformation = { friendUser -> onNavigateToUserInformation(friendUser.toDto().toAppInitUserDto().toSearchUser()) }
            )
        }
    }
}
