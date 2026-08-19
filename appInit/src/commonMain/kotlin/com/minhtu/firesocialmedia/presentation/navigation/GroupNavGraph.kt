package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.search.entity.user.UserInstance

interface GroupNavGraph {

    fun getGroupRoute(): String

    fun getCreateGroupRoute(): String

    fun getExploreGroupRoute(): String

    fun getGroupDetailsRoute(): String

    fun getSelectGroupRoute(): String

    fun getInviteMemberRoute(): String

    fun getManageMembersRoute(): String

    fun getCreatePollRoute(): String

    fun registerGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        paddingValues: PaddingValues,
        getSelectedGroupId: () -> String?,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToExploreGroup: () -> Unit,
        onNavigateToSelectGroup: () -> Unit
    )

    fun registerCreateGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        context: Any,
        paddingValues: PaddingValues,
        homeViewModel: HomeViewModel,
        onCreateGroupSuccess: (groupId: String) -> Unit
    )

    fun registerExploreGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModel,
        onNavigateToGroupDetails: (groupId: String) -> Unit
    )

    fun registerGroupDetailsRoute(
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
    )

    fun registerGroupDetailsDeepLinkRoute(
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
    )

    fun registerSelectGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModel,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToSelectedGroup: (groupId: String) -> Unit
    )

    fun registerInviteMemberRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?
    )

    fun registerManageMembersRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?,
        onNavigateToInviteMembers: () -> Unit,
        onNavigateToUserInformation: (user: UserInstance) -> Unit,
        onNavigateToSelectGroup: () -> Unit
    )

    fun registerCreatePollRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModel,
        getSelectedGroupId: () -> String?
    )
}
