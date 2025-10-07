package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.mapper.notification.toDomain
import com.minhtu.firesocialmedia.data.remote.mapper.notification.toDto
import com.minhtu.firesocialmedia.data.remote.mapper.user.toDTONotifications
import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.repository.NotificationRepository
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService

class NotificationRepositoryImpl(
    private val databaseService: DatabaseService
) : NotificationRepository {
    override suspend fun getAllNotificationsOfUser(
        currentUserUid: String
    ): List<NotificationInstance>? {
        return databaseService.getAllNotificationsOfUser(
            DataConstant.NOTIFICATION_PATH,
            currentUserUid).orEmpty().map { it.toDomain() }
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