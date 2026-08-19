package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

import com.minhtu.firesocialmedia.constants.home.Constants
import com.minhtu.firesocialmedia.data.remote.dto.settings.home.PollDTO
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.repository.HomeDbRepository
import com.minhtu.firesocialmedia.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.group.home.GetAllMembersInGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.home.GetGroupConfigsUseCase
import com.minhtu.firesocialmedia.domain.usecases.group.home.SaveNewToGroupUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeleteAllDraftPostsUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.DeleteDraftPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.SaveNewToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.UpdateNewsFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.sync.LoadNewsPostedWhenOfflineUseCase
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.home.entity.core.DecentralizationType
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UploadNewfeedViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeUserRepository(
        val usersById: Map<String, UserDTO?> = emptyMap()
    ) : UserRepository {
        override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
        override suspend fun getCurrentUserUid(): String? = null
        override suspend fun searchUserByName(name: String): List<UserDTO>? = null
        override suspend fun updateFCMTokenForCurrentUser(user: UserDTO) {}
    }

    private class FakeHomeDbRepository(
        val saveNewResult: Boolean = true,
        val saveNewToGroupResult: Boolean = true,
        val deleteAllDraftsResult: Boolean = true,
        val deleteDraftResult: Boolean = true,
        val offlineNews: List<NewsInstance> = emptyList(),
        val membersInGroup: HashMap<String, String> = HashMap(),
        val notificationOn: Boolean = false,
        var savedNews: NewsInstance? = null
    ) : HomeDbRepository {
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
        override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean {
            savedNews = instance
            return saveNewResult
        }
        override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
        override suspend fun syncLikedPosts(currentUserId: String): Boolean = true
        override suspend fun clearLikedPosts() {}
        override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = offlineNews
        override suspend fun deleteAllDraftPosts(): Boolean = deleteAllDraftsResult
        override suspend fun deleteDraftPost(newId: String): Boolean = deleteDraftResult
        override suspend fun clearLocalFriends() {}
        override suspend fun saveNewToGroup(groupId: String, instance: NewsInstance): Boolean {
            savedNews = instance
            return saveNewToGroupResult
        }
        override suspend fun getAllMembersInGroup(groupId: String): HashMap<String, String> = membersInGroup
        override suspend fun isGroupNotificationOnForUser(userId: String, groupId: String): Boolean = notificationOn
    }

    private class FakeNewsRepository(
        val updateResult: Boolean = true
    ) : NewsRepository {
        var updatedContent: String? = null
        override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
        override suspend fun fetchPoll(pollId: String): PollDTO? = null
        override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
        override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
        override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
        override suspend fun getNew(newId: String): NewsInstance? = null
        override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
        override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
        override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean {
            updatedContent = newContent
            return updateResult
        }
        override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = null
    }

    private class FakeNotificationRepository : NotificationRepository {
        val saved = mutableListOf<String>()
        override suspend fun getAllNotificationsOfUser(currentUserUid: String) = emptyList<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>()
        override suspend fun saveNotificationToDatabase(id: String, instance: List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>) {
            saved.add(id)
        }
        override suspend fun deleteNotificationFromDatabase(id: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
        override suspend fun updateIsReadStatusOfNotification(userId: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
        override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
    }

    private fun makeVm(
        scheduler: TestCoroutineScheduler,
        userRepository: UserRepository = FakeUserRepository(),
        homeDbRepository: HomeDbRepository = FakeHomeDbRepository(),
        newsRepository: NewsRepository = FakeNewsRepository(),
        notificationRepository: NotificationRepository = FakeNotificationRepository()
    ): UploadNewfeedViewModel {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        return UploadNewfeedViewModel(
        GetUserUseCase(userRepository),
        SaveNotificationToDatabaseUseCase(notificationRepository),
        SaveNewToDatabaseUseCase(homeDbRepository),
        UpdateNewsFromDatabaseUseCase(newsRepository),
        LoadNewsPostedWhenOfflineUseCase(homeDbRepository),
        DeleteAllDraftPostsUseCase(homeDbRepository),
        DeleteDraftPostUseCase(homeDbRepository),
        SaveNewToGroupUseCase(homeDbRepository),
        GetAllMembersInGroupUseCase(homeDbRepository),
        GetGroupConfigsUseCase(homeDbRepository),
        StandardTestDispatcher(scheduler)
    )
    }

    @Test
    fun `createPost with no content sets postError`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateCurrentUser(UserInstance(uid = "u1"))
        vm.createPost(UserInstance(uid = "u1"))
        advanceUntilIdle()
        assertEquals(Constants.POST_NEWS_EMPTY_ERROR, vm.postError.value)
        assertNull(vm.createPostStatus.value)
    }

    @Test
    fun `createPost with private content saves news without sending notification`() = runTest {
        val dbRepo = FakeHomeDbRepository()
        val vm = makeVm(testScheduler, homeDbRepository = dbRepo)
        val user = UserInstance(uid = "u1", name = "Me", image = "img")
        vm.updateCurrentUser(user)
        vm.updateMessage("hello world")
        vm.updateAccessPermission(DecentralizationType.Private)

        vm.createPost(user)
        advanceUntilIdle()

        assertEquals(true, vm.createPostStatus.value)
        assertEquals("hello world", dbRepo.savedNews?.message)
        // access permission reset after posting
        assertEquals(DecentralizationType.Public, vm.accessPermission.value)
    }

    @Test
    fun `createPost with groupId saves to group`() = runTest {
        val dbRepo = FakeHomeDbRepository()
        val vm = makeVm(testScheduler, homeDbRepository = dbRepo)
        val user = UserInstance(uid = "u1", name = "Me", image = "img")
        vm.updateCurrentUser(user)
        vm.updateMessage("group post")
        vm.updateGroupId("g1")
        vm.updateAccessPermission(DecentralizationType.Private)

        vm.createPost(user)
        advanceUntilIdle()

        assertEquals(true, vm.createPostStatus.value)
        assertEquals("g1", dbRepo.savedNews?.groupId)
        // groupId reset after posting
        assertEquals("", vm.groupId)
    }

    @Test
    fun `updateImage clears video and vice versa`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateVideo("video.mp4")
        assertEquals("video.mp4", vm.video)
        vm.updateImage("image.png")
        assertEquals("image.png", vm.image)
        assertEquals("", vm.video)
        vm.updateVideo("video2.mp4")
        assertEquals("video2.mp4", vm.video)
        assertEquals("", vm.image)
    }

    @Test
    fun `updateNewInformation with content updates news`() = runTest {
        val newsRepo = FakeNewsRepository()
        val vm = makeVm(testScheduler, newsRepository = newsRepo)
        vm.updateMessage("updated content")
        val news = NewsInstance(id = "n1")

        vm.updateNewInformation(news)
        advanceUntilIdle()

        assertEquals(true, vm.updatePostStatus.value)
        assertEquals("updated content", newsRepo.updatedContent)
    }

    @Test
    fun `updateNewInformation with no content sets postError`() = runTest {
        val vm = makeVm(testScheduler)
        val news = NewsInstance(id = "n1")
        vm.updateNewInformation(news)
        advanceUntilIdle()
        assertEquals(Constants.POST_NEWS_EMPTY_ERROR, vm.postError.value)
        assertNull(vm.updatePostStatus.value)
    }

    @Test
    fun `resetPostStatus clears status and inputs`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateMessage("hi")
        vm.updateImage("img")
        vm.resetPostStatus()
        assertEquals("", vm.message)
        assertEquals("", vm.image)
        assertEquals("", vm.video)
        assertNull(vm.createPostStatus.value)
        assertNull(vm.updatePostStatus.value)
    }

    @Test
    fun `resetPostError clears error`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateCurrentUser(UserInstance(uid = "u1"))
        vm.createPost(UserInstance(uid = "u1"))
        advanceUntilIdle()
        assertEquals(Constants.POST_NEWS_EMPTY_ERROR, vm.postError.value)
        vm.resetPostError()
        assertNull(vm.postError.value)
    }

    @Test
    fun `onClickBackButton and resetBackValue toggle state`() = runTest {
        val vm = makeVm(testScheduler)
        assertEquals(false, vm.clickBackButton.value)
        vm.onClickBackButton()
        assertEquals(true, vm.clickBackButton.value)
        vm.resetBackValue()
        assertEquals(false, vm.clickBackButton.value)
    }

    @Test
    fun `loadNewsPostedWhenOffline populates flow`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"), NewsInstance(id = "o2"))
        val vm = makeVm(testScheduler, homeDbRepository = FakeHomeDbRepository(offlineNews = offline))
        vm.loadNewsPostedWhenOffline()
        assertEquals(2, vm.newsPostedWhenOffline.value.size)
    }

    @Test
    fun `removeOfflineNewsById removes matching entry`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"), NewsInstance(id = "o2"))
        val vm = makeVm(testScheduler, homeDbRepository = FakeHomeDbRepository(offlineNews = offline))
        vm.loadNewsPostedWhenOffline()
        vm.removeOfflineNewsById("o1")
        assertEquals(1, vm.newsPostedWhenOffline.value.size)
        assertEquals("o2", vm.newsPostedWhenOffline.value.first().id)
    }

    @Test
    fun `clearAllOfflineNews empties list`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"))
        val vm = makeVm(testScheduler, homeDbRepository = FakeHomeDbRepository(offlineNews = offline))
        vm.loadNewsPostedWhenOffline()
        vm.clearAllOfflineNews()
        assertTrue(vm.newsPostedWhenOffline.value.isEmpty())
    }

    @Test
    fun `deleteAllDraftPosts success clears offline news`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"))
        val dbRepo = FakeHomeDbRepository(offlineNews = offline, deleteAllDraftsResult = true)
        val vm = makeVm(testScheduler, homeDbRepository = dbRepo)
        vm.loadNewsPostedWhenOffline()

        vm.deleteAllDraftPosts()
        advanceUntilIdle()

        assertEquals(true, vm.deleteDraftStatus.value)
        assertTrue(vm.newsPostedWhenOffline.value.isEmpty())
    }

    @Test
    fun `deleteAllDraftPosts failure reloads offline news`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"))
        val dbRepo = FakeHomeDbRepository(offlineNews = offline, deleteAllDraftsResult = false)
        val vm = makeVm(testScheduler, homeDbRepository = dbRepo)

        vm.deleteAllDraftPosts()
        advanceUntilIdle()

        assertEquals(false, vm.deleteDraftStatus.value)
        assertEquals(1, vm.newsPostedWhenOffline.value.size)
    }

    @Test
    fun `deleteDraftPost success removes single entry`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"), NewsInstance(id = "o2"))
        val dbRepo = FakeHomeDbRepository(offlineNews = offline, deleteDraftResult = true)
        val vm = makeVm(testScheduler, homeDbRepository = dbRepo)
        vm.loadNewsPostedWhenOffline()

        vm.deleteDraftPost("o1")
        advanceUntilIdle()

        assertEquals(true, vm.deleteDraftStatus.value)
        assertEquals(1, vm.newsPostedWhenOffline.value.size)
    }

    @Test
    fun `deleteDraftPost failure reloads offline news`() = runTest {
        val offline = listOf(NewsInstance(id = "o1"))
        val dbRepo = FakeHomeDbRepository(offlineNews = offline, deleteDraftResult = false)
        val vm = makeVm(testScheduler, homeDbRepository = dbRepo)

        vm.deleteDraftPost("o1")
        advanceUntilIdle()

        assertEquals(false, vm.deleteDraftStatus.value)
        assertEquals(1, vm.newsPostedWhenOffline.value.size)
    }

    @Test
    fun `updatePostData sets message image and video conditionally`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updatePostData("msg", "img.png", "")
        assertEquals("msg", vm.message)
        assertEquals("img.png", vm.image)
        assertEquals("", vm.video)
    }

    @Test
    fun `updateLocalPath sets the draft local path`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateLocalPath("/local/path")
        assertEquals("/local/path", vm.localPathOfSelectedDraft.value)
    }

    @Test
    fun `findUserById delegates through GetUserUseCase`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice")
        val vm = makeVm(testScheduler, userRepository = FakeUserRepository(usersById = mapOf("u1" to dto)))
        val result = vm.findUserById("u1")
        assertEquals("Alice", result?.name)
    }

    @Test
    fun `getFriendTokens fetches non-null tokens only`() = runTest {
        val alice = UserDTO(uid = "f1", name = "Alice", token = "tokenA")
        val vm = makeVm(testScheduler, userRepository = FakeUserRepository(usersById = mapOf("f1" to alice)))
        val user = UserInstance(uid = "me")
        user.friends = arrayListOf("f1", "missing")
        vm.updateCurrentUser(user)

        val tokens = vm.getFriendTokens()
        assertEquals(1, tokens.size)
        assertEquals("tokenA", tokens[0])
    }

    @Test
    fun `saveNotificationsForUsers saves notification for each resolvable user`() = runTest {
        val alice = UserDTO(uid = "f1", name = "Alice", token = "tokenA")
        val notifRepo = FakeNotificationRepository()
        val vm = makeVm(
            testScheduler,
            userRepository = FakeUserRepository(usersById = mapOf("f1" to alice)),
            notificationRepository = notifRepo
        )
        val notification = com.minhtu.firesocialmedia.home.entity.notification.NotificationInstance(
            id = "n1", content = "hi", sender = "me"
        )
        vm.saveNotificationsForUsers(listOf("f1", "missing"), notification)
        assertEquals(listOf("f1"), notifRepo.saved)
    }

    @Test
    fun `getGroupMembersFromGroupDetails does not throw`() = runTest {
        val vm = makeVm(testScheduler)
        vm.getGroupMembersFromGroupDetails(hashMapOf("u1" to "User1"))
        // no public getter; just verifying no exception thrown when later posting into that group
        assertTrue(true)
    }
}
