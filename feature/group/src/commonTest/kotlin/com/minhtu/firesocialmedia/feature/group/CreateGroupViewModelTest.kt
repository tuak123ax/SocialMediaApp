package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.usecases.group.CreateGroupUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.creategroup.CreateGroupViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class FakeCreateGroupRepo : GroupRepository {
    var saveGroupResult: Boolean = true
    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String): Boolean = saveGroupResult
    override suspend fun getAllGroups(userId: String): Set<GroupInstance> = emptySet()
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance = GroupInstance()
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String): Boolean = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String): Boolean = true
    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = hashMapOf()
    override suspend fun getGroupConfigs(userId: String, groupId: String) =
        com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String): Boolean = false
    override suspend fun copyLink(copyData: String) {}
    override suspend fun inviteFriendToGroup(friend: UserInstance) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun removeMember(member: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> = emptyList()
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreateGroupViewModelTest {

    @Test
    fun `createGroup success updates createGroupState and adds to user groups`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCreateGroupRepo().apply { saveGroupResult = true }
        val vm = CreateGroupViewModel(
            createGroupUseCase = CreateGroupUseCase(repo),
            ioDispatcher = dispatcher
        )
        val user = UserInstance(uid = "u1")
        vm.updateGroupName("My Group")
        vm.updateAvatar("avatar_url")
        vm.updateAccessPermission(DecentralizationType.Private)
        vm.updatePassword("secret")

        vm.createGroup(user)
        advanceUntilIdle()

        val created = vm.createGroupState.value
        assertNotNull(created)
        assertEquals("My Group", created?.name)
        assertEquals("avatar_url", created?.avatar)
        assertEquals("secret", created?.password)
        assertTrue(user.groups.containsKey(created!!.id))
    }

    @Test
    fun `createGroup success resets accessPermission to Public`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCreateGroupRepo()
        val vm = CreateGroupViewModel(CreateGroupUseCase(repo), dispatcher)
        vm.updateAccessPermission(DecentralizationType.Private)
        vm.createGroup(UserInstance(uid = "u1"))
        advanceUntilIdle()
        assertEquals(DecentralizationType.Public, vm.accessPermission.value)
    }

    @Test
    fun `createGroup failure sets createGroupState to empty GroupInstance`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCreateGroupRepo().apply { saveGroupResult = false }
        val vm = CreateGroupViewModel(CreateGroupUseCase(repo), dispatcher)
        val user = UserInstance(uid = "u2")
        vm.updateGroupName("Fail Group")
        vm.createGroup(user)
        advanceUntilIdle()

        assertEquals(GroupInstance(), vm.createGroupState.value)
        assertTrue(user.groups.isEmpty())
    }

    @Test
    fun `createGroup with Public permission does not set password on GroupInstance`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCreateGroupRepo()
        val vm = CreateGroupViewModel(CreateGroupUseCase(repo), dispatcher)
        vm.updateGroupName("Public Group")
        vm.updatePassword("should_not_appear")
        vm.updateAccessPermission(DecentralizationType.Public)
        vm.createGroup(UserInstance(uid = "u3"))
        advanceUntilIdle()

        val created = vm.createGroupState.value
        assertNotNull(created)
        assertEquals("", created?.password)
    }

    @Test
    fun `resetCreateGroupState sets createGroupState to null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCreateGroupRepo()
        val vm = CreateGroupViewModel(CreateGroupUseCase(repo), dispatcher)
        vm.updateGroupName("Temp")
        vm.createGroup(UserInstance(uid = "u4"))
        advanceUntilIdle()
        assertNotNull(vm.createGroupState.value)

        vm.resetCreateGroupState()
        assertEquals(null, vm.createGroupState.value)
    }

    @Test
    fun `resetCreateGroupUiState clears name password and avatar`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreateGroupViewModel(CreateGroupUseCase(FakeCreateGroupRepo()), dispatcher)
        vm.updateGroupName("Name")
        vm.updatePassword("pass")
        vm.updateAvatar("custom_avatar")

        vm.resetCreateGroupUiState()

        assertEquals("", vm.groupName.value)
        assertEquals("", vm.password.value)
        assertTrue(vm.avatar != "custom_avatar")
    }

    @Test
    fun `updateAccessPermission and resetAccessPermission work correctly`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreateGroupViewModel(CreateGroupUseCase(FakeCreateGroupRepo()), dispatcher)

        vm.updateAccessPermission(DecentralizationType.Private)
        assertEquals(DecentralizationType.Private, vm.accessPermission.value)

        vm.resetAccessPermission()
        assertEquals(DecentralizationType.Public, vm.accessPermission.value)
    }

    @Test
    fun `creator is set as admin member of the newly created group`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCreateGroupRepo()
        val vm = CreateGroupViewModel(CreateGroupUseCase(repo), dispatcher)
        val user = UserInstance(uid = "creator123")
        vm.updateGroupName("Admin Group")
        vm.createGroup(user)
        advanceUntilIdle()

        val created = vm.createGroupState.value
        assertNotNull(created)
        assertEquals("admin", created!!.members["creator123"])
    }
}

