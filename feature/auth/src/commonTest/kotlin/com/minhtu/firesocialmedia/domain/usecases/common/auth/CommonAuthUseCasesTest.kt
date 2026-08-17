package com.minhtu.firesocialmedia.domain.usecases.common.auth

import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.data.local.service.crypto.AuthCryptoService
import com.minhtu.firesocialmedia.data.remote.dto.crypto.CredentialsDTO
import com.minhtu.firesocialmedia.domain.repository.auth.UserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeUserRepository(
    private val currentUserUid: String? = null,
    private val user: UserDTO? = null
) : UserRepository {
    var requestedUserId: String? = null
    var requestedIsCurrentUser: Boolean? = null
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        requestedUserId = userId
        requestedIsCurrentUser = isCurrentUser
        return user
    }
    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

private class FakeAuthCryptoService(private val token: String) : AuthCryptoService {
    override fun saveAccount(email: String, password: String) {}
    override suspend fun loadAccount(): CredentialsDTO? = null
    override suspend fun clearAccount() {}
    override suspend fun getFCMToken(): String = token
}

class GetCurrentUserUidUseCaseTest {
    @Test
    fun `invoke returns uid from repository`() = runTest {
        val useCase = GetCurrentUserUidUseCase(FakeUserRepository(currentUserUid = "u1"))
        assertEquals("u1", useCase.invoke())
    }

    @Test
    fun `invoke returns null when no current user`() = runTest {
        val useCase = GetCurrentUserUidUseCase(FakeUserRepository(currentUserUid = null))
        assertNull(useCase.invoke())
    }
}

class GetFCMTokenUseCaseTest {
    @Test
    fun `invoke returns token from crypto service`() = runTest {
        val useCase = GetFCMTokenUseCase(FakeAuthCryptoService("token-123"))
        assertEquals("token-123", useCase.invoke())
    }
}

class GetUserUseCaseTest {
    @Test
    fun `invoke maps DTO to domain user when found`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice", email = "a@b.com", twoFAEnabled = true)
        val repo = FakeUserRepository(user = dto)
        val useCase = GetUserUseCase(repo)

        val result = useCase.invoke("u1", true)

        assertEquals("u1", result?.uid)
        assertEquals("Alice", result?.name)
        assertEquals(true, result?.twoFAEnabled)
        assertEquals("u1", repo.requestedUserId)
        assertEquals(true, repo.requestedIsCurrentUser)
    }

    @Test
    fun `invoke returns null when user not found`() = runTest {
        val repo = FakeUserRepository(user = null)
        val useCase = GetUserUseCase(repo)

        assertNull(useCase.invoke("missing", false))
    }
}
