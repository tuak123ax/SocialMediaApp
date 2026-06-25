package com.minhtu.firesocialmedia.feature.search.navigation

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
import com.minhtu.firesocialmedia.feature.search.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.SearchNavGraph

class SearchNavGraphImpl : SearchNavGraph {
    override fun getSearchRoute(): String = SearchNavGraph.SEARCH_SCREEN_ROUTE

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        onNavigateBack: () -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToUploadNewsFeed: (updateNew: NewsInstance?) -> Unit
    ) {
        navGraphBuilder.composable(
            route = getSearchRoute(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            Search.SearchScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                paddingValues = paddingValues,
                homeViewModel = homeViewModel,
                localImageLoaderValue = localImageLoaderValue,
                onNavigateBack = onNavigateBack,
                onNavigateToUserInformation = onNavigateToUserInformation,
                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                onNavigateToCommentScreen = onNavigateToCommentScreen,
                onNavigateToUploadNewsFeed = onNavigateToUploadNewsFeed
            )
        }
    }
}

