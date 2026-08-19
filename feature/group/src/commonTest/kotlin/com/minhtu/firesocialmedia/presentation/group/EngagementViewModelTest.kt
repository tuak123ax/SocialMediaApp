package com.minhtu.firesocialmedia.presentation.group

import com.minhtu.firesocialmedia.data.remote.dto.settings.group.PollDTO
import com.minhtu.firesocialmedia.domain.repository.group.UserRepository
import com.minhtu.firesocialmedia.domain.repository.news.GroupNewsRepository
import com.minhtu.firesocialmedia.domain.usecases.common.group.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikeNotificationUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.group.SaveLikedPostUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.DeleteNewsUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.GetNewByIdUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.group.UpdateLikeCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.newsfeed.group.DeletePollUseCase
import com.minhtu.firesocialmedia.group.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.group.entity.news.NewsInstance
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance as SharedNotificationInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeEngagementUserRepository : UserRepository {
    val users = hashMapOf<String, UserDTO>()
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun saveLikedPost(userId: String, likedPosts: HashMap<String, Int>): Boolean = true
}

private class FakeEngagementNotificationRepository : NotificationRepository {
    var saved: Pair<String, List<SharedNotificationInstance>>? = null
    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<SharedNotificationInstance>? = null
    override suspend fun saveNotificationToDatabase(id: String, instance: List<SharedNotificationInstance>) {
        saved = id to instance
    }
    override suspend fun deleteNotificationFromDatabase(id: String, notification: SharedNotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: SharedNotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

private class FakeEngagementNewsRepository : GroupNewsRepository {
    var newToReturn: NewsInstance? = NewsInstance(id = "shared1", message = "hello")
    var deleteResult: Boolean = true
    var deletePollResult: Boolean = true
    var lastUpdatedLikeCount: Pair<String, Int>? = null
    var lastDeletedNews: NewsInstance? = null

    override suspend fun getNew(newId: String): NewsInstance? = newToReturn
    override suspend fun deleteNewsFromDatabase(new: NewsInstance): Boolean {
        lastDeletedNews = new
        return deleteResult
    }
    override suspend fun updateLikeCountForNew(newsId: String, value: Int) {
        lastUpdatedLikeCount = newsId to value
    }
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = deletePollResult
    override suspend fun fetchPoll(pollId: String): PollDTO? = null
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
    override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
    override suspend fun createPoll(poll: PollDTO, newsId: String, groupId: String): Boolean = true
}

private fun makeVm(
    newsRepo: FakeEngagementNewsRepository,
    userRepo: FakeEngagementUserRepository = FakeEngagementUserRepository(),
    notificationRepo: FakeEngagementNotificationRepository = FakeEngagementNotificationRepository()
) = EngagementViewModel(
    UpdateLikeCountForNewUseCase(newsRepo),
    SaveLikedPostUseCase(userRepo),
    SaveLikeNotificationUseCase(GetUserUseCase(userRepo), SaveNotificationToDatabaseUseCase(notificationRepo)),
    GetNewByIdUseCase(newsRepo),
    DeleteNewsUseCase(newsRepo),
    DeletePollUseCase(newsRepo)
)

class EngagementViewModelTest {

    @Test
    fun `seedLikedPosts copies liked posts from user`() {
        val vm = makeVm(FakeEngagementNewsRepository())
        val user = UserInstance(uid = "u1", likedPosts = hashMapOf("n1" to 1))
        vm.seedLikedPosts(user)
        assertEquals(1, vm.likedPosts.value["n1"])
    }

    @Test
    fun `seedLikedPosts with null user results in empty map`() {
        val vm = makeVm(FakeEngagementNewsRepository())
        vm.seedLikedPosts(null)
        assertTrue(vm.likedPosts.value.isEmpty())
    }

    @Test
    fun `addLikeCountData and addCommentCountData populate maps`() {
        val vm = makeVm(FakeEngagementNewsRepository())
        vm.addLikeCountData("n1", 5)
        vm.addCommentCountData("n1", 3)
        assertEquals(5, vm.likeCountList.value["n1"])
        assertEquals(3, vm.commentCountList.value["n1"])
    }

    @Test
    fun `clickLikeButton on unliked post marks liked and increments count`() {
        val vm = makeVm(FakeEngagementNewsRepository())
        vm.addLikeCountData("n1", 0)
        vm.clickLikeButton("n1", "poster1", null)
        assertEquals(1, vm.likedPosts.value["n1"])
        assertEquals(1, vm.likeCountList.value["n1"])
    }

    @Test
    fun `clickLikeButton on already liked post unlikes and decrements count`() {
        val vm = makeVm(FakeEngagementNewsRepository())
        vm.addLikeCountData("n1", 0)
        vm.clickLikeButton("n1", "poster1", null) // like: 0 -> 1
        vm.clickLikeButton("n1", "poster1", null) // unlike: 1 -> 0
        assertTrue(!vm.likedPosts.value.containsKey("n1"))
        assertEquals(0, vm.likeCountList.value["n1"])
    }

    @Test
    fun `clickCommentButton sets commentStatus and resetCommentStatus clears it`() {
        val vm = makeVm(FakeEngagementNewsRepository())
        val news = NewsInstance(id = "n1")
        vm.clickCommentButton(news)
        assertEquals(news, vm.commentStatus.value)
        vm.resetCommentStatus()
        assertNull(vm.commentStatus.value)
    }

    @Test
    fun `ensureSharedNew fetches and caches shared news`() = runTest {
        val newsRepo = FakeEngagementNewsRepository().apply { newToReturn = NewsInstance(id = "shared1", message = "hi") }
        val vm = makeVm(newsRepo)
        vm.ensureSharedNew("shared1")
        assertEquals("hi", vm.sharedNewsById.value["shared1"]?.message)
    }

    @Test
    fun `ensureSharedNew with blank id does nothing`() = runTest {
        val newsRepo = FakeEngagementNewsRepository()
        val vm = makeVm(newsRepo)
        vm.ensureSharedNew("")
        assertTrue(vm.sharedNewsById.value.isEmpty())
    }

    @Test
    fun `ensureSharedNew does not refetch when already cached`() = runTest {
        val newsRepo = FakeEngagementNewsRepository().apply { newToReturn = NewsInstance(id = "shared1", message = "first") }
        val vm = makeVm(newsRepo)
        vm.ensureSharedNew("shared1")
        newsRepo.newToReturn = NewsInstance(id = "shared1", message = "second")
        vm.ensureSharedNew("shared1")
        assertEquals("first", vm.sharedNewsById.value["shared1"]?.message)
    }

    @Test
    fun `deleteNews delegates to use case`() = runTest {
        val newsRepo = FakeEngagementNewsRepository()
        val vm = makeVm(newsRepo)
        val news = NewsInstance(id = "n1")
        vm.deleteNews(news)
        assertEquals(news, newsRepo.lastDeletedNews)
    }

    @Test
    fun `deletePoll delegates to use case and returns result`() = runTest {
        val newsRepo = FakeEngagementNewsRepository().apply { deletePollResult = true }
        val vm = makeVm(newsRepo)
        val result = vm.deletePoll("n1", "p1", "g1")
        assertTrue(result)
    }

    @Test
    fun `SaveLikeNotificationUseCase does nothing when poster not found`() = runTest {
        // NOTE: The "poster found" branch of SaveLikeNotificationUseCase always falls through to
        // createMessageForServer/sendMessageToServer, which builds an android.org.json.JSONObject.
        // That class is not mocked/shadowed under plain JUnit unit tests (no Robolectric in this
        // module's test setup), so it throws "Method toString in org.json.JSONObject not mocked."
        // Only the "poster not found" branch (which returns before touching JSONObject) is
        // unit-testable here; the same limitation is documented for the profile module's analog.
        val userRepo = FakeEngagementUserRepository()
        val notificationRepo = FakeEngagementNotificationRepository()
        val useCase = SaveLikeNotificationUseCase(GetUserUseCase(userRepo), SaveNotificationToDatabaseUseCase(notificationRepo))
        useCase.invoke(UserInstance(uid = "liker"), "missingPoster", "n1")
        assertNull(notificationRepo.saved)
    }
}
