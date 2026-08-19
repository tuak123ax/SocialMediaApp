package com.minhtu.firesocialmedia.domain.usecases.common.friend

import com.minhtu.firesocialmedia.domain.repository.UserRepository
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class GetCurrentUserUidFakeRepository(
    private val currentUserUid: String? = null
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = null
    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

class GetCurrentUserUidUseCaseTest {

    @Test
    fun `invoke returns uid from repository`() = runTest {
        val repo = GetCurrentUserUidFakeRepository(currentUserUid = "user-123")
        val useCase = GetCurrentUserUidUseCase(repo)

        assertEquals("user-123", useCase.invoke())
    }

    @Test
    fun `invoke returns null when repository has no current user`() = runTest {
        val repo = GetCurrentUserUidFakeRepository(currentUserUid = null)
        val useCase = GetCurrentUserUidUseCase(repo)

        assertNull(useCase.invoke())
    }
}
