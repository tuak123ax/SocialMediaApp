package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

interface NotificationNavGraph {
    fun getNotificationRoute(): String
    fun getNotificationConfigsRoute(): String

    fun registerRoutes(
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
    )

    companion object {
        const val NOTIFICATION_SCREEN_ROUTE = "NotificationScreen"
        const val NOTIFICATION_CONFIGS_SCREEN_ROUTE = "NotificationConfigsScreen"
    }
}

