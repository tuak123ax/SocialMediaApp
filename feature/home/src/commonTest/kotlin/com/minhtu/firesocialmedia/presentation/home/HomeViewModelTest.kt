package com.minhtu.firesocialmedia.presentation.home

import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.home.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.home.entity.notification.NotificationType
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------- Fakes ----------------------
    private class FakeUserInteractor(
        val currentUserId: String? = null,
        val usersById: Map<String, UserInstance?> = emptyMap(),
        val searchResult: List<UserInstance>? = emptyList(),
        val saveLikedPostResult: Boolean = true,
        var savedCurrentUser: UserInstance? = null,
        var clearedLocalData: Boolean = false,
        var clearedLocalFriends: Boolean = false
    ) : UserInteractor {
        override suspend fun getCurrentUserId(): String? = currentUserId
        override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = usersById[userId]
        override suspend fun updateFcmToken(user: UserInstance) {}
        override suspend fun saveCurrentUserInfo(user: UserInstance) {
            savedCurrentUser = user
        }
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = saveLikedPostResult
        override suspend fun searchUserByName(name: String): List<UserInstance>? = searchResult
        override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
        override suspend fun clearLocalData() { clearedLocalData = true }
        override suspend fun clearLocalFriends() { clearedLocalFriends = true }
    }

    private class FakeNewsInteractor(
        val pageLatestResult: LatestNewsResult? = null,
        val saveNewsResult: Boolean = true,
        val findNewByIdResult: NewsInstance? = null,
        val deletePollResult: Boolean = true,
        var deletedNews: NewsInstance? = null,
        var likedIds: MutableList<Pair<String, Int>> = mutableListOf(),
        var unlikedIds: MutableList<Pair<String, Int>> = mutableListOf()
    ) : NewsInteractor {
        override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = pageLatestResult
        override suspend fun like(id: String, value: Int) { likedIds.add(id to value) }
        override suspend fun unlike(id: String, value: Int) { unlikedIds.add(id to value) }
        override suspend fun delete(new: NewsInstance) { deletedNews = new }
        override suspend fun deletePoll(newsId: String, pollId: String, groupId: String): Boolean = deletePollResult
        override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
        override suspend fun saveNews(news: NewsInstance): Boolean = saveNewsResult
        override suspend fun findNewById(newsId: String): NewsInstance? = findNewByIdResult
    }

    private class FakeNotificationRepository : NotificationRepository {
        val saved = mutableListOf<Pair<String, List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>>>()
        override suspend fun getAllNotificationsOfUser(currentUserUid: String) = emptyList<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>()
        override suspend fun saveNotificationToDatabase(id: String, instance: List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>) {
            saved.add(id to instance)
        }
        override suspend fun deleteNotificationFromDatabase(id: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
        override suspend fun updateIsReadStatusOfNotification(userId: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
        override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
    }

    private fun makeVm(
        scheduler: TestCoroutineScheduler,
        userInteractor: UserInteractor = FakeUserInteractor(),
        newsInteractor: NewsInteractor = FakeNewsInteractor(),
        notificationRepository: NotificationRepository = FakeNotificationRepository()
    ): HomeViewModel {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        return HomeViewModel(
        userInteractor,
        newsInteractor,
        SaveNotificationToDatabaseUseCase(notificationRepository),
        StandardTestDispatcher(scheduler)
    )
    }

    // ---------------------- getCurrentUserAndFriends ----------------------
    @Test
    fun `getCurrentUserAndFriends succeeds and loads friends`() = runTest {
        val currentUser = UserInstance(uid = "me", name = "Me").apply {
            friends = arrayListOf("f1", "f2")
        }
        val f1 = UserInstance(uid = "f1", name = "Friend1")
        val f2 = UserInstance(uid = "f2", name = "Friend2")
        val vm = makeVm(
            testScheduler,
            userInteractor = FakeUserInteractor(
                currentUserId = "me",
                usersById = mapOf("me" to currentUser, "f1" to f1, "f2" to f2)
            )
        )
        vm.getCurrentUserAndFriends()
        advanceUntilIdle()

        assertEquals(true, vm.getCurrentUserStatus.value)
        assertEquals("me", vm.currentUser?.uid)
        assertEquals(2, vm.allUserFriends.value.size)
    }

    @Test
    fun `getCurrentUserAndFriends fails when no current user id`() = runTest {
        val vm = makeVm(testScheduler, userInteractor = FakeUserInteractor(currentUserId = null))
        vm.getCurrentUserAndFriends()
        advanceUntilIdle()
        assertEquals(false, vm.getCurrentUserStatus.value)
    }

    @Test
    fun `getCurrentUserAndFriends fails when user is null`() = runTest {
        val vm = makeVm(testScheduler, userInteractor = FakeUserInteractor(currentUserId = "me", usersById = emptyMap()))
        vm.getCurrentUserAndFriends()
        advanceUntilIdle()
        assertEquals(false, vm.getCurrentUserStatus.value)
    }

    // ---------------------- getLatestNews ----------------------
    @Test
    fun `getLatestNews success adds news and like comment counts`() = runTest {
        val news = listOf(
            NewsInstance(id = "n1", posterId = "p1", likeCount = 3, commentCount = 1),
            NewsInstance(id = "n2", posterId = "p2", likeCount = 0, commentCount = 0)
        )
        val vm = makeVm(
            testScheduler,
            newsInteractor = FakeNewsInteractor(pageLatestResult = LatestNewsResult(news, 123.0, "key1"))
        )
        vm.getLatestNews()
        advanceUntilIdle()

        assertEquals(true, vm.getAllNewsStatus.value)
        assertEquals(2, vm.allNews.value.size)
        assertEquals(3, vm.likeCountList.value["n1"])
        assertEquals(1, vm.commentCountList.value["n1"])
        assertEquals(true, vm.hasMoreData.value)
    }

    @Test
    fun `getLatestNews sets hasMoreData false when no lastTimePosted`() = runTest {
        val news = listOf(NewsInstance(id = "n1", posterId = "p1"))
        val vm = makeVm(
            testScheduler,
            newsInteractor = FakeNewsInteractor(pageLatestResult = LatestNewsResult(news, null, null))
        )
        vm.getLatestNews()
        advanceUntilIdle()
        assertEquals(false, vm.hasMoreData.value)
    }

    @Test
    fun `getLatestNews fails when result is null`() = runTest {
        val vm = makeVm(testScheduler, newsInteractor = FakeNewsInteractor(pageLatestResult = null))
        vm.getLatestNews()
        advanceUntilIdle()
        assertEquals(false, vm.getAllNewsStatus.value)
    }

    @Test
    fun `getLatestNews does not reload when isLoadingMore is true`() = runTest {
        val vm = makeVm(testScheduler)
        vm.isLoadingMore.value = true
        vm.getLatestNews()
        advanceUntilIdle()
        assertEquals(false, vm.getAllNewsStatus.value)
    }

    @Test
    fun `getLatestNews does not reload when hasMoreData is false`() = runTest {
        val vm = makeVm(testScheduler)
        vm.hasMoreData.value = false
        vm.getLatestNews()
        advanceUntilIdle()
        assertEquals(false, vm.getAllNewsStatus.value)
    }

    @Test
    fun `resetGetLatestNewsParams resets news loading state`() = runTest {
        val vm = makeVm(testScheduler)
        vm.isLoadingMore.value = true
        vm.hasMoreData.value = false
        vm.addNews(ArrayList(listOf(NewsInstance(id = "n1"))))

        vm.resetGetLatestNewsParams()

        assertEquals(false, vm.getAllNewsStatus.value)
        assertEquals(false, vm.isLoadingMore.value)
        assertEquals(true, vm.hasMoreData.value)
        assertEquals(0, vm.allNews.value.size)
        assertEquals(0, vm.listNews.size)
    }

    // ---------------------- addNews / updateNews ----------------------
    @Test
    fun `addNews de-duplicates by id`() = runTest {
        val vm = makeVm(testScheduler)
        vm.addNews(ArrayList(listOf(NewsInstance(id = "n1"), NewsInstance(id = "n2"))))
        vm.addNews(ArrayList(listOf(NewsInstance(id = "n2"), NewsInstance(id = "n3"))))
        assertEquals(3, vm.allNews.value.size)
    }

    @Test
    fun `updateNews replaces all news`() = runTest {
        val vm = makeVm(testScheduler)
        vm.addNews(ArrayList(listOf(NewsInstance(id = "n1"))))
        vm.updateNews(ArrayList(listOf(NewsInstance(id = "n2"))))
        assertEquals(1, vm.allNews.value.size)
        assertEquals("n2", vm.allNews.value.first().id)
    }

    // ---------------------- numberOfListNeedToLoad ----------------------
    @Test
    fun `decreaseNumberOfListNeedToLoad decreases until zero`() = runTest {
        val vm = makeVm(testScheduler)
        assertEquals(2, vm.numberOfListNeedToLoad)
        vm.decreaseNumberOfListNeedToLoad(1)
        assertEquals(1, vm.numberOfListNeedToLoad)
        vm.decreaseNumberOfListNeedToLoad(1)
        assertEquals(0, vm.numberOfListNeedToLoad)
        // should not go negative because guard checks > 0 before decrementing
        vm.decreaseNumberOfListNeedToLoad(1)
        assertEquals(0, vm.numberOfListNeedToLoad)
    }

    // ---------------------- like/unlike ----------------------
    @Test
    fun `clickLikeButton likes then unlikes updating counts and flow`() = runTest {
        val newsInteractor = FakeNewsInteractor()
        val vm = makeVm(testScheduler, newsInteractor = newsInteractor)
        vm.currentUser = UserInstance(uid = "u1", name = "Me")
        vm.addLikeCountData("n1", 5)

        vm.clickLikeButton("n1")
        assertEquals(1, vm.likedPosts.value["n1"])
        assertEquals(6, vm.likeCountList.value["n1"])

        vm.clickLikeButton("n1")
        assertNull(vm.likedPosts.value["n1"])
        assertEquals(5, vm.likeCountList.value["n1"])
    }

    @Test
    fun `clickLikeButton on unseen id defaults like count to 1`() = runTest {
        val vm = makeVm(testScheduler)
        vm.currentUser = UserInstance(uid = "u1")
        vm.clickLikeButton("newPost")
        assertEquals(1, vm.likeCountList.value["newPost"])
        assertEquals(1, vm.likedPosts.value["newPost"])
    }

    @Test
    fun `addLikeCountData and addCommentCountData populate maps`() = runTest {
        val vm = makeVm(testScheduler)
        vm.addLikeCountData("post1", 10)
        vm.addCommentCountData("post1", 3)
        assertEquals(10, vm.likeCountList.value["post1"])
        assertEquals(3, vm.commentCountList.value["post1"])
    }

    // ---------------------- comment click status ----------------------
    @Test
    fun `clickCommentButton and resetCommentStatus`() = runTest {
        val vm = makeVm(testScheduler)
        val news = NewsInstance(id = "n1")
        vm.clickCommentButton(news)
        assertEquals("n1", vm.commentStatus.value?.id)
        vm.resetCommentStatus()
        assertNull(vm.commentStatus.value)
    }

    // ---------------------- deleteOrHideNew / deletePoll ----------------------
    @Test
    fun `deleteOrHideNew with Delete action calls interactor delete and removes from list`() = runTest {
        val newsInteractor = FakeNewsInteractor()
        val vm = makeVm(testScheduler, newsInteractor = newsInteractor)
        val news = NewsInstance(id = "n1")
        vm.listNews.add(news)

        vm.deleteOrHideNew("Delete", "n1")
        advanceUntilIdle()

        assertEquals("n1", newsInteractor.deletedNews?.id)
        assertTrue(vm.listNews.none { it.id == "n1" })
    }

    @Test
    fun `deleteOrHideNew with Hide action does not call interactor delete but still removes locally`() = runTest {
        val newsInteractor = FakeNewsInteractor()
        val vm = makeVm(testScheduler, newsInteractor = newsInteractor)
        val news = NewsInstance(id = "n1")
        vm.listNews.add(news)

        vm.deleteOrHideNew("Hide", "n1")
        advanceUntilIdle()

        assertNull(newsInteractor.deletedNews)
        assertTrue(vm.listNews.none { it.id == "n1" })
    }

    @Test
    fun `deleteOrHideNew does nothing when news not found`() = runTest {
        val vm = makeVm(testScheduler)
        vm.deleteOrHideNew("Delete", "missing")
        advanceUntilIdle()
        assertEquals(0, vm.listNews.size)
    }

    @Test
    fun `deletePoll removes news with matching pollId`() = runTest {
        val newsInteractor = FakeNewsInteractor()
        val vm = makeVm(testScheduler, newsInteractor = newsInteractor)
        val news = NewsInstance(id = "n1", pollId = "poll1")
        vm.listNews.add(news)

        vm.deletePoll("n1", "group1")
        advanceUntilIdle()

        assertTrue(vm.listNews.none { it.id == "n1" })
    }

    @Test
    fun `deletePoll does nothing when news has no pollId`() = runTest {
        val vm = makeVm(testScheduler)
        val news = NewsInstance(id = "n1", pollId = null)
        vm.listNews.add(news)

        vm.deletePoll("n1", "group1")
        advanceUntilIdle()

        assertEquals(1, vm.listNews.size)
    }

    // ---------------------- findUserById / cache ----------------------
    @Test
    fun `findUserById delegates to interactor`() = runTest {
        val user = UserInstance(uid = "u1", name = "Alice")
        val vm = makeVm(testScheduler, userInteractor = FakeUserInteractor(usersById = mapOf("u1" to user)))
        val result = vm.findUserById("u1")
        assertEquals("Alice", result?.name)
    }

    @Test
    fun `findUserByIdInCache returns null for unknown user`() = runTest {
        val vm = makeVm(testScheduler)
        assertNull(vm.findUserByIdInCache("nonexistent"))
    }

    @Test
    fun `ensureUserLoaded populates cache`() = runTest {
        val user = UserInstance(uid = "u1", name = "Alice")
        val vm = makeVm(testScheduler, userInteractor = FakeUserInteractor(usersById = mapOf("u1" to user)))
        vm.ensureUserLoaded("u1")
        advanceUntilIdle()
        assertEquals("Alice", vm.findUserByIdInCache("u1")?.name)
    }

    @Test
    fun `ensureUserLoaded ignores blank id`() = runTest {
        val vm = makeVm(testScheduler)
        vm.ensureUserLoaded("")
        advanceUntilIdle()
        assertNull(vm.findUserByIdInCache(""))
    }

    // ---------------------- searchUserByName ----------------------
    @Test
    fun `searchUserByName with blank name returns empty list`() = runTest {
        val vm = makeVm(testScheduler)
        val result = vm.searchUserByName("")
        assertEquals(0, result.size)
    }

    @Test
    fun `searchUserByName delegates to interactor`() = runTest {
        val userList = listOf(UserInstance(uid = "u1", name = "Alice"))
        val vm = makeVm(testScheduler, userInteractor = FakeUserInteractor(currentUserId = null, searchResult = userList))
        val result = vm.searchUserByName("Alice")
        assertEquals(1, result.size)
        assertEquals("u1", result[0].uid)
    }

    @Test
    fun `searchUserByName returns empty list when interactor returns null`() = runTest {
        val vm = makeVm(testScheduler, userInteractor = FakeUserInteractor(searchResult = null))
        val result = vm.searchUserByName("Alice")
        assertEquals(0, result.size)
    }

    // ---------------------- ensureSharedNew ----------------------
    @Test
    fun `ensureSharedNew loads from local listNews first`() = runTest {
        val vm = makeVm(testScheduler)
        vm.listNews.add(NewsInstance(id = "shared1", message = "hi"))
        vm.ensureSharedNew("shared1")
        assertEquals("hi", vm.sharedNewsById.value["shared1"]?.message)
    }

    @Test
    fun `ensureSharedNew falls back to interactor when not local`() = runTest {
        val vm = makeVm(
            testScheduler,
            newsInteractor = FakeNewsInteractor(findNewByIdResult = NewsInstance(id = "remote1", message = "remote"))
        )
        vm.ensureSharedNew("remote1")
        assertEquals("remote", vm.sharedNewsById.value["remote1"]?.message)
    }

    @Test
    fun `ensureSharedNew ignores blank id`() = runTest {
        val vm = makeVm(testScheduler)
        vm.ensureSharedNew("")
        assertTrue(vm.sharedNewsById.value.isEmpty())
    }

    // ---------------------- isFriendOf ----------------------
    @Test
    fun `isFriendOf returns true when user is friend`() = runTest {
        val currentUser = UserInstance(uid = "me", name = "Me").apply { friends = arrayListOf("friend1", "friend2") }
        val vm = makeVm(
            testScheduler,
            userInteractor = FakeUserInteractor(
                currentUserId = "me",
                usersById = mapOf(
                    "me" to currentUser,
                    "friend1" to UserInstance(uid = "friend1"),
                    "friend2" to UserInstance(uid = "friend2")
                )
            )
        )
        vm.getCurrentUserAndFriends()
        advanceUntilIdle()
        assertEquals(true, vm.isFriendOf("friend1"))
        assertEquals(false, vm.isFriendOf("stranger"))
    }

    // ---------------------- clearLocalData ----------------------
    @Test
    fun `clearLocalData delegates to interactor`() = runTest {
        val userInteractor = FakeUserInteractor()
        val vm = makeVm(testScheduler, userInteractor = userInteractor)
        vm.clearLocalData()
        advanceUntilIdle()
        assertTrue(userInteractor.clearedLocalData)
    }

    // ---------------------- updateCurrentUser ----------------------
    @Test
    fun `updateCurrentUser stores user and saves via interactor`() = runTest {
        val userInteractor = FakeUserInteractor()
        val vm = makeVm(testScheduler, userInteractor = userInteractor)
        val user = UserInstance(uid = "u1", name = "Me")
        vm.updateCurrentUser(user)
        assertEquals("u1", vm.currentUser?.uid)
        assertEquals("u1", userInteractor.savedCurrentUser?.uid)
    }
}
