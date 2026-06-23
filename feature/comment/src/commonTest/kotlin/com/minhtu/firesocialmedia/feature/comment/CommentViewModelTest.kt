package com.minhtu.firesocialmedia.feature.comment

import com.minhtu.firesocialmedia.core.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.interactor.comment.CommentInteractor
import com.minhtu.firesocialmedia.core.domain.repository.CommonDbRepository
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.comment.SaveLikedCommentsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateCommentCountForNewUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateLikeCountForCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateLikeCountForSubCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.comment.UpdateReplyCountForCommentUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.feature.comment.presentation.comment.CommentFeatureViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeCommentInteractor : CommentInteractor {
    var comments: List<CommentInstance> = emptyList()

    override suspend fun saveComment(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true

    override suspend fun saveSubComment(
        id: String,
        selectedNewId: String,
        parentCommentId: String,
        instance: BaseNewsInstance
    ): Boolean = true

    override suspend fun deleteComment(selectedNewId: String, comment: BaseNewsInstance) {}

    override suspend fun deleteSubComment(selectedNewId: String, parentCommentId: String, comment: BaseNewsInstance) {}

    override suspend fun getAllComments(newsId: String): List<CommentInstance>? = comments
}

private class FakeCommonDbRepository : CommonDbRepository {
    override suspend fun saveLikedPost(id: String, value: HashMap<String, Int>): Boolean = true
    override suspend fun saveNewToDatabase(instance: NewsInstance): Boolean = true
    override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = true
    override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = true
    override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {}
    override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {}
    override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {}
    override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {}
    override suspend fun updateLikeCountForNewInDatabase(id: String, value: Int) {}
    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {}
    override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {}
    override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean = true
    override suspend fun saveFriend(id: String, value: ArrayList<String>) {}
    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {}
    override suspend fun syncData(currentUserId: String): Boolean = true
    override suspend fun clearLikedPosts() {}
    override suspend fun clearComments() {}
    override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> = emptyList()
    override suspend fun deleteAllDraftPosts(): Boolean = true
    override suspend fun deleteDraftPost(newId: String): Boolean = true
    override suspend fun clearLocalFriends() {}
    override suspend fun saveLoginActivityInfo(userId: String) {}
}

private class FakeUserRepository : UserRepository {
    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = null
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String): List<UserInstance>? = emptyList()
}

private class FakeNotificationRepository : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<NotificationInstance>? = emptyList()
    override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
    override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(user: UserInstance, notification: NotificationInstance) {}
    override suspend fun deleteAllNotifications(user: UserInstance): Result<Unit> = Result.success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class CommentViewModelTest {

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        interactor: FakeCommentInteractor = FakeCommentInteractor()
    ): Pair<CommentFeatureViewModel, FakeCommentInteractor> {
        val commonDb = FakeCommonDbRepository()
        val vm = CommentFeatureViewModel(
            commentInteractor = interactor,
            getUserUseCase = GetUserUseCase(FakeUserRepository()),
            saveLikedCommentsUseCase = SaveLikedCommentsUseCase(commonDb),
            saveNotificationToDatabaseUseCase = SaveNotificationToDatabaseUseCase(FakeNotificationRepository()),
            updateCommentCountForNewUseCase = UpdateCommentCountForNewUseCase(commonDb),
            updateReplyCountForCommentUseCase = UpdateReplyCountForCommentUseCase(commonDb),
            updateLikeCountForCommentUseCase = UpdateLikeCountForCommentUseCase(commonDb),
            updateLikeCountForSubCommentUseCase = UpdateLikeCountForSubCommentUseCase(commonDb),
            ioDispatcher = StandardTestDispatcher(scheduler)
        )
        return vm to interactor
    }

    @Test
    fun `updateMessage updates state`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)

        vm.updateMessage("hello")

        assertEquals("hello", vm.message)
    }

    @Test
    fun `updateMessage updates messageFlow StateFlow`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)

        vm.updateMessage("test message")
        advanceUntilIdle()

        assertEquals("test message", vm.messageFlow.value)
        assertEquals("test message", vm.message)
    }

    @Test
    fun `updateCommentBeReplied updates and clears selected comment`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)
        val comment = CommentInstance(id = "c1", posterName = "User")

        vm.updateCommentBeReplied(comment)
        assertEquals("c1", vm.commentBeReplied.value?.id)

        vm.updateCommentBeReplied(null)
        assertNull(vm.commentBeReplied.value)
    }

    @Test
    fun `getAllCommentsOfNew loads comments then clearCommentList clears state`() = runTest {
        val interactor = FakeCommentInteractor().apply {
            comments = listOf(
                CommentInstance(id = "c1", message = "one"),
                CommentInstance(id = "c2", message = "two")
            )
        }
        val (vm, _) = buildViewModel(testScheduler, interactor)

        vm.getAllCommentsOfNew("news-1")
        advanceUntilIdle()
        assertEquals(2, vm.allComments.value.size)

        vm.clearCommentList()
        assertEquals(0, vm.allComments.value.size)
    }
}
