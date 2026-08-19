package com.minhtu.firesocialmedia.presentation.comment

import com.minhtu.firesocialmedia.data.remote.service.clipboard.home.ClipboardService
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.interactor.comment.HomeCommentInteractor
import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.home.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeSaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.HomeUpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.home.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import com.minhtu.firesocialmedia.home.entity.user.UserInstance
import com.minhtu.firesocialmedia.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class HomeCommentFeatureViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeCommentInteractor(
        val saveCommentResult: Boolean = true,
        val saveSubCommentResult: Boolean = true,
        val allComments: List<CommentInstance>? = emptyList(),
        var deletedComment: BaseNewsInstance? = null,
        var deletedSubComment: BaseNewsInstance? = null
    ) : HomeCommentInteractor {
        override suspend fun saveComment(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = saveCommentResult
        override suspend fun saveSubComment(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = saveSubCommentResult
        override suspend fun deleteComment(selectedNewId: String, comment: BaseNewsInstance) {
            deletedComment = comment
        }
        override suspend fun deleteSubComment(selectedNewId: String, parentCommentId: String, comment: BaseNewsInstance) {
            deletedSubComment = comment
        }
        override suspend fun getAllComments(newsId: String): List<CommentInstance>? = allComments
    }

    private class FakeUserRepository(val usersById: Map<String, UserDTO?> = emptyMap()) : UserRepository {
        override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = usersById[userId]
        override suspend fun getCurrentUserUid(): String? = null
        override suspend fun searchUserByName(name: String): List<UserDTO>? = null
        override suspend fun updateFCMTokenForCurrentUser(user: UserDTO) {}
    }

    private class FakeCommentDbRepository(
        val saveLikedResult: Boolean = true
    ) : HomeCommentDbRepository {
        var lastLikeCountUpdate: Triple<String, String, Int>? = null
        var lastSubLikeCountUpdate: List<Any>? = null
        var lastCommentCount: Pair<String, Int>? = null
        var lastReplyCount: Pair<String, Int>? = null

        override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
        override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
        override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
        override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
        override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {
            lastCommentCount = id to value
        }
        override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {
            lastReplyCount = currentCommentId to value
        }
        override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {
            lastLikeCountUpdate = Triple(selectedNewId, likedComment, value)
        }
        override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {
            lastSubLikeCountUpdate = listOf(selectedNewId, likedComment, parentCommentId, value)
        }
        override suspend fun saveLikedComments(id: String, value: HashMap<String, Int>): Boolean = saveLikedResult
        override suspend fun syncComments(): Boolean = true
        override suspend fun clearComments() {}
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

    private class FakeClipboardService : ClipboardService {
        var copiedText: String? = null
        override fun copy(text: String) {
            copiedText = text
        }
    }

    private class FakePlatformContext : PlatformContext {
        override val networkMonitor: NetworkMonitor = object : NetworkMonitor {
            override val isOnline = MutableStateFlow(true)
        }
    }

    private fun makeVm(
        scheduler: TestCoroutineScheduler,
        commentInteractor: HomeCommentInteractor = FakeCommentInteractor(),
        userRepository: UserRepository = FakeUserRepository(),
        commentDbRepository: HomeCommentDbRepository = FakeCommentDbRepository(),
        notificationRepository: NotificationRepository = FakeNotificationRepository(),
        clipboardService: ClipboardService = FakeClipboardService()
    ): HomeCommentFeatureViewModel {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        return HomeCommentFeatureViewModel(
        commentInteractor,
        GetUserUseCase(userRepository),
        HomeSaveLikedCommentsUseCase(commentDbRepository),
        SaveNotificationToDatabaseUseCase(notificationRepository),
        HomeUpdateCommentCountForNewUseCase(commentDbRepository),
        HomeUpdateReplyCountForCommentUseCase(commentDbRepository),
        HomeUpdateLikeCountForCommentUseCase(commentDbRepository),
        HomeUpdateLikeCountForSubCommentUseCase(commentDbRepository),
        clipboardService,
        StandardTestDispatcher(scheduler)
    )
    }

    @Test
    fun `sendComment adds comment and resets message`() = runTest {
        val dbRepo = FakeCommentDbRepository()
        val vm = makeVm(testScheduler, commentDbRepository = dbRepo)
        vm.updateMessage("hello")

        vm.sendComment(UserInstance(uid = "u1", name = "Me"), "news1", "poster1")
        advanceUntilIdle()

        assertEquals(true, vm.createCommentStatus.value)
        assertEquals(1, vm.allComments.value.size)
        assertEquals("", vm.message)
        assertEquals("news1" to 1, dbRepo.lastCommentCount)
    }

    @Test
    fun `sendComment with blank message does nothing`() = runTest {
        val vm = makeVm(testScheduler)
        vm.sendComment(UserInstance(uid = "u1"), "news1", "poster1")
        advanceUntilIdle()
        assertNull(vm.createCommentStatus.value)
        assertEquals(0, vm.allComments.value.size)
    }

    @Test
    fun `resetCommentStatus clears status`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateMessage("hi")
        vm.sendComment(UserInstance(uid = "u1"), "news1", "poster1")
        advanceUntilIdle()
        vm.resetCommentStatus()
        assertNull(vm.createCommentStatus.value)
    }

    @Test
    fun `updateCommentBeReplied stores the target comment`() = runTest {
        val vm = makeVm(testScheduler)
        val comment = CommentInstance(id = "c1")
        vm.updateCommentBeReplied(comment)
        assertEquals("c1", vm.commentBeReplied.value?.id)
        vm.updateCommentBeReplied(null)
        assertNull(vm.commentBeReplied.value)
    }

    @Test
    fun `sendComment while replying triggers reply flow and clears reply target`() = runTest {
        val dbRepo = FakeCommentDbRepository()
        val vm = makeVm(testScheduler, commentDbRepository = dbRepo)
        val parent = CommentInstance(id = "parent1")
        vm.listComments.add(parent)
        vm.updateCommentBeReplied(parent)
        vm.updateMessage("a reply")

        vm.sendComment(UserInstance(uid = "u1", name = "Me"), "news1", "poster1")
        advanceUntilIdle()

        assertNull(vm.commentBeReplied.value)
        assertEquals(1, parent.listReplies.size)
        assertEquals("parent1" to 1, dbRepo.lastReplyCount)
    }

    @Test
    fun `onLikeComment likes then unlikes updating counts`() = runTest {
        val vm = makeVm(testScheduler)
        val comment = CommentInstance(id = "c1", likeCount = 2)
        val user = UserInstance(uid = "u1")

        vm.onLikeComment("news1", user, comment)
        assertEquals(1, vm.likedComments.value["c1"])
        assertEquals(1, vm.likeCountList.value["c1"])

        vm.onLikeComment("news1", user, comment)
        assertNull(vm.likedComments.value["c1"])
        assertEquals(0, vm.likeCountList.value["c1"])
    }

    @Test
    fun `updateLikeCommentOfCurrentUser seeds like cache from user`() = runTest {
        val vm = makeVm(testScheduler)
        val user = UserInstance(uid = "u1")
        user.likedComments = hashMapOf("c1" to 1)
        vm.updateLikeCommentOfCurrentUser(user)
        vm.updateLikeStatus()
        assertEquals(1, vm.likedComments.value["c1"])
    }

    @Test
    fun `onDeleteComment deletes top-level comment via interactor`() = runTest {
        val interactor = FakeCommentInteractor()
        val vm = makeVm(testScheduler, commentInteractor = interactor)
        val comment = CommentInstance(id = "c1")
        vm.listComments.add(comment)

        vm.onDeleteComment("news1", comment)
        advanceUntilIdle()

        assertEquals("c1", interactor.deletedComment?.id)
    }

    @Test
    fun `onDeleteComment deletes sub comment via interactor`() = runTest {
        val interactor = FakeCommentInteractor()
        val vm = makeVm(testScheduler, commentInteractor = interactor)
        val subComment = CommentInstance(id = "sub1")
        vm.mapSubComments["sub1"] = subComment

        vm.onDeleteComment("news1", subComment)
        advanceUntilIdle()

        assertEquals("sub1", interactor.deletedSubComment?.id)
    }

    @Test
    fun `findUserById delegates through GetUserUseCase`() = runTest {
        val dto = UserDTO(uid = "u1", name = "Alice")
        val vm = makeVm(testScheduler, userRepository = FakeUserRepository(usersById = mapOf("u1" to dto)))
        val result = vm.findUserById("u1")
        assertEquals("Alice", result?.name)
    }

    @Test
    fun `getAllCommentsOfNew populates comments and like counts`() = runTest {
        val reply = CommentInstance(id = "r1", likeCount = 4)
        val comment = CommentInstance(id = "c1", likeCount = 2, listReplies = hashMapOf("r1" to reply))
        val interactor = FakeCommentInteractor(allComments = listOf(comment))
        val vm = makeVm(testScheduler, commentInteractor = interactor)

        vm.getAllCommentsOfNew("news1")
        advanceUntilIdle()

        assertEquals(1, vm.allComments.value.size)
        assertEquals(2, vm.likeCountList.value["c1"])
        assertEquals(4, vm.likeCountList.value["r1"])
    }

    @Test
    fun `clearCommentList empties the comments flow`() = runTest {
        val comment = CommentInstance(id = "c1")
        val interactor = FakeCommentInteractor(allComments = listOf(comment))
        val vm = makeVm(testScheduler, commentInteractor = interactor)
        vm.getAllCommentsOfNew("news1")
        advanceUntilIdle()

        vm.clearCommentList()
        assertTrue(vm.allComments.value.isEmpty())
    }

    @Test
    fun `copyToClipboard delegates to clipboard service`() = runTest {
        val clipboard = FakeClipboardService()
        val vm = makeVm(testScheduler, clipboardService = clipboard)
        vm.copyToClipboard("copied text", FakePlatformContext())
        advanceUntilIdle()
        assertEquals("copied text", clipboard.copiedText)
    }

    @Test
    fun `updateImage sets image field`() = runTest {
        val vm = makeVm(testScheduler)
        vm.updateImage("photo.png")
        assertEquals("photo.png", vm.image)
    }
}
