package com.minhtu.firesocialmedia.presentation.notification

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.entity.user.notification.UserInstance
import com.minhtu.firesocialmedia.domain.interactor.home.NotificationInteractor
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetCurrentUserUidUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeNotificationsUserRepository : UserRepository {
    var currentUserUid: String? = "u1"

    override suspend fun getUser(userId: String, isCurrentUser: Boolean) = null
    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

private class FakeNotificationInteractor : NotificationInteractor {
    var notifications: List<NotificationInstance>? = emptyList()
    var deletedId: String? = null
    var deletedNotification: NotificationInstance? = null

    override suspend fun allNotificationsOf(userId: String): List<NotificationInstance>? = notifications
    override suspend fun saveNotificationToDatabase(id: String, instance: ArrayList<NotificationInstance>) {}
    override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {
        deletedId = id
        deletedNotification = notification
    }
    override suspend fun storeNotificationsToRoom(notifications: List<NotificationInstance>) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private fun buildViewModel(
        scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
        userRepo: FakeNotificationsUserRepository = FakeNotificationsUserRepository(),
        interactor: FakeNotificationInteractor = FakeNotificationInteractor()
    ): NotificationsViewModel {
        val dispatcher = StandardTestDispatcher(scheduler)
        return NotificationsViewModel(
            getCurrentUserUidUseCase = GetCurrentUserUidUseCase(userRepo),
            notificationInteractor = interactor,
            ioDispatcher = dispatcher
        )
    }

    @Test
    fun `getAllNotificationsOfUser populates list and marks success`() = runTest {
        val notifications = listOf(
            NotificationInstance(id = "n1", sender = "s1", type = NotificationType.LIKE),
            NotificationInstance(id = "n2", sender = "s2", type = NotificationType.COMMENT)
        )
        val interactor = FakeNotificationInteractor().apply { this.notifications = notifications }
        val vm = buildViewModel(testScheduler, interactor = interactor)

        vm.getAllNotificationsOfUser()
        advanceUntilIdle()

        assertTrue(vm.getAllNotificationsOfCurrentUser.value)
        assertEquals(2, vm.listNotificationOfCurrentUser.size)
        assertEquals(notifications, vm.allNotifications.value)
    }

    @Test
    fun `getAllNotificationsOfUser marks failure when interactor returns null`() = runTest {
        val interactor = FakeNotificationInteractor().apply { notifications = null }
        val vm = buildViewModel(testScheduler, interactor = interactor)

        vm.getAllNotificationsOfUser()
        advanceUntilIdle()

        assertFalse(vm.getAllNotificationsOfCurrentUser.value)
        assertTrue(vm.listNotificationOfCurrentUser.isEmpty())
    }

    @Test
    fun `getAllNotificationsOfUser does nothing when no current user`() = runTest {
        val userRepo = FakeNotificationsUserRepository().apply { currentUserUid = null }
        val vm = buildViewModel(testScheduler, userRepo = userRepo)

        vm.getAllNotificationsOfUser()
        advanceUntilIdle()

        assertFalse(vm.getAllNotificationsOfCurrentUser.value)
        assertTrue(vm.listNotificationOfCurrentUser.isEmpty())
    }

    @Test
    fun `removeNotificationInList removes matching notification`() = runTest {
        val vm = buildViewModel(testScheduler)
        val notification = NotificationInstance(id = "n1")
        vm.listNotificationOfCurrentUser.add(notification)

        vm.removeNotificationInList(notification)

        assertTrue(vm.listNotificationOfCurrentUser.isEmpty())
    }

    @Test
    fun `updateNotifications sets allNotifications state`() = runTest {
        val vm = buildViewModel(testScheduler)
        val notifications = arrayListOf(NotificationInstance(id = "n1"))

        vm.updateNotifications(notifications)

        assertEquals(notifications, vm.allNotifications.value)
    }

    @Test
    fun `deleteNotification removes from user and calls interactor`() = runTest {
        val interactor = FakeNotificationInteractor()
        val vm = buildViewModel(testScheduler, interactor = interactor)
        val notification = NotificationInstance(id = "n1")
        val user = UserInstance(uid = "u1", notifications = arrayListOf(notification))

        vm.deleteNotification(notification, user)

        assertTrue(user.notifications.isEmpty())
        assertEquals("u1", interactor.deletedId)
        assertEquals(notification, interactor.deletedNotification)
    }
}
