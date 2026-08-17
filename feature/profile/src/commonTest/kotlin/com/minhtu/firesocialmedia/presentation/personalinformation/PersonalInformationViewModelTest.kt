package com.minhtu.firesocialmedia.presentation.personalinformation

import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserAvatarUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserStringFieldUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.profile.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeUserRepository(
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class FakeProfileAuthService(private val reAuthResult: Boolean = true) : ProfileAuthService {
    override suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean = reAuthResult
}

private class FakeProfileDatabaseService(
    private val updateStringResult: Boolean = true,
    private val updateAvatarResult: Boolean = true
) : ProfileDatabaseService {
    var lastUpdatedField: Triple<String, String, String>? = null
    var lastUpdatedAvatar: Pair<String, String>? = null
    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? = null
    override suspend fun deleteNewsFromDatabase(new: NewsDTO, newsPath: String): Boolean = true
    override suspend fun updateLikeCountForNew(newsId: String, value: Int, newsPath: String, likedCountPath: String) {}
    override suspend fun deletePollFromDatabase(
        newsId: String, pollId: String, groupPath: String, groupId: String,
        postsPath: String, pollPath: String, pollVotesPath: String
    ): Boolean = true
    override suspend fun getUser(userId: String): UserDTO? = null
    override suspend fun saveValueToDatabase(id: String, path: String, value: HashMap<String, Int>, externalPath: String): Boolean = true
    override suspend fun saveListToDatabase(id: String, path: String, value: ArrayList<String>, externalPath: String) {}
    override suspend fun updateUserStringField(userId: String, fieldPath: String, value: String, userPath: String): Boolean {
        lastUpdatedField = Triple(userId, fieldPath, value)
        return updateStringResult
    }
    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean {
        lastUpdatedAvatar = userId to imageUri
        return updateAvatarResult
    }
    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean = true
    override suspend fun anyChildMatchesFieldValue(path: String, fields: List<String>, value: String): Boolean? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalInformationViewModelTest {

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
        databaseService: FakeProfileDatabaseService = FakeProfileDatabaseService(),
        authService: FakeProfileAuthService = FakeProfileAuthService(),
        userRepo: FakeUserRepository = FakeUserRepository()
    ): PersonalInformationViewModel {
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        return PersonalInformationViewModel(
            UpdateUserStringFieldUseCase(databaseService),
            UpdateUserAvatarUseCase(databaseService),
            VerifyCurrentPasswordUseCase(authService),
            GetUserUseCase(userRepo),
            dispatcher
        )
    }

    @Test
    fun `fetchCurrentUser populates fetchedUser on success`() = runTest {
        val userRepo = FakeUserRepository(usersById = mapOf("u1" to UserDTO(uid = "u1", name = "Alice")))
        val vm = buildViewModel(testScheduler, userRepo = userRepo)

        vm.fetchCurrentUser("u1")
        advanceUntilIdle()

        assertEquals("Alice", vm.fetchedUser.value?.name)
    }

    @Test
    fun `fetchCurrentUser keeps fetchedUser null when user not found`() = runTest {
        val vm = buildViewModel(testScheduler, userRepo = FakeUserRepository())

        vm.fetchCurrentUser("missing")
        advanceUntilIdle()

        assertNull(vm.fetchedUser.value)
    }

    @Test
    fun `onAvatarPicked sets avatarUri`() = runTest {
        val vm = buildViewModel(testScheduler)
        vm.onAvatarPicked("content://avatar1")
        assertEquals("content://avatar1", vm.avatarUri)
    }

    @Test
    fun `updateAvatar with no picked uri is a no-op`() = runTest {
        val vm = buildViewModel(testScheduler)
        vm.updateAvatar("u1")
        advanceUntilIdle()
        assertNull(vm.updateStatus.value)
    }

    @Test
    fun `updateAvatar success uploads and updates state`() = runTest {
        val service = FakeProfileDatabaseService(updateAvatarResult = true)
        val vm = buildViewModel(testScheduler, databaseService = service)
        vm.onAvatarPicked("content://avatar1")

        vm.updateAvatar("u1")
        advanceUntilIdle()

        assertEquals(true, vm.updateStatus.value)
        assertEquals("content://avatar1", vm.uploadedAvatarUri)
        assertNull(vm.avatarUri)
        assertEquals("u1" to "content://avatar1", service.lastUpdatedAvatar)
    }

    @Test
    fun `updateAvatar failure keeps avatarUri for retry`() = runTest {
        val service = FakeProfileDatabaseService(updateAvatarResult = false)
        val vm = buildViewModel(testScheduler, databaseService = service)
        vm.onAvatarPicked("content://avatar1")

        vm.updateAvatar("u1")
        advanceUntilIdle()

        assertEquals(false, vm.updateStatus.value)
        assertNull(vm.uploadedAvatarUri)
        assertEquals("content://avatar1", vm.avatarUri)
    }

    @Test
    fun `reAuthAndUpdatePhone succeeds when password correct`() = runTest {
        val service = FakeProfileDatabaseService(updateStringResult = true)
        val authService = FakeProfileAuthService(reAuthResult = true)
        val vm = buildViewModel(testScheduler, databaseService = service, authService = authService)

        vm.reAuthAndUpdatePhone("a@b.com", "pw", "u1", "0123456789")
        advanceUntilIdle()

        assertEquals(true, vm.updateStatus.value)
        assertNull(vm.reAuthStatus.value)
        assertEquals(Triple("u1", DataConstant.PHONE_PATH, "0123456789"), service.lastUpdatedField)
    }

    @Test
    fun `reAuthAndUpdatePhone fails when password wrong`() = runTest {
        val authService = FakeProfileAuthService(reAuthResult = false)
        val vm = buildViewModel(testScheduler, authService = authService)

        vm.reAuthAndUpdatePhone("a@b.com", "wrongpw", "u1", "0123456789")
        advanceUntilIdle()

        assertEquals(false, vm.reAuthStatus.value)
        assertNull(vm.updateStatus.value)
    }

    @Test
    fun `resetReAuthStatus clears reAuthStatus`() = runTest {
        val authService = FakeProfileAuthService(reAuthResult = false)
        val vm = buildViewModel(testScheduler, authService = authService)
        vm.reAuthAndUpdatePhone("a@b.com", "wrongpw", "u1", "0123456789")
        advanceUntilIdle()
        assertEquals(false, vm.reAuthStatus.value)

        vm.resetReAuthStatus()

        assertNull(vm.reAuthStatus.value)
    }

    @Test
    fun `updateName delegates to use case`() = runTest {
        val service = FakeProfileDatabaseService(updateStringResult = true)
        val vm = buildViewModel(testScheduler, databaseService = service)

        vm.updateName("u1", "New Name")
        advanceUntilIdle()

        assertTrue(vm.updateStatus.value == true)
        assertEquals(Triple("u1", DataConstant.NAME_PATH, "New Name"), service.lastUpdatedField)
    }

    @Test
    fun `updateStatus delegates to use case`() = runTest {
        val service = FakeProfileDatabaseService(updateStringResult = true)
        val vm = buildViewModel(testScheduler, databaseService = service)

        vm.updateStatus("u1", "Feeling good")
        advanceUntilIdle()

        assertTrue(vm.updateStatus.value == true)
        assertEquals(Triple("u1", DataConstant.STATUS_PATH, "Feeling good"), service.lastUpdatedField)
    }

    @Test
    fun `resetUpdateStatus clears updateStatus`() = runTest {
        val vm = buildViewModel(testScheduler)
        vm.updateName("u1", "Name")
        advanceUntilIdle()

        vm.resetUpdateStatus()

        assertNull(vm.updateStatus.value)
    }
}
