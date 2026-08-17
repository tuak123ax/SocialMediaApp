package com.minhtu.firesocialmedia.presentation.friend

import com.minhtu.firesocialmedia.domain.repository.friend.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.friend.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.friend.GetUserUseCase
import com.minhtu.firesocialmedia.friend.data.remote.dto.user.UserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeUserRepository(
    private val currentUserUid: String? = null,
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    val requestedUserIds = mutableListOf<String>()
    var getUserCallCount = 0

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        getUserCallCount++
        requestedUserIds += userId
        return usersById[userId]
    }

    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @Test
    fun `init loads current user when uid is available`() = runTest {
        // Pin the ViewModel's Main dispatcher to this test's scheduler so advanceUntilIdle()
        // deterministically waits for the init{} coroutine instead of racing a real background thread.
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dto = UserDTO(uid = "current", name = "Alice")
            val repo = FakeUserRepository(currentUserUid = "current", usersById = mapOf("current" to dto))
            val vm = SessionViewModel(
                getUserUseCase = GetUserUseCase(repo),
                getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo)
            )
            advanceUntilIdle()

            assertEquals("current", vm.currentUser?.uid)
            assertEquals("Alice", vm.currentUser?.name)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `init leaves currentUser null when no uid available`() = runTest {
        val repo = FakeUserRepository(currentUserUid = null)
        val vm = SessionViewModel(
            getUserUseCase = GetUserUseCase(repo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo)
        )

        assertNull(vm.currentUser)
    }

    @Test
    fun `findUserById fetches and caches user`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Bob")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val vm = SessionViewModel(
            getUserUseCase = GetUserUseCase(repo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo)
        )

        val result = vm.findUserById("u1")

        assertEquals("Bob", result?.name)
        assertEquals(1, repo.getUserCallCount)
        assertTrue(vm.loadedUsersCache.containsKey("u1"))
        assertEquals("Bob", vm.loadedUserState.value["u1"]?.name)
    }

    @Test
    fun `findUserById returns cached value without calling repository again`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Bob")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val vm = SessionViewModel(
            getUserUseCase = GetUserUseCase(repo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo)
        )

        vm.findUserById("u1")
        val secondResult = vm.findUserById("u1")

        assertEquals("Bob", secondResult?.name)
        assertEquals(1, repo.getUserCallCount)
    }

    @Test
    fun `findUserById caches null result for missing user`() = runTest {
        val repo = FakeUserRepository(usersById = emptyMap())
        val vm = SessionViewModel(
            getUserUseCase = GetUserUseCase(repo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo)
        )

        val result = vm.findUserById("missing")

        assertNull(result)
        assertTrue(vm.loadedUsersCache.containsKey("missing"))
        assertNull(vm.loadedUsersCache["missing"])
    }

    @Test
    fun `ensureUserLoaded skips repository call when already cached`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Bob")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val vm = SessionViewModel(
            getUserUseCase = GetUserUseCase(repo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo)
        )

        vm.findUserById("u1")
        assertEquals(1, repo.getUserCallCount)

        vm.ensureUserLoaded("u1")

        assertEquals(1, repo.getUserCallCount)
    }
}
