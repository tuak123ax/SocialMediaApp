package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.notification.Notification
import com.minhtu.firesocialmedia.presentation.setting.notificationconfigs.NotificationConfigs
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.NotificationNavGraph

class NotificationNavGraphImpl : NotificationNavGraph {
    override fun getNotificationRoute(): String = NotificationNavGraph.NOTIFICATION_SCREEN_ROUTE

    override fun getNotificationConfigsRoute(): String = NotificationNavGraph.NOTIFICATION_CONFIGS_SCREEN_ROUTE

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        onNavigateToPostInformation: (NewsInstance) -> Unit,
        onNavigateToUserInformation: (UserInstance?) -> Unit,
        onNavigateToGroupDetails: (groupId: String) -> Unit,
        onNavigateBackFromConfigs: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = getNotificationRoute(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            Notification.NotificationScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                homeViewModel = homeViewModel,
                loadingViewModel = loadingViewModel,
                onNavigateToPostInformation = onNavigateToPostInformation,
                onNavigateToUserInformation = onNavigateToUserInformation,
                onNavigateToGroupDetails = onNavigateToGroupDetails
            )
        }

        navGraphBuilder.composable(
            route = getNotificationConfigsRoute(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            NotificationConfigs.NotificationConfigsScreen(
                paddingValues = paddingValues,
                onNavigateBack = onNavigateBackFromConfigs
            )
        }
    }
}


