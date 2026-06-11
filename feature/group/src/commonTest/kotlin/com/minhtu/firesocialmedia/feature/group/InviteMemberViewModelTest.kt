package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.CopyLinkUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.InviteFriendToGroupUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.invitemember.InviteMemberViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeInviteGroupRepo : GroupRepository {
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
    override suspend fun getGroupConfigs(userId: String, groupId: String) =
        com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs()
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
    fun copyLinkForwardsToRepository() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepo()
        val vm = InviteMemberViewModel(
            CopyLinkUseCase(repo), GetUserUseCase(FakeUserRepoForInvite()),
            InviteFriendToGroupUseCase(repo), dispatcher
        )
        vm.copyLink("https://link.test")
        advanceUntilIdle()
        assertEquals("https://link.test", repo.lastCopied)
    }

    @Test
    fun findUserByIdReturnsUserFromRepository() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepo()
        val userRepo = FakeUserRepoForInvite().apply { users["u1"] = UserInstance(uid = "u1", name = "Alice") }
        val vm = InviteMemberViewModel(CopyLinkUseCase(repo), GetUserUseCase(userRepo), InviteFriendToGroupUseCase(repo), dispatcher)
        val user = vm.findUserById("u1")
        assertNotNull(user)
        assertEquals("Alice", user.name)
    }

    @Test
    fun findUserByIdReturnsNullWhenNotFound() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepo()
        val vm = InviteMemberViewModel(
            CopyLinkUseCase(repo), GetUserUseCase(FakeUserRepoForInvite()),
            InviteFriendToGroupUseCase(repo), dispatcher
        )
        assertNull(vm.findUserById("unknown"))
    }

    @Test
    fun inviteFriendAddsNotificationAndCallsRepository() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepo()
        val vm = InviteMemberViewModel(
            CopyLinkUseCase(repo), GetUserUseCase(FakeUserRepoForInvite()),
            InviteFriendToGroupUseCase(repo), dispatcher
        )
        val current = UserInstance(uid = "me", name = "Me", image = "img")
        val friend = UserInstance(uid = "friend", token = "")
        val group = GroupInstance(id = "g1", name = "Group")
        vm.inviteFriendToGroup(current, friend, group)
        advanceUntilIdle()
        assertEquals(friend, repo.lastInvited)
        assertTrue(friend.notifications.isNotEmpty())
    }

    @Test
    fun inviteFriendWithEmptyTokenStillAddsNotification() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeInviteGroupRepo()
        val vm = InviteMemberViewModel(
            CopyLinkUseCase(repo), GetUserUseCase(FakeUserRepoForInvite()),
            InviteFriendToGroupUseCase(repo), dispatcher
        )
        val current = UserInstance(uid = "me", name = "Me", image = "img")
        val friend = UserInstance(uid = "f1", token = "")
        vm.inviteFriendToGroup(current, friend, GroupInstance(id = "g1", name = "Group"))
        advanceUntilIdle()
        assertEquals(1, friend.notifications.size)
    }
}

