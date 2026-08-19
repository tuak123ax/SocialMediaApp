package com.minhtu.firesocialmedia.presentation.comment

import com.minhtu.firesocialmedia.comment.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.comment.entity.user.UserInstance
import com.minhtu.firesocialmedia.data.remote.service.clipboard.comment.ClipboardService
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance as SharedNotificationInstance
import com.minhtu.firesocialmedia.domain.repository.comment.UserRepository
import com.minhtu.firesocialmedia.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import com.minhtu.firesocialmedia.domain.usecases.comment.SaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.UpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.domain.usecases.comment.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeCommentInteractor : CommentInteractor {
    var comments: List<CommentInstance>? = emptyList()
    var saveCommentResult = true
    var saveSubCommentResult = true
    val savedComments = mutableListOf<Triple<String, String, CommentInstance>>()
    val savedSubComments = mutableListOf<BaseNewsInstance>()
    val deletedComments = mutableListOf<BaseNewsInstance>()
    val deletedSubComments = mutableListOf<Pair<String, BaseNewsInstance>>()

    override suspend fun saveComment(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean {
        savedComments.add(Triple(selectedNewId, commentId, instance))
        return saveCommentResult
    }

    override suspend fun saveSubComment(
        id: String,
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ): Boolean {
        savedSubComments.add(instance)
        return saveSubCommentResult
    }

    override suspend fun deleteComment(selectedNewId: String, comment: BaseNewsInstance) {
        deletedComments.add(comment)
    }

    override suspend fun deleteSubComment(selectedNewId: String, parentCommentId: String, comment: BaseNewsInstance) {
        deletedSubComments.add(parentCommentId to comment)
    }

    override suspend fun getAllComments(newsId: String): List<CommentInstance>? = comments
}

private class FakeCommentDbRepository : CommentDbRepository {
    var saveLikedCommentsResult = true
    val likeCountUpdates = mutableListOf<Triple<String, String, Int>>()
    val subLikeCountUpdates = mutableListOf<Triple<String, String, Int>>()
    val commentCountUpdates = mutableListOf<Pair<String, Int>>()
    val replyCountUpdates = mutableListOf<Triple<String, String, Int>>()

    override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
    override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
    override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
    override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}

    override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {
        commentCountUpdates.add(id to value)
    }

    override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {
        replyCountUpdates.add(Triple(id, currentCommentId, value))
    }

    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {
        likeCountUpdates.add(Triple(selectedNewId, likedComment, value))
    }

    override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {
        subLikeCountUpdates.add(Triple(selectedNewId, likedComment, value))
    }

    override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean = saveLikedCommentsResult

    override suspend fun syncComments(): Boolean = true
    override suspend fun clearComments() {}
}

private class FakeUserRepository : UserRepository {
    var userToReturn: UserDTO? = null

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = userToReturn
}

private class FakeNotificationRepository : NotificationRepository {
    val savedNotifications = mutableListOf<Pair<String, List<SharedNotificationInstance>>>()

    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<SharedNotificationInstance>? = emptyList()

    override suspend fun saveNotificationToDatabase(id: String, instance: List<SharedNotificationInstance>) {
        savedNotifications.add(id to instance)
    }

    override suspend fun deleteNotificationFromDatabase(id: String, notification: SharedNotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: SharedNotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = Result.success(Unit)
}

private class FakeClipboardService : ClipboardService {
    val copiedTexts = mutableListOf<String>()
    override fun copy(text: String) {
        copiedTexts.add(text)
    }
}

private class FakeNetworkMonitor : NetworkMonitor {
    override val isOnline: kotlinx.coroutines.flow.Flow<Boolean> = MutableStateFlow(true)
}

private class FakePlatformContext : PlatformContext {
    override val networkMonitor: NetworkMonitor = FakeNetworkMonitor()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CommentViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        scheduler: TestCoroutineScheduler,
        interactor: FakeCommentInteractor = FakeCommentInteractor(),
        commentDb: FakeCommentDbRepository = FakeCommentDbRepository(),
        userRepository: FakeUserRepository = FakeUserRepository(),
        notificationRepository: FakeNotificationRepository = FakeNotificationRepository(),
        clipboardService: FakeClipboardService = FakeClipboardService()
    ): CommentFeatureViewModel {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        return CommentFeatureViewModel(
            commentInteractor = interactor,
            getUserUseCase = GetUserUseCase(userRepository),
            saveLikedCommentsUseCase = SaveLikedCommentsUseCase(commentDb),
            saveNotificationToDatabaseUseCase = SaveNotificationToDatabaseUseCase(notificationRepository),
            updateCommentCountForNewUseCase = UpdateCommentCountForNewUseCase(commentDb),
            updateReplyCountForCommentUseCase = UpdateReplyCountForCommentUseCase(commentDb),
            updateLikeCountForCommentUseCase = UpdateLikeCountForCommentUseCase(commentDb),
            updateLikeCountForSubCommentUseCase = UpdateLikeCountForSubCommentUseCase(commentDb),
            clipboardService = clipboardService,
            ioDispatcher = StandardTestDispatcher(scheduler)
        )
    }

