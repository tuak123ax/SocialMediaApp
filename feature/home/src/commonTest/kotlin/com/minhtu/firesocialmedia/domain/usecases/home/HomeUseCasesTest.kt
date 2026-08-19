package com.minhtu.firesocialmedia.domain.usecases.home

import com.minhtu.firesocialmedia.data.local.service.crypto.HomeCryptoService
import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeUseCasesTest {

    private class FakeHomeCryptoService : HomeCryptoService {
        var cleared = false
        var savedUser: UserDTO? = null
        override suspend fun clearAccount() { cleared = true }
        override suspend fun saveCurrentUserInfo(user: UserDTO) { savedUser = user }
    }

    private class FakeHomeCommentDbRepository : HomeCommentDbRepository {
        var cleared = false
        var synced = false
        override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
        override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
        override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
        override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
        override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {}
        override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {}
        override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {}
        override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {}
        override suspend fun saveLikedComments(id: String, value: HashMap<String, Int>): Boolean = true
        override suspend fun syncComments(): Boolean { synced = true; return true }
        override suspend fun clearComments() { cleared = true }
    }

    private class FakeHomeDbRepository(
        val saveLikedPostResult: Boolean = true
    ) : HomeDbRepository {
        var clearedLikedPosts = false
        var clearedLocalFriends = false
        var updatedLikeCount: Pair<String, Int>? = null
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = saveLikedPostResult
        override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean = true
        override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {
            updatedLikeCount = id to value
        }
        override suspend fun syncLikedPosts(currentUserId: String): Boolean = true
        override suspend fun clearLikedPosts() { clearedLikedPosts = true }
        override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
        override suspend fun deleteAllDraftPosts(): Boolean = true
        override suspend fun deleteDraftPost(newId: String): Boolean = true
        override suspend fun clearLocalFriends() { clearedLocalFriends = true }
        override suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean = true
        override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = HashMap()
        override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean = false
    }

    private class FakeNewsRepository(
        val newById: Map<String, NewsInstance?> = emptyMap()
    ) : NewsRepository {
        var deletedNews: NewsInstance? = null
        override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
        override suspend fun fetchPoll(pollId: String): PollDTO? = null
        override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
        override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
        override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
        override suspend fun getNew(newId: String): NewsInstance? = newById[newId]
        override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
        override suspend fun deleteNewsFromDatabase(new: NewsInstance) { deletedNews = new }
        override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean = true
        override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = null
    }

    private class FakeUserRepository(
        val usersById: Map<String, UserDTO?> = emptyMap(),
        val currentUid: String? = null,
        val searchResult: List<UserDTO>? = null
    ) : UserRepository {
        var updatedFcmUser: UserDTO? = null
        override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
        override suspend fun getCurrentUserUid(): String? = currentUid
        override suspend fun searchUserByName(name: String): List<UserDTO>? = searchResult
        override suspend fun updateFCMTokenForCurrentUser(user: UserDTO) { updatedFcmUser = user }
    }

    private class FakeHomeNewsRoomService : HomeNewsRoomService {
        var stored: List<NewsInstance>? = null
        override suspend fun storeNewsToRoom(news: List<NewsInstance>) { stored = news }
        override suspend fun getFirstPage(number: Int): List<NewsInstance> = emptyList()
        override suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?): List<NewsInstance> = emptyList()
        override suspend fun getNewById(newId: String): NewsInstance? = null
        override suspend fun saveNews(new: NewsInstance) {}
        override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
        override suspend fun deleteDraftPost(id: String) {}
        override suspend fun deleteAllDraftPosts() {}
        override suspend fun saveLikedPost(value: HashMap<String, Int>) {}
        override suspend fun getAllLikedPosts(): HashMap<String, Int> = HashMap()
        override suspend fun clearLikedPosts() {}
        override suspend fun hasLikedPost(): Boolean = false
    }

    @Test
    fun `ClearAccountUseCase delegates to crypto service`() = runTest {
        val service = FakeHomeCryptoService()
        ClearAccountUseCase(service).invoke()
        assertTrue(service.cleared)
    }

    @Test
    fun `ClearLocalDataUseCase clears both liked posts and comments`() = runTest {
        val homeRepo = FakeHomeDbRepository()
        val commentRepo = FakeHomeCommentDbRepository()
        ClearLocalDataUseCase(homeRepo, commentRepo).invoke()
        assertTrue(homeRepo.clearedLikedPosts)
        assertTrue(commentRepo.cleared)
    }

    @Test
    fun `ClearLocalFriendsUseCase delegates to repository`() = runTest {
        val homeRepo = FakeHomeDbRepository()
        ClearLocalFriendsUseCase(homeRepo).invoke()
        assertTrue(homeRepo.clearedLocalFriends)
    }

    @Test
    fun `DeleteNewsFromDatabaseUseCase delegates to repository`() = runTest {
        val newsRepo = FakeNewsRepository()
        val news = NewsInstance(id = "n1")
        DeleteNewsFromDatabaseUseCase(newsRepo).invoke(news)
        assertEquals("n1", newsRepo.deletedNews?.id)
    }

    @Test
    fun `GetCurrentUserUidUseCase delegates to repository`() = runTest {
        val userRepo = FakeUserRepository(currentUid = "uid1")
        val result = GetCurrentUserUidUseCase(userRepo).invoke()
        assertEquals("uid1", result)
    }

    @Test
    fun `GetLatestNewsUseCase filters out blank news`() = runTest {
        val validNews = NewsInstance(id = "n1", message = "hello")
        val blankNews = NewsInstance(id = "n2", message = "", image = "", video = "")
        val withVideo = NewsInstance(id = "n3", message = "", image = "", video = "v.mp4")
        val repo = object : NewsRepository {
            override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
            override suspend fun fetchPoll(pollId: String): PollDTO? = null
            override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
            override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
            override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
            override suspend fun getNew(newId: String): NewsInstance? = null
            override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
            override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
            override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean = true
            override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? {
                return LatestNewsResult(listOf(validNews, blankNews, withVideo), 100.0, "key1")
            }
        }
        val result = GetLatestNewsUseCase(repo).invoke(10, null, null)
        assertEquals(listOf("n1", "n3"), result?.news?.map { it.id })
        assertEquals(100.0, result?.lastTimePostedValue)
    }

    @Test
    fun `GetLatestNewsUseCase returns null when repository returns null`() = runTest {
        val repo = FakeNewsRepository()
        val result = GetLatestNewsUseCase(repo).invoke(10, null, null)
        assertNull(result)
    }

    @Test
    fun `GetUserUseCase maps DTO to domain instance`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice")
        val repo = FakeUserRepository(usersById = mapOf("u1" to dto))
        val result = GetUserUseCase(repo).invoke("u1", false)
        assertEquals("Alice", result?.name)
        assertEquals("u1", result?.uid)
    }

    @Test
    fun `GetUserUseCase returns null when not found`() = runTest {
        val repo = FakeUserRepository()
        val result = GetUserUseCase(repo).invoke("missing", false)
        assertNull(result)
    }

    @Test
    fun `SaveCurrentUserInfoUseCase saves mapped dto via crypto service`() = runTest {
        val service = FakeHomeCryptoService()
        val user = UserInstance(uid = "u1", name = "Me")
        SaveCurrentUserInfoUseCase(service).invoke(user)
        assertEquals("u1", service.savedUser?.uid)
    }

    @Test
    fun `SaveLikedPostUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository(saveLikedPostResult = true)
        val result = SaveLikedPostUseCase(repo).invoke("u1", hashMapOf("n1" to 1))
        assertTrue(result)
    }

    @Test
    fun `SearchUserByNameUseCase maps results to domain instances`() = runTest {
        val repo = FakeUserRepository(searchResult = listOf(UserDTO(uid = "u1", name = "Alice")))
        val result = SearchUserByNameUseCase(repo).invoke("Alice")
        assertEquals(1, result?.size)
        assertEquals("Alice", result?.first()?.name)
    }

    @Test
    fun `SearchUserByNameUseCase returns null when repository returns null`() = runTest {
        val repo = FakeUserRepository(searchResult = null)
        val result = SearchUserByNameUseCase(repo).invoke("Alice")
        assertNull(result)
    }

    @Test
    fun `StoreNewsToRoomUseCase delegates to room service`() = runTest {
        val service = FakeHomeNewsRoomService()
        val news = listOf(NewsInstance(id = "n1"))
        StoreNewsToRoomUseCase(service).invoke(news)
        assertEquals(news, service.stored)
    }

    @Test
    fun `StoreUserFriendsToRoomUseCase maps and stores friends`() = runTest {
        var storedDtos: List<UserDTO?>? = null
        val localStore = FriendsLocalStore { friends -> storedDtos = friends }
        val friends = listOf<UserInstance?>(UserInstance(uid = "f1", name = "Friend1"), null)
        StoreUserFriendsToRoomUseCase(localStore).invoke(friends)
        assertEquals(2, storedDtos?.size)
        assertEquals("f1", storedDtos?.get(0)?.uid)
        assertNull(storedDtos?.get(1))
    }

    @Test
    fun `UpdateFCMTokenUseCase delegates to repository`() = runTest {
        val repo = FakeUserRepository()
        val user = UserInstance(uid = "u1", token = "tok1")
        UpdateFCMTokenUseCase(repo).invoke(user)
        assertEquals("tok1", repo.updatedFcmUser?.token)
    }

    @Test
    fun `UpdateLikeCountForNewUseCase delegates to repository`() = runTest {
        val repo = FakeHomeDbRepository()
        UpdateLikeCountForNewUseCase(repo).invoke("n1", 4)
        assertEquals("n1" to 4, repo.updatedLikeCount)
    }
}
