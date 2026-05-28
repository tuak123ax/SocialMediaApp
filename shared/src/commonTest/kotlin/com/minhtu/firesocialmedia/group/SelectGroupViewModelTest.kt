package com.minhtu.firesocialmedia.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.usecases.group.GetAllGroupsUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.SelectGroupViewModel
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

private class FakeGroupRepoForSelect : GroupRepository {
	var groupsForUser: Set<GroupInstance> = emptySet()
	override suspend fun getAllGroups(userId: String): Set<GroupInstance> = groupsForUser

	override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
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

@OptIn(ExperimentalCoroutinesApi::class)
class SelectGroupViewModelTest {

	@Test
	fun `getAllGroupsOfUser sets state with groups`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val repo = FakeGroupRepoForSelect().apply {
			groupsForUser = setOf(
				GroupInstance(id = "g1", name = "G1"),
				GroupInstance(id = "g2", name = "G2")
			)
		}
		val vm = SelectGroupViewModel(
			getAllGroupsUseCase = GetAllGroupsUseCase(repo),
			ioDispatcher = dispatcher
		)

		Dispatchers.setMain(dispatcher)
		vm.getAllGroupsOfUser("u1")
		advanceUntilIdle()
		Dispatchers.resetMain()

		val state = vm.getAllGroupsState.value
		assertTrue(state != null)
        assertEquals(2, state!!.size)
    }

    @Test
    fun `getAllGroupsOfUser with no groups returns empty set`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeGroupRepoForSelect().apply { groupsForUser = emptySet() }
        val vm = SelectGroupViewModel(
            getAllGroupsUseCase = GetAllGroupsUseCase(repo),
            ioDispatcher = dispatcher
        )

        Dispatchers.setMain(dispatcher)
        vm.getAllGroupsOfUser("u99")
        advanceUntilIdle()
        Dispatchers.resetMain()

        val state = vm.getAllGroupsState.value
        assertEquals(0, state?.size)
    }
}