    private fun user(uid: String = "user-1", name: String = "Alice") =
        UserInstance(uid = uid, name = name, email = "alice@test.com", image = "avatar.png", token = "token-1")

    // ---- messageFlow ----

    @Test
    fun `updateMessage updates messageFlow and message property`() = runTest {
        val vm = buildViewModel(testScheduler)

        vm.updateMessage("hello world")

        assertEquals("hello world", vm.messageFlow.value)
        assertEquals("hello world", vm.message)
    }

    @Test
    fun `message setter also updates messageFlow`() = runTest {
        val vm = buildViewModel(testScheduler)

        vm.message = "set directly"

        assertEquals("set directly", vm.messageFlow.value)
    }

    // ---- commentBeReplied ----

    @Test
    fun `updateCommentBeReplied sets and clears selected comment`() = runTest {
        val vm = buildViewModel(testScheduler)
        val comment = CommentInstance(id = "c1", posterName = "User")

        vm.updateCommentBeReplied(comment)
        assertEquals("c1", vm.commentBeReplied.value?.id)

        vm.updateCommentBeReplied(null)
        assertNull(vm.commentBeReplied.value)
    }

    // ---- allComments / getAllCommentsOfNew ----

    @Test
    fun `getAllCommentsOfNew loads comments into allComments flow`() = runTest {
        val interactor = FakeCommentInteractor().apply {
            comments = listOf(
                CommentInstance(id = "c1", message = "one"),
                CommentInstance(id = "c2", message = "two")
            )
        }
        val vm = buildViewModel(testScheduler, interactor)

        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()

        assertEquals(2, vm.allComments.value.size)
        assertEquals(2, vm.likeCountList.value.size)
    }

    @Test
    fun `getAllCommentsOfNew populates likeCountList including replies`() = runTest {
        val reply = CommentInstance(id = "r1", message = "reply", likeCount = 3)
        val parent = CommentInstance(
            id = "c1",
            message = "parent",
            likeCount = 5,
            listReplies = hashMapOf("r1" to reply)
        )
        val interactor = FakeCommentInteractor().apply { comments = listOf(parent) }
        val vm = buildViewModel(testScheduler, interactor)

        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()

        assertEquals(5, vm.likeCountList.value["c1"])
        assertEquals(3, vm.likeCountList.value["r1"])
    }

    @Test
    fun `getAllCommentsOfNew with null result leaves allComments empty`() = runTest {
        val interactor = FakeCommentInteractor().apply { comments = null }
        val vm = buildViewModel(testScheduler, interactor)

        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()

        assertTrue(vm.allComments.value.isEmpty())
    }

    @Test
    fun `clearCommentList empties allComments flow`() = runTest {
        val interactor = FakeCommentInteractor().apply {
            comments = listOf(CommentInstance(id = "c1", message = "one"))
        }
        val vm = buildViewModel(testScheduler, interactor)
        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()
        assertEquals(1, vm.allComments.value.size)

        vm.clearCommentList()

        assertEquals(0, vm.allComments.value.size)
    }

    // ---- createCommentStatus / sendComment ----

    @Test
    fun `sendComment with blank message does nothing`() = runTest {
        val interactor = FakeCommentInteractor()
        val vm = buildViewModel(testScheduler, interactor)
        vm.updateMessage("   ")

        vm.sendComment(user(), "news-1", "poster-1")
        advanceUntilIdle()

        assertNull(vm.createCommentStatus.value)
        assertTrue(interactor.savedComments.isEmpty())
    }

