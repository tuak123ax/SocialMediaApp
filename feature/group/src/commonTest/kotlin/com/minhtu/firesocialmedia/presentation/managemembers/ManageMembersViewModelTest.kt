package com.minhtu.firesocialmedia.presentation.managemembers

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.DemoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.PromoteMemberUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.RemoveMemberUseCase
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeManageMembersGroupRepo : GroupRepository {
    var groupInfo: GroupInstance = GroupInstance(id = "g1", name = "Group")
    var removeResult: Boolean = true
    var promoteResult: Boolean = true
    var demoteResult: Boolean = true

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance = groupInfo
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = false
    override suspend fun copyLink(copyData: String) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = removeResult
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = promoteResult
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = demoteResult
    override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
    override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

private class FakeManageMembersUserRepo : UserRepository {
    val users = mutableMapOf<String, UserDTO?>()
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class ManageMembersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    private fun makeVm(groupRepo: FakeManageMembersGroupRepo, userRepo: FakeManageMembersUserRepo = FakeManageMembersUserRepo()) =
        ManageMembersViewModel(
            GetUserUseCase(userRepo),
            RemoveMemberUseCase(groupRepo),
            PromoteMemberUseCase(groupRepo),
            DemoteMemberUseCase(groupRepo),
            FetchGroupInfoUseCase(groupRepo),
            dispatcher
        )

    @Test
    fun `findUserById returns user from repository`() = runTest(dispatcher) {
        val userRepo = FakeManageMembersUserRepo().apply { users["u1"] = UserDTO(uid = "u1", name = "Alice") }
        val vm = makeVm(FakeManageMembersGroupRepo(), userRepo)

        val result = vm.findUserById("u1")

        assertEquals("Alice", result?.name)
    }

    @Test
    fun `fetchGroupInfo updates state and resetFetchGroupInfoState clears it`() = runTest(dispatcher) {
        val groupRepo = FakeManageMembersGroupRepo().apply { groupInfo = GroupInstance(id = "g1", name = "Test") }
        val vm = makeVm(groupRepo)

        vm.fetchGroupInfo("g1")
        advanceUntilIdle()
        assertEquals("Test", vm.fetchGroupInfoState.value?.name)

        vm.resetFetchGroupInfoState()
        assertNull(vm.fetchGroupInfoState.value)
    }

    @Test
    fun `removeMember removes member from cached group and clears member group id`() = runTest(dispatcher) {
        val group = GroupInstance(id = "g1").apply {
            members["u1"] = "admin"
            members["u2"] = "member"
        }
        val groupRepo = FakeManageMembersGroupRepo().apply { groupInfo = group; removeResult = true }
        val vm = makeVm(groupRepo)
        vm.fetchGroupInfo("g1")
        advanceUntilIdle()

        val member = UserInstance(uid = "u2").apply { groups.add("g1") }
        vm.removeMember(member)
        advanceUntilIdle()

        assertEquals(true, vm.removeMemberStatus.value)
        assertFalse(vm.fetchGroupInfoState.value!!.members.containsKey("u2"))
        assertFalse(member.groups.contains("g1"))
    }

    @Test
    fun `removeMember does nothing when no group is cached`() = runTest(dispatcher) {
        val groupRepo = FakeManageMembersGroupRepo()
        val vm = makeVm(groupRepo)

        vm.removeMember(UserInstance(uid = "u2"))
        advanceUntilIdle()

        assertNull(vm.removeMemberStatus.value)
    }

    @Test
    fun `promoteMember sets member role to admin`() = runTest(dispatcher) {
        val group = GroupInstance(id = "g1").apply { members["u2"] = "member" }
        val groupRepo = FakeManageMembersGroupRepo().apply { groupInfo = group; promoteResult = true }
        val vm = makeVm(groupRepo)
        vm.fetchGroupInfo("g1")
        advanceUntilIdle()

        vm.promoteMember(UserInstance(uid = "u2"))
        advanceUntilIdle()

        assertEquals(true, vm.promoteMemberStatus.value)
        assertEquals("admin", vm.fetchGroupInfoState.value?.members?.get("u2"))
        vm.resetPromoteMemberStatus()
        assertNull(vm.promoteMemberStatus.value)
    }

    @Test
    fun `demoteMember sets member role to member`() = runTest(dispatcher) {
        val group = GroupInstance(id = "g1").apply { members["u2"] = "admin" }
        val groupRepo = FakeManageMembersGroupRepo().apply { groupInfo = group; demoteResult = true }
        val vm = makeVm(groupRepo)
        vm.fetchGroupInfo("g1")
        advanceUntilIdle()

        vm.demoteMember(UserInstance(uid = "u2"))
        advanceUntilIdle()

        assertEquals(true, vm.demoteMemberStatus.value)
        assertEquals("member", vm.fetchGroupInfoState.value?.members?.get("u2"))
        vm.resetDemoteMemberStatus()
        assertNull(vm.demoteMemberStatus.value)
    }

    @Test
    fun `fetchAdminList resolves users and add remove admin list mutate list`() = runTest(dispatcher) {
        val userRepo = FakeManageMembersUserRepo().apply {
            users["u1"] = UserDTO(uid = "u1", name = "Admin1")
            users["u2"] = UserDTO(uid = "u2", name = "Admin2")
        }
        val vm = makeVm(FakeManageMembersGroupRepo(), userRepo)

        vm.fetchAdminList(setOf("u1", "u2"))
        advanceUntilIdle()
        assertEquals(2, vm.fetchAdminListStatus.value.size)

        val extra = UserInstance(uid = "u3", name = "Admin3")
        vm.addAdminToList(extra)
        assertEquals(3, vm.fetchAdminListStatus.value.size)

        vm.removeAdminFromList(extra)
        assertEquals(2, vm.fetchAdminListStatus.value.size)
    }

    @Test
    fun `fetchMemberList resolves users and add remove member list mutate list`() = runTest(dispatcher) {
        val userRepo = FakeManageMembersUserRepo().apply {
            users["u1"] = UserDTO(uid = "u1", name = "Member1")
        }
        val vm = makeVm(FakeManageMembersGroupRepo(), userRepo)

        vm.fetchMemberList(setOf("u1"))
        advanceUntilIdle()
        assertEquals(1, vm.fetchMemberListStatus.value.size)

        val extra = UserInstance(uid = "u4", name = "Member2")
        vm.addMemberToList(extra)
        assertEquals(2, vm.fetchMemberListStatus.value.size)

        vm.removeMemberFromList(extra)
        assertEquals(1, vm.fetchMemberListStatus.value.size)
    }

    @Test
    fun `resetAdminAndMemberList clears both lists`() = runTest(dispatcher) {
        val userRepo = FakeManageMembersUserRepo().apply { users["u1"] = UserDTO(uid = "u1", name = "A") }
        val vm = makeVm(FakeManageMembersGroupRepo(), userRepo)
        vm.fetchAdminList(setOf("u1"))
        vm.fetchMemberList(setOf("u1"))
        advanceUntilIdle()
        assertTrue(vm.fetchAdminListStatus.value.isNotEmpty())
        assertTrue(vm.fetchMemberListStatus.value.isNotEmpty())

        vm.resetAdminAndMemberList()

        assertTrue(vm.fetchAdminListStatus.value.isEmpty())
        assertTrue(vm.fetchMemberListStatus.value.isEmpty())
    }
}
