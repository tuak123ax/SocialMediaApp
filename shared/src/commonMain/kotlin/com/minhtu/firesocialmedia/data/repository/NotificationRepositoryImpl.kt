package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.local.mapper.room.toNotificationEntity
import com.minhtu.firesocialmedia.data.local.mapper.room.toNotificationInstance
import com.minhtu.firesocialmedia.data.local.service.room.RoomService
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.notification.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.notification.toDto
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDTONotifications
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.core.NetworkMonitor
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.first

class NotificationRepositoryImpl(
    private val databaseService: DatabaseService,
    private val localDatabaseService : RoomService,
    private val networkMonitor: NetworkMonitor
) : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(
        currentUserUid: String
    ): List<NotificationInstance>? {
        val isOnline = networkMonitor.isOnline.first()
        if(isOnline) {
            val notifications = databaseService.getAllNotificationsOfUser(
                DataConstant.NOTIFICATION_PATH,
                currentUserUid).orEmpty().map { it.toDomain() }
            localDatabaseService.storeNotificationsToRoom(notifications.toNotificationEntity())
            return notifications
        } else {
            return localDatabaseService.getAllNotifications().toNotificationInstance()
        }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        instance: ArrayList<NotificationInstance>
    ) {
        databaseService.saveNotificationToDatabase(
            id,
            DataConstant.USER_PATH,
            instance.toDTONotifications())
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        notification: NotificationInstance
    ) {
        databaseService.deleteNotificationFromDatabase(
            id,
            DataConstant.USER_PATH,
            notification.toDto())
    }
}