package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance

interface NotificationRepository {
    suspend fun getAllNotificationsOfUser(currentUserUid: String) : List<NotificationInstance>?
    suspend fun saveNotificationToDatabase(id : String,
                                           instance : ArrayList<NotificationInstance>)
    suspend fun deleteNotificationFromDatabase(
        id: String,
        notification: NotificationInstance
    )

    suspend fun updateIsReadStatusOfNotification(user : UserInstance,
                                                 notification: NotificationInstance)

    suspend fun deleteAllNotifications(user : UserInstance): Result<Unit>
}