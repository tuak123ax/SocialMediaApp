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
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser
import com.minhtu.firesocialmedia.domain.entity.user.notification.UserInstance as NotificationUserInstance
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDto as notificationUserToDto
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.presentation.notification.Notification
import com.minhtu.firesocialmedia.presentation.setting.notificationconfigs.NotificationConfigs
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.NotificationNavGraph

// Composition-root-only conversion: notification's local NewsInstance -> home's NewsInstance,
// which is the canonical News type at the composition root.
private fun com.minhtu.firesocialmedia.notification.entity.news.NewsInstance.toCoreNews(): NewsInstance {
    val coreDecentralizationType = when (decentralizationType) {
        is com.minhtu.firesocialmedia.notification.entity.core.DecentralizationType.Public ->
            com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Public
        is com.minhtu.firesocialmedia.notification.entity.core.DecentralizationType.Private ->
            com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Private
        is com.minhtu.firesocialmedia.notification.entity.core.DecentralizationType.OnlyFriends ->
            com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.OnlyFriends
        null -> null
    }
    return NewsInstance(
        id = id,
        posterId = posterId,
        posterName = posterName,
        avatar = avatar,
        message = message,
        image = image,
        video = video,
        isVisible = isVisible,
        likeCount = likeCount,
        commentCount = commentCount,
        timePosted = timePosted,
        localPath = localPath,
        shareContentId = shareContentId,
        decentralizationType = coreDecentralizationType,
        groupId = groupId,
        type = type,
        pollId = pollId
    )
}

class NotificationNavGraphImpl : NotificationNavGraph {
    override fun getNotificationRoute(): String = NotificationNavGraph.NOTIFICATION_SCREEN_ROUTE

    override fun getNotificationConfigsRoute(): String = NotificationNavGraph.NOTIFICATION_CONFIGS_SCREEN_ROUTE

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
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
                onNavigateToPostInformation = { new -> onNavigateToPostInformation(new.toCoreNews()) },
                onNavigateToUserInformation = { user: NotificationUserInstance? -> onNavigateToUserInformation(user?.notificationUserToDto()?.toAppInitUserDto()?.toSearchUser()) },
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