    @Test
    fun `sendComment happy path saves comment and updates createCommentStatus`() = runTest {
        val interactor = FakeCommentInteractor().apply { saveCommentResult = true }
        val userRepository = FakeUserRepository().apply { userToReturn = null }
        val vm = buildViewModel(testScheduler, interactor, userRepository = userRepository)
        vm.updateMessage("hi there")

        vm.sendComment(user(), "news-1", "poster-1")
        advanceUntilIdle()

        assertEquals(true, vm.createCommentStatus.value)
        assertEquals(1, interactor.savedComments.size)
        assertEquals(1, vm.allComments.value.size)
        assertEquals("", vm.message)
    }

    @Test
    fun `sendComment failure surfaces false createCommentStatus`() = runTest {
        val interactor = FakeCommentInteractor().apply { saveCommentResult = false }
        val vm = buildViewModel(testScheduler, interactor)
        vm.updateMessage("hi there")

        vm.sendComment(user(), "news-1", "poster-1")
        advanceUntilIdle()

        assertEquals(false, vm.createCommentStatus.value)
    }

    @Test
    fun `sendComment notifies poster when poster is found`() = runTest {
        val interactor = FakeCommentInteractor()
        val notificationRepository = FakeNotificationRepository()
        val userRepository = FakeUserRepository().apply {
            userToReturn = UserDTO(uid = "poster-1", name = "Poster", token = "poster-token")
        }
        val vm = buildViewModel(
            testScheduler,
            interactor,
            userRepository = userRepository,
            notificationRepository = notificationRepository
        )
        vm.updateMessage("hi there")

        vm.sendComment(user(), "news-1", "poster-1")
        advanceUntilIdle()

        assertEquals(1, notificationRepository.savedNotifications.size)
        assertEquals("poster-1", notificationRepository.savedNotifications.first().first)
    }

    @Test
    fun `resetCommentStatus clears createCommentStatus`() = runTest {
        val interactor = FakeCommentInteractor()
        val vm = buildViewModel(testScheduler, interactor)
        vm.updateMessage("hi there")
        vm.sendComment(user(), "news-1", "poster-1")
        advanceUntilIdle()
        assertEquals(true, vm.createCommentStatus.value)

        vm.resetCommentStatus()

        assertNull(vm.createCommentStatus.value)
    }

    @Test
    fun `sendComment while replying routes to reply flow and clears commentBeReplied`() = runTest {
        val interactor = FakeCommentInteractor().apply {
            comments = listOf(CommentInstance(id = "c1", message = "parent"))
        }
        val vm = buildViewModel(testScheduler, interactor)
        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()
        val parent = vm.allComments.value.first { it.id == "c1" }
        vm.updateCommentBeReplied(parent)
        vm.updateMessage("a reply")

        vm.sendComment(user(), "news-1", "poster-1")
        advanceUntilIdle()

        assertNull(vm.commentBeReplied.value)
        assertEquals(1, interactor.savedSubComments.size)
        assertEquals("", vm.message)
    }

    // ---- likedComments / likeCountList ----

    @Test
    fun `onLikeComment marks comment liked and increments like count`() = runTest {
        val commentDb = FakeCommentDbRepository()
        val vm = buildViewModel(testScheduler, commentDb = commentDb)
        val comment = CommentInstance(id = "c1", message = "hi")

        vm.onLikeComment("news-1", user(), comment)
        advanceUntilIdle()

        assertEquals(1, vm.likedComments.value["c1"])
        assertEquals(1, vm.likeCountList.value["c1"])
        assertTrue(vm.sendLikeDataStatus.value)
    }

    @Test
    fun `onLikeComment twice toggles back to unliked and decrements count`() = runTest {
        val commentDb = FakeCommentDbRepository()
        val vm = buildViewModel(testScheduler, commentDb = commentDb)
        val comment = CommentInstance(id = "c1", message = "hi")

        vm.onLikeComment("news-1", user(), comment)
        advanceUntilIdle()
        vm.onLikeComment("news-1", user(), comment)
        advanceUntilIdle()

        assertFalse(vm.likedComments.value.containsKey("c1"))
        assertEquals(0, vm.likeCountList.value["c1"])
    }

