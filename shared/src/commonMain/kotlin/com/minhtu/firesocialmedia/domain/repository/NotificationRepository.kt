package com.minhtu.firesocialmedia.domain.repository

import com.minhtu.firesocialmedia.domain.entity.notification.NotificationInstance

interface NotificationRepository {
    suspend fun getAllNotificationsOfUser(currentUserUid: String) : List<NotificationInstance>?
    suspend fun saveNotificationToDatabase(id : String,
                                           instance : ArrayList<NotificationInstance>)
    suspend fun deleteNotificationFromDatabase(
        id: String,
        notification: NotificationInstance
    )
}