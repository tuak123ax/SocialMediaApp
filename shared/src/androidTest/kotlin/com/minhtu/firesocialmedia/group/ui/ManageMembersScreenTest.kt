package com.minhtu.firesocialmedia.group.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembers
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembersViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.seiko.imageloader.LocalImageLoader
import org.junit.Rule
import org.junit.Test

private class UserRepoForManage : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = UserInstance(uid = userId, name = userId)
     override suspend fun getCurrentUserUid(): String? = null
     override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
     override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
}

private class RepoForManage : BaseFakeGroupRepository()

class ManageMembersScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_search_bar() {
        val vm = ManageMembersViewModel(
            getUserUseCase = GetUserUseCase(UserRepoForManage()),
            removeMemberUseCase = RemoveMemberUseCase(RepoForManage()),
            promoteMemberUseCase = PromoteMemberUseCase(RepoForManage()),
            demoteMemberUseCase = DemoteMemberUseCase(RepoForManage())
        )
        val searchVm = SearchViewModel()
        val loadingVm = LoadingViewModel()
        val currentUser = UserInstance(uid = "admin1").apply { name = "Admin" }
        val group = GroupInstance(id = "g1", name = "G").apply {
            members["admin1"] = "admin"
            members["m1"] = "member"
        }

        composeRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader()) {
                ManageMembers.ManageMembersScreen(
                    currentUser = currentUser,
                    group = group,
                    manageMembersViewModel = vm,
                    searchViewModel = searchVm,
                    loadingViewModel = loadingVm,
                    paddingValues = PaddingValues(),
                    localImageLoaderValue = LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader(),
                    onNavigateBack = {},
                    onInviteMembers = {},
                    onNavigateToUserInformationScreen = {},
                    onNavigateToSelectGroupScreen = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_SEARCH_BAR).assertIsDisplayed()
    }
}


