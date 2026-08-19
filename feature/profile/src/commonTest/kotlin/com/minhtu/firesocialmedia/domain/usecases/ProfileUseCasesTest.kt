package com.minhtu.firesocialmedia.domain.usecases

import com.minhtu.firesocialmedia.data.remote.service.auth.ProfileAuthService
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.domain.repository.NetworkRepository
import com.minhtu.firesocialmedia.domain.repository.ProfileFriendDbRepository
import com.minhtu.firesocialmedia.domain.repository.call.profile.CallRepository
import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendRequestUseCase
import com.minhtu.firesocialmedia.domain.usecases.friend.ProfileSaveFriendUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.CheckCalleeAvailableUseCase
import com.minhtu.firesocialmedia.domain.usecases.network.CheckInternetConnectionUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.profile.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserAvatarUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserBackgroundUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.UpdateUserStringFieldUseCase
import com.minhtu.firesocialmedia.domain.usecases.settings.profile.VerifyCurrentPasswordUseCase
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.NewsDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.profile.data.remote.dto.news.ProfileLatestNewsDTO
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.news.ProfileNewsPage
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ---------------------- Fakes ----------------------

private class FakeUserRepository(
    private val user: UserDTO? = null,
    private val currentUserUid: String? = null,
    private val saveLikedPostResult: Boolean = true
) : UserRepository {
    var savedLikedPostArgs: Pair<String, HashMap<String, Int>>? = null
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = user
    override suspend fun getCurrentUserUid(): String? = currentUserUid
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean {
        savedLikedPostArgs = userId to likedPosts
        return saveLikedPostResult
    }
}

private class FakeProfileFriendDbRepository : ProfileFriendDbRepository {
    var savedFriend: Pair<String, ArrayList<String>>? = null
    var savedFriendRequest: Pair<String, ArrayList<String>>? = null
    override suspend fun saveFriend(id: String, value: ArrayList<String>) {
        savedFriend = id to value
    }
    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
        savedFriendRequest = id to value
    }
}

private class FakeNetworkRepository(private val hasConnection: Boolean = true) : NetworkRepository {
    override suspend fun hasInternetConnection(): Flow<Boolean> = flowOf(hasConnection)
}

private class FakeCallRepository(private val available: Boolean? = true) : CallRepository {
    var lastCalleeId: String? = null
    override suspend fun isCalleeInActiveCall(calleeId: String): Boolean? {
        lastCalleeId = calleeId
        return available
    }
}

private class FakeProfileNewsRepository(
    private val newInstance: NewsInstance? = null,
    private val deleteResult: Boolean = true,
    private val deletePollResult: Boolean = true
) : ProfileNewsRepository {
    var lastUpdateLikeCount: Pair<String, Int>? = null
    var lastDeletedNews: NewsInstance? = null
    var lastDeletePollArgs: Triple<String, String, String>? = null
    override suspend fun getNew(newId: String): NewsInstance? = newInstance
    override suspend fun getNewsByUser(posterId: String, number: Int, lastTimePosted: Double?, lastKey: String?): ProfileNewsPage =
        ProfileNewsPage(emptyList(), null, null)
    override suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean {
        lastDeletedNews = new
        return deleteResult
    }
    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {
        lastUpdateLikeCount = newsId to value
    }
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean {
        lastDeletePollArgs = Triple(newsId, pollId, groupId)
        return deletePollResult
    }
}

private class FakeProfileAuthService(private val reAuthResult: Boolean = true) : ProfileAuthService {
    var lastArgs: Pair<String, String>? = null
    override suspend fun reAuthenticate(currentUserEmail: String, currentPassword: String): Boolean {
        lastArgs = currentUserEmail to currentPassword
        return reAuthResult
    }
}

