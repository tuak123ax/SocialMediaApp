package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.core.domain.entity.settings.PollObject
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchGroupInfoUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchNotificationStateUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FindGroupByIdUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.JoinGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.LeaveAndDeleteGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.LeaveGroupUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.UpdateNotificationStatusUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.newsfeed.DeletePollUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.groupdetails.GroupDetailsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeDetailsGroupRepo : GroupRepository {
    var groupInfo: GroupInstance = GroupInstance(id = "g1", name = "Group")
    var notificationState: Boolean = false
    var updateNotificationResult: Boolean = true
    var joinResult: Boolean = true
    var leaveResult: Boolean = true
    var deleteGroupResult: Boolean = true

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
    override suspend fun fetchGroupInfo(groupId: String): GroupInstance = groupInfo
    override suspend fun saveNewToGroup(instance: NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String): Boolean {
        notificationState = newStatus
        return updateNotificationResult
    }
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) =
        com.minhtu.firesocialmedia.core.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = notificationState
    override suspend fun copyLink(copyData: String) {}
    override suspend fun inviteFriendToGroup(friend: UserInstance) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = joinResult
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = leaveResult
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = deleteGroupResult
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun fetchRecommendGroups(limit: Int) = emptyList<GroupInstance>()
    override suspend fun fetchFeatureGroups(limit: Int) = emptyList<GroupInstance>()
}

private val fakeNewsRepo = object : NewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? = null
    override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?) = null
    override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String) = true
    override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance) = true
    override suspend fun fetchPoll(pollId: String): PollObject? = null
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
    override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>) = true
}

private fun makeVm(repo: FakeDetailsGroupRepo, dispatcher: CoroutineDispatcher): GroupDetailsViewModel =
    GroupDetailsViewModel(
        fetchGroupInfoUseCase = FetchGroupInfoUseCase(repo),
        updateNotificationStatusUseCase = UpdateNotificationStatusUseCase(repo),
        fetchNotificationStateUseCase = FetchNotificationStateUseCase(repo),
        findGroupByIdUseCase = FindGroupByIdUseCase(repo),
        joinGroupUseCase = JoinGroupUseCase(repo),
        leaveGroupUseCase = LeaveGroupUseCase(repo),
        leaveAndDeleteGroupUseCase = LeaveAndDeleteGroupUseCase(repo),
        deletePollUseCase = DeletePollUseCase(fakeNewsRepo),
        ioDispatcher = dispatcher
    )

@OptIn(ExperimentalCoroutinesApi::class)
class GroupDetailsViewModelTest {

    @Test
    fun `fetchGroupInfo updates fetchGroupInfoState`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeDetailsGroupRepo().apply { groupInfo = GroupInstance(id = "gid", name = "TestGroup") }
        val vm = makeVm(repo, dispatcher)

        vm.fetchGroupInfo("gid")
        advanceUntilIdle()

