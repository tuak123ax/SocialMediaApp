package com.minhtu.firesocialmedia.group.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.group.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.managemembers.ManageMembers
import com.minhtu.firesocialmedia.presentation.managemembers.ManageMembersViewModel
import com.seiko.imageloader.LocalImageLoader
import org.junit.Rule
import org.junit.Test
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private class UserRepoForManage : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = UserDTO(uid = userId, name = userId)
     override suspend fun getCurrentUserUid(): String? = null
     override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class RepoForManage : BaseFakeGroupRepository() {
    override suspend fun fetchGroupInfo(groupId: String) = GroupInstance(id = groupId, name = "G").apply {
        members["admin1"] = "admin"
        members["m1"] = "member"
    }
}

class ManageMembersScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_search_bar() {
        val repo = RepoForManage()
        val vm = ManageMembersViewModel(
            getUserUseCase = GetUserUseCase(UserRepoForManage()),
            removeMemberUseCase = RemoveMemberUseCase(repo),
            promoteMemberUseCase = PromoteMemberUseCase(repo),
            demoteMemberUseCase = DemoteMemberUseCase(repo),
            fetchGroupInfoUseCase = FetchGroupInfoUseCase(repo)
        )
        val currentUser = UserInstance(uid = "admin1").apply { name = "Admin" }

        composeRule.setContent {
            KoinApplication(application = { modules(module { viewModel { LoadingViewModel() } }) }) {
                CompositionLocalProvider(LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader()) {
                    ManageMembers.ManageMembersScreen(
                        currentUser = currentUser,
                        groupId = "g1",
                        manageMembersViewModel = vm,
                        paddingValues = PaddingValues(),
                        localImageLoaderValue = LocalImageLoader provides com.minhtu.firesocialmedia.platform.generateImageLoader(),
                        onNavigateBack = {},
                        onInviteMembers = {},
                        onNavigateToUserInformationScreen = {},
                        onNavigateToSelectGroupScreen = {}
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_SEARCH_BAR).assertIsDisplayed()
    }
}