private class FakeProfileDatabaseService(
    private val updateStringResult: Boolean = true,
    private val updateAvatarResult: Boolean = true,
    private val updateBackgroundResult: Boolean = true
) : ProfileDatabaseService {
    var lastUpdatedStringField: Triple<String, String, String>? = null
    var lastUpdatedAvatar: Pair<String, String>? = null
    var lastUpdatedBackground: Pair<String, String>? = null
    override suspend fun getNew(newId: String, newsPath: String): NewsDTO? = null
    override suspend fun getNewsByPoster(
        posterId: String,
        number: Int,
        lastTimePosted: Double?,
        lastKey: String?,
        newsPath: String
    ): ProfileLatestNewsDTO = ProfileLatestNewsDTO(emptyList(), null, null)
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
        lastUpdatedStringField = Triple(userId, fieldPath, value)
        return updateStringResult
    }
    override suspend fun updateUserAvatar(userId: String, imageUri: String, userPath: String): Boolean {
        lastUpdatedAvatar = userId to imageUri
        return updateAvatarResult
    }
    override suspend fun updateUserBackground(userId: String, imageUri: String, userPath: String): Boolean {
        lastUpdatedBackground = userId to imageUri
        return updateBackgroundResult
    }
    override suspend fun anyChildMatchesFieldValue(path: String, fields: List<String>, value: String): Boolean? = null
}

