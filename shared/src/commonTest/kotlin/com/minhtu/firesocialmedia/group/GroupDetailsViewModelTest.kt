package com.minhtu.firesocialmedia.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.GroupDetailsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class DetailsRepo : GroupRepository {
    var groupInfo: GroupInstance = GroupInstance(id = "g1", name = "G")
    var notificationState: Boolean = false
    var updateNotificationResult: Boolean = true
    var joinResult: Boolean = true
    var leaveResult: Boolean = true
    var deleteGroupResult: Boolean = true

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String): Boolean = true
    override suspend fun getAllGroups(userId: String): Set<GroupInstance> = emptySet()
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance = groupInfo
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String): Boolean = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String): Boolean {
        notificationState = newStatus
        return updateNotificationResult
    }
    override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = hashMapOf()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String): Boolean = notificationState
    override suspend fun copyLink(copyData: String) {}
    override suspend fun inviteFriendToGroup(friend: UserInstance) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance): Boolean = joinResult
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance): Boolean = leaveResult
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance): Boolean = deleteGroupResult
    override suspend fun removeMember(member: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance): Boolean = true
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> = emptyList()
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class GroupDetailsViewModelTest {

    private fun vm(repo: DetailsRepo, dispatcher: CoroutineDispatcher): GroupDetailsViewModel {
        return GroupDetailsViewModel(
            fetchGroupInfoUseCase = FetchGroupInfoUseCase(repo),
            updateNotificationStatusUseCase = UpdateNotificationStatusUseCase(repo),
            fetchNotificationStateUseCase = FetchNotificationStateUseCase(repo),
            findGroupByIdUseCase = FindGroupByIdUseCase(repo),
            joinGroupUseCase = JoinGroupUseCase(repo),
            leaveGroupUseCase = LeaveGroupUseCase(repo),
            leaveAndDeleteGroupUseCase = LeaveAndDeleteGroupUseCase(repo),
            ioDispatcher = dispatcher
        )
    }

    @Test
    fun `fetchGroupInfo updates state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = DetailsRepo().apply { groupInfo = GroupInstance(id = "gid", name = "GroupName") }
        val viewModel = vm(repo, dispatcher)

        viewModel.fetchGroupInfo("gid")
        advanceUntilIdle()

        assertEquals("gid", viewModel.fetchGroupInfoState.value?.id)
        assertEquals("GroupName", viewModel.fetchGroupInfoState.value?.name)
        Dispatchers.resetMain()
    }

    @Test
    fun `notification flow fetch and update toggles value and sets result`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = DetailsRepo().apply {
            notificationState = false
            updateNotificationResult = true
        }
        val viewModel = vm(repo, dispatcher)

        viewModel.fetchNotificationState("u1", "g1")
        advanceUntilIdle()
        assertEquals(false, viewModel.notificationState.value)

        viewModel.updateNotificationStatus("g1", "u1")
        advanceUntilIdle()
        // toggled from false -> true
        assertEquals(true, viewModel.notificationState.value)
        // update result propagated
        assertEquals(true, viewModel.updateNotificationState.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `requestFindGroupDetailsById sets deep link state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo().apply { groupInfo = GroupInstance(id = "g2", name = "DeepLinkGroup") }
        val viewModel = vm(repo, dispatcher)

        viewModel.requestFindGroupDetailsById("g2")
        advanceUntilIdle()

        assertEquals("g2", viewModel.groupDetailsFromDeepLink.value?.id)
        assertEquals("DeepLinkGroup", viewModel.groupDetailsFromDeepLink.value?.name)
    }

    @Test
    fun `joinGroup updates status`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo().apply { joinResult = true }
        val viewModel = vm(repo, dispatcher)

        viewModel.joinGroup(UserInstance(uid = "u1"), GroupInstance(id = "g1"))
        advanceUntilIdle()

        assertEquals(true, viewModel.joinGroupStatus.value)
        viewModel.resetJoinGroupState()
        assertEquals(null, viewModel.joinGroupStatus.value)
    }

    @Test
    fun `leaveGroup removes membership and calls correct use case`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo().apply {
            leaveResult = true
            deleteGroupResult = true
        }
        val viewModel = vm(repo, dispatcher)

        val user = UserInstance(uid = "u1").apply {
            groups["g1"] = GroupInstance(id = "g1")
        }

        // Case 1: more than 1 member -> leave only
        val groupMulti = GroupInstance(id = "g1").apply {
            members["u1"] = "admin"
            members["u2"] = "member"
        }
        viewModel.leaveGroup(user, groupMulti)
        advanceUntilIdle()
        assertEquals(true, viewModel.leaveGroupStatus.value)
        assertTrue("u1" !in groupMulti.members.keys)
        assertTrue("g1" !in user.groups.keys)
        viewModel.resetLeaveGroupStatus()

        // Reset user and group for case 2
        user.groups["g2"] = GroupInstance(id = "g2")
        val groupSolo = GroupInstance(id = "g2").apply {
            members["u1"] = "admin"
        }
        viewModel.leaveGroup(user, groupSolo)
        advanceUntilIdle()
        assertEquals(true, viewModel.leaveGroupStatus.value)
        assertTrue("u1" !in groupSolo.members.keys)
        assertTrue("g2" !in user.groups.keys)
    }

    @Test
    fun `resetFetchGroupInfoState clears state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = DetailsRepo().apply { groupInfo = GroupInstance(id = "gid", name = "Test") }
        val viewModel = vm(repo, dispatcher)
        viewModel.fetchGroupInfo("gid")
        advanceUntilIdle()
        assertEquals("gid", viewModel.fetchGroupInfoState.value?.id)

        viewModel.resetFetchGroupInfoState()
        assertEquals(null, viewModel.fetchGroupInfoState.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `joinGroup failure sets status to false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo().apply { joinResult = false }
        val viewModel = vm(repo, dispatcher)

        viewModel.joinGroup(UserInstance(uid = "u1"), GroupInstance(id = "g1"))
        advanceUntilIdle()

        assertEquals(false, viewModel.joinGroupStatus.value)
    }

    @Test
    fun `init populates visibleImages with first page`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo()
        val viewModel = vm(repo, dispatcher)
        val posts = (1..50).map { NewsInstance(id = "n$it", image = "img$it") }

        viewModel.init(posts)

        // Default page size is 30
        assertEquals(30, viewModel.visibleImages.size)
    }

    @Test
    fun `loadMore appends next page of visibleImages`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo()
        val viewModel = vm(repo, dispatcher)
        val posts = (1..50).map { NewsInstance(id = "n$it", image = "img$it") }
        viewModel.init(posts)
        assertEquals(30, viewModel.visibleImages.size)

        viewModel.loadMore()

        assertEquals(50, viewModel.visibleImages.size)
    }

    @Test
    fun `onPostsUpdated adds new posts at top`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = DetailsRepo()
        val viewModel = vm(repo, dispatcher)
        val initial = (1..5).map { NewsInstance(id = "n$it", image = "img$it", timePosted = it.toLong()) }
        viewModel.init(initial)
        assertEquals(5, viewModel.visibleImages.size)

        val updated = initial + NewsInstance(id = "n6", image = "newImg", timePosted = 100L)
        viewModel.onPostsUpdated(updated)

        // New image inserted at top (newest first)
        assertEquals("newImg", viewModel.visibleImages.first())
        assertEquals(6, viewModel.visibleImages.size)
    }
}


