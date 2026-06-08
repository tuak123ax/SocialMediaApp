package com.minhtu.firesocialmedia.feature.profile.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.feature.profile.presentation.personalinformation.PersonalInformation
import com.minhtu.firesocialmedia.feature.profile.presentation.personalinformation.PersonalInformationViewModel
import com.minhtu.firesocialmedia.feature.profile.presentation.userinformation.UserInformation
import com.minhtu.firesocialmedia.feature.profile.presentation.userinformation.UserInformationViewModel
import com.minhtu.firesocialmedia.platform.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.ProfileNavGraph
import org.koin.compose.viewmodel.koinViewModel

class ProfileNavGraphImpl : ProfileNavGraph {

    override fun getUserInformationRoute(): String = UserInformation.getScreenName()

    override fun registerUserInformationRoute(
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
    ) {
        navGraphBuilder.composable(
            route = UserInformation.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val userInformationViewModel: UserInformationViewModel = koinViewModel()
            val selectedUser = getSelectedUser()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> userInformationViewModel.updateCover(uri) },
                onVideoPicked = {}
            )
            val isFriend = selectedUser?.friends?.contains(homeViewModel.currentUser?.uid) == true
            UserInformation.UserInformationScreen(
                imagePicker = picker,
                user = selectedUser,
                isCurrentUser = selectedUser == homeViewModel.currentUser,
                isFriend = isFriend,
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                homeViewModel = homeViewModel,
                userInformationViewModel = userInformationViewModel,
                loadingViewModel = loadingViewModel,
                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                onNavigateBack = {
                    userInformationViewModel.resetOldData()
                    navController.popBackStack()
                },
                onNavigateToUploadNewsfeed = onNavigateToUploadNewsfeed,
                onNavigateToCallingScreen = onNavigateToCallingScreen,
                onNavigateToCommentScreen = onNavigateToCommentScreen
            )
        }
    }

    override fun getPersonalInformationRoute(): String = PersonalInformation.getScreenName()

    override fun registerPersonalInformationRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        context: Any,
        getSelectedUser: () -> UserInstance?
    ) {
        navGraphBuilder.composable(
            route = PersonalInformation.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val personalInformationViewModel: PersonalInformationViewModel = koinViewModel()
            val selectedUser = getSelectedUser()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> personalInformationViewModel.onAvatarPicked(uri) },
                onVideoPicked = {}
            )
            if (selectedUser != null) {
                PersonalInformation.PersonalInformationScreen(
                    currentUser = selectedUser,
                    imagePicker = picker,
                    paddingValues = paddingValues,
                    personalInformationViewModel = personalInformationViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
