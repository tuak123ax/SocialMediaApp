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
import com.minhtu.firesocialmedia.group.entity.user.UserInstance as GroupUserInstance
import com.minhtu.firesocialmedia.group.entity.user.toGroupUser
import com.minhtu.firesocialmedia.group.entity.user.toDto as groupUserToDto
import com.minhtu.firesocialmedia.entity.bridge.toAppInitUserDto
import com.minhtu.firesocialmedia.entity.bridge.toGroupUserDto
import com.minhtu.firesocialmedia.home.entity.user.UserInstance as HomeUserInstance
import com.minhtu.firesocialmedia.home.entity.user.toDto as homeUserToDto
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
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.group.rememberPlatformImagePicker
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.navigation.DefaultNavAnimations
import com.minhtu.firesocialmedia.presentation.navigation.GroupNavGraph
import org.koin.compose.viewmodel.koinViewModel

// Bridges feature/home's local UserInstance into feature/group's own local UserInstance,
// which is the type group's own composables expect for `currentUser`.
private fun HomeUserInstance.toGroupUser(): GroupUserInstance = this.homeUserToDto().toAppInitUserDto().toGroupUserDto().toGroupUser()

// Composition-root-only conversion: group's local NewsInstance -> home's NewsInstance,
// which is the canonical News type used by the shared callbacks
// (onNavigateToUploadNewsfeed / onNavigateToCommentScreen) at the composition root.
private fun com.minhtu.firesocialmedia.group.entity.news.NewsInstance.toCoreNews(): NewsInstance {
    val coreDecentralizationType = when (decentralizationType) {
        is com.minhtu.firesocialmedia.group.entity.core.DecentralizationType.Public ->
            com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Public
        is com.minhtu.firesocialmedia.group.entity.core.DecentralizationType.Private ->
            com.minhtu.firesocialmedia.home.entity.core.DecentralizationType.Private
        is com.minhtu.firesocialmedia.group.entity.core.DecentralizationType.OnlyFriends ->
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
        homeViewModel: HomeViewModel,
        paddingValues: PaddingValues,
        getSelectedGroupId: () -> String?,
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
                    homeViewModel.currentUser!!.toGroupUser(),
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
        homeViewModel: HomeViewModel,
        onCreateGroupSuccess: (groupId: String) -> Unit
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
                onImagePicked = { uri -> createGroupViewModel.updateAvatar(uri) }
            )
            if (homeViewModel.currentUser != null) {
                CreateGroup.CreateGroupScreen(
                    paddingValues,
                    createGroupViewModel,
                    picker,
                    homeViewModel.currentUser!!.toGroupUser(),
                    onCreateGroupSuccess = { groupInstance -> onCreateGroupSuccess(groupInstance.id) },
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
        homeViewModel: HomeViewModel,
        onNavigateToGroupDetails: (groupId: String) -> Unit
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
                    homeViewModel.currentUser!!.toGroupUser(),
                    paddingValues,
                    localImageLoaderValue,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGroupDetails = { groupInstance -> onNavigateToGroupDetails(groupInstance.id) }
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
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToUploadNewsfeed: (updateNew: NewsInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToInviteMember: () -> Unit,
        onLeaveGroup: () -> Unit,
        onManageMembers: (groupId: String) -> Unit,
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
            val selectedGroupId = getSelectedGroupId()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> groupDetailsViewModel.updateCover(uri) }
            )
            if (selectedGroupId != null) {
                GroupDetails.GroupDetailsScreen(
                    homeViewModel.currentUser!!.toGroupUser(),
                    picker,
                    selectedGroupId,
                    paddingValues,
                    localImageLoaderValue,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    groupDetailsViewModel = groupDetailsViewModel,
                    onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                    onNavigateToUserInformation = { user -> onNavigateToUserInformation(user?.groupUserToDto()?.toAppInitUserDto()?.toSearchUser()) },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToUploadNewsfeed = { n -> onNavigateToUploadNewsfeed(n?.toCoreNews()) },
                    onNavigateToCommentScreen = { n -> onNavigateToCommentScreen(n.toCoreNews()) },
                    onClickInviteButton = onNavigateToInviteMember,
                    onLeaveGroup = onLeaveGroup,
                    onManageMembers = { groupInstance -> onManageMembers(groupInstance.id) },
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
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?,
        onNavigateToShowImageScreen: (image: String) -> Unit,
        onNavigateToUserInformation: (user: UserInstance?) -> Unit,
        onNavigateToUploadNewsfeed: (updateNew: NewsInstance?) -> Unit,
        onNavigateToCommentScreen: (selectedNew: NewsInstance) -> Unit,
        onNavigateToInviteMember: () -> Unit,
        onLeaveGroup: () -> Unit,
        onManageMembers: (groupId: String) -> Unit,
        onCreatePoll: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = "groups/{groupId}"
        ) { backStackEntry ->
            val groupDetailsViewModel: GroupDetailsViewModel = koinViewModel()
            val picker = rememberPlatformImagePicker(
                context = context,
                onImagePicked = { uri -> groupDetailsViewModel.updateCover(uri) }
            )
            val groupId = backStackEntry.savedStateHandle.get<String>("groupId") ?: return@composable

            GroupDetails.GroupDetailsScreen(
                currentUser = homeViewModel.currentUser!!.toGroupUser(),
                imagePicker = picker,
                groupId = groupId,
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                groupDetailsViewModel = groupDetailsViewModel,
                onNavigateToShowImageScreen = onNavigateToShowImageScreen,
                onNavigateToUserInformation = { user -> onNavigateToUserInformation(user?.groupUserToDto()?.toAppInitUserDto()?.toSearchUser()) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUploadNewsfeed = { n -> onNavigateToUploadNewsfeed(n?.toCoreNews()) },
                onNavigateToCommentScreen = { n -> onNavigateToCommentScreen(n.toCoreNews()) },
                onClickInviteButton = onNavigateToInviteMember,
                onLeaveGroup = onLeaveGroup,
                onManageMembers = { groupInstance -> onManageMembers(groupInstance.id) },
                onCreatePoll = onCreatePoll
            )
        }
    }

    override fun registerSelectGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModel,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToSelectedGroup: (groupId: String) -> Unit
    ) {
        navGraphBuilder.composable(
            route = SelectGroup.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            SelectGroup.SelectGroupScreen(
                homeViewModel.currentUser!!.toGroupUser(),
                paddingValues = paddingValues,
                localImageLoaderValue = localImageLoaderValue,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToSelectedGroup = { group -> onNavigateToSelectedGroup(group.id) }
            )
        }
    }

    override fun registerInviteMemberRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?
    ) {
        navGraphBuilder.composable(
            route = InviteMember.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val selectedGroupId = getSelectedGroupId()
            if (selectedGroupId != null) {
                InviteMember.InviteMemberScreen(
                    selectedGroupId,
                    homeViewModel.currentUser!!.toGroupUser(),
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
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?,
        onNavigateToInviteMembers: () -> Unit,
        onNavigateToUserInformation: (user: UserInstance) -> Unit,
        onNavigateToSelectGroup: () -> Unit
    ) {
        navGraphBuilder.composable(
            route = ManageMembers.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val selectedGroupId = getSelectedGroupId()
            if (selectedGroupId != null && homeViewModel.currentUser != null) {
                ManageMembers.ManageMembersScreen(
                    homeViewModel.currentUser!!.toGroupUser(),
                    selectedGroupId,
                    paddingValues = paddingValues,
                    localImageLoaderValue = localImageLoaderValue,
                    onNavigateBack = { navController.popBackStack() },
                    onInviteMembers = onNavigateToInviteMembers,
                    onNavigateToUserInformationScreen = { user -> onNavigateToUserInformation(user.groupUserToDto().toAppInitUserDto().toSearchUser()) },
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
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?
    ) {
        navGraphBuilder.composable(
            route = CreatePoll.getScreenName(),
            enterTransition = DefaultNavAnimations.enter,
            popEnterTransition = DefaultNavAnimations.popEnter,
            exitTransition = DefaultNavAnimations.exit,
            popExitTransition = DefaultNavAnimations.popExit
        ) {
            val selectedGroupId = getSelectedGroupId()
            if (selectedGroupId != null) {
                CreatePoll.CreatePollScreen(
                    groupId = selectedGroupId,
                    currentUser = homeViewModel.currentUser?.toGroupUser() ?: GroupUserInstance(),
                    onClose = { navController.popBackStack() }
                )
            } else {
                showToast("Cannot get group information now. Please try again!!!")
                navController.popBackStack()
            }
        }
    }
}
