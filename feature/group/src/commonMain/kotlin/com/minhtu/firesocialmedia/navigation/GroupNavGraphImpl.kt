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
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.presentation.creategroup.CreateGroup
import com.minhtu.firesocialmedia.presentation.creategroup.CreateGroupViewModel
import com.minhtu.firesocialmedia.presentation.createpoll.CreatePoll
import com.minhtu.firesocialmedia.presentation.exploregroup.ExploreGroup
import com.minhtu.firesocialmedia.presentation.group.Group
import com.minhtu.firesocialmedia.presentation.groupdetails.GroupDetails
import com.minhtu.firesocialmedia.presentation.groupdetails.GroupDetailsViewModel
import com.minhtu.firesocialmedia.presentation.invitemember.InviteMember
import com.minhtu.firesocialmedia.presentation.managemembers.ManageMembers
import com.minhtu.firesocialmedia.presentation.selectgroup.SelectGroup
import com.minhtu.firesocialmedia.platform.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.GroupNavGraph
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance as CoreUserInstance
import org.koin.compose.viewmodel.koinViewModel

class GroupNavGraphImpl : GroupNavGraph {

    override fun getGroupRoute(): String = Group.getScreenName()

    override fun getCreateGroupRoute(): String = CreateGroup.getScreenName()

    override fun getExploreGroupRoute(): String = ExploreGroup.getScreenName()

    override fun getGroupDetailsRoute(): String = GroupDetails.getScreenName()

    override fun getSelectGroupRoute(): String = SelectGroup.getScreenName()

    override fun getInviteMemberRoute(): String = InviteMember.getScreenName()

    override fun getManageMembersRoute(): String = ManageMembers.getScreenName()

    override fun getCreatePollRoute(): String = CreatePoll.getScreenName()

