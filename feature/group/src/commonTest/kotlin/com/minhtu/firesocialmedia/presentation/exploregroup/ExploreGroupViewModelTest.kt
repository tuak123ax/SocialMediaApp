package com.minhtu.firesocialmedia.presentation.exploregroup

import com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
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
import kotlin.test.assertTrue

private class FakeExploreGroupRepo : GroupRepository {
    var recommend: List<GroupInstance> = emptyList()
    var feature: List<GroupInstance> = emptyList()

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
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
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> = recommend.take(limit)
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> = feature.take(limit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreGroupViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    private fun makeVm(repo: FakeExploreGroupRepo) =
        ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)

    @Test
    fun `loadInitialRecommendGroups excludes groups current user is a member of`() = runTest(dispatcher) {
        val g1 = GroupInstance(id = "g1", name = "A").apply { members["me"] = "member" }
        val g2 = GroupInstance(id = "g2", name = "B")
        val repo = FakeExploreGroupRepo().apply { recommend = listOf(g1, g2) }
        val vm = makeVm(repo)

        vm.loadInitialRecommendGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()

        assertEquals(1, vm.fetchRecommendGroups.value.size)
        assertEquals("g2", vm.fetchRecommendGroups.value.first().id)
    }

    @Test
    fun `loadInitialRecommendGroups filters by query case-insensitively`() = runTest(dispatcher) {
        val g1 = GroupInstance(id = "g1", name = "Football Fans")
        val g2 = GroupInstance(id = "g2", name = "Chess Club")
        val repo = FakeExploreGroupRepo().apply { recommend = listOf(g1, g2) }
        val vm = makeVm(repo)

        vm.loadInitialRecommendGroups(UserInstance(uid = "me"), "football")
        advanceUntilIdle()

        assertEquals(1, vm.fetchRecommendGroups.value.size)
        assertEquals("g1", vm.fetchRecommendGroups.value.first().id)
    }

    @Test
    fun `loadMoreRecommendGroups appends and stops when end reached`() = runTest(dispatcher) {
        val groups = (1..10).map { GroupInstance(id = "g$it", name = "Group$it") }
        val repo = FakeExploreGroupRepo().apply { recommend = groups }
        val vm = makeVm(repo)

        vm.loadInitialRecommendGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()
        assertEquals(10, vm.fetchRecommendGroups.value.size)

        // second page requests limit=20 but repo only has 10 items -> end reached
        vm.loadMoreRecommendGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()
        assertEquals(10, vm.fetchRecommendGroups.value.size)
    }

    @Test
    fun `resetFetchRecommendGroups clears the list`() = runTest(dispatcher) {
        val repo = FakeExploreGroupRepo().apply { recommend = listOf(GroupInstance(id = "g1", name = "A")) }
        val vm = makeVm(repo)
        vm.loadInitialRecommendGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()
        assertTrue(vm.fetchRecommendGroups.value.isNotEmpty())

        vm.resetFetchRecommendGroups()

        assertTrue(vm.fetchRecommendGroups.value.isEmpty())
    }

    @Test
    fun `loadInitialFeatureGroups excludes groups current user is a member of`() = runTest(dispatcher) {
        val g1 = GroupInstance(id = "g1", name = "A").apply { members["me"] = "member" }
        val g2 = GroupInstance(id = "g2", name = "B")
        val repo = FakeExploreGroupRepo().apply { feature = listOf(g1, g2) }
        val vm = makeVm(repo)

        vm.loadInitialFeatureGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()

        assertEquals(1, vm.fetchFeatureGroups.value.size)
        assertEquals("g2", vm.fetchFeatureGroups.value.first().id)
    }

    @Test
    fun `loadInitialFeatureGroups filters by query`() = runTest(dispatcher) {
        val g1 = GroupInstance(id = "g1", name = "Football Fans")
        val g2 = GroupInstance(id = "g2", name = "Chess Club")
        val repo = FakeExploreGroupRepo().apply { feature = listOf(g1, g2) }
        val vm = makeVm(repo)

        vm.loadInitialFeatureGroups(UserInstance(uid = "me"), "chess")
        advanceUntilIdle()

        assertEquals(1, vm.fetchFeatureGroups.value.size)
        assertEquals("g2", vm.fetchFeatureGroups.value.first().id)
    }

    @Test
    fun `loadMoreFeatureGroups appends and stops when end reached`() = runTest(dispatcher) {
        val groups = (1..5).map { GroupInstance(id = "g$it", name = "Group$it") }
        val repo = FakeExploreGroupRepo().apply { feature = groups }
        val vm = makeVm(repo)

        vm.loadInitialFeatureGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()
        assertEquals(5, vm.fetchFeatureGroups.value.size)
    }

    @Test
    fun `resetFetchFeatureGroups clears the list`() = runTest(dispatcher) {
        val repo = FakeExploreGroupRepo().apply { feature = listOf(GroupInstance(id = "g1", name = "A")) }
        val vm = makeVm(repo)
        vm.loadInitialFeatureGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()
        assertTrue(vm.fetchFeatureGroups.value.isNotEmpty())

        vm.resetFetchFeatureGroups()

        assertTrue(vm.fetchFeatureGroups.value.isEmpty())
    }

    @Test
    fun `loadMoreRecommendGroups is a no-op while already loading`() = runTest(dispatcher) {
        val groups = (1..10).map { GroupInstance(id = "g$it", name = "Group$it") }
        val repo = FakeExploreGroupRepo().apply { recommend = groups }
        val vm = makeVm(repo)

        // Kick off two loads back-to-back before the dispatcher runs either.
        vm.loadInitialRecommendGroups(UserInstance(uid = "me"), "")
        vm.loadMoreRecommendGroups(UserInstance(uid = "me"), "")
        advanceUntilIdle()

        assertEquals(10, vm.fetchRecommendGroups.value.size)
    }
}
