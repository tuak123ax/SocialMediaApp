package com.minhtu.firesocialmedia.group.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.group.ImagePicker
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.group.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.creategroup.CreateGroup
import com.minhtu.firesocialmedia.presentation.creategroup.CreateGroupViewModel
import org.junit.Rule
import org.junit.Test
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private class FakeImagePicker : ImagePicker {
    @Composable
    override fun RegisterLauncher(hideLoading: () -> Unit) {}
    override fun pickImage() {}
    override fun pickVideo() {}
    override suspend fun loadImageBytes(uri: String): ByteArray? = null
    @Composable
    override fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier) {}
}

private class FakeGroupRepository : GroupRepository {
    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
    override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = false
    override suspend fun copyLink(copyData: String) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
    override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

class CreateGroupScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_basic_controls() {
        val vm = CreateGroupViewModel(CreateGroupUseCase(FakeGroupRepository()))
        val picker = FakeImagePicker()
        val currentUser = UserInstance(uid = "u1")

        composeRule.setContent {
            KoinApplication(application = { modules(module { viewModel { LoadingViewModel() } }) }) {
                CreateGroup.CreateGroupScreen(
                    paddingValues = PaddingValues(0.dp),
                    createGroupViewModel = vm,
                    imagePicker = picker,
                    currentUser = currentUser,
                    onCreateGroupSuccess = {},
                    onNavigateBack = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_GROUP_NAME).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(TestTag.TAG_BUTTON_ACCESS_MODIFIER).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(TestTag.TAG_BUTTON_NEXT).assertIsDisplayed()
    }
}





