package com.minhtu.firesocialmedia.domain.usecases

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationType
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.repository.news.NotificationNewsRepository
import com.minhtu.firesocialmedia.domain.repository.notification.UserRepository
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.notification.GetUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.home.StoreNotificationsToRoomUseCase
import com.minhtu.firesocialmedia.domain.usecases.local.StoreNotificationsLocalUseCase
import com.minhtu.firesocialmedia.domain.usecases.news.notification.FindNewByIdInDbUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteAllNotificationsUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.DeleteNotificationFromDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.GetAllNotificationOfUserUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.SaveNotificationToDatabaseUseCase
import com.minhtu.firesocialmedia.domain.usecases.notification.UpdateIsReadStatusOfNotificationUseCase
import com.minhtu.firesocialmedia.notification.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.notification.entity.news.NewsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeNotificationRepository : NotificationRepository {
    var allNotifications: List<NotificationInstance>? = emptyList()
    var deleteAllResult: Result<Unit> = Result.success(Unit)

    var savedId: String? = null
    var savedInstances: List<NotificationInstance>? = null
    var deletedId: String? = null
    var deletedNotification: NotificationInstance? = null
    var updatedUserId: String? = null
    var updatedNotification: NotificationInstance? = null

    override suspend fun getAllNotificationsOfUser(currentUserUid: String): List<NotificationInstance>? =
        allNotifications

    override suspend fun saveNotificationToDatabase(id: String, instance: List<NotificationInstance>) {
        savedId = id
        savedInstances = instance
    }

    override suspend fun deleteNotificationFromDatabase(id: String, notification: NotificationInstance) {
        deletedId = id
        deletedNotification = notification
    }

    override suspend fun updateIsReadStatusOfNotification(userId: String, notification: NotificationInstance) {
        updatedUserId = userId
        updatedNotification = notification
    }

    override suspend fun deleteAllNotifications(userId: String): Result<Unit> = deleteAllResult
}

private class FakeUserRepository : UserRepository {
    val users = hashMapOf<String, UserDTO>()
    var currentUserUid: String? = "current-uid"

    override suspend fun getUser(userId: String, isCurrentUser: Boolean): UserDTO? = users[userId]
    override suspend fun getCurrentUserUid(): String? = currentUserUid
}

private class FakeNotificationNewsRepository : NotificationNewsRepository {
    val news = hashMapOf<String, NewsInstance>()

    override suspend fun getNew(newId: String): NewsInstance? = news[newId]
}

private class FakeStoreNotificationsLocalUseCase : StoreNotificationsLocalUseCase {
    var stored: List<NotificationInstance>? = null

    override suspend fun invoke(notifications: List<NotificationInstance>) {
        stored = notifications
    }
}

class UpdateIsReadStatusOfNotificationUseCaseTest {
    @Test
    fun `invoke delegates to repository with user id and notification`() = runTest {
        val repository = FakeNotificationRepository()
        val useCase = UpdateIsReadStatusOfNotificationUseCase(repository)
        val notification = NotificationInstance(id = "n1", sender = "u1", type = NotificationType.LIKE)

        useCase.invoke("u1", notification)

        assertEquals("u1", repository.updatedUserId)
        assertEquals(notification, repository.updatedNotification)
    }
}

