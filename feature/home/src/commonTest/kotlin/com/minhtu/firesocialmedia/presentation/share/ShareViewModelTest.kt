package com.minhtu.firesocialmedia.presentation.share

import com.minhtu.firesocialmedia.constants.home.Constants
import com.minhtu.firesocialmedia.domain.entity.home.LatestNewsResult
import com.minhtu.firesocialmedia.domain.interactor.home.NewsInteractor
import com.minhtu.firesocialmedia.domain.interactor.home.UserInteractor
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ShareViewModelTest {

    private class FakeUserInteractor(
        val usersById: Map<String, UserInstance?> = emptyMap()
    ) : UserInteractor {
        override suspend fun getCurrentUserId(): String? = null
        override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = usersById[userId]
        override suspend fun updateFcmToken(user: UserInstance) {}
        override suspend fun saveCurrentUserInfo(user: UserInstance) {}
        override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
        override suspend fun searchUserByName(name: String): List<UserInstance>? = emptyList()
        override suspend fun storeUserFriendsToRoom(friends: List<UserInstance?>) {}
        override suspend fun clearLocalData() {}
        override suspend fun clearLocalFriends() {}
    }

    private class FakeNewsInteractor(
        val saveNewsResult: Boolean = true,
        var savedNews: NewsInstance? = null
    ) : NewsInteractor {
        override suspend fun pageLatest(number: Int, lastTimePosted: Double?, lastKey: String?): LatestNewsResult? = null
        override suspend fun like(id: String, value: Int) {}
        override suspend fun unlike(id: String, value: Int) {}
        override suspend fun delete(new: NewsInstance) {}
        override suspend fun deletePoll(newsId: String, pollId: String, groupId: String): Boolean = true
        override suspend fun storeNewsToRoom(news: List<NewsInstance>) {}
        override suspend fun saveNews(news: NewsInstance): Boolean {
            savedNews = news
            return saveNewsResult
        }
        override suspend fun findNewById(newsId: String): NewsInstance? = null
    }

    private class FakeNotificationRepository : NotificationRepository {
        override suspend fun getAllNotificationsOfUser(currentUserUid: String) = emptyList<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>()
        override suspend fun saveNotificationToDatabase(id: String, instance: List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>) {}
        override suspend fun deleteNotificationFromDatabase(id: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
        override suspend fun updateIsReadStatusOfNotification(userId: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
        override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
    }

    private fun makeVm(
        scheduler: TestCoroutineScheduler,
        userInteractor: UserInteractor = FakeUserInteractor(),
        newsInteractor: NewsInteractor = FakeNewsInteractor()
    ): ShareViewModel = ShareViewModel(
        userInteractor,
        newsInteractor,
        SaveNotificationToDatabaseUseCase(FakeNotificationRepository()),
        StandardTestDispatcher(scheduler)
    )

    @Test
    fun `sharePost with no content sets shareError`() = runTest {
        val vm = makeVm(testScheduler)
        vm.sharePost(UserInstance(uid = "u1"))
        advanceUntilIdle()
        assertEquals(Constants.POST_NEWS_EMPTY_ERROR, vm.shareError.value)
        assertNull(vm.sharePostStatus.value)
    }

    @Test
    fun `sharePost with null current user does nothing`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateShareContent(NewsInstance(id = "orig1"))
        vm.sharePost(null)
        advanceUntilIdle()
        assertNull(vm.sharePostStatus.value)
        assertNull(vm.shareError.value)
    }

    @Test
    fun `sharePost with content and no friends saves news`() = runTest {
        val newsInteractor = FakeNewsInteractor()
        val vm = makeVm(testScheduler, newsInteractor = newsInteractor)
        val user = UserInstance(uid = "u1", name = "Me", image = "img")
        user.friends = arrayListOf()
        vm.updateShareContent(NewsInstance(id = "orig1"))
        vm.updateShareMessage("check this out")

        vm.sharePost(user)
        advanceUntilIdle()

        assertEquals(true, vm.sharePostStatus.value)
        assertEquals("check this out", newsInteractor.savedNews?.message)
        assertEquals("orig1", newsInteractor.savedNews?.shareContentId)
    }

    @Test
    fun `resetShareContentAndStatus resets all share fields`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateShareMessage("hello")
        vm.updateShareContent(NewsInstance(id = "x"))
        vm.sharePost(UserInstance(uid = "u1").apply { friends = arrayListOf() })
        advanceUntilIdle()

        vm.resetShareContentAndStatus()

        assertNull(vm.sharePostStatus.value)
        assertNull(vm.shareError.value)
    }
}
