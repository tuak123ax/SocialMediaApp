package com.minhtu.firesocialmedia.domain.usecases.common.appinit

import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeUserRepository(
    private val currentUserUid: String? = null,
    private val usersById: Map<String, UserDTO?> = emptyMap(),
    private val searchResults: List<UserDTO>? = null
) : UserRepository {
    var lastRequestedUserId: String? = null
    var lastSearchedName: String? = null

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        lastRequestedUserId = userId
        return usersById[userId]
    }

    override suspend fun getCurrentUserUid(): String? = currentUserUid

    override suspend fun searchUserByName(name: String): List<UserDTO>? {
        lastSearchedName = name
        return searchResults
    }
}

class GetCurrentUserUidUseCaseTest {
    @Test
    fun `invoke returns uid from repository`() = runTest {
        val repo = FakeUserRepository(currentUserUid = "uid-1")
        val useCase = GetCurrentUserUidUseCase(repo)

        assertEquals("uid-1", useCase())
    }

    @Test
    fun `invoke returns null when no current user`() = runTest {
        val repo = FakeUserRepository(currentUserUid = null)
        val useCase = GetCurrentUserUidUseCase(repo)

        assertNull(useCase())
    }
}

class GetUserUseCaseTest {
    @Test
    fun `invoke maps dto to search user instance`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice", email = "alice@example.com")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val useCase = GetUserUseCase(repo)

        val result = useCase("u1", false)

        assertEquals("u1", result?.uid)
        assertEquals("Alice", result?.name)
        assertEquals("alice@example.com", result?.email)
        assertEquals("u1", repo.lastRequestedUserId)
    }

    @Test
    fun `invoke returns null when user not found`() = runTest {
        val repo = FakeUserRepository(usersById = emptyMap())
        val useCase = GetUserUseCase(repo)

        assertNull(useCase("missing", false))
    }
}

class SearchUserByNameUseCaseTest {
    @Test
    fun `invoke maps all matched dtos to search users`() = runTest {
        val results = listOf(
            UserDTO(uid = "u1", name = "Alice"),
            UserDTO(uid = "u2", name = "Alicia")
        )
        val repo = FakeUserRepository(searchResults = results)
        val useCase = SearchUserByNameUseCase(repo)

        val users = useCase("Ali")

        assertEquals(2, users.size)
        assertEquals(listOf("u1", "u2"), users.map { it.uid })
        assertEquals("Ali", repo.lastSearchedName)
    }

    @Test
    fun `invoke returns empty list when repository returns null`() = runTest {
        val repo = FakeUserRepository(searchResults = null)
        val useCase = SearchUserByNameUseCase(repo)

        val users = useCase("nobody")

        assertTrue(users.isEmpty())
    }

    @Test
    fun `invoke returns empty list when repository returns empty list`() = runTest {
        val repo = FakeUserRepository(searchResults = emptyList())
        val useCase = SearchUserByNameUseCase(repo)

        assertTrue(useCase("nobody").isEmpty())
    }
}
