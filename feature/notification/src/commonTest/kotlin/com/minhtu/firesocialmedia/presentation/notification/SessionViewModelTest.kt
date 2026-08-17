package com.minhtu.firesocialmedia.presentation.notification

import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetUserUseCase
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeSessionUserRepository : UserRepository {
    val users = hashMapOf<String, UserDTO>()
    var currentUserUid: String? = null

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads current user when uid is available`() = runTest(dispatcher) {
        val userRepo = FakeSessionUserRepository().apply {
            currentUserUid = "u1"
            users["u1"] = UserDTO(uid = "u1", name = "Current User")
        }
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        assertEquals("u1", vm.currentUser?.uid)
        assertEquals("Current User", vm.currentUser?.name)
    }

    @Test
    fun `init leaves currentUser null when no uid`() = runTest(dispatcher) {
        val userRepo = FakeSessionUserRepository().apply { currentUserUid = null }
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        assertNull(vm.currentUser)
    }

    @Test
    fun `findUserById caches result and updates loadedUserState`() = runTest(dispatcher) {
        val userRepo = FakeSessionUserRepository().apply {
            users["u2"] = UserDTO(uid = "u2", name = "User 2")
        }
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        val user = vm.findUserById("u2")
        advanceUntilIdle()

        assertEquals("User 2", user?.name)
        assertEquals("User 2", vm.loadedUserState.value["u2"]?.name)
    }

    @Test
    fun `findUserById returns cached value without calling repository again`() = runTest(dispatcher) {
        val userRepo = FakeSessionUserRepository().apply {
            users["u2"] = UserDTO(uid = "u2", name = "User 2")
        }
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        val first = vm.findUserById("u2")
        // Mutate backing repository to prove the second call hits the cache, not the repository.
        userRepo.users["u2"] = UserDTO(uid = "u2", name = "Changed Name")
        val second = vm.findUserById("u2")

        assertEquals(first, second)
        assertEquals("User 2", second?.name)
    }

    @Test
    fun `ensureUserLoaded fetches user only once`() = runTest(dispatcher) {
        val userRepo = FakeSessionUserRepository().apply {
            users["u3"] = UserDTO(uid = "u3", name = "User 3")
        }
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        vm.ensureUserLoaded("u3")
        advanceUntilIdle()
        vm.ensureUserLoaded("u3")
        advanceUntilIdle()

        assertEquals("User 3", vm.loadedUserState.value["u3"]?.name)
    }
}
