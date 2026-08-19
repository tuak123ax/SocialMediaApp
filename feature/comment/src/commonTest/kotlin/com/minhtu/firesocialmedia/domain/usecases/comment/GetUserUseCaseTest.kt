package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.repository.comment.UserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeUserRepository : UserRepository {
    var userToReturn: UserDTO? = null
    var requestedUserId: String? = null
    var requestedIsCurrentUser: Boolean? = null

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        requestedUserId = userId
        requestedIsCurrentUser = isCurrentUser
        return userToReturn
    }
}

class GetUserUseCaseTest {

    @Test
    fun `invoke maps dto to comment user instance`() = runTest {
        val repository = FakeUserRepository().apply {
            userToReturn = UserDTO(uid = "u1", name = "Alice", email = "alice@test.com")
        }
        val useCase = GetUserUseCase(repository)

        val result = useCase.invoke("u1", false)

        assertEquals("u1", result?.uid)
        assertEquals("Alice", result?.name)
        assertEquals("alice@test.com", result?.email)
    }

    @Test
    fun `invoke returns null when repository has no user`() = runTest {
        val repository = FakeUserRepository().apply { userToReturn = null }
        val useCase = GetUserUseCase(repository)

        val result = useCase.invoke("missing", false)

        assertNull(result)
    }

    @Test
    fun `invoke forwards userId and isCurrentUser to repository`() = runTest {
        val repository = FakeUserRepository()
        val useCase = GetUserUseCase(repository)

        useCase.invoke("u2", true)

        assertEquals("u2", repository.requestedUserId)
        assertEquals(true, repository.requestedIsCurrentUser)
    }
}
