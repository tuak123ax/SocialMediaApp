package com.minhtu.firesocialmedia.feature.notification

import androidx.compose.runtime.mutableStateListOf
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.core.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.core.domain.repository.NewsRepository
import com.minhtu.firesocialmedia.core.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.core.domain.repository.UserRepository
import com.minhtu.firesocialmedia.core.domain.usecases.common.GetUserUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.core.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.feature.notification.presentation.notification.NotificationViewModel
import com.minhtu.firesocialmedia.feature.notification.presentation.setting.notificationconfigs.NotificationConfigsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeUserRepository : UserRepository {
    val users = hashMapOf<String, UserInstance>()

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserInstance? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
    override suspend fun updateFCMTokenForCurrentUser(user: UserInstance) {}
    override suspend fun searchUserByName(name: String): List<UserInstance>? = emptyList()
}

private class FakeNotificationRepository : NotificationRepository {
    var deleteAllResult: Result<Unit> = Result.success(Unit)

    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<NotificationInstance>? = emptyList()
    override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
    override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(user: UserInstance, notification: NotificationInstance) {}
    override suspend fun deleteAllNotifications(user: UserInstance): Result<Unit> = deleteAllResult
}

private class FakeNewsRepository : NewsRepository {
    override suspend fun getNew(newId: String): NewsInstance? = NewsInstance(id = newId)
    override suspend fun getLatestNews(number: Int, lastTimePosted: Double?, lastKey: String?) = null
    override suspend fun deleteNewsFromDatabase(new: NewsInstance) {}
    override suspend fun deletePollFromDatabase(newsId: String, pollId: String, groupId: String): Boolean = true
    override suspend fun updateNewsFromDatabase(newContent: String, newImage: String, newVideo: String, new: NewsInstance): Boolean = true
    override suspend fun fetchPoll(pollId: String) = null
    override suspend fun loadMyVotes(pollId: String, userId: String): List<Int> = emptyList()
    override suspend fun loadAllVoters(pollId: String): Map<String, List<Int>> = emptyMap()
    override suspend fun submitVote(pollId: String, userId: String, selectedIndices: List<Int>, previousIndices: List<Int>): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        userRepo: FakeUserRepository = FakeUserRepository(),
        notificationRepo: FakeNotificationRepository = FakeNotificationRepository()
    ): Pair<NotificationViewModel, FakeNotificationRepository> {
        val dispatcher = StandardTestDispatcher(scheduler)
        val vm = NotificationViewModel(
            getUserUseCase = GetUserUseCase(userRepo),
            findNewByIdInDbUseCase = FindNewByIdInDbUseCase(FakeNewsRepository()),
            updateIsReadStatusOfNotificationUseCase = UpdateIsReadStatusOfNotificationUseCase(notificationRepo),
            deleteAllNotificationsUseCase = DeleteAllNotificationsUseCase(notificationRepo),
            ioDispatcher = dispatcher
        )
        return vm to notificationRepo
    }

    @Test
    fun `checkUsersInCacheAndGetMore loads missing users and marks ready`() = runTest {
        val userRepo = FakeUserRepository().apply {
            users["u2"] = UserInstance(uid = "u2", name = "User 2")
        }
        val (vm, _) = buildViewModel(testScheduler, userRepo)

        val loadedUsersCache = hashMapOf<String, UserInstance?>(
            "u1" to UserInstance(uid = "u1", name = "User 1")
        )
        val notifications = mutableStateListOf(
            NotificationInstance(
                id = "n1",
                content = "",
                avatar = "",
                sender = "u1",
                timeSend = 1L,
                type = NotificationType.LIKE,
                relatedInfo = ""
            ),
            NotificationInstance(
                id = "n2",
                content = "",
                avatar = "",
                sender = "u2",
                timeSend = 2L,
                type = NotificationType.COMMENT,
                relatedInfo = ""
            )
        )

        vm.checkUsersInCacheAndGetMore(loadedUsersCache, notifications)
        advanceUntilIdle()

        assertTrue(vm.getNeededUsersStatus.value)
        assertNotNull(vm.findLoadedUserInSet("u2"))
        assertNotNull(loadedUsersCache["u2"])
    }

    @Test
    fun `deleteAllNotifications success updates status then reset clears it`() = runTest {
        val (vm, _) = buildViewModel(testScheduler)

        vm.deleteAllNotifications(UserInstance(uid = "u1"))
        advanceUntilIdle()

        val status = vm.deleteAllNotificationsStatus.value
        assertNotNull(status)
        assertTrue(status.success)

        vm.resetDeleteAllNotificationsStatus()
        assertNull(vm.deleteAllNotificationsStatus.value)
    }

    @Test
    fun `deleteAllNotifications network failure maps to friendly message`() = runTest {
        val fakeNotificationRepository = FakeNotificationRepository().apply {
            deleteAllResult = Result.failure(IllegalStateException("network timeout"))
        }
        val (vm, _) = buildViewModel(testScheduler, notificationRepo = fakeNotificationRepository)

        vm.deleteAllNotifications(UserInstance(uid = "u1"))
        advanceUntilIdle()

        val status = vm.deleteAllNotificationsStatus.value
        assertNotNull(status)
        assertFalse(status.success)
        assertEquals("Please recheck your network!", status.message)
    }
}

class NotificationConfigsViewModelTest {

    @Test
    fun `updateNotification toggles a specific notification type`() {
        val vm = NotificationConfigsViewModel()

        val initial = vm.notificationSettings.value[NotificationType.LIKE]
        assertEquals(true, initial)

        vm.updateNotification(NotificationType.LIKE, false)
        assertEquals(false, vm.notificationSettings.value[NotificationType.LIKE])

        vm.updateNotification(NotificationType.LIKE, true)
        assertEquals(true, vm.notificationSettings.value[NotificationType.LIKE])
    }
}

