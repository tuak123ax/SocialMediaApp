package com.minhtu.firesocialmedia.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.InviteMemberViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class FakeInviteGroupRepository : GroupRepository {
	var lastCopied: String? = null
	var lastInvited: UserInstance? = null
	override suspend fun copyLink(copyData: String) { lastCopied = copyData }
	override suspend fun inviteFriendToGroup(friend: UserInstance) { lastInvited = friend }

	override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
	override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
	override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
	override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
	override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
	override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
	override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs()
	override suspend fun fetchNotificationState(userId: String, groupId: String) = false
	override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
	override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
	override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
	override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
	override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
	override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

private class FakeUserRepoForInvite : UserRepository {
	val users = mutableMapOf<String, UserInstance?>()
	override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = users[userId]
	override suspend fun getCurrentUserUid(): String? = null
	override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
	override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
}

@OptIn(ExperimentalCoroutinesApi::class)
class InviteMemberViewModelTest {

	@Test
	fun `copyLink forwards to use case`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val repo = FakeInviteGroupRepository()
		val userRepo = FakeUserRepoForInvite()
		val vm = InviteMemberViewModel(
			copyLinkUseCase = CopyLinkUseCase(repo),
			getUserUseCase = GetUserUseCase(userRepo),
			inviteFriendToGroupUseCase = InviteFriendToGroupUseCase(repo),
			ioDispatcher = dispatcher
		)

		vm.copyLink("my-link")
		advanceUntilIdle()

		assertEquals("my-link", repo.lastCopied)
	}

	@Test
	fun `findUserById returns from use case`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val repo = FakeInviteGroupRepository()
		val userRepo = FakeUserRepoForInvite().apply {
			users["u1"] = UserInstance(uid = "u1", name = "User 1")
		}
		val vm = InviteMemberViewModel(
			copyLinkUseCase = CopyLinkUseCase(repo),
			getUserUseCase = GetUserUseCase(userRepo),
			inviteFriendToGroupUseCase = InviteFriendToGroupUseCase(repo),
			ioDispatcher = dispatcher
		)

		val user = vm.findUserById("u1")
		assertNotNull(user)
		assertEquals("User 1", user.name)
	}

	@Test
	fun `inviteFriendToGroup appends notification and calls repository`() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val repo = FakeInviteGroupRepository()
		val userRepo = FakeUserRepoForInvite()
		val vm = InviteMemberViewModel(
			copyLinkUseCase = CopyLinkUseCase(repo),
			getUserUseCase = GetUserUseCase(userRepo),
			inviteFriendToGroupUseCase = InviteFriendToGroupUseCase(repo),
			ioDispatcher = dispatcher
		)

		val current = UserInstance(uid = "current", name = "Alice", image = "img")
        // Leave token empty to avoid Android JSON code path in unit tests
        val friend = UserInstance(uid = "friend", token = "")
		val group = GroupInstance(id = "g1", name = "Cool Group")

		vm.inviteFriendToGroup(current, friend, group)
		advanceUntilIdle()

		assertEquals(friend, repo.lastInvited)
        assertTrue(friend.notifications.isNotEmpty())
    }

    @Test
    fun `findUserById returns null when user not found`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepository()
        val userRepo = FakeUserRepoForInvite()  // empty users map
        val vm = InviteMemberViewModel(
            copyLinkUseCase = CopyLinkUseCase(repo),
            getUserUseCase = GetUserUseCase(userRepo),
            inviteFriendToGroupUseCase = InviteFriendToGroupUseCase(repo),
            ioDispatcher = dispatcher
        )
        val result = vm.findUserById("nonexistent")
        assertEquals(null, result)
    }

    @Test
    fun `inviteFriendToGroup with empty token skips push notification`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepository()
        val userRepo = FakeUserRepoForInvite()
        val vm = InviteMemberViewModel(
            copyLinkUseCase = CopyLinkUseCase(repo),
            getUserUseCase = GetUserUseCase(userRepo),
            inviteFriendToGroupUseCase = InviteFriendToGroupUseCase(repo),
            ioDispatcher = dispatcher
        )
        val current = UserInstance(uid = "me", name = "Me", image = "img")
        val friend = UserInstance(uid = "f1", token = "")  // empty token
        val group = GroupInstance(id = "g1", name = "Group")

        vm.inviteFriendToGroup(current, friend, group)
        advanceUntilIdle()

        // Notification still added to friend list even without push
        assertEquals(friend, repo.lastInvited)
        assertEquals(1, friend.notifications.size)
    }
}
