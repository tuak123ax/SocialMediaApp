package com.minhtu.firesocialmedia.presentation.notification

import androidx.compose.runtime.mutableStateListOf
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.notification.UserInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.news.NotificationNewsRepository
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeUserRepository : UserRepository {
    val users = hashMapOf<String, UserDTO>()

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = null
}

private class FakeNotificationRepository : NotificationRepository {
    var deleteAllResult: Result<Unit> = Result.success(Unit)

    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<NotificationInstance>? = emptyList()
    override suspend fun saveNotificationToDatabase(id: String, instance: List<NotificationInstance>) {}
    override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {}
    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: NotificationInstance) {}
    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = deleteAllResult
}

private class FakeNotificationNewsRepository : NotificationNewsRepository {
    val news = hashMapOf<String, NewsInstance>()

    override suspend fun getNew(newId: String): NewsInstance? = news[newId]
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private fun buildViewModel(
        scheduler: TestCoroutineScheduler,
        userRepo: FakeUserRepository = FakeUserRepository(),
        notificationRepo: FakeNotificationRepository = FakeNotificationRepository(),
        newsRepo: FakeNotificationNewsRepository = FakeNotificationNewsRepository()
    ): NotificationViewModel {
        val dispatcher = StandardTestDispatcher(scheduler)
        return NotificationViewModel(
            getUserUseCase = GetUserUseCase(userRepo),
            findNewByIdInDbUseCase = FindNewByIdInDbUseCase(newsRepo),
            updateIsReadStatusOfNotificationUseCase = UpdateIsReadStatusOfNotificationUseCase(notificationRepo),
            deleteAllNotificationsUseCase = DeleteAllNotificationsUseCase(notificationRepo),
            ioDispatcher = dispatcher
        )
    }

    @Test
    fun `checkUsersInCacheAndGetMore loads missing users and marks ready`() = runTest {
        val userRepo = FakeUserRepository().apply {
            users["u2"] = UserDTO(uid = "u2", name = "User 2")
        }
        val vm = buildViewModel(testScheduler, userRepo)

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
    fun `checkUsersInCacheAndGetMore does not overwrite already cached users`() = runTest {
        val vm = buildViewModel(testScheduler)

        val loadedUsersCache = hashMapOf<String, UserInstance?>(
            "u1" to UserInstance(uid = "u1", name = "User 1")
        )
        val notifications = mutableStateListOf(
            NotificationInstance(id = "n1", sender = "u1", type = NotificationType.LIKE)
        )

        vm.checkUsersInCacheAndGetMore(loadedUsersCache, notifications)
        advanceUntilIdle()

        assertTrue(vm.getNeededUsersStatus.value)
        assertEquals("User 1", vm.findLoadedUserInSet("u1")?.name)
    }

    @Test
    fun `findLoadedUserInSet returns null for unknown user`() = runTest {
        val vm = buildViewModel(testScheduler)

        assertNull(vm.findLoadedUserInSet("unknown"))
    }

    @Test
    fun `requestFindNewById returns news instance from use case`() = runTest {
        val newsRepo = FakeNotificationNewsRepository().apply {
            news["news1"] = NewsInstance(id = "news1", message = "hi")
        }
        val vm = buildViewModel(testScheduler, newsRepo = newsRepo)

        val result = vm.requestFindNewById("news1")

        assertEquals("news1", result?.id)
    }

    @Test
    fun `updateIsReadStatusOfNotification replaces notification and persists`() = runTest {
        val notificationRepo = FakeNotificationRepository()
        val vm = buildViewModel(testScheduler, notificationRepo = notificationRepo)

        val original = NotificationInstance(id = "n1", sender = "u1", beRead = false)
        val user = UserInstance(uid = "u1", notifications = arrayListOf(original))
        val updated = original.copy(beRead = true)

        vm.updateIsReadStatusOfNotification(updated, user)

        assertTrue(user.notifications.first { it.id == "n1" }.beRead)
    }

    @Test
    fun `deleteAllNotifications success updates status then reset clears it`() = runTest {
        val vm = buildViewModel(testScheduler)

        vm.deleteAllNotifications("u1")
        advanceUntilIdle()

        val status = vm.deleteAllNotificationsStatus.value
        assertNotNull(status)
        assertTrue(status.success)

        vm.resetDeleteAllNotificationsStatus()
        assertNull(vm.deleteAllNotificationsStatus.value)
    }

    @Test
    fun `deleteAllNotifications network failure maps to friendly message`() = runTest {
        val notificationRepo = FakeNotificationRepository().apply {
            deleteAllResult = Result.failure(IllegalStateException("network timeout"))
        }
        val vm = buildViewModel(testScheduler, notificationRepo = notificationRepo)

        vm.deleteAllNotifications("u1")
        advanceUntilIdle()

        val status = vm.deleteAllNotificationsStatus.value
        assertNotNull(status)
        assertFalse(status.success)
        assertEquals("Please recheck your network!", status.message)
    }

    @Test
    fun `deleteAllNotifications other failure maps to generic message`() = runTest {
        val notificationRepo = FakeNotificationRepository().apply {
            deleteAllResult = Result.failure(IllegalStateException("permission denied"))
        }
        val vm = buildViewModel(testScheduler, notificationRepo = notificationRepo)

        vm.deleteAllNotifications("u1")
        advanceUntilIdle()

        val status = vm.deleteAllNotificationsStatus.value
        assertNotNull(status)
        assertFalse(status.success)
        assertEquals("Cannot delete notifications. Please try again!", status.message)
    }
}
