package com.minhtu.firesocialmedia.domain.usecases.common.security

import com.minhtu.firesocialmedia.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetCurrentUserUidUseCaseTest {

    @Test
    fun `invoke returns the uid from the repository`() = runTest {
        val repo = FakeUserRepository(currentUid = "uid-42")
        val useCase = GetCurrentUserUidUseCase(repo)

        assertEquals("uid-42", useCase())
    }

    @Test
    fun `invoke returns null when no user is signed in`() = runTest {
        val repo = FakeUserRepository(currentUid = null)
        val useCase = GetCurrentUserUidUseCase(repo)

        assertNull(useCase())
    }
}