class DeleteAllNotificationsUseCaseTest {
    @Test
    fun `invoke returns success result from repository`() = runTest {
        val repository = FakeNotificationRepository().apply { deleteAllResult = Result.success(Unit) }
        val useCase = DeleteAllNotificationsUseCase(repository)

        val result = useCase.invoke("u1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure result from repository`() = runTest {
        val repository = FakeNotificationRepository().apply {
            deleteAllResult = Result.failure(IllegalStateException("network timeout"))
        }
        val useCase = DeleteAllNotificationsUseCase(repository)

        val result = useCase.invoke("u1")

        assertTrue(result.isFailure)
        assertEquals("network timeout", result.exceptionOrNull()?.message)
    }
}

class DeleteNotificationFromDatabaseUseCaseTest {
    @Test
    fun `invoke delegates to repository`() = runTest {
        val repository = FakeNotificationRepository()
        val useCase = DeleteNotificationFromDatabaseUseCase(repository)
        val notification = NotificationInstance(id = "n1")

        useCase.invoke("u1", notification)

        assertEquals("u1", repository.deletedId)
        assertEquals(notification, repository.deletedNotification)
    }
}

class GetAllNotificationOfUserUseCaseTest {
    @Test
    fun `invoke returns notifications from repository`() = runTest {
        val notifications = listOf(NotificationInstance(id = "n1"), NotificationInstance(id = "n2"))
        val repository = FakeNotificationRepository().apply { allNotifications = notifications }
        val useCase = GetAllNotificationOfUserUseCase(repository)

        val result = useCase.invoke("u1")

        assertEquals(notifications, result)
    }

    @Test
    fun `invoke returns null when repository returns null`() = runTest {
        val repository = FakeNotificationRepository().apply { allNotifications = null }
        val useCase = GetAllNotificationOfUserUseCase(repository)

        val result = useCase.invoke("u1")

        assertNull(result)
    }
}

class SaveNotificationToDatabaseUseCaseTest {
    @Test
    fun `invoke delegates to repository`() = runTest {
        val repository = FakeNotificationRepository()
        val useCase = SaveNotificationToDatabaseUseCase(repository)
        val instances = arrayListOf(NotificationInstance(id = "n1"))

        useCase.invoke("u1", instances)

        assertEquals("u1", repository.savedId)
        assertEquals(instances, repository.savedInstances)
    }
}

class StoreNotificationsToRoomUseCaseTest {
    @Test
    fun `invoke delegates to local use case`() = runTest {
        val localUseCase = FakeStoreNotificationsLocalUseCase()
        val useCase = StoreNotificationsToRoomUseCase(localUseCase)
        val notifications = listOf(NotificationInstance(id = "n1"))

        useCase.invoke(notifications)

        assertEquals(notifications, localUseCase.stored)
    }
}

class GetCurrentUserUidUseCaseTest {
    @Test
    fun `invoke returns uid from repository`() = runTest {
        val repository = FakeUserRepository().apply { currentUserUid = "abc" }
        val useCase = GetCurrentUserUidUseCase(repository)

        assertEquals("abc", useCase.invoke())
    }

    @Test
    fun `invoke returns null when no current user`() = runTest {
        val repository = FakeUserRepository().apply { currentUserUid = null }
        val useCase = GetCurrentUserUidUseCase(repository)

        assertNull(useCase.invoke())
    }
}

class GetUserUseCaseTest {
    @Test
    fun `invoke maps dto to domain instance`() = runTest {
        val repository = FakeUserRepository().apply {
            users["u1"] = UserDTO(uid = "u1", name = "User One", email = "u1@test.com")
        }
        val useCase = GetUserUseCase(repository)

        val user = useCase.invoke("u1", false)

        assertEquals("u1", user?.uid)
        assertEquals("User One", user?.name)
        assertEquals("u1@test.com", user?.email)
    }

    @Test
    fun `invoke returns null when user not found`() = runTest {
        val repository = FakeUserRepository()
        val useCase = GetUserUseCase(repository)

        val user = useCase.invoke("missing", false)

        assertNull(user)
    }
}

class FindNewByIdInDbUseCaseTest {
    @Test
    fun `invoke returns matching news instance`() = runTest {
        val repository = FakeNotificationNewsRepository().apply {
            news["news1"] = NewsInstance(id = "news1", message = "hello")
        }
        val useCase = FindNewByIdInDbUseCase(repository)

        val result = useCase.invoke("news1")

        assertEquals("news1", result.id)
        assertEquals("hello", result.message)
    }

    @Test
    fun `invoke returns default instance when not found`() = runTest {
        val repository = FakeNotificationNewsRepository()
        val useCase = FindNewByIdInDbUseCase(repository)

        val result = useCase.invoke("missing")

        assertEquals(NewsInstance(), result)
        assertFalse(result.id == "missing")
    }
}
