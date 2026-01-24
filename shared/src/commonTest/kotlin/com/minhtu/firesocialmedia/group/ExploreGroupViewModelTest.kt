package com.minhtu.firesocialmedia.group

import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.repository.GroupRepository
import com.minhtu.firesocialmedia.domain.usecases.group.FetchFeatureGroupsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.FetchRecommendGroupsUseCase
import com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group.ExploreGroupViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class ExploreRepo : GroupRepository {
    var recommendBacking: List<GroupInstance> = emptyList()
    var featureBacking: List<GroupInstance> = emptyList()

    override suspend fun saveGroupAndUserGroups(group: GroupInstance, userId: String) = true
    override suspend fun getAllGroups(userId: String) = emptySet<GroupInstance>()
    override suspend fun fetchGroupInfo(groupId: String) = GroupInstance()
    override suspend fun saveNewToGroup(instance: com.minhtu.firesocialmedia.domain.entity.news.NewsInstance, groupId: String) = true
    override suspend fun updateNotificationStatus(newStatus: Boolean, groupId: String, userId: String) = true
    override suspend fun getAllMembersInGroup(groupId: String) = hashMapOf<String, String>()
    override suspend fun getGroupConfigs(userId: String, groupId: String) = com.minhtu.firesocialmedia.domain.entity.group.GroupConfigs()
    override suspend fun fetchNotificationState(userId: String, groupId: String) = false
    override suspend fun copyLink(copyData: String) {}
    override suspend fun inviteFriendToGroup(friend: UserInstance) {}
    override suspend fun joinGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun leaveAndDeleteGroup(user: UserInstance, group: GroupInstance) = true
    override suspend fun removeMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun promoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun demoteMember(member: UserInstance, group: GroupInstance) = true
    override suspend fun fetchRecommendGroups(limit: Int): List<GroupInstance> = recommendBacking.take(min(limit, recommendBacking.size))
    override suspend fun fetchFeatureGroups(limit: Int): List<GroupInstance> = featureBacking.take(min(limit, featureBacking.size))
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreGroupViewModelTest {

    @Test
    fun `load initial and more recommend groups with filters`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = ExploreRepo()
        val currentUser = UserInstance(uid = "u1")
        // 15 groups, some include current user and some names filtered
        repo.recommendBacking = (1..15).map {
            val g = GroupInstance(id = "g$it", name = if (it % 3 == 0) "abc-$it" else "name-$it")
            if (it % 5 == 0) g.members["u1"] = "member"
            g
        }

        val vm = ExploreGroupViewModel(
            fetchRecommendGroupsUseCase = FetchRecommendGroupsUseCase(repo),
            fetchFeatureGroupsUseCase = FetchFeatureGroupsUseCase(repo),
            ioDispatcher = dispatcher
        )

        // Initial load with blank query
        vm.loadInitialRecommendGroups(currentUser, "")
        advanceUntilIdle()
        // Should load up to first page size (10) minus those where user is a member (5,10,15 -> 2 of the first 10)
        val firstPage = vm.fetchRecommendGroups.value
        assertTrue(firstPage.size in 8..10)
        assertTrue(firstPage.none { currentUser.uid in it.members })

        // Load more -> expands up to full available (<=15) excluding membership
        vm.loadMoreRecommendGroups(currentUser, "")
        advanceUntilIdle()
        val secondPage = vm.fetchRecommendGroups.value
        val expectedTotal = repo.recommendBacking.count { currentUser.uid !in it.members }
        assertEquals(expectedTotal, secondPage.size)

        // Apply query filter
        vm.loadInitialRecommendGroups(currentUser, "abc")
        advanceUntilIdle()
        val filtered = vm.fetchRecommendGroups.value
        assertTrue(filtered.all { it.name.contains("abc", true) })
        assertTrue(filtered.none { currentUser.uid in it.members })
    }

    @Test
    fun `load initial and more feature groups with filters`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = ExploreRepo()
        val currentUser = UserInstance(uid = "u2")
        repo.featureBacking = (1..17).map {
            val g = GroupInstance(id = "f$it", name = if (it % 4 == 0) "tag-$it" else "other-$it")
            if (it % 6 == 0) g.members["u2"] = "member"
            g
        }

        val vm = ExploreGroupViewModel(
            fetchRecommendGroupsUseCase = FetchRecommendGroupsUseCase(repo),
            fetchFeatureGroupsUseCase = FetchFeatureGroupsUseCase(repo),
            ioDispatcher = dispatcher
        )

        vm.loadInitialFeatureGroups(currentUser, "tag")
        advanceUntilIdle()
        val first = vm.fetchFeatureGroups.value
        assertTrue(first.all { it.name.contains("tag", true) })
        assertTrue(first.none { currentUser.uid in it.members })
        // Size should be <= page size (10)
        assertTrue(first.size <= 10)

        vm.loadMoreFeatureGroups(currentUser, "tag")
        advanceUntilIdle()
        val second = vm.fetchFeatureGroups.value
        val expected = repo.featureBacking
            .filter { currentUser.uid !in it.members }
            .filter { it.name.contains("tag", true) }
            .size
        assertEquals(expected, second.size)
    }
}


