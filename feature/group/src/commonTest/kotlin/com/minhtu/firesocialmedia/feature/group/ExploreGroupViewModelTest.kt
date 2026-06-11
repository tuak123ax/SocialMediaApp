package com.minhtu.firesocialmedia.feature.group

import com.minhtu.firesocialmedia.core.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.feature.group.presentation.exploregroup.ExploreGroupViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeExploreGroupRepo : GroupRepository {
    var recommendBacking: List<GroupInstance> = emptyList()
    var featureBacking: List<GroupInstance> = emptyList()
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
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> =
        recommendBacking.take(min(limit, recommendBacking.size))
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> =
        featureBacking.take(min(limit, featureBacking.size))
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreGroupViewModelTest {

    @Test
    fun excludesMembersFromRecommend() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo()
        val user = UserInstance(uid = "u1")
        repo.recommendBacking = (1..10).map {
            val g = GroupInstance(id = "g$it", name = "group$it")
            if (it % 2 == 0) g.members["u1"] = "member"
            g
        }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialRecommendGroups(user, "")
        advanceUntilIdle()
        assertTrue(vm.fetchRecommendGroups.value.none { "u1" in it.members })
    }

    @Test
    fun filtersByNameQuery() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo()
        repo.recommendBacking = (1..10).map {
            GroupInstance(id = "g$it", name = if (it % 3 == 0) "kotlin-$it" else "java-$it")
        }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialRecommendGroups(UserInstance(uid = "u1"), "kotlin")
        advanceUntilIdle()
        assertTrue(vm.fetchRecommendGroups.value.all { it.name.contains("kotlin", ignoreCase = true) })
    }

    @Test
    fun loadMoreAppendsRemainingRecommendGroups() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo()
        repo.recommendBacking = (1..15).map { GroupInstance(id = "g$it", name = "g$it") }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialRecommendGroups(UserInstance(uid = "u1"), "")
        advanceUntilIdle()
        vm.loadMoreRecommendGroups(UserInstance(uid = "u1"), "")
        advanceUntilIdle()
        assertEquals(15, vm.fetchRecommendGroups.value.size)
    }

    @Test
    fun loadMoreBlockedAfterEndReached() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo().apply { recommendBacking = (1..3).map { GroupInstance(id = "r$it") } }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialRecommendGroups(UserInstance(uid = "u1"), "")
        advanceUntilIdle()
        assertEquals(3, vm.fetchRecommendGroups.value.size)
        vm.loadMoreRecommendGroups(UserInstance(uid = "u1"), "")
        advanceUntilIdle()
        assertEquals(3, vm.fetchRecommendGroups.value.size)
    }

    @Test
    fun resetRecommendClearsList() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo().apply { recommendBacking = (1..5).map { GroupInstance(id = "g$it") } }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialRecommendGroups(UserInstance(uid = "u1"), "")
        advanceUntilIdle()
        assertTrue(vm.fetchRecommendGroups.value.isNotEmpty())
        vm.resetFetchRecommendGroups()
        assertEquals(0, vm.fetchRecommendGroups.value.size)
    }

    @Test
    fun featureGroupsExcludesMembers() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo()
        val user = UserInstance(uid = "u2")
        repo.featureBacking = (1..8).map {
            val g = GroupInstance(id = "f$it", name = "f$it")
            if (it % 3 == 0) g.members["u2"] = "member"
            g
        }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialFeatureGroups(user, "")
        advanceUntilIdle()
        assertTrue(vm.fetchFeatureGroups.value.none { "u2" in it.members })
    }

    @Test
    fun loadMoreFeatureGroupsAppends() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo()
        repo.featureBacking = (1..17).map { GroupInstance(id = "f$it", name = "f$it") }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialFeatureGroups(UserInstance(uid = "u2"), "")
        advanceUntilIdle()
        vm.loadMoreFeatureGroups(UserInstance(uid = "u2"), "")
        advanceUntilIdle()
        assertEquals(17, vm.fetchFeatureGroups.value.size)
    }

    @Test
    fun resetFeatureGroupsClearsList() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeExploreGroupRepo().apply { featureBacking = (1..5).map { GroupInstance(id = "f$it") } }
        val vm = ExploreGroupViewModel(FetchRecommendGroupsUseCase(repo), FetchFeatureGroupsUseCase(repo), dispatcher)
        vm.loadInitialFeatureGroups(UserInstance(uid = "u2"), "")
        advanceUntilIdle()
        vm.resetFetchFeatureGroups()
        assertEquals(0, vm.fetchFeatureGroups.value.size)
    }
}