    @Test
    fun `onLikeComment persists failure reflected in sendLikeDataStatus`() = runTest {
        val commentDb = FakeCommentDbRepository().apply { saveLikedCommentsResult = false }
        val vm = buildViewModel(testScheduler, commentDb = commentDb)
        val comment = CommentInstance(id = "c1", message = "hi")

        vm.onLikeComment("news-1", user(), comment)
        advanceUntilIdle()

        assertFalse(vm.sendLikeDataStatus.value)
    }

    @Test
    fun `updateLikeCommentOfCurrentUser seeds like cache used by onLikeComment`() = runTest {
        val vm = buildViewModel(testScheduler)
        val currentUser = user().apply { likedComments = hashMapOf("c1" to 1) }
        vm.updateLikeCommentOfCurrentUser(currentUser)

        // c1 is already liked in the cache, so liking it again should unlike it.
        vm.onLikeComment("news-1", currentUser, CommentInstance(id = "c1"))
        advanceUntilIdle()

        assertFalse(vm.likedComments.value.containsKey("c1"))
    }

    @Test
    fun `updateLikeStatus republishes likedComments flow from cache`() = runTest {
        val vm = buildViewModel(testScheduler)
        val currentUser = user().apply { likedComments = hashMapOf("c1" to 1) }
        vm.updateLikeCommentOfCurrentUser(currentUser)

        vm.updateLikeStatus()

        assertEquals(1, vm.likedComments.value["c1"])
    }

    // ---- deletion ----

    @Test
    fun `onDeleteComment removes top level comment via interactor`() = runTest {
        val interactor = FakeCommentInteractor().apply {
            comments = listOf(CommentInstance(id = "c1", message = "one"))
        }
        val vm = buildViewModel(testScheduler, interactor)
        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()

        vm.onDeleteComment("news-1", CommentInstance(id = "c1", message = "one"))
        advanceUntilIdle()

        assertEquals(1, interactor.deletedComments.size)
        assertTrue(interactor.deletedSubComments.isEmpty())
    }

    @Test
    fun `onDeleteComment removes sub comment via interactor`() = runTest {
        val reply = CommentInstance(id = "r1", message = "reply")
        val parent = CommentInstance(id = "c1", message = "parent", listReplies = hashMapOf("r1" to reply))
        val interactor = FakeCommentInteractor().apply { comments = listOf(parent) }
        val vm = buildViewModel(testScheduler, interactor)
        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()

        vm.onDeleteComment("news-1", CommentInstance(id = "r1", message = "reply"))
        advanceUntilIdle()

        assertEquals(1, interactor.deletedSubComments.size)
        assertEquals("c1", interactor.deletedSubComments.first().first)
        assertTrue(interactor.deletedComments.isEmpty())
    }

    // ---- misc ----

    @Test
    fun `findUserById maps repository dto to comment user instance`() = runTest {
        val userRepository = FakeUserRepository().apply {
            userToReturn = UserDTO(uid = "u2", name = "Bob")
        }
        val vm = buildViewModel(testScheduler, userRepository = userRepository)

        val result = vm.findUserById("u2")

        assertEquals("u2", result?.uid)
        assertEquals("Bob", result?.name)
    }

    @Test
    fun `findUserById returns null when user not found`() = runTest {
        val userRepository = FakeUserRepository().apply { userToReturn = null }
        val vm = buildViewModel(testScheduler, userRepository = userRepository)

        val result = vm.findUserById("missing")

        assertNull(result)
    }

    @Test
    fun `updateImage updates image field`() = runTest {
        val vm = buildViewModel(testScheduler)

        vm.updateImage("image.png")

        assertEquals("image.png", vm.image)
    }

    @Test
    fun `copyToClipboard delegates to clipboard service`() = runTest {
        val clipboardService = FakeClipboardService()
        val vm = buildViewModel(testScheduler, clipboardService = clipboardService)

        vm.copyToClipboard("copied text", FakePlatformContext())
        advanceUntilIdle()

        assertEquals(listOf("copied text"), clipboardService.copiedTexts)
    }
}
