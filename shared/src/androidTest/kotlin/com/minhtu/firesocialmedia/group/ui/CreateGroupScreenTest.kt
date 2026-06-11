package com.minhtu.firesocialmedia.group.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.minhtu.firesocialmedia.core.constants.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.feature.group.presentation.creategroup.CreateGroup
import com.minhtu.firesocialmedia.feature.group.presentation.creategroup.CreateGroupViewModel
import org.junit.Rule
import org.junit.Test

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
    override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = false
    override suspend fun copyLink(copyData: String) {}
    override suspend fun inviteFriendToGroup(friend: UserInstance) {}
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
        val loadingVm = LoadingViewModel()
        val picker = FakeImagePicker()
        val currentUser = UserInstance(uid = "u1")

        composeRule.setContent {
            CreateGroup.CreateGroupScreen(
                createGroupViewModel = vm,
                loadingViewModel = loadingVm,
                imagePicker = picker,
                currentUser = currentUser,
                onCreateGroupSuccess = {}
            )
        }

        composeRule.onNodeWithContentDescription(TestTag.TAG_GROUP_NAME).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(TestTag.TAG_BUTTON_ACCESS_MODIFIER).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(TestTag.TAG_BUTTON_NEXT).assertIsDisplayed()
    }
}





