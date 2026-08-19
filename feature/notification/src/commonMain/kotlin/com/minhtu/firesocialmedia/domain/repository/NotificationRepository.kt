package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface NotificationRepository {
    suspend fun getAllNotificationsOfUser(currentUserUid: String) : List<NotificationInstance>?
    suspend fun saveNotificationToDatabase(id : String,
                                           instance : List<NotificationInstance>)
    suspend fun deleteNotificationFromDatabase(
        id: String,
        notification: NotificationInstance
    )

    suspend fun updateIsReadStatusOfNotification(userId : String,
                                                 notification: NotificationInstance)

    suspend fun deleteAllNotifications(userId : String): Result<Unit>
}
