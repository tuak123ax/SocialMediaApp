package com.minhtu.firesocialmedia.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidedValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.presentation.home.HomeViewModelContract
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel

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
        homeViewModel: HomeViewModelContract,
        paddingValues: PaddingValues,
        getSelectedGroup: () -> GroupInstance?,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToExploreGroup: () -> Unit,
        onNavigateToSelectGroup: () -> Unit
    )

    fun registerCreateGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        context: Any,
        paddingValues: PaddingValues,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        onCreateGroupSuccess: (GroupInstance) -> Unit
    )

    fun registerExploreGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        onNavigateToGroupDetails: (GroupInstance) -> Unit
    )

    fun registerGroupDetailsRoute(
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
    )

    fun registerGroupDetailsDeepLinkRoute(
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
    )

    fun registerSelectGroupRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        onNavigateToCreateGroup: () -> Unit,
        onNavigateToSelectedGroup: (GroupInstance) -> Unit
    )

    fun registerInviteMemberRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        getSelectedGroup: () -> GroupInstance?
    )

    fun registerManageMembersRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        paddingValues: PaddingValues,
        localImageLoaderValue: ProvidedValue<*>,
        homeViewModel: HomeViewModelContract,
        loadingViewModel: LoadingViewModel,
        getSelectedGroup: () -> GroupInstance?,
        onNavigateToInviteMembers: () -> Unit,
        onNavigateToUserInformation: (user: UserInstance) -> Unit,
        onNavigateToSelectGroup: () -> Unit
    )

    fun registerCreatePollRoute(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        homeViewModel: HomeViewModelContract,
        getSelectedGroup: () -> GroupInstance?
    )
}

