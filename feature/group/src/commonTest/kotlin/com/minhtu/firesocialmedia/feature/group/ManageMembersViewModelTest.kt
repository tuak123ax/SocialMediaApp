package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.managemembers.ManageMembersViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeManageGroupRepo : GroupRepository {
    var removeResult = true
    var promoteResult = true
    var demoteResult = true
    override suspend fun removeMember(member: UserInstance, group: GroupInstance): Boolean = removeResult
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance): Boolean = promoteResult
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance): Boolean = demoteResult
    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
    override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) =
        com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = false
    override suspend fun copyLink(copyData: String) {}
    override suspend fun inviteFriendToGroup(friend: UserInstance) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
    override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

private class FakeManageUserRepo : UserRepository {
    val users = mutableMapOf<String, UserInstance?>()
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String) = emptyList<UserInstance>()
}

private fun makeManageVm(
    groupRepo: FakeManageGroupRepo,
    userRepo: FakeManageUserRepo,
    dispatcher: CoroutineDispatcher
) = ManageMembersViewModel(
    GetUserUseCase(userRepo), RemoveMemberUseCase(groupRepo),
    PromoteMemberUseCase(groupRepo), DemoteMemberUseCase(groupRepo), dispatcher
)

@OptIn(ExperimentalCoroutinesApi::class)
class ManageMembersViewModelTest {

    @Test
    fun removeMemberSuccessUpdatesStatusAndMaps() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeManageVm(FakeManageGroupRepo(), FakeManageUserRepo(), dispatcher)
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
        assertNull(vm.removeMemberStatus.value)
    }

    @Test
    fun removeMemberFailureSetsStatusToFalse() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeManageVm(FakeManageGroupRepo().apply { removeResult = false }, FakeManageUserRepo(), dispatcher)
        val member = UserInstance(uid = "u1")
        val group = GroupInstance(id = "g1")
        group.members["u1"] = "member"
        member.groups["g1"] = group
        vm.removeMember(member, group)
        advanceUntilIdle()
        assertEquals(false, vm.removeMemberStatus.value)
    }

    @Test
    fun promoteMemberSetsRoleToAdmin() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeManageVm(FakeManageGroupRepo(), FakeManageUserRepo(), dispatcher)
        val member = UserInstance(uid = "u2")
        val group = GroupInstance(id = "g2")
        vm.promoteMember(member, group)
        advanceUntilIdle()
        assertEquals(true, vm.promoteMemberStatus.value)
        assertEquals("admin", group.members["u2"])
        vm.resetPromoteMemberStatus()
        assertNull(vm.promoteMemberStatus.value)
    }

    @Test
    fun demoteMemberSetsRoleToMember() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeManageVm(FakeManageGroupRepo(), FakeManageUserRepo(), dispatcher)
        val member = UserInstance(uid = "u3")
        val group = GroupInstance(id = "g3")
        group.members["u3"] = "admin"
        vm.demoteMember(member, group)
        advanceUntilIdle()
        assertEquals(true, vm.demoteMemberStatus.value)
        assertEquals("member", group.members["u3"])
        vm.resetDemoteMemberStatus()
        assertNull(vm.demoteMemberStatus.value)
    }

    @Test
    fun fetchAdminAndMemberListsLoadsUsers() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val userRepo = FakeManageUserRepo().apply {
            users["a1"] = UserInstance(uid = "a1", name = "Admin 1")
            users["a2"] = UserInstance(uid = "a2", name = "Admin 2")
            users["m1"] = UserInstance(uid = "m1", name = "Member 1")
        }
        val vm = makeManageVm(FakeManageGroupRepo(), userRepo, dispatcher)
        vm.fetchAdminList(setOf("a1", "a2"))
        vm.fetchMemberList(setOf("m1"))
        advanceUntilIdle()
        assertEquals(2, vm.fetchAdminListStatus.value.size)
        assertEquals(1, vm.fetchMemberListStatus.value.size)
    }

    @Test
    fun addRemoveAdminAndMemberHelpers() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeManageVm(FakeManageGroupRepo(), FakeManageUserRepo(), dispatcher)
        val admin = UserInstance(uid = "a3")
        val member = UserInstance(uid = "m2")
        vm.addAdminToList(admin)
        vm.addMemberToList(member)
        assertTrue(vm.fetchAdminListStatus.value.contains(admin))
        assertTrue(vm.fetchMemberListStatus.value.contains(member))
        vm.removeAdminFromList(admin)
        vm.removeMemberFromList(member)
        assertTrue(!vm.fetchAdminListStatus.value.contains(admin))
        assertTrue(!vm.fetchMemberListStatus.value.contains(member))
    }

    @Test
    fun resetAdminAndMemberListClearsAll() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val userRepo = FakeManageUserRepo().apply {
            users["a1"] = UserInstance(uid = "a1")
            users["m1"] = UserInstance(uid = "m1")
        }
        val vm = makeManageVm(FakeManageGroupRepo(), userRepo, dispatcher)
        vm.fetchAdminList(setOf("a1"))
        vm.fetchMemberList(setOf("m1"))
        advanceUntilIdle()
        vm.resetAdminAndMemberList()
        assertTrue(vm.fetchAdminListStatus.value.isEmpty())
        assertTrue(vm.fetchMemberListStatus.value.isEmpty())
    }

    @Test
    fun findUserByIdReturnsCorrectUser() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val userRepo = FakeManageUserRepo().apply { users["u5"] = UserInstance(uid = "u5", name = "Charlie") }
        val vm = makeManageVm(FakeManageGroupRepo(), userRepo, dispatcher)
        assertEquals("Charlie", vm.findUserById("u5")?.name)
        assertNull(vm.findUserById("unknown"))
    }
}

