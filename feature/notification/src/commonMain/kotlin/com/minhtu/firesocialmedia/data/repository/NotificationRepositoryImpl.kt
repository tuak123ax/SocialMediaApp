package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.service.room.NotificationRoomService
import com.minhtu.firesocialmedia.constants.notification.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.notification.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.notification.toDto
import com.minhtu.firesocialmedia.data.remote.service.database.NotificationRemoteService
import com.minhtu.firesocialmedia.network.notification.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.first

class NotificationRepositoryImpl(
    private val remoteService: NotificationRemoteService,
    private val localDatabaseService : NotificationRoomService,
    private val networkMonitor: NetworkMonitor
) : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(
        currentUserUid: String
    ): List<NotificationInstance> {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            val notifications = remoteService.getAllNotificationsOfUser(
                DataConstant.NOTIFICATION_PATH,
                currentUserUid).orEmpty().map { it.toDomain() }
            localDatabaseService.storeNotificationsToRoom(notifications)
            return notifications
        } else {
            return localDatabaseService.getAllNotifications()
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        instance: List<NotificationInstance>
    ) {
        remoteService.saveNotificationToDatabase(
            id,
            DataConstant.USER_PATH,
            ArrayList(instance.map { it.toDto() }))
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        notification: NotificationInstance
    ) {
        remoteService.deleteNotificationFromDatabase(
            id,
            DataConstant.USER_PATH,
            notification.toDto())
    }

    override suspend fun updateIsReadStatusOfNotification(userId : String,
                                                          notification: NotificationInstance) {
        remoteService.updateIsReadStatusOfNotification(
            userId,
            notification.id,
            DataConstant.USER_PATH,
            DataConstant.NOTIFICATION_PATH
        )
    }

    override suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        val isOnline = networkMonitor.isOnline.first()
        return if(isOnline) {
            remoteService.deleteAllNotifications(
                userId,
                DataConstant.USER_PATH,
                DataConstant.NOTIFICATION_PATH
            )
        } else {
            Result.failure(Throwable("No network!"))
        }
    }
}
