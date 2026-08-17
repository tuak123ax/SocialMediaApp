package com.minhtu.firesocialmedia.presentation.userinformation

import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.domain.repository.NetworkRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.ProfileFriendDbRepository
import com.minhtu.firesocialmedia.domain.repository.call.profile.CallRepository
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance as SharedNotificationInstance
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import com.minhtu.firesocialmedia.storage.profile.SupabaseStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

private class FakeUiFriendDbRepository : ProfileFriendDbRepository {
    val savedFriends = mutableListOf<Pair<String, List<String>>>()
    val savedFriendRequests = mutableListOf<Pair<String, List<String>>>()

    override suspend fun saveFriend(id: String, value: ArrayList<String>) {
        savedFriends += id to value.toList()
    }
    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
        savedFriendRequests += id to value.toList()
    }
}

private class FakeUiNotificationRepository : NotificationRepository {
    var savedNotifications: Pair<String, List<SharedNotificationInstance>>? = null
    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<SharedNotificationInstance>? = null
    override suspend fun saveNotificationToDatabase(id: String, instance: List<SharedNotificationInstance>) {
        savedNotifications = id to instance
    }
    override suspend fun deleteNotificationFromDatabase(id: String, notification: SharedNotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: SharedNotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

private class FakeUiCallRepository(private val available: Boolean? = false) : CallRepository {
    override suspend fun isCalleeInActiveCall(calleeId: String): Boolean? = available
}

private class FakeUiNetworkRepository(private val hasConnection: Boolean = true) : NetworkRepository {
    override suspend fun hasInternetConnection(): Flow<Boolean> = flowOf(hasConnection)
}

private class FakeUiUserRepository(
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class FakeUiProfileDatabaseService(
    private val updateBackgroundResult: Boolean = true
) : ProfileDatabaseService {
    var lastUpdatedBackground: Pair<String, String>? = null
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
    override suspend fun updateUserStringField(userId: String, fieldPath: String, value: String, userPath: String): Boolean = true
    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean = true
    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean {
        lastUpdatedBackground = userId to imageUri
        return updateBackgroundResult
    }
    override suspend fun anyChildMatchesFieldValue(path: String, fields: List<String>, value: String): Boolean? = null
}

// clickAddFriendButton intentionally launches its work on a standalone
// CoroutineScope(SupervisorJob() + Dispatchers.IO) instead of viewModelScope, so that the
// request survives navigation. That means it runs on a real dispatcher outside the test
// scheduler's virtual time, so advanceUntilIdle() alone cannot guarantee it has completed.
// Poll on a real dispatcher until the expected state shows up (or time out).
private suspend fun waitUntilReal(timeoutMs: Long = 2_000, intervalMs: Long = 20, condition: () -> Boolean) {
    kotlinx.coroutines.withContext(Dispatchers.Default) {
        var waited = 0L
        while (!condition() && waited < timeoutMs) {
            kotlinx.coroutines.delay(intervalMs)
            waited += intervalMs
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UserInformationViewModelTest {

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
        friendRepo: FakeUiFriendDbRepository = FakeUiFriendDbRepository(),
        notificationRepo: FakeUiNotificationRepository = FakeUiNotificationRepository(),
        callRepo: FakeUiCallRepository = FakeUiCallRepository(),
        networkRepo: FakeUiNetworkRepository = FakeUiNetworkRepository(),
        userRepo: FakeUiUserRepository = FakeUiUserRepository(),
        databaseService: FakeUiProfileDatabaseService = FakeUiProfileDatabaseService()
    ): UserInformationViewModel {
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        return UserInformationViewModel(
            ProfileSaveFriendUseCase(friendRepo),
            ProfileSaveFriendRequestUseCase(friendRepo),
            SaveNotificationToDatabaseUseCase(notificationRepo),
            CheckCalleeAvailableUseCase(callRepo),
            GetUserUseCase(userRepo),
            CheckInternetConnectionUseCase(networkRepo),
            UpdateUserBackgroundUseCase(databaseService),
            dispatcher
        )
    }

    @Test
    fun `checkInternetConnection returns repository value`() = runTest {
        val vm = buildViewModel(testScheduler, networkRepo = FakeUiNetworkRepository(hasConnection = false))
        val result = vm.checkInternetConnection()
        assertFalse(result)
    }

    @Test
    fun `checkRelationship returns FRIEND when already friends`() = runTest {
        val vm = buildViewModel(testScheduler)
        val currentUser = UserInstance(uid = "current", friends = arrayListOf("friend1"))
        val friend = UserInstance(uid = "friend1")

        assertEquals(Relationship.FRIEND, vm.checkRelationship(friend, currentUser))
    }

    @Test
    fun `checkRelationship returns WAITING_RESPONSE when current user already requested`() = runTest {
        val vm = buildViewModel(testScheduler)
        val currentUser = UserInstance(uid = "current", friendRequests = arrayListOf("friend1"))
        val friend = UserInstance(uid = "friend1")

        assertEquals(Relationship.WAITING_RESPONSE, vm.checkRelationship(friend, currentUser))
    }

    @Test
    fun `checkRelationship returns FRIEND_REQUEST when friend already sent a request`() = runTest {
        val vm = buildViewModel(testScheduler)
        val currentUser = UserInstance(uid = "current")
        val friend = UserInstance(uid = "friend1", friendRequests = arrayListOf("current"))

        assertEquals(Relationship.FRIEND_REQUEST, vm.checkRelationship(friend, currentUser))
    }

    @Test
    fun `checkRelationship returns NONE when no relation exists`() = runTest {
        val vm = buildViewModel(testScheduler)
        val currentUser = UserInstance(uid = "current")
        val friend = UserInstance(uid = "friend1")

        assertEquals(Relationship.NONE, vm.checkRelationship(friend, currentUser))
    }

    @Test
    fun `updateRelationship sets currentRelationship and addFriendStatus`() = runTest {
        val vm = buildViewModel(testScheduler)
        vm.updateRelationship(Relationship.FRIEND)

        assertEquals(Relationship.FRIEND, vm.currentRelationship)
        assertEquals(Relationship.FRIEND, vm.addFriendStatus.value)
    }

    @Test
    fun `clickAddFriendButton with NONE relationship sends a friend request`() = runTest {
        val friendRepo = FakeUiFriendDbRepository()
        val vm = buildViewModel(testScheduler, friendRepo = friendRepo)
        vm.updateRelationship(Relationship.NONE)
        val friend = UserInstance(uid = "friend1", token = "tok-friend")
        val currentUser = UserInstance(uid = "current", token = "tok-current")

        vm.clickAddFriendButton(friend, currentUser)
        advanceUntilIdle()
        waitUntilReal { vm.addFriendStatus.value == Relationship.FRIEND_REQUEST }

        assertEquals(Relationship.FRIEND_REQUEST, vm.addFriendStatus.value)
        assertTrue(friend.friendRequests.contains("current"))
        assertEquals("friend1", friendRepo.savedFriendRequests.single().first)
        assertEquals(listOf("current"), friendRepo.savedFriendRequests.single().second)
    }

    @Test
    fun `clickAddFriendButton with FRIEND relationship removes friendship both ways`() = runTest {
        val friendRepo = FakeUiFriendDbRepository()
        val vm = buildViewModel(testScheduler, friendRepo = friendRepo)
        vm.updateRelationship(Relationship.FRIEND)
        val friend = UserInstance(uid = "friend1", friends = arrayListOf("current"))
        val currentUser = UserInstance(uid = "current", friends = arrayListOf("friend1"))

        vm.clickAddFriendButton(friend, currentUser)
        advanceUntilIdle()
        waitUntilReal { vm.addFriendStatus.value == Relationship.NONE }

        assertEquals(Relationship.NONE, vm.addFriendStatus.value)
        assertFalse(friend.friends.contains("current"))
        assertFalse(currentUser.friends.contains("friend1"))
        assertEquals(2, friendRepo.savedFriends.size)
    }

    @Test
    fun `clickAddFriendButton with FRIEND_REQUEST relationship cancels the pending request`() = runTest {
        val friendRepo = FakeUiFriendDbRepository()
        val vm = buildViewModel(testScheduler, friendRepo = friendRepo)
        vm.updateRelationship(Relationship.FRIEND_REQUEST)
        val friend = UserInstance(uid = "friend1", friendRequests = arrayListOf("current"))
        val currentUser = UserInstance(uid = "current")

        vm.clickAddFriendButton(friend, currentUser)
        advanceUntilIdle()
        waitUntilReal { vm.addFriendStatus.value == Relationship.NONE }

        assertEquals(Relationship.NONE, vm.addFriendStatus.value)
        assertFalse(friend.friendRequests.contains("current"))
    }

    @Test
    fun `clickAddFriendButton is a no-op when friend or currentUser is null`() = runTest {
        val friendRepo = FakeUiFriendDbRepository()
        val vm = buildViewModel(testScheduler, friendRepo = friendRepo)

        vm.clickAddFriendButton(null, UserInstance(uid = "current"))
        vm.clickAddFriendButton(UserInstance(uid = "friend1"), null)
        advanceUntilIdle()

        assertNull(vm.addFriendStatus.value)
        assertTrue(friendRepo.savedFriends.isEmpty())
        assertTrue(friendRepo.savedFriendRequests.isEmpty())
    }

    @Test
    fun `checkCalleeAvailable populates calleeCurrentState`() = runTest {
        val vm = buildViewModel(testScheduler, callRepo = FakeUiCallRepository(available = true))
        vm.checkCalleeAvailable(UserInstance(uid = "callee1"))
        advanceUntilIdle()

        assertEquals(true, vm.calleeCurrentState.value)
    }

    @Test
    fun `resetCalleeState clears calleeCurrentState`() = runTest {
        val vm = buildViewModel(testScheduler, callRepo = FakeUiCallRepository(available = true))
        vm.checkCalleeAvailable(UserInstance(uid = "callee1"))
        advanceUntilIdle()

        vm.resetCalleeState()

        assertNull(vm.calleeCurrentState.value)
    }

    @Test
    fun `fetchUserInformation populates fetchedUser on success`() = runTest {
        val userRepo = FakeUiUserRepository(usersById = mapOf("u1" to UserDTO(uid = "u1", name = "Alice")))
        val vm = buildViewModel(testScheduler, userRepo = userRepo)

        vm.fetchUserInformation("u1", isCurrentUser = true)
        advanceUntilIdle()

        assertEquals("Alice", vm.fetchedUser.value?.name)
    }

    @Test
    fun `fetchUserInformation keeps fetchedUser null when user not found`() = runTest {
        val vm = buildViewModel(testScheduler, userRepo = FakeUiUserRepository())

        vm.fetchUserInformation("missing", isCurrentUser = false)
        advanceUntilIdle()

        assertNull(vm.fetchedUser.value)
    }

    @Test
    fun `uploadBackground with default cover photo is a no-op`() = runTest {
        val service = FakeUiProfileDatabaseService()
        val vm = buildViewModel(testScheduler, databaseService = service)

        vm.uploadBackground("u1")
        advanceUntilIdle()

        assertNull(vm.backgroundUploadStatus.value)
        assertNull(service.lastUpdatedBackground)
    }

    @Test
    fun `uploadBackground success updates state and resets coverPhoto`() = runTest {
        val service = FakeUiProfileDatabaseService(updateBackgroundResult = true)
        val vm = buildViewModel(testScheduler, databaseService = service)
        vm.updateCover("content://cover1")

        vm.uploadBackground("u1")
        advanceUntilIdle()

        assertEquals(true, vm.backgroundUploadStatus.value)
        assertEquals("content://cover1", vm.uploadedBackgroundUri)
        assertEquals(SupabaseStorageProvider.DEFAULT_AVATAR_URL, vm.coverPhoto)
        assertEquals("u1" to "content://cover1", service.lastUpdatedBackground)
    }

    @Test
    fun `uploadBackground failure keeps coverPhoto for retry`() = runTest {
        val service = FakeUiProfileDatabaseService(updateBackgroundResult = false)
        val vm = buildViewModel(testScheduler, databaseService = service)
        vm.updateCover("content://cover1")

        vm.uploadBackground("u1")
        advanceUntilIdle()

        assertEquals(false, vm.backgroundUploadStatus.value)
        assertNull(vm.uploadedBackgroundUri)
        assertEquals("content://cover1", vm.coverPhoto)
    }

    @Test
    fun `resetBackgroundUploadStatus clears status`() = runTest {
        val service = FakeUiProfileDatabaseService(updateBackgroundResult = false)
        val vm = buildViewModel(testScheduler, databaseService = service)
        vm.updateCover("content://cover1")
        vm.uploadBackground("u1")
        advanceUntilIdle()

        vm.resetBackgroundUploadStatus()

        assertNull(vm.backgroundUploadStatus.value)
    }

    @Test
    fun `resetOldData clears fetched user, cover photo, upload state and friend status`() = runTest {
        val service = FakeUiProfileDatabaseService(updateBackgroundResult = true)
        val userRepo = FakeUiUserRepository(usersById = mapOf("u1" to UserDTO(uid = "u1", name = "Alice")))
        val vm = buildViewModel(testScheduler, databaseService = service, userRepo = userRepo)
        vm.fetchUserInformation("u1", isCurrentUser = true)
        vm.updateCover("content://cover1")
        vm.uploadBackground("u1")
        vm.updateRelationship(Relationship.FRIEND)
        advanceUntilIdle()

        vm.resetOldData()

        assertNull(vm.fetchedUser.value)
        assertEquals(SupabaseStorageProvider.DEFAULT_AVATAR_URL, vm.coverPhoto)
        assertNull(vm.uploadedBackgroundUri)
        assertNull(vm.addFriendStatus.value)
    }
}
