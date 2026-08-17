package com.minhtu.firesocialmedia.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.search.entity.user.UserInstance
import com.minhtu.firesocialmedia.search.entity.user.toDto as searchUserToDto
import com.minhtu.firesocialmedia.search.entity.user.toSearchUser
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance as ProfileNewsInstance
import com.minhtu.firesocialmedia.profile.entity.user.toProfileUser
import com.minhtu.firesocialmedia.profile.entity.user.toDto as profileUserToDto
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.entity.bridge.toProfileUserDto
import kotlinx.coroutines.flow.MutableStateFlow
import com.minhtu.firesocialmedia.presentation.personalinformation.PersonalInformation
import com.minhtu.firesocialmedia.presentation.personalinformation.PersonalInformationViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformation
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformationViewModel
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.profile.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.ProfileNavGraph
import org.koin.compose.viewmodel.koinViewModel

// Composition-root-only conversions between home's canonical NewsInstance and
// profile's local NewsInstance.
private fun NewsInstance.toProfileNews(): ProfileNewsInstance = ProfileNewsInstance(
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
    decentralizationType = decentralizationType?.let {
        when (it) {
            is com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Public ->
                com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType.Public
            is com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.OnlyFriends ->
                com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType.OnlyFriends
            is com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Private ->
                com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType.Private
        }
    },
    groupId = groupId,
    type = type,
    pollId = pollId
)

private fun ProfileNewsInstance.toCoreNews(): NewsInstance = NewsInstance(
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
    decentralizationType = decentralizationType?.let {
        when (it) {
            is com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType.Public ->
                com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Public
            is com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType.OnlyFriends ->
                com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.OnlyFriends
            is com.minhtu.firesocialmedia.profile.entity.core.DecentralizationType.Private ->
                com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Private
        }
    },
    groupId = groupId,
    type = type,
    pollId = pollId
)

class ProfileNavGraphImpl : ProfileNavGraph {

    override fun getUserInformationRoute(): String = UserInformation.getScreenName()

    override fun registerUserInformationRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
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
                onImagePicked = { uri -> userInformationViewModel.updateCover(uri) }
            )
            val isFriend = selectedUser?.friends?.contains(homeViewModel.currentUser?.uid) == true
            val profileNewsFeed = remember { MutableStateFlow<List<ProfileNewsInstance>>(emptyList()) }
            LaunchedEffect(homeViewModel.allNews) {
                homeViewModel.allNews.collect { news ->
                    profileNewsFeed.value = news.map { it.toProfileNews() }
                }
            }
            UserInformation.UserInformationScreen(
                imagePicker = picker,
                user = selectedUser?.searchUserToDto()?.toProfileUserDto()?.toProfileUser(),
                isCurrentUser = selectedUser?.uid == homeViewModel.currentUser?.uid,
                isFriend = isFriend,
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                newsFeed = profileNewsFeed,
                userInformationViewModel = userInformationViewModel,
                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                onNavigateBack = {
                    userInformationViewModel.resetOldData()
                    navController.popBackStack()
                },
                onNavigateToUploadNewsfeed = { updateNew -> onNavigateToUploadNewsfeed(updateNew?.toCoreNews()) },
                onNavigateToCallingScreen = { user -> onNavigateToCallingScreen(user?.profileUserToDto()?.toAppInitUserDto()?.toSearchUser()) },
                onNavigateToCommentScreen = { selectedNew -> onNavigateToCommentScreen(selectedNew.toCoreNews()) }
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
                onImagePicked = { uri -> personalInformationViewModel.onAvatarPicked(uri) }
            )
            if (selectedUser != null) {
                PersonalInformation.PersonalInformationScreen(
                    currentUser = selectedUser.searchUserToDto().toProfileUserDto().toProfileUser(),
                    imagePicker = picker,
                    paddingValues = paddingValues,
                    personalInformationViewModel = personalInformationViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
