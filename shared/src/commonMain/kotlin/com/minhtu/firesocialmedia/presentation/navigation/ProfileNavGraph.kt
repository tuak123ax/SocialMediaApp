package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

interface ProfileNavGraph {

    fun getUserInformationRoute(): String

    fun registerUserInformationRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        context: Any,
        getSelectedUser: () -> UserInstance?,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToCallingScreen: (user: UserInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToUploadNewsfeed: (updateNew: NewsInstance?) -> Unit
    )

    fun getPersonalInformationRoute(): String

    fun registerPersonalInformationRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        context: Any,
        getSelectedUser: () -> UserInstance?
    )
}
