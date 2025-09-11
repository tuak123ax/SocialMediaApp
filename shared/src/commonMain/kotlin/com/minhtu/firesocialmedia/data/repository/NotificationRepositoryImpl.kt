package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.mapper.notification.toDomain
import com.minhtu.firesocialmedia.data.mapper.notification.toDto
import com.minhtu.firesocialmedia.data.mapper.user.toDTONotifications
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.domain.service.database.DatabaseService

class NotificationRepositoryImpl(
    private val databaseService: DatabaseService
) : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(
        path: String,
        currentUserUid: String
    ): List<NotificationInstance>? {
        return databaseService.getAllNotificationsOfUser(
            Constants.NOTIFICATION_PATH,
            currentUserUid)?.map { item -> item.toDomain() }
    }

    override suspend fun saveNotificationToDatabase(
        id: String,
        path: String,
        instance: ArrayList<NotificationInstance>
    ) {
        databaseService.saveNotificationToDatabase(
            id,
            path,
            instance.toDTONotifications())
    }

    override suspend fun deleteNotificationFromDatabase(
        id: String,
        path: String,
        notification: NotificationInstance
    ) {
        databaseService.deleteNotificationFromDatabase(
            id,
            path,
            notification.toDto())
    }
}