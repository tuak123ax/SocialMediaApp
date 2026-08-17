package com.minhtu.firesocialmedia.presentation.profile

import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.news.ProfileNewsRepository
import com.minhtu.firesocialmedia.domain.repository.profile.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.profile.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.profile.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.profile.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.profile.DeletePollUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.profile.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.profile.entity.news.NewsInstance
import com.minhtu.firesocialmedia.profile.entity.user.UserInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeUserRepository(
    private val usersById: Map<String, UserDTO?> = emptyMap()
) : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class FakeNotificationRepository : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(currentUserUid: String) = emptyList<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>()
    override suspend fun saveNotificationToDatabase(id: String, instance: List<com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance>) {}
    override suspend fun deleteNotificationFromDatabase(id: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

private class FakeProfileNewsRepository(
    private val newInstance: NewsInstance? = null
) : ProfileNewsRepository {
    var deletedNews: NewsInstance? = null
    var lastDeletePollArgs: Triple<String, String, String>? = null
    override suspend fun getNew(newId: String): NewsInstance? = newInstance
    override suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean {
        deletedNews = new
        return true
    }
    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean {
        lastDeletePollArgs = Triple(newsId, pollId, groupId)
        return true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EngagementViewModelTest {

    // Poster is not found by default so SaveLikeNotificationUseCase's server-side
    // notification path (which performs real network I/O) is never triggered.
    private fun buildViewModel(
        userRepo: UserRepository = FakeUserRepository(),
        newsRepo: ProfileNewsRepository = FakeProfileNewsRepository()
    ): EngagementViewModel = EngagementViewModel(
        UpdateLikeCountForNewUseCase(newsRepo),
        SaveLikedPostUseCase(userRepo),
        SaveLikeNotificationUseCase(GetUserUseCase(userRepo), SaveNotificationToDatabaseUseCase(FakeNotificationRepository())),
        GetNewByIdUseCase(newsRepo),
        DeleteNewsUseCase(newsRepo),
        DeletePollUseCase(newsRepo)
    )

    @Test
    fun `seedLikedPosts copies liked posts from user`() = runTest {
        val vm = buildViewModel()
        val user = UserInstance(uid = "u1", likedPosts = hashMapOf("n1" to 1))
        vm.seedLikedPosts(user)
        assertEquals(1, vm.likedPosts.value["n1"])
    }

    @Test
    fun `seedLikedPosts with null user clears liked posts`() = runTest {
        val vm = buildViewModel()
        vm.seedLikedPosts(null)
        assertTrue(vm.likedPosts.value.isEmpty())
    }

    @Test
    fun `addLikeCountData and addCommentCountData populate maps`() = runTest {
        val vm = buildViewModel()
        vm.addLikeCountData("n1", 10)
        vm.addCommentCountData("n1", 3)
        assertEquals(10, vm.likeCountList.value["n1"])
        assertEquals(3, vm.commentCountList.value["n1"])
    }

    @Test
    fun `clickLikeButton likes an unliked post and increments count`() = runTest {
        val vm = buildViewModel()
        vm.addLikeCountData("n1", 5)
        val currentUser = UserInstance(uid = "liker")

        vm.clickLikeButton("n1", "poster1", currentUser)

        assertEquals(1, vm.likedPosts.value["n1"])
        assertEquals(6, vm.likeCountList.value["n1"])
    }

    @Test
    fun `clickLikeButton unlikes an already liked post and decrements count`() = runTest {
        val vm = buildViewModel()
        vm.addLikeCountData("n1", 5)
        val currentUser = UserInstance(uid = "liker")

        vm.clickLikeButton("n1", "poster1", currentUser) // like
        vm.clickLikeButton("n1", "poster1", currentUser) // unlike

        assertNull(vm.likedPosts.value["n1"])
        assertEquals(5, vm.likeCountList.value["n1"])
    }

    @Test
    fun `updateLikeStatus republishes liked posts snapshot`() = runTest {
        val vm = buildViewModel()
        val currentUser = UserInstance(uid = "liker")
        vm.clickLikeButton("n1", "poster1", currentUser)

        vm.updateLikeStatus()

        assertEquals(1, vm.likedPosts.value["n1"])
    }

    @Test
    fun `clickCommentButton sets and resetCommentStatus clears commentStatus`() = runTest {
        val vm = buildViewModel()
        val news = NewsInstance(id = "n1")

        vm.clickCommentButton(news)
        assertEquals(news, vm.commentStatus.value)

        vm.resetCommentStatus()
        assertNull(vm.commentStatus.value)
    }

    @Test
    fun `ensureSharedNew fetches and caches shared news`() = runTest {
        val news = NewsInstance(id = "shared1")
        val newsRepo = FakeProfileNewsRepository(newInstance = news)
        val vm = buildViewModel(newsRepo = newsRepo)

        vm.ensureSharedNew("shared1")

        assertEquals(news, vm.sharedNewsById.value["shared1"])
    }

    @Test
    fun `ensureSharedNew is no-op for blank id`() = runTest {
        val vm = buildViewModel()
        vm.ensureSharedNew("")
        assertTrue(vm.sharedNewsById.value.isEmpty())
    }

    @Test
    fun `ensureSharedNew does not refetch already cached news`() = runTest {
        var callCount = 0
        val newsRepo = object : ProfileNewsRepository {
            override suspend fun getNew(newId: String): NewsInstance? {
                callCount++
                return NewsInstance(id = newId)
            }
            override suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean = true
            override suspend fun updateLikeCountForNew(newsId: String, value: Int) {}
            override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
        }
        val vm = buildViewModel(newsRepo = newsRepo)

        vm.ensureSharedNew("shared2")
        vm.ensureSharedNew("shared2")

        assertEquals(1, callCount)
    }

    @Test
    fun `deleteNews delegates to use case`() = runTest {
        val newsRepo = FakeProfileNewsRepository()
        val vm = buildViewModel(newsRepo = newsRepo)
        val news = NewsInstance(id = "n1")

        vm.deleteNews(news)

        assertEquals(news, newsRepo.deletedNews)
    }

    @Test
    fun `deletePoll delegates to use case and returns result`() = runTest {
        val newsRepo = FakeProfileNewsRepository()
        val vm = buildViewModel(newsRepo = newsRepo)

        val result = vm.deletePoll("n1", "p1", "g1")

        assertTrue(result)
        assertEquals(Triple("n1", "p1", "g1"), newsRepo.lastDeletePollArgs)
    }
}
