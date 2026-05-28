package com.minhtu.firesocialmedia.group.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMember
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMemberViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.seiko.imageloader.LocalImageLoader
import org.junit.Rule
import org.junit.Test

private class UserRepoForInvite : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = UserInstance(uid = userId, name = "U")
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
}

private class RepoForInvite : BaseFakeGroupRepository()

class InviteMemberScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_search_bar() {
        val vm = InviteMemberViewModel(
            copyLinkUseCase = CopyLinkUseCase(RepoForInvite()),
            getUserUseCase = GetUserUseCase(UserRepoForInvite()),
            inviteFriendToGroupUseCase = InviteFriendToGroupUseCase(RepoForInvite())
        )
        val searchVm = SearchViewModel()
        val group = GroupInstance(id = "g1", name = "G")
        val currentUser = UserInstance(uid = "u1")

        composeRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader()) {
                InviteMember.InviteMemberScreen(
                    group = group,
                    currentUser = currentUser,
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                    localImageLoaderValue = LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader(),
                    inviteMemberViewModel = vm,
                    searchViewModel = searchVm,
                    onNavigateBack = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_SEARCH_BAR).assertIsDisplayed()
    }
}


