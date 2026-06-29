package com.minhtu.firesocialmedia.group

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

private class CreateGroupRepo : GroupRepository {
    var saveGroupResult: Boolean = true
    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String): Boolean = saveGroupResult
    override suspend fun getAllGroups(userId: String): Set<GroupInstance> = emptySet()
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance = GroupInstance()
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String): Boolean = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String): Boolean = true
    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = hashMapOf()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs()
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
    fun `createGroup success updates state and user groups`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = CreateGroupRepo().apply { saveGroupResult = true }
        val vm = CreateGroupViewModel(
            createGroupUseCase = CreateGroupUseCase(repo),
            ioDispatcher = dispatcher
        )

        val currentUser = UserInstance(uid = "u1")
        vm.updateGroupName("My Group")
        vm.updateAvatar("avatar_url")
        vm.updateAccessPermission(DecentralizationType.Private)
        vm.updatePassword("secret")

        vm.createGroup(currentUser)
        advanceUntilIdle()

        val created = vm.createGroupState.value
        assertNotNull(created)
        assertEquals("My Group", created?.name)
        assertEquals("avatar_url", created?.avatar)
        assertEquals("secret", created?.password)
        assertTrue(currentUser.groups.containsKey(created!!.id))
        // Permission resets to Public after operation
        assertEquals(DecentralizationType.Public, vm.accessPermission.value)
    }

    @Test
    fun `createGroup failure reverts user groups and sets empty state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = CreateGroupRepo().apply { saveGroupResult = false }
        val vm = CreateGroupViewModel(
            createGroupUseCase = CreateGroupUseCase(repo),
            ioDispatcher = dispatcher
        )

        val currentUser = UserInstance(uid = "u1")
        vm.updateGroupName("Fail Group")
        vm.updateAccessPermission(DecentralizationType.Public)

        vm.createGroup(currentUser)
        advanceUntilIdle()

        val created = vm.createGroupState.value
        // On failure viewModel sets empty GroupInstance()
        assertEquals(GroupInstance(), created)
        assertTrue(currentUser.groups.isEmpty())
        assertEquals(DecentralizationType.Public, vm.accessPermission.value)
    }

    @Test
    fun `resetCreateGroupUiState clears inputs`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreateGroupViewModel(
            createGroupUseCase = CreateGroupUseCase(CreateGroupRepo()),
            ioDispatcher = dispatcher
        )
        vm.updateGroupName("Name")
        vm.updateAvatar("a")
        vm.updatePassword("p")

        vm.resetCreateGroupUiState()

        assertEquals("", vm.groupName.value)
        assertEquals("", vm.password.value)
        // avatar resets to default value inside VM; we just verify it's not the custom one
        assertTrue(vm.avatar != "a")
    }

    @Test
    fun `resetCreateGroupState clears createGroupState`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = CreateGroupRepo().apply { saveGroupResult = true }
        val vm = CreateGroupViewModel(
            createGroupUseCase = CreateGroupUseCase(repo),
            ioDispatcher = dispatcher
        )
        vm.updateGroupName("Temp")
        vm.createGroup(UserInstance(uid = "u1"))
        advanceUntilIdle()
        assertNotNull(vm.createGroupState.value)

        vm.resetCreateGroupState()
        assertEquals(null, vm.createGroupState.value)
    }

    @Test
    fun `updateAccessPermission and resetAccessPermission work correctly`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = CreateGroupViewModel(
            createGroupUseCase = CreateGroupUseCase(CreateGroupRepo()),
            ioDispatcher = dispatcher
        )
        vm.updateAccessPermission(DecentralizationType.Private)
        assertEquals(DecentralizationType.Private, vm.accessPermission.value)

        vm.resetAccessPermission()
        assertEquals(DecentralizationType.Public, vm.accessPermission.value)
    }
}


