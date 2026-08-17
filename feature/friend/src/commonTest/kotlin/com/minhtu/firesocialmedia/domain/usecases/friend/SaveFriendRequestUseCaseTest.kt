package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class SaveFriendRequestFakeRepository : FriendDbRepository {
    val savedFriends = mutableListOf<Pair<String, List<String>>>()
    val savedFriendRequests = mutableListOf<Pair<String, List<String>>>()

    override suspend fun saveFriend(id: String, value: ArrayList<String>) {
        savedFriends += id to value.toList()
    }

    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
        savedFriendRequests += id to value.toList()
    }
}

class SaveFriendRequestUseCaseTest {

    @Test
    fun `invoke delegates to repository saveFriendRequest`() = runTest {
        val repo = SaveFriendRequestFakeRepository()
        val useCase = SaveFriendRequestUseCase(repo)

        useCase.invoke("user1", arrayListOf("requesterA"))

        assertEquals(1, repo.savedFriendRequests.size)
        assertEquals("user1" to listOf("requesterA"), repo.savedFriendRequests.single())
        assertEquals(0, repo.savedFriends.size)
    }

    @Test
    fun `invoke with empty list saves empty request list`() = runTest {
        val repo = SaveFriendRequestFakeRepository()
        val useCase = SaveFriendRequestUseCase(repo)

        useCase.invoke("user1", arrayListOf())

        assertEquals("user1" to emptyList(), repo.savedFriendRequests.single())
    }
}
