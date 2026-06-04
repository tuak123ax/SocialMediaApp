package com.minhtu.firesocialmedia.feature.home.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.domain.entity.call.CallingRequestData
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.feature.home.presentation.home.Home
import com.minhtu.firesocialmedia.feature.home.presentation.uploadnewsfeed.UploadNewsfeed
import com.minhtu.firesocialmedia.platform.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.HomeNavGraph
import com.minhtu.firesocialmedia.presentation.uploadnewsfeed.UploadNewfeedViewModelContract
import org.koin.compose.koinInject

class HomeNavGraphImpl : HomeNavGraph {

    override fun getHomeRoute(): String = Home.getScreenName()

    override fun getUploadNewsfeedRoute(): String = UploadNewsfeed.getScreenName()

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        navigateToCallingScreen: Boolean,
        platformContext: PlatformContext?,
        onNavigateToUploadNews: (updateNew: NewsInstance?) -> Unit,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToSearch: () -> Unit,
        onNavigateToSignIn: () -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToCallingScreen: suspend (CallingRequestData) -> Unit,
        onNavigateToCallingScreenWithUI: suspend () -> Unit,
        onNavigateToPostInformation: () -> Unit,
        onShareNews: (String, NewsInstance) -> Unit,
        onNavigateToJoinGroup: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = Home.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            Home.HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                homeViewModel,
                loadingViewModel,
                navigateToCallingScreen,
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                onNavigateToUploadNews = onNavigateToUploadNews,
                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToSignIn = onNavigateToSignIn,
                onNavigateToUserInformation = onNavigateToUserInformation,
                onNavigateToCommentScreen = onNavigateToCommentScreen,
                onNavigateToCallingScreen = onNavigateToCallingScreen,
                onNavigateToCallingScreenWithUI = onNavigateToCallingScreenWithUI,
                onNavigateToPostInformation = onNavigateToPostInformation,
                onShareNews = onShareNews,
                onNavigateToJoinGroup = onNavigateToJoinGroup,
                platform = platformContext
            )
        }
    }

    override fun registerUploadNewsfeedRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        context: Any,
        getUpdateNew: () -> NewsInstance?
    ) {
        navGraphBuilder.composable(
            route = UploadNewsfeed.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val uploadNewsfeedViewModel: UploadNewfeedViewModelContract = koinInject()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> uploadNewsfeedViewModel.updateImage(uri) },
                onVideoPicked = { uri -> uploadNewsfeedViewModel.updateVideo(uri) }
            )
            UploadNewsfeed.UploadNewsfeedScreen(
                paddingValues,
                imagePicker = picker,
                localImageLoaderValue,
                homeViewModel = homeViewModel,
                uploadNewsfeedViewModel = uploadNewsfeedViewModel,
                loadingViewModel = loadingViewModel,
                updateNew = getUpdateNew(),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