    override fun registerGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        paddingValues: PaddingValues,
        getSelectedGroup: () -> GroupInstance?,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToExploreGroup: () -> Unit,
        onNavigateToSelectGroup: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = Group.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            if (homeViewModel.currentUser != null) {
                Group.GroupScreen(
                    homeViewModel.currentUser!!,
                    paddingValues,
                    onNavigateToCreateGroupScreen = onNavigateToCreateGroup,
                    onNavigateToExploreGroupScreen = onNavigateToExploreGroup,
                    onNavigateToSelectGroupScreen = onNavigateToSelectGroup,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                showToast("Cannot open group screen. Retry later!!!")
                navController.popBackStack()
            }
        }
    }

    override fun registerCreateGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        context: Any,
        paddingValues: PaddingValues,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        onCreateGroupSuccess: (GroupInstance) -> Unit
    ) {
        navGraphBuilder.composable(
            route = CreateGroup.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val createGroupViewModel: CreateGroupViewModel = koinViewModel()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> createGroupViewModel.updateAvatar(uri) },
                onVideoPicked = {}
            )
            if (homeViewModel.currentUser != null) {
                CreateGroup.CreateGroupScreen(
                    paddingValues,
                    createGroupViewModel,
                    loadingViewModel,
                    picker,
                    homeViewModel.currentUser!!,
                    onCreateGroupSuccess = onCreateGroupSuccess,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                showToast("Cannot get current user. Please retry later!!!")
            }
        }
    }

    override fun registerExploreGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        onNavigateToGroupDetails: (GroupInstance) -> Unit
    ) {
        navGraphBuilder.composable(
            route = ExploreGroup.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            if (homeViewModel.currentUser != null) {
                ExploreGroup.ExploreGroupScreen(
                    homeViewModel.currentUser!!,
                    paddingValues,
                    localImageLoaderValue,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGroupDetails = onNavigateToGroupDetails
                )
            }
        }
    }

    override fun registerGroupDetailsRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        context: Any,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        getSelectedGroup: () -> GroupInstance?,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToUploadNewsfeed: (updateNew: NewsInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToInviteMember: () -> Unit,
        onLeaveGroup: () -> Unit,
        onManageMembers: (GroupInstance) -> Unit,
        onCreatePoll: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = GroupDetails.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val groupDetailsViewModel: GroupDetailsViewModel = koinViewModel()
            val selectedGroup = getSelectedGroup()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> groupDetailsViewModel.updateCover(uri) },
                onVideoPicked = {}
            )
            if (selectedGroup != null) {
                GroupDetails.GroupDetailsScreen(
                    homeViewModel.currentUser!!,
                    picker,
                    selectedGroup.id,
                    paddingValues,
                    localImageLoaderValue,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    homeViewModel = homeViewModel,
                    loadingViewModel = loadingViewModel,
                    groupDetailsViewModel = groupDetailsViewModel,
                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                    onNavigateToUserInformation = onNavigateToUserInformation,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToUploadNewsfeed = onNavigateToUploadNewsfeed,
                    onNavigateToCommentScreen = onNavigateToCommentScreen,
                    onClickInviteButton = onNavigateToInviteMember,
                    onLeaveGroup = onLeaveGroup,
                    onManageMembers = onManageMembers,
                    onCreatePoll = onCreatePoll
                )
            } else {
                showToast("Cannot get group info this time. Please retry later!")
            }
        }
    }

    override fun registerGroupDetailsDeepLinkRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        context: Any,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        getSelectedGroup: () -> GroupInstance?,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToUploadNewsfeed: (updateNew: NewsInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToInviteMember: () -> Unit,
        onLeaveGroup: () -> Unit,
        onManageMembers: (GroupInstance) -> Unit,
        onCreatePoll: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = "groups/{groupId}"
        ) { backStackEntry ->
            val groupDetailsViewModel: GroupDetailsViewModel = koinViewModel()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> groupDetailsViewModel.updateCover(uri) },
                onVideoPicked = {}
            )
            val groupId = backStackEntry.savedStateHandle.get<String>("groupId") ?: return@composable

            GroupDetails.GroupDetailsScreen(
                currentUser = homeViewModel.currentUser!!,
                imagePicker = picker,
                groupId = groupId,
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                homeViewModel = homeViewModel,
                loadingViewModel = loadingViewModel,
                groupDetailsViewModel = groupDetailsViewModel,
                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                onNavigateToUserInformation = onNavigateToUserInformation,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUploadNewsfeed = onNavigateToUploadNewsfeed,
                onNavigateToCommentScreen = onNavigateToCommentScreen,
                onClickInviteButton = onNavigateToInviteMember,
                onLeaveGroup = onLeaveGroup,
                onManageMembers = onManageMembers,
                onCreatePoll = onCreatePoll
            )
        }
    }

    override fun registerSelectGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToSelectedGroup: (GroupInstance) -> Unit
    ) {
        navGraphBuilder.composable(
            route = SelectGroup.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            SelectGroup.SelectGroupScreen(
                homeViewModel.currentUser!!,
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToSelectedGroup = onNavigateToSelectedGroup
            )
        }
    }

    override fun registerInviteMemberRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        getSelectedGroup: () -> GroupInstance?
    ) {
        navGraphBuilder.composable(
            route = InviteMember.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val selectedGroup = getSelectedGroup()
            if (selectedGroup != null) {
                InviteMember.InviteMemberScreen(
                    selectedGroup,
                    homeViewModel.currentUser!!,
                    paddingValues,
                    localImageLoaderValue,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                showToast("Cannot get group info to share this time. Please retry later!")
            }
        }
    }

    override fun registerManageMembersRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        getSelectedGroup: () -> GroupInstance?,
        onNavigateToInviteMembers: () -> Unit,
        onNavigateToUserInformation: (user: CoreUserInstance) -> Unit,
        onNavigateToSelectGroup: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = ManageMembers.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val selectedGroup = getSelectedGroup()
            if (selectedGroup != null && homeViewModel.currentUser != null) {
                ManageMembers.ManageMembersScreen(
                    homeViewModel.currentUser!!,
                    selectedGroup,
                    loadingViewModel = loadingViewModel,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    onNavigateBack = { navController.popBackStack() },
                    onInviteMembers = onNavigateToInviteMembers,
                    onNavigateToUserInformationScreen = onNavigateToUserInformation,
                    onNavigateToSelectGroupScreen = onNavigateToSelectGroup
                )
            } else {
                showToast("Cannot open manage members screen now. Please try again!!!")
                navController.popBackStack()
            }
        }
    }

    override fun registerCreatePollRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        getSelectedGroup: () -> GroupInstance?
    ) {
        navGraphBuilder.composable(
            route = CreatePoll.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val selectedGroup = getSelectedGroup()
            if (selectedGroup != null) {
                CreatePoll.CreatePollScreen(
                    groupId = selectedGroup.id,
                    currentUser = homeViewModel.currentUser ?: com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance(),
                    onClose = { navController.popBackStack() }
                )
            } else {
                showToast("Cannot get group information now. Please try again!!!")
                navController.popBackStack()
            }
        }
    }
}

