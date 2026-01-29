package com.minhtu.firesocialmedia.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ManageMembersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeGroupRepoForManage : GroupRepository {
	var removeCalled: Pair<UserInstance, GroupInstance>? = null
	var promoteCalled: Pair<UserInstance, GroupInstance>? = null
	var demoteCalled: Pair<UserInstance, GroupInstance>? = null
	var removeResult = true
	var promoteResult = true
	var demoteResult = true

	override suspend fun removeMember(member: UserInstance, group: GroupInstance): Boolean {
		removeCalled = member to group; return removeResult
	}
	override suspend fun promoteMember(member: UserInstance, group: GroupInstance): Boolean {
		promoteCalled = member to group; return promoteResult
	}
	override suspend fun demoteMember(member: UserInstance, group: GroupInstance): Boolean {
		demoteCalled = member to group; return demoteResult
	}

	override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
	override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
	override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
	override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
	override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
	override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
	override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs()
	override suspend fun fetchNotificationState(userId: String, groupId: String) = false
	override suspend fun copyLink(copyData: String) {}
	override suspend fun inviteFriendToGroup(friend: UserInstance) {}
	override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
	override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

private class FakeUserRepoForManage : UserRepository {
	val users = mutableMapOf<String, UserInstance?>()
	override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = users[userId]
	override suspend fun getCurrentUserUid(): String? = null
	override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
	override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ManageMembersViewModelTest {

	@Test
	fun `removeMember updates status and local maps`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val groupRepo = FakeGroupRepoForManage()
		val userRepo = FakeUserRepoForManage()
		val vm = ManageMembersViewModel(
			getUserUseCase = GetUserUseCase(userRepo),
			removeMemberUseCase = RemoveMemberUseCase(groupRepo),
			promoteMemberUseCase = PromoteMemberUseCase(groupRepo),
			demoteMemberUseCase = DemoteMemberUseCase(groupRepo),
			ioDispatcher = dispatcher
		)
		val member = UserInstance(uid = "u1")
		val group = GroupInstance(id = "g1")
		group.members["u1"] = "member"
		member.groups["g1"] = group

		vm.removeMember(member, group)
		advanceUntilIdle()

		assertEquals(true, vm.removeMemberStatus.value)
		assertTrue("u1" !in group.members.keys)
		assertTrue("g1" !in member.groups.keys)
		vm.resetRemoveMemberStatus()
		assertEquals(null, vm.removeMemberStatus.value)
	}

	@Test
	fun `promoteMember updates status and role`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val groupRepo = FakeGroupRepoForManage()
		val userRepo = FakeUserRepoForManage()
		val vm = ManageMembersViewModel(
			getUserUseCase = GetUserUseCase(userRepo),
			removeMemberUseCase = RemoveMemberUseCase(groupRepo),
			promoteMemberUseCase = PromoteMemberUseCase(groupRepo),
			demoteMemberUseCase = DemoteMemberUseCase(groupRepo),
			ioDispatcher = dispatcher
		)
		val member = UserInstance(uid = "u2")
		val group = GroupInstance(id = "g2")

		vm.promoteMember(member, group)
		advanceUntilIdle()

		assertEquals(true, vm.promoteMemberStatus.value)
		assertEquals("admin", group.members["u2"])
		vm.resetPromoteMemberStatus()
		assertEquals(null, vm.promoteMemberStatus.value)
	}

	@Test
	fun `demoteMember updates status and role`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val groupRepo = FakeGroupRepoForManage()
		val userRepo = FakeUserRepoForManage()
		val vm = ManageMembersViewModel(
			getUserUseCase = GetUserUseCase(userRepo),
			removeMemberUseCase = RemoveMemberUseCase(groupRepo),
			promoteMemberUseCase = PromoteMemberUseCase(groupRepo),
			demoteMemberUseCase = DemoteMemberUseCase(groupRepo),
			ioDispatcher = dispatcher
		)
		val member = UserInstance(uid = "u3")
		val group = GroupInstance(id = "g3")
		group.members["u3"] = "admin"

		vm.demoteMember(member, group)
		advanceUntilIdle()

		assertEquals(true, vm.demoteMemberStatus.value)
		assertEquals("member", group.members["u3"])
		vm.resetDemoteMemberStatus()
		assertEquals(null, vm.demoteMemberStatus.value)
	}

	@Test
	fun `fetchAdminList and fetchMemberList load users`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val groupRepo = FakeGroupRepoForManage()
		val userRepo = FakeUserRepoForManage().apply {
			users["a1"] = UserInstance(uid = "a1", name = "Admin 1")
			users["a2"] = UserInstance(uid = "a2", name = "Admin 2")
			users["m1"] = UserInstance(uid = "m1", name = "Member 1")
		}
		val vm = ManageMembersViewModel(
			getUserUseCase = GetUserUseCase(userRepo),
			removeMemberUseCase = RemoveMemberUseCase(groupRepo),
			promoteMemberUseCase = PromoteMemberUseCase(groupRepo),
			demoteMemberUseCase = DemoteMemberUseCase(groupRepo),
			ioDispatcher = dispatcher
		)

		vm.fetchAdminList(setOf("a1", "a2"))
		vm.fetchMemberList(setOf("m1"))
		advanceUntilIdle()

		assertEquals(2, vm.fetchAdminListStatus.value.size)
		assertEquals(1, vm.fetchMemberListStatus.value.size)

		// Add/remove helpers
		val extraAdmin = UserInstance(uid = "a3")
		val extraMember = UserInstance(uid = "m2")
		vm.addAdminToList(extraAdmin)
		vm.addMemberToList(extraMember)
		assertTrue(vm.fetchAdminListStatus.value.contains(extraAdmin))
		assertTrue(vm.fetchMemberListStatus.value.contains(extraMember))

		vm.removeAdminFromList(extraAdmin)
		vm.removeMemberFromList(extraMember)
		assertTrue(!vm.fetchAdminListStatus.value.contains(extraAdmin))
		assertTrue(!vm.fetchMemberListStatus.value.contains(extraMember))

		vm.resetAdminAndMemberList()
		assertTrue(vm.fetchAdminListStatus.value.isEmpty())
		assertTrue(vm.fetchMemberListStatus.value.isEmpty())
	}
}





