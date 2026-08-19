package com.minhtu.firesocialmedia.domain.usecases.common.friend

import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class GetUserFakeRepository(
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    var lastRequestedIsCurrentUser: Boolean? = null

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        lastRequestedIsCurrentUser = isCurrentUser
        return usersById[userId]
    }

    override suspend fun getCurrentUserUid(): String? = null
}

class GetUserUseCaseTest {

    @Test
    fun `invoke maps found dto to friend user instance`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice", email = "alice@example.com")
        val repo = GetUserFakeRepository(usersById = mapOf("u1" to dto))
        val useCase = GetUserUseCase(repo)

        val result = useCase.invoke("u1", true)

        assertEquals("u1", result?.uid)
        assertEquals("Alice", result?.name)
        assertEquals("alice@example.com", result?.email)
        assertEquals(true, repo.lastRequestedIsCurrentUser)
    }

    @Test
    fun `invoke returns null when repository has no matching user`() = runTest {
        val repo = GetUserFakeRepository(usersById = emptyMap())
        val useCase = GetUserUseCase(repo)

        val result = useCase.invoke("missing", false)

        assertNull(result)
    }
}
