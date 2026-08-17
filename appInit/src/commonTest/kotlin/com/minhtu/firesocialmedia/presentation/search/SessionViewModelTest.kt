package com.minhtu.firesocialmedia.presentation.search

import com.minhtu.firesocialmedia.appinit.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.repository.appinit.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.appinit.SearchUserByNameUseCase
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
    private val usersById: Map<String, UserDTO?> = emptyMap(),
    private val searchResults: List<UserDTO>? = null
) : UserRepository {
    var getUserCallCount = 0

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
        getUserCallCount++
        return usersById[userId]
    }

    override suspend fun getCurrentUserUid(): String? = currentUserUid

    override suspend fun searchUserByName(name: String): List<UserDTO>? = searchResults
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private fun buildViewModel(repo: FakeUserRepository): SessionViewModel {
        return SessionViewModel(
            getUserUseCase = GetUserUseCase(repo),
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(repo),
            searchUserByNameUseCase = SearchUserByNameUseCase(repo)
        )
    }

    @Test
    fun `init loads current user when uid is available`() = runTest {
        // Pin the ViewModel's Main dispatcher to this test's scheduler so advanceUntilIdle()
        // deterministically waits for the init{} coroutine instead of racing a real background thread.
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dto = UserDTO(uid = "current", name = "Alice")
            val repo = FakeUserRepository(currentUserUid = "current", usersById = mapOf("current" to dto))
            val vm = buildViewModel(repo)
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
        val vm = buildViewModel(repo)

        assertNull(vm.currentUser)
    }

    @Test
    fun `findUserById fetches and caches user`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Bob")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val vm = buildViewModel(repo)

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
        val vm = buildViewModel(repo)

        vm.findUserById("u1")
        val secondResult = vm.findUserById("u1")

        assertEquals("Bob", secondResult?.name)
        assertEquals(1, repo.getUserCallCount)
    }

    @Test
    fun `findUserById caches null result for missing user`() = runTest {
        val repo = FakeUserRepository(usersById = emptyMap())
        val vm = buildViewModel(repo)

        val result = vm.findUserById("missing")

        assertNull(result)
        assertTrue(vm.loadedUsersCache.containsKey("missing"))
        assertNull(vm.loadedUsersCache["missing"])
    }

    @Test
    fun `ensureUserLoaded skips repository call when already cached`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Bob")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val vm = buildViewModel(repo)

        vm.findUserById("u1")
        assertEquals(1, repo.getUserCallCount)

        vm.ensureUserLoaded("u1")

        assertEquals(1, repo.getUserCallCount)
    }

    @Test
    fun `searchUserByName delegates to use case and maps results`() = runTest {
        val results = listOf(UserDTO(uid = "u1", name = "Alice"), UserDTO(uid = "u2", name = "Alicia"))
        val repo = FakeUserRepository(searchResults = results)
        val vm = buildViewModel(repo)

        val users = vm.searchUserByName("Ali")

        assertEquals(2, users.size)
        assertEquals(listOf("u1", "u2"), users.map { it.uid })
    }

    @Test
    fun `searchUserByName returns empty list when nothing matches`() = runTest {
        val repo = FakeUserRepository(searchResults = null)
        val vm = buildViewModel(repo)

        assertTrue(vm.searchUserByName("nobody").isEmpty())
    }
}
