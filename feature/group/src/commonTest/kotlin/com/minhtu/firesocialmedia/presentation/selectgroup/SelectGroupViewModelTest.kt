package com.minhtu.firesocialmedia.presentation.selectgroup

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeSelectGroupRepo : GroupRepository {
    var groupsForUser: Set<GroupInstance> = emptySet()
    override suspend fun getAllGroups(userId: String): Set<GroupInstance> = groupsForUser
    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = GroupConfigs()
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

@OptIn(ExperimentalCoroutinesApi::class)
class SelectGroupViewModelTest {

    @Test
    fun getAllGroupsOfUserSetsStateWithGroups() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeSelectGroupRepo().apply {
            groupsForUser = setOf(
                GroupInstance(id = "g1", name = "G1"),
                GroupInstance(id = "g2", name = "G2")
            )
        }
        val vm = SelectGroupViewModel(GetAllGroupsUseCase(repo), dispatcher)
        vm.getAllGroupsOfUser("u1")
        advanceUntilIdle()
        Dispatchers.resetMain()
        assertEquals(2, vm.getAllGroupsState.value?.size)
    }

    @Test
    fun getAllGroupsOfUserWithNoGroupsReturnsEmptySet() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeSelectGroupRepo().apply { groupsForUser = emptySet() }
        val vm = SelectGroupViewModel(GetAllGroupsUseCase(repo), dispatcher)
        vm.getAllGroupsOfUser("u99")
        advanceUntilIdle()
        Dispatchers.resetMain()
        assertEquals(0, vm.getAllGroupsState.value?.size)
    }

    @Test
    fun getAllGroupsOfUserSetsCorrectGroupIds() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeSelectGroupRepo().apply {
            groupsForUser = setOf(GroupInstance(id = "gA", name = "GroupA"))
        }
        val vm = SelectGroupViewModel(GetAllGroupsUseCase(repo), dispatcher)
        vm.getAllGroupsOfUser("u1")
        advanceUntilIdle()
        Dispatchers.resetMain()
        assertTrue(vm.getAllGroupsState.value?.any { it.id == "gA" } == true)
    }

    @Test
    fun getAllGroupsOfUserInitialStateIsNull() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = SelectGroupViewModel(GetAllGroupsUseCase(FakeSelectGroupRepo()), dispatcher)
        assertEquals(null, vm.getAllGroupsState.value)
    }
}