private class FakeNotificationRepository : com.minhtu.firesocialmedia.domain.repository.NotificationRepository {
    var savedNotifications: Pair<String, List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>>? = null
    override suspend fun getAllNotificationsOfUser(currentUserUid: String) = emptyList<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>()
    override suspend fun saveNotificationToDatabase(id: String, instance: List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>) {
        savedNotifications = id to instance
    }
    override suspend fun deleteNotificationFromDatabase(id: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

class ProfileUseCasesTest {

    // ---------- common/profile ----------

    @Test
    fun `GetCurrentUserUidUseCase delegates to repository`() = runTest {
        val repo = FakeUserRepository(currentUserUid = "u1")
        val result = GetCurrentUserUidUseCase(repo).invoke()
        assertEquals("u1", result)
    }

    @Test
    fun `GetUserUseCase maps DTO to profile UserInstance`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice", email = "a@b.com")
        val repo = FakeUserRepository(user = dto)
        val result = GetUserUseCase(repo).invoke("u1", true)
        assertEquals("u1", result?.uid)
        assertEquals("Alice", result?.name)
    }

    @Test
    fun `GetUserUseCase returns null when repository returns null`() = runTest {
        val repo = FakeUserRepository(user = null)
        val result = GetUserUseCase(repo).invoke("missing", false)
        assertNull(result)
    }

    @Test
    fun `SaveLikedPostUseCase delegates to repository`() = runTest {
        val repo = FakeUserRepository(saveLikedPostResult = true)
        val result = SaveLikedPostUseCase(repo).invoke("u1", hashMapOf("n1" to 1))
        assertTrue(result)
        assertEquals("u1", repo.savedLikedPostArgs?.first)
        assertEquals(1, repo.savedLikedPostArgs?.second?.get("n1"))
    }

    @Test
    fun `SaveLikeNotificationUseCase does nothing when poster not found`() = runTest {
        val userRepo = FakeUserRepository(user = null)
        val notificationRepo = FakeNotificationRepository()
        val useCase = SaveLikeNotificationUseCase(
            GetUserUseCase(userRepo),
            SaveNotificationToDatabaseUseCase(notificationRepo)
        )
        val liker = UserInstance(uid = "liker", name = "Liker")
        useCase.invoke(liker, "poster1", "news1")
        assertNull(notificationRepo.savedNotifications)
    }

    // NOTE: The "poster found" branch of SaveLikeNotificationUseCase always falls through to
    // createMessageForServer/sendMessageToServer, which builds an android.org.json.JSONObject.
    // That class is not mocked/shadowed under plain JUnit unit tests (no Robolectric in this
    // module's test setup), so it throws "Method toString in org.json.JSONObject not mocked."
    // This mirrors the same limitation already documented in EngagementViewModelTest, which
    // keeps the poster unresolved for the same reason. Only the "poster not found" branch
    // (which returns before touching JSONObject) is unit-testable here.

    // ---------- friend ----------

    @Test
    fun `ProfileSaveFriendUseCase delegates to repository`() = runTest {
        val repo = FakeProfileFriendDbRepository()
        ProfileSaveFriendUseCase(repo).invoke("u1", arrayListOf("f1", "f2"))
        assertEquals("u1", repo.savedFriend?.first)
        assertEquals(listOf("f1", "f2"), repo.savedFriend?.second?.toList())
    }

    @Test
    fun `ProfileSaveFriendRequestUseCase delegates to repository`() = runTest {
        val repo = FakeProfileFriendDbRepository()
        ProfileSaveFriendRequestUseCase(repo).invoke("u1", arrayListOf("r1"))
        assertEquals("u1", repo.savedFriendRequest?.first)
        assertEquals(listOf("r1"), repo.savedFriendRequest?.second?.toList())
    }

    // ---------- network ----------

    @Test
    fun `CheckInternetConnectionUseCase returns flow from repository`() = runTest {
        val repo = FakeNetworkRepository(hasConnection = true)
        val result = CheckInternetConnectionUseCase(repo).invoke()
        assertEquals(true, result.first())
    }

    // ---------- information ----------

    @Test
    fun `CheckCalleeAvailableUseCase delegates to repository`() = runTest {
        val repo = FakeCallRepository(available = false)
        val result = CheckCalleeAvailableUseCase(repo).invoke("callee1")
        assertEquals(false, result)
        assertEquals("callee1", repo.lastCalleeId)
    }

    // ---------- news/profile ----------

    @Test
    fun `GetNewByIdUseCase delegates to repository`() = runTest {
        val news = NewsInstance(id = "n1")
        val repo = FakeProfileNewsRepository(newInstance = news)
        val result = GetNewByIdUseCase(repo).invoke("n1")
        assertEquals("n1", result?.id)
    }

    @Test
    fun `DeleteNewsUseCase delegates to repository`() = runTest {
        val repo = FakeProfileNewsRepository(deleteResult = true)
        val news = NewsInstance(id = "n1")
        val result = DeleteNewsUseCase(repo).invoke(news)
        assertTrue(result)
        assertEquals(news, repo.lastDeletedNews)
    }

    @Test
    fun `UpdateLikeCountForNewUseCase delegates to repository`() = runTest {
        val repo = FakeProfileNewsRepository()
        UpdateLikeCountForNewUseCase(repo).invoke("n1", 5)
        assertEquals("n1" to 5, repo.lastUpdateLikeCount)
    }

    // ---------- newsfeed/profile ----------

    @Test
    fun `DeletePollUseCase delegates to repository`() = runTest {
        val repo = FakeProfileNewsRepository(deletePollResult = true)
        val result = DeletePollUseCase(repo).invoke("n1", "p1", "g1")
        assertTrue(result)
        assertEquals(Triple("n1", "p1", "g1"), repo.lastDeletePollArgs)
    }

    // ---------- settings ----------

    @Test
    fun `UpdateUserAvatarUseCase delegates to database service`() = runTest {
        val service = FakeProfileDatabaseService(updateAvatarResult = true)
        val result = UpdateUserAvatarUseCase(service).invoke("u1", "uri1")
        assertTrue(result)
        assertEquals("u1" to "uri1", service.lastUpdatedAvatar)
    }

    @Test
    fun `UpdateUserBackgroundUseCase delegates to database service`() = runTest {
        val service = FakeProfileDatabaseService(updateBackgroundResult = false)
        val result = UpdateUserBackgroundUseCase(service).invoke("u1", "uri2")
        assertFalse(result)
        assertEquals("u1" to "uri2", service.lastUpdatedBackground)
    }

    @Test
    fun `UpdateUserStringFieldUseCase delegates to database service`() = runTest {
        val service = FakeProfileDatabaseService(updateStringResult = true)
        val result = UpdateUserStringFieldUseCase(service).invoke("u1", "name", "New Name")
        assertTrue(result)
        assertEquals(Triple("u1", "name", "New Name"), service.lastUpdatedStringField)
    }

    @Test
    fun `VerifyCurrentPasswordUseCase delegates to auth service`() = runTest {
        val service = FakeProfileAuthService(reAuthResult = true)
        val result = VerifyCurrentPasswordUseCase(service).invoke("a@b.com", "pw")
        assertTrue(result)
        assertEquals("a@b.com" to "pw", service.lastArgs)
    }

    @Test
    fun `VerifyCurrentPasswordUseCase returns false on wrong password`() = runTest {
        val service = FakeProfileAuthService(reAuthResult = false)
        val result = VerifyCurrentPasswordUseCase(service).invoke("a@b.com", "wrong")
        assertFalse(result)
    }
}
