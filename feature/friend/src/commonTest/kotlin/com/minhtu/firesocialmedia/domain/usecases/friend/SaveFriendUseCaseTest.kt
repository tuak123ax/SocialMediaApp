package com.minhtu.firesocialmedia.domain.usecases.friend

import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class SaveFriendFakeRepository : FriendDbRepository {
    val savedFriends = mutableListOf<Pair<String, List<String>>>()
    val savedFriendRequests = mutableListOf<Pair<String, List<String>>>()

    override suspend fun saveFriend(id: String, value: ArrayList<String>) {
        savedFriends += id to value.toList()
    }

    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
        savedFriendRequests += id to value.toList()
    }
}

class SaveFriendUseCaseTest {

    @Test
    fun `invoke delegates to repository saveFriend`() = runTest {
        val repo = SaveFriendFakeRepository()
        val useCase = SaveFriendUseCase(repo)

        useCase.invoke("user1", arrayListOf("friendA", "friendB"))

        assertEquals(1, repo.savedFriends.size)
        assertEquals("user1" to listOf("friendA", "friendB"), repo.savedFriends.single())
        assertEquals(0, repo.savedFriendRequests.size)
    }

    @Test
    fun `invoke with empty list saves empty friend list`() = runTest {
        val repo = SaveFriendFakeRepository()
        val useCase = SaveFriendUseCase(repo)

        useCase.invoke("user1", arrayListOf())

        assertEquals("user1" to emptyList(), repo.savedFriends.single())
    }
}
