package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract

interface SearchNavGraph {
    fun getSearchRoute(): String

    fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        onNavigateBack: () -> Unit,
        onNavigateToUserInformation: (UserInstance?) -> Unit,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToUploadNewsFeed: (updateNew: NewsInstance?) -> Unit
    )

    companion object {
        const val SEARCH_SCREEN_ROUTE = "SearchScreen"
    }
}

