package com.minhtu.firesocialmedia.presentation.profile

import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
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

private class FakeSessionUserRepository(
    private val currentUserUid: String? = null,
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
    override suspend fun getCurrentUserUid(): String? = currentUserUid
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        userRepo: FakeSessionUserRepository = FakeSessionUserRepository()
    ): SessionViewModel {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        return SessionViewModel(
            GetUserUseCase(userRepo),
            GetCurrentUserUidUseCase(userRepo)
        )
    }

    @Test
    fun `init loads current user when uid available`() = runTest {
        val userRepo = FakeSessionUserRepository(
            currentUserUid = "u1",
            usersById = mapOf("u1" to UserDTO(uid = "u1", name = "Alice"))
        )
        val vm = buildViewModel(testScheduler, userRepo)
        advanceUntilIdle()

        assertEquals("u1", vm.currentUser?.uid)
        assertEquals("Alice", vm.currentUser?.name)
    }

    @Test
    fun `init leaves currentUser null when no uid available`() = runTest {
        val userRepo = FakeSessionUserRepository(currentUserUid = null)
        val vm = buildViewModel(testScheduler, userRepo)
        advanceUntilIdle()

        assertNull(vm.currentUser)
    }

    @Test
    fun `findUserById fetches and caches user`() = runTest {
        val userRepo = FakeSessionUserRepository(
            usersById = mapOf("u2" to UserDTO(uid = "u2", name = "Bob"))
        )
        val vm = buildViewModel(testScheduler, userRepo)
        advanceUntilIdle()

        val result = vm.findUserById("u2")
        assertEquals("u2", result?.uid)
        assertEquals("Bob", vm.loadedUsersCache["u2"]?.name)
    }

    @Test
    fun `findUserById returns cached value without refetching`() = runTest {
        var callCount = 0
        val userRepo = object : UserRepository {
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
                callCount++
                return UserDTO(uid = userId, name = "Name-$userId")
            }
            override suspend fun getCurrentUserUid(): String? = null
            override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
        }
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        vm.findUserById("u3")
        vm.findUserById("u3")
        advanceUntilIdle()

        assertEquals(1, callCount)
    }

    @Test
    fun `ensureUserLoaded populates loadedUserState`() = runTest {
        val userRepo = FakeSessionUserRepository(
            usersById = mapOf("u4" to UserDTO(uid = "u4", name = "Carol"))
        )
        val vm = buildViewModel(testScheduler, userRepo)
        advanceUntilIdle()

        vm.ensureUserLoaded("u4")
        advanceUntilIdle()

        assertEquals("Carol", vm.loadedUserState.value["u4"]?.name)
    }

    @Test
    fun `ensureUserLoaded does nothing when user already cached`() = runTest {
        var callCount = 0
        val userRepo = object : UserRepository {
            override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? {
                callCount++
                return UserDTO(uid = userId)
            }
            override suspend fun getCurrentUserUid(): String? = null
            override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
        }
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SessionViewModel(GetUserUseCase(userRepo), GetCurrentUserUidUseCase(userRepo))
        advanceUntilIdle()

        vm.ensureUserLoaded("u5")
        advanceUntilIdle()
        assertEquals(1, callCount)

        vm.ensureUserLoaded("u5")
        advanceUntilIdle()
        assertEquals(1, callCount)
    }
}