        assertEquals("gid", vm.fetchGroupInfoState.value?.id)
        assertEquals("TestGroup", vm.fetchGroupInfoState.value?.name)
        Dispatchers.resetMain()
    }

    @Test
    fun `resetFetchGroupInfoState sets state to null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeDetailsGroupRepo().apply { groupInfo = GroupInstance(id = "gid", name = "Test") }
        val vm = makeVm(repo, dispatcher)
        vm.fetchGroupInfo("gid")
        advanceUntilIdle()
        assertNotNull(vm.fetchGroupInfoState.value)

        vm.resetFetchGroupInfoState()
        assertNull(vm.fetchGroupInfoState.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchNotificationState and updateNotificationStatus toggle state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = FakeDetailsGroupRepo().apply { notificationState = false; updateNotificationResult = true }
        val vm = makeVm(repo, dispatcher)

        vm.fetchNotificationState("u1", "g1")
        advanceUntilIdle()
        assertEquals(false, vm.notificationState.value)

        vm.updateNotificationStatus("g1", "u1")
        advanceUntilIdle()
        assertEquals(true, vm.notificationState.value)
        assertEquals(true, vm.updateNotificationState.value)
        Dispatchers.resetMain()
    }

    @Test
    fun `requestFindGroupDetailsById sets groupDetailsFromDeepLink`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo().apply { groupInfo = GroupInstance(id = "g2", name = "DeepLink") }
        val vm = makeVm(repo, dispatcher)

        vm.requestFindGroupDetailsById("g2")
        advanceUntilIdle()

        assertEquals("g2", vm.groupDetailsFromDeepLink.value?.id)
        assertEquals("DeepLink", vm.groupDetailsFromDeepLink.value?.name)
    }

    @Test
    fun `joinGroup success sets joinGroupStatus to true then can be reset`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo().apply { joinResult = true }
        val vm = makeVm(repo, dispatcher)

        vm.joinGroup(UserInstance(uid = "u1"), GroupInstance(id = "g1"))
        advanceUntilIdle()
        assertEquals(true, vm.joinGroupStatus.value)

        vm.resetJoinGroupState()
        assertNull(vm.joinGroupStatus.value)
    }

    @Test
    fun `joinGroup failure sets joinGroupStatus to false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo().apply { joinResult = false }
        val vm = makeVm(repo, dispatcher)

        vm.joinGroup(UserInstance(uid = "u1"), GroupInstance(id = "g1"))
        advanceUntilIdle()
        assertEquals(false, vm.joinGroupStatus.value)
    }

    @Test
    fun `leaveGroup with multiple members calls leaveGroup use case`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo().apply { leaveResult = true }
        val vm = makeVm(repo, dispatcher)
        val user = UserInstance(uid = "u1").apply { groups["g1"] = GroupInstance(id = "g1") }
        val group = GroupInstance(id = "g1").apply {
            members["u1"] = "admin"
            members["u2"] = "member"
        }

        vm.leaveGroup(user, group)
        advanceUntilIdle()

        assertEquals(true, vm.leaveGroupStatus.value)
        assertTrue("u1" !in group.members.keys)
        assertTrue("g1" !in user.groups.keys)
    }

    @Test
    fun `leaveGroup as sole member calls leaveAndDeleteGroup use case`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo().apply { deleteGroupResult = true }
        val vm = makeVm(repo, dispatcher)
        val user = UserInstance(uid = "u1").apply { groups["g2"] = GroupInstance(id = "g2") }
        val group = GroupInstance(id = "g2").apply { members["u1"] = "admin" }

        vm.leaveGroup(user, group)
        advanceUntilIdle()

        assertEquals(true, vm.leaveGroupStatus.value)
        assertTrue("u1" !in group.members.keys)
        assertTrue("g2" !in user.groups.keys)
    }

    @Test
    fun `resetLeaveGroupStatus sets state to null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo()
        val vm = makeVm(repo, dispatcher)
        val user = UserInstance(uid = "u1").apply { groups["g1"] = GroupInstance(id = "g1") }
        val group = GroupInstance(id = "g1").apply { members["u1"] = "admin" }
        vm.leaveGroup(user, group)
        advanceUntilIdle()

        vm.resetLeaveGroupStatus()
        assertNull(vm.leaveGroupStatus.value)
    }

    @Test
    fun `deletePoll success sets deletePollState to true`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo()
        val vm = makeVm(repo, dispatcher)
        val news = NewsInstance(id = "n1", pollId = "poll1")

        vm.deletePoll(news, "g1")
        advanceUntilIdle()

        assertEquals(true, vm.deletePollState.value)
        vm.resetDeletePollState()
        assertNull(vm.deletePollState.value)
    }

    @Test
    fun `deletePoll with null pollId does nothing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo()
        val vm = makeVm(repo, dispatcher)
        val news = NewsInstance(id = "n1", pollId = null)

        vm.deletePoll(news, "g1")
        advanceUntilIdle()

        assertNull(vm.deletePollState.value)
    }

    @Test
    fun `init populates visibleImages with first 30 items`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeDetailsGroupRepo()
        val vm = makeVm(repo, dispatcher)
        val posts = (1..50).map { NewsInstance(id = "n$it", image = "img$it") }

        vm.init(posts)

        assertEquals(30, vm.visibleImages.size)
    }

    @Test
    fun `loadMore appends next page of visibleImages`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeVm(FakeDetailsGroupRepo(), dispatcher)
        val posts = (1..50).map { NewsInstance(id = "n$it", image = "img$it") }
        vm.init(posts)
        assertEquals(30, vm.visibleImages.size)

        vm.loadMore()

        assertEquals(50, vm.visibleImages.size)
    }

    @Test
    fun `onPostsUpdated inserts new post at the top`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeVm(FakeDetailsGroupRepo(), dispatcher)
        val initial = (1..5).map { NewsInstance(id = "n$it", image = "img$it", timePosted = it.toLong()) }
        vm.init(initial)

        val updated = initial + NewsInstance(id = "n6", image = "newImg", timePosted = 100L)
        vm.onPostsUpdated(updated)

        assertEquals("newImg", vm.visibleImages.first())
        assertEquals(6, vm.visibleImages.size)
    }

    @Test
    fun `onPostsUpdated with same size does not modify visibleImages`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = makeVm(FakeDetailsGroupRepo(), dispatcher)
        val posts = (1..5).map { NewsInstance(id = "n$it", image = "img$it", timePosted = it.toLong()) }
        vm.init(posts)
        val sizeBefore = vm.visibleImages.size

        vm.onPostsUpdated(posts) // same list – no change

        assertEquals(sizeBefore, vm.visibleImages.size)
    }
}

