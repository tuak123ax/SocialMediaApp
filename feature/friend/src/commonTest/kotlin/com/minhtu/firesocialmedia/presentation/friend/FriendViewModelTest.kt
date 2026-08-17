package com.minhtu.firesocialmedia.presentation.friend

import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.SaveFriendUseCase
import com.minhtu.firesocialmedia.friend.entity.user.UserInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeFriendDbRepository : FriendDbRepository {
    val savedFriends = mutableListOf<Pair<String, List<String>>>()
    val savedFriendRequests = mutableListOf<Pair<String, List<String>>>()

    override suspend fun saveFriend(id: String, value: ArrayList<String>) {
        savedFriends += id to value.toList()
    }

    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
        savedFriendRequests += id to value.toList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {

    @Test
    fun `update methods replace friend and request state`() = runTest {
        val repo = FakeFriendDbRepository()
        val vm = FriendViewModel(
            saveFriendUseCase = SaveFriendUseCase(repo),
            saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        vm.updateFriendRequests(listOf("a", "b"))
        vm.updateFriends(listOf("x"))

        assertEquals(listOf("a", "b"), vm.friendRequestsStatus.value)
        assertEquals(listOf("x"), vm.friendStatus.value)
    }

    @Test
    fun `initial state is empty`() = runTest {
        val repo = FakeFriendDbRepository()
        val vm = FriendViewModel(
            saveFriendUseCase = SaveFriendUseCase(repo),
            saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        assertTrue(vm.friendRequestsStatus.value.isEmpty())
        assertTrue(vm.friendStatus.value.isEmpty())
    }

    @Test
    fun `acceptFriendRequest updates both users and persists friend data`() = runTest {
        val repo = FakeFriendDbRepository()
        val vm = FriendViewModel(
            saveFriendUseCase = SaveFriendUseCase(repo),
            saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val currentUser = UserInstance(uid = "current", friendRequests = arrayListOf("requester"))
        val requester = UserInstance(uid = "requester")

        vm.acceptFriendRequest(requester, currentUser)
        advanceUntilIdle()

        assertFalse(currentUser.friendRequests.contains("requester"))
        assertTrue(currentUser.friends.contains("requester"))
        assertTrue(requester.friends.contains("current"))
        assertEquals(listOf("current"), repo.savedFriends.last { it.first == "requester" }.second)
        assertEquals(emptyList(), repo.savedFriendRequests.last().second)
        assertEquals(listOf("requester"), vm.friendStatus.value)
    }

    @Test
    fun `acceptFriendRequest does not duplicate existing friendship`() = runTest {
        val repo = FakeFriendDbRepository()
        val vm = FriendViewModel(
            saveFriendUseCase = SaveFriendUseCase(repo),
            saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val currentUser = UserInstance(
            uid = "current",
            friendRequests = arrayListOf("requester"),
            friends = arrayListOf("requester")
        )
        val requester = UserInstance(uid = "requester", friends = arrayListOf("current"))

        vm.acceptFriendRequest(requester, currentUser)
        advanceUntilIdle()

        assertEquals(1, currentUser.friends.count { it == "requester" })
        assertEquals(1, requester.friends.count { it == "current" })
    }

    @Test
    fun `rejectFriendRequest removes pending request and does not add friend`() = runTest {
        val repo = FakeFriendDbRepository()
        val vm = FriendViewModel(
            saveFriendUseCase = SaveFriendUseCase(repo),
            saveFriendRequestUseCase = SaveFriendRequestUseCase(repo),
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val currentUser = UserInstance(uid = "current", friendRequests = arrayListOf("requester"))
        val requester = UserInstance(uid = "requester")

        vm.rejectFriendRequest(requester, currentUser)
        advanceUntilIdle()

        assertFalse(currentUser.friendRequests.contains("requester"))
        assertTrue(currentUser.friends.isEmpty())
        assertEquals(emptyList(), vm.friendRequestsStatus.value)
        assertTrue(repo.savedFriends.isEmpty())
        assertEquals("current", repo.savedFriendRequests.single().first)
    }
}
